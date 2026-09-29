package pe.edu.upeu.pharmamobile.domain.usecase

import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

data class ErroresProducto(
    val nombre: String? = null,
    val precio: String? = null,
    val stock: String? = null,
    val categoria: String? = null,
) {
    val tieneErrores: Boolean
        get() = nombre != null || precio != null || stock != null || categoria != null
}

class ProductoInvalidoException(
    val errores: ErroresProducto,
) : IllegalArgumentException("Los datos del producto no son válidos")

class RegistrarProductoUseCase(
    private val repository: ProductoRepository,
) {
    fun validar(nombre: String, precio: String, stock: String, categoriaId: Long? = null): ErroresProducto {
        val precioNumerico = precio.toDoubleOrNull()
        val stockNumerico = stock.toIntOrNull()

        return ErroresProducto(
            nombre = when {
                nombre.isBlank() -> "El nombre es obligatorio"
                nombre.trim().length !in 3..150 -> "El nombre debe tener entre 3 y 150 caracteres"
                else -> null
            },
            precio = when {
                precio.isBlank() -> "El precio es obligatorio"
                precioNumerico == null -> "El precio debe ser numérico"
                !precioNumerico.isFinite() || precioNumerico < 0.01 -> "El precio debe ser al menos 0.01"
                else -> null
            },
            categoria = if (categoriaId == null || categoriaId <= 0) "Selecciona una categoría" else null,
            stock = when {
                stock.isBlank() -> "El stock es obligatorio"
                stockNumerico == null -> "El stock debe ser un número entero"
                stockNumerico < 0 -> "El stock no puede ser negativo"
                else -> null
            },
        )
    }

    suspend operator fun invoke(
        nombre: String,
        precio: String,
        stock: String,
        categoriaId: Long? = null,
    ): Result<Producto> {
        val errores = validar(nombre, precio, stock, categoriaId)
        if (errores.tieneErrores) {
            return Result.failure(ProductoInvalidoException(errores))
        }

        return runCatching {
            repository.registrar(
                Producto(
                    id = 0L,
                    nombre = nombre.trim(),
                    precio = precio.toDouble(),
                    stock = stock.toInt(),
                    categoriaId = requireNotNull(categoriaId),
                )
            )
        }
    }
}
