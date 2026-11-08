# AppGestionGym

## Publicar el frontend en Vercel

Vercel debe usar la **raíz de este repositorio** como Root Directory, donde está `vercel.json` (no selecciones `demoappgym`). La configuración ejecuta `npm run build` y publica `dist`; la ruta inicial `/` servirá `index.html` sin necesitar rewrites.

## Desplegar el backend y conectar la API

Vercel publica solo el frontend. Railway ejecutará Spring Boot y MySQL. Railway detecta el `Dockerfile` de la raíz, que construye el API con Java 21 y activa el perfil `prod`. No configures Railway para publicar la carpeta `dist`: eso es únicamente para Vercel.

### Crear el backend y MySQL en Railway

1. Crea un proyecto Railway y agrega **MySQL** usando **+ New → Database → MySQL**.
2. Agrega el repositorio de GitHub como un segundo servicio (**Deploy from GitHub repo**). Usa la raíz del repositorio; Railway detectará automáticamente el `Dockerfile`.
3. En las variables del servicio del backend, referencia las variables del servicio MySQL. Si el servicio se llama `MySQL`, agrega:
   - `MYSQLHOST=${{MySQL.MYSQLHOST}}`
   - `MYSQLPORT=${{MySQL.MYSQLPORT}}`
   - `MYSQLUSER=${{MySQL.MYSQLUSER}}`
   - `MYSQLPASSWORD=${{MySQL.MYSQLPASSWORD}}`
   - `MYSQLDATABASE=${{MySQL.MYSQLDATABASE}}`

   Usa el selector de referencias de Railway en vez de escribir una contraseña literal. Si el servicio de base tiene otro nombre, reemplaza `MySQL` por ese nombre.
4. Si el frontend se sirve desde Railway (misma URL que el backend), permite ese origen en `APP_CORS_ALLOWED_ORIGINS`; para este deployment usa `https://appgestiongym-production-3de6.up.railway.app`. Si además publicas el frontend en Vercel, agrega también su dominio Production separado por coma. Usa dominios exactos, con `https://`, sin rutas ni `/` final; no uses `*`. La configuración de producción usa el dominio Railway anterior como valor predeterminado si omites la variable. Agrega `INVOICE_DELETE_PASSWORD` solo si quieres habilitar la cancelación protegida de facturas.
5. En Railway → backend → **Settings → Networking**, selecciona **Generate Domain** para exponer el API por HTTPS. Configura la ruta de Healthcheck como `/actuator/health`. Railway asigna `PORT`; Spring lo usa automáticamente.
6. Espera que el deployment esté en estado **Healthy**. Comprueba `https://<dominio-railway>/actuator/health` y confirma que responde `{"status":"UP"}`.
7. En Vercel → **Settings → Environment Variables**, agrega `APP_API_BASE_URL=https://<dominio-railway>/api` para Production y vuelve a desplegar Vercel.

Para comprobar que el login existe sin usar credenciales reales, envía `POST https://<dominio-railway>/api/auth/login` con `{}` y espera `400` por validación; `404` significa que la URL o el path no apunta al backend.

### Cargar los datos y la cuenta de administrador

MySQL en Railway es una base nueva: el backend **no crea automáticamente** al administrador y no tiene registro público inicial. Antes de iniciar sesión, importa a la base Railway el esquema y los datos requeridos de la base local, incluida una cuenta cuyo rol sea `ADMIN`. Exporta la base local con MySQL Workbench y carga el respaldo en la instancia Railway siguiendo sus instrucciones de conexión; no subas el respaldo con datos personales a GitHub.

Como alternativa, crea/provisiona el administrador por un proceso controlado directamente en la base de producción. No habilites un endpoint público de registro de administradores ni publiques contraseñas en archivos de configuración.

Consulta [demoappgym/README.md](./demoappgym/README.md) para la configuración de la contraseña de cancelación de facturas.
