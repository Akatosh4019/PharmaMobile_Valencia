package pe.edu.upeu.pharmamobile.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.Logger
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobile.data.local.EstadoSesion

expect fun loggerHttp(): Logger

fun crearHttpClient(
    engine: HttpClientEngine,
    baseUrl: String,
    almacen: AlmacenSeguro,
    estadoSesion: EstadoSesion,
    esDepuracion: Boolean,
): HttpClient = HttpClient(engine) {
    expectSuccess = true
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        })
    }
    install(Logging) {
        logger = loggerHttp()
        level = if (esDepuracion) LogLevel.HEADERS else LogLevel.NONE
        sanitizeHeader { it == HttpHeaders.Authorization }
    }
    install(Auth) {
        bearer {
            loadTokens { almacen.leerToken()?.let { BearerTokens(it, "") } }
            sendWithoutRequest { request -> !request.url.build().encodedPath.endsWith("/auth/login") }
        }
    }
    HttpResponseValidator {
        handleResponseExceptionWithRequest { exception, request ->
            if (exception is ClientRequestException &&
                exception.response.status.value == 401 &&
                !request.url.encodedPath.endsWith("/auth/login")
            ) {
                estadoSesion.limpiar("Tu sesión expiró. Inicia sesión nuevamente")
            }
        }
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 15_000
        connectTimeoutMillis = 10_000
    }
    defaultRequest {
        url(baseUrl)
        contentType(ContentType.Application.Json)
    }
}
