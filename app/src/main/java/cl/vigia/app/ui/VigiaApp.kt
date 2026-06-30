package cl.vigia.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cl.vigia.app.data.Repo
import cl.vigia.app.ui.screens.AlertasScreen
import cl.vigia.app.ui.screens.DashboardScreen
import cl.vigia.app.ui.screens.DatosScreen
import cl.vigia.app.ui.screens.LoginScreen
import cl.vigia.app.ui.screens.MapaScreen
import cl.vigia.app.ui.screens.PerfilScreen
import cl.vigia.app.ui.screens.SensorDetailScreen
import cl.vigia.app.ui.theme.InkFaint
import cl.vigia.app.ui.theme.Moss
import cl.vigia.app.ui.theme.MossSoft
import cl.vigia.app.ui.theme.Paper
import cl.vigia.app.ui.theme.SurfaceCard
import kotlinx.coroutines.launch

private val MAIN_ROUTES = setOf("dashboard", "mapa", "alertas", "datos", "perfil")

private fun NavController.navigateTab(route: String) = navigate(route) {
    launchSingleTop = true
    restoreState = true
    popUpTo("dashboard") { saveState = true }
}

@Composable
fun VigiaApp() {
    val nav = rememberNavController()
    val backStackEntry by nav.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val showBar = route in MAIN_ROUTES

    // Zona minera activa, compartida por toda la app.
    var zoneId by rememberSaveable { mutableStateOf(Repo.defaultZone) }
    val selectZone: (String) -> Unit = { zoneId = it }

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val toast: (String) -> Unit = { msg -> scope.launch { snackbar.showSnackbar(msg) } }

    Scaffold(
        containerColor = Paper,
        bottomBar = { if (showBar) BottomBar(nav, route) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { inner ->
        NavHost(nav, startDestination = "login", modifier = Modifier.padding(inner)) {
            composable("login") {
                LoginScreen(onEnter = {
                    nav.navigate("dashboard") { popUpTo("login") { inclusive = true } }
                })
            }
            composable("dashboard") {
                DashboardScreen(
                    zoneId = zoneId,
                    onSelectZone = selectZone,
                    onOpenSensor = { nav.navigate("sensor/$it") },
                    onGoAlertas = { nav.navigateTab("alertas") },
                    onGoDatos = { nav.navigateTab("datos") },
                    onGoMapa = { nav.navigateTab("mapa") },
                )
            }
            composable("mapa") {
                MapaScreen(
                    zoneId = zoneId,
                    onSelectZone = selectZone,
                    onOpenSensor = { nav.navigate("sensor/$it") },
                    onGoAlertas = { nav.navigateTab("alertas") },
                )
            }
            composable("sensor/{tipo}") { entry ->
                SensorDetailScreen(
                    zoneId = zoneId,
                    tipo = entry.arguments?.getString("tipo") ?: "agua",
                    onBack = { nav.popBackStack() },
                    onGoPerfil = { nav.navigateTab("perfil") },
                )
            }
            composable("alertas") {
                AlertasScreen(
                    zoneId = zoneId,
                    onSelectZone = selectZone,
                    onOpenSensor = { nav.navigate("sensor/$it") },
                    onGoPerfil = { nav.navigateTab("perfil") },
                )
            }
            composable("datos") {
                DatosScreen(zoneId = zoneId, onSelectZone = selectZone, toast = toast)
            }
            composable("perfil") { PerfilScreen(zoneId = zoneId, toast = toast) }
        }
    }
}

@Composable
private fun BottomBar(nav: NavController, current: String?) {
    val items = listOf(
        Item("dashboard", "Resumen", Icons.Outlined.Dashboard),
        Item("mapa", "Mapa", Icons.Outlined.Map),
        Item("alertas", "Alertas", Icons.Outlined.Notifications),
        Item("datos", "Datos", Icons.Outlined.Description),
        Item("perfil", "Perfil", Icons.Outlined.Person),
    )
    NavigationBar(containerColor = SurfaceCard, tonalElevation = 0.dp) {
        items.forEach { item ->
            NavigationBarItem(
                selected = current == item.route,
                onClick = { if (current != item.route) nav.navigateTab(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Moss,
                    selectedTextColor = Moss,
                    indicatorColor = MossSoft,
                    unselectedIconColor = InkFaint,
                    unselectedTextColor = InkFaint,
                ),
            )
        }
    }
}

private data class Item(val route: String, val label: String, val icon: ImageVector)
