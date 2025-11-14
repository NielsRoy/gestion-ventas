package com.example.virtualstore.ventas.models

import android.content.ContentValues
import android.database.Cursor
import com.example.virtualstore.DBHelper

data class ClienteM(
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
            val values = toCV(nombre, celular, direccion)
            val result = db.insert("cliente", null, values)
            if (result == -1L) {
                throw Exception("Error al guardar el producto")
            }
        } catch (e: NumberFormatException) {
            throw Exception("Celular deben ser un número válido")
        }
    }

    fun getAll(): List<ClienteM> {
        val clientesList = mutableListOf<ClienteM>()
        val db = dbHelper?.readableDatabase ?: throw IllegalStateException("dbHelper is null")
        val cursor = db.query(
            "cliente",
            arrayOf("id", "nombre", "celular", "direccion"),
            null, null, null, null, "id DESC"
        )
        while (cursor.moveToNext()) {
            val cliente = toCM(cursor)
            clientesList.add(cliente)
        }
        cursor.close()
        return clientesList
    }

    fun getById(id: Int): ClienteM? {
        val db = dbHelper?.readableDatabase ?: throw IllegalStateException("dbHelper is null")
        val cursor = db.query(
            "cliente",
            arrayOf("id", "nombre", "celular", "direccion"),
            "id = ?",
            arrayOf(id.toString()),
            null, null, null
        )
        var cliente: ClienteM? = null
        if(cursor.moveToFirst()) {
            cliente = toCM(cursor)
        }
        cursor.close()
        return cliente
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
            val values = toCV(nombre, celular, direccion)
            val filasAfectadas = db.update("cliente", values, "id = ?", arrayOf(id.toString()))
            if (filasAfectadas <= 0) {
                throw Exception("Error al actualizar el producto")
            }
        } catch (e: NumberFormatException) {
            throw Exception("Celular deben ser un número válido")
        }
    }

    fun eliminar(id: Int) {
        val db = dbHelper?.writableDatabase ?: throw IllegalStateException("dbHelper is null")
        val filasAfectadas = db.delete("cliente", "id = ?", arrayOf(id.toString()))
        if (filasAfectadas <= 0) {
            throw Exception("Error al eliminar el producto")
        }
    }

    private fun toCV(nombre: String, celular: Int, direccion: String): ContentValues {
        return ContentValues().apply {
            put("nombre", nombre)
            put("celular", celular)
            put("direccion", direccion)
        }
    }

    private fun toCM(cursor: Cursor): ClienteM {
        with(cursor) {
            return ClienteM(
                id = getInt(getColumnIndexOrThrow("id")),
                nombre = getString(getColumnIndexOrThrow("nombre")),
                celular = getInt(getColumnIndexOrThrow("celular")),
                direccion = getString(getColumnIndexOrThrow("direccion")),
            )
        }
    }
}