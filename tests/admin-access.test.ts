import test from 'node:test';
import assert from 'node:assert/strict';
import { randomBytes } from 'node:crypto';
import { administratorLogin } from '../lib/admin-access.js';

function rejected(status = 400) { return Object.assign(new Error('Authentication rejected'), { status }); }

test('administrador: crea la cuenta confirmada al primer acceso autorizado', async () => {
  const initial = randomBytes(24).toString('hex');
  let attempts = 0, creates = 0;
  const result = await administratorLogin('admin@example.test', initial, initial, {
    async login() { if (!attempts++) throw rejected(); return { id: 'administrator' }; },
    async createUser(input) { assert.equal(input.email, 'admin@example.test'); assert.ok(input.password === initial); creates++; },
  });
  assert.equal(result.id, 'administrator'); assert.equal(creates, 1); assert.equal(attempts, 2);
});

test('administrador: contraseña incorrecta o configuración ausente no crea cuentas', async () => {
  const initial = randomBytes(24).toString('hex');
  let creates = 0;
  const identity = { async login(): Promise<never> { throw rejected(); }, async createUser() { creates++; } };
  await assert.rejects(administratorLogin('admin@example.test', randomBytes(24).toString('hex'), initial, identity));
  await assert.rejects(administratorLogin('admin@example.test', initial, undefined, identity));
  assert.equal(creates, 0);
});

test('administrador: cuenta existente y errores del servicio no se sobrescriben', async () => {
  const initial = randomBytes(24).toString('hex');
  let creates = 0;
  const existing = await administratorLogin('admin@example.test', initial, initial, { async login() { return { id: 'existing' }; }, async createUser() { creates++; } });
  assert.equal(existing.id, 'existing');
  await assert.rejects(administratorLogin('admin@example.test', initial, initial, { async login(): Promise<never> { throw rejected(429); }, async createUser() { creates++; } }));
  assert.equal(creates, 0);
});

test('administrador: primer acceso concurrente reintenta login sin restablecer contraseñas', async () => {
  const initial = randomBytes(24).toString('hex');
  let attempts = 0;
  const result = await administratorLogin('admin@example.test', initial, initial, { async login() { if (!attempts++) throw rejected(); return { id: 'existing' }; }, async createUser() { throw rejected(422); } });
  assert.equal(result.id, 'existing');
});
