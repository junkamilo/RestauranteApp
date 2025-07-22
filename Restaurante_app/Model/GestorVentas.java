package Restaurante_app.Model;

import Restaurante_app.Model.Venta;
import java.util.ArrayList;
import java.util.List;

public class GestorVentas {
    private List<Venta> ventas;

    public GestorVentas() {
        this.ventas = new ArrayList<>();
    }

    public void registrarVenta(Venta venta) {
    	// Si la venta del mismo plato ya existe, sumar cantidades y precio
        for (Venta v : ventas) {
            if (v.getNombrePlato().equals(venta.getNombrePlato())) {
                v.setCantidad(v.getCantidad() + venta.getCantidad());
                v.setPrecioTotal(v.getPrecioTotal() + venta.getPrecioTotal());
                return;
            }
        }
        // Si no existe, añadir nueva
        ventas.add(venta);
    }

    public int calcularTotalPlatos() {
    	int total = 0;
        for (Venta v : ventas) {
            total += v.getCantidad();
        }
        return total;
    }

    public int calcularMontoTotal() {
    	int total = 0;
        for (Venta v : ventas) {
            total += v.getPrecioTotal();
        }
        return total;
    }

    public List<Venta> obtenerVentas() {
        return ventas;
    }

    public void limpiarVentas() {
        ventas.clear();
    }
}

