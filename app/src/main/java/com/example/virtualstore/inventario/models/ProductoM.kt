package com.example.virtualstore.inventario.models

import android.content.ContentValues
import android.database.Cursor
import com.example.virtualstore.DBHelper

data class ProductoM(
    var id: Int? = null,
    var nombre: String? = null,
    var precio: Double? = null,
    var cantidad: Int? = null,
    var categoria: CategoriaM? = null,
    val dbHelper: DBHelper? = null
) {
    fun crear(nombre: String, precioStr: String, cantidadStr: String, categoriaId: Int) {
        if (nombre.isBlank() || precioStr.isBlank() || cantidadStr.isBlank()) {
            throw Exception("Todos los campos son obligatorios")
        }
        try {
            val precio = precioStr.toDouble()
            val cantidad = cantidadStr.toInt()
            if (precio <= 0 || cantidad <= 0) {
                throw Exception("Precio y cantidad deben ser mayores a 0")
            }
            val db = dbHelper?.writableDatabase ?: throw IllegalStateException("dbHelper is null")
            val values = toContentValues(nombre, precio, cantidad, categoriaId)
            val result = db.insert("producto", null, values)
            if (result == -1L) {
                throw Exception("Error al guardar el producto")
            }
        } catch (e: NumberFormatException) {
            throw Exception("Precio y cantidad deben ser números válidos")
        }
    }

    fun obtenerTodos(): List<ProductoM> {
        val productosList = mutableListOf<ProductoM>()
        if (dbHelper == null) {
            throw IllegalStateException("dbHelper is null")
        }
        if (dbHelper.readableDatabase == null) {
            throw IllegalStateException("dbHelper is null")
        }
        val db = dbHelper?.readableDatabase
        val cursor = db!!.query(
            "producto",
            arrayOf("id", "nombre", "precio", "cantidad", "categoria_id"),
            null, null, null, null, "id DESC"
        )
        while (cursor.moveToNext()) {
            val producto = toProductoModel(cursor)
            productosList.add(producto)
        }
        cursor.close()
        return productosList
    }

    fun obtenerPorId(id: Int): ProductoM? {
        val db = dbHelper?.readableDatabase ?: throw IllegalStateException("dbHelper is null")
        val cursor = db.query(
            "producto",
            arrayOf("id", "nombre", "precio", "cantidad", "categoria_id"),
            "id = ?",
            arrayOf(id.toString()),
            null, null, null
        )
        var producto: ProductoM? = null
        if(cursor.moveToFirst()) {
            producto = toProductoModel(cursor)
        }
        cursor.close()
        return producto
    }

    fun actualizar(id: Int, nombre: String, precioStr: String, cantidadStr: String, categoriaId: Int) {
        if (nombre.isBlank() || precioStr.isBlank() || cantidadStr.isBlank()) {
            throw Exception("Todos los campos son obligatorios")
        }
        try {
            val precio = precioStr.toDouble()
            val cantidad = cantidadStr.toInt()
            if (precio <= 0 || cantidad <= 0) {
                throw Exception("Precio y cantidad deben ser mayores a 0")
            }
            val db = dbHelper?.writableDatabase ?: throw IllegalStateException("dbHelper is null")
            val values = toContentValues(nombre, precio, cantidad, categoriaId)
            val filasAfectadas = db.update("producto", values, "id = ?", arrayOf(id.toString()))
            if (filasAfectadas <= 0) {
                throw Exception("Error al actualizar el producto")
            }
        } catch (e: NumberFormatException) {
            throw Exception("Precio y cantidad deben ser números válidos")
        }
    }

    fun eliminar(id: Int) {
        val db = dbHelper?.writableDatabase ?: throw IllegalStateException("dbHelper is null")
        val filasAfectadas = db.delete("producto", "id = ?", arrayOf(id.toString()))
        if (filasAfectadas <= 0) {
            throw Exception("Error al eliminar el producto")
        }
    }

    private fun toContentValues(nombre: String, precio: Double, cantidad: Int, categoriaId: Int): ContentValues {
        return ContentValues().apply {
            put("nombre", nombre)
            put("precio", precio)
            put("cantidad", cantidad)
            put("categoria_id", categoriaId)
        }
    }

    private fun toProductoModel(cursor: Cursor): ProductoM {
        with(cursor) {
            val categoriaId = getInt(getColumnIndexOrThrow("categoria_id"))
            return ProductoM(
                id = getInt(getColumnIndexOrThrow("id")),
                nombre = getString(getColumnIndexOrThrow("nombre")),
                precio = getDouble(getColumnIndexOrThrow("precio")),
                cantidad = getInt(getColumnIndexOrThrow("cantidad")),
                categoria = CategoriaM(dbHelper = dbHelper).obtenerPorId(categoriaId)
            )
        }
    }

    fun obtenerTodasLasCategorias(): List<CategoriaM> {
        return CategoriaM(dbHelper = dbHelper).obtenerTodos()
    }
}