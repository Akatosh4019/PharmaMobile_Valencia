package pe.edu.upeu.pharmamobile.presentation.producto

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobile.presentation.components.ValidatedTextField
import pe.edu.upeu.pharmamobile.platform.InfoDispositivo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoScreen(viewModel: ProductoViewModel, puedeEscribir: Boolean = true) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(viewModel) { viewModel.alMostrarPantalla() }
    val formulario = uiState.formulario
    val tipoOperacion = (uiState.operacion as? ProductoOperacion.EnCurso)?.tipo
    val guardando = tipoOperacion == ProductoOperacion.Tipo.CREAR ||
        tipoOperacion == ProductoOperacion.Tipo.ACTUALIZAR
    val filtros = ProductoFiltro.entries
    val titulos = listOf("Activos", "Inactivos", "Bajo stock")
    val listState = rememberLazyListState()
    var categoriasAbiertas by remember { mutableStateOf(false) }
    var productoDetalle by remember { mutableStateOf<ProductoUi?>(null) }
    var mostrarAcercaDe by remember { mutableStateOf(false) }
    val infoDispositivo = remember { InfoDispositivo() }
    LaunchedEffect(formulario.editandoId) {
        if (formulario.editandoId != null) listState.animateScrollToItem(0)
    }

    if (puedeEscribir) uiState.productoPendienteEliminar?.let { producto ->
        AlertDialog(
            onDismissRequest = viewModel::cancelarEliminacion,
            title = { Text("Desactivar producto") },
            text = { Text("¿Desactivar ${producto.nombre}? Seguirá visible en Inactivos y podrás reactivarlo al editar.") },
            confirmButton = { TextButton(onClick = viewModel::confirmarEliminar) { Text("Desactivar") } },
            dismissButton = { TextButton(onClick = viewModel::cancelarEliminacion) { Text("Cancelar") } },
        )
    }

    productoDetalle?.let { item ->
        AlertDialog(
            onDismissRequest = { productoDetalle = null },
            title = { Text(item.producto.nombre) },
            text = {
                Column {
                    Text("Precio: ${item.precioFormateado}")
                    Text("Stock: ${item.producto.stock}")
                    item.producto.categoriaNombre?.let { Text("Categoría: $it") }
                    Text(if (item.producto.activo) "Activo" else "Inactivo")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    productoDetalle = null
                    viewModel.compartir(item)
                }) { Text("Compartir") }
            },
            dismissButton = {
                TextButton(onClick = { productoDetalle = null }) { Text("Cerrar") }
            },
        )
    }

    if (mostrarAcercaDe) {
        AlertDialog(
            onDismissRequest = { mostrarAcercaDe = false },
            title = { Text("Acerca de PharmaMobil") },
            text = {
                Column {
                    Text("Sistema operativo: ${infoDispositivo.sistema}")
                    Text("Versión: ${infoDispositivo.version}")
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarAcercaDe = false }) { Text("Cerrar") }
            },
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        state = listState,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 18.dp),
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Outlined.Inventory2,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp),
                    )
                    Column(modifier = Modifier.padding(start = 14.dp)) {
                        Text("Inventario", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Inventario disponible incluso sin conexión",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
            TextButton(onClick = { mostrarAcercaDe = true }) { Text("Acerca de este dispositivo") }
            if (puedeEscribir) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = MaterialTheme.shapes.large,
            ) {
            Column(modifier = Modifier.padding(18.dp)) {
            Text(
                if (formulario.editandoId == null) "Nuevo producto" else "Editar producto #${formulario.editandoId}",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Text(
                "Completa los datos del inventario",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 14.dp),
            )

            ValidatedTextField(formulario.nombre, viewModel::cambiarNombre, "Nombre", formulario.nombreError)
            ValidatedTextField(formulario.precio, viewModel::cambiarPrecio, "Precio", formulario.precioError)
            ValidatedTextField(formulario.stock, viewModel::cambiarStock, "Stock", formulario.stockError)

            Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                OutlinedButton(
                    onClick = { categoriasAbiertas = true },
                    enabled = uiState.categorias.isNotEmpty() && !guardando,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(uiState.categorias.firstOrNull { it.id == formulario.categoriaId }?.nombre ?: "Seleccionar categoría")
                }
                DropdownMenu(expanded = categoriasAbiertas, onDismissRequest = { categoriasAbiertas = false }) {
                    uiState.categorias.forEach { categoria ->
                        DropdownMenuItem(
                            text = { Text(categoria.nombre) },
                            onClick = {
                                viewModel.cambiarCategoria(categoria.id)
                                categoriasAbiertas = false
                            },
                        )
                    }
                }
            }
            formulario.categoriaError?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            if (uiState.categorias.isEmpty()) {
                TextButton(onClick = viewModel::cargarCategorias) { Text("Reintentar cargar categorías") }
            }

            if (formulario.editandoId != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = formulario.activo,
                        onCheckedChange = viewModel::cambiarActivo,
                        enabled = !guardando,
                    )
                    Text("Producto activo")
                }
            }

            Button(
                onClick = viewModel::guardar,
                enabled = !guardando && uiState.categorias.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp).height(48.dp),
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text(if (guardando) "Guardando..." else if (formulario.editandoId == null) "Crear producto" else "Guardar cambios")
            }
            if (formulario.editandoId != null) {
                TextButton(onClick = viewModel::cancelarEdicion, enabled = !guardando) {
                    Text("Cancelar edición")
                }
            }

            uiState.mensajeError?.let {
                Text("Error: $it", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }

            (uiState.operacion as? ProductoOperacion.Fallida)?.let {
                Text(it.mensaje, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }

            uiState.mensajeResultado?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            }
            }
            }

            Text(
                text = "Inventario de productos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp, bottom = 2.dp),
            )
            Text(
                "Explora, filtra y consulta tus productos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            Text(
                textoAntiguedadDatos(uiState.ultimaSincronizacionMillis),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            if (uiState.productos.isNotEmpty()) uiState.avisoRed?.let { error ->
                Text(
                    "Mostrando datos guardados. $error",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            OutlinedButton(
                onClick = viewModel::cargarProductos,
                enabled = uiState.fase !is ProductoFase.Cargando && !uiState.operando,
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
            ) {
                Icon(Icons.Outlined.Refresh, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Actualizar lista")
            }

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
                if (uiState.productosUiFiltrados.isEmpty()) {
                    item {
                        Text("No hay productos en esta categoría", modifier = Modifier.padding(vertical = 24.dp))
                    }
                } else {
                    items(uiState.productosUiFiltrados, key = { it.producto.id }) { productoUi ->
                        ProductoItem(
                            productoUi = productoUi,
                            onEditar = { viewModel.editar(productoUi.producto) },
                            onEliminar = { viewModel.solicitarEliminar(productoUi.producto) },
                            onDetalle = { productoDetalle = productoUi },
                            puedeEscribir = puedeEscribir,
                            operando = uiState.operando,
                            modifier = Modifier.padding(top = 10.dp),
                        )
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
private fun ProductoItem(
    productoUi: ProductoUi,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    onDetalle: () -> Unit,
    puedeEscribir: Boolean,
    operando: Boolean,
    modifier: Modifier = Modifier,
) {
    val producto = productoUi.producto
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Surface(
                    color = if (producto.activo) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = if (producto.activo) "Activo" else "Inactivo",
                        color = if (producto.activo) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                }
            }
            Text(
                productoUi.precioFormateado,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 10.dp),
            )
            if (producto.id < 0) {
                Text(
                    "Pendiente de sincronización",
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            Text("${producto.stock} unidades disponibles", style = MaterialTheme.typography.bodyMedium)
            producto.categoriaNombre?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (producto.requiereReposicion()) {
                Text(
                    text = if (producto.stock == 0) "Sin stock" else "Bajo stock",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                TextButton(onClick = onDetalle) { Text("Detalle") }
                if (puedeEscribir) {
                TextButton(onClick = onEditar, enabled = !operando) { Text("Editar") }
                if (producto.activo) {
                    TextButton(onClick = onEliminar, enabled = !operando) { Text("Desactivar") }
                }
                }
            }
        }
    }
}
