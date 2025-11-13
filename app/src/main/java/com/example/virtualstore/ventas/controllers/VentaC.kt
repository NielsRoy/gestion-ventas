package com.example.virtualstore.ventas.controllers

import com.example.virtualstore.ventas.models.DetalleVentaM
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
        ventaV.setGuardarVentaListener { fecha, hora, clienteId, repartidorId, detalles ->
            crearVenta(fecha, hora, clienteId, repartidorId, detalles)
        }
        ventaV.setActualizarVentaListener { ventaNro, fecha, hora, clienteId, repartidorId, detalles ->
            actualizarVenta(ventaNro, fecha, hora, clienteId, repartidorId, detalles)
        }
        ventaV.setEliminarVentaListener {
            eliminarVenta(it)
        }
    }

    private fun cargarDatos() {
        ventaV.actualizarVentas(ventaM.obtenerTodos())
        ventaV.actualizarClientes(ventaM.obtenerTodosLosClientes())
        ventaV.actualizarRepartidores(ventaM.obtenerTodosLosRepartidores())
        ventaV.actualizarProductos(ventaM.obtenerTodosLosProductos())
    }

    fun crearVenta(fecha: String, hora: String, clienteId: Int, repartidorId: Int,  detalles: List<DetalleVentaM>) {
        try {
            ventaM.crear(fecha, hora, clienteId, repartidorId, detalles)
            ventaV.mostrarMensaje("Venta registrada exitosamente")
            cargarDatos()
        } catch (e: Exception) {
            ventaV.mostrarMensaje(e.message)
        }
    }

    fun actualizarVenta(ventaNro: Int, fecha: String, hora: String, clienteId: Int, repartidorId: Int, detalles: List<DetalleVentaM>) {
        try {
            ventaM.actualizar(ventaNro, fecha, hora, clienteId, repartidorId, detalles)
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