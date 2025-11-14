package com.example.virtualstore.ventas.models

class VentaDescuento: VentaStrategy {

    private val descuento: Double

    constructor(p: Double) {
        descuento = p
    }

    override fun calcularTotal(detalles: List<DetalleVM>): Double {
        val subtotal = detalles.sumOf { it.calcularSubtotal() }
        if (descuento <= 0 || descuento > 100) {
            return subtotal
        }
        val descuento = subtotal * (descuento / 100.0)
        return subtotal - descuento
    }

    override fun getNombre(): String = "Venta con ${descuento}% Descuento"
}
