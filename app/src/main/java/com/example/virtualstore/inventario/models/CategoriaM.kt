package com.example.virtualstore.inventario.models

import android.content.ContentValues
import android.database.Cursor
import com.example.virtualstore.DBHelper

data class CategoriaM (
    var id: Int? = null,
    var nombre: String? = null,
    var descripcion: String? = null,
    val dbHelper: DBHelper? = null
) {
    fun crear(nombre: String, descripcion: String) {
        if (nombre.isBlank()) {
            throw Exception("El campo nombre es obligatorio")
        }
        val db = dbHelper?.writableDatabase ?: throw IllegalStateException("dbHelper is null")
        val values = toContentValues(nombre, descripcion)
        val result = db.insert("categoria", null, values)
        if (result == -1L) {
            throw Exception("Error al guardar la categoría")
        }
    }
    fun obtenerTodos(): List<CategoriaM> {
        val categoriasList = mutableListOf<CategoriaM>()
        val db = dbHelper?.readableDatabase ?: throw IllegalStateException("dbHelper is null")
        val cursor = db.query(
            "categoria",
            arrayOf("id", "nombre", "descripcion"),
            null, null, null, null, "id DESC"
        )
        while (cursor.moveToNext()) {
            val categoria = toCategoriaModel(cursor)
            categoriasList.add(categoria)
        }
        cursor.close()
        return categoriasList
    }

    fun obtenerPorId(id: Int): CategoriaM? {
        val db = dbHelper?.readableDatabase ?: throw IllegalStateException("dbHelper is null")
        val cursor = db.query(
            "categoria",
            arrayOf("id", "nombre", "descripcion"),
            "id = ?",
            arrayOf(id.toString()),
            null, null, null
        )
        var categoria: CategoriaM? = null
        if(cursor.moveToFirst()) {
            categoria = toCategoriaModel(cursor)
        }
        cursor.close()
        return categoria
    }

    fun actualizar(id: Int, nombre: String, descripcion: String) {
        if (nombre.isBlank()) {
            throw Exception("El campo nombre es obligatorio")
        }
        val db = dbHelper?.writableDatabase ?: throw IllegalStateException("dbHelper is null")
        val values = toContentValues(nombre, descripcion)
        val filasAfectadas = db.update("categoria", values, "id = ?", arrayOf(id.toString()))
        if (filasAfectadas <= 0) {
            throw Exception("Error al actualizar la categoría")
        }
    }

    fun eliminar(id: Int) {
        val db = dbHelper?.writableDatabase ?: throw IllegalStateException("dbHelper is null")
        val filasAfectadas = db.delete("categoria", "id = ?", arrayOf(id.toString()))
        if (filasAfectadas <= 0) {
            throw Exception("Error al eliminar la categoría")
        }
    }

    private fun toContentValues(nombre: String, descripcion: String): ContentValues {
        return ContentValues().apply {
            put("nombre", nombre)
            put("descripcion", descripcion)
        }
    }

    private fun toCategoriaModel(cursor: Cursor): CategoriaM {
        with(cursor) {
            return CategoriaM(
                id = getInt(getColumnIndexOrThrow("id")),
                nombre = getString(getColumnIndexOrThrow("nombre")),
                descripcion = getString(getColumnIndexOrThrow("descripcion"))
            )
        }
    }
}