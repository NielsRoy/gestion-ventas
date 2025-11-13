package com.example.virtualstore.ventas.controllers

import com.example.virtualstore.ventas.models.ClienteM
import com.example.virtualstore.ventas.views.ClienteV

class ClienteC {
    private val clienteV: ClienteV
    private val clienteM: ClienteM

    constructor(clienteV: ClienteV, clienteM: ClienteM) {
        this.clienteV = clienteV
        this.clienteM = clienteM
        configurarListeners()
        clienteV.actualizarClientes(clienteM.obtenerTodos())
    }

    private fun configurarListeners() {
        clienteV.setGuardarListener { nombre, celular, direccion ->
            crear(nombre, celular, direccion)
        }
        clienteV.setEditarListener { id, nombre, precio, cantidad ->
            actualizar(id, nombre, precio, cantidad)
        }
        clienteV.setEliminarListener { id ->
            eliminar(id)
        }
    }

    fun crear(nombre: String, celular: String, direccion: String) {
        try {
            clienteM.crear(nombre, celular, direccion)
            clienteV.mostrarMensaje("Producto guardado exitosamente")
            clienteV.actualizarClientes(clienteM.obtenerTodos())
        } catch (e: Exception) {
            clienteV.mostrarMensaje(e.message)
        }
    }

    fun actualizar(id: Int, nombre: String, precioStr: String, cantidadStr: String) {
        try {
            clienteM.actualizar(id, nombre, precioStr, cantidadStr)
            clienteV.mostrarMensaje("Producto actualizado exitosamente")
            clienteV.actualizarClientes(clienteM.obtenerTodos())
        } catch (e: Exception) {
            clienteV.mostrarMensaje(e.message)
        }
    }

    fun eliminar(id: Int) {
        try {
            clienteM.eliminar(id)
            clienteV.mostrarMensaje("Producto eliminado exitosamente")
            clienteV.actualizarClientes(clienteM.obtenerTodos())
        } catch (e: Exception) {
            clienteV.mostrarMensaje(e.message)
        }
    }
}