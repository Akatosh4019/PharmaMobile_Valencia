package pe.edu.upeu.pharmamobile.domain.presentation.producto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobile.domain.model.Producto

@Composable
fun ProductoScreen() {

    var nombre by remember {
        mutableStateOf("")
    }

    var precio by remember {
        mutableStateOf("")
    }

    var stock by remember {
        mutableStateOf("")
    }

    var nombreError by remember {
        mutableStateOf<String?>(null)
    }

    var precioError by remember {
        mutableStateOf<String?>(null)
    }

    var stockError by remember {
        mutableStateOf<String?>(null)
    }

    var mensajeResultado by remember {
        mutableStateOf<String?>(null)
    }

    var intentoRegistrar by remember {
        mutableStateOf(false)
    }

    fun validarFormulario(): Boolean {
        nombreError = ProductoValidator.validarNombre(nombre)
        precioError = ProductoValidator.validarPrecio(precio)
        stockError = ProductoValidator.validarStock(stock)

        return nombreError == null && precioError == null && stockError == null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text("PharmaMobil")
        Text("Registro de Producto")

        OutlinedTextField(
            value = nombre,
            onValueChange = {
                nombre = it
                if (intentoRegistrar) {
                    nombreError = ProductoValidator.validarNombre(it)
                }
            },
            label = {
                Text("Nombre")
            },
            isError = intentoRegistrar && nombreError != null,
            supportingText = {
                if (intentoRegistrar) {
                    nombreError?.let { Text(it) }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = precio,
            onValueChange = {
                precio = it
                if (intentoRegistrar) {
                    precioError = ProductoValidator.validarPrecio(it)
                }
            },
            label = {
                Text("Precio")
            },
            isError = intentoRegistrar && precioError != null,
            supportingText = {
                if (intentoRegistrar) {
                    precioError?.let { Text(it) }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = stock,
            onValueChange = {
                stock = it
                if (intentoRegistrar) {
                    stockError = ProductoValidator.validarStock(it)
                }
            },
            label = {
                Text("Stock")
            },
            isError = intentoRegistrar && stockError != null,
            supportingText = {
                if (intentoRegistrar) {
                    stockError?.let { Text(it) }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                mensajeResultado = null
                intentoRegistrar = true

                if (validarFormulario()) {
                    val producto = Producto(
                        id = 1L,
                        nombre = nombre.trim(),
                        precio = precio.toDouble(),
                        stock = stock.toInt()
                    )

                    mensajeResultado =
                        "Producto ${producto.nombre} registrado correctamente"

                    nombre = ""
                    precio = ""
                    stock = ""
                    intentoRegistrar = false
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar")
        }

        mensajeResultado?.let {
            Text(it)
        }
    }
}
