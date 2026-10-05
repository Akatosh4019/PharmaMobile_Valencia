# Sesión 8 actividad autónoma

Esta carpeta conserva capturas de Android (01 a 14) y Swagger UI (15 y 16). Las primeras documentan listado, creación, actualización, baja lógica, validaciones 400, conflicto 409, pérdida de conexión y timeout. La captura 15 verifica GET por ID 200; la 16 verifica PUT sobre ID inexistente 404. El timeout se provocó temporalmente con `requestTimeoutMillis = 1` y luego se restauró el valor normal de 15 segundos. No se atribuye a la app una respuesta observada solo en Swagger.

## Estado comprobado

| Requisito | Estado |
| --- | --- |
| Listar GET 200 | Captura de la lista; el código 200 consta en el registro de la práctica de sesión 8. |
| Crear POST 201 | Capturas 02, 03 y 04. |
| Actualizar PUT 200 | Capturas 05 y 06 muestran el cambio en la app; falta capturar el código PUT 200. |
| Desactivar DELETE 204 | Captura 07 muestra el producto en Inactivos; falta capturar el código DELETE 204. |
| Obtener GET por ID 200 | Captura 15: GET `/api/v1/productos/1` devuelve Paracetamol con HTTP 200 en Swagger. |
| Actualización de ID inexistente 404 | Captura 16: PUT `/api/v1/productos/99999999` con cuerpo válido devuelve HTTP 404 y "Producto no encontrado" en Swagger; no se observó este error en la pantalla Android. |
| Validación del nombre 400 | Capturas 08 y 09. |
| Validación del precio 400 | Captura 10: precio `0`, mensaje bajo Precio y POST 400 en Logcat. |
| Conflicto por nombre duplicado 409 | Captura 11: mensaje de producto existente y POST 409 en Logcat. |
| Servidor caído | Captura 12: GET de productos falla con `ConnectException`; la app muestra "Sin conexión con el servidor" y ofrece Reintentar. No hubo respuesta HTTP, por lo que no hay código de estado. |
| Tiempo agotado | Capturas 13 y 14: Ktor informa `Request timeout has expired` con límite de 1 ms; la app muestra "La solicitud tardó demasiado". No hubo respuesta HTTP. |
| Pruebas en commonTest | `:shared:testAndroidHostTest` pasó tras añadir la prueba PUT 404: 35 pruebas, 0 fallos. |
| Compilación Android | `:androidApp:assembleDebug` pasó el 4 de octubre de 2026. |

## Escenarios aún por ejecutar

1. Doble DELETE: desactivar un producto activo y repetir el DELETE sobre el mismo ID desde Swagger. Este backend devuelve **409 Conflict**, no 404, porque la baja es lógica y el registro sigue existiendo. Documentar esa diferencia con la ficha; no atribuirle `NoEncontrado`.
2. Cancelación: iniciar una operación y salir de la pantalla antes de la respuesta. El código relanza `CancellationException` sin convertirla en `ErrorApi`; la prueba automatizada `cancelacionNoSeTraduceAErrorDeRed` verifica esa regla, pero falta una observación visual si la rúbrica la exige.

La ficha solicita capturas de Android e iOS; según la indicación del docente comunicada por el estudiante, iOS no forma parte de esta entrega. No se debe inventar evidencia iOS.
