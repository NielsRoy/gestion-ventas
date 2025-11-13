package com.example.virtualstore.ventas.views

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.virtualstore.ventas.models.ClienteM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ClienteV {
    private var guardarListener: (String, String, String) -> Unit = { _, _, _ -> }
    private var editarListener: (Int, String, String, String) -> Unit = { _, _, _, _ -> }
    private var eliminarListener: (Int) -> Unit = { _ -> }
    private var clientes = MutableStateFlow<List<ClienteM>>(emptyList())
    private var clienteEnEdicion by mutableStateOf<ClienteM?>(null)
    private val mensaje = MutableStateFlow<String?>(null)
    private var mostrarDialogoConfirmacion by mutableStateOf(false)
    private var clienteAEliminar by mutableStateOf<ClienteM?>(null)

    fun mostrarMensaje(m: String?) {
        mensaje.value = m
    }
    fun setGuardarListener(listener: (String, String, String) -> Unit) {
        guardarListener = listener
    }
    fun setEditarListener(listener: (Int, String, String, String) -> Unit) {
        editarListener = listener
    }
    fun setEliminarListener(listener: (Int) -> Unit) {
        eliminarListener = listener
    }
    fun actualizarClientes(clientes: List<ClienteM>) {
        this.clientes.value = clientes
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
                    clienteAEliminar = null
                },
                title = { Text("Confirmar Eliminación") },
                text = {
                    Text("¿Está seguro de que desea eliminar al cliente \"${clienteAEliminar?.nombre}\"? Esta acción no se puede deshacer.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            clienteAEliminar?.id?.let { id ->
                                eliminarListener(id)
                                if (clienteEnEdicion?.id == id) {
                                    clienteEnEdicion = null
                                }
                            }
                            mostrarDialogoConfirmacion = false
                            clienteAEliminar = null
                        }
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            mostrarDialogoConfirmacion = false
                            clienteAEliminar = null
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
        Scaffold (
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Clientes") },
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
                    },
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp)
                    .padding(top = 8.dp)
            ) {
                if (formularioVisible) {
                    Formulario()
                    Spacer(modifier = Modifier.height(24.dp))
                }
                Lista(onEditarClick = { cliente ->
                    //setClienteEditando(cliente)
                    clienteEnEdicion = cliente
                    formularioVisible = true
                })
            }
        }
    }

    @Composable
    private fun Formulario() {
        var nombre by remember { mutableStateOf("") }
        var celular by remember { mutableStateOf("") }
        var direccion by remember { mutableStateOf("") }

        LaunchedEffect(clienteEnEdicion) {
            clienteEnEdicion?.let { cliente ->
                nombre = cliente.nombre ?: "Sin nombre"
                celular = cliente.celular.toString()
                direccion = cliente.direccion.toString()
            } ?: run {
                nombre = ""
                celular = ""
                direccion = ""
            }
        }

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = celular,
            onValueChange = { celular = it },
            label = { Text("Celular") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = direccion,
            onValueChange = { direccion = it },
            label = { Text("Dirección") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (clienteEnEdicion != null && clienteEnEdicion?.id != null) {
                        editarListener(clienteEnEdicion!!.id!!, nombre, celular, direccion)
                        //setClienteEditando(null)
                        clienteEnEdicion = null
                    } else {
                        guardarListener(nombre, celular, direccion)
                    }
                    nombre = ""
                    celular = ""
                    direccion = ""
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (clienteEnEdicion != null) "Actualizar" else "Agregar")
            }

            if (clienteEnEdicion != null) {
                Button(
                    onClick = {
                        //setClienteEditando(null)
                        clienteEnEdicion = null
                        nombre = ""
                        celular = ""
                        direccion = ""
                    },
                    modifier = Modifier.weight(1f),
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
    private fun Lista(onEditarClick: (ClienteM) -> Unit) {
        val clientes by clientes.collectAsState(initial = emptyList())

        if (clientes.isEmpty()) {
            Text(
                text = "No hay clientes registrados",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(clientes.size) { index ->
                    ClienteItem(
                        cliente = clientes[index],
                        onEditar = { onEditarClick(clientes[index]) },
                        onEliminar = {
                            clienteAEliminar = clientes[index]
                            mostrarDialogoConfirmacion = true
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun ClienteItem(
        cliente: ClienteM,
        onEditar: () -> Unit,
        onEliminar: () -> Unit
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text (
                    text = cliente.nombre ?: "sin nombre",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text (
                    text = "Celular: ${cliente.celular}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text (
                    text = "Dirección: ${cliente.direccion}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onEditar,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text("Editar")
                    }
                    Button(
                        onClick = onEliminar,
                        modifier = Modifier.weight(1f),
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