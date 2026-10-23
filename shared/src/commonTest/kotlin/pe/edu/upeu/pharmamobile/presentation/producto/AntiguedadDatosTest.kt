package pe.edu.upeu.pharmamobile.presentation.producto

import kotlin.test.Test
import kotlin.test.assertEquals

class AntiguedadDatosTest {
    @Test
    fun sinPrimeraSincronizacionNoMuestraFechaVacia() {
        assertEquals("Nunca se ha sincronizado", textoAntiguedadDatos(null, 0))
    }

    @Test
    fun datosRecientesSeExpresanEnMinutos() {
        assertEquals("Actualizado hace 5 minutos", textoAntiguedadDatos(0, 5 * 60_000L))
    }

    @Test
    fun datosDeMasDeUnDiaSeExpresanEnDias() {
        assertEquals("Actualizado hace 2 días", textoAntiguedadDatos(0, 2 * 24 * 60 * 60_000L))
    }
}
