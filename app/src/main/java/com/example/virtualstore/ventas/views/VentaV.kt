package com.example.virtualstore.ventas.views

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.virtualstore.R
import com.example.virtualstore.ventas.models.ClienteM
import com.example.virtualstore.ventas.models.DetalleVM
import com.example.virtualstore.inventario.models.ProductoM
import com.example.virtualstore.ventas.models.VentaStrategy
import com.example.virtualstore.ventas.models.RepartidorM
import com.example.virtualstore.ventas.models.VentaDescuento
import com.example.virtualstore.ventas.models.VentaM
import com.example.virtualstore.ventas.models.VentaNormal
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale

class VentaV {
    private val ventaM: VentaM

    constructor(ventaM: VentaM) {
        this.ventaM = ventaM
        actualizarProductos(ventaM.getAllProductos())
    }

    private var guardarVentaListener: (String, String, Int, Int, List<DetalleVM>, VentaStrategy) -> Unit = { _, _, _, _, _, _ -> }
    private var actualizarVentaListener: (Int, String, String, Int, Int, List<DetalleVM>, VentaStrategy) -> Unit = { _, _, _, _, _, _, _ -> }
    private var eliminarVentaListener: (Int) -> Unit = { _ -> }
    private var ventas = MutableStateFlow<List<VentaM>>(emptyList())
    private var clientes = MutableStateFlow<List<ClienteM>>(emptyList())
    private var repartidores = MutableStateFlow<List<RepartidorM>>(emptyList())
    private var productos = MutableStateFlow<List<ProductoM>>(emptyList())
    private val mensaje = MutableStateFlow<String?>(null)
    private val monedaString = Currency.getInstance(Locale.getDefault()).symbol
    var mostrarAgregarVenta by mutableStateOf(false)
    var ventaSeleccionada by mutableStateOf<VentaM?>(null)
    private var mostrarDialogoConfirmacion by mutableStateOf(false)
    private var ventaAEliminar by mutableStateOf<VentaM?>(null)

