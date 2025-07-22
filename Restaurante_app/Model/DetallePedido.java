package Restaurante_app.Model;

/**
 * Clase que representa el detalle de un pedido en el restaurante.
 * Contiene información sobre el producto pedido, cantidad, especificaciones,
 * estado en cocina y servicio, además de atributos adicionales para UI o lógica.
 */
public class DetallePedido {
    // Campos principales que representan el detalle del pedido
    private int id_detalle;        // Identificador único del detalle
    private int id_pedido;         // Identificador del pedido al que pertenece
    private int id_producto;       // Identificador del producto pedido
    private int cantidad;          // Cantidad del producto solicitado
    private boolean sopa_incluida; // Indica si la sopa está incluida en el pedido
    private String tamano_sopa;    // Tamaño de la sopa (completa, mediana, pequeña)
    private String especificaciones; // Detalles u observaciones especiales del pedido

    // Nuevos atributos para mostrar o manejar estados y datos extras
    private String nombreProducto; // Nombre del producto (para mostrar en UI)
    private String imagen;         // Ruta o nombre de la imagen del producto
    private String estadoCocina;   // Estado del pedido en cocina (Pendiente, Listo, etc.)
    private String estadoServicio; // Estado del pedido en servicio (Entregado, etc.)

    /**
     * Constructor con todos los campos originales (sin campos nuevos)
     * Útil para instanciar con datos base del detalle pedido.
     */
    public DetallePedido(int id_detalle, int id_pedido, int id_producto, int cantidad,
                         boolean sopa_incluida, String tamano_sopa, String especificaciones) {
        this.id_detalle = id_detalle;
        this.id_pedido = id_pedido;
        this.id_producto = id_producto;
        this.cantidad = cantidad;
        this.sopa_incluida = sopa_incluida;
        this.tamano_sopa = tamano_sopa;
        this.especificaciones = especificaciones;
    }

    /**
     * Constructor vacío requerido para crear objetos sin inicializar
     * y usar luego setters para asignar valores.
     */
    public DetallePedido() {
    }

    // ----------- GETTERS -----------
    //obtener,devuelven el valor de un atributo

    public int getIdDetalle() {
        return id_detalle;
    }

    public int getIdPedido() {
        return id_pedido;
    }

    public int getProducto() {
        return id_producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public boolean isSopaIncluida() {
        return sopa_incluida;
    }

    public String getTamanoSopa() {
        return tamano_sopa;
    }

    public String getEspecificaciones() {
        return especificaciones;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public String getImagen() {
        return imagen;
    }

    public String getEstadoCocina() {
        return estadoCocina;
    }

    public String getEstadoServicio() {
        return estadoServicio;
    }

    // ----------- SETTERS -----------
    // establecer,permiten modificar ese valor

    public void setIdDetalle(int id_detalle) {
        this.id_detalle = id_detalle;
    }

    public void setIdPedido(int id_pedido) {
        this.id_pedido = id_pedido;
    }

    public void setIdProducto(int id_producto) {
        this.id_producto = id_producto;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public void setSopaIncluida(boolean sopa_incluida) {
        this.sopa_incluida = sopa_incluida;
    }

    public void setTamanoSopa(String tamano_sopa) {
        this.tamano_sopa = tamano_sopa;
    }

    public void setEspecificaciones(String especificaciones) {
        this.especificaciones = especificaciones;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public void setEstadoCocina(String estadoCocina) {
        this.estadoCocina = estadoCocina;
    }

    public void setEstadoServicio(String estadoServicio) {
        this.estadoServicio = estadoServicio;
    }
}

