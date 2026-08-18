package pe.edu.upeu.pharmamobile.domain.model

import pharmamobile.shared.generated.resources.Res

data class Producto(
    val id: Long,
    val nombre: String,
    val precio: Double,
    val stock: Int,
)
