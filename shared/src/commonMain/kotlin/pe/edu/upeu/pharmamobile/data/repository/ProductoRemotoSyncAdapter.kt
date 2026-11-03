package pe.edu.upeu.pharmamobile.data.repository

import pe.edu.upeu.pharmamobile.data.mapper.aResultadoEnvio
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.model.ResultadoEnvio
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRemotoSyncRepository
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

/** Expone al caso de uso solo las operaciones remotas necesarias para sincronizar. */
class ProductoRemotoSyncAdapter(
    private val remoto: ProductoRepository,
) : ProductoRemotoSyncRepository {
    override suspend fun enviar(producto: Producto): ResultadoEnvio = try {
        ResultadoEnvio.Exito(remoto.registrar(producto.copy(id = 0)))
    } catch (error: Exception) {
        error.aResultadoEnvio()
    }

    override suspend fun listar(): List<Producto> = remoto.listar()
}
