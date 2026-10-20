package pe.edu.upeu.pharmamobile.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobile.data.local.PharmaDatabase
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobile.domain.model.Categoria
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProductoRepositorioOfflineTest {
    private fun crearBase(): PharmaDatabase {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        PharmaDatabase.Schema.create(driver)
        return PharmaDatabase(driver)
    }

    @Test
    fun conRedLaSincronizacionActualizaLaBaseObservable() = runTest {
        val remoto = RemotoFalso().apply { productos += Producto(7, "Paracetamol", 15.5, 20) }
        val repo = ProductoRepositorioOffline(remoto, crearBase())
        repo.sincronizar()
        assertEquals("Paracetamol", repo.observar().first().single().nombre)
    }

    @Test
    fun sinRedElProductoQuedaEnLaBaseConIdTemporal() = runTest {
        val remoto = RemotoFalso().apply { conectado = false }
        val repo = ProductoRepositorioOffline(remoto, crearBase())
        val creado = repo.registrar(Producto(0, "Ibuprofeno", 4.5, 20, categoriaId = 1))
        assertTrue(creado.id < 0)
        assertEquals(creado, repo.observar().first().single())
    }

    @Test
    fun alReconectarSubePendienteAntesDeBajarYConservaElProducto() = runTest {
        val remoto = RemotoFalso().apply { conectado = false }
        val repo = ProductoRepositorioOffline(remoto, crearBase())
        repo.registrar(Producto(0, "Ibuprofeno", 4.5, 20, categoriaId = 1))
        remoto.conectado = true
        repo.sincronizar()
        assertEquals(listOf("registrar", "listar"), remoto.operaciones)
        assertEquals(1, repo.listar().size)
        assertTrue(repo.listar().single().id > 0)
    }
}

private class RemotoFalso : ProductoRepository {
    var conectado = true
    val productos = mutableListOf<Producto>()
    val operaciones = mutableListOf<String>()

    private fun exigirRed() {
        if (!conectado) throw ErrorApiException(ErrorApi.SinConexion)
    }

    override suspend fun registrar(producto: Producto): Producto {
        exigirRed()
        operaciones += "registrar"
        return producto.copy(id = (productos.maxOfOrNull { it.id } ?: 0) + 1).also(productos::add)
    }

    override suspend fun listar(): List<Producto> {
        exigirRed()
        operaciones += "listar"
        return productos.toList()
    }

    override suspend fun obtener(id: Long) = productos.first { it.id == id }
    override suspend fun listarCategorias() = listOf(Categoria(1, "Medicamentos"))
    override suspend fun actualizar(producto: Producto) = producto
    override suspend fun eliminar(id: Long) = Unit
}
