package pe.edu.upeu.pharmamobile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.pharmamobile.navigation.Screen
import pe.edu.upeu.pharmamobile.domain.model.Sesion
import pe.edu.upeu.pharmamobile.presentation.cliente.ClienteScreen
import pe.edu.upeu.pharmamobile.presentation.inicio.InicioScreen
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoScreen
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoViewModel
import pe.edu.upeu.pharmamobile.presentation.sesion.LoginScreen
import pe.edu.upeu.pharmamobile.presentation.sesion.SesionViewModel
import pe.edu.upeu.pharmamobile.theme.PharmaMobilTheme
import org.jetbrains.compose.resources.painterResource
import pharmamobile.shared.generated.resources.Res
import pharmamobile.shared.generated.resources.pharmamobil_mark

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

private val LocalSesion = compositionLocalOf<Sesion> { error("Sesión no disponible") }
private val LocalCerrarSesion = compositionLocalOf<() -> Unit> { {} }

private fun destinosPermitidos(sesion: Sesion) = destinos.filter {
    when (it.screen) {
        Screen.Clientes -> sesion.puede("CLIENTES_LEER")
        Screen.Pedidos -> sesion.puede("VENTAS_REGISTRAR")
        else -> true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    var pantallaActual by remember { mutableStateOf<Screen>(Screen.Inicio) }
    var darkTheme by remember { mutableStateOf(false) }

    KoinContext {
        val sesionViewModel = koinViewModel<SesionViewModel>()
        val sesion by sesionViewModel.sesion.collectAsState()
        val inicializando by sesionViewModel.inicializando.collectAsState()
        LaunchedEffect(sesion?.usuario) { pantallaActual = Screen.Inicio }
        PharmaMobilTheme(darkTheme = darkTheme) {
            if (inicializando) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (sesion == null) {
                LoginScreen(sesionViewModel)
            } else {
            CompositionLocalProvider(
                LocalSesion provides requireNotNull(sesion),
                LocalCerrarSesion provides sesionViewModel::salir
            ) {
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
            onPantallaSeleccionada = onPantallaSeleccionada,
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
            destinosPermitidos(LocalSesion.current).forEach { destino ->
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
            onPantallaSeleccionada = onPantallaSeleccionada,
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
            onPantallaSeleccionada = onPantallaSeleccionada,
            mostrarBotonMenu = false,
            onMenuClick = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PharmaScaffold(
    pantallaActual: Screen,
    onPantallaSeleccionada: (Screen) -> Unit,
    mostrarBotonMenu: Boolean,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            tituloPantalla(pantallaActual),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            "PharmaMobil",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                navigationIcon = {
                    if (mostrarBotonMenu) {
                        IconButton(onClick = onMenuClick) {
                            Icon(Icons.Default.Menu, contentDescription = "Abrir menú")
                        }
                    }
                },
                actions = {
                    val cerrarSesion = LocalCerrarSesion.current
                    IconButton(onClick = cerrarSesion) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión")
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
                Screen.Inicio -> InicioScreen(
                    usuario = LocalSesion.current.nombreCompleto.ifBlank { LocalSesion.current.usuario },
                    onAbrirProductos = { onPantallaSeleccionada(Screen.Productos) },
                )
                Screen.Productos -> ProductoScreen(
                    viewModel = koinViewModel<ProductoViewModel>(),
                    puedeEscribir = LocalSesion.current.puede("CATALOGO_ESCRIBIR")
                )
                Screen.Clientes -> if (LocalSesion.current.puede("CLIENTES_LEER")) ClienteScreen() else Text("No tienes permiso para ver clientes")
                Screen.Pedidos -> PedidosPendientes()
            }
        }
    }
}

@Composable
private fun PedidosPendientes() {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Pedidos", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Un espacio para tus operaciones de venta",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 22.dp),
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(34.dp),
                )
                Text(
                    "Módulo en preparación",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 14.dp),
                )
                Text(
                    "La gestión de pedidos estará disponible en una próxima versión.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
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
            .padding(horizontal = 14.dp)
    ) {
        DrawerHeader()
        destinosPermitidos(LocalSesion.current).forEach { destino ->
            NavigationDrawerItem(
                label = { Text(destino.titulo) },
                selected = pantallaActual == destino.screen,
                onClick = { onPantallaSeleccionada(destino.screen) },
                icon = { Icon(destino.icono, contentDescription = destino.titulo) }
            )
        }
        HorizontalDivider(modifier = Modifier.padding(top = 18.dp, bottom = 8.dp))
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
        val cerrarSesion = LocalCerrarSesion.current
        TextButton(onClick = cerrarSesion) { Text("Cerrar sesión") }
    }
}

@Composable
private fun DrawerHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 25.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.pharmamobil_mark),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(52.dp),
        )
        Column(modifier = Modifier.padding(start = 10.dp)) {
            Text(
                "PharmaMobil",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text("Gestión farmacéutica", style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun tituloPantalla(screen: Screen): String = when (screen) {
    Screen.Inicio -> "Inicio"
    Screen.Productos -> "Productos"
    Screen.Clientes -> "Clientes"
    Screen.Pedidos -> "Pedidos"
}
