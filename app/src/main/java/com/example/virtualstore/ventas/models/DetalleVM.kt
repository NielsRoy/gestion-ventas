package com.example.virtualstore.ventas.models

import android.content.ContentValues
import android.database.Cursor
import com.example.virtualstore.inventario.models.ProductoM
import com.example.virtualstore.DBHelper
import kotlin.collections.iterator
import kotlin.collections.set

data class DetalleVM(
    var venta: VentaM? = null,
    var producto: ProductoM? = null,
    var cantidad: Int? = null,
    var precio: Double? = null,
    val dbHelper: DBHelper? = null
) {
    fun calcularSubtotal(): Double {
        return (precio ?: 0.0) * (cantidad ?: 0)
    }

    fun crear(ventaNro: Long, detalles: List<DetalleVM>) { //db: SQLiteDatabase
        val db = dbHelper?.writableDatabase ?: throw IllegalStateException("dbHelper is null")
        for (detalle in detalles) {
            val values = toCV(ventaNro, detalle)
            val result = db.insert("detalle_venta", null, values)
            if (result == -1L) {
                throw Exception("Error al crear el detalle de venta")
            }
            val updateQuery = "UPDATE producto SET cantidad = cantidad - ? WHERE id = ?"
            db.execSQL(
                updateQuery,
                arrayOf(detalle.cantidad.toString(), detalle.producto?.id.toString())
            )
        }
    }

    fun getByNroVenta(ventaNro: Int): List<DetalleVM> {
        val detallesList = mutableListOf<DetalleVM>()
        val db = dbHelper?.readableDatabase ?: throw IllegalStateException("dbHelper is null")
        val cursor = db.query(
            "detalle_venta",
            arrayOf("venta_nro","producto_id", "cantidad", "precio"),
            "venta_nro = ?",
            arrayOf(ventaNro.toString()),
            null, null, null
        )
        while (cursor.moveToNext()) {
            val detalle = toDVM(cursor)
            detallesList.add(detalle)
        }
        cursor.close()
        return detallesList
    }

    fun actualizar(ventaNro: Long, detalles: List<DetalleVM>) { //, db: SQLiteDatabase
        val db = dbHelper?.writableDatabase ?: throw IllegalStateException("dbHelper is null")
        val detallesAntiguos = getByNroVenta(ventaNro.toInt())
        for (d in detallesAntiguos) {
            val restoreQuery = "UPDATE producto SET cantidad = cantidad + ? WHERE id = ?"
            db.execSQL(restoreQuery, arrayOf(d.cantidad.toString(), d.producto?.id.toString()))
        }
        eliminarPorNroVenta(ventaNro.toInt())
        crear(ventaNro, detalles)
    }

    fun eliminarPorNroVenta(ventaNro: Int) { //, db: SQLiteDatabase? = null
        val db = dbHelper?.writableDatabase ?: throw IllegalStateException("dbHelper is null")
        val filasAfectadas = db.delete("detalle_venta", "venta_nro = ?", arrayOf(ventaNro.toString()))
        if (filasAfectadas == 0) {
            throw Exception("Se eliminaron $filasAfectadas registros")
        }
    }

    fun validarStock(ventaNro: Int? = null, detalles: List<DetalleVM>): Boolean {
        if (ventaNro != null) {
            val diferencias = mutableMapOf<Int, Int>()

            for (detalle in detalles) {
                val productoId = detalle.producto?.id ?: continue
                val cantidadNueva = detalle.cantidad ?: 0
                diferencias[productoId] = cantidadNueva
            }

            val detallesActuales = getByNroVenta(ventaNro)
            for (detalle in detallesActuales) {
                val productoId = detalle.producto?.id ?: continue
                val cantidadActual = detalle.cantidad ?: 0
                diferencias[productoId] = (diferencias[productoId] ?: 0) - cantidadActual
            }
            for ((productoId, diferencia) in diferencias) {
                if (diferencia > 0) {
                    val producto = ProductoM(dbHelper = dbHelper).obtenerPorId(productoId)
                    if (diferencia > (producto?.cantidad ?: 0)) {
                        return false
                    }
                }
            }
            return true
        }
        for (detalle in detalles) {
            val productoId = detalle.producto?.id ?: continue
            val producto = ProductoM(dbHelper = dbHelper).obtenerPorId(productoId)
            if ((detalle.cantidad ?: 0) > (producto?.cantidad ?: 0)) {
                return false
            }
        }
        return true
    }

    private fun toCV(ventaNro: Long, detalle: DetalleVM): ContentValues {
        return ContentValues().apply {
            put("venta_nro", ventaNro)
            put("producto_id", detalle.producto?.id)
            put("cantidad", detalle.cantidad)
            put("precio", detalle.precio)
        }
    }

    private fun toDVM(cursor: Cursor): DetalleVM {
        with(cursor) {
            val productoId = getInt(getColumnIndexOrThrow("producto_id"))
            return DetalleVM(
                producto = ProductoM(dbHelper = dbHelper).obtenerPorId(productoId),
                cantidad = getInt(cursor.getColumnIndexOrThrow("cantidad")),
                precio = getDouble(cursor.getColumnIndexOrThrow("precio"))
            )
        }
    }
}