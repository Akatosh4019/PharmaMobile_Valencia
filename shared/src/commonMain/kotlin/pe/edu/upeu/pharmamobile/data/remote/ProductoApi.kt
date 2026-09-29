package pe.edu.upeu.pharmamobile.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import pe.edu.upeu.pharmamobile.data.remote.dto.PaginaProductosDto

class ProductoApi(private val client: HttpClient) {
    suspend fun obtenerProductos(pagina: Int, tamanio: Int = 20): PaginaProductosDto =
        client.get("productos") {
            url {
                parameters.append("pagina", pagina.toString())
                parameters.append("tamanio", tamanio.toString())
            }
        }.body()
}
