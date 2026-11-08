import type { UserValidateEvent } from '@netlify/functions';
export default {
  // Public registration is disabled. The administrator is provisioned server-side.
  userValidate(event: UserValidateEvent) { return event.deny(); },
};
