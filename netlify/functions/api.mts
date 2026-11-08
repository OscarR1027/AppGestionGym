import type { Config } from '@netlify/functions';
import { getUser, login, admin, logout, refreshSession, verifyRequestOrigin, getSettings, requestPasswordRecovery, AuthError } from '@netlify/identity';
import { getStore } from '@netlify/blobs';
import { randomUUID } from 'node:crypto';
import { database } from '../../db/index.js';
import { ApiError, required, id, resources, special } from '../../lib/gym.js';
import { administratorLogin } from '../../lib/admin-access.js';

function administratorEmail() { return Netlify.env.get('GYM_ADMIN_EMAIL')?.trim().toLowerCase(); }
function json(value: unknown, status = 200) { return Response.json(value, { status, headers: { 'Cache-Control': 'no-store' } }); }
async function body(req: Request) {
  const text = await req.text();
  if (text.length > 2_000_000) throw new ApiError(413, 'El archivo es demasiado grande.');
  try { const value = JSON.parse(text); if (!value || typeof value !== 'object' || Array.isArray(value)) throw Error(); return value; }
  catch { throw new ApiError(400, 'Envía un objeto JSON válido.'); }
}
async function storePhoto(value: unknown) {
  if (value == null || value === '') return value;
  if (typeof value !== 'string') throw new ApiError(400, 'Foto inválida.');
  if (/^\/api\/photos\/[a-f0-9-]+\.(jpeg|png|webp)$/.test(value)) return value;
  const match = /^data:image\/(jpeg|png|webp);base64,([A-Za-z0-9+/=]+)$/.exec(value);
  if (!match) throw new ApiError(400, 'Usa una imagen JPEG, PNG o WebP.');
  const bytes = Buffer.from(match[2], 'base64');
  if (bytes.length > 1_000_000) throw new ApiError(413, 'La foto no puede superar 1 MB.');
  const key = `${randomUUID()}.${match[1]}`;
  await getStore('gym-photos').set(key, new Blob([bytes], { type: `image/${match[1]}` }));
  return `/api/photos/${key}`;
}
export default async (req: Request) => {
  try {
    const url = new URL(req.url), parts = url.pathname.split('/').filter(Boolean).slice(1);
    if (req.method !== 'GET') verifyRequestOrigin(req);
    if (parts[0] === 'health' && req.method === 'GET') return json({ status: 'ok', backend: 'netlify' });
    if (!administratorEmail()) throw new ApiError(503, 'Falta configurar el correo del administrador del gimnasio.');
    if (parts[0] === 'auth') {
      const action = parts[1];
      if (action === 'settings' && req.method === 'GET') {
        const settings = await getSettings();
        return json({ emailEnabled: settings.providers.email, autoconfirm: settings.autoconfirm });
      }
      if (action === 'logout' && req.method === 'POST') { await logout(); return json({ ok: true }); }
      if (['login','recover'].includes(action) && req.method === 'POST') {
        const data = await body(req), email = required(data.email, 'Correo').toLowerCase();
        if (email !== administratorEmail()) throw new ApiError(403, 'Este correo no está autorizado para administrar el gimnasio.');
        if (action === 'recover') { await requestPasswordRecovery(email); return json({ message: 'Revisa tu correo para recuperar el acceso.' }); }
        const password = required(data.password, 'Contraseña');
        const user = await administratorLogin(email, password, Netlify.env.get('GYM_ADMIN_INITIAL_PASSWORD'), { login, createUser: input => admin.createUser(input) });
        return json({ email: user.email, role: 'ADMIN' });
      }
      if (action !== 'me' || req.method !== 'GET') throw new ApiError(404, 'Ruta de acceso no encontrada.');
    }
    await refreshSession();
    const user = await getUser();
    if (!user) throw new ApiError(401, 'Ingresa para continuar.');
    // Authorization uses trusted server configuration; customer roles never grant panel access.
    if (user.email?.trim().toLowerCase() !== administratorEmail()) throw new ApiError(403, 'Acceso reservado al administrador configurado.');
    if (parts[0] === 'auth') return json({ email: user.email, role: 'ADMIN' });
    if (parts[0] === 'photos' && parts.length === 2 && req.method === 'GET') {
      const key = parts[1];
      if (!/^[a-f0-9-]+\.(jpeg|png|webp)$/.test(key)) throw new ApiError(404, 'Foto no encontrada.');
      const photo = await getStore('gym-photos').get(key, { type: 'arrayBuffer' });
      if (!photo) throw new ApiError(404, 'Foto no encontrada.');
      return new Response(photo, { headers: { 'Content-Type': `image/${key.split('.').pop()}`, 'Cache-Control': 'private, max-age=3600', 'X-Content-Type-Options': 'nosniff' } });
    }
    const [resource, segment, suffix] = parts;
    if (!['users','coachs','products','gymservice','typeplan','affiliation','sales'].includes(resource) || parts.length > 3) throw new ApiError(404, 'Ruta no encontrada.');
    if (req.method === 'GET' && segment && ['low-stock','expiring-soon','monthly-revenue'].includes(segment) && !suffix) return json(await special(database(), resource, segment));
    const recordId = segment ? id(segment) : undefined;
    if (suffix && !(resource === 'sales' && suffix === 'factura' && req.method === 'GET')) throw new ApiError(404, 'Ruta no encontrada.');
    if (!['GET','POST','PUT','DELETE'].includes(req.method) || (req.method === 'POST' && recordId) || (['PUT','DELETE'].includes(req.method) && !recordId)) throw new ApiError(405, 'Método no permitido.');
    let data = ['POST','PUT','DELETE'].includes(req.method) && (req.method !== 'DELETE' || ['sales','affiliation'].includes(resource)) ? await body(req) : {};
    if (req.method === 'DELETE' && ['sales','affiliation'].includes(resource)) {
      try { await login(user.email!, required(data.password, 'Contraseña')); }
      catch { throw new ApiError(403, 'Contraseña de administrador incorrecta.'); }
    }
    if (['POST','PUT'].includes(req.method) && ['users','coachs','products'].includes(resource)) data = { ...data, photoData: await storePhoto(data.photoData) };
    const result = await resources(database(), resource, req.method, recordId, data);
    return result === null ? new Response(null, { status: 204, headers: { 'Cache-Control': 'no-store' } }) : json(result, req.method === 'POST' ? 201 : 200);
  } catch (error) {
    if (error instanceof ApiError) return json({ message: error.message }, error.status);
    if (error instanceof AuthError) return json({ message: error.status === 403 ? 'Solicitud de acceso rechazada.' : 'No se pudo completar el acceso. Revisa el correo, la contraseña y la confirmación de tu cuenta.' }, error.status && error.status >= 400 && error.status < 600 ? error.status : 502);
    const code = (error as any)?.cause?.code || (error as any)?.code;
    if (code === '23505') return json({ message: 'El correo o la cédula ya están registrados.' }, 409);
    if (code === '23503') return json({ message: 'Hay registros relacionados: comprueba las referencias o conserva este registro para mantener el historial.' }, 409);
    if (['23514','22003','22001','22P02'].includes(code)) return json({ message: 'Revisa los valores del formulario.' }, 400);
    // Never include request payloads, authentication data, or database connection errors in responses/logs.
    return json({ message: 'No se pudo completar la operación. Comprueba que el despliegue y las migraciones de la base de datos hayan terminado.' }, 503);
  }
};
export const config: Config = { path: '/api/*' };
