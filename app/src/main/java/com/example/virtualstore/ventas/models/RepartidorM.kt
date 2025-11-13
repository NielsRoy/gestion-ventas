package com.example.virtualstore.ventas.models

import android.content.ContentValues
import android.database.Cursor
import com.example.virtualstore.DBHelper

data class RepartidorM(
    var id: Int? = null,
    var nombre: String? = null,
    var celular: Int? = null,
    var direccion: String? = null,
    val dbHelper: DBHelper? = null
) {

    fun crear(nombre: String, celularStr: String, direccion: String) {
        if (nombre.isBlank() || celularStr.isBlank()) {
            throw Exception("Los campos nombre y celular son obligatorios")
        }
        try {
            val celular = celularStr.toInt()
            if (celular <= 99 ) {
                throw Exception("Celular debe tener al menos 3 digitos")
            }
            val db = dbHelper?.writableDatabase ?: throw IllegalStateException("dbHelper is null")
            val values = toContentValues(nombre, celular, direccion)
            val result = db.insert("repartidor", null, values)
            if (result == -1L) {
                throw Exception("Error al guardar al repartidor")
            }
        } catch (e: NumberFormatException) {
            throw Exception("Celular deben ser un número válido")
        }
    }

    fun obtenerTodos(): List<RepartidorM> {
        val repartidoresList = mutableListOf<RepartidorM>()
        val db = dbHelper?.readableDatabase ?: throw IllegalStateException("dbHelper is null")
        val cursor = db.query(
            "repartidor",
            arrayOf("id", "nombre", "celular", "direccion"),
            null, null, null, null, "id DESC"
        )
        while (cursor.moveToNext()) {
            val repartidor = toRepartidorModel(cursor)
            repartidoresList.add(repartidor)
        }
        cursor.close()
        return repartidoresList
    }

    fun obtenerPorId(id: Int): RepartidorM? {
        val db = dbHelper?.readableDatabase ?: throw IllegalStateException("dbHelper is null")
        val cursor = db.query(
            "repartidor",
            arrayOf("id", "nombre", "celular", "direccion"),
            "id = ?",
            arrayOf(id.toString()),
            null, null, null
        )
        var repartidor: RepartidorM? = null
        if(cursor.moveToFirst()) {
            repartidor = toRepartidorModel(cursor)
        }
        cursor.close()
        return repartidor
    }

    fun actualizar(id: Int, nombre: String, celularStr: String, direccion: String) {
        if (nombre.isBlank() || celularStr.isBlank()) {
            throw Exception("Los campos nombre y celular son obligatorios")
        }
        try {
            val celular = celularStr.toInt()
            if (celular <= 99) {
                throw Exception("Celular debe tener al menos 3 digitos")
            }
            val db = dbHelper?.writableDatabase ?: throw IllegalStateException("dbHelper is null")
            val values = toContentValues(nombre, celular, direccion)
            val filasAfectadas = db.update("repartidor", values, "id = ?", arrayOf(id.toString()))
            if (filasAfectadas <= 0) {
                throw Exception("Error al actualizar al repartidor")
            }
        } catch (e: NumberFormatException) {
            throw Exception("Celular deben ser un número válido")
        }
    }

    fun eliminar(id: Int) {
        val db = dbHelper?.writableDatabase ?: throw IllegalStateException("dbHelper is null")
        val filasAfectadas = db.delete("repartidor", "id = ?", arrayOf(id.toString()))
        if (filasAfectadas <= 0) {
            throw Exception("Error al eliminar al repartidor")
        }
    }

    private fun toContentValues(nombre: String, celular: Int, direccion: String): ContentValues {
        return ContentValues().apply {
            put("nombre", nombre)
            put("celular", celular)
            put("direccion", direccion)
        }
    }

    private fun toRepartidorModel(cursor: Cursor): RepartidorM {
        with(cursor) {
            return RepartidorM(
                id = getInt(getColumnIndexOrThrow("id")),
                nombre = getString(getColumnIndexOrThrow("nombre")),
                celular = getInt(getColumnIndexOrThrow("celular")),
                direccion = getString(getColumnIndexOrThrow("direccion")),
            )
        }
    }
}