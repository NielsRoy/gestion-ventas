package com.example.virtualstore.ventas.models

import android.content.ContentValues
import android.database.Cursor
import com.example.virtualstore.inventario.models.ProductoM
import com.example.virtualstore.DBHelper

data class VentaM(
    var nro: Int? = null,
    var fecha: String? = null,
    var hora: String? = null,
    var total: Double? = null,
    var cliente: ClienteM? = null,
    var repartidor: RepartidorM? = null,
    var detalles: MutableList<DetalleVentaM> = mutableListOf(),
    val dbHelper: DBHelper? = null
) {
    fun crear(
        fecha: String,
        hora: String,
        clienteId: Int,
        repartidorId: Int,
        detalles: List<DetalleVentaM>
    ) {
        if (detalles.isEmpty()) {
            throw Exception("La venta debe tener al menos un producto")
        }
        if (!DetalleVentaM(dbHelper = dbHelper).validarStock(detalles = detalles)) {
            throw Exception("Stock insuficiente para uno o más productos")
        }
        val db = dbHelper?.writableDatabase ?: throw IllegalStateException("dbHelper is null")
        try {
            db.beginTransaction()
            val total = detalles.sumOf { it.calcularSubtotal() }
            val values = toContentValues(fecha, hora, total, clienteId, repartidorId)
            val ventaNro = db.insert("venta", null, values)
            if (ventaNro == -1L) {
                throw Exception("Error al crear la venta")
            }
            DetalleVentaM(dbHelper = dbHelper).crear(ventaNro, detalles)
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            throw e
        } finally {
            db.endTransaction()
        }
    }

    fun obtenerTodos(): List<VentaM> {
        val ventasList = mutableListOf<VentaM>()
        val db = dbHelper?.readableDatabase ?: throw IllegalStateException("dbHelper is null")
        val cursor = db.query(
            "venta",
            arrayOf("nro", "fecha", "hora", "total", "cliente_id", "repartidor_id"),
            null, null, null, null, "nro DESC"
        )
        while (cursor.moveToNext()) {
            val venta = toVentaModel(cursor)
            ventasList.add(venta)
        }
        cursor.close()
        return ventasList
    }

    fun actualizar(
        ventaNro: Int,
        fecha: String,
        hora: String,
        clienteId: Int,
        repartidorId: Int,
        detalles: List<DetalleVentaM>
    ) {
        if (detalles.isEmpty()) {
            throw Exception("La venta debe tener al menos un producto")
        }
        if (!DetalleVentaM(dbHelper = dbHelper).validarStock(ventaNro, detalles)) {
            throw Exception("Stock insuficiente para uno o más productos")
        }
        val db = dbHelper?.writableDatabase ?: throw IllegalStateException("dbHelper is null")
        try {
            db.beginTransaction()
            val total = detalles.sumOf { it.calcularSubtotal() }
            val values = toContentValues(fecha, hora, total, clienteId, repartidorId)
            val rowsAffected = db.update("venta", values, "nro = ?", arrayOf(ventaNro.toString()))
            if (rowsAffected == 0) {
                throw Exception("Error al actualizar la venta")
            }
            DetalleVentaM(dbHelper = dbHelper).actualizar(ventaNro.toLong(), detalles)
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            throw e
        } finally {
            db.endTransaction()
        }
    }

    fun eliminar(ventaNro: Int) {
        val db = dbHelper?.writableDatabase ?: throw IllegalStateException("dbHelper is null")
        val filasAfectadas = db.delete("venta", "nro = ?", arrayOf(ventaNro.toString()))
        if (filasAfectadas <= 0) {
            throw Exception("Error al eliminar la venta")
        }
    }

    private fun toVentaModel(cursor: Cursor): VentaM {
        with(cursor) {
            val clienteId = getInt(getColumnIndexOrThrow("cliente_id"))
            val repartidorId = getInt(getColumnIndexOrThrow("repartidor_id"))
            val nro = getInt(getColumnIndexOrThrow("nro"))
            val detalles = DetalleVentaM(dbHelper = dbHelper).obtenerPorNroVenta(nro) as MutableList<DetalleVentaM>
            return VentaM(
                nro = nro,
                fecha = getString(getColumnIndexOrThrow("fecha")),
                hora = getString(getColumnIndexOrThrow("hora")),
                total = getDouble(getColumnIndexOrThrow("total")),
                cliente = ClienteM(dbHelper = dbHelper).obtenerPorId(clienteId),
                repartidor = RepartidorM(dbHelper = dbHelper).obtenerPorId(repartidorId),
                detalles = detalles
            )
        }
    }

    private fun toContentValues(fecha: String, hora: String, total: Double, clienteId: Int, repartidorId: Int): ContentValues {
        return ContentValues().apply {
            put("fecha", fecha)
            put("hora", hora)
            put("total", total)
            put("cliente_id", clienteId)
            put("repartidor_id", repartidorId)
        }
    }

    fun obtenerTodosLosClientes(): List<ClienteM> {
        return ClienteM(dbHelper = dbHelper).obtenerTodos()
    }
    fun obtenerTodosLosRepartidores(): List<RepartidorM> {
        return RepartidorM(dbHelper = dbHelper).obtenerTodos()
    }
    fun obtenerTodosLosProductos(): List<ProductoM> {
        return ProductoM(dbHelper = dbHelper).obtenerTodos()
    }
}