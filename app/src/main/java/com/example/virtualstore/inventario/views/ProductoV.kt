package com.example.virtualstore.inventario.views

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.virtualstore.inventario.models.CategoriaM
import com.example.virtualstore.inventario.models.ProductoM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.Currency
import java.util.Locale

class ProductoV {
    private var guardarListener: (String, String, String, Int) -> Unit = { _, _, _, _ -> }
    private var editarListener: (Int, String, String, String, Int) -> Unit = { _, _, _, _, _ -> }
    private var eliminarListener: (Int) -> Unit = { _ -> }
    private var productos = MutableStateFlow<List<ProductoM>>(emptyList())
    private var categorias = MutableStateFlow<List<CategoriaM>>(emptyList())
    private var productoEnEdicion by mutableStateOf<ProductoM?>(null)
    private val mensaje = MutableStateFlow<String?>(null)
    private var mostrarDialogoConfirmacion by mutableStateOf(false)
    private var productoAEliminar by mutableStateOf<ProductoM?>(null)

    fun mostrarMensaje(m: String?) {
        mensaje.value = m
    }
    fun setGuardarListener(listener: (String, String, String, Int) -> Unit) {
        guardarListener = listener
    }
    fun setEditarListener(listener: (Int, String, String, String, Int) -> Unit) {
        editarListener = listener
    }
    fun setEliminarListener(listener: (Int) -> Unit) {
        eliminarListener = listener
    }
    fun actualizarProductos(productos: List<ProductoM>) {
        this.productos.value = productos
    }
    fun actualizarCategorias(categorias: List<CategoriaM>) {
        this.categorias.value = categorias
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun setVisible(drawerState: DrawerState, scope: CoroutineScope) {
        val context = LocalContext.current
        val mensaje by mensaje.collectAsState()
        var formularioVisible by remember { mutableStateOf(true) }
        LaunchedEffect(mensaje) {
            mensaje?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                mostrarMensaje(null)
            }
        }
        if (mostrarDialogoConfirmacion) {
            AlertDialog(
                onDismissRequest = {
                    mostrarDialogoConfirmacion = false
                    productoAEliminar = null
                },
                title = { Text("Confirmar Eliminación") },
                text = {
                    Text("¿Está seguro de que desea eliminar el producto \"${productoAEliminar?.nombre}\"? Esta acción no se puede deshacer.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            productoAEliminar?.id?.let { id ->
                                eliminarListener(id)
                                if (productoEnEdicion?.id == id) {
                                    productoEnEdicion = null
                                }
                            }
                            mostrarDialogoConfirmacion = false
                            productoAEliminar = null
                        }
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            mostrarDialogoConfirmacion = false
                            productoAEliminar = null
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Productos") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Abrir menú")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { formularioVisible = !formularioVisible }
                        ) {
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null)
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier.Companion
                    .fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp)
                    .padding(top = 8.dp)
            ) {
                if (formularioVisible) {
                    Formulario()
                    Spacer(modifier = Modifier.Companion.height(24.dp))
                }
                Lista(onEditarClick = { producto ->
                    productoEnEdicion = producto
                    formularioVisible = true
                })
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun Formulario() {
        var nombre by remember { mutableStateOf("") }
        var precio by remember { mutableStateOf("") }
        var cantidad by remember { mutableStateOf("") }
        var categoriaSeleccionada by remember { mutableStateOf<CategoriaM?>(null) }
        var menuExpandido by remember { mutableStateOf(false) }
        val categorias by categorias.collectAsState()
        LaunchedEffect(productoEnEdicion) {
            productoEnEdicion?.let { producto ->
                nombre = producto.nombre ?: ""
                precio = producto.precio?.toString() ?: ""
                cantidad = producto.cantidad?.toString() ?: ""
                categoriaSeleccionada = producto.categoria
            } ?: run {
                nombre = ""
                precio = ""
                cantidad = ""
                categoriaSeleccionada = null
            }
        }
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.Companion.fillMaxWidth()
        )
        Spacer(modifier = Modifier.Companion.height(8.dp))
        OutlinedTextField(
            value = precio,
            onValueChange = { precio = it },
            label = { Text("Precio") },
            leadingIcon = { Text(Currency.getInstance(Locale.getDefault()).symbol) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Companion.Decimal),
            modifier = Modifier.Companion.fillMaxWidth()
        )
        Spacer(modifier = Modifier.Companion.height(8.dp))
        OutlinedTextField(
            value = cantidad,
            onValueChange = { cantidad = it },
            label = { Text("Cantidad") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Companion.Number),
            modifier = Modifier.Companion.fillMaxWidth()
        )
        Spacer(modifier = Modifier.Companion.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = menuExpandido,
            onExpandedChange = { menuExpandido = it }
        ) {
            OutlinedTextField(
                value = categoriaSeleccionada?.nombre ?: "Seleccione una categoría",
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpandido) },
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .menuAnchor(),
                label = { Text("Categoría") }
            )

            ExposedDropdownMenu(
                expanded = menuExpandido,
                onDismissRequest = { menuExpandido = false }
            ) {
                if (categorias.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No hay categorías disponibles") },
                        onClick = { }
                    )
                } else {
                    categorias.forEach { categoria ->
                        DropdownMenuItem(
                            text = { Text(categoria.nombre ?: "Sin nombre") },
                            onClick = {
                                categoriaSeleccionada = categoria
                                menuExpandido = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.Companion.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (categoriaSeleccionada == null) {
                        mostrarMensaje("Debe seleccionar una categoría")
                        return@Button
                    }

                    if (productoEnEdicion != null && productoEnEdicion?.id != null) {
                        editarListener(
                            productoEnEdicion!!.id!!,
                            nombre,
                            precio,
                            cantidad,
                            categoriaSeleccionada!!.id!!
                        )
                        productoEnEdicion = null
                    } else {
                        guardarListener(nombre, precio, cantidad, categoriaSeleccionada!!.id!!)
                    }
                    nombre = ""
                    precio = ""
                    cantidad = ""
                    categoriaSeleccionada = null
                },
                modifier = Modifier.Companion.weight(1f)
            ) {
                Text(if (productoEnEdicion != null) "Actualizar" else "Agregar")
            }
            if (productoEnEdicion != null) {
                Button(
                    onClick = {
                        productoEnEdicion = null
                        nombre = ""
                        precio = ""
                        cantidad = ""
                        categoriaSeleccionada = null
                    },
                    modifier = Modifier.Companion.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text("Cancelar")
                }
            }
        }
    }

    @Composable
    private fun Lista(onEditarClick: (ProductoM) -> Unit) {
        val productos by productos.collectAsState()
        if (productos.isEmpty()) {
            Text(
                text = "No hay productos registrados",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.Companion.fillMaxWidth(),
                textAlign = TextAlign.Companion.Center
            )
        } else {
            LazyColumn(
                modifier = Modifier.Companion.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(productos.size) { index ->
                    ProductoItem(
                        producto = productos[index],
                        onEditar = { onEditarClick(productos[index]) },
                        onEliminar = {
                            productoAEliminar = productos[index]
                            mostrarDialogoConfirmacion = true
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun ProductoItem(
        producto: ProductoM,
        onEditar: () -> Unit,
        onEliminar: () -> Unit
    ) {
        Card(
            modifier = Modifier.Companion.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = producto.nombre ?: "sin nombre",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Precio: ${Currency.getInstance(Locale.getDefault()).symbol} ${producto.precio}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Cantidad: ${producto.cantidad}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Categoría: ${producto.categoria?.nombre ?: "Sin categoría"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.Companion.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onEditar,
                        modifier = Modifier.Companion.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text("Editar")
                    }
                    Button(
                        onClick = onEliminar,
                        modifier = Modifier.Companion.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text("Eliminar")
                    }
                }
            }
        }
    }
}