package pe.edu.upeu.pharmamobile.data.local

import kotlin.io.encoding.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Clock

class SesionDesdeJwtTest {
    @Test
    fun tokenVigentePermiteLeerInventarioSinRed() {
        val sesion = sesionVigenteSinRed(token(Clock.System.now().epochSeconds + 3600))
        assertEquals("admin", sesion?.usuario)
        assertEquals(setOf("CATALOGO_LEER"), sesion?.permisos)
    }

    @Test
    fun tokenVencidoNoAbreLaSesionOffline() {
        assertNull(sesionVigenteSinRed(token(Clock.System.now().epochSeconds - 1)))
    }

    private fun token(expira: Long): String {
        val cuerpo = """{"sub":"admin","authorities":["CATALOGO_LEER"],"exp":$expira}"""
        return "cabecera.${Base64.UrlSafe.encode(cuerpo.encodeToByteArray())}.firma"
    }
}
