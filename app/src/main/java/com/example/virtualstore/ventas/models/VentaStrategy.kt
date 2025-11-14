package com.example.virtualstore.ventas.models

interface VentaStrategy {

    fun calcularTotal(detalles: List<DetalleVM>): Double

    fun getNombre(): String
}