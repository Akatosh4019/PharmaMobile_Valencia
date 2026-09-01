package pe.edu.upeu.pharmamobile.presentation.inicio
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import pharmamobile.shared.generated.resources.Res
import pharmamobile.shared.generated.resources.pharmamobil_logo

@Composable
fun InicioScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Image(
            painter = painterResource(Res.drawable.pharmamobil_logo),
            contentDescription = "Logo corporativo de PharmaMobil",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(140.dp)
                .padding(bottom = 20.dp)
        )

        Text(
            text = "PharmaMobil",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Sistema de gestión farmacéutica",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
