package pe.edu.upeu.pharmamobile.domain.presentation.producto

object ProductoValidator {

    fun validarNombre(nombre: String): String? {
        return if (nombre.isBlank()) "El nombre es obligatorio" else null
    }

    fun validarPrecio(precio: String): String? {
        val precioNumerico = precio.toDoubleOrNull()
        return when {
            precio.isBlank() -> "El precio es obligatorio"
            precioNumerico == null -> "El precio debe ser numérico"
            precioNumerico <= 0 -> "El precio debe ser mayor que cero"
            else -> null
        }
    }

    fun validarStock(stock: String): String? {
        val stockNumerico = stock.toIntOrNull()
        return when {
            stock.isBlank() -> "El stock es obligatorio"
            stockNumerico == null -> "El stock debe ser un número entero"
            stockNumerico < 0 -> "El stock no puede ser negativo"
            else -> null
        }
    }
}
