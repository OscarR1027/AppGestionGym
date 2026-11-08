import test from 'node:test';
import assert from 'node:assert/strict';
import handler from '../netlify/functions/api.mjs';

test('API: salud, configuración pendiente, autenticación y protección de solicitudes', async () => {
  const previous = (globalThis as any).Netlify;
  try {
    (globalThis as any).Netlify = { env: { get: () => undefined } };
    const health = await handler(new Request('https://gym.example.test/api/health'));
    assert.equal(health.status, 200);
    const unconfigured = await handler(new Request('https://gym.example.test/api/users'));
    assert.equal(unconfigured.status, 503);
    (globalThis as any).Netlify = { env: { get: () => 'admin@example.test' } };
    const forbidden = await handler(new Request('https://gym.example.test/api/users', { method: 'POST', headers: { Origin: 'https://untrusted.example.test' }, body: '{}' }));
    assert.equal(forbidden.status, 403);
    const missingOrigin = await handler(new Request('https://gym.example.test/api/auth/login', { method: 'POST', body: '{}' }));
    assert.equal(missingOrigin.status, 403);
    const anonymous = await handler(new Request('https://gym.example.test/api/users'));
    assert.equal(anonymous.status, 401);
    const blocked = await handler(new Request('https://gym.example.test/api/auth/signup', { method: 'POST', headers: { Origin: 'https://gym.example.test' }, body: JSON.stringify({ email: 'another@example.test' }) }));
    assert.equal(blocked.status, 404);
  } finally { (globalThis as any).Netlify = previous; }
});
