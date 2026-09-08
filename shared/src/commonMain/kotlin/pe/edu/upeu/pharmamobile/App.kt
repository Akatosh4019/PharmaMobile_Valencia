package pe.edu.upeu.pharmamobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.pharmamobile.navigation.Screen
import pe.edu.upeu.pharmamobile.presentation.cliente.ClienteScreen
import pe.edu.upeu.pharmamobile.presentation.inicio.InicioScreen
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoScreen
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoViewModel
import pe.edu.upeu.pharmamobile.theme.PharmaMobilTheme

private const val COMPACT_MAX_WIDTH = 600
private const val MEDIUM_MAX_WIDTH = 840

private data class Destino(
    val screen: Screen,
    val titulo: String,
    val icono: ImageVector
)

private val destinos = listOf(
    Destino(Screen.Inicio, "Inicio", Icons.Default.Home),
    Destino(Screen.Productos, "Productos", Icons.Default.Medication),
    Destino(Screen.Clientes, "Clientes", Icons.Default.Person),
    Destino(Screen.Pedidos, "Pedidos", Icons.Default.ShoppingCart)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    var pantallaActual by remember { mutableStateOf<Screen>(Screen.Inicio) }
    var darkTheme by remember { mutableStateOf(false) }

    KoinContext {
        PharmaMobilTheme(darkTheme = darkTheme) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                when {
                    maxWidth < COMPACT_MAX_WIDTH.dp -> CompactNavigation(
                        pantallaActual = pantallaActual,
                        onPantallaSeleccionada = { pantallaActual = it },
                        darkTheme = darkTheme,
                        onDarkThemeChange = { darkTheme = it }
                    )

                    maxWidth < MEDIUM_MAX_WIDTH.dp -> MediumNavigation(
                        availableWidth = maxWidth,
                        pantallaActual = pantallaActual,
                        onPantallaSeleccionada = { pantallaActual = it },
                        darkTheme = darkTheme,
                        onDarkThemeChange = { darkTheme = it }
                    )

                    else -> ExpandedNavigation(
                        pantallaActual = pantallaActual,
                        onPantallaSeleccionada = { pantallaActual = it },
                        darkTheme = darkTheme,
                        onDarkThemeChange = { darkTheme = it }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactNavigation(
    pantallaActual: Screen,
    onPantallaSeleccionada: (Screen) -> Unit,
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(
                    pantallaActual = pantallaActual,
                    onPantallaSeleccionada = {
                        onPantallaSeleccionada(it)
                        scope.launch { drawerState.close() }
                    },
                    darkTheme = darkTheme,
                    onDarkThemeChange = onDarkThemeChange
                )
            }
        }
    ) {
        PharmaScaffold(
            pantallaActual = pantallaActual,
            mostrarBotonMenu = true,
            onMenuClick = { scope.launch { drawerState.open() } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MediumNavigation(
    availableWidth: Dp,
    pantallaActual: Screen,
    onPantallaSeleccionada: (Screen) -> Unit,
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        NavigationRail(
            modifier = Modifier.fillMaxHeight(),
            header = {
                Text(
                    text = "PM",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        ) {
            destinos.forEach { destino ->
                NavigationRailItem(
                    selected = pantallaActual == destino.screen,
                    onClick = { onPantallaSeleccionada(destino.screen) },
                    icon = { Icon(destino.icono, contentDescription = destino.titulo) },
                    label = { Text(destino.titulo) }
                )
            }
            Icon(Icons.Default.WbSunny, contentDescription = "Tema", modifier = Modifier.padding(top = 12.dp))
            Switch(
                checked = darkTheme,
                onCheckedChange = onDarkThemeChange,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        PharmaScaffold(
            pantallaActual = pantallaActual,
            mostrarBotonMenu = false,
            onMenuClick = {},
            modifier = Modifier
                .width(availableWidth - 80.dp)
                .fillMaxHeight()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpandedNavigation(
    pantallaActual: Screen,
    onPantallaSeleccionada: (Screen) -> Unit,
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit
) {
    PermanentNavigationDrawer(
        drawerContent = {
            PermanentDrawerSheet {
                DrawerContent(
                    pantallaActual = pantallaActual,
                    onPantallaSeleccionada = onPantallaSeleccionada,
                    darkTheme = darkTheme,
                    onDarkThemeChange = onDarkThemeChange
                )
            }
        }
    ) {
        PharmaScaffold(
            pantallaActual = pantallaActual,
            mostrarBotonMenu = false,
            onMenuClick = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PharmaScaffold(
    pantallaActual: Screen,
    mostrarBotonMenu: Boolean,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(tituloPantalla(pantallaActual)) },
                navigationIcon = {
                    if (mostrarBotonMenu) {
                        IconButton(onClick = onMenuClick) {
                            Icon(Icons.Default.Menu, contentDescription = "Abrir menú")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (pantallaActual) {
                Screen.Inicio -> InicioScreen()
                Screen.Productos -> ProductoScreen(
                    viewModel = koinViewModel<ProductoViewModel>()
                )
                Screen.Clientes -> ClienteScreen()
                Screen.Pedidos -> Text(
                    text = "Pantalla de pedidos en construcción",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun DrawerContent(
    pantallaActual: Screen,
    onPantallaSeleccionada: (Screen) -> Unit,
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 12.dp)
    ) {
        DrawerHeader()
        destinos.forEach { destino ->
            NavigationDrawerItem(
                label = { Text(destino.titulo) },
                selected = pantallaActual == destino.screen,
                onClick = { onPantallaSeleccionada(destino.screen) },
                icon = { Icon(destino.icono, contentDescription = destino.titulo) }
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Modo oscuro")
            Switch(checked = darkTheme, onCheckedChange = onDarkThemeChange)
        }
    }
}

@Composable
private fun DrawerHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 24.dp)
    ) {
        Text(
            "PharmaMobil",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text("Gestión farmacéutica", style = MaterialTheme.typography.bodyMedium)
    }
}

private fun tituloPantalla(screen: Screen): String = when (screen) {
    Screen.Inicio -> "Inicio"
    Screen.Productos -> "Productos"
    Screen.Clientes -> "Clientes"
    Screen.Pedidos -> "Pedidos"
}
