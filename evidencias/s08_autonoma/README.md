# Sesión 8 actividad autónoma

Esta carpeta conserva capturas originales de la ejecución en Android. Los archivos 01 a 09 documentan listado, creación, actualización, baja lógica y validación 400 del nombre. Los archivos 10 y 11 documentan la validación 400 del precio y el conflicto 409 por nombre duplicado. No son evidencia de los otros escenarios de la bitácora.

## Estado comprobado

| Requisito | Estado |
| --- | --- |
| Listar GET 200 | Captura de la lista; el código 200 consta en el registro de la práctica de sesión 8. |
| Crear POST 201 | Capturas 02, 03 y 04. |
| Actualizar PUT 200 | Capturas 05 y 06 muestran el cambio en la app; falta capturar el código PUT 200. |
| Desactivar DELETE 204 | Captura 07 muestra el producto en Inactivos; falta capturar el código DELETE 204. |
| Obtener GET por ID 200 | Pendiente de ejecución y captura. |
| Validación del nombre 400 | Capturas 08 y 09. |
| Validación del precio 400 | Captura 10: precio `0`, mensaje bajo Precio y POST 400 en Logcat. |
| Conflicto por nombre duplicado 409 | Captura 11: mensaje de producto existente y POST 409 en Logcat. |
| Pruebas en commonTest | `:shared:testAndroidHostTest` pasó el 4 de octubre de 2026: 34 pruebas, 0 fallos. |
| Compilación Android | `:androidApp:assembleDebug` pasó el 4 de octubre de 2026. |

## Escenarios aún por ejecutar

1. Actualización de ID inexistente: ejecutar un PUT con un ID que no exista en Swagger o Postman y registrar el 404. La interfaz normal no permite editar un producto ausente; una captura de Swagger por sí sola no demuestra el mensaje en la app.
2. Doble DELETE: desactivar un producto activo y repetir el DELETE sobre el mismo ID desde Swagger. Este backend devuelve **409 Conflict**, no 404, porque la baja es lógica y el registro sigue existiendo. Documentar esa diferencia con la ficha; no atribuirle `NoEncontrado`.
3. Sin conexión: detener PharmaSoft o Docker y pulsar Actualizar lista. Capturar el mensaje controlado; reiniciar el backend después.
4. Timeout: usar temporalmente un `requestTimeoutMillis` muy bajo y actualizar la lista. Capturar `TiempoAgotado`; restaurar luego la configuración normal de 15 segundos.
5. Cancelación: iniciar una operación y salir de la pantalla antes de la respuesta. El código relanza `CancellationException` sin convertirla en `ErrorApi`; la prueba automatizada `cancelacionNoSeTraduceAErrorDeRed` verifica esa regla, pero falta una observación visual si la rúbrica la exige.

La ficha solicita capturas de Android e iOS; según la indicación del docente comunicada por el estudiante, iOS no forma parte de esta entrega. No se debe inventar evidencia iOS.
