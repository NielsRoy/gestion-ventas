package com.example.virtualstore.ventas.controllers

import com.example.virtualstore.ventas.models.DetalleVM
import com.example.virtualstore.ventas.models.VentaStrategy // Importar la interfaz
import com.example.virtualstore.ventas.models.VentaM
import com.example.virtualstore.ventas.views.VentaV

class VentaC {
    private val ventaV: VentaV
    private val ventaM: VentaM

    constructor(ventaV: VentaV, ventaM: VentaM) {
        this.ventaV = ventaV
        this.ventaM = ventaM
        configurarListeners()
        cargarDatos()
    }

    private fun configurarListeners() {
        ventaV.setGuardarVentaListener { fecha, hora, clienteId, repartidorId, detalles, estrategia ->
            crearVenta(fecha, hora, clienteId, repartidorId, detalles, estrategia)
        }
        ventaV.setActualizarVentaListener { ventaNro, fecha, hora, clienteId, repartidorId, detalles, estrategia ->
            actualizarVenta(ventaNro, fecha, hora, clienteId, repartidorId, detalles, estrategia)
        }
        ventaV.setEliminarVentaListener {
            eliminarVenta(it)
        }
    }

    private fun cargarDatos() {
        ventaV.actualizarVentas(ventaM.getTodos())
        ventaV.actualizarClientes(ventaM.getAllClientes())
        ventaV.actualizarRepartidores(ventaM.getAllRepartidores())
        ventaV.actualizarProductos(ventaM.getAllProductos())
    }

    fun crearVenta(fecha: String, hora: String, clienteId: Int, repartidorId: Int, detalles: List<DetalleVM>,
                   estrategia: VentaStrategy
    ) {
        try {
            ventaM.crear(fecha, hora, clienteId, repartidorId, detalles, estrategia)
            ventaV.mostrarMensaje("Venta registrada exitosamente")
            cargarDatos()
        } catch (e: Exception) {
            ventaV.mostrarMensaje(e.message)
        }
    }

    fun actualizarVenta(ventaNro: Int, fecha: String, hora: String, clienteId: Int, repartidorId: Int, detalles: List<DetalleVM>,
                        estrategia: VentaStrategy
    ) {
        try {
            ventaM.actualizar(ventaNro, fecha, hora, clienteId, repartidorId, detalles, estrategia)
            ventaV.mostrarMensaje("Venta actualizada exitosamente")
            cargarDatos()
        } catch (e: Exception) {
            ventaV.mostrarMensaje(e.message)
        }
    }

    fun eliminarVenta(ventaNro: Int) {
        try {
            ventaM.eliminar(ventaNro)
            ventaV.mostrarMensaje("Venta eliminada exitosamente")
            cargarDatos()
        } catch (e: Exception) {
            ventaV.mostrarMensaje(e.message)
        }
    }
}