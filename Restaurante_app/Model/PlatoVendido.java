package Restaurante_app.Model;

import java.math.BigDecimal;

public class PlatoVendido {
    private String categoria;
    private String nombre;
    private int cantidad;
    private BigDecimal total;

    // ✅ Constructor vacío
    public PlatoVendido() {
    }

    // Constructor completo
    public PlatoVendido(String categoria, String nombre, int cantidad, BigDecimal total) {
        this.categoria = categoria;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.total = total;
    }

    // Getters
    public String getCategoria() {
        return categoria;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getTotal() {
        return total;
    }

    // Setters
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
    
    public String toString() {
        return "PlatoVendido{" +
               "nombre='" + nombre + '\'' +
               ", categoria='" + categoria + '\'' +
               ", cantidad=" + cantidad +
               ", total=" + total +
               '}';
    }
}
