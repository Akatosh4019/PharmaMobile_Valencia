package pe.edu.upeu.pharmamobile.data

import kotlinx.serialization.json.Json
import pe.edu.upeu.pharmamobile.data.mapper.aDominio
import pe.edu.upeu.pharmamobile.data.remote.dto.PaginaProductosDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ProductoDtoTest {
    @Test
    fun `interpreta la respuesta paginada del backend y mapea el estado`() {
        val json = """{
            "contenido": [{
                "id": 7, "nombre": "Amoxicilina", "precio": 25.0,
                "stock": 5, "estado": false, "categoriaNombre": "Medicamentos"
            }],
            "pagina": 0, "tamanio": 20, "totalElementos": 1,
            "totalPaginas": 1, "ultima": true
        }"""
        val pagina = Json { ignoreUnknownKeys = true }
            .decodeFromString<PaginaProductosDto>(json)

        assertEquals(1, pagina.contenido.size)
        assertEquals(20, pagina.tamanio)
        assertEquals(1, pagina.totalElementos)
        val producto = pagina.contenido.single().aDominio()
        assertEquals("Amoxicilina", producto.nombre)
        assertFalse(producto.activo)
    }
}
