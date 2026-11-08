# AppGestionGym

## Secure invoice cancellation

Invoice cancellation requires the server-side `INVOICE_DELETE_PASSWORD` environment variable. If it is not configured, the API rejects cancellation requests. Set it in the same PowerShell session used to start Spring Boot; keep the actual password private and out of source control:

```powershell
$env:INVOICE_DELETE_PASSWORD = "<your-private-password>"
.\mvnw.cmd spring-boot:run
```
