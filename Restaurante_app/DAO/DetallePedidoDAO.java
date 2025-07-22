package Restaurante_app.DAO;

import Restaurante_app.BaseDatos.Conexion_BaseDatos;
import Restaurante_app.Model.DetallePedido;
import Restaurante_app.Model.DetallePedidoConNombre;
import Restaurante_app.Model.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class DetallePedidoDAO {

    // ELIMINADO: private final PedidoDAO pedidoDao = new PedidoDAO();
    // Ahora se instanciará localmente en los métodos que lo necesiten.

    /**
     * Obtiene los detalles de un pedido, incluyendo el nombre del producto, precio y ID.
     * @param idPedido ID del pedido
     * @return Lista de detalles del pedido con nombre de producto
     * @throws SQLException Si ocurre un error con la base de datos
     */
	public List<DetallePedidoConNombre> obtenerDetallesConNombrePorPedido(int idPedido) throws SQLException {
	    List<DetallePedidoConNombre> detalles = new ArrayList<>();
	    String sql = "SELECT dp.id_detalle, dp.cantidad, dp.sopa_incluida, dp.tamano_sopa, dp.especificaciones, " +
	                 "       p.nombre AS nombre_producto, p.imagen, p.precio, dp.estado_cocina, dp.estado_servicio, dp.id_pedido, dp.id_producto " +
	                 "FROM detalle_pedido dp " +
	                 "JOIN productos p ON dp.id_producto = p.id_producto " +
	                 "WHERE dp.id_pedido = ?";

	    System.out.println("DEBUG (DetallePedidoDAO): Obteniendo detalles con nombre para Pedido ID: " + idPedido + "...");
	    try (Connection conn = Conexion_BaseDatos.conectar();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setInt(1, idPedido);
	        try (ResultSet rs = stmt.executeQuery()) {
	            while (rs.next()) {
	                DetallePedidoConNombre detalle = new DetallePedidoConNombre(
	                    rs.getString("nombre_producto"),
	                    rs.getInt("cantidad"),
	                    rs.getBoolean("sopa_incluida"),
	                    rs.getString("tamano_sopa"),
	                    rs.getString("especificaciones")
	                );
	                detalle.setIdDetalle(rs.getInt("id_detalle"));
	                detalle.setIdProducto(rs.getInt("id_producto")); 
	                detalle.setImagen(rs.getString("imagen"));
	                detalle.setPrecioUnitario(rs.getBigDecimal("precio"));
	                detalle.setEstadoCocina(rs.getString("estado_cocina"));
	                detalle.setEstadoServicio(rs.getString("estado_servicio"));
	                detalle.setIdPedido(rs.getInt("id_pedido"));
	                System.out.println("DEBUG (DetallePedidoDAO - fetch by Pedido): ID Detalle: " + detalle.getIdDetalle() + ", Producto: " + detalle.getNombreProducto() + ", Sopa Incluida: " + detalle.isSopaIncluida() + ", Estado Servicio: '" + detalle.getEstadoServicio() + "'");
	                detalles.add(detalle);
	            }
	        }
	    }
	    return detalles;
	}

    /**
     * Inserta un nuevo detalle de pedido a la base de datos.
     * @param idPedido         ID del pedido
     * @param idProducto       ID del producto a agregar
     * @param cantidad         Cantidad solicitada
     * @param sopaIncluida     Si se incluyó sopa en el pedido
     * @param tamanoSopa       Tamaño de la sopa (completa, media, etc.)
     * @param especificaciones Observaciones del cliente (ej. sin arroz)
     * @throws SQLException Si ocurre un error de conexión o ejecución
     */
    public void insertarDetalle(int idPedido,
                                int idProducto,
                                int cantidad,
                                boolean sopaIncluida,
                                String tamanoSopa,
                                String especificaciones) throws SQLException {
        
        String sqlInsertar = "INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad, especificaciones, sopa_incluida, tamano_sopa, estado_cocina, estado_servicio) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement psInsert = conn.prepareStatement(sqlInsertar)) {

            psInsert.setInt(1, idPedido);
            psInsert.setInt(2, idProducto);
            psInsert.setInt(3, cantidad);
            psInsert.setString(4, especificaciones);
            psInsert.setBoolean(5, sopaIncluida);

            if (sopaIncluida && tamanoSopa != null && !tamanoSopa.isEmpty()) {
                psInsert.setString(6, tamanoSopa);
            } else {
                psInsert.setNull(6, Types.VARCHAR);
            }
            psInsert.setString(7, "Pendiente"); 
            psInsert.setString(8, "Pendiente"); 

            psInsert.executeUpdate();
            System.out.println("DEBUG (DetallePedidoDAO): Insertado nuevo detalle para pedido " + idPedido + ", producto " + idProducto + ", sopa_incluida=" + sopaIncluida + ", estado_cocina='Pendiente', estado_servicio='Pendiente'.");
            
            // Instancia PedidoDAO localmente para esta operación
            PedidoDAO pedidoDao = new PedidoDAO();
            pedidoDao.recalcularTotalPedido(idPedido);

        }
    }

    /**
     * Actualiza los detalles de un producto en un pedido existente, incluyendo el producto en sí.
     * @param detalle Objeto DetallePedidoConNombre con los datos actualizados.
     * @return true si la actualización fue exitosa, false en caso contrario.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public boolean actualizarDetalle(DetallePedidoConNombre detalle) throws SQLException {
        String sql = "UPDATE detalle_pedido SET id_producto = ?, cantidad = ?, sopa_incluida = ?, tamano_sopa = ?, especificaciones = ? WHERE id_detalle = ?";
        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, detalle.getIdProducto()); 
            ps.setInt(2, detalle.getCantidad());
            ps.setBoolean(3, detalle.isSopaIncluida());
            ps.setString(4, detalle.isSopaIncluida() && detalle.getTamanoSopa() != null && !detalle.getTamanoSopa().isEmpty() ? detalle.getTamanoSopa() : null);
            ps.setString(5, detalle.getEspecificaciones());
            ps.setInt(6, detalle.getIdDetalle());
            
            boolean exito = ps.executeUpdate() > 0;
            System.out.println("DEBUG (DetallePedidoDAO): Actualizado detalle ID " + detalle.getIdDetalle() + " (producto data). Éxito: " + exito);
            if (exito) {
                // Instancia PedidoDAO localmente para esta operación
                PedidoDAO pedidoDao = new PedidoDAO();
                pedidoDao.recalcularTotalPedido(detalle.getIdPedido()); 
            }
            return exito;
        }
    }

    /**
     * Obtiene un único detalle de pedido por su ID, incluyendo el nombre y la imagen del producto,
     * y los estados de cocina y servicio.
     *
     * @param idDetalle ID del detalle de pedido a buscar.
     * @return Un objeto DetallePedidoConNombre si se encuentra, o null si no existe.
     * @throws SQLException Si ocurre un error con la base de datos.
     */
    public DetallePedidoConNombre obtenerDetallePedidoPorId(int idDetalle) throws SQLException {
        DetallePedidoConNombre detalle = null;
        String sql = "SELECT dp.id_detalle, dp.cantidad, dp.sopa_incluida, dp.tamano_sopa, dp.especificaciones, " +
                     "       p.nombre AS nombre_producto, p.imagen, p.precio, dp.estado_cocina, dp.estado_servicio, dp.id_pedido, dp.id_producto " +
                     "FROM detalle_pedido dp " +
                     "JOIN productos p ON dp.id_producto = p.id_producto " +
                     "WHERE dp.id_detalle = ?";

        System.out.println("DEBUG (DetallePedidoDAO): Obteniendo detalle por ID: " + idDetalle + "...");
        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idDetalle);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    detalle = new DetallePedidoConNombre(
                        rs.getString("nombre_producto"),
                        rs.getInt("cantidad"),
                        rs.getBoolean("sopa_incluida"),
                        rs.getString("tamano_sopa"),
                        rs.getString("especificaciones")
                    );
                    detalle.setIdDetalle(rs.getInt("id_detalle"));
                    detalle.setIdProducto(rs.getInt("id_producto"));
                    detalle.setImagen(rs.getString("imagen"));
                    detalle.setPrecioUnitario(rs.getBigDecimal("precio"));
                    detalle.setEstadoCocina(rs.getString("estado_cocina"));
                    detalle.setEstadoServicio(rs.getString("estado_servicio")); 
                    detalle.setIdPedido(rs.getInt("id_pedido"));
                    System.out.println("DEBUG (DetallePedidoDAO - fetch by ID result): ID Detalle: " + idDetalle + ", Sopa Incluida: " + detalle.isSopaIncluida() + ", Estado Cocina: '" + detalle.getEstadoCocina() + "', Estado Servicio: '" + detalle.getEstadoServicio() + "'");
                } else {
                    System.out.println("DEBUG (DetallePedidoDAO - fetch by ID): No se encontró detalle para ID: " + idDetalle);
                }
            }
        }
        return detalle;
    }


    /**
     * Lista todos los detalles de un pedido, incluyendo nombre e imagen del producto,
     * y estados de cocina y servicio.
     *
     * @param idPedido ID del pedido
     * @return Lista de objetos DetallePedido
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public List<DetallePedido> listarDetallePorPedido(int idPedido) throws SQLException {
        List<DetallePedido> detalles = new ArrayList<>();

        String sql = "SELECT dp.*, p.nombre, p.imagen FROM detalle_pedido dp " +
                     "JOIN productos p ON dp.id_producto = p.id_producto " +
                     "WHERE dp.id_pedido = ?";

        System.out.println("DEBUG (DetallePedidoDAO): Listando detalles (DetallePedido model) por Pedido ID: " + idPedido);
        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPedido);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                DetallePedido detalle = new DetallePedido();
                detalle.setIdDetalle(rs.getInt("id_detalle"));
                detalle.setIdProducto(rs.getInt("id_producto"));
                detalle.setNombreProducto(rs.getString("nombre"));
                detalle.setImagen(rs.getString("imagen"));
                detalle.setCantidad(rs.getInt("cantidad"));
                detalle.setEstadoCocina(rs.getString("estado_cocina"));
                detalle.setEstadoServicio(rs.getString("estado_servicio"));
                detalle.setSopaIncluida(rs.getBoolean("sopa_incluida"));
                detalle.setTamanoSopa(rs.getString("tamano_sopa"));
                detalles.add(detalle);
            }
        }
        return detalles;
    }

    /**
     * Actualiza el estado del producto en cocina (ej: "Pendiente", "Listo", "En preparación").
     * Utilizado principalmente por el Cocinero.
     *
     * @param idDetalle   ID del detalle a actualizar.
     * @param nuevoEstadoCocina Nuevo estado para cocina.
     * @return true si se actualizó con éxito, false si no.
     * @throws SQLException Si ocurre un error en la base de datos.
     */
    public boolean actualizarEstadoCocina(int idDetalle, String nuevoEstadoCocina) throws SQLException {
        String sql = "UPDATE detalle_pedido SET estado_cocina = ? WHERE id_detalle = ?";
        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, nuevoEstadoCocina);
            ps.setInt(2, idDetalle);
            int rowsAffected = ps.executeUpdate();
            System.out.println("DEBUG (DetallePedidoDAO): Actualizando estado_cocina para id_detalle " + idDetalle + " a '" + nuevoEstadoCocina + "'. Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        }
    }

    /**
     * Actualiza el estado del producto en servicio (ej: "Pendiente", "Servido").
     * Utilizado principalmente por el Mesero Auxiliar para platos individuales.
     *
     * @param idDetalle   ID del detalle a actualizar.
     * @param nuevoEstadoServicio Nuevo estado para servicio.
     * @return true si se actualizó con éxito, false si no.
     * @throws SQLException Si ocurre un error en la base de datos.
     */
    public boolean actualizarEstadoServicio(int idDetalle, String nuevoEstadoServicio) throws SQLException {
        String sql = "UPDATE detalle_pedido SET estado_servicio = ? WHERE id_detalle = ?";
        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, nuevoEstadoServicio);
            ps.setInt(2, idDetalle);
            int rowsAffected = ps.executeUpdate();
            System.out.println("DEBUG (DetallePedidoDAO): Actualizando estado_servicio para id_detalle " + idDetalle + " a '" + nuevoEstadoServicio + "'. Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        }
    }

    /**
     * Actualiza el estado de servicio de TODOS los detalles de SOPA para un pedido específico.
     * Este método es para cuando el Mesero Auxiliar marca el checkbox general de sopas.
     * @param idPedido ID del pedido al que pertenecen las sopas.
     * @param nuevoEstadoServicio El estado al que se cambiará (ej. "servido" o "pendiente").
     * @return true si se actualizaron detalles, false si no.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public boolean actualizarEstadoServicioParaSopasDePedido(int idPedido, String nuevoEstadoServicio) throws SQLException {
        String sql = "UPDATE detalle_pedido SET estado_servicio = ? WHERE id_pedido = ? AND sopa_incluida = TRUE";
        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, nuevoEstadoServicio);
            ps.setInt(2, idPedido);
            int rowsAffected = ps.executeUpdate();
            System.out.println("DEBUG (DetallePedidoDAO): Actualizando estado_servicio para TODAS las sopas del Pedido ID " + idPedido + " a '" + nuevoEstadoServicio + "'. Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        }
    }


    /**
     * Elimina un detalle de pedido de la base de datos.
     * @param idDetalle ID del detalle a eliminar.
     * @return true si se eliminó con éxito, false si no.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public boolean eliminarDetalle(int idDetalle) throws SQLException {
        int idPedido = -1;
        String sqlSelectPedido = "SELECT id_pedido FROM detalle_pedido WHERE id_detalle = ?";
        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement psSelect = conn.prepareStatement(sqlSelectPedido)) {

            psSelect.setInt(1, idDetalle);
            try (ResultSet rs = psSelect.executeQuery()) {
                if (rs.next()) {
                    idPedido = rs.getInt("id_pedido");
                }
            }
        }

        if (idPedido == -1) {
            System.err.println("Error: No se encontró el pedido asociado al detalle " + idDetalle);
            return false;
        }

        String sqlDelete = "DELETE FROM detalle_pedido WHERE id_detalle = ?";
        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement psDelete = conn.prepareStatement(sqlDelete)) {
            
            psDelete.setInt(1, idDetalle);
            int rowsAffected = psDelete.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("DEBUG (DetallePedidoDAO): Detalle ID " + idDetalle + " eliminado. Recalculando total para pedido " + idPedido + ".");
                // Instancia PedidoDAO localmente para esta operación
                PedidoDAO pedidoDao = new PedidoDAO();
                pedidoDao.recalcularTotalPedido(idPedido);
            }
            return rowsAffected > 0;
        }
    }
}