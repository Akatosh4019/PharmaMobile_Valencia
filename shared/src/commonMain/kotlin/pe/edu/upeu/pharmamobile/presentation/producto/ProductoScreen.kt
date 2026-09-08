package pe.edu.upeu.pharmamobile.presentation.producto

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.presentation.components.ValidatedTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoScreen(viewModel: ProductoViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val formulario = uiState.formulario
    val filtros = ProductoFiltro.entries
    val titulos = listOf("Activos", "Inactivos", "Bajo stock")

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
    ) {
        item {
            Text("PharmaMobil", style = MaterialTheme.typography.titleMedium)
            Text(
                "Registro de Producto",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            )

            ValidatedTextField(formulario.nombre, viewModel::cambiarNombre, "Nombre", formulario.nombreError)
            ValidatedTextField(formulario.precio, viewModel::cambiarPrecio, "Precio", formulario.precioError)
            ValidatedTextField(formulario.stock, viewModel::cambiarStock, "Stock", formulario.stockError)

            Button(
                onClick = viewModel::registrar,
                enabled = uiState.fase !is ProductoFase.Cargando,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            ) {
                Text("Registrar")
            }

            uiState.mensajeResultado?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            Text(
                text = "Inventario de productos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
            )

            PrimaryTabRow(selectedTabIndex = filtros.indexOf(uiState.filtro)) {
                filtros.forEachIndexed { indice, filtro ->
                    Tab(
                        selected = uiState.filtro == filtro,
                        onClick = { viewModel.cambiarFiltro(filtro) },
                        text = { Text(titulos[indice]) },
                    )
                }
            }
        }

        when (val fase = uiState.fase) {
            ProductoFase.Cargando -> item {
                CircularProgressIndicator(modifier = Modifier.padding(24.dp))
            }

            ProductoFase.SinProductos -> item {
                Text("Aún no hay productos registrados", modifier = Modifier.padding(vertical = 24.dp))
            }

            is ProductoFase.ConProductos -> {
                if (uiState.productosFiltrados.isEmpty()) {
                    item {
                        Text("No hay productos en esta categoría", modifier = Modifier.padding(vertical = 24.dp))
                    }
                } else {
                    items(uiState.productosFiltrados, key = { it.id }) { producto ->
                        ProductoItem(producto, Modifier.padding(top = 10.dp))
                    }
                }
            }

            is ProductoFase.Error -> item {
                Text("Error: ${fase.mensaje}", color = MaterialTheme.colorScheme.error)
                Button(onClick = viewModel::cargarProductos) { Text("Reintentar") }
            }
        }
    }
}

@Composable
private fun ProductoItem(producto: Producto, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = if (producto.activo) "Activo" else "Inactivo",
                    color = if (producto.activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Text("Precio: S/ ${producto.precio}", modifier = Modifier.padding(top = 6.dp))
            Text("Stock: ${producto.stock}")
            if (producto.requiereReposicion()) {
                Text(
                    text = if (producto.stock == 0) "Sin stock" else "Bajo stock",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}
