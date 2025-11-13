package com.example.virtualstore.inventario.models

interface CategoriaComponent {

    fun getCantProductos(c: CategoriaM): Int

    fun getCategoria(): CategoriaM

}