package com.example.virtualstore.ventas.controllers

import com.example.virtualstore.ventas.models.RepartidorM
import com.example.virtualstore.ventas.views.RepartidorV

class RepartidorC {
    private val repartidorV: RepartidorV
    private val repartidorM: RepartidorM

    constructor(repartidorV: RepartidorV, repartidorM: RepartidorM) {
        this.repartidorV = repartidorV
        this.repartidorM = repartidorM
        configurarListeners()
        repartidorV.actualizarRepartidores(repartidorM.getAll())
    }

    private fun configurarListeners() {
        repartidorV.setGuardarListener { nombre, celular, direccion ->
            crear(nombre, celular, direccion)
        }
        repartidorV.setEditarListener { id, nombre, precio, cantidad ->
            actualizar(id, nombre, precio, cantidad)
        }
        repartidorV.setEliminarListener { id ->
            eliminar(id)
        }
    }

    private fun crear(nombre: String, celular: String, direccion: String) {
        try {
            repartidorM.crear(nombre, celular, direccion)
            repartidorV.mostrarMensaje("Repartidor guardado exitosamente")
            repartidorV.actualizarRepartidores(repartidorM.getAll())
        } catch (e: Exception) {
            repartidorV.mostrarMensaje(e.message)
        }
    }

    private fun actualizar(id: Int, nombre: String, precioStr: String, cantidadStr: String) {
        try {
            repartidorM.actualizar(id, nombre, precioStr, cantidadStr)
            repartidorV.mostrarMensaje("Repartidor actualizado exitosamente")
            repartidorV.actualizarRepartidores(repartidorM.getAll())
        } catch (e: Exception) {
            repartidorV.mostrarMensaje(e.message)
        }
    }

    private fun eliminar(id: Int) {
        try {
            repartidorM.eliminar(id)
            repartidorV.mostrarMensaje("Repartidor eliminado exitosamente")
            repartidorV.actualizarRepartidores(repartidorM.getAll())
        } catch (e: Exception) {
            repartidorV.mostrarMensaje(e.message)
        }
    }
}