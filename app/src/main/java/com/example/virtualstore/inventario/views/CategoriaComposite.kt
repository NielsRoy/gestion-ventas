package com.example.virtualstore.inventario.views

import com.example.virtualstore.inventario.models.CategoriaM

class CategoriaComposite: CategoriaComponent {
    private var categoria: CategoriaM
    private var children: MutableList<CategoriaComponent> = mutableListOf()

    constructor(categoria: CategoriaM) {
        this.categoria = categoria
    }

    override fun getCantProductos(c: CategoriaM): Int {
        var total = c.getCantProductos(categoria.id!!);
        for (sub in children) {
            total += sub.getCantProductos(sub.getCategoria())
        }
        return total
    }

    override fun getCategoria(): CategoriaM {
        return this.categoria
    }

    fun agregar(c: CategoriaComponent) {
        children.add(c);
    }

    fun eliminar(c: CategoriaComponent) {
        children.remove(c);
    }

    fun getChildren(): List<CategoriaComponent> {
        return this.children
    }

}