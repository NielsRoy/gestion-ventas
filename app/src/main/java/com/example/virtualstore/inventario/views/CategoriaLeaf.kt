package com.example.virtualstore.inventario.views

import com.example.virtualstore.inventario.models.CategoriaM

class CategoriaLeaf: CategoriaComponent {
    private var categoria: CategoriaM

    constructor(categoria: CategoriaM) {
        this.categoria = categoria
    }

    override fun getCantProductos(c: CategoriaM): Int {
        if (c.id == null) return 0;
        return c.getCantProductos(c.id!!);
    }

    override fun getCategoria(): CategoriaM {
        return categoria
    }
}