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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.presentation.components.ValidatedTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoScreen(
    onRegistrar: (Producto) -> Unit = {}
) {
    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var nombreError by remember { mutableStateOf<String?>(null) }
    var precioError by remember { mutableStateOf<String?>(null) }
    var stockError by remember { mutableStateOf<String?>(null) }
    var mensajeExito by remember { mutableStateOf<String?>(null) }
    var tabSeleccionada by remember { mutableStateOf(0) }

    val productos = remember {
        mutableStateListOf(
            ProductoInventario(1, "Paracetamol", 15.50, 100, true),
            ProductoInventario(2, "Ibuprofeno", 18.90, 50, true),
            ProductoInventario(3, "Amoxicilina", 25.00, 5, true),
            ProductoInventario(4, "Loratadina", 12.50, 0, false),
            ProductoInventario(5, "Diclofenaco", 20.00, 3, true)
        )
    }

    val productosFiltrados = when (tabSeleccionada) {
        0 -> productos.filter { it.activo }
        1 -> productos.filter { !it.activo }
        else -> productos.filter { it.stock <= STOCK_BAJO }
    }

    fun validar(): Producto? {
        nombreError = ProductoValidator.validarNombre(nombre)
        precioError = ProductoValidator.validarPrecio(precio)
        stockError = ProductoValidator.validarStock(stock)

        if (nombreError != null || precioError != null || stockError != null) return null

        return Producto(
            id = 0L,
            nombre = nombre.trim(),
            precio = precio.toDouble(),
            stock = stock.toInt()
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
    ) {
        item {
            Text("PharmaMobil", style = MaterialTheme.typography.titleMedium)
            Text(
                "Registro de Producto",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
        }

        item {
            ValidatedTextField(nombre, { nombre = it }, "Nombre", nombreError)
            ValidatedTextField(precio, { precio = it }, "Precio", precioError)
            ValidatedTextField(stock, { stock = it }, "Stock", stockError)
        }

        item {
            Button(
                onClick = {
                    mensajeExito = null
                    val producto = validar()
                    if (producto != null) {
                        onRegistrar(producto)
                        productos.add(
                            0,
                            ProductoInventario(
                                id = (productos.maxOfOrNull { it.id } ?: 0) + 1,
                                nombre = producto.nombre,
                                precio = producto.precio,
                                stock = producto.stock,
                                activo = true
                            )
                        )
                        tabSeleccionada = if (producto.stock <= STOCK_BAJO) 2 else 0
                        mensajeExito = "Producto \"${producto.nombre}\" registrado correctamente"
                        nombre = ""
                        precio = ""
                        stock = ""
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Text("Registrar")
            }

            mensajeExito?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Text(
                text = "Inventario de productos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
            )

            PrimaryTabRow(selectedTabIndex = tabSeleccionada) {
                TITULOS_TABS.forEachIndexed { indice, titulo ->
                    Tab(
                        selected = tabSeleccionada == indice,
                        onClick = { tabSeleccionada = indice },
                        text = { Text(titulo) }
                    )
                }
            }
        }

        if (productosFiltrados.isEmpty()) {
            item {
                Text(
                    "No hay productos en esta categoría",
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
        } else {
            items(productosFiltrados, key = { it.id }) { producto ->
                ProductoItem(
                    producto = producto,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun ProductoItem(
    producto: ProductoInventario,
    modifier: Modifier = Modifier
) {
    val esBajoStock = producto.stock <= STOCK_BAJO
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (producto.activo) "Activo" else "Inactivo",
                    color = if (producto.activo) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Text(
                text = "Precio: S/ ${producto.precio}",
                modifier = Modifier.padding(top = 6.dp)
            )
            Text(text = "Stock: ${producto.stock}")

            if (esBajoStock) {
                Text(
                    text = if (producto.stock == 0) "Sin stock" else "Bajo stock",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

private data class ProductoInventario(
    val id: Long,
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val activo: Boolean
)

private const val STOCK_BAJO = 5
private val TITULOS_TABS = listOf("Activos", "Inactivos", "Bajo stock")
