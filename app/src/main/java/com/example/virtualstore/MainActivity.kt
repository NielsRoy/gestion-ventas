package com.example.virtualstore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.virtualstore.inventario.controllers.CategoriaC
import com.example.virtualstore.ventas.controllers.ClienteC
import com.example.virtualstore.inventario.controllers.ProductoC
import com.example.virtualstore.ventas.controllers.RepartidorC
import com.example.virtualstore.ventas.controllers.VentaC
import com.example.virtualstore.inventario.models.CategoriaM
import com.example.virtualstore.ventas.models.ClienteM
import com.example.virtualstore.inventario.models.ProductoM
import com.example.virtualstore.ventas.models.RepartidorM
import com.example.virtualstore.ventas.models.VentaM
import com.example.virtualstore.inventario.views.CategoriaV
import com.example.virtualstore.ventas.views.ClienteV
import com.example.virtualstore.inventario.views.ProductoV
import com.example.virtualstore.ventas.views.RepartidorV
import com.example.virtualstore.ventas.views.VentaV
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dbHelper = DBHelper(this)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DrawerNavigationExample(dbHelper)
                }
            }
        }
    }

    @Composable
    fun DrawerNavigationExample(dbHelper: DBHelper) {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val navController = rememberNavController()

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Menú", style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { scope.launch { drawerState.close() } }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar menú")
                        }
                    }

                    Divider()

                    NavigationDrawerItem(
                        label = { Text("Ventas") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("ventas") {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Repartidores") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("repartidores") {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Productos") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("productos") {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }

                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Categorías") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("categorias") {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Clientes") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("clientes") {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) {
            NavHost(
                navController = navController,
                startDestination = "ventas"
            ) {
                composable("productos") {
                    val productoM = ProductoM(dbHelper = dbHelper)
                    val productoV = ProductoV()
                    val productoC = ProductoC(productoV, productoM)
                    productoV.setVisible(drawerState, scope)
                }
                composable("categorias") {
                    val categoriaM = CategoriaM(dbHelper = dbHelper)
                    val categoriaV = CategoriaV()
                    val categoriaC = CategoriaC(categoriaV, categoriaM)
                    categoriaV.setVisible(drawerState, scope)
                }
                composable("clientes") {
                    val clienteM = ClienteM(dbHelper = dbHelper)
                    val clienteV = ClienteV()
                    val clienteC = ClienteC(clienteV, clienteM)
                    clienteV.setVisible(drawerState, scope)
                }
                composable("repartidores") {
                    val repartidorM = RepartidorM(dbHelper = dbHelper)
                    val repartidorV = RepartidorV()
                    val repartidorC = RepartidorC(repartidorV, repartidorM)
                    repartidorV.setVisible(drawerState, scope)
                }
                composable("ventas") {
                    val ventaM = VentaM(dbHelper = dbHelper)
                    val ventaV = VentaV(ventaM)
                    val ventaC = VentaC(ventaV, ventaM)
                    ventaV.setVisible(drawerState, scope)
                }
            }
        }
    }
}


