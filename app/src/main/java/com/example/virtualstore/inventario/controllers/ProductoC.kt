package com.example.virtualstore.inventario.controllers

import com.example.virtualstore.inventario.models.ProductoM
import com.example.virtualstore.inventario.views.ProductoV

class ProductoC {
    val productoV: ProductoV
    val productoM: ProductoM

    constructor(productoV: ProductoV, productoM: ProductoM) {
        this.productoV = productoV
        this.productoM = productoM
        configurarListeners()
        cargarDatos()
    }

    private fun configurarListeners() {
        productoV.setGuardarListener { nombre, precio, cantidad, categoriaId ->
            crear(nombre, precio, cantidad, categoriaId)
        }

        productoV.setEditarListener { id, nombre, precio, cantidad, categoriaId ->
            actualizar(id, nombre, precio, cantidad, categoriaId)
        }

        productoV.setEliminarListener { id ->
            eliminar(id)
        }
    }

    fun cargarDatos() {
        productoV.actualizarProductos(productoM.obtenerTodos())
        productoV.actualizarCategorias(productoM.obtenerTodasLasCategorias())
    }

    private fun crear(nombre: String, precioStr: String, cantidadStr: String, categoriaId: Int) {
        try {
            productoM.crear(nombre, precioStr, cantidadStr, categoriaId)
            productoV.mostrarMensaje("Producto guardado exitosamente")
            cargarDatos()
        } catch (e: Exception) {
            productoV.mostrarMensaje(e.message)
        }
    }

    private fun actualizar(id: Int, nombre: String, precioStr: String, cantidadStr: String, categoriaId: Int) {
        try {
            productoM.actualizar(id, nombre, precioStr, cantidadStr, categoriaId)
            productoV.mostrarMensaje("Producto actualizado exitosamente")
            cargarDatos()
        } catch (e: Exception) {
            productoV.mostrarMensaje(e.message)
        }
    }

    private fun eliminar(id: Int) {
        try {
            productoM.eliminar(id)
            productoV.mostrarMensaje("Producto eliminado exitosamente")
            cargarDatos()
        } catch (e: Exception) {
            productoV.mostrarMensaje(e.message)
        }
    }
}