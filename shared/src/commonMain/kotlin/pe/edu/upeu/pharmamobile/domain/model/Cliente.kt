package pe.edu.upeu.pharmamobile.domain.model

data class Cliente(
    val id: Long,
    val nombre: String,
    val correo: String,
    val telefono: String?,
){
    fun obtenertelefono(): String{
        return telefono ?: "No hay telefono registrado"
    }
}