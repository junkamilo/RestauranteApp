package Restaurante_app.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import Restaurante_app.BaseDatos.Conexion_BaseDatos; // Usando tu paquete para la conexión
import Restaurante_app.Model.Usuario; // Asegúrate de que esta clase exista y esté correctamente definida

/**
 * Clase DAO encargada de gestionar operaciones relacionadas con los usuarios.
 * Ahora maneja 'nombre_login' para el login del usuario y 'nombre' (ENUM) para el tipo de rol.
 */
public class UsuarioDao {

    // --- Consultas SQL Actualizadas ---
    // Usamos 'nombre_login' para el login del usuario y 'nombre' (el ENUM de la BD) para el rol.

    private static final String SQL_INSERTAR_USUARIO = "INSERT INTO usuarios (nombre_login, contraseña, nombre) VALUES (?, ?, ?)";
    private static final String SQL_VALIDAR_CREDENCIALES = "SELECT id_usuario, nombre_login, contraseña, nombre FROM usuarios WHERE nombre_login = ? AND contraseña = ?";
    private static final String SQL_LISTAR_NOMBRES_LOGIN = "SELECT nombre_login FROM usuarios ORDER BY nombre_login"; // Para listar los nombres de login
    private static final String SQL_LISTAR_TODOS_USUARIOS = "SELECT id_usuario, nombre_login, contraseña, nombre FROM usuarios ORDER BY nombre_login"; // Listar todo, incluyendo el rol
    private static final String SQL_OBTENER_CONTRASENA = "SELECT contraseña FROM usuarios WHERE nombre_login = ?";
    private static final String SQL_OBTENER_ID_POR_LOGIN = "SELECT id_usuario FROM usuarios WHERE nombre_login = ?";
    private static final String SQL_ELIMINAR_USUARIO = "DELETE FROM usuarios WHERE id_usuario = ?";
    private static final String SQL_ACTUALIZAR_CONTRASENA = "UPDATE usuarios SET contraseña = ? WHERE id_usuario = ?";
    private static final String SQL_OBTENER_ROL_POR_LOGIN = "SELECT nombre FROM usuarios WHERE nombre_login = ?"; // Nuevo: para obtener el rol ENUM por el login

