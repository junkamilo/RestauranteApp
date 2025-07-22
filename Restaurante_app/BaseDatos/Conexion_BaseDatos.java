package Restaurante_app.BaseDatos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion_BaseDatos {

    private static final String URL  = "jdbc:mysql://localhost:3306/restaurante_app";
    private static final String USER = "usuario_restaurante";
    private static final String PASS = "usuario_2025";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("DEBUG (Conexion_BaseDatos): Driver MySQL cargado exitosamente.");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("MySQL Driver no encontrado: " + e);
        }
    }

    public static Connection conectar() throws SQLException {
        Connection conn = DriverManager.getConnection(URL, USER, PASS);
        if (!conn.getAutoCommit()) {
            conn.setAutoCommit(true);
            System.out.println("DEBUG (Conexion_BaseDatos): AutoCommit for new connection set to TRUE.");
        } else {
            System.out.println("DEBUG (Conexion_BaseDatos): New connection already has AutoCommit TRUE.");
        }
        return conn;
    }
}
