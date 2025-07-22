package Restaurante_app.Model;

public class Venta {
    private String categoria;
    private String nombrePlato;
    private int cantidad;
    private int precioTotal; // En centavos o unidad monetaria entera para evitar floats

    // Constructor completo
    public Venta(String categoria, String nombrePlato, int cantidad, int precioTotal) {
        this.categoria = categoria;
        this.nombrePlato = nombrePlato;
        this.cantidad = cantidad;
        this.precioTotal = precioTotal;
    }

    // Getters y setters
    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getNombrePlato() {
        return nombrePlato;
    }

    public void setNombrePlato(String nombrePlato) {
        this.nombrePlato = nombrePlato;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public int getPrecioTotal() {
        return precioTotal;
    }

    public void setPrecioTotal(int precioTotal) {
        this.precioTotal = precioTotal;
    }
}
