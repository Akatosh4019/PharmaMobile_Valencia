package pe.edu.upeu.pharmamobile.presentation.producto

/** Prepara el texto visible en la hoja nativa sin filtrar APIs de plataforma. */
fun ProductoUi.comoTextoParaCompartir(): String = buildString {
    appendLine(producto.nombre)
    appendLine("Precio: $precioFormateado")
    append("Stock: ${producto.stock}")
}
