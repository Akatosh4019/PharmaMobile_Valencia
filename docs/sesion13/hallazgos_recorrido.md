# Sesión 13 — recorrido y hallazgos

**Estado:** pendiente de completar durante el recorrido manual de Roberto. No se registran como observados escenarios que todavía no se hayan probado.

| Paso | Acción del usuario | Resultado esperado | Resultado observado | ¿Hay brecha? | Nota o decisión |
| --- | --- | --- | --- | --- | --- |
| 1 | Abrir la aplicación e iniciar sesión | La app muestra inicio o un error recuperable | Con credenciales de demostración rechazadas, el backend respondió 401 y la app mostró “Usuario o contraseña incorrectos” | Sin brecha en el rechazo | No se pudo comprobar acceso válido porque el backend activo no acepta la cuenta de demostración documentada |
| 2 | Consultar el inventario con conexión | Se muestran los productos recibidos del backend | Pendiente de recorrido | Pendiente | |
| 3 | Crear un producto con conexión | El producto queda guardado y aparece en la lista | Pendiente de recorrido | Pendiente | |
| 4 | Crear un producto sin conexión | El producto queda guardado localmente y marcado pendiente | Pendiente de recorrido | Pendiente | |
| 5 | Recuperar la conexión y sincronizar | Los pendientes se envían y la lista se actualiza | Pendiente de recorrido | Pendiente | |
| 6 | Probar un rechazo del servidor | Se conserva el dato local y se explica qué requiere revisión | Pendiente de recorrido | Pendiente | |
| 7 | Solicitar y cambiar el permiso de cámara | Se comunica concedido, denegado o bloqueado y se ofrece Ajustes | Pendiente de recorrido | Pendiente | |

Al finalizar el recorrido se reemplazan las celdas “Pendiente de recorrido” por lo observado y se priorizan las brechas como alta, media o baja. Los fallos reproducibles se registran con los pasos exactos para corregirlos.

## Cambios de la práctica

- SQLDelight guarda `intentos` y `ultimo_error` en cada producto pendiente. La migración `shared/src/commonMain/sqldelight/pe/edu/upeu/pharmamobile/data/local/migrations/2.sqm` agrega las columnas a las instalaciones existentes.
- `SincronizarUseCase` sube la cola local antes de descargar el catálogo, distingue rechazos permanentes de fallos temporales, limita los intentos a cinco y aplica espera creciente entre intentos.
- La pantalla de productos expone pendientes, fallidos, cantidad de intentos, motivo y antigüedad de la última sincronización.
- `GestorPermisos` define un contrato común; Android e iOS aportan su implementación nativa para el permiso de cámara. La app no captura ni almacena fotografías.
- `Telemetria` mantiene Crashlytics detrás de un contrato de dominio y registra solo contexto técnico permitido, nunca tokens ni datos personales.

## Requisito para activar Firebase

La app Android está registrada en el proyecto Firebase `pharmamobil-valencia`; `androidApp/google-services.json` coincide con `pe.edu.upeu.pharmamobile`. Gradle procesa ese archivo y aplica los plugins de Google Services y Crashlytics. Para recibir reportes, habilita Crashlytics en la consola y ejecuta la app. iOS se omite según la indicación del profesor.

## Consideración de reintentos

La API actual crea productos mediante `POST` y no ofrece clave de idempotencia. Si el servidor guarda un producto pero la respuesta se pierde, un reintento puede crear un duplicado. Por eso los registros fallidos muestran el motivo y el contador para revisión. Para garantizar reintentos seguros frente a respuestas ambiguas, el backend debe aceptar y deduplicar una clave de idempotencia estable por operación.

## Verificación pendiente

Verificado en Windows: `:shared:testAndroidHostTest` y `:androidApp:assembleDebug` pasaron con la configuración Firebase Android. El APK se instaló y abrió en el emulador sin excepción fatal; se comprobó el rechazo de credenciales inválidas (401). El acceso válido, cámara y sincronización manual siguen pendientes de un backend y cuenta de prueba compatibles. iOS se omite por instrucción del profesor.
