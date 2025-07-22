package Restaurante_app;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image; 
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.io.File; 

import javax.swing.BorderFactory;
import javax.swing.ImageIcon; 
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLightLaf;

import Restaurante_app.BaseDatos.Conexion_BaseDatos;
import Restaurante_app.DAO.ProductoDao;
import Restaurante_app.DAO.UsuarioDao;
import Restaurante_app.DAO.VentaDao; 
import Restaurante_app.GestionPaneles.Control_ventas;
import Restaurante_app.GestionPaneles.Detalles_pedido;
import Restaurante_app.GestionPaneles.Gestion_menu;
import Restaurante_app.GestionPaneles.Inventario;
import Restaurante_app.GestionPaneles.Tomar_pedido;
import Restaurante_app.GestionPaneles.Nuevo_usuario; 
import Restaurante_app.Model.PlatoVendido;
import Restaurante_app.Model.Usuario;
import Restaurante_app.roles.Administrador;
import Restaurante_app.roles.Cocinero;
import Restaurante_app.roles.Mesero;
import Restaurante_app.roles.Mesero_Auxiliar; 

public class InicioRoles extends JFrame {

    private static final long serialVersionUID = 1L;

    private CardLayout cardLayout;
    private JPanel contenedorPanel;

    private int currentUserId;
    private String nombreLoginActual; 
    private String rolTipoActual;     
    
    private String panelDeRolOrigen; 
    private String currentDisplayedPanelKey; // NUEVA VARIABLE para rastrear el panel visible

    private Tomar_pedido tomarpedidoPanel;
    private Detalles_pedido detallesPedidoPanel; 
    private Nuevo_usuario nuevoUsuarioPanel;
    private Control_ventas controlVentasPanel; 

    private JComboBox<String> usuariosCombo; 
    private JPasswordField passwordField;
    
    private Gestion_menu gestionMenuPanel; // <--- NUEVA DECLARACIÓN: Variable para mantener la referencia a Gestion_menu
    private Inventario inventarioPanel;

    private final UsuarioDao usuarioDao = new UsuarioDao();

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("No se pudo aplicar FlatLaf: " + ex);
        }

        EventQueue.invokeLater(() -> new InicioRoles().setVisible(true));
    }

    public InicioRoles() {
        setTitle("Restaurante App");

        limpiarPedidosAnteriores(); // Limpiar al inicio de la aplicación
        limpiarMenuDelDiaInicial(); // Limpiar al inicio de la aplicación

        initComponents();
        cargarUsuarios();
    }

    /**
     * Obtiene la ruta base de la carpeta 'imagenes' del proyecto.
     * Se asume que 'imagenes' está al mismo nivel que la carpeta 'src'.
     * @return La ruta absoluta al directorio 'imagenes'.
     */
    private static String getRutaBaseImagenes() {
        String appDir = System.getProperty("user.dir");
        return appDir + File.separator + "imagenes";
    }

    private void initComponents() {
        cardLayout = new CardLayout();
        contenedorPanel = new JPanel(cardLayout);
        setContentPane(contenedorPanel);

        contenedorPanel.add(crearLoginPanel(), "Login");

        contenedorPanel.add(new Administrador(this, "Login"), "Administrador"); 
        contenedorPanel.add(new Mesero(this, "Login"), "Mesero");         
        contenedorPanel.add(new Mesero_Auxiliar(this, "Login"), "Mesero Auxiliar"); 
        contenedorPanel.add(new Cocinero(this, "Login"), "Cocinero");
        
        contenedorPanel.add(new Gestion_menu(this), "Gestion_menu"); 

        controlVentasPanel = new Control_ventas(this); 
        controlVentasPanel.setName("Control_ventas");
        contenedorPanel.add(controlVentasPanel, "Control_ventas");

        tomarpedidoPanel = new Tomar_pedido(currentUserId, controlVentasPanel, this);
        contenedorPanel.add(tomarpedidoPanel, "Tomar_pedido");

        detallesPedidoPanel = new Detalles_pedido(this, "Login"); 
        detallesPedidoPanel.setName("Detalles_pedido"); 
        contenedorPanel.add(detallesPedidoPanel, "Detalles_pedido");

        contenedorPanel.add(new Inventario(this), "Inventario"); 

        nuevoUsuarioPanel = new Nuevo_usuario(this, "Administrador"); 
        contenedorPanel.add(nuevoUsuarioPanel, "Nuevo_usuario"); 


        setSize(360, 640); // Tamaño inicial al iniciar la aplicación (corresponde al login)
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Importante: Manejaremos el cierre manualmente

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Obtener la clave del panel actualmente visible
                String activePanelName = currentDisplayedPanelKey; 
                System.out.println("DEBUG: Se intentó cerrar la ventana desde el panel: " + activePanelName);

                if ("Administrador".equals(activePanelName)) {
                    int op = JOptionPane.showConfirmDialog(
                        InicioRoles.this,
                        "¿Deseas salir? Se reiniciará el menú del día y los pedidos.",
                        "Confirmar salida y reinicio",
                        JOptionPane.YES_NO_OPTION
                    );
                    if (op == JOptionPane.YES_OPTION) {
                        limpiarPedidosAnteriores(); 
                        limpiarMenuDelDiaInicial(); 
                        dispose(); // Cerrar la aplicación
                    }
                } else if ("Login".equals(activePanelName)) {
                    // Si se intenta cerrar desde la pantalla de login, simplemente cierra la aplicación
                    dispose();
                } else {
                    // Para cualquier otro rol o subpanel, volver a la pantalla de login sin reiniciar nada
                    int op = JOptionPane.showConfirmDialog(
                        InicioRoles.this,
                        "¿Deseas volver a la pantalla de inicio de sesión?",
                        "Confirmar salida de rol",
                        JOptionPane.YES_NO_OPTION
                    );
                    if (op == JOptionPane.YES_OPTION) {
                        volverAlPanelDeRol(); // Volver al login y ajustar tamaño
                    }
                }
            }
        });

        mostrarPanel("Login"); // Muestra el panel de login al iniciar
    }

    /**
     * Lógica para limpiar pedidos anteriores de la base de datos.
     * Esto solo se llamará cuando el Administrador decide reiniciar el día.
     */
    private void limpiarPedidosAnteriores() {
        try (Connection conn = Conexion_BaseDatos.conectar();
             java.sql.Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("DELETE FROM detalle_pedido");
            stmt.executeUpdate("DELETE FROM pedidos");

            System.out.println("DEBUG: Pedidos anteriores eliminados por Administrador.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("ERROR: No se pudieron eliminar los pedidos anteriores: " + e.getMessage());
        }
    }

    /**
     * Lógica para limpiar el menú del día de la base de datos.
     * Esto solo se llamará cuando el Administrador decide reiniciar el día.
     */
    private void limpiarMenuDelDiaInicial() {
        try {
            new ProductoDao().limpiarMenuDelDia();
            System.out.println("DEBUG: Menú del día reiniciado por Administrador.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("ERROR: No se pudo reiniciar el menú del día: " + e.getMessage());
        }
    }

    public void mostrarPanel(String clave) {
        this.currentDisplayedPanelKey = clave; // ¡ACTUALIZA LA VARIABLE DEL PANEL VISIBLE!

        if ("Tomar_pedido".equals(clave)) {
            if (tomarpedidoPanel != null) {
                tomarpedidoPanel.setUserId(currentUserId); 
                tomarpedidoPanel.cargarMenuDelDia();
            }
        }

        if ("Detalles_pedido".equals(clave)) {
            if (detallesPedidoPanel != null) {
                detallesPedidoPanel.setPanelRegreso(capitalize(this.rolTipoActual)); 
                detallesPedidoPanel.cargarPedidos(); 
            }
        }
        
        if ("Nuevo_usuario".equals(clave)) {
            if (nuevoUsuarioPanel != null) {
                 nuevoUsuarioPanel.setPanelRegreso(capitalize(this.rolTipoActual)); 
                 nuevoUsuarioPanel.cargarUsuariosParaEliminar(); 
            }
        }

        if ("Control_ventas".equals(clave)) {
            if (controlVentasPanel != null) {
                controlVentasPanel.setPanelRegreso(capitalize(this.rolTipoActual));
                controlVentasPanel.actualizarDatosVentas(); 
            }
        }
        
        cardLayout.show(contenedorPanel, clave);
        setLocationRelativeTo(null);
    }


    private JPanel crearLoginPanel() {
        try {
            UIManager.put("Panel.background", new Color(255, 248, 240)); 
            UIManager.put("Label.foreground", new Color(60, 44, 30)); 
            UIManager.put("ComboBox.background", Color.WHITE);
            UIManager.put("ComboBox.foreground", new Color(60, 44, 30));
            UIManager.put("ComboBox.borderColor", new Color(219, 168, 86)); 
            UIManager.put("ComboBox.buttonBackground", new Color(219, 168, 86));
            UIManager.put("PasswordField.background", Color.WHITE);
            UIManager.put("PasswordField.foreground", new Color(60, 44, 30));
            UIManager.put("PasswordField.borderColor", new Color(219, 168, 86));
            UIManager.put("PasswordField.caretColor", new Color(219, 168, 86));
            UIManager.put("Button.arc", 15);
            UIManager.put("Button.background", new Color(219, 168, 86)); 
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Button.hoverBackground", new Color(235, 180, 95)); 
            UIManager.put("Button.default.focusedBackground", new Color(235, 180, 95));
            UIManager.put("Component.focusWidth", 1);
            UIManager.put("Component.focusColor", new Color(219, 168, 86));
            
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(255, 248, 240)); 

        JPanel headerPanel = new JPanel();
        headerPanel.setBounds(0, 0, 360, 180);
        headerPanel.setLayout(null); 
        panel.add(headerPanel);
        
        // --- INICIO: Adición de la Imagen de Fondo ---
        JLabel backgroundLabel = new JLabel();
        backgroundLabel.setBounds(0, 0, 360, 180); 
        backgroundLabel.setHorizontalAlignment(SwingConstants.CENTER);
        backgroundLabel.setVerticalAlignment(SwingConstants.CENTER);
        backgroundLabel.setOpaque(true); 

        String imageFileName = "RestauranteTrezEsquinas.png"; 
        String imagePath = getRutaBaseImagenes() + File.separator + imageFileName;
        File imageFile = new File(imagePath);

        System.out.println("DEBUG (InicioRoles - Login Panel): Ruta de la imagen para el fondo: " + imageFile.getAbsolutePath());

        if (imageFile.exists()) {
            System.out.println("DEBUG (InicioRoles - Login Panel): Archivo de imagen '" + imageFileName + "' encontrado. Intentando cargar y escalar...");
            ImageIcon originalIcon = new ImageIcon(imageFile.getAbsolutePath());
            Image originalImage = originalIcon.getImage();

            if (originalImage != null && originalIcon.getImageLoadStatus() == java.awt.MediaTracker.COMPLETE) {
                System.out.println("DEBUG (InicioRoles - Login Panel): Imagen cargada exitosamente. Dimensiones originales: " + originalImage.getWidth(null) + "x" + originalImage.getHeight(null));
                Image scaledImage = originalImage.getScaledInstance(headerPanel.getWidth(), headerPanel.getHeight(), Image.SCALE_SMOOTH);
                backgroundLabel.setIcon(new ImageIcon(scaledImage));
                backgroundLabel.setText(""); 
                backgroundLabel.setBackground(new Color(0,0,0,0)); 
            } else {
                System.err.println("ERROR (InicioRoles - Login Panel): La imagen se encontró pero no se cargó completamente o es inválida. Estado: " + originalIcon.getImageLoadStatus());
                backgroundLabel.setText("IMAGEN NO CARGADA O INVALIDA");
                backgroundLabel.setFont(new Font("Arial", Font.BOLD, 14));
                backgroundLabel.setForeground(Color.WHITE); 
                backgroundLabel.setBackground(Color.RED); 
            }
        } else {
            System.err.println("ERROR (InicioRoles - Login Panel): Archivo de imagen '" + imageFileName + "' NO encontrado en la ruta: " + imageFile.getAbsolutePath());
            backgroundLabel.setText("IMAGEN NO ENCONTRADA");
            backgroundLabel.setFont(new Font("Arial", Font.BOLD, 14));
            backgroundLabel.setForeground(Color.WHITE); 
            backgroundLabel.setBackground(Color.ORANGE); 
        }
        headerPanel.add(backgroundLabel); 
        // --- FIN: Adición de la Imagen de Fondo ---

        JPanel loginContainer = new JPanel();
        loginContainer.setLayout(null);
        loginContainer.setBounds(40, 150, 280, 240);
        loginContainer.setBackground(Color.WHITE);
        loginContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(240, 240, 240), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        loginContainer.putClientProperty("JPanel.shadow", true);
        
        JLabel lblTituloForm = new JLabel("ACCESO AL SISTEMA", SwingConstants.CENTER);
        lblTituloForm.setBounds(0, 0, 240, 30);
        lblTituloForm.setFont(new Font("Arial", Font.BOLD, 16));
        lblTituloForm.setForeground(new Color(60, 44, 30));
        loginContainer.add(lblTituloForm);

        JLabel lblRol = new JLabel("NOMBRE DE USUARIO (LOGIN)");
        lblRol.setBounds(10, 40, 220, 20);
        lblRol.setFont(new Font("Arial", Font.PLAIN, 12));
        lblRol.setForeground(new Color(120, 120, 120));
        loginContainer.add(lblRol);

        usuariosCombo = new JComboBox<>();
        usuariosCombo.setBounds(10, 60, 220, 35);
        usuariosCombo.setFont(new Font("Arial", Font.PLAIN, 12));
        loginContainer.add(usuariosCombo);

        JLabel lblPass = new JLabel("CONTRASEÑA");
        lblPass.setBounds(10, 105, 220, 20);
        lblPass.setFont(new Font("Arial", Font.PLAIN, 12));
        lblPass.setForeground(new Color(120, 120, 120));
        loginContainer.add(lblPass);

        passwordField = new JPasswordField();
        passwordField.setBounds(10, 125, 220, 35);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 12));
        loginContainer.add(passwordField);

        JButton btnLogin = new JButton("INICIAR SESIÓN");
        btnLogin.setBounds(10, 180, 220, 40);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
        loginContainer.add(btnLogin);
        btnLogin.addActionListener(e -> autenticarUsuario());
        
        panel.add(loginContainer);

        JLabel lblFooter = new JLabel("© 2023 La Esquina - Todos los derechos reservados", SwingConstants.CENTER);
        lblFooter.setBounds(0, 400, 360, 20);
        lblFooter.setFont(new Font("Arial", Font.PLAIN, 10));
        lblFooter.setForeground(new Color(150, 150, 150));
        panel.add(lblFooter);

        return panel;
    }

    private void cargarUsuarios() {
        try {
            List<String> nombresLogin = usuarioDao.listarNombres();
            usuariosCombo.removeAllItems();
            nombresLogin.forEach(usuariosCombo::addItem);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "No se pudo cargar la lista de usuarios.\n" + e.getMessage(),
                "Error BD",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void autenticarUsuario() {
        String nombreLogin = (String) usuariosCombo.getSelectedItem(); 
        String contrasenaInput = new String(passwordField.getPassword()).trim();

        if (nombreLogin == null || nombreLogin.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona un usuario.", "Error de Login", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (contrasenaInput.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingresa la contraseña.", "Error de Login", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Usuario usuarioAutenticado = usuarioDao.validarCredenciales(nombreLogin, contrasenaInput);

            if (usuarioAutenticado != null) {
                this.currentUserId = usuarioAutenticado.getIdUsuario();
                this.nombreLoginActual = usuarioAutenticado.getNombreLogin(); 
                this.rolTipoActual = usuarioAutenticado.getRolTipo();       

                this.panelDeRolOrigen = "Login"; 

                if (tomarpedidoPanel != null) {
                    tomarpedidoPanel.setUserId(this.currentUserId);
                    tomarpedidoPanel.cargarMenuDelDia(); 
                }
                
                cambiarPanelPorRol(this.rolTipoActual); 

                JOptionPane.showMessageDialog(this, "Bienvenido, " + capitalize(this.nombreLoginActual) + "!", "Inicio de Sesión Exitoso", JOptionPane.INFORMATION_MESSAGE);

            } else {
                JOptionPane.showMessageDialog(this, "Credenciales incorrectas.", "Error de Login", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Error al autenticar usuario.\n" + e.getMessage(),
                "Error de BD",
                JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace(); 
        } finally {
            passwordField.setText(""); 
        }
    }

    private void cambiarPanelPorRol(String rolTipo) {
        String rolMinus = rolTipo.toLowerCase();
        
        switch (rolMinus) {
            case "administrador":
                setSize(375, 678); 
                mostrarPanel("Administrador");
                break;

            case "mesero":
                setSize(375, 678);
                mostrarPanel("Mesero");
                break;

            case "mesero auxiliar":
                setSize(800, 600);
                mostrarPanel("Mesero Auxiliar");
                break;

            case "cocinero":
                setSize(800, 600);
                mostrarPanel("Cocinero");
                break;

            default:
                JOptionPane.showMessageDialog(this, "Tipo de rol no reconocido: " + rolTipo, "Error de Rol", JOptionPane.ERROR_MESSAGE);
                return;
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        String lowerCaseStr = str.toLowerCase(); 
        return Character.toUpperCase(lowerCaseStr.charAt(0)) + lowerCaseStr.substring(1); 
    }

    public Component obtenerPanel(String clave) {
        for (Component comp : contenedorPanel.getComponents()) {
            if (comp.getName() != null && comp.getName().equals(clave)) {
                return comp;
            }
        }
        return null;
    }

    public int getCurrentUserId() {
        return currentUserId;
    }

    public String getNombreLoginActual() {
        return nombreLoginActual;
    }

    public String getRolActual() { 
        return rolTipoActual;
    }
    
    public void volverAlPanelDeRol() {
        setSize(360, 640); 
        setLocationRelativeTo(null); 
        usuariosCombo.setSelectedIndex(0);
        mostrarPanel("Login");
    }
    
    public void refrescarGestionMenu() {
        if (gestionMenuPanel != null) {
            System.out.println("DEBUG (InicioRoles): Llamando a refrescar Gestion_menu...");
            gestionMenuPanel.cargarProductosDelMenu();
        } else {
            System.err.println("ERROR (InicioRoles): El panel Gestion_menu no está inicializado. No se puede refrescar.");
        }
    }
}