package com.example.virtualstore.inventario.models

class CategoriaComposite: CategoriaComponent {
    private var categoria: CategoriaM
    var children: MutableList<CategoriaComponent> = mutableListOf()

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

}