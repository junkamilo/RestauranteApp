package Restaurante_app.Model;

public class Usuario {
    private int idUsuario;
    private String nombreLogin; // <-- ¡NUEVO CAMPO! Esto corresponde a 'nombre_login' en la DB
    private String contrasena;
    private String rolTipo;     // <-- Campo renombrado/reutilizado. Esto corresponde a 'nombre' (ENUM) en la DB

    /**
     * Constructor por defecto.
     * Permite crear una instancia de Usuario sin inicializar sus propiedades.
     */
    public Usuario() {
    }

    /**
     * Constructor para crear un nuevo usuario antes de insertarlo en la base de datos
     * (el ID se generará automáticamente en la DB).
     *
     * @param nombreLogin El nombre de usuario que se usará para el login (corresponde a 'nombre_login' en DB).
     * @param contrasena La contraseña del usuario.
     * @param rolTipo El tipo de rol asignado al usuario (corresponde a 'nombre' ENUM en DB, Ej: "Administrador").
     */
    public Usuario(String nombreLogin, String contrasena, String rolTipo) {
        this.nombreLogin = nombreLogin;
        this.contrasena = contrasena;
        this.rolTipo = rolTipo;
    }

    /**
     * Constructor completo para cuando se recupera un usuario de la base de datos
     * (ya tiene un ID y ambos nombres).
     *
     * @param idUsuario El ID único del usuario.
     * @param nombreLogin El nombre de usuario para login.
     * @param contrasena La contraseña del usuario.
     * @param rolTipo El tipo de rol del usuario.
     */
    public Usuario(int idUsuario, String nombreLogin, String contrasena, String rolTipo) {
        this.idUsuario = idUsuario;
        this.nombreLogin = nombreLogin;
        this.contrasena = contrasena;
        this.rolTipo = rolTipo;
    }

    // --- GETTERS ---
    /**
     * Obtiene el ID del usuario.
     * @return El ID del usuario.
     */
    public int getIdUsuario() {
        return idUsuario;
    }

    /**
     * Obtiene el nombre de usuario que se usa para el login.
     * @return El nombre de login del usuario.
     */
    public String getNombreLogin() {
        return nombreLogin;
    }

    /**
     * Obtiene la contraseña del usuario.
     * @return La contraseña del usuario.
     */
    public String getContrasena() {
        return contrasena;
    }

    /**
     * Obtiene el tipo de rol del usuario (Ej: "Administrador", "Mesero").
     * @return El tipo de rol del usuario.
     */
    public String getRolTipo() {
        return rolTipo;
    }

    // --- SETTERS ---
    /**
     * Establece el ID del usuario.
     * @param idUsuario El ID a establecer.
     */
    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    /**
     * Establece el nombre de usuario para el login.
     * @param nombreLogin El nombre de login a establecer.
     */
    public void setNombreLogin(String nombreLogin) {
        this.nombreLogin = nombreLogin;
    }

    /**
     * Establece la contraseña del usuario.
     * @param contrasena La contraseña a establecer.
     */
    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    /**
     * Establece el tipo de rol del usuario.
     * @param rolTipo El tipo de rol a establecer.
     */
    public void setRolTipo(String rolTipo) {
        this.rolTipo = rolTipo;
    }

    /**
     * Sobreescribe el método toString para una representación de cadena del objeto.
     * Útil para depuración.
     * La contraseña se oculta por seguridad.
     */
    @Override
    public String toString() {
        return "Usuario{" +
               "idUsuario=" + idUsuario +
               ", nombreLogin='" + nombreLogin + '\'' +
               ", rolTipo='" + rolTipo + '\'' +
               ", contrasena='[PROTECTED]'" + // No exponer la contraseña real en logs
               '}';
    }
}