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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.virtualstore.inventario.models.CategoriaM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CategoriaV {
    private var guardarListener: (String, String) -> Unit = { _, _ -> }
    private var editarListener: (Int, String, String) -> Unit = { _, _, _ -> }
    private var eliminarListener: (Int) -> Unit = { _ -> }
    private var categorias = MutableStateFlow<List<CategoriaM>>(emptyList())
    private var categoriaEnEdicion by mutableStateOf<CategoriaM?>(null)
    private val mensaje = MutableStateFlow<String?>(null)
    private var mostrarDialogoConfirmacion by mutableStateOf(false)
    private var categoriaAEliminar by mutableStateOf<CategoriaM?>(null)

    fun mostrarMensaje(m: String?) {
        mensaje.value = m
    }
    fun setGuardarListener(listener: (String, String) -> Unit) {
        guardarListener = listener
    }
    fun setEditarListener(listener: (Int, String, String) -> Unit) {
        editarListener = listener
    }
    fun setEliminarListener(listener: (Int) -> Unit) {
        eliminarListener = listener
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
                mostrarMensaje(null) // limpiar para que no se repita
            }
        }
        if (mostrarDialogoConfirmacion) {
            AlertDialog(
                onDismissRequest = {
                    mostrarDialogoConfirmacion = false
                    categoriaAEliminar = null
                },
                title = { Text("Confirmar Eliminación") },
                text = {
                    Text("¿Está seguro de que desea eliminar la categoria \"${categoriaAEliminar?.nombre}\"? Esta acción no se puede deshacer.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            categoriaAEliminar?.id?.let { id ->
                                eliminarListener(id)
                                if (categoriaEnEdicion?.id == id) {
                                    categoriaEnEdicion = null
                                }
                            }
                            mostrarDialogoConfirmacion = false
                            categoriaAEliminar = null
                        }
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            mostrarDialogoConfirmacion = false
                            categoriaAEliminar = null
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
                    title = { Text("Categorias") },
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
                Lista(onEditarClick = { categoria ->
                    categoriaEnEdicion = categoria
                    //setCategoriaEditando(categoria)
                    formularioVisible = true
                })
            }
        }
    }

    @Composable
    private fun Formulario() {
        var nombre by remember { mutableStateOf("") }
        var descripcion by remember { mutableStateOf("") }

        LaunchedEffect(categoriaEnEdicion) {
            categoriaEnEdicion?.let { categoria ->
                nombre = categoria.nombre ?: "Sin nombre"
                descripcion = categoria.descripcion.toString()
            } ?: run {
                nombre = ""
                descripcion = ""
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
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción") },
            modifier = Modifier.Companion.fillMaxWidth()
        )
        Spacer(modifier = Modifier.Companion.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (categoriaEnEdicion != null && categoriaEnEdicion?.id != null) {
                        editarListener(categoriaEnEdicion!!.id!!, nombre, descripcion)
                        categoriaEnEdicion = null
                    } else {
                        guardarListener(nombre, descripcion)
                    }
                    nombre = ""
                    descripcion = ""
                },
                modifier = Modifier.Companion.weight(1f)
            ) {
                Text(if (categoriaEnEdicion != null) "Actualizar" else "Agregar")
            }

            if (categoriaEnEdicion != null) {
                Button(
                    onClick = {
                        categoriaEnEdicion = null
                        nombre = ""
                        descripcion = ""
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
    private fun Lista(onEditarClick: (CategoriaM) -> Unit) {
        val categorias by categorias.collectAsState(initial = emptyList())

        if (categorias.isEmpty()) {
            Text(
                text = "No hay categorias registradas",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.Companion.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        } else {
            LazyColumn(
                modifier = Modifier.Companion.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categorias.size) { index ->
                    CategoriaItem(
                        categoria = categorias[index],
                        onEditar = { onEditarClick(categorias[index]) },
                        onEliminar = {
                            categoriaAEliminar = categorias[index]
                            mostrarDialogoConfirmacion = true
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun CategoriaItem(
        categoria: CategoriaM,
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
                    text = categoria.nombre ?: "sin nombre",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${categoria.descripcion}",
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