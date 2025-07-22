package Restaurante_app.DAO;

import Restaurante_app.Model.Producto;
import Restaurante_app.BaseDatos.Conexion_BaseDatos;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

/**
 * DAO para manejar operaciones de productos.
 */
public class ProductoDao {

    // CRUD - Insertar
    public boolean insertarProducto(Producto p) throws SQLException {
        // Asegúrate de que los campos en la BD coincidan con este INSERT
        String sql = "INSERT INTO productos (nombre, precio, categoria, descripcion, imagen, disponibilidad, en_menu_dia) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getNombre());
            stmt.setBigDecimal(2, p.getPrecio());
            stmt.setString(3, p.getCategoria());
            stmt.setString(4, p.getDescripcion());
            stmt.setString(5, p.getImagen());
            stmt.setBoolean(6, p.isDisponibilidad());
            stmt.setBoolean(7, p.isEnMenuDia());
            return stmt.executeUpdate() > 0;
        }
    }

    // CRUD - Actualizar
    public boolean actualizarProducto(Producto p) throws SQLException {
        // Asegúrate de que los campos en la BD coincidan con este UPDATE
        String sql = "UPDATE productos SET nombre = ?, precio = ?, categoria = ?, descripcion = ?, imagen = ?, disponibilidad = ?, en_menu_dia = ? WHERE id_producto = ?";
        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getNombre());
            stmt.setBigDecimal(2, p.getPrecio());
            stmt.setString(3, p.getCategoria());
            stmt.setString(4, p.getDescripcion());
            stmt.setString(5, p.getImagen());
            stmt.setBoolean(6, p.isDisponibilidad());
            stmt.setBoolean(7, p.isEnMenuDia());
            stmt.setInt(8, p.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    // CRUD - Eliminar
    public boolean eliminarProducto(int id) throws SQLException {
        String sql = "DELETE FROM productos WHERE id_producto = ?";
        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    // CRUD - Listar todos
    public List<Producto> listarTodos() throws SQLException {
        List<Producto> lista = new ArrayList<>();
        // Selecciona TODOS los campos relevantes de la tabla productos
        String sql = "SELECT id_producto, nombre, precio, descripcion, categoria, imagen, disponibilidad, en_menu_dia FROM productos";
        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Producto p = new Producto(
                    rs.getInt("id_producto"),
                    rs.getString("nombre"),
                    rs.getBigDecimal("precio"),
                    rs.getString("descripcion"),
                    rs.getString("categoria"),
                    rs.getString("imagen"),
                    rs.getBoolean("disponibilidad"),
                    rs.getBoolean("en_menu_dia")
                );
                lista.add(p);
            }
        }
        return lista;
    }
    
    // Obtener Producto por Nombre
    public Producto obtenerProductoPorNombre(String nombre) throws SQLException {
        // Selecciona TODOS los campos relevantes de la tabla productos
        String sql = "SELECT id_producto, nombre, precio, descripcion, categoria, imagen, disponibilidad, en_menu_dia FROM productos WHERE nombre = ?";
        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Producto(
                    rs.getInt("id_producto"),
                    rs.getString("nombre"),
                    rs.getBigDecimal("precio"),
                    rs.getString("descripcion"),
                    rs.getString("categoria"),
                    rs.getString("imagen"),
                    rs.getBoolean("disponibilidad"),
                    rs.getBoolean("en_menu_dia")
                );
            }
        }
        return null;
    }

    /**
     * Obtiene un Producto por su ID.
     * @param id El ID del producto a buscar.
     * @return El objeto Producto si se encuentra, o null si no existe.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public Producto obtenerProductoPorId(int id) throws SQLException {
        String sql = "SELECT id_producto, nombre, precio, descripcion, categoria, imagen, disponibilidad, en_menu_dia FROM productos WHERE id_producto = ?";
        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Producto(
                    rs.getInt("id_producto"),
                    rs.getString("nombre"),
                    rs.getBigDecimal("precio"),
                    rs.getString("descripcion"),
                    rs.getString("categoria"),
                    rs.getString("imagen"),
                    rs.getBoolean("disponibilidad"),
                    rs.getBoolean("en_menu_dia")
                );
            }
        }
        return null;
    }


    // Buscar por nombre o categoría
    public List<Producto> buscar(String texto) throws SQLException {
        List<Producto> lista = new ArrayList<>();
        // Selecciona TODOS los campos relevantes de la tabla productos
        String sql = "SELECT id_producto, nombre, precio, descripcion, categoria, imagen, disponibilidad, en_menu_dia FROM productos WHERE nombre LIKE ? OR categoria LIKE ?";
        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String query = "%" + texto + "%";
            stmt.setString(1, query);
            stmt.setString(2, query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Producto p = new Producto(
                    rs.getInt("id_producto"),
                    rs.getString("nombre"),
                    rs.getBigDecimal("precio"),
                    rs.getString("descripcion"),
                    rs.getString("categoria"),
                    rs.getString("imagen"),
                    rs.getBoolean("disponibilidad"),
                    rs.getBoolean("en_menu_dia")
                );
                lista.add(p);
            }
        }
        return lista;
    }

    // Listar productos por categoría
    public List<Producto> listarPorCategoria(String categoria) throws SQLException {
        List<Producto> lista = new ArrayList<>();
        // Selecciona TODOS los campos relevantes de la tabla productos
        String sql = "SELECT id_producto, nombre, precio, descripcion, categoria, imagen, disponibilidad, en_menu_dia FROM productos WHERE categoria = ?";

        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoria);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                // Usa el constructor completo para asegurar que todos los campos se mapeen correctamente
                lista.add(new Producto(
                    rs.getInt("id_producto"),
                    rs.getString("nombre"),
                    rs.getBigDecimal("precio"),
                    rs.getString("descripcion"),
                    rs.getString("categoria"),
                    rs.getString("imagen"),
                    rs.getBoolean("disponibilidad"),
                    rs.getBoolean("en_menu_dia")
                ));
            }
        }
        return lista;
    }

    /**
     * Lista todos los productos que están marcados como 'en_menu_dia = true'.
     * @return Una lista de objetos Producto que están en el menú del día.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public List<Producto> listarMenuDelDia() throws SQLException {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT id_producto, nombre, precio, descripcion, categoria, imagen, disponibilidad, en_menu_dia FROM productos WHERE en_menu_dia = true";
        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                productos.add(new Producto(
                    rs.getInt("id_producto"),
                    rs.getString("nombre"),
                    rs.getBigDecimal("precio"),
                    rs.getString("descripcion"),
                    rs.getString("categoria"),
                    rs.getString("imagen"),
                    rs.getBoolean("disponibilidad"),
                    rs.getBoolean("en_menu_dia")
                ));
            }
        }
        return productos;
    }


    // Listar productos del menú del día por categoría
    public List<Producto> listarMenuDelDiaPorCategoria(String categoria) throws SQLException {
        List<Producto> productos = new ArrayList<>();
        // Selecciona TODOS los campos relevantes de la tabla productos
        String sql = "SELECT id_producto, nombre, precio, descripcion, categoria, imagen, disponibilidad, en_menu_dia FROM productos WHERE en_menu_dia = true AND categoria = ?";
        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, categoria);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                productos.add(new Producto(
                    rs.getInt("id_producto"),
                    rs.getString("nombre"),
                    rs.getBigDecimal("precio"),
                    rs.getString("descripcion"),
                    rs.getString("categoria"),
                    rs.getString("imagen"),
                    rs.getBoolean("disponibilidad"),
                    rs.getBoolean("en_menu_dia")
                ));
            }
        }
        return productos;
    }

    // Actualizar productos activos en el menú del día (Manejo de Transacciones)
    public void actualizarMenuDelDia(List<Producto> seleccionados) throws SQLException {
        // SQL para desactivar todos los productos del menú del día
        String desactivarTodo = "UPDATE productos SET en_menu_dia = false";
        // SQL para activar los productos seleccionados en el menú del día
        String activarSeleccionados = "UPDATE productos SET en_menu_dia = true WHERE id_producto = ?";
        
        Connection conn = null; 
        try {
            conn = Conexion_BaseDatos.conectar(); 
            conn.setAutoCommit(false); 

            try (PreparedStatement psDesactivar = conn.prepareStatement(desactivarTodo)) {
                psDesactivar.executeUpdate();
            }

            try (PreparedStatement psActivar = conn.prepareStatement(activarSeleccionados)) {
                for (Producto p : seleccionados) {
                    psActivar.setInt(1, p.getId());
                    psActivar.executeUpdate();
                }
            }
            conn.commit(); 
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); 
            }
            e.printStackTrace();
            throw e; 
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); 
                    conn.close(); 
                } catch (SQLException e) {
                    e.printStackTrace(); 
                }
            }
        }
    }

    // Limpiar menú del día
    public void limpiarMenuDelDia() throws SQLException {
        String sql = "UPDATE productos SET en_menu_dia = false WHERE en_menu_dia = true";
        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }
}