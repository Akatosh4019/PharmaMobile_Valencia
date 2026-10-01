package pe.edu.upeu.pharmamobile.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import pe.edu.upeu.pharmamobile.data.remote.dto.ProductoRequestDto
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertFailsWith

class ProductoApiErrorTest {
    @Test
    fun error400TraduceValidacionPorCampo() = runTest {
        val client = cliente(HttpStatusCode.BadRequest, """{
            "status":400,"message":"Existen errores de validación",
            "validationErrors":{"nombre":"El nombre debe tener entre 3 y 150 caracteres"}
        }""")
        try {
            val error = assertFailsWith<ErrorApiException> {
                ProductoApi(client).crear(ProductoRequestDto("AB", 10.0, 1, true, 1L))
            }
            val validacion = assertIs<ErrorApi.Validacion>(error.error)
            assertEquals("El nombre debe tener entre 3 y 150 caracteres", validacion.porCampo["nombre"])
        } finally {
            client.close()
        }
    }

    @Test
    fun error404SeConvierteEnNoEncontrado() = runTest {
        val client = cliente(HttpStatusCode.NotFound, """{"status":404,"message":"Producto no encontrado"}""")
        try {
            val error = assertFailsWith<ErrorApiException> { ProductoApi(client).obtener(999999L) }
            assertIs<ErrorApi.NoEncontrado>(error.error)
        } finally {
            client.close()
        }
    }

    private fun cliente(status: HttpStatusCode, jsonError: String): HttpClient = HttpClient(
        MockEngine { respond(jsonError, status, headersOf(HttpHeaders.ContentType, "application/json")) }
    ) {
        expectSuccess = true
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        defaultRequest {
            url("http://localhost/api/v1/")
            contentType(ContentType.Application.Json)
        }
    }
}
