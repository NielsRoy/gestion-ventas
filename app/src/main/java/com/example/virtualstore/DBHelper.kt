package com.example.virtualstore

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "VirtualStore.db"
    }
    private val SQL_TABLE_CATEGORIA = """
        CREATE TABLE categoria (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            nombre TEXT NOT NULL,
            descripcion TEXT DEFAULT NULL,
            categoria_id INTEGER DEFAULT NULL,
            FOREIGN KEY (categoria_id) REFERENCES categoria(id)
        )
    """
    private val SQL_TABLE_PRODUCTO = """
        CREATE TABLE producto (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            nombre TEXT NOT NULL,
            precio REAL NOT NULL,
            cantidad INTEGER NOT NULL,
            categoria_id INTEGER NOT NULL,
            FOREIGN KEY (categoria_id) REFERENCES categoria(id) ON DELETE CASCADE
        )
    """
    private val SQL_TABLE_CLIENTE = """
        CREATE TABLE cliente (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            nombre TEXT NOT NULL,
            celular INTEGER NOT NULL,
            direccion TEXT DEFAULT NULL
        )
    """
    private val SQL_TABLE_REPARTIDOR = """
        CREATE TABLE repartidor (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            nombre TEXT NOT NULL,
            celular INTEGER NOT NULL,
            direccion TEXT DEFAULT NULL
        )
    """
    private val SQL_TABLE_VENTA = """
        CREATE TABLE venta (
            nro INTEGER PRIMARY KEY AUTOINCREMENT,
            fecha DATE NOT NULL DEFAULT CURRENT_DATE,
            hora TIME NOT NULL DEFAULT CURRENT_TIME,
            total REAL NOT NULL,
            cliente_id INTEGER NOT NULL,
            repartidor_id INTEGER NOT NULL,
            FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE,
            FOREIGN KEY (repartidor_id) REFERENCES repartidor(id) ON DELETE CASCADE
        )
    """
    private val SQL_TABLE_DETALLE_VENTA = """
        CREATE TABLE detalle_venta (
            venta_nro INTEGER NOT NULL,
            producto_id INTEGER NOT NULL,
            cantidad INTEGER NOT NULL,
            precio REAL NOT NULL,
            PRIMARY KEY (venta_nro, producto_id),
            FOREIGN KEY (venta_nro) REFERENCES venta(nro) ON DELETE CASCADE,
            FOREIGN KEY (producto_id) REFERENCES producto(id) ON DELETE CASCADE
        )
    """

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_TABLE_CATEGORIA)
        db.execSQL(SQL_TABLE_PRODUCTO)
        db.execSQL(SQL_TABLE_CLIENTE)
        db.execSQL(SQL_TABLE_REPARTIDOR)
        db.execSQL(SQL_TABLE_VENTA)
        db.execSQL(SQL_TABLE_DETALLE_VENTA)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS detalle_venta")
        db.execSQL("DROP TABLE IF EXISTS venta")
        db.execSQL("DROP TABLE IF EXISTS cliente")
        db.execSQL("DROP TABLE IF EXISTS repartidor")
        db.execSQL("DROP TABLE IF EXISTS producto")
        db.execSQL("DROP TABLE IF EXISTS categoria")
        onCreate(db)
    }
}