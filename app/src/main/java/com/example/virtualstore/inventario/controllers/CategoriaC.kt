package com.example.virtualstore.inventario.controllers

import com.example.virtualstore.inventario.models.CategoriaM
import com.example.virtualstore.inventario.views.CategoriaV

class CategoriaC {
    private val categoriaV: CategoriaV
    private val categoriaM: CategoriaM

    constructor(categoriaV: CategoriaV, categoriaM: CategoriaM) {
        this.categoriaV = categoriaV
        this.categoriaM = categoriaM
        configListeners()
        categoriaV.actualizarCategs(categoriaM.getTodos())
    }

    private fun configListeners() {
        categoriaV.setGuardarLn { nombre, descripcion, parentId ->
            crear(nombre, descripcion, parentId)
        }

        categoriaV.setEditarLn { id, nombre, descripcion, parentId ->
            actualizar(id, nombre, descripcion, parentId)
        }

        categoriaV.setEliminarLn { id ->
            eliminar(id)
        }
    }

    fun crear(nombre: String, descripcion: String, categoria_id: Int? = null) {
        try {
            categoriaM.crear(nombre, descripcion, categoria_id)
            categoriaV.showMsj("Categoría guardada exitosamente")
            categoriaV.actualizarCategs(categoriaM.getTodos())
        } catch (e: Exception) {
            categoriaV.showMsj(e.message)
        }
    }

    fun actualizar(id: Int, nombre: String, descripcion: String, categoria_id: Int? = null) {
        try {
            categoriaM.actualizar(id, nombre, descripcion, categoria_id)
            categoriaV.showMsj("Categoría actualizada exitosamente")
            categoriaV.actualizarCategs(categoriaM.getTodos())
        } catch (e: Exception) {
            categoriaV.showMsj(e.message)
        }
    }

    fun eliminar(id: Int) {
        try {
            categoriaM.eliminar(id)
            categoriaV.showMsj("Categoría eliminada exitosamente")
            categoriaV.actualizarCategs(categoriaM.getTodos())
        } catch (e: Exception) {
            categoriaV.showMsj(e.message)
        }
    }
}