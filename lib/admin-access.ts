import { createHash, timingSafeEqual } from 'node:crypto';
import { ApiError } from './gym.js';

interface IdentityAccess<T> {
  login(email: string, password: string): Promise<T>;
  createUser(input: { email: string; password: string }): Promise<unknown>;
}

// Provision only when the configured initial secret matches the submitted password.
// Existing accounts are never reset or overwritten by this bootstrap mechanism.
export async function administratorLogin<T>(email: string, password: string, initialPassword: string | undefined, identity: IdentityAccess<T>): Promise<T> {
  try { return await identity.login(email, password); }
  catch (error) {
    const status = (error as { status?: number })?.status;
    if (![400, 401].includes(status ?? 0) || !initialPassword) throw error;
    const supplied = createHash('sha256').update(password).digest();
    const expected = createHash('sha256').update(initialPassword).digest();
    if (!timingSafeEqual(supplied, expected)) throw error;
    if (initialPassword.length < 12) throw new ApiError(503, 'La contraseña inicial del administrador requiere al menos 12 caracteres.');
    try {
      // Identity's server-only admin API creates a confirmed account without a signup form.
      await identity.createUser({ email, password: initialPassword });
    } catch {
      // Another concurrent first login may have created the account already.
      // Retrying authentication is safe; do not replace an existing account's password.
      try { return await identity.login(email, password); }
      catch { throw new ApiError(503, 'No se pudo preparar la cuenta de administrador. Comprueba la configuración de acceso del despliegue.'); }
    }
    return identity.login(email, password);
  }
}
