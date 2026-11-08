import { and, eq, gte, lte, asc, sql } from 'drizzle-orm';
import type { AnyPgTable, AnyPgColumn } from 'drizzle-orm/pg-core';
import type { Database } from '../db/index.js';
import * as s from '../db/schema.js';
export class ApiError extends Error { constructor(public status: number, message: string) { super(message); } }
export function required(value: unknown, label: string): string {
  if (typeof value !== 'string' || !value.trim() || value.length > 10000) throw new ApiError(400, `${label}: introduce un valor válido.`);
  return value.trim();
}
export function number(value: unknown, label: string, integer = false, min = 0): number {
  if (value === '' || value === null || typeof value === 'boolean' || value === undefined) throw new ApiError(400, `${label}: valor inválido.`);
  const n = Number(value);
  if (!Number.isFinite(n) || n < min || n > 999999999999 || (integer && (!Number.isSafeInteger(n) || n > 2147483647))) throw new ApiError(400, `${label}: valor inválido.`);
  return n;
}
export function id(value: unknown) { return number(value, 'ID', true, 1); }
function money(value: unknown) { const n = number(value, 'Precio'); if (Math.abs(n * 100 - Math.round(n * 100)) > 0.0001) throw new ApiError(400, 'El precio admite dos decimales.'); return n; }
function date(value: unknown): string {
  const v = required(value, 'Fecha');
  if (!/^\d{4}-\d{2}-\d{2}$/.test(v) || !Number.isFinite(Date.parse(v)) || new Date(v).toISOString().slice(0, 10) !== v) throw new ApiError(400, 'Fecha inválida.');
  return v;
}
export function today() { return new Intl.DateTimeFormat('en-CA', { timeZone: 'America/Bogota', year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date()); }
function payment(value: unknown) { if (!['CASH', 'CARD', 'TRANSFER'].includes(String(value))) throw new ApiError(400, 'Selecciona una forma de pago válida.'); return String(value); }
function array(value: unknown): any[] { if (value == null) return []; if (!Array.isArray(value) || value.length > 100) throw new ApiError(400, 'Lista inválida o demasiado extensa.'); return value; }
async function one<T>(rows: T[]) { if (!rows.length) throw new ApiError(404, 'No se encontró el registro.'); return rows[0]; }
export async function listAffiliations(db: Database) {
  const rows = await db.select({ affiliation: s.affiliations, user: s.users, plan: s.plans }).from(s.affiliations).innerJoin(s.users, eq(s.users.id, s.affiliations.userId)).innerJoin(s.plans, eq(s.plans.idTypePlan, s.affiliations.typePlanId)).orderBy(asc(s.affiliations.id));
  const assigned = await db.select({ assignment: s.affiliationCoaches, coach: s.coaches }).from(s.affiliationCoaches).innerJoin(s.coaches, eq(s.coaches.idCoach, s.affiliationCoaches.coachId));
  return rows.map(({ affiliation: a, user: u, plan: p }) => ({ ...a, userName: u.name, userDocumentNumber: u.documentNumber, userPhoneNumber: u.phoneNumber, typePlanName: p.namePlan, coaches: assigned.filter(x => x.assignment.affiliationId === a.id).map(x => ({ coachId: x.coach.idCoach, coachName: x.coach.nameCoach, startDate: x.assignment.startDate, endDate: x.assignment.endDate })) }));
}
export async function listSales(db: Database) {
  const rows = await db.select({ sale: s.sales, user: s.users }).from(s.sales).innerJoin(s.users, eq(s.users.id, s.sales.userId)).orderBy(asc(s.sales.id));
  const lines = await db.select().from(s.saleProducts);
  return rows.map(({ sale, user }) => ({ ...sale, userName: user.name, userDocumentNumber: user.documentNumber, userPhoneNumber: user.phoneNumber, products: lines.filter(x => x.saleId === sale.id) }));
}
export async function listPlans(db: Database) {
  const plans = await db.select().from(s.plans).orderBy(asc(s.plans.idTypePlan));
  const included = await db.select({ relation: s.planServices, service: s.services }).from(s.planServices).innerJoin(s.services, eq(s.services.idService, s.planServices.serviceId));
  return plans.map(p => ({ ...p, services: included.filter(x => x.relation.planId === p.idTypePlan).map(x => x.service) }));
}
export async function saveAffiliation(db: Database, body: any, recordId?: number) {
  const start = date(body.affiliationDate || today()), end = date(body.endAffiliation);
  if (end < start) throw new ApiError(400, 'El vencimiento no puede ser anterior al inicio.');
  const userId = id(body.userId), typePlanId = id(body.typePlanId), paymentMethod = payment(body.paymentMethod);
  const assignments = array(body.coaches).map(c => ({ coachId: id(c.coachId), startDate: date(c.startDate || start), endDate: date(c.endDate || end) }));
  if (new Set(assignments.map(x => x.coachId)).size !== assignments.length || assignments.some(c => c.startDate < start || c.endDate > end || c.endDate < c.startDate)) throw new ApiError(400, 'Revisa los entrenadores y sus fechas de asignación.');
  await db.transaction(async tx => {
    // Lock the customer to serialize simultaneous renewals for the same membership.
    await one(await tx.select().from(s.users).where(eq(s.users.id, userId)).for('update'));
    const plan = await one(await tx.select().from(s.plans).where(eq(s.plans.idTypePlan, typePlanId)));
    const old = recordId ? await one(await tx.select().from(s.affiliations).where(eq(s.affiliations.id, recordId)).for('update')) : null;
    if (old?.invoiceCancelled) throw new ApiError(409, 'La factura está anulada.');
    if (old && old.userId !== userId) throw new ApiError(400, 'Una afiliación existente no puede cambiar de cliente.');
    const history = await tx.select({ id: s.affiliations.id }).from(s.affiliations).where(eq(s.affiliations.userId, userId));
    const values = { userId, typePlanId, affiliationDate: start, endAffiliation: end, paymentMethod, paidValue: old?.paidValue ?? plan.value, renewal: old?.renewal ?? history.length > 0 };
    const [saved] = old ? await tx.update(s.affiliations).set(values).where(eq(s.affiliations.id, recordId!)).returning() : await tx.insert(s.affiliations).values(values).returning();
    await tx.delete(s.affiliationCoaches).where(eq(s.affiliationCoaches.affiliationId, saved.id));
    if (assignments.length) await tx.insert(s.affiliationCoaches).values(assignments.map(c => ({ ...c, affiliationId: saved.id })));
    recordId = saved.id;
  });
  return (await listAffiliations(db)).find(x => x.id === recordId);
}
export async function saveSale(db: Database, body: any, recordId?: number) {
  const userId = id(body.userId), paymentMethod = payment(body.paymentMethod);
  const totals = new Map<number, number>();
  for (const line of array(body.products)) { const productId = id(line.productId); totals.set(productId, (totals.get(productId) || 0) + number(line.amount, 'Cantidad', true, 1)); }
  if (!totals.size) throw new ApiError(400, 'Agrega al menos un producto.');
  await db.transaction(async tx => {
    await one(await tx.select().from(s.users).where(eq(s.users.id, userId)));
    const old = recordId ? await one(await tx.select().from(s.sales).where(eq(s.sales.id, recordId)).for('update')) : null;
    const oldLines = old ? await tx.select().from(s.saleProducts).where(eq(s.saleProducts.saleId, old.id)) : [];
    // Always lock products in ID order to prevent competing sales overselling or deadlocking.
    const productIds = [...new Set([...totals.keys(), ...oldLines.map(l => l.productId)])].sort((a,b) => a-b);
    const locked = new Map<number, typeof s.products.$inferSelect>();
    for (const productId of productIds) locked.set(productId, await one(await tx.select().from(s.products).where(eq(s.products.id, productId)).for('update')));
    for (const line of oldLines) locked.get(line.productId)!.amount += line.amount;
    const lines = [...totals].map(([productId, amount]) => {
      const product = locked.get(productId)!;
      if (product.amount < amount) throw new ApiError(409, `Existencias insuficientes de ${product.nameProduct}.`);
      product.amount -= amount;
      return { productId, amount, productName: product.nameProduct, unitPrice: product.value, subtotal: Math.round(product.value * 100) * amount / 100 };
    });
    const totalValue = lines.reduce((sum, l) => sum + Math.round(l.subtotal * 100), 0) / 100;
    const values = { userId, paymentMethod, totalValue, saleDate: old?.saleDate ?? today() };
    const [saved] = old ? await tx.update(s.sales).set(values).where(eq(s.sales.id, old.id)).returning() : await tx.insert(s.sales).values(values).returning();
    for (const [productId, product] of locked) await tx.update(s.products).set({ amount: product.amount }).where(eq(s.products.id, productId));
    await tx.delete(s.saleProducts).where(eq(s.saleProducts.saleId, saved.id));
    await tx.insert(s.saleProducts).values(lines.map(l => ({ ...l, saleId: saved.id })));
    recordId = saved.id;
  });
  return (await listSales(db)).find(x => x.id === recordId);
}
export async function cancelSale(db: Database, saleId: number) {
  await db.transaction(async tx => {
    await one(await tx.select().from(s.sales).where(eq(s.sales.id, saleId)).for('update'));
    const lines = await tx.select().from(s.saleProducts).where(eq(s.saleProducts.saleId, saleId)).orderBy(asc(s.saleProducts.productId));
    for (const line of lines) await tx.update(s.products).set({ amount: sql`${s.products.amount} + ${line.amount}` }).where(eq(s.products.id, line.productId));
    await tx.delete(s.sales).where(eq(s.sales.id, saleId));
  });
}
export async function cancelAffiliation(db: Database, affiliationId: number) {
  await db.transaction(async tx => {
    const old = await one(await tx.select().from(s.affiliations).where(eq(s.affiliations.id, affiliationId)).for('update'));
    if (old.invoiceCancelled) throw new ApiError(409, 'La factura ya está anulada.');
    if (old.renewal) await tx.update(s.affiliations).set({ invoiceCancelled: true }).where(eq(s.affiliations.id, affiliationId));
    else await tx.delete(s.affiliations).where(eq(s.affiliations.id, affiliationId));
  });
}
export async function resources(db: Database, resource: string, method: string, recordId: number | undefined, body: any) {
  if (resource === 'sales' || resource === 'affiliation') {
    if (method === 'GET') { const rows = resource === 'sales' ? await listSales(db) : await listAffiliations(db); return recordId ? one(rows.filter(x => x.id === recordId)) : rows; }
    if (method === 'POST' || method === 'PUT') return resource === 'sales' ? saveSale(db, body, recordId) : saveAffiliation(db, body, recordId);
    if (method === 'DELETE' && recordId) { await (resource === 'sales' ? cancelSale(db, recordId) : cancelAffiliation(db, recordId)); return null; }
  }
  let table: AnyPgTable, key: AnyPgColumn;
  switch (resource) {
    case 'users': table = s.users; key = s.users.id; break;
    case 'coachs': table = s.coaches; key = s.coaches.idCoach; break;
    case 'products': table = s.products; key = s.products.id; break;
    case 'gymservice': table = s.services; key = s.services.idService; break;
    case 'typeplan': table = s.plans; key = s.plans.idTypePlan; break;
    default: throw new ApiError(404, 'Ruta no encontrada.');
  }
  if (method === 'GET') {
    if (resource === 'typeplan') { const rows = await listPlans(db); return recordId ? one(rows.filter(x => x.idTypePlan === recordId)) : rows; }
    const rows = await db.select().from(table).where(recordId ? eq(key, recordId) : undefined).orderBy(asc(key));
    return recordId ? one(rows) : rows;
  }
  if (method === 'DELETE' && recordId) { await one(await db.delete(table).where(eq(key, recordId)).returning()); return null; }
  if (method !== 'POST' && method !== 'PUT') throw new ApiError(405, 'Método no permitido.');
  const photo = body.photoData == null ? undefined : required(body.photoData, 'Foto');
  let values: Record<string, unknown>;
  if (resource === 'users') {
    const mail = required(body.mail, 'Correo').toLowerCase();
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(mail)) throw new ApiError(400, 'Correo inválido.');
    const role = body.role || 'USER'; if (!['USER','COACH','ADMIN'].includes(role)) throw new ApiError(400, 'Rol inválido.');
    values = { name: required(body.name, 'Nombre'), mail, address: required(body.address, 'Dirección'), phoneNumber: required(String(body.phoneNumber ?? ''), 'Teléfono'), documentNumber: required(body.documentNumber, 'Cédula'), physicalConditionReport: body.physicalConditionReport ? required(body.physicalConditionReport, 'Reporte') : null, photoData: photo, role };
  } else if (resource === 'coachs') values = { nameCoach: required(body.nameCoach, 'Nombre'), photoData: photo };
  else if (resource === 'products') values = { nameProduct: required(body.nameProduct, 'Producto'), value: money(body.value), amount: number(body.amount, 'Existencias', true), photoData: photo };
  else if (resource === 'gymservice') values = { serviceName: required(body.serviceName, 'Servicio') };
  else values = { namePlan: required(body.namePlan, 'Plan'), value: money(body.value) };
  let saved: any;
  await db.transaction(async tx => {
    saved = recordId ? await one(await tx.update(table).set(values).where(eq(key, recordId)).returning()) : await one(await tx.insert(table).values(values).returning());
    if (resource === 'typeplan') {
      const planId = saved.idTypePlan;
      const serviceIds = [...new Set(array(body.serviceIds).map(id))];
      await tx.delete(s.planServices).where(eq(s.planServices.planId, planId));
      if (serviceIds.length) await tx.insert(s.planServices).values(serviceIds.map(serviceId => ({ planId, serviceId })));
    }
  });
  if (resource === 'typeplan') return (await listPlans(db)).find(x => x.idTypePlan === saved.idTypePlan);
  return saved;
}
export async function special(db: Database, resource: string, action: string) {
  if (resource === 'products' && action === 'low-stock') return db.select().from(s.products).where(lte(s.products.amount, 5));
  if (resource === 'affiliation' && action === 'expiring-soon') { const start = today(); const end = new Date(`${start}T12:00:00Z`); end.setUTCDate(end.getUTCDate()+3); return (await listAffiliations(db)).filter(a => !a.invoiceCancelled && a.endAffiliation >= start && a.endAffiliation <= end.toISOString().slice(0,10)); }
  if (resource === 'sales' && action === 'monthly-revenue') { const start = today().slice(0,7) + '-01'; const rows = await db.select({ totalValue: sql<number>`coalesce(sum(${s.sales.totalValue}), 0)` }).from(s.sales).where(and(gte(s.sales.saleDate, start), lte(s.sales.saleDate, today()))); return { totalValue: Number(rows[0].totalValue) }; }
  throw new ApiError(404, 'Ruta no encontrada.');
}
