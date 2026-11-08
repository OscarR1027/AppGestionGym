import test from 'node:test';
import assert from 'node:assert/strict';
import { readFile, readdir } from 'node:fs/promises';
import { PGlite } from '@electric-sql/pglite';
import { drizzle } from 'drizzle-orm/pglite';
import { eq } from 'drizzle-orm';
import type { Database } from '../db/index.js';
import * as schema from '../db/schema.js';
import { resources, listSales, listAffiliations, saveSale, saveAffiliation, cancelSale, cancelAffiliation, special } from '../lib/gym.js';

async function fixture() {
  const engine = new PGlite();
  const directory = 'netlify/database/migrations';
  for (const folder of (await readdir(directory)).sort()) {
    if (!/^\d+_/.test(folder)) continue;
    await engine.exec(await readFile(`${directory}/${folder}/migration.sql`, 'utf8'));
  }
  const db = drizzle({ client: engine, schema }) as unknown as Database;
  const user = await resources(db, 'users', 'POST', undefined, { name: 'Cliente de prueba', mail: 'client@example.test', phoneNumber: '0312345678', address: 'Dirección', documentNumber: '123', role: 'USER' });
  const product = await resources(db, 'products', 'POST', undefined, { nameProduct: 'Agua', value: 12.35, amount: 10 });
  return { engine, db, user, product };
}

test('ventas: factura con precio histórico, inventario, edición, rollback y anulación', async () => {
  const { engine, db, user, product } = await fixture();
  try {
    const sale = (await saveSale(db, { userId: user.id, paymentMethod: 'CASH', products: [{ productId: product.id, amount: 2 }, { productId: product.id, amount: 1 }] }))!;
    assert.equal(sale.totalValue, 37.05);
    assert.equal(sale.products.length, 1);
    assert.equal((await resources(db, 'products', 'GET', product.id, {})).amount, 7);
    await resources(db, 'products', 'PUT', product.id, { nameProduct: 'Agua nueva', value: 20, amount: 7 });
    const invoice = (await listSales(db))[0];
    assert.equal(invoice.products[0].unitPrice, 12.35);
    assert.equal(invoice.products[0].productName, 'Agua');
    await assert.rejects(saveSale(db, { userId: user.id, paymentMethod: 'CARD', products: [{ productId: product.id, amount: 99 }] }, sale.id), /Existencias insuficientes/);
    assert.equal((await resources(db, 'products', 'GET', product.id, {})).amount, 7);
    assert.equal((await listSales(db))[0].totalValue, 37.05);
    const edited = (await saveSale(db, { userId: user.id, paymentMethod: 'CARD', products: [{ productId: product.id, amount: 4 }] }, sale.id))!;
    assert.equal(edited.totalValue, 80);
    assert.equal((await resources(db, 'products', 'GET', product.id, {})).amount, 6);
    assert.deepEqual(await special(db, 'sales', 'monthly-revenue'), { totalValue: 80 });
    await cancelSale(db, sale.id);
    assert.equal((await resources(db, 'products', 'GET', product.id, {})).amount, 10);
    await assert.rejects(cancelSale(db, sale.id), /No se encontró/);
    assert.equal((await resources(db, 'products', 'GET', product.id, {})).amount, 10);
    assert.equal((await listSales(db)).length, 0);
  } finally { await engine.close(); }
});

test('afiliaciones: planes con servicios, entrenadores, renovaciones y conservación del cobro', async () => {
  const { engine, db, user } = await fixture();
  try {
    const service = await resources(db, 'gymservice', 'POST', undefined, { serviceName: 'Musculación' });
    const plan = await resources(db, 'typeplan', 'POST', undefined, { namePlan: 'Mensual', value: 100, serviceIds: [service.idService] });
    const coach = await resources(db, 'coachs', 'POST', undefined, { nameCoach: 'Entrenador' });
    assert.equal(plan.services[0].serviceName, 'Musculación');
    const payload = { userId: user.id, typePlanId: plan.idTypePlan, affiliationDate: '2026-11-08', endAffiliation: '2026-12-08', paymentMethod: 'TRANSFER', coaches: [{ coachId: coach.idCoach, startDate: '2026-11-08', endDate: '2026-12-08' }] };
    const first = (await saveAffiliation(db, payload))!;
    const renewal = (await saveAffiliation(db, { ...payload, affiliationDate: '2026-12-09', endAffiliation: '2027-01-09', coaches: [] }))!;
    assert.equal(first.renewal, false);
    assert.equal(first.coaches[0].coachName, 'Entrenador');
    assert.equal(renewal.renewal, true);
    await resources(db, 'typeplan', 'PUT', plan.idTypePlan, { namePlan: 'Mensual', value: 200, serviceIds: [] });
    const updated = (await saveAffiliation(db, payload, first.id))!;
    assert.equal(updated.paidValue, 100);
    await assert.rejects(saveAffiliation(db, { ...payload, coaches: [{ coachId: coach.idCoach, startDate: '2026-10-08', endDate: '2026-12-08' }] }), /fechas/);
    await assert.rejects(resources(db, 'users', 'DELETE', user.id, {}));
    await cancelAffiliation(db, renewal.id);
    assert.equal((await listAffiliations(db)).find(a => a.id === renewal.id)!.invoiceCancelled, true);
    await assert.rejects(saveAffiliation(db, payload, renewal.id), /anulada/);
    await cancelAffiliation(db, first.id);
    assert.equal((await listAffiliations(db)).length, 1);
    assert.equal((await db.select().from(schema.affiliationCoaches).where(eq(schema.affiliationCoaches.affiliationId, first.id))).length, 0);
  } finally { await engine.close(); }
});

test('validaciones y rollback de referencias inválidas', async () => {
  const { engine, db, user, product } = await fixture();
  try {
    await assert.rejects(saveSale(db, { userId: user.id, paymentMethod: 'CASH', products: [{ productId: product.id, amount: 0 }] }), /Cantidad/);
    await assert.rejects(resources(db, 'products', 'POST', undefined, { nameProduct: 'Inválido', value: -1, amount: 1 }), /Precio/);
    await assert.rejects(resources(db, 'products', 'POST', undefined, { nameProduct: 'Inválido', value: 1.234, amount: 1 }), /decimales/);
    await assert.rejects(resources(db, 'typeplan', 'POST', undefined, { namePlan: 'Inválido', value: 10, serviceIds: [9999] }));
    assert.deepEqual(await resources(db, 'typeplan', 'GET', undefined, {}), []);
    assert.equal((await resources(db, 'users', 'GET', user.id, {})).phoneNumber, '0312345678');
    await assert.rejects(resources(db, 'users', 'GET', 9999, {}), /No se encontró/);
  } finally { await engine.close(); }
});
