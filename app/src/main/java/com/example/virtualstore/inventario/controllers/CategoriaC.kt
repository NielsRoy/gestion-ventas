package com.example.virtualstore.inventario.controllers

import com.example.virtualstore.inventario.models.CategoriaM
import com.example.virtualstore.inventario.views.CategoriaV

class CategoriaC {
    private val categoriaV: CategoriaV
    private val categoriaM: CategoriaM

    constructor(categoriaV: CategoriaV, categoriaM: CategoriaM) {
        this.categoriaV = categoriaV
        this.categoriaM = categoriaM
        configurarListeners()
        categoriaV.actualizarCategorias(categoriaM.obtenerTodos())
    }

    private fun configurarListeners() {
        categoriaV.setGuardarListener { nombre, descripcion, parentId ->
            crear(nombre, descripcion, parentId)
        }

        categoriaV.setEditarListener { id, nombre, descripcion, parentId ->
            actualizar(id, nombre, descripcion, parentId)
        }

        categoriaV.setEliminarListener { id ->
            eliminar(id)
        }
    }

    fun crear(nombre: String, descripcion: String, categoria_id: Int? = null) {
        try {
            categoriaM.crear(nombre, descripcion, categoria_id)
            categoriaV.mostrarMensaje("Categoría guardada exitosamente")
            categoriaV.actualizarCategorias(categoriaM.obtenerTodos())
        } catch (e: Exception) {
            categoriaV.mostrarMensaje(e.message)
        }
    }

    fun actualizar(id: Int, nombre: String, descripcion: String, categoria_id: Int? = null) {
        try {
            categoriaM.actualizar(id, nombre, descripcion, categoria_id)
            categoriaV.mostrarMensaje("Categoría actualizada exitosamente")
            categoriaV.actualizarCategorias(categoriaM.obtenerTodos())
        } catch (e: Exception) {
            categoriaV.mostrarMensaje(e.message)
        }
    }

    fun eliminar(id: Int) {
        try {
            categoriaM.eliminar(id)
            categoriaV.mostrarMensaje("Categoría eliminada exitosamente")
            categoriaV.actualizarCategorias(categoriaM.obtenerTodos())
        } catch (e: Exception) {
            categoriaV.mostrarMensaje(e.message)
        }
    }
}