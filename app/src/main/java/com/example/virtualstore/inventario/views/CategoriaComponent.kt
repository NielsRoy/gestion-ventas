package com.example.virtualstore.inventario.views

import com.example.virtualstore.inventario.models.CategoriaM

interface CategoriaComponent {

    fun getCantProductos(c: CategoriaM): Int

    fun getCategoria(): CategoriaM

}