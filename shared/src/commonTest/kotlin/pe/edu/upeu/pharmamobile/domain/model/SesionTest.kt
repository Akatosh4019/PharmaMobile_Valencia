package pe.edu.upeu.pharmamobile.domain.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SesionTest {
    @Test
    fun permisosSeAplicanPorAccion() {
        val sesion = Sesion("jwt-secreto", "vendedor", "Vendedor", setOf("CATALOGO_LEER"))
        assertTrue(sesion.puede("CATALOGO_LEER"))
        assertFalse(sesion.puede("CATALOGO_ESCRIBIR"))
    }

    @Test
    fun secretosNoSeExponenEnToString() {
        assertFalse(Credenciales("admin", "clave-secreta").toString().contains("clave-secreta"))
        assertFalse(Sesion("jwt-secreto", "admin", "Admin", emptySet()).toString().contains("jwt-secreto"))
    }
}
