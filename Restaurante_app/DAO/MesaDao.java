package Restaurante_app.DAO;

// Importa la clase para obtener conexiones a la base de datos
import Restaurante_app.BaseDatos.Conexion_BaseDatos;
// Importa la clase modelo Mesa para manejar objetos Mesa
import Restaurante_app.Model.Mesa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MesaDao {

    // Consulta SQL para listar todas las mesas con sus campos id, nombre y estado
    private static final String SQL_LISTAR = "SELECT id_mesa, nombre_mesa, estado FROM mesas";
    // Consulta SQL para obtener una mesa por su ID (aquí hay un error en la columna "id" que debería ser "id_mesa")
    private static final String SQL_POR_ID = "SELECT id_mesa, nombre_mesa, estado FROM mesas WHERE id = ?";

    /**
     * Método para listar todas las mesas existentes en la base de datos.
     * @return lista de objetos Mesa
     * @throws SQLException en caso de error en la consulta o conexión
     */
    public List<Mesa> listarMesas() throws SQLException {
        // Crea una lista vacía para almacenar las mesas obtenidas
        List<Mesa> mesas = new ArrayList<>();

        // Intenta abrir conexión, preparar consulta y ejecutar
        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement ps = conn.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {

            // Itera sobre los resultados del ResultSet
            while (rs.next()) {
                // Obtiene valores de cada columna por su nombre
                int id = rs.getInt("id_mesa");
                String nombre = rs.getString("nombre_mesa");
                String estado = rs.getString("estado");

                // Crea un nuevo objeto Mesa con los datos y lo añade a la lista
                mesas.add(new Mesa(id, nombre, estado));
            }
        }

        // Retorna la lista completa de mesas
        return mesas;
    }

    /**
     * Método para obtener una mesa específica a partir de su ID.
     * @param id identificador de la mesa
     * @return objeto Mesa si la encuentra, null si no existe
     * @throws SQLException si ocurre un error con la base de datos
     */
    public Mesa obtenerMesaPorId(int id) throws SQLException {
        // Intenta abrir conexión y preparar la consulta con parámetro
        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement ps = conn.prepareStatement(SQL_POR_ID)) {

            // Establece el parámetro id en la consulta
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                // Si hay resultado, crea y devuelve el objeto Mesa
                if (rs.next()) {
                    return new Mesa(
                        rs.getInt("id_mesa"),
                        rs.getString("nombre_mesa"),
                        rs.getString("estado")
                    );
                }
            }
        }

        // Si no se encontró la mesa, retorna null
        return null;
    }
    
    /**
     * Método para agregar una nueva mesa con estado predeterminado 'libre'.
     * @param nombre nombre o identificador visible de la mesa
     * @return true si la inserción fue exitosa, false en caso contrario
     */
    public boolean agregarMesa(String nombre) throws SQLException {
        String sql = "INSERT INTO mesas (nombre_mesa, estado) VALUES (?, 'Disponible')";

        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombre);
            int filasInsertadas = stmt.executeUpdate();
            return filasInsertadas > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }
    
 // Método para actualizar el estado de una mesa
    public void actualizarEstadoMesa(int idMesa, String nuevoEstado) throws SQLException {
        String sql = "UPDATE mesas SET estado = ? WHERE id_mesa = ?";
        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idMesa);
            ps.executeUpdate();
        }
    }
    
    public Mesa obtenerMesaPorNombre(String nombreMesa) throws SQLException {
        String sql = "SELECT id_mesa, nombre_mesa, estado FROM mesas WHERE nombre_mesa = ?";
        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombreMesa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Mesa(rs.getInt("id_mesa"), rs.getString("nombre_mesa"), rs.getString("estado"));
                }
            }
        }
        return null; // Retorna null si no se encuentra la mesa
    }

}


