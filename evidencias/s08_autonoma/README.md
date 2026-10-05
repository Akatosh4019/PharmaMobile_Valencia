# Sesión 8 actividad autónoma

Esta carpeta conserva capturas de Android (01 a 14), Swagger UI (15 a 20) y una ejecución de prueba automatizada (21). Las primeras documentan listado, creación, actualización, baja lógica, validaciones 400, conflicto 409, pérdida de conexión y timeout. La captura 15 verifica GET por ID 200; la 16 verifica PUT sobre ID inexistente 404. Las capturas 17 a 19 documentan la doble baja del producto 101. La captura 20 verifica PUT `/api/v1/productos/1` con HTTP 200 y precio actualizado a 25.01. La captura 21 muestra una prueba individual aprobada; el nombre queda cortado en la vista y conviene otra captura que lo muestre completo. El timeout se provocó temporalmente con `requestTimeoutMillis = 1` y luego se restauró el valor normal de 15 segundos. No se atribuye a la app una respuesta observada solo en Swagger.

## Estado comprobado

| Requisito | Estado |
| --- | --- |
| Listar GET 200 | Captura de la lista; el código 200 consta en el registro de la práctica de sesión 8. |
| Crear POST 201 | Capturas 02, 03 y 04. |
| Actualizar PUT 200 | Capturas 05 y 06 muestran un cambio en la app; captura 20 verifica otro PUT exitoso (ID 1, precio 25.01) con HTTP 200 en Swagger. |
| Desactivar DELETE 204 | Captura 07 muestra un producto en Inactivos; captura 18 verifica un DELETE exitoso (ID 101) con HTTP 204 en Swagger. |
| Obtener GET por ID 200 | Captura 15: GET `/api/v1/productos/1` devuelve Paracetamol con HTTP 200 en Swagger. |
| Actualización de ID inexistente 404 | Captura 16: PUT `/api/v1/productos/99999999` con cuerpo válido devuelve HTTP 404 y "Producto no encontrado" en Swagger; no se observó este error en la pantalla Android. |
| Doble DELETE del mismo ID | Captura 17: POST creó `PruebaDobleBajaOct` con ID 101. Captura 18: primer DELETE `/api/v1/productos/101` devolvió 204. Captura 19: segundo DELETE al mismo ID devolvió 409 "ya se encuentra inactivo". Es una diferencia real con el 404 esperado en la ficha. |
| Validación del nombre 400 | Capturas 08 y 09. |
| Validación del precio 400 | Captura 10: precio `0`, mensaje bajo Precio y POST 400 en Logcat. |
| Conflicto por nombre duplicado 409 | Captura 11: mensaje de producto existente y POST 409 en Logcat. |
| Servidor caído | Captura 12: GET de productos falla con `ConnectException`; la app muestra "Sin conexión con el servidor" y ofrece Reintentar. No hubo respuesta HTTP, por lo que no hay código de estado. |
| Tiempo agotado | Capturas 13 y 14: Ktor informa `Request timeout has expired` con límite de 1 ms; la app muestra "La solicitud tardó demasiado". No hubo respuesta HTTP. |
| Pruebas en commonTest | `:shared:testAndroidHostTest` pasó tras añadir la prueba PUT 404: 35 pruebas, 0 fallos. |
| Compilación Android | `:androidApp:assembleDebug` pasó el 4 de octubre de 2026. |
| Cancelación | Captura 21: ejecución individual aprobada (`1 test passed`, `BUILD SUCCESSFUL`); falta captura con nombre de prueba completo para asociarla visualmente a `cancelacionNoSeTraduceAErrorDeRed`. |

## Escenarios aún por ejecutar

1. Cancelación: el código relanza `CancellationException` sin convertirla en `ErrorApi`; la prueba automatizada `cancelacionNoSeTraduceAErrorDeRed` verifica esa regla. Conviene capturar la vista de Run con el nombre de la prueba legible. La ejecución automatizada no sustituye una observación en la UI si la rúbrica exige expresamente iniciar una operación y salir de la pantalla.

La ficha solicita capturas de Android e iOS; según la indicación del docente comunicada por el estudiante, iOS no forma parte de esta entrega. No se debe inventar evidencia iOS.
