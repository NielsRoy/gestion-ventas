package com.example.virtualstore.inventario.views

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.virtualstore.inventario.models.CategoriaComponent
import com.example.virtualstore.inventario.models.CategoriaComposite
import com.example.virtualstore.inventario.models.CategoriaLeaf
import com.example.virtualstore.inventario.models.CategoriaM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CategoriaV {
    private var guardarListener: (String, String, Int?) -> Unit = { _, _, _ -> }
    private var editarListener: (Int, String, String, Int?) -> Unit = { _, _, _, _ -> }
    private var eliminarListener: (Int) -> Unit = { _ -> }
    private var categorias = MutableStateFlow<List<CategoriaM>>(emptyList())
    private var categoriaEnEdicion by mutableStateOf<CategoriaM?>(null)
    private val mensaje = MutableStateFlow<String?>(null)
    private var mostrarDialogoConfirmacion by mutableStateOf(false)
    private var categoriaAEliminar by mutableStateOf<CategoriaM?>(null)

    private var components = MutableStateFlow<List<CategoriaComponent>>(emptyList())

    fun mostrarMensaje(m: String?) {
        mensaje.value = m
    }
    fun setGuardarListener(listener: (String, String, Int?) -> Unit) {
        guardarListener = listener
    }
    fun setEditarListener(listener: (Int, String, String, Int?) -> Unit) {
        editarListener = listener
    }
    fun setEliminarListener(listener: (Int) -> Unit) {
        eliminarListener = listener
    }
    fun actualizarCategorias(categorias: List<CategoriaM>) {
        this.categorias.value = categorias

        this.components.value = getComponents(categorias)
    }

    // En CategoriaV.kt

    fun getComponents(c: List<CategoriaM>): List<CategoriaComponent> {
        // 1. Identificar quiénes serán Composite (tienen hijos)
        val parentIds = c.mapNotNull { it.categoria_id }.toSet()

        val componentMap = mutableMapOf<Int, CategoriaComponent>()

        // 2. Crear los wrappers (Composite o Leaf) para TODAS las categorías
        for (categoria in c) {
            if (categoria.id == null) continue
            val component = if (categoria.id in parentIds) {
                CategoriaComposite(categoria)
            } else {
                CategoriaLeaf(categoria)
            }
            componentMap[categoria.id!!] = component
        }

        // 3. Armar la estructura de árbol (SOLO para lógica interna de conteo)
        for (component in componentMap.values) {
            val categoria = component.getCategoria()
            val parentId = categoria.categoria_id

            if (parentId != null) {
                val parent = componentMap[parentId]
                // Agregamos el hijo al padre para que el Composite calcule bien el total
                if (parent is CategoriaComposite) {
                    parent.agregar(component)
                }
            }
        }

        // 4. CORRECCIÓN PRINCIPAL:
        // Devolvemos una lista ordenada exactamente igual que la lista de entrada 'c'.
        // Así components[index] siempre corresponde a categorias[index].
        val orderedComponents = mutableListOf<CategoriaComponent>()
        for (categoria in c) {
            categoria.id?.let { id ->
                componentMap[id]?.let { comp ->
                    orderedComponents.add(comp)
                }
            }
        }

        return orderedComponents
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun setVisible(drawerState: DrawerState, scope: CoroutineScope) {
        val context = LocalContext.current
        val mensaje by mensaje.collectAsState()
        var formularioVisible by remember { mutableStateOf(true) }

        val categoriasList by categorias.collectAsState()

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
                    Formulario(categorias = categoriasList)
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun Formulario(categorias: List<CategoriaM>) {
        var nombre by remember { mutableStateOf("") }
        var descripcion by remember { mutableStateOf("") }

        // --- ESTADOS PARA EL COMBOBOX ---
        var expanded by remember { mutableStateOf(false) }
        // Opción "Ninguna" para categorías raíz
        val categoriaNinguna = CategoriaM(id = null, nombre = "Ninguna")
        var selectedParent by remember { mutableStateOf<CategoriaM?>(categoriaNinguna) }

        // Creamos la lista para el dropdown
        // Filtramos para que una categoría no pueda ser su propio padre
        val dropdownOptions = listOf(categoriaNinguna) + categorias.filter {
            it.id != categoriaEnEdicion?.id
        }

        LaunchedEffect(categoriaEnEdicion) {
            categoriaEnEdicion?.let { categoria ->
                nombre = categoria.nombre ?: "Sin nombre"
                descripcion = categoria.descripcion.toString()
                selectedParent = dropdownOptions.find { it.id == categoria.categoria_id } ?: categoriaNinguna
            } ?: run {
                nombre = ""
                descripcion = ""
                selectedParent = categoriaNinguna
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
        Spacer(modifier = Modifier.Companion.height(8.dp))
        // --- INICIO: COMBOBOX (ExposedDropdownMenuBox) ---
        Box(modifier = Modifier.fillMaxWidth()) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    value = selectedParent?.nombre ?: "Ninguna",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría Padre") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    dropdownOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.nombre ?: "Error") },
                            onClick = {
                                selectedParent = option
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
        // --- FIN: COMBOBOX ---

        Spacer(modifier = Modifier.Companion.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val parentId = selectedParent?.id // Será null si se seleccionó "Ninguna"

                    if (categoriaEnEdicion != null && categoriaEnEdicion?.id != null) {
                        editarListener(categoriaEnEdicion!!.id!!, nombre, descripcion, parentId)
                        categoriaEnEdicion = null
                    } else {
                        guardarListener(nombre, descripcion, parentId)
                    }
                    nombre = ""
                    descripcion = ""
                    selectedParent = categoriaNinguna
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
                        selectedParent = categoriaNinguna
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
        val components by components.collectAsState(initial = emptyList())

        if (categorias.isEmpty()) {
            Text(
                text = "No hay categorias registradas",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.Companion.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        } else { //if (categorias.size == components.size) { // <-- Verificación
            LazyColumn(
                modifier = Modifier.Companion.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categorias.size) { index ->
                    CategoriaItem(
                        categoria = categorias[index],
                        component = components[index],
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
        component: CategoriaComponent,
        onEditar: () -> Unit,
        onEliminar: () -> Unit
    ) {
        val productCount = component.getCantProductos(categoria)

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
                // --- MOSTRAR EL CONTEO DE PRODUCTOS ---
                Text(
                    text = "Productos: $productCount",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
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