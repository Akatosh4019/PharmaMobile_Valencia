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
import kotlinx.coroutines.CancellationException
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
    fun error401SeConvierteEnSesionNoAutenticada() = runTest {
        val client = cliente(HttpStatusCode.Unauthorized, """{"status":401,"message":"Token inválido"}""")
        try {
            val error = assertFailsWith<ErrorApiException> { ProductoApi(client).obtener(1L) }
            assertIs<ErrorApi.NoAutenticado>(error.error)
        } finally {
            client.close()
        }
    }

    @Test
    fun error403SeConvierteEnPermisoDenegado() = runTest {
        val client = cliente(HttpStatusCode.Forbidden, """{"status":403,"message":"Sin permiso"}""")
        try {
            val error = assertFailsWith<ErrorApiException> {
                ProductoApi(client).crear(ProductoRequestDto("Producto", 10.0, 1, true, 1L))
            }
            assertIs<ErrorApi.SinPermiso>(error.error)
        } finally {
            client.close()
        }
    }

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

    @Test
    fun actualizarIdInexistenteSeTraduceANoEncontrado() = runTest {
        val client = cliente(HttpStatusCode.NotFound, """{"status":404,"message":"Producto no encontrado con id: 99999999"}""")
        try {
            val error = assertFailsWith<ErrorApiException> {
                ProductoApi(client).actualizar(
                    99999999L,
                    ProductoRequestDto("Inexistente", 1.01, 1, true, 1L),
                )
            }
            assertIs<ErrorApi.NoEncontrado>(error.error)
        } finally {
            client.close()
        }
    }

    @Test
    fun precioCeroDevuelveValidacionPorCampo() = runTest {
        val client = cliente(HttpStatusCode.BadRequest, """{
            "status":400,"message":"Existen errores de validación",
            "validationErrors":{"precio":"El precio debe ser mayor o igual a 0.01"}
        }""")
        try {
            val error = assertFailsWith<ErrorApiException> {
                ProductoApi(client).crear(ProductoRequestDto("Prueba precio", 0.0, 1, true, 1L))
            }
            val validacion = assertIs<ErrorApi.Validacion>(error.error)
            assertEquals("El precio debe ser mayor o igual a 0.01", validacion.porCampo["precio"])
        } finally {
            client.close()
        }
    }

    @Test
    fun segundaDesactivacionSeTraduceAConflicto() = runTest {
        val client = cliente(HttpStatusCode.Conflict, """{
            "status":409,"message":"El producto nuevo ya se encuentra inactivo"
        }""")
        try {
            val error = assertFailsWith<ErrorApiException> { ProductoApi(client).eliminar(81L) }
            val conflicto = assertIs<ErrorApi.Conflicto>(error.error)
            assertEquals("El producto nuevo ya se encuentra inactivo", conflicto.mensaje)
        } finally {
            client.close()
        }
    }

    @Test
    fun cancelacionNoSeTraduceAErrorDeRed() = runTest {
        assertFailsWith<CancellationException> {
            ejecutarLlamada<Unit> { throw CancellationException("Pantalla cerrada") }
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
