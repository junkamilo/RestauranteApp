package Restaurante_app.DAO;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.math.BigDecimal; // Importar BigDecimal

import Restaurante_app.BaseDatos.Conexion_BaseDatos;
import Restaurante_app.Model.PlatoVendido;

public class VentaDao {

    /**
     * Obtiene el monto total de ventas del día actual, excluyendo pedidos cancelados.
     * Utiliza BigDecimal para mayor precisión monetaria.
     * @return BigDecimal con el monto total del día, o BigDecimal.ZERO si no hay ventas.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public BigDecimal obtenerMontoTotalDelDia() throws SQLException {
        BigDecimal total = BigDecimal.ZERO;
        String sql = """
            SELECT SUM(dp.cantidad * p.precio) AS monto_total
            FROM detalle_pedido dp
            JOIN productos p ON dp.id_producto = p.id_producto
            JOIN pedidos ped ON dp.id_pedido = ped.id_pedido
            WHERE DATE(ped.fecha_hora) = CURDATE() AND ped.estado != 'Cancelado'
        """;

        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                BigDecimal result = rs.getBigDecimal("monto_total");
                if (result != null) {
                    total = result;
                }
            }
        }
        return total;
    }

    /**
     * Obtiene la cantidad total de platos vendidos del día actual, excluyendo pedidos cancelados.
     * @return int con la cantidad total de platos.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public int obtenerTotalPlatosVendidosHoy() throws SQLException {
        int total = 0;
        String sql = """
            SELECT SUM(dp.cantidad) AS total_platos
            FROM detalle_pedido dp
            JOIN pedidos ped ON dp.id_pedido = ped.id_pedido
            WHERE DATE(ped.fecha_hora) = CURDATE() AND ped.estado != 'Cancelado'
        """;

        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                total = rs.getInt("total_platos");
            }
        }
        return total;
    }

    /**
     * Obtiene una lista de todos los platos vendidos hoy, agrupados por nombre y categoría,
     * sumando sus cantidades y totales. Excluye pedidos cancelados.
     * Ordena **primero por categoría**, luego por cantidad total descendente y nombre alfabético.
     * @return Lista de objetos PlatoVendido.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public List<PlatoVendido> obtenerPlatosVendidosHoy() throws SQLException {
        List<PlatoVendido> lista = new ArrayList<>();

        String sql = """
            SELECT 
                p.categoria,
                p.nombre AS nombre_producto,
                SUM(dp.cantidad) AS cantidad,
                SUM(dp.cantidad * p.precio) AS total
            FROM detalle_pedido dp
            JOIN productos p ON dp.id_producto = p.id_producto
            JOIN pedidos ped ON dp.id_pedido = ped.id_pedido
            WHERE DATE(ped.fecha_hora) = CURDATE() AND ped.estado != 'Cancelado'
            GROUP BY p.categoria, p.nombre
            ORDER BY p.categoria ASC, cantidad DESC, nombre_producto ASC 
        """;

        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                PlatoVendido pv = new PlatoVendido();
                pv.setCategoria(rs.getString("categoria"));
                pv.setNombre(rs.getString("nombre_producto"));
                pv.setCantidad(rs.getInt("cantidad"));
                pv.setTotal(rs.getBigDecimal("total"));
                lista.add(pv);
            }
        }
        return lista;
    }

    /**
     * Obtiene el plato más vendido del día (no cancelado), basado en la cantidad total vendida.
     * Si hay empate en cantidad, se desempata por el monto total vendido y luego alfabéticamente.
     * @return Objeto PlatoVendido del plato más vendido, o null si no hay ventas hoy.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public PlatoVendido obtenerPlatoMasVendidoHoy() throws SQLException {
        PlatoVendido platoMasVendido = null;
        String sql = """
            SELECT p.nombre AS nombre_plato, p.categoria, 
                   SUM(dp.cantidad) AS cantidad_total, 
                   SUM(dp.cantidad * p.precio) AS monto_total_plato
            FROM detalle_pedido dp 
            JOIN productos p ON dp.id_producto = p.id_producto 
            JOIN pedidos ped ON dp.id_pedido = ped.id_pedido 
            WHERE DATE(ped.fecha_hora) = CURDATE() AND ped.estado != 'Cancelado' 
            GROUP BY p.nombre, p.categoria 
            ORDER BY cantidad_total DESC, monto_total_plato DESC, p.nombre ASC 
            LIMIT 1
        """;

        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                String nombre = rs.getString("nombre_plato");
                String categoria = rs.getString("categoria");
                int cantidad = rs.getInt("cantidad_total");
                BigDecimal total = rs.getBigDecimal("monto_total_plato");
                platoMasVendido = new PlatoVendido(nombre, categoria, cantidad, total);
            }
        }
        return platoMasVendido;
    }
}