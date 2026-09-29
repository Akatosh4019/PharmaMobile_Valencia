# PharmaMobil - cliente Ktor y gestión de productos

Aplicación Kotlin Multiplatform con Compose, Clean Architecture y MVVM. En esta sesión, la pantalla de productos obtiene su listado del backend PharmaSoft mediante Ktor. Android y iOS comparten DTO, mapper, repositorio y ViewModel; cada plataforma aporta su motor HTTP y URL base.

## Requisitos y ejecución

1. Clonar [BackendPharmobile](https://github.com/Akatosh4019/BackendPharmobile) y, en esa carpeta, ejecutar `Copy-Item .env.example .env`, `mvn -DskipTests package` y `docker compose up -d --build`. Docker inicia Oracle y la API; el primer arranque de Oracle puede tardar varios minutos.
2. Comprobar `http://localhost:8080/api/health` y `http://localhost:8080/api/v1/productos`. El segundo GET debe devolver 200 y un objeto con `contenido` no vacío.
3. Abrir este proyecto en Android Studio y sincronizar Gradle.
4. Iniciar un emulador Android y ejecutar la configuración `androidApp`. Para compilar sin emulador: `./gradlew :androidApp:assembleDebug` (en Windows, `./gradlew.bat`). El backend debe seguir encendido mientras se usa la app.
5. En macOS, abrir `iosApp` con Xcode para ejecutar el simulador iOS. No es posible verificar el simulador iOS desde Windows.

La versión de Ktor fijada en `gradle/libs.versions.toml` es 3.5.2. Se usa en esta instalación porque Ktor 3.6.0 resolvió una dependencia OkHttp que exigía `compileSdk` 37, mientras el proyecto y su plugin Android compilan con 36.

## URL y endpoint

| Plataforma | URL base local | Motor |
| --- | --- | --- |
| Android Emulator | `http://10.0.2.2:8080/api/v1/` | OkHttp |
| iOS Simulator | `http://localhost:8080/api/v1/` | Darwin |

La petición de la sesión es `GET productos?pagina=0&tamanio=20`. `10.0.2.2` representa la computadora anfitriona desde el emulador Android. El permiso `INTERNET` y las excepciones de tráfico HTTP en Android/iOS se limitan al backend local; en producción debe usarse HTTPS.

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
