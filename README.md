# AppGestionGym

## Publicar el frontend en Vercel

Vercel debe usar la **raíz de este repositorio** como Root Directory, donde está `vercel.json` (no selecciones `demoappgym`). La configuración ejecuta `npm run build` y publica `dist`; la ruta inicial `/` servirá `index.html` sin necesitar rewrites.

Para que el inicio de sesión y las funciones de gestión trabajen en producción:

1. Despliega el backend Spring Boot y MySQL en servicios que soporten Java 21 y una base de datos accesible desde internet. Vercel publica aquí solo el frontend estático; no ejecuta la aplicación Spring Boot.
2. En **Settings → Environment Variables** de Vercel, configura `APP_API_BASE_URL` para Production (y Preview si corresponde) con la URL pública del API, incluyendo `/api`, por ejemplo `https://api.ejemplo.com/api`. Después de cambiarla, crea un nuevo deployment.
3. En el backend, configura `app.cors.allowed-origins` con el origen exacto del sitio de Vercel, por ejemplo `https://mi-gimnasio.vercel.app`. Incluye también el dominio de producción si es diferente.

El build de producción falla intencionalmente si falta `APP_API_BASE_URL` o no termina en `/api`, para evitar publicar un login que apunte al propio frontend en lugar del backend. Los builds locales pueden omitirla y usan `/api`.

Consulta [demoappgym/README.md](./demoappgym/README.md) para la configuración de la contraseña de cancelación de facturas.
