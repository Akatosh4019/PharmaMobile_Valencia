package pe.edu.upeu.pharmamobile.domain.usecase

import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

class ObtenerProductoUseCase(private val repository: ProductoRepository) {
    suspend operator fun invoke(id: Long): Result<Producto> = runCatching { repository.obtener(id) }
}

class ActualizarProductoUseCase(private val repository: ProductoRepository) {
    suspend operator fun invoke(producto: Producto): Result<Producto> =
        runCatching { repository.actualizar(producto) }
}

class EliminarProductoUseCase(private val repository: ProductoRepository) {
    suspend operator fun invoke(id: Long): Result<Unit> = runCatching { repository.eliminar(id) }
}
