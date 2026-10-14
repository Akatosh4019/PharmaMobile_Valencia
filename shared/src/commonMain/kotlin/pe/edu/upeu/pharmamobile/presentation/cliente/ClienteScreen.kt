package pe.edu.upeu.pharmamobile.presentation.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobile.presentation.components.ValidatedTextField

@Composable
fun ClienteScreen() {

    var nombre by remember {
        mutableStateOf("")
    }

    var correo by remember {
        mutableStateOf("")
    }

    var telefono by remember {
        mutableStateOf("")
    }

    var nombreError by remember {
        mutableStateOf<String?>(null)
    }

    var correoError by remember {
        mutableStateOf<String?>(null)
    }

    var telefonoError by remember {
        mutableStateOf<String?>(null)
    }

    var mensajeExito by remember {
        mutableStateOf<String?>(null)
    }

    fun validar(): Boolean {
        nombreError = ClienteValidator.validarNombre(nombre)
        correoError = ClienteValidator.validarCorreo(correo)
        telefonoError = ClienteValidator.validarTelefono(telefono)

        return nombreError == null && correoError == null && telefonoError == null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        Text("Clientes", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Organiza la información de contacto",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
        Text("Nuevo cliente", style = MaterialTheme.typography.titleLarge)
        Text(
            "Completa sus datos para continuar",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        ValidatedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = "Nombre",
            error = nombreError,
            modifier = Modifier.fillMaxWidth()
        )

        ValidatedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = "Correo",
            error = correoError,
            modifier = Modifier.fillMaxWidth()
        )

        ValidatedTextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = "Teléfono (opcional)",
            error = telefonoError,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                mensajeExito = null
                if (validar()) {
                    mensajeExito = "Cliente \"$nombre\" registrado correctamente"
                    nombre = ""
                    correo = ""
                    telefono = ""
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(48.dp)
        ) {
            Text("Registrar cliente")
        }

        mensajeExito?.let {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(12.dp),
                )
            }
        }
        }
        }
    }
}
