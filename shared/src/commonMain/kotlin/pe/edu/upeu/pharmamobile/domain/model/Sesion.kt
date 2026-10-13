package pe.edu.upeu.pharmamobile.domain.model

data class Credenciales(val usuario: String, val clave: String) {
    override fun toString(): String = "Credenciales(usuario=$usuario, clave=***)"
}

data class Sesion(
    val token: String,
    val usuario: String,
    val nombreCompleto: String,
    val permisos: Set<String>,
) {
    fun puede(permiso: String): Boolean = permiso in permisos
    override fun toString(): String = "Sesion(token=***, usuario=$usuario, nombreCompleto=$nombreCompleto, permisos=$permisos)"
}
