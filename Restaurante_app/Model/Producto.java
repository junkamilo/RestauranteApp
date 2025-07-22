package Restaurante_app.Model;

import java.math.BigDecimal;

public class Producto {
    private int id;
    private String nombre;
    private BigDecimal precio;
    private String descripcion;
    private String categoria;
    private String imagen; // Nombre del archivo de imagen
    private boolean disponibilidad; // Si está disponible para la venta
    private boolean en_menu_dia; // Si está en el menú del día (para la tabla de Gestión_menu)

    // Constructor por defecto
    public Producto() {
    }

    // Constructor COMPLETO (8 argumentos) - Ideal para cargar desde la BD con todos los campos
    public Producto(int id, String nombre, BigDecimal precio, String descripcion, String categoria, String imagen, boolean disponibilidad, boolean en_menu_dia) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.imagen = imagen;
        this.disponibilidad = disponibilidad;
        this.en_menu_dia = en_menu_dia;
    }

    // Constructor para INSERTAR nuevo producto (sin ID, con disponibilidad y en_menu_dia por defecto)
    public Producto(String nombre, BigDecimal precio, String descripcion, String categoria, String imagen) {
        this.nombre = nombre;
        this.precio = precio;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.imagen = imagen;
        this.disponibilidad = true; // Por defecto, nuevo producto disponible
        this.en_menu_dia = false; // Por defecto, nuevo producto no está en el menú del día
    }

    // Constructor simplificado para algunos listados (si solo se necesitan estos campos, ajusta el DAO para usarlo)
    // Actualmente, no se usa en los DAOs principales que cargan todo, pero podría ser útil en otros contextos.
    public Producto(int id, String nombre, BigDecimal precio, String categoria, String imagen) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.categoria = categoria;
        this.imagen = imagen;
        this.descripcion = null; // No inicializado
        this.disponibilidad = true; // Por defecto
        this.en_menu_dia = false; // Por defecto
    }

    // --- GETTERS ---
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public BigDecimal getPrecio() { return precio; }
    public String getDescripcion() { return descripcion; }
    public String getCategoria() { return categoria; }
    public String getImagen() { return imagen; }
    public boolean isDisponibilidad() { return disponibilidad; }
    public boolean isEnMenuDia() { return en_menu_dia; } // ¡Este es el método que faltaba!

    // --- SETTERS ---
    public void setId(int id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public void setImagen(String imagen) { this.imagen = imagen; }
    public void setDisponibilidad(boolean disponibilidad) { this.disponibilidad = disponibilidad; }
    public void setEnMenuDia(boolean en_menu_dia) { this.en_menu_dia = en_menu_dia; }

    @Override
    public String toString() {
        return "Producto{" +
               "id=" + id +
               ", nombre='" + nombre + '\'' +
               ", precio=" + precio +
               ", descripcion='" + descripcion + '\'' +
               ", categoria='" + categoria + '\'' +
               ", imagen='" + imagen + '\'' +
               ", disponibilidad=" + disponibilidad +
               ", en_menu_dia=" + en_menu_dia +
               '}';
    }
}