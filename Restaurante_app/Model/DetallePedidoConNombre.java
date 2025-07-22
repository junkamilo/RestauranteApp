package Restaurante_app.Model;

import java.math.BigDecimal;

public class DetallePedidoConNombre {
    private int idDetalle;
    private int idPedido; 
    private int idProducto;
    private String nombreProducto;
    private int cantidad;
    private boolean sopaIncluida;
    private String tamanoSopa;
    private String especificaciones;
    private String imagen; 
    private BigDecimal precioUnitario; 
    private String estadoCocina; 
    private String estadoServicio; 

    // Constructor que ya tenías, podría ser usado para crear nuevos detalles
    public DetallePedidoConNombre(String nombreProducto, int cantidad, boolean sopaIncluida, String tamanoSopa, String especificaciones) {
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.sopaIncluida = sopaIncluida;
        this.tamanoSopa = tamanoSopa;
        this.especificaciones = especificaciones;
        this.idProducto = 0; 
    }

    // Constructor completo (ajustado para incluir idProducto)
    public DetallePedidoConNombre(int idDetalle, int idPedido, int idProducto, String nombreProducto, int cantidad, boolean sopaIncluida, String tamanoSopa, String especificaciones, String imagen, BigDecimal precioUnitario, String estadoCocina, String estadoServicio) {
        this.idDetalle = idDetalle;
        this.idPedido = idPedido;
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.sopaIncluida = sopaIncluida;
        this.tamanoSopa = tamanoSopa;
        this.especificaciones = especificaciones;
        this.imagen = imagen;
        this.precioUnitario = precioUnitario;
        this.estadoCocina = estadoCocina;
        this.estadoServicio = estadoServicio;
    }

    // --- Getters ---
    public int getIdDetalle() {
        return idDetalle;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public int getIdProducto() { 
        return idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public boolean isSopaIncluida() {
        return sopaIncluida;
    }

    public String getTamanoSopa() {
        return tamanoSopa;
    }

    public String getEspecificaciones() {
        return especificaciones;
    }

    public String getImagen() {
        return imagen;
    }

    public BigDecimal getPrecioUnitario() { 
        return precioUnitario;
    }

    public String getEstadoCocina() {
        return estadoCocina;
    }

    public String getEstadoServicio() {
        return estadoServicio;
    }

    // --- Setters ---
    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public void setIdProducto(int idProducto) { // ¡NUEVO SETTER!
        this.idProducto = idProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public void setSopaIncluida(boolean sopaIncluida) {
        this.sopaIncluida = sopaIncluida;
    }

    public void setTamanoSopa(String tamanoSopa) {
        this.tamanoSopa = tamanoSopa;
    }

    public void setEspecificaciones(String especificaciones) {
        this.especificaciones = especificaciones;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) { 
        this.precioUnitario = precioUnitario;
    }

    public void setEstadoCocina(String estadoCocina) {
        this.estadoCocina = estadoCocina;
    }

    public void setEstadoServicio(String estadoServicio) {
        this.estadoServicio = estadoServicio;
    }
}
