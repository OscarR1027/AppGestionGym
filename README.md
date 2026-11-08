# Pulso · Gestión del gimnasio

La interfaz se publica desde `demoappgym/src/main/resources/static`. La API `/api/*` funciona con Netlify Functions y guarda los registros en Netlify Database (Postgres). Las fotos se guardan en Netlify Blobs y solo se sirven a sesiones autorizadas. El código Spring Boot se conserva como referencia; el despliegue de Netlify ya no necesita Java, Docker ni MySQL.

## Primer acceso

Configura `GYM_ADMIN_EMAIL` con el correo de la persona responsable del gimnasio en los contextos donde se use la aplicación. Esta variable es la lista de acceso del panel: los roles de las fichas de clientes no conceden permisos de administración.

Configura también `GYM_ADMIN_INITIAL_PASSWORD` como secreto privado de al menos 12 caracteres. Después del despliegue, abre el sitio HTTPS e ingresa directamente con el correo autorizado y esa contraseña. Si la cuenta aún no existe, la API la crea y confirma mediante la API administrativa de Identity al autenticar por primera vez. No hay formulario ni endpoint público de registro.

Las cuentas existentes nunca se sobrescriben con la contraseña inicial. Una vez creado el administrador, puede retirarse la variable de contraseña inicial; los accesos posteriores se verifican con Identity. La recuperación por correo solo funciona si se controla un buzón real asociado a la cuenta.

Las cuentas y los datos de la antigua base MySQL no se transfieren automáticamente. Este despliegue usa una base nueva: registra servicios, planes, clientes, entrenadores y productos antes de crear afiliaciones y ventas. Una importación de los datos históricos requiere una exportación de la base original y un procedimiento separado; no se modificó ni borró esa base.

## Operaciones

Las ventas descuentan inventario en una transacción, conservan el nombre y precio facturados y restauran existencias al anularse. La anulación de ventas y afiliaciones requiere volver a introducir la contraseña del administrador. Sustituye el antiguo requisito de una contraseña especial configurada en Spring Boot.

Las afiliaciones conservan el valor cobrado aunque cambie el precio del plan. Las renovaciones anuladas permanecen en el historial; al anular una afiliación nueva se elimina su registro, igual que en la aplicación original. Las referencias de la base impiden eliminar clientes, planes, servicios, productos o entrenadores usados por otros registros.

## Verificación y despliegue

`npm test` valida el SQL generado y los flujos de ventas, inventario, facturas, afiliaciones y acceso usando una base Postgres efímera de pruebas. `npm run typecheck` verifica los tipos. El esquema está en `db/schema.ts`; sus migraciones están en `netlify/database/migrations` y la plataforma las aplica durante el despliegue. No ejecutes migraciones manuales sobre la base del sitio.

Netlify Identity se verifica en un despliegue HTTPS; no se emula con el servidor local de Netlify. `/api/health` permite comprobar que la función responde; el acceso al panel y sus listados comprueban también la sesión y la base de datos. La publicación y la prueba del acceso real se completan después del despliegue automático.
