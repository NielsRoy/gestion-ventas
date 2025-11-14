package com.example.virtualstore.inventario.models

import android.content.ContentValues
import android.database.Cursor
import com.example.virtualstore.DBHelper

data class CategoriaM (
    var id: Int? = null,
    var nombre: String? = null,
    var descripcion: String? = null,
    var categoria_id: Int? = null,
    val dbHelper: DBHelper? = null
) {
    fun crear(nombre: String, descripcion: String, categoria_id: Int? = null) {
        if (nombre.isBlank()) {
            throw Exception("El campo nombre es obligatorio")
        }
        val db = dbHelper?.writableDatabase ?: throw IllegalStateException("dbHelper is null")
        val values = toCV(nombre, descripcion, categoria_id)
        val result = db.insert("categoria", null, values)
        if (result == -1L) {
            throw Exception("Error al guardar la categoría")
        }
    }
    fun getTodos(): List<CategoriaM> {
        val categoriasList = mutableListOf<CategoriaM>()
        val db = dbHelper?.readableDatabase ?: throw IllegalStateException("dbHelper is null")
        val cursor = db.query(
            "categoria",
            arrayOf("id", "nombre", "descripcion", "categoria_id"),
            null, null, null, null, "id DESC"
        )
        while (cursor.moveToNext()) {
            val categoria = toCM(cursor)
            categoriasList.add(categoria)
        }
        cursor.close()
        return categoriasList
    }

    fun getPorId(id: Int): CategoriaM? {
        val db = dbHelper?.readableDatabase ?: throw IllegalStateException("dbHelper is null")
        val cursor = db.query(
            "categoria",
            arrayOf("id", "nombre", "descripcion", "categoria_id"),
            "id = ?",
            arrayOf(id.toString()),
            null, null, null
        )
        var categoria: CategoriaM? = null
        if(cursor.moveToFirst()) {
            categoria = toCM(cursor)
        }
        cursor.close()
        return categoria
    }

    fun actualizar(id: Int, nombre: String, descripcion: String, categoria_id: Int? = null) {
        if (nombre.isBlank()) {
            throw Exception("El campo nombre es obligatorio")
        }
        val db = dbHelper?.writableDatabase ?: throw IllegalStateException("dbHelper is null")
        val values = toCV(nombre, descripcion, categoria_id)
        val filasAfectadas = db.update("categoria", values, "id = ?", arrayOf(id.toString()))
        if (filasAfectadas <= 0) {
            throw Exception("Error al actualizar la categoría")
        }
    }

    fun eliminar(id: Int) {
        val db = dbHelper?.writableDatabase ?: throw IllegalStateException("dbHelper is null")
        val subCategorias = getSubcate(id)
        if (subCategorias.isNotEmpty()) {
            throw Exception("No se puede eliminar: Esta categoría contiene subcategorías.")
        }
        val filasAfectadas = db.delete("categoria", "id = ?", arrayOf(id.toString()))
        if (filasAfectadas <= 0) {
            throw Exception("Error al eliminar la categoría")
        }
    }

    private fun toCV(nombre: String, descripcion: String, categoria_id: Int? = null): ContentValues {
        return ContentValues().apply {
            put("nombre", nombre)
            put("descripcion", descripcion)
            put("categoria_id", categoria_id)
        }
    }

    private fun toCM(cursor: Cursor): CategoriaM {
        with(cursor) {
            val catIdIndex = getColumnIndexOrThrow("categoria_id")
            val categoriaId = if (isNull(catIdIndex)) null else getInt(catIdIndex)

            return CategoriaM(
                id = getInt(getColumnIndexOrThrow("id")),
                nombre = getString(getColumnIndexOrThrow("nombre")),
                descripcion = getString(getColumnIndexOrThrow("descripcion")),
                categoria_id = categoriaId,
                dbHelper = dbHelper // <-- IMPORTANTE: AÑADIDO (para que el modelo pueda hacer consultas)
            )
        }
    }

    fun getCantProductos(id: Int): Int {
        val db = dbHelper?.readableDatabase ?: throw IllegalStateException("dbHelper is null")
        val query = "SELECT COUNT(id) FROM producto WHERE categoria_id = ?"
        val cursor = db.rawQuery(query, arrayOf(id.toString()))
        var cantidad = 0
        if (cursor.moveToFirst()) {
            cantidad = cursor.getInt(0)
        }
        cursor.close()
        return cantidad
    }

    fun getSubcate(parentId: Int): List<CategoriaM> {
        val categoriasList = mutableListOf<CategoriaM>()
        val db = dbHelper?.readableDatabase ?: throw IllegalStateException("dbHelper is null")
        val cursor = db.query(
            "categoria",
            arrayOf("id", "nombre", "descripcion", "categoria_id"),
            "categoria_id = ?", // Selección por ID padre
            arrayOf(parentId.toString()),
            null, null, null
        )
        while (cursor.moveToNext()) {
            val categoria = toCM(cursor)
            categoriasList.add(categoria)
        }
        cursor.close()
        return categoriasList
    }

//    fun getComponentes(c: List<CategoriaM>): List<CategoriaComponent> {
//
//    }

}