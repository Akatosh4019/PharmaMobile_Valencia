package pe.edu.upeu.pharmamobile.data.repository

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobile.data.local.EstadoSesion
import pe.edu.upeu.pharmamobile.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobile.domain.model.Sesion
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CierreSesionSeguroTest {
    @Test
    fun salirBorraTokenPersistidoYCacheDelClienteHttp() = runTest {
        val almacen = AlmacenPrueba().apply { guardarToken("jwt-de-prueba") }
        val estado = EstadoSesion(almacen).apply {
            activar(Sesion("jwt-de-prueba", "admin", "Admin", emptySet()))
        }
        val cabeceras = mutableListOf<String?>()
        val motor = MockEngine { solicitud ->
            cabeceras += solicitud.headers[HttpHeaders.Authorization]
            respond("{}", HttpStatusCode.OK)
        }
        val cliente = crearHttpClient(motor, "http://localhost/api/v1/", almacen, estado, false)
        try {
            cliente.get("productos")
            AuthRepositorioRest(cliente, almacen, estado).cerrarSesion()
            cliente.get("productos")

            assertEquals("Bearer jwt-de-prueba", cabeceras[0])
            assertNull(cabeceras[1], "La siguiente petición no debe enviar Authorization")
            assertNull(almacen.leerToken())
            assertNull(estado.sesion.value)
        } finally {
            cliente.close()
        }
    }

    private class AlmacenPrueba : AlmacenSeguro {
        private var token: String? = null
        override fun guardarToken(token: String) { this.token = token }
        override fun leerToken(): String? = token
        override fun borrarToken() { token = null }
    }
}
