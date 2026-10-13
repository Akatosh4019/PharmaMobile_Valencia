package pe.edu.upeu.pharmamobile.platform

/** Información del sistema obtenida con la API nativa de cada plataforma. */
expect class InfoDispositivo() {
    val sistema: String
    val version: String
}