    /**
     * Inserta un nuevo usuario en la base de datos.
     * La información se mapea desde el objeto Usuario a las columnas de la BD.
     *
     * @param usuario El objeto Usuario a insertar, que contiene el nombre de login y el tipo de rol.
     * @return true si la inserción fue exitosa, false en caso contrario.
     * @throws SQLException Si ocurre un error de SQL.
     */
    public boolean insertarUsuario(Usuario usuario) throws SQLException {
        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERTAR_USUARIO)) {
            ps.setString(1, usuario.getNombreLogin());   // Mapea a 'nombre_login' en la BD
            ps.setString(2, usuario.getContrasena());    // Mapea a 'contraseña' en la BD
            ps.setString(3, usuario.getRolTipo());       // Mapea a 'nombre' (ENUM) en la BD
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Valida las credenciales de un usuario usando su nombre de login y contraseña.
     *
     * @param nombreLogin El nombre de login del usuario.
     * @param contrasena La contraseña en texto plano (se comparará directamente).
     * @return El objeto Usuario si las credenciales son válidas, null en caso contrario.
     * @throws SQLException Si ocurre un error de SQL.
     */
    public Usuario validarCredenciales(String nombreLogin, String contrasena) throws SQLException {
        Usuario usuario = null;
        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement ps = conn.prepareStatement(SQL_VALIDAR_CREDENCIALES)) {
            ps.setString(1, nombreLogin);
            ps.setString(2, contrasena);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    usuario = new Usuario();
                    usuario.setIdUsuario(rs.getInt("id_usuario"));
                    usuario.setNombreLogin(rs.getString("nombre_login")); // Lee de 'nombre_login'
                    usuario.setContrasena(rs.getString("contraseña"));
                    usuario.setRolTipo(rs.getString("nombre"));          // Lee de 'nombre' (ENUM) para el rol
                }
            }
        }
        return usuario;
    }

    /**
     * Lista todos los nombres de login de usuario registrados en el sistema.
     * Este método reemplaza el anterior que listaba los roles.
     *
     * @return una lista de nombres de login de usuario.
     * @throws SQLException si ocurre un error de conexión o consulta.
     */
    public List<String> listarNombres() throws SQLException { // Mantener nombre para compatibilidad en llamadas
        List<String> lista = new ArrayList<>();
        try (
            Connection conn = Conexion_BaseDatos.conectar();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(SQL_LISTAR_NOMBRES_LOGIN) // <-- Usa la nueva consulta
        ) {
            while (rs.next()) {
                lista.add(rs.getString("nombre_login")); // <-- Lee de 'nombre_login'
            }
        }
        return lista;
    }

    /**
     * Lista todos los usuarios completos (ID, nombreLogin, contraseña, rolTipo) registrados en el sistema.
     * Útil para funcionalidades de administración.
     *
     * @return una lista de objetos Usuario.
     * @throws SQLException si ocurre un error de conexión o consulta.
     */
    public List<Usuario> listarTodosLosUsuarios() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement ps = conn.prepareStatement(SQL_LISTAR_TODOS_USUARIOS);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(rs.getInt("id_usuario"));
                usuario.setNombreLogin(rs.getString("nombre_login")); // Lee de 'nombre_login'
                usuario.setContrasena(rs.getString("contraseña"));
                usuario.setRolTipo(rs.getString("nombre"));          // Lee de 'nombre' (ENUM) para el rol
                usuarios.add(usuario);
            }
        }
        return usuarios;
    }

    /**
     * Obtiene la contraseña de un usuario específico según su nombre de login.
     *
     * @param nombreLogin el nombre de login del usuario.
     * @return la contraseña del usuario o null si no se encuentra.
     * @throws SQLException si ocurre un error de base de datos.
     */
    public String obtenerContrasena(String nombreLogin) throws SQLException {
        try (
            Connection conn = Conexion_BaseDatos.conectar();
            PreparedStatement ps = conn.prepareStatement(SQL_OBTENER_CONTRASENA)
        ) {
            ps.setString(1, nombreLogin);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("contraseña");
                }
            }
        }
        return null;
    }

    /**
     * Obtiene el ID del usuario a partir de su nombre de login.
     *
     * @param nombreLogin el nombre de login del usuario.
     * @return el ID del usuario.
     * @throws SQLException si el usuario no se encuentra o si hay un error en la base de datos.
     */
    public int obtenerIdPorNombre(String nombreLogin) throws SQLException { // Mantener nombre para compatibilidad en llamadas
        try (
            Connection conn = Conexion_BaseDatos.conectar();
            PreparedStatement ps = conn.prepareStatement(SQL_OBTENER_ID_POR_LOGIN) // <-- Usa la nueva consulta
        ) {
            ps.setString(1, nombreLogin);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_usuario");
                } else {
                    throw new SQLException("Usuario no encontrado con nombre de login: " + nombreLogin);
                }
            }
        }
    }

    /**
     * Elimina un usuario por su ID.
     *
     * @param idUsuario El ID del usuario a eliminar.
     * @return true si la eliminación fue exitosa, false en caso contrario.
     * @throws SQLException Si ocurre un error de SQL.
     */
    public boolean eliminarUsuario(int idUsuario) throws SQLException {
        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement ps = conn.prepareStatement(SQL_ELIMINAR_USUARIO)) {
            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Actualiza la contraseña de un usuario por su ID.
     *
     * @param idUsuario El ID del usuario.
     * @param nuevaContrasena La nueva contraseña (ya hasheada si aplica).
     * @return true si la actualización fue exitosa, false en caso contrario.
     * @throws SQLException Si ocurre un error de SQL.
     */
    public boolean actualizarContrasena(int idUsuario, String nuevaContrasena) throws SQLException {
        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement ps = conn.prepareStatement(SQL_ACTUALIZAR_CONTRASENA)) {
            ps.setString(1, nuevaContrasena);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Obtiene el tipo de rol (valor del ENUM 'nombre' en la BD) de un usuario por su nombre de login.
     * Este método es útil para determinar los permisos o la interfaz a mostrar.
     * @param nombreLogin El nombre de login del usuario.
     * @return El tipo de rol (ej. "Administrador", "Mesero"), o null si no se encuentra.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public String obtenerRolTipoPorLogin(String nombreLogin) throws SQLException {
        try (Connection conn = Conexion_BaseDatos.conectar();
             PreparedStatement ps = conn.prepareStatement(SQL_OBTENER_ROL_POR_LOGIN)) {
            ps.setString(1, nombreLogin);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nombre"); // Retorna el valor del ENUM 'nombre' de la BD
                }
            }
        }
        return null;
    }
}
