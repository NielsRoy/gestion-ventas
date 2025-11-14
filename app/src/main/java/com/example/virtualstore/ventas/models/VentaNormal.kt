package com.example.virtualstore.ventas.models

class VentaNormal: VentaStrategy {

    override fun calcularTotal(detalles: List<DetalleVM>): Double {
        return detalles.sumOf { it.calcularSubtotal() }
    }

    override fun getNombre(): String = "Venta Normal"
}