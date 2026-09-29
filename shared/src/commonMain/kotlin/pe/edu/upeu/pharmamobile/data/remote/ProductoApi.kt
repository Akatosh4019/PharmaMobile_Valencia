package pe.edu.upeu.pharmamobile.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.delete
import io.ktor.client.request.setBody
import pe.edu.upeu.pharmamobile.data.remote.dto.CategoriaDto
import pe.edu.upeu.pharmamobile.data.remote.dto.PaginaProductosDto
import pe.edu.upeu.pharmamobile.data.remote.dto.ProductoDto
import pe.edu.upeu.pharmamobile.data.remote.dto.ProductoRequestDto

class ProductoApi(private val client: HttpClient) {
    suspend fun obtenerProductos(pagina: Int, tamanio: Int = 20): PaginaProductosDto =
        client.get("productos") {
            url {
                parameters.append("pagina", pagina.toString())
                parameters.append("tamanio", tamanio.toString())
            }
        }.body()

    suspend fun obtenerCategorias(): List<CategoriaDto> = client.get("categorias").body()

    suspend fun crear(request: ProductoRequestDto): ProductoDto =
        client.post("productos") { setBody(request) }.body()

    suspend fun actualizar(id: Long, request: ProductoRequestDto): ProductoDto =
        client.put("productos/$id") { setBody(request) }.body()

    suspend fun eliminar(id: Long) {
        client.delete("productos/$id")
    }
}
