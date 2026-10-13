package pe.edu.upeu.pharmamobile.presentation.sesion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(viewModel: SesionViewModel) {
    val estado by viewModel.formulario.collectAsState()
    val aviso by viewModel.aviso.collectAsState()
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("PharmaMobil", style = MaterialTheme.typography.headlineMedium)
        Text("Iniciar sesión", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 24.dp))
        aviso?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        OutlinedTextField(
            value = estado.usuario,
            onValueChange = viewModel::cambiarUsuario,
            label = { Text("Usuario") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = estado.clave,
            onValueChange = viewModel::cambiarClave,
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        )
        estado.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }
        Button(
            onClick = viewModel::ingresar,
            enabled = !estado.cargando,
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
        ) {
            if (estado.cargando) CircularProgressIndicator() else Text("Ingresar")
        }
    }
}
