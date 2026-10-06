package pe.edu.upeu.pharmamobile.domain.platform

/** Contrato común para abrir la opción nativa de compartir texto. */
interface Compartidor {
    fun compartir(texto: String)
}
