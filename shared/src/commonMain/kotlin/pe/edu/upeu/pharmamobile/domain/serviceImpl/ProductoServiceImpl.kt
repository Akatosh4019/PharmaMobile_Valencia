package pe.edu.upeu.pharmamobile.domain.serviceImpl

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.model.ResultadoProductos
import pe.edu.upeu.pharmamobile.domain.service.ProductoService

class ProductoServiceImpl : ProductoService {

    private val productosSimulados = mutableListOf(
        Producto(1L, "Paracetamol", 5.50, 100),
        Producto(2L, "Ibuprofeno", 12.00, 50),
        Producto(3L, "Amoxicilina", 18.50, 20)
    )

    override fun listar(): List<Producto> = productosSimulados.toList()

    override fun buscarPorId(id: Long): Producto? =
        productosSimulados.find { it.id == id }

    override fun agregar(producto: Producto) {
        productosSimulados.add(producto)
    }

    override fun actualizar(producto: Producto): Boolean {
        val indice = productosSimulados.indexOfFirst { it.id == producto.id }
        if (indice == -1) return false
        productosSimulados[indice] = producto
        return true
    }

    override fun eliminar(id: Long): Boolean =
        productosSimulados.removeAll { it.id == id }

    override fun listarConStock(): List<Producto> =
        productosSimulados.filter { it.stock > 0 }

    override suspend fun obtenerProductos(): List<Producto> {
        delay(1000)
        return productosSimulados.toList()
    }

    override fun observarEstados(): Flow<String> = flow {
        emit("Iniciando")
        delay(500)
        emit("Procesando")
        delay(500)
        emit("Finalizado")
    }

    override fun observarProductos(): Flow<List<Producto>> = flow {
        emit(emptyList())
        delay(1000)
        emit(productosSimulados.toList())

        delay(500)
        val productosActualizados = productosSimulados.map { producto ->
            if (producto.id == 1L) producto.copy(stock = 90) else producto
        }
        emit(productosActualizados)
    }

    override fun cargarProductos(): Flow<ResultadoProductos> = flow {
        emit(ResultadoProductos.Cargando)

        try {
            val productos = obtenerProductos()
            emit(ResultadoProductos.Exito(productos))
        } catch (error: Exception) {
            emit(
                ResultadoProductos.Error(
                    error.message ?: "Error desconocido"
                )
            )
        }
    }
}
