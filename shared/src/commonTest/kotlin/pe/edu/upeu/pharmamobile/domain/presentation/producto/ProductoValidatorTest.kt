package pe.edu.upeu.pharmamobile.domain.presentation.producto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProductoValidatorTest {

    @Test
    fun datosCorrectosNoGeneranErrores() {
        assertNull(ProductoValidator.validarNombre("Paracetamol 500 mg"))
        assertNull(ProductoValidator.validarPrecio("8.50"))
        assertNull(ProductoValidator.validarStock("100"))
    }

    @Test
    fun nombreVacioGeneraError() {
        assertEquals(
            "El nombre es obligatorio",
            ProductoValidator.validarNombre("")
        )
    }

    @Test
    fun precioConTextoGeneraError() {
        assertEquals(
            "El precio debe ser numérico",
            ProductoValidator.validarPrecio("abc")
        )
    }

    @Test
    fun stockNegativoGeneraError() {
        assertEquals(
            "El stock no puede ser negativo",
            ProductoValidator.validarStock("-5")
        )
    }

    @Test
    fun precioCeroGeneraError() {
        assertEquals(
            "El precio debe ser mayor que cero",
            ProductoValidator.validarPrecio("0")
        )
    }

    @Test
    fun stockConTextoGeneraError() {
        assertEquals(
            "El stock debe ser un número entero",
            ProductoValidator.validarStock("abc")
        )
    }

    @Test
    fun stockCeroEsValido() {
        assertNull(ProductoValidator.validarNombre("Loratadina"))
        assertNull(ProductoValidator.validarPrecio("10"))
        assertNull(ProductoValidator.validarStock("0"))
    }
}