    fun mostrarMensaje(m: String?) {
        mensaje.value = m
    }
    fun setGuardarVentaListener(listener: (String, String, Int, Int, List<DetalleVM>, VentaStrategy) -> Unit) {
        guardarVentaListener = listener
    }
    fun setActualizarVentaListener(listener: (Int, String, String, Int, Int, List<DetalleVM>, VentaStrategy) -> Unit) {
        actualizarVentaListener = listener
    }
    fun setEliminarVentaListener(listener: (Int) -> Unit) {
        eliminarVentaListener = listener
    }
    fun actualizarVentas(ventas: List<VentaM>) {
        this.ventas.value = ventas
    }
    fun actualizarClientes(clientes: List<ClienteM>) {
        this.clientes.value = clientes
    }
    fun actualizarRepartidores(repartidores: List<RepartidorM>) {
        this.repartidores.value = repartidores
    }
    fun actualizarProductos(productos: List<ProductoM>) {
        this.productos.value = productos.filter { (it.cantidad ?: 0) > 0 }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun setVisible(drawerState: DrawerState, scope: CoroutineScope) {
        val context = LocalContext.current
        val mensaje by mensaje.collectAsState()
        val clientes by clientes.collectAsState()
        val r by repartidores.collectAsState()
        //val productos by productosState.collectAsState()
        LaunchedEffect(mensaje) {
            mensaje?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                mostrarMensaje(null)
            }
        }
        if (mostrarAgregarVenta) {
            FormularioVenta(
                title = "Nueva Venta",
                onBack = {
                    mostrarAgregarVenta = false
                    actualizarProductos(ventaM.getAllProductos())
                },
                onGuardarVenta = { nroVenta, fecha, hora, clienteId, repartidorId, detalles, estrategia ->
                    guardarVentaListener(fecha, hora, clienteId, repartidorId, detalles, estrategia)
                    mostrarAgregarVenta = false
                },
                clientes = clientes,
                repartidores = r
                //productos = productos.filter { (it.cantidad ?: 0) > 0 }
            )
        } else if (ventaSeleccionada != null) {

            FormularioVenta(
                title = "Editar Venta #${ventaSeleccionada!!.nro}",
                onBack = {
                    ventaSeleccionada = null
                    actualizarProductos(ventaM.getAllProductos())
                },
                onGuardarVenta = { nroVenta, fecha, hora, clienteId, repartidorId, detalles, estrategia ->
                    actualizarVentaListener(nroVenta!!, fecha, hora, clienteId, repartidorId, detalles, estrategia)
                    ventaSeleccionada = null
                },
                clientes = clientes,
                repartidores = r,
                ventaEditar = ventaSeleccionada
            )
        } else {
            ListaVentasScreen(drawerState, scope)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ListaVentasScreen(drawerState: DrawerState, scope: CoroutineScope) {
        val ventas by ventas.collectAsState()
        val productos by productos.collectAsState()
        if (mostrarDialogoConfirmacion) {
            AlertDialog(
                onDismissRequest = {
                    mostrarDialogoConfirmacion = false
                    ventaAEliminar = null
                },
                title = { Text("Confirmar Eliminación") },
                text = {
                    Text("¿Está seguro de que desea eliminar la venta \"${ventaAEliminar?.nro}\"? Esta acción no se puede deshacer.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            ventaAEliminar?.nro?.let { nro ->
                                eliminarVentaListener(nro)
//                                if (productoEnEdicion?.nro == id) {
//                                    productoEnEdicion = null
//                                }
                            }
                            mostrarDialogoConfirmacion = false
                            ventaAEliminar = null
                        }
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            mostrarDialogoConfirmacion = false
                            ventaAEliminar = null
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
                    title = { Text("Ventas") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Abrir menú")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { mostrarAgregarVenta = true }
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Agregar venta")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                if (ventas.isEmpty()) {
                    Text(
                        text = "No hay ventas registradas",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ventas.size) { index ->
                            VentaItem(
                                venta = ventas[index],
                                onVerDetalle = {
                                    ventaSeleccionada = ventas[index]
                                    ///val productos = mutableListOf(*productos.toTypedArray())
                                    val detalles =
                                        ventaSeleccionada?.detalles?.map { it.producto?.id }?.toSet()
                                    actualizarProductos(productos.filter { detalles?.contains(it.id) != true  })
                                },
                                onEliminar = {
                                    ventaAEliminar = ventas[index]
                                    mostrarDialogoConfirmacion = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun VentaItem(venta: VentaM, onVerDetalle: () -> Unit, onEliminar: () -> Unit) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = onVerDetalle
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Venta #${venta.nro}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "Fecha: ${venta.fecha} ${venta.hora}")
                Text(text = "Cliente: ${venta.cliente?.nombre ?: "Sin cliente"}")
                Text(text = "Repartidor: ${venta.repartidor?.nombre ?: "Sin repartidor"}")
                Text(
                    text = "Total: $${venta.total ?: 0.0}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "Productos: ${venta.detalles?.size}")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onEliminar,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    )
                    {
                        Text("Eliminar")
                    }
                    Button(
                        onClick = onVerDetalle,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                    {
                        Text("Ver Detalles")
                        Icon(Icons.Filled.ArrowForward, contentDescription = "Ver detalles", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun FormularioVenta(
        title: String,
        onBack: () -> Unit,
        onGuardarVenta: (Int?, String, String, Int, Int, List<DetalleVM>, VentaStrategy) -> Unit,
        clientes: List<ClienteM>,
        repartidores: List<RepartidorM>,
        ventaEditar: VentaM? = null
    )
    {
        val esEdicion = ventaEditar != null
        val initialFecha = ventaEditar?.fecha?.let {
            LocalDate.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        } ?: LocalDate.now()
        val initialHora = ventaEditar?.hora?.let {
            LocalTime.parse(it, DateTimeFormatter.ofPattern("HH:mm:ss"))
        } ?: LocalTime.now()
        val initialCliente = ventaEditar?.cliente
        val initialRepartidor = ventaEditar?.repartidor
        var fechaState by remember { mutableStateOf(initialFecha) }
        var horaState by remember { mutableStateOf(initialHora) }
        val fecha by remember {
            derivedStateOf {
                DateTimeFormatter.ofPattern("yyyy-MM-dd").format(fechaState)
            }
        }
        val hora by remember {
            derivedStateOf {
                DateTimeFormatter.ofPattern("HH:mm:ss").format(horaState)
            }
        }
        val fechaDialogState = rememberMaterialDialogState()
        val horaDialogState = rememberMaterialDialogState()
        var clienteSeleccionado by remember { mutableStateOf<ClienteM?>(initialCliente) }
        var repartidorSeleccionado by remember { mutableStateOf<RepartidorM?>(initialRepartidor) }
        var menuClienteExpandido by remember { mutableStateOf(false) }
        var menuRepartidorExpandido by remember { mutableStateOf(false) }

        val estrategiasDisponibles = remember {
            listOf(
                VentaNormal(),
                VentaDescuento(10.0),
                VentaDescuento(25.0)
            )
        }
        var estrategiaSeleccionada by remember { mutableStateOf(estrategiasDisponibles[0]) }
        var menuEstrategiaExpandido by remember { mutableStateOf(false) }

        val productos by productos.collectAsState()
        var productoSeleccionado by remember { mutableStateOf<ProductoM?>(null) }
        var cantidad by remember { mutableStateOf("") }
        var precio by remember { mutableStateOf("") }
        val initialDetalles = remember(ventaEditar) { ventaEditar?.detalles?.toMutableList() ?: emptyList() }
        //Consultar por key
        val detalles = remember { mutableStateListOf(*initialDetalles.toTypedArray()) }
        var detalleEditando by remember { mutableStateOf<DetalleVM?>(null) }
        var indiceEditando by remember { mutableStateOf<Int?>(null) }
        var formularioADVisible by remember { mutableStateOf(true) }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )
            },
            bottomBar = {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        val total by remember(detalles.toList(), estrategiaSeleccionada) {
                            derivedStateOf {
                                estrategiaSeleccionada.calcularTotal(detalles.toList())
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        )
                        {
                            Text(
                                text = "Total:",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$monedaString ${String.format("%.2f", total)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val mensaje = validarCamposVenta(
                                    clienteSeleccionado,
                                    repartidorSeleccionado,
                                    detalles.toList()
                                )
                                if (mensaje != null) {
                                    mostrarMensaje(mensaje)
                                } else {
                                    onGuardarVenta(
                                        ventaEditar?.nro,
                                        fecha,
                                        hora,
                                        clienteSeleccionado!!.id!!,
                                        repartidorSeleccionado!!.id!!,
                                        detalles.toList(),
                                        estrategiaSeleccionada // Aquí se pasa
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = clienteSeleccionado != null && repartidorSeleccionado != null && detalles.isNotEmpty()
                        ) {
                            Text(if (esEdicion) "Actualizar Venta" else "Finalizar Venta")
                        }
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            )
            {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                )
                {
                    OutlinedTextField(
                        value = fecha,
                        onValueChange = { },
                        label = { Text("Fecha *") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { fechaDialogState.show() }) {
                                Icon(painterResource(R.drawable.calendar_today_24px), contentDescription = "Seleccionar fecha")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = hora,
                        onValueChange = { },
                        label = { Text("Hora *") },
                        modifier = Modifier.weight(1f),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { horaDialogState.show() }) {
                                Icon(painterResource(R.drawable.schedule_24px), contentDescription = "Seleccionar hora")
                            }
                        }
                    )
                }
                MaterialDialog(
                    dialogState = fechaDialogState,
                    buttons = {
                        positiveButton(text = "OK")
                        negativeButton(text = "Cancelar")
                    }
                )
                {
                    datepicker (
                        initialDate = fechaState,
                        title = "Seleccionar fecha"
                    ) { fechaState = it }
                }
                MaterialDialog(
                    dialogState = horaDialogState,
                    buttons = {
                        positiveButton(text = "OK")
                        negativeButton(text = "Cancelar")
                    }
                )
                {
                    timepicker(
                        initialTime = horaState,
                        title = "Seleccionar hora"
                    ) { horaState = it }
                }
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = menuClienteExpandido,
                    onExpandedChange = { menuClienteExpandido = it }
                )
                {
                    OutlinedTextField(
                        value = clienteSeleccionado?.nombre ?: "Seleccione un cliente",
                        onValueChange = { },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuClienteExpandido) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        label = { Text("Cliente *") }
                    )
                    ExposedDropdownMenu(
                        expanded = menuClienteExpandido,
                        onDismissRequest = { menuClienteExpandido = false }
                    ) {
                        if (clientes.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No hay clientes disponibles") },
                                onClick = { }
                            )
                        } else {
                            clientes.forEach { cliente ->
                                DropdownMenuItem(
                                    text = { Text(cliente.nombre ?: "Sin nombre") },
                                    onClick = {
                                        clienteSeleccionado = cliente
                                        menuClienteExpandido = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = menuRepartidorExpandido,
                    onExpandedChange = { menuRepartidorExpandido = it }
                )
                {
                    OutlinedTextField(
                        value = repartidorSeleccionado?.nombre ?: "Seleccione un repartidor",
                        onValueChange = { },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuRepartidorExpandido) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        label = { Text("Repartidor *") }
                    )
                    ExposedDropdownMenu(
                        expanded = menuRepartidorExpandido,
                        onDismissRequest = { menuRepartidorExpandido = false }
                    ) {
                        if (repartidores.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No hay repartidores disponibles") },
                                onClick = { }
                            )
                        } else {
                            repartidores.forEach { repartidor ->
                                DropdownMenuItem(
                                    text = { Text(repartidor.nombre ?: "Sin nombre") },
                                    onClick = {
                                        repartidorSeleccionado = repartidor
                                        menuRepartidorExpandido = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // 10. Añadir el Dropdown para seleccionar la estrategia
                ExposedDropdownMenuBox(
                    expanded = menuEstrategiaExpandido,
                    onExpandedChange = { menuEstrategiaExpandido = it }
                )
                {
                    OutlinedTextField(
                        value = estrategiaSeleccionada.getNombre(),
                        onValueChange = { },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuEstrategiaExpandido) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        label = { Text("Tipo de Venta *") }
                    )
                    ExposedDropdownMenu(
                        expanded = menuEstrategiaExpandido,
                        onDismissRequest = { menuEstrategiaExpandido = false }
                    ) {
                        estrategiasDisponibles.forEach { estrategia ->
                            DropdownMenuItem(
                                text = { Text(estrategia.getNombre()) },
                                onClick = {
                                    estrategiaSeleccionada = estrategia
                                    menuEstrategiaExpandido = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                FormularioAgregarProducto(
                    formularioVisible = formularioADVisible,
                    onShowFormulario = { formularioADVisible = !formularioADVisible },
                    productos = productos,
                    productoSeleccionado = productoSeleccionado,
                    cantidad = cantidad,
                    precio = precio,
                    textoBotonAgregarDetalle = if (detalleEditando != null) "Actualizar" else "Agregar",
                    onSeleccionarProducto = {
                        productoSeleccionado = it
                        precio = it.precio?.toString() ?: ""
                    },
                    onCantidadChange = { cantidad = it },
                    onPrecioChange = { precio = it },
                    onAgregarDetalle = { detalleVenta ->
                        if (detalleEditando != null && indiceEditando != null) {
                            detalles[indiceEditando!!] = detalleVenta
                            detalleEditando = null
                            indiceEditando = null
                            mostrarMensaje("Producto actualizado")
                        } else {
                            detalles.add(detalleVenta)
                            mostrarMensaje("Producto agregado a la venta")
                        }
                        actualizarProductos(productos.filter { it.id != detalleVenta.producto?.id })
                        productoSeleccionado = null
                        cantidad = ""
                        precio = ""
                    },
                    editandoDetalle = detalleEditando != null,
                    onCancelarEdicion = {
                        actualizarProductos(productos.filter { it.id != detalleEditando?.producto?.id })
                        detalleEditando = null
                        indiceEditando = null
                        productoSeleccionado = null
                        cantidad = ""
                        precio = ""
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                ListaDetallesVenta(
                    detalles = detalles,
                    onEditarDetalle = { index ->
                        val detalle = detalles[index]
                        formularioADVisible = true
                        detalleEditando = detalle
                        indiceEditando = index
                        productoSeleccionado = detalle.producto
                        cantidad = detalle.cantidad?.toString() ?: ""
                        precio = detalle.precio?.toString() ?: ""

                        val producto = detalle.producto
                        val p = mutableListOf(*productos.toTypedArray())
                        p.add(producto!!)
                        actualizarProductos(p)
                    },
                    onEliminarDetalle = { index ->
                        val detalle = detalles[index]
                        detalles.removeAt(index)
                        if (detalleEditando == detalle) {
                            detalleEditando = null
                            indiceEditando = null
                            productoSeleccionado = null
                            cantidad = ""
                            precio = ""
                        }

                        val producto = detalle.producto
                        val p = mutableListOf(*productos.toTypedArray())
                        p.add(producto!!)
                        actualizarProductos(p)
                    },
                    modifier = Modifier.weight(1f).padding(bottom = 16.dp),
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun FormularioAgregarProducto(
        formularioVisible: Boolean,
        onShowFormulario: () -> Unit,
        productos: List<ProductoM>,
        productoSeleccionado: ProductoM?,
        cantidad: String,
        precio: String,
        textoBotonAgregarDetalle: String,
        onSeleccionarProducto: (ProductoM) -> Unit,
        onCantidadChange: (String) -> Unit,
        onPrecioChange: (String) -> Unit,
        onAgregarDetalle: (DetalleVM) -> Unit,
        editandoDetalle: Boolean,
        onCancelarEdicion: () -> Unit
    )
    {
        var menuProductoExpandido by remember { mutableStateOf(false) }
        //val productos by productosState.collectAsState()
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        )
        {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable{
                        onShowFormulario()
                    }
                ) {
                    Text(
                        text = "Agregar Detalle",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null)
                }
                if (formularioVisible) {
                    Spacer(modifier = Modifier.height(8.dp))
                    // Selección de producto
                    ExposedDropdownMenuBox(
                        expanded = menuProductoExpandido,
                        onExpandedChange = { menuProductoExpandido = it }
                    ) {
                        OutlinedTextField(
                            value = productoSeleccionado?.nombre
                                ?: "Seleccione un producto",
                            onValueChange = { },
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = menuProductoExpandido
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            label = { Text("Producto *") }
                        )
                        ExposedDropdownMenu(
                            expanded = menuProductoExpandido,
                            onDismissRequest = { menuProductoExpandido = false }
                        ) {
                            if (productos.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No hay productos disponibles") },
                                    onClick = { }
                                )
                            } else {
                                productos.forEach { producto ->
                                    DropdownMenuItem(
                                        text = {
                                            Text("${producto.nombre} - $${producto.precio} (Stock: ${producto.cantidad})")
                                        },
                                        onClick = {
                                            onSeleccionarProducto(producto)
                                            menuProductoExpandido = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = cantidad,
                            onValueChange = { onCantidadChange(it) },
                            label = { Text("Cantidad *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = precio,
                            onValueChange = { onPrecioChange(it) },
                            label = { Text("Precio *") },
                            leadingIcon = { Text(monedaString) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    )
                    {
                        Button(
                            onClick = {
                                val nuevoDetalle = obtenerDetalleVentaValidado(
                                    producto = productoSeleccionado,
                                    cantidadStr = cantidad,
                                    precioStr = precio
                                )
                                if (nuevoDetalle != null) {
                                    onAgregarDetalle(nuevoDetalle)
                                    menuProductoExpandido = false
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(textoBotonAgregarDetalle)
                        }
                        if (editandoDetalle) {
                            Button(
                                onClick = { onCancelarEdicion() },
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
            }
        }
    }

    private fun validarCamposVenta(
        clienteSeleccionado: ClienteM?,
        repartidorSeleccionado: RepartidorM?,
        detalles: List<DetalleVM>
    ): String?
    {
        if (clienteSeleccionado == null) {
            return "Seleccione un cliente"
        }
        if (repartidorSeleccionado == null) {
            return "Seleccione un repartidor"
        }
        if (detalles.isEmpty()) {
            return "Agregue al menos un producto"
        }
        return null
    }
    private fun obtenerDetalleVentaValidado(
        producto: ProductoM?,
        cantidadStr: String,
        precioStr: String
    ): DetalleVM?
    {
        if (producto == null) {
            mostrarMensaje("Seleccione un producto")
            return null
        }
        if (cantidadStr.isBlank() || precioStr.isBlank()) {
            mostrarMensaje("Complete cantidad y precio")
            return null
        }
        val cantidad = cantidadStr.toIntOrNull()
        val precio = precioStr.toDoubleOrNull()
        if (cantidad == null || cantidad <= 0) {
            mostrarMensaje("Cantidad debe ser un número mayor a 0")
            return null
        }
        if (precio == null || precio <= 0) {
            mostrarMensaje("Precio debe ser un número mayor a 0")
            return null
        }
        val stockDisponible = producto.cantidad ?: 0
        if (cantidad > stockDisponible) {
            mostrarMensaje("Stock insuficiente. Disponible: $stockDisponible")
            return null
        }
        return DetalleVM(
            producto = producto,
            cantidad = cantidad,
            precio = precio
        )
    }

    @Composable
    private fun ListaDetallesVenta(
        detalles: List<DetalleVM>,
        onEditarDetalle: (Int) -> Unit,
        onEliminarDetalle: (Int) -> Unit,
        modifier: Modifier = Modifier
    )
    {
        Text(
            text = "Productos en la venta (${detalles.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (detalles.isEmpty()) {
            Text(
                text = "No hay productos agregados",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(detalles.size) { index ->
                    val detalle = detalles[index]
                    DetalleVentaItem(
                        detalle = detalle,
                        onEditar = { onEditarDetalle(index) },
                        onEliminar = {
                            onEliminarDetalle(index)
                            mostrarMensaje("Producto eliminado")
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun DetalleVentaItem(
        detalle: DetalleVM,
        onEditar: () -> Unit,
        onEliminar: () -> Unit
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = detalle.producto?.nombre ?: "Producto sin nombre",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "Cantidad: ${detalle.cantidad}")
                Text(text = "Precio unitario: $${String.format("%.2f", detalle.precio ?: 0.0)}")
                Text(
                    text = "Subtotal: $${String.format("%.2f", detalle.calcularSubtotal())}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
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