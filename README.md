# PharmaMobil - cliente Ktor y gestión de productos

Aplicación Kotlin Multiplatform con Compose, Clean Architecture y MVVM. En esta sesión, la pantalla de productos obtiene su listado del backend PharmaSoft mediante Ktor. Android y iOS comparten DTO, mapper, repositorio y ViewModel; cada plataforma aporta su motor HTTP y URL base.

## Requisitos y ejecución

1. Clonar [BackendPharmobile](https://github.com/Akatosh4019/BackendPharmobile) y, en esa carpeta, ejecutar `Copy-Item .env.example .env`, `mvn -DskipTests package` y `docker compose up -d --build`. Docker inicia Oracle y la API; el primer arranque de Oracle puede tardar varios minutos.
2. Comprobar `http://localhost:8080/api/health` y `http://localhost:8080/api/v1/productos`. El segundo GET debe devolver 200 y un objeto con `contenido` no vacío.
3. Abrir este proyecto en Android Studio y sincronizar Gradle.
4. Iniciar un emulador Android y ejecutar la configuración `androidApp`. Para compilar sin emulador: `./gradlew :androidApp:assembleDebug` (en Windows, `./gradlew.bat`). El backend debe seguir encendido mientras se usa la app.
5. En macOS, abrir `iosApp` con Xcode para ejecutar el simulador iOS. No es posible verificar el simulador iOS desde Windows.

La versión de Ktor fijada en `gradle/libs.versions.toml` es 3.5.2. Se usa en esta instalación porque Ktor 3.6.0 resolvió una dependencia OkHttp que exigía `compileSdk` 37, mientras el proyecto y su plugin Android compilan con 36.

## Conectividad REST

| Plataforma | URL base local | Motor |
| --- | --- | --- |
| Android Emulator | `http://10.0.2.2:8080/api/v1/` | OkHttp |
| iOS Simulator | `http://localhost:8080/api/v1/` | Darwin |

La petición de la sesión es `GET productos?pagina=0&tamanio=20`. `10.0.2.2` representa la computadora anfitriona desde el emulador Android. El permiso `INTERNET` y las excepciones de tráfico HTTP en Android/iOS se limitan al backend local; en producción debe usarse HTTPS.

El cliente Ktor se configura en `shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobile/data/remote/HttpClientFactory.kt`: `ContentNegotiation` deserializa JSON con `ignoreUnknownKeys = true`, `expectSuccess = true` convierte respuestas HTTP 4xx/5xx en excepciones y `HttpTimeout` limita la petición a 15 segundos. En Android, los registros de petición y respuesta se encuentran en Logcat con el filtro `tag:PharmaMobilHTTP`. La API se encapsula en `ProductoApi` y se transforma a modelos de dominio mediante `ProductoRepositorioRemoto`.

La evidencia de la sesión 7 autónoma está en `evidencias/s07_autonoma/`. Allí se documentan GET 200, recurso inexistente 404, pérdida y recuperación de conexión, timeout de 1 ms y rechazo de campos JSON desconocidos con `ignoreUnknownKeys = false`. Las modificaciones temporales empleadas para 404, timeout y JSON desconocido se restauraron; la configuración entregada usa la ruta normal, 15 segundos e `ignoreUnknownKeys = true`. La prueba de 404 mostró una excepción capturada pero un mensaje técnico sin simplificar en la interfaz; la captura registra ese resultado real.

## Contrato JSON y recorrido de datos

PharmaSoft entrega una **página**, no una lista JSON simple. `PaginaProductosDto` recibe `contenido`, `pagina`, `tamanio`, `totalElementos`, `totalPaginas` y `ultima`. Cada `ProductoDto` recibe `id`, `nombre`, `precio`, `stock`, `estado`, `categoriaId` y `categoriaNombre`; las fechas adicionales se ignoran con `ignoreUnknownKeys`.

`ProductoApi` ejecuta el GET → `ProductoRepositorioRemoto` extrae `contenido` y mapea cada DTO a `Producto` del dominio → `ProductoViewModel` expone carga, lista, vacío o error → `ProductoScreen` muestra el resultado. El cliente HTTP es una sola instancia de Koin con ContentNegotiation, Logging (solo encabezados), HttpTimeout y DefaultRequest.

## Crear, editar y desactivar productos

La pantalla de productos también consume `GET categorias`, `POST productos`, `PUT productos/{id}` y `DELETE productos/{id}`. Al crear se elige una categoría y se envían nombre, precio, stock y estado; **no se envía ID**, porque el backend lo genera. Editar precarga esos datos y permite reactivar un producto inactivo.

En este backend, DELETE realiza una **baja lógica**: cambia `estado` a `false` y conserva el registro. La app pide confirmación antes de desactivar y luego muestra el producto en la pestaña Inactivos. Después de cada operación se vuelve a consultar el listado remoto. Es posible comprobarlo también desde Swagger UI (`http://localhost:8080/swagger-ui.html`).

## Verificaciones y evidencias para entregar

- Captura en Swagger UI de al menos una categoría y tres productos del backend.
- Captura del registro GET de Ktor con respuesta 200.
- Captura de la lista de productos en el emulador Android y, si hay acceso a macOS, en el simulador iOS.
- Captura del estado de error al desactivar la conexión (modo avión), sin cierre de la app.
- Enlace a la rama `feature/ktor-client-valencia` con al menos tres commits descriptivos de la sesión.

Pruebas locales: `./gradlew.bat :shared:testAndroidHostTest :androidApp:assembleDebug`. El test de DTO verifica la deserialización paginada y el de ViewModel cubre lista, vacío y error. Si el docente dispensa iOS por trabajar en Windows, conviene indicarlo expresamente en la entrega; esta rama no incluye evidencia iOS verificada.

## Sesión 8 - CRUD y errores controlados

La rama `feature/crud-productos-valencia` amplía `ProductoRepository` con `obtener(id)` y utiliza un DTO genérico de paginación. `ProductoApi` encapsula GET, POST, PUT y DELETE; DELETE no intenta leer un cuerpo del `204 No Content`. Los casos de uso de obtener, registrar, actualizar y eliminar están registrados en Koin.

`data/remote/EjecutarLlamada.kt` es el único punto que convierte respuestas 400/404/409/5xx, timeout y pérdida de conexión en `domain/error/ErrorApi`. La capa `presentation` solo conoce estos errores del dominio. `ProductoUiState` distingue la fase de la lista de la operación de guardar o desactivar; un guardado no oculta los productos visibles. Si el backend responde 400 con `validationErrors`, los mensajes se muestran debajo de los campos correspondientes.

Para evidenciar el 400 pedido en la guía, con PharmaSoft activo abre la app, escribe `AB` en Nombre, un precio y stock válidos, selecciona una categoría y pulsa Crear producto. La longitud del nombre se valida en el servidor; el texto de `validationErrors.nombre` debe aparecer bajo Nombre. Filtra Logcat con `tag:PharmaMobilHTTP` para capturar la petición POST y el `400`. Después usa un nombre de tres o más caracteres para probar un `201` y realiza una actualización y una desactivación. La baja es lógica: el producto pasa a Inactivos.

## Manejo de errores

`EjecutarLlamada.kt` transforma HTTP 400 en `ErrorApi.Validacion`, 404 en `NoEncontrado`, 409 en `Conflicto` y 5xx en `Servidor`. También separa falta de conexión, tiempo agotado y respuesta inválida. La cancelación normal de una corrutina se relanza y no se muestra como error al usuario. El ViewModel conserva la lista mientras guarda o desactiva y coloca los mensajes de validación del servidor bajo `nombre`, `precio`, `stock` o `categoriaId`.

La validación local solo rechaza entradas ausentes o que no pueden convertirse a número. Un nombre de dos caracteres y un precio numérico de cero llegan al backend para que su respuesta 400 se pueda comprobar en Android. La segunda llamada DELETE sobre un producto ya inactivo devuelve **409 Conflict** en esta versión de PharmaSoft, no 404: el backend conserva el registro por baja lógica y responde "ya se encuentra inactivo". Para obtener un 404 de actualización debe usarse un identificador inexistente. Un 409 de regla de negocio también se puede reproducir intentando crear otro producto con un nombre ya existente.

## Sesión 9 - Capacidades nativas

`platform/Formato.kt` declara `expect fun formatearSoles(Double)`. Android la implementa con `NumberFormat` y la configuración regional `es-PE`; iOS usa `NSNumberFormatter` y `NSLocale("es_PE")`. Los tres archivos conservan el mismo paquete y la misma firma. La conversión de `Producto` a `ProductoUi` se hace en la capa de presentación cuando el ViewModel recibe los productos, antes de que `ProductoScreen` dibuje el precio; el modelo de dominio conserva `precio: Double`.

La ausencia temporal de los `actual` produjo el error de compilación esperado `Expected formatearSoles has no actual declaration`. Tras agregar ambas implementaciones, `:shared:testAndroidHostTest` y `:androidApp:assembleDebug` pasaron en Windows. El target iOS aún requiere compilarse y probarse en macOS con Xcode; esta verificación de Android no demuestra que la presentación de iOS ya esté validada.

El contrato `domain/platform/Compartidor.kt` mantiene la acción de compartir fuera de la lógica de la plataforma. `CompartidorAndroid` usa `Intent.ACTION_SEND` con texto plano y `CompartidorIos` presenta un `UIActivityViewController`; Koin registra la implementación correspondiente en cada plataforma. Desde «Detalle» en la tarjeta de un producto, el botón «Compartir» envía nombre, precio formateado y stock. En Android se verificó en el emulador que se abre el selector nativo y que la vista previa contiene esos tres datos. Las capturas están en `evidencias/s09_practica/`.

Archivos de esta sesión por source set:

- `shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobile/platform/Formato.kt`: declaración `expect`.
- `shared/src/androidMain/kotlin/pe/edu/upeu/pharmamobile/platform/Formato.android.kt` y `shared/src/iosMain/kotlin/pe/edu/upeu/pharmamobile/platform/Formato.ios.kt`: implementaciones `actual`.
- `shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobile/domain/platform/Compartidor.kt`: contrato de dominio.
- `shared/src/androidMain/kotlin/pe/edu/upeu/pharmamobile/platform/CompartidorAndroid.kt` y `shared/src/iosMain/kotlin/pe/edu/upeu/pharmamobile/platform/CompartidorIos.kt`: capacidades nativas.
- `shared/src/androidMain/kotlin/pe/edu/upeu/pharmamobile/di/PlatformModule.android.kt` y `shared/src/iosMain/kotlin/pe/edu/upeu/pharmamobile/di/PlatformModule.ios.kt`: registros de Koin.
- `shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobile/presentation/producto/ProductoUi.kt`, `TextoParaCompartir.kt`, `ProductoViewModel.kt` y `ProductoScreen.kt`: precio y acción visibles sin importar APIs de Android o UIKit.

Evidencias Android: `evidencias/s09_practica/02-paso1-emulador-verificacion.png`, `03-precios-formateados-listado-android.png`, `04-detalle-producto-android.png` y `05-selector-compartir-android.png`. `01-expect-sin-actual-editor.png` muestra la declaración antes de añadir `actual`, pero no sustituye una captura del mensaje del compilador. Según la indicación del docente comunicada por el equipo, no se adjuntan capturas iOS porque no se dispone de macOS ni simulador iOS; el código iOS permanece sin verificación en Xcode.
