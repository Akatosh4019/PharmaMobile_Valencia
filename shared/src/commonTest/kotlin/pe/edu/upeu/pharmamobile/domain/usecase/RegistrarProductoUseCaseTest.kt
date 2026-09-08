package pe.edu.upeu.pharmamobile.domain.usecase

import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RegistrarProductoUseCaseTest {
    private val repository = ProductoRepositoryFalso()
    private val useCase = RegistrarProductoUseCase(repository)

    @Test
    fun datosCorrectosRegistranProducto() = runTest {
        val resultado = useCase("Paracetamol 500 mg", "8.50", "100")

        assertTrue(resultado.isSuccess)
        assertEquals(1L, resultado.getOrThrow().id)
        assertEquals(1, repository.listar().size)
    }

    @Test
    fun nombreVacioGeneraError() {
        val errores = useCase.validar("", "8.50", "100")

        assertEquals("El nombre es obligatorio", errores.nombre)
    }

    @Test
    fun precioConTextoGeneraError() {
        val errores = useCase.validar("Ibuprofeno", "abc", "100")

        assertEquals("El precio debe ser numérico", errores.precio)
    }

    @Test
    fun precioCeroGeneraError() {
        val errores = useCase.validar("Ibuprofeno", "0", "100")

        assertEquals("El precio debe ser mayor que cero", errores.precio)
    }

    @Test
    fun stockNegativoGeneraError() {
        val errores = useCase.validar("Ibuprofeno", "12", "-5")

        assertEquals("El stock no puede ser negativo", errores.stock)
    }

    @Test
    fun stockConTextoGeneraError() {
        val errores = useCase.validar("Ibuprofeno", "12", "abc")

        assertEquals("El stock debe ser un número entero", errores.stock)
    }

    @Test
    fun stockCeroEsValido() {
        val errores = useCase.validar("Loratadina", "10", "0")

        assertNull(errores.nombre)
        assertNull(errores.precio)
        assertNull(errores.stock)
    }
}

private class ProductoRepositoryFalso : ProductoRepository {
    private val productos = mutableListOf<Producto>()

    override suspend fun registrar(producto: Producto): Producto =
        producto.copy(id = (productos.size + 1).toLong()).also(productos::add)

    override suspend fun listar(): List<Producto> = productos.toList()
}
