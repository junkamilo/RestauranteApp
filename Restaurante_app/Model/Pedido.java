package Restaurante_app.Model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal; 
import Restaurante_app.DAO.DetallePedidoDAO; 
import java.sql.SQLException; 

public class Pedido {
    private int id_pedido;
    private int id_usuario; 
    private int id_mesa;
    private String nombre_mesa;
    private LocalDateTime fecha_hora;
    private String estado;
    private String observaciones;
    private List<DetallePedidoConNombre> detalles; 
    private BigDecimal total_pedido; 
    private boolean sopasEntregadasAuxiliar;
    // Campo 'pagado' ELIMINADO
    

    // --- CONSTRUCTORES ---

    public Pedido() {
        this.detalles = new ArrayList<>();
        this.total_pedido = BigDecimal.ZERO; 
        this.sopasEntregadasAuxiliar = false; 
    }

    // Constructor completo
    public Pedido(int id_pedido, int id_usuario, int id_mesa, String nombre_mesa, LocalDateTime fecha_hora, String estado, String observaciones, BigDecimal total_pedido) {
        this.id_pedido = id_pedido;
        this.id_usuario = id_usuario;
        this.id_mesa = id_mesa;
        this.nombre_mesa = nombre_mesa;
        this.fecha_hora = fecha_hora;
        this.estado = estado;
        this.observaciones = observaciones;
        this.detalles = new ArrayList<>(); 
        this.total_pedido = total_pedido; 
        this.sopasEntregadasAuxiliar = false; 
    }

    // Constructor utilizado por PedidoDAO para listar/obtener pedidos (con sopasEntregadasAuxiliar, SIN pagado)
    public Pedido(int idPedido, int idUsuario, int idMesa, String nombreMesa, LocalDateTime fechaHora, BigDecimal totalPedido, String estado, String observaciones, boolean sopasEntregadasAuxiliar) { 
        this.id_pedido = idPedido;
        this.id_usuario = idUsuario; 
        this.id_mesa = idMesa;
        this.nombre_mesa = nombreMesa;
        this.fecha_hora = fechaHora;
        this.total_pedido = totalPedido;
        this.estado = estado;
        this.observaciones = observaciones; 
        this.detalles = new ArrayList<>(); 
        this.sopasEntregadasAuxiliar = sopasEntregadasAuxiliar; 
    }

    // Constructor para insertar un nuevo pedido
    public Pedido(int id_usuario, int id_mesa, String estado, String observaciones) {
        this.id_usuario = id_usuario;
        this.id_mesa = id_mesa;
        this.estado = estado;
        this.observaciones = observaciones;
        this.fecha_hora = LocalDateTime.now();
        this.id_pedido = 0; 
        this.nombre_mesa = null; 
        this.detalles = new ArrayList<>();
        this.total_pedido = BigDecimal.ZERO; 
        this.sopasEntregadasAuxiliar = false; 
    }


    // --- GETTERS ---
    public int getIdPedido() { return id_pedido; }
    public int getIdUsuario() { return id_usuario; }
    public int getIdMesa() { return id_mesa; }
    public String getNombreMesa() {
        return (nombre_mesa != null && !nombre_mesa.isEmpty()) ? nombre_mesa : "Mesa " + id_mesa;
    }
    public LocalDateTime getFechaHora() { return fecha_hora; }
    public String getEstado() { return estado; }
    public String getObservaciones() { return observaciones; }
    public List<DetallePedidoConNombre> getDetalles() { return detalles; } 
    public BigDecimal getTotalPedido() { return total_pedido; }
    public boolean isSopasEntregadasAuxiliar() {
        return sopasEntregadasAuxiliar;
    }


    // --- SETTERS ---
    public void setIdPedido(int id_pedido) { this.id_pedido = id_pedido; }
    public void setIdUsuario(int id_usuario) { this.id_usuario = id_usuario; }
    public void setIdMesa(int id_mesa) { this.id_mesa = id_mesa; }
    public void setNombreMesa(String nombreMesa) { this.nombre_mesa = nombreMesa; }
    public void setFechaHora(LocalDateTime fecha_hora) { this.fecha_hora = fecha_hora; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public void setDetalles(List<DetallePedidoConNombre> detalles) { this.detalles = detalles; } 
    public void addDetalle(DetallePedidoConNombre detalle) { this.detalles.add(detalle); } 
    public void setTotalPedido(BigDecimal total_pedido) { this.total_pedido = total_pedido; } 
    public void setSopasEntregadasAuxiliar(boolean sopasEntregadasAuxiliar) {
        this.sopasEntregadasAuxiliar = sopasEntregadasAuxiliar;
    }

    /**
     * Verifica si el pedido contiene alguna sopa.
     * @return true si el pedido tiene al menos una sopa, false en caso contrario.
     */
    public boolean hasSoups() {
        try {
            DetallePedidoDAO detalleDao = new DetallePedidoDAO();
            List<DetallePedidoConNombre> detalles = detalleDao.obtenerDetallesConNombrePorPedido(this.id_pedido);
            return detalles.stream().anyMatch(DetallePedidoConNombre::isSopaIncluida);
        } catch (SQLException e) {
            System.err.println("ERROR (Pedido.hasSoups): Error al verificar sopas para pedido " + this.id_pedido + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String toString() {
        return "Pedido{" +
               "id_pedido=" + id_pedido +
               ", id_usuario=" + id_usuario +
               ", id_mesa=" + id_mesa +
               ", nombreMesa='" + nombre_mesa + '\'' +
               ", fecha_hora=" + fecha_hora +
               ", estado='" + estado + '\'' +
               ", observaciones='" + observaciones + '\'' +
               ", total_pedido=" + total_pedido + 
               ", sopasEntregadasAuxiliar=" + sopasEntregadasAuxiliar + 
               ", detalles=" + (detalles != null ? detalles.size() : 0) + " items" + 
               '}';
    }
}