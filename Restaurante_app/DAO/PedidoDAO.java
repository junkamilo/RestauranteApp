package Restaurante_app.DAO;

import Restaurante_app.BaseDatos.Conexion_BaseDatos;
import Restaurante_app.Model.Pedido;
import Restaurante_app.Model.DetallePedidoConNombre; 

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal; 

public class PedidoDAO {

    // ELIMINADO: private final DetallePedidoDAO detallePedidoDao = new DetallePedidoDAO();
    // Ahora se instanciará localmente en los métodos que lo necesiten.

    public int guardarPedido(Pedido pedido) throws SQLException {
        // REVISADA LA SENTENCIA SQL: debe tener 7 placeholders (?) para los 7 ps.set...
        // Columnas: id_usuario, id_mesa, estado, observaciones, fecha_hora, total_pedido, sopas_entregadas_auxiliar
        String sql = "INSERT INTO pedidos (id_usuario, id_mesa, estado, observaciones, fecha_hora, total_pedido, sopas_entregadas_auxiliar) VALUES (?, ?, ?, ?, ?, ?, ?)";
        int idGenerado = -1;

        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, pedido.getIdUsuario()); // 1
            ps.setInt(2, pedido.getIdMesa());    // 2
            ps.setString(3, pedido.getEstado()); // 3
            ps.setString(4, pedido.getObservaciones()); // 4
            ps.setTimestamp(5, Timestamp.valueOf(pedido.getFechaHora())); // 5
            ps.setBigDecimal(6, pedido.getTotalPedido()); // 6
            ps.setBoolean(7, pedido.isSopasEntregadasAuxiliar()); // 7

            // ps.setBoolean(8, pedido.isPagado()); // ESTO ERA EL PARÁMETRO 8 SI ESTUVIERA ACTIVO. LO ELIMINAMOS.

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    idGenerado = rs.getInt(1);
                    pedido.setIdPedido(idGenerado);
                    System.out.println("DEBUG (PedidoDAO - guardarPedido): Nuevo pedido insertado con ID: " + idGenerado + ", SopasEntregadasAuxiliar: " + pedido.isSopasEntregadasAuxiliar());
                }
            }
        }
        return idGenerado;
    }
    
    public List<Pedido> listarTodosActivos() throws SQLException {
        List<Pedido> pedidos = new ArrayList<>();
        // Asegúrate de que todas las columnas seleccionadas existan en tu tabla 'pedidos'.
        // Si 'id_usuario' en Pedido.java mapea a 'id_mesero' en tu tabla, ajústalo en la SELECT.
        String sql = "SELECT p.id_pedido, p.id_usuario, p.id_mesa, m.nombre_mesa AS nombre_mesa, p.fecha_hora, p.estado, p.observaciones, p.total_pedido, p.sopas_entregadas_auxiliar " + 
                     "FROM pedidos p JOIN mesas m ON p.id_mesa = m.id_mesa WHERE p.estado NOT IN ('Cancelado', 'Entregado') ORDER BY p.fecha_hora DESC";
        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Pedido pedido = new Pedido();
                pedido.setIdPedido(rs.getInt("id_pedido"));
                pedido.setIdMesa(rs.getInt("id_mesa"));
                pedido.setNombreMesa(rs.getString("nombre_mesa"));
                pedido.setFechaHora(rs.getTimestamp("fecha_hora").toLocalDateTime()); 
                pedido.setEstado(rs.getString("estado"));
                pedido.setIdUsuario(rs.getInt("id_usuario")); // Asumiendo que 'id_usuario' de ResultSet es el 'id_mesero' de la tabla 'pedidos'
                pedido.setObservaciones(rs.getString("observaciones")); 
                pedido.setTotalPedido(rs.getBigDecimal("total_pedido"));
                pedido.setSopasEntregadasAuxiliar(rs.getBoolean("sopas_entregadas_auxiliar")); 
                pedidos.add(pedido);
                System.out.println("DEBUG (PedidoDAO - listarTodosActivos): Pedido ID " + pedido.getIdPedido() + ", Estado: '" + pedido.getEstado() + "', SopasEntregadasAuxiliar: " + pedido.isSopasEntregadasAuxiliar());
            }
        }
        return pedidos;
    }
    
    public boolean actualizarSopasEntregadasAuxiliar(int idPedido, boolean entregadas) throws SQLException {
        String sql = "UPDATE pedidos SET sopas_entregadas_auxiliar = ? WHERE id_pedido = ?";
        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, entregadas);
            ps.setInt(2, idPedido);
            int rowsAffected = ps.executeUpdate();
            System.out.println("DEBUG (PedidoDAO): sopas_entregadas_auxiliar para Pedido ID " + idPedido + " actualizado a " + entregadas + ". Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        }
    }

    public List<Pedido> listarTodos() throws SQLException {
        // Asegúrate de que todas las columnas seleccionadas existan en tu tabla 'pedidos'.
        String sql = "SELECT p.id_pedido, p.id_usuario, p.id_mesa, m.nombre_mesa AS nombre_mesa, " + 
                     "       p.fecha_hora, p.estado, p.observaciones, p.total_pedido, p.sopas_entregadas_auxiliar " + 
                     "FROM pedidos p " +
                     "JOIN mesas m ON p.id_mesa = m.id_mesa " +
                     "ORDER BY p.fecha_hora ASC";
        List<Pedido> lista = new ArrayList<>();

        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Pedido pedido = new Pedido(); 
                pedido.setIdPedido(rs.getInt("id_pedido"));
                pedido.setIdUsuario(rs.getInt("id_usuario")); 
                pedido.setIdMesa(rs.getInt("id_mesa"));
                pedido.setNombreMesa(rs.getString("nombre_mesa"));
                pedido.setFechaHora(rs.getTimestamp("fecha_hora").toLocalDateTime()); 
                pedido.setEstado(rs.getString("estado"));
                pedido.setObservaciones(rs.getString("observaciones")); 
                pedido.setTotalPedido(rs.getBigDecimal("total_pedido"));
                pedido.setSopasEntregadasAuxiliar(rs.getBoolean("sopas_entregadas_auxiliar")); 
                lista.add(pedido);
                System.out.println("DEBUG (PedidoDAO - listarTodos): Pedido ID " + pedido.getIdPedido() + ", Estado: '" + pedido.getEstado() + "', SopasEntregadasAuxiliar: " + pedido.isSopasEntregadasAuxiliar());
            }
        }
        return lista;
    }
    
    /**
     * Lista pedidos NO entregados ni cancelados, incluyendo sus detalles completos
     * (nombre, estado de cocina, imagen).
     * Este es el método principal para Cocinero y Mesero Auxiliar.
     * @return Lista de objetos Pedido con sus DetallePedidoConNombre.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public List<Pedido> listarPedidosNoEntregados() throws SQLException {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT p.id_pedido, p.id_mesa, m.nombre_mesa AS nombre_mesa, p.fecha_hora, p.total_pedido, p.estado, p.id_usuario, p.observaciones, p.sopas_entregadas_auxiliar " + 
                     "FROM pedidos p " +
                     "JOIN mesas m ON p.id_mesa = m.id_mesa " +
                     "WHERE p.estado != 'Entregado' AND p.estado != 'Cancelado' " +
                     "ORDER BY p.fecha_hora ASC";

        DetallePedidoDAO detalleDao = new DetallePedidoDAO(); 

        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Pedido pedido = new Pedido(); 
                pedido.setIdPedido(rs.getInt("id_pedido"));
                pedido.setIdUsuario(rs.getInt("id_usuario")); 
                pedido.setIdMesa(rs.getInt("id_mesa"));
                pedido.setNombreMesa(rs.getString("nombre_mesa"));
                pedido.setFechaHora(rs.getTimestamp("fecha_hora").toLocalDateTime());
                pedido.setEstado(rs.getString("estado"));
                pedido.setObservaciones(rs.getString("observaciones")); 
                pedido.setTotalPedido(rs.getBigDecimal("total_pedido"));
                pedido.setSopasEntregadasAuxiliar(rs.getBoolean("sopas_entregadas_auxiliar")); 
                
                List<DetallePedidoConNombre> detalles = detalleDao.obtenerDetallesConNombrePorPedido(pedido.getIdPedido());
                pedido.setDetalles(detalles); 

                pedidos.add(pedido);
                System.out.println("DEBUG (PedidoDAO - listarPedidosNoEntregados): Pedido ID " + pedido.getIdPedido() + ", Estado: '" + pedido.getEstado() + "', SopasEntregadasAuxiliar: " + pedido.isSopasEntregadasAuxiliar());
            }
        }
        return pedidos;
    }

    /**
     * Obtiene un pedido por su ID, incluyendo el nombre de la mesa y total_pedido.
     * Nota: Este método no carga los detalles.
     * @param id El ID del pedido a buscar.
     * @return El objeto Pedido si se encuentra, o null.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public Pedido obtenerPedidoPorId(int id) throws SQLException {
        Pedido pedido = null;
        String sql = "SELECT p.id_pedido, p.id_usuario, p.id_mesa, m.nombre_mesa AS nombre_mesa, p.estado, p.fecha_hora, p.total_pedido, p.observaciones, p.sopas_entregadas_auxiliar " + 
                     "FROM pedidos p JOIN mesas m ON p.id_mesa = m.id_mesa WHERE p.id_pedido = ?";
        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    pedido = new Pedido(); 
                    pedido.setIdPedido(rs.getInt("id_pedido"));
                    pedido.setIdUsuario(rs.getInt("id_usuario")); 
                    pedido.setIdMesa(rs.getInt("id_mesa"));
                    pedido.setNombreMesa(rs.getString("nombre_mesa"));
                    pedido.setFechaHora(rs.getTimestamp("fecha_hora").toLocalDateTime());
                    pedido.setEstado(rs.getString("estado"));
                    pedido.setObservaciones(rs.getString("observaciones")); 
                    pedido.setTotalPedido(rs.getBigDecimal("total_pedido"));
                    pedido.setSopasEntregadasAuxiliar(rs.getBoolean("sopas_entregadas_auxiliar")); 
                    System.out.println("DEBUG (PedidoDAO - obtenerPedidoPorId): Pedido ID " + id + ", Estado: '" + pedido.getEstado() + "', SopasEntregadasAuxiliar: " + pedido.isSopasEntregadasAuxiliar());
                }
            }
        }
        return pedido;
    }

    /**
     * Recalcula y actualiza el total de un pedido en la base de datos.
     * @param idPedido El ID del pedido cuyo total se va a recalcular.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public void recalcularTotalPedido(int idPedido) throws SQLException {
        BigDecimal nuevoTotal = BigDecimal.ZERO;
        DetallePedidoDAO detalleDao = new DetallePedidoDAO();
        List<DetallePedidoConNombre> detalles = detalleDao.obtenerDetallesConNombrePorPedido(idPedido);

        for (DetallePedidoConNombre detalle : detalles) {
            BigDecimal precioUnitario = (detalle.getPrecioUnitario() != null) ? detalle.getPrecioUnitario() : BigDecimal.ZERO;
            nuevoTotal = nuevoTotal.add(precioUnitario.multiply(new BigDecimal(detalle.getCantidad())));
        }

        String sqlUpdate = "UPDATE pedidos SET total_pedido = ? WHERE id_pedido = ?";
        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdate)) {
            stmtUpdate.setBigDecimal(1, nuevoTotal);
            stmtUpdate.setInt(2, idPedido);
            stmtUpdate.executeUpdate();
            System.out.println("DEBUG (PedidoDAO): Total de pedido " + idPedido + " recalculado a: " + nuevoTotal);
        }
    }

    /**
     * Actualiza el estado de un pedido en la base de datos.
     * @param idPedido El ID del pedido a actualizar.
     * @param nuevoEstado El nuevo estado del pedido (ej. "pendiente", "listo", "entregado", "cancelado").
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public void actualizarEstadoPedido(int idPedido, String nuevoEstado) throws SQLException {
        String sql = "UPDATE pedidos SET estado = ? WHERE id_pedido = ?";
        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nuevoEstado);
            stmt.setInt(2, idPedido);
            stmt.executeUpdate();
            System.out.println("DEBUG (PedidoDAO): Estado de pedido " + idPedido + " actualizado a: " + nuevoEstado);
        }
    }
}