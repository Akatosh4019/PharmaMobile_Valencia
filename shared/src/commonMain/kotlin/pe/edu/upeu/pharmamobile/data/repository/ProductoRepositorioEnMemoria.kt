package pe.edu.upeu.pharmamobile.data.repository

import kotlinx.coroutines.delay
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.model.Categoria
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

class ProductoRepositorioEnMemoria : ProductoRepository {
    private val productos = mutableListOf(
        Producto(1L, "Paracetamol", 15.50, 100),
        Producto(2L, "Ibuprofeno", 18.90, 50),
        Producto(3L, "Amoxicilina", 25.00, 5),
        Producto(4L, "Loratadina", 12.50, 0, activo = false),
        Producto(5L, "Diclofenaco", 20.00, 3),
    )
    private var siguienteId = 6L

    override suspend fun registrar(producto: Producto): Producto {
        delay(500)
        val productoRegistrado = producto.copy(id = siguienteId++)
        productos.add(productoRegistrado)
        return productoRegistrado
    }

    override suspend fun listar(): List<Producto> {
        delay(500)
        return productos.toList()
    }

    override suspend fun listarCategorias(): List<Categoria> = listOf(Categoria(1L, "Medicamentos"))

    override suspend fun actualizar(producto: Producto): Producto {
        val indice = productos.indexOfFirst { it.id == producto.id }
        require(indice >= 0) { "Producto no encontrado" }
        productos[indice] = producto
        return producto
    }

    override suspend fun eliminar(id: Long) {
        val indice = productos.indexOfFirst { it.id == id }
        require(indice >= 0) { "Producto no encontrado" }
        productos[indice] = productos[indice].copy(activo = false)
    }
}
