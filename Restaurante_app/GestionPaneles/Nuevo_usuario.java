package Restaurante_app.GestionPaneles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

import Restaurante_app.InicioRoles;
import Restaurante_app.DAO.UsuarioDao;
import Restaurante_app.Model.Usuario;

public class Nuevo_usuario extends JPanel {

    private static final long serialVersionUID = 1L;
    private JTextField txtContrasena;
    private JTextField txtNuevoUsuarioLogin;
    private JComboBox<String> cmbAsignarRolTipo;
    private JComboBox<String> cmbEliminarUsuario;
    private InicioRoles mainApp;
    private String panelRegreso;
    
    private JButton btnEliminarUsuario; 

    private UsuarioDao usuarioDAO = new UsuarioDao();

    // ¡IMPORTANTE! Estos deben ser los NOMBRES DE LOGIN EXACTOS de tus usuarios iniciales protegidos
    private final String[] NOMBRES_LOGIN_PROTEGIDOS = {"Administrador_1", "Mesero_2", "Cocinero_3", "Mesero_Auxiliar_4"}; 

    public Nuevo_usuario(InicioRoles mainApp, String panelRegreso) {
        this.mainApp = mainApp;
        this.panelRegreso = panelRegreso;

        setBackground(new Color(255, 248, 240));
        setPreferredSize(new Dimension(360, 640));
        setLayout(new BorderLayout(0, 0)); 

        // --- Panel Superior (Botón VOLVER) ---
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(255, 248, 240));
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        JButton btnVolver = new JButton("← VOLVER");
        btnVolver.setFont(new Font("Arial", Font.BOLD, 12));
        btnVolver.setBackground(new Color(219, 168, 86));
        btnVolver.setForeground(Color.WHITE);
        btnVolver.setFocusPainted(false);
        btnVolver.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnVolver.addActionListener(e -> mainApp.mostrarPanel(this.panelRegreso));
        topPanel.add(btnVolver);
        add(topPanel, BorderLayout.NORTH);

        // --- Panel Central para contener las dos secciones (Crear y Eliminar) ---
        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setBackground(new Color(255, 248, 240));
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.Y_AXIS));
        mainContentPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); 

        // --- SECCIÓN: CREAR NUEVO USUARIO ---
        JPanel crearUsuarioPanel = new JPanel();
        crearUsuarioPanel.setBackground(new Color(255, 248, 240));
        crearUsuarioPanel.setLayout(new SpringLayout());
        crearUsuarioPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(180, 140, 100), 1), 
            " Crear Nuevo Usuario ", 
            TitledBorder.CENTER, 
            TitledBorder.TOP, 
            new Font("Arial", Font.BOLD, 18), 
            new Color(60, 44, 30) 
        ));
        crearUsuarioPanel.setMaximumSize(new Dimension(340, 260)); 
        crearUsuarioPanel.setPreferredSize(new Dimension(340, 260));


        // Componentes para Crear Usuario
        JLabel lblNuevoUsuarioLogin = new JLabel("Nombre de Usuario (Login):");
        styleLabel(lblNuevoUsuarioLogin);
        txtNuevoUsuarioLogin = createStyledTextField();

        JLabel lblAsignarRolTipo = new JLabel("Asignar Tipo de Rol:");
        styleLabel(lblAsignarRolTipo);
        String[] tiposDeRoles = {"Administrador", "Mesero", "Mesero Auxiliar", "Cocinero"};
        cmbAsignarRolTipo = createStyledComboBox(tiposDeRoles);

        JLabel lblContrasena = new JLabel("Contraseña:");
        styleLabel(lblContrasena);
        txtContrasena = createStyledTextField();

        JButton btnCrearUsuario = new JButton("CREAR USUARIO");
        btnCrearUsuario.setFont(new Font("Arial", Font.BOLD, 16));
        btnCrearUsuario.setBackground(new Color(80, 180, 100)); 
        btnCrearUsuario.setForeground(Color.WHITE);
        btnCrearUsuario.setFocusPainted(false);
        btnCrearUsuario.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        btnCrearUsuario.addActionListener(e -> crearNuevoUsuario());

        // Añadir componentes al panel de Crear Usuario
        crearUsuarioPanel.add(lblNuevoUsuarioLogin);
        crearUsuarioPanel.add(txtNuevoUsuarioLogin);
        crearUsuarioPanel.add(lblAsignarRolTipo);
        crearUsuarioPanel.add(cmbAsignarRolTipo);
        crearUsuarioPanel.add(lblContrasena);
        crearUsuarioPanel.add(txtContrasena);
        
        // Panel para el botón de Crear Usuario
        JPanel btnCrearPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnCrearPanel.setBackground(new Color(255, 248, 240));
        btnCrearPanel.add(btnCrearUsuario);
        crearUsuarioPanel.add(btnCrearPanel); 

        // Add a dummy component to fill the last spot for makeCompactGrid if there's an odd number
        // (7 components total -> needs 8 for a perfect 4x2 grid).
        // This is a workaround to satisfy the current `SpringUtilities.makeCompactGrid` implementation
        // which expects `rows * cols` exact components.
        crearUsuarioPanel.add(new JPanel() { // Invisible dummy panel
            @Override
            public Dimension getPreferredSize() { return new Dimension(0, 0); }
            @Override
            public Dimension getMinimumSize() { return new Dimension(0, 0); }
            @Override
            public Dimension getMaximumSize() { return new Dimension(Short.MAX_VALUE, Short.MAX_VALUE); }
        });


        // APLICAR SpringLayout para organizar las filas y columnas
        // Now, with the dummy panel, crearUsuarioPanel has 8 components.
        // So 4 rows, 2 columns works perfectly.
        SpringUtilities.makeCompactGrid(crearUsuarioPanel,
                                        4, 2, 
                                        10, 10, 
                                        10, 10); 
        
        // --- SECCIÓN: ELIMINAR USUARIO ---
        JPanel eliminarUsuarioPanel = new JPanel();
        eliminarUsuarioPanel.setBackground(new Color(255, 248, 240));
        eliminarUsuarioPanel.setLayout(new SpringLayout());
        eliminarUsuarioPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(180, 140, 100), 1),
            " Eliminar Usuario Existente ",
            TitledBorder.CENTER,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 18),
            new Color(60, 44, 30)
        ));
        eliminarUsuarioPanel.setMaximumSize(new Dimension(340, 180)); 
        eliminarUsuarioPanel.setPreferredSize(new Dimension(340, 180));


        // Componentes para Eliminar Usuario
        JLabel lblSeleccionarUsuario = new JLabel("Seleccione Usuario a Eliminar:");
        styleLabel(lblSeleccionarUsuario);
        cmbEliminarUsuario = createStyledComboBox(new String[]{}); 

        btnEliminarUsuario = new JButton("ELIMINAR USUARIO");
        btnEliminarUsuario.setFont(new Font("Arial", Font.BOLD, 16));
        btnEliminarUsuario.setBackground(new Color(200, 80, 80)); 
        btnEliminarUsuario.setForeground(Color.WHITE);
        btnEliminarUsuario.setFocusPainted(false);
        btnEliminarUsuario.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        btnEliminarUsuario.addActionListener(e -> eliminarUsuarioSeleccionado());

        // Añadir componentes al panel de Eliminar Usuario
        eliminarUsuarioPanel.add(lblSeleccionarUsuario);
        eliminarUsuarioPanel.add(cmbEliminarUsuario);
        
        // Panel para el botón de Eliminar Usuario
        JPanel btnEliminarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnEliminarPanel.setBackground(new Color(255, 248, 240));
        btnEliminarPanel.add(btnEliminarUsuario);
        eliminarUsuarioPanel.add(btnEliminarPanel);

        // Add a dummy component to fill the last spot for makeCompactGrid if there's an odd number
        // (3 components total -> needs 4 for a perfect 2x2 grid).
        eliminarUsuarioPanel.add(new JPanel() { // Invisible dummy panel
            @Override
            public Dimension getPreferredSize() { return new Dimension(0, 0); }
            @Override
            public Dimension getMinimumSize() { return new Dimension(0, 0); }
            @Override
            public Dimension getMaximumSize() { return new Dimension(Short.MAX_VALUE, Short.MAX_VALUE); }
        });

        // APLICAR SpringLayout para organizar las filas y columnas
        // Now, with the dummy panel, eliminarUsuarioPanel has 4 components.
        // So 2 rows, 2 columns works perfectly.
        SpringUtilities.makeCompactGrid(eliminarUsuarioPanel,
                                        2, 2, 
                                        10, 10, 
                                        10, 10); 

        // Añadir los subpaneles al panel de contenido principal
        mainContentPanel.add(crearUsuarioPanel);
        mainContentPanel.add(Box.createVerticalStrut(20)); 
        mainContentPanel.add(eliminarUsuarioPanel);
        mainContentPanel.add(Box.createVerticalGlue()); 

        // Añadir el panel de contenido principal al centro del BorderLayout
        add(mainContentPanel, BorderLayout.CENTER);
    }

    // --- Métodos de utilidad para estilizar componentes ---
    private void styleLabel(JLabel label) {
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(60, 44, 30));
    }

    private JTextField createStyledTextField() {
        JTextField textField = new JTextField(20);
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setBackground(Color.WHITE);
        textField.setForeground(new Color(60, 44, 30));
        textField.setPreferredSize(new Dimension(200, 35));
        textField.setBorder(BorderFactory.createLineBorder(new Color(180, 140, 100), 1));
        return textField;
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(new Color(60, 44, 30));
        comboBox.setPreferredSize(new Dimension(200, 35));
        comboBox.setBorder(BorderFactory.createLineBorder(new Color(180, 140, 100), 1));
        return comboBox;
    }

    // --- Métodos de lógica de negocio ---

    public void setPanelRegreso(String panelRegreso) {
        this.panelRegreso = panelRegreso;
    }

    private void crearNuevoUsuario() {
        String nombreLogin = txtNuevoUsuarioLogin.getText().trim(); 
        String contrasena = txtContrasena.getText().trim();
        String rolTipoAsignado = (String) cmbAsignarRolTipo.getSelectedItem(); 

        if (nombreLogin.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese un nombre de usuario (login).", "Campo Vacío", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (contrasena.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese una contraseña.", "Campo Vacío", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (contrasena.length() < 4) {
            JOptionPane.showMessageDialog(this, "La contraseña debe tener al menos 4 caracteres.", "Contraseña Corta", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Usuario nuevoUsuario = new Usuario(nombreLogin, contrasena, rolTipoAsignado); 

        try {
            List<String> nombresLoginExistentes = usuarioDAO.listarNombres(); 
            if (nombresLoginExistentes.contains(nombreLogin)) { 
                JOptionPane.showMessageDialog(this, "Ya existe un usuario con el nombre de login '" + nombreLogin + "'. Por favor, elija otro nombre.", "Error de Creación", JOptionPane.ERROR_MESSAGE);
                return;
            }

            for(String usuarioProtegido : NOMBRES_LOGIN_PROTEGIDOS) {
                if(nombreLogin.equalsIgnoreCase(usuarioProtegido)) {
                    JOptionPane.showMessageDialog(this, "El nombre de login '" + nombreLogin + "' está reservado y no puede ser creado.", "Nombre Reservado", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            boolean exito = usuarioDAO.insertarUsuario(nuevoUsuario);
            if (exito) {
                JOptionPane.showMessageDialog(this, "Usuario '" + nombreLogin + "' creado con éxito con rol '" + rolTipoAsignado + "'.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                txtNuevoUsuarioLogin.setText(""); 
                txtContrasena.setText("");
                cmbAsignarRolTipo.setSelectedIndex(0); 
                cargarUsuariosParaEliminar(); 
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo crear el usuario. Inténtelo de nuevo.", "Error de Creación", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error de base de datos al crear usuario: " + e.getMessage(), "Error de BD", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ocurrió un error inesperado: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void cargarUsuariosParaEliminar() { 
        cmbEliminarUsuario.removeAllItems(); 
        try {
            List<String> nombresLoginUsuarios = usuarioDAO.listarNombres(); 
            
            boolean addedAnyUser = false; 
            for (String nombreLogin : nombresLoginUsuarios) {
                boolean esUsuarioProtegido = false;
                for (String usuarioProtegido : NOMBRES_LOGIN_PROTEGIDOS) { 
                    if (nombreLogin.equalsIgnoreCase(usuarioProtegido)) {
                        esUsuarioProtegido = true;
                        break;
                    }
                }
                if (!esUsuarioProtegido) { 
                    cmbEliminarUsuario.addItem(nombreLogin);
                    addedAnyUser = true; 
                }
            }
            
            if (cmbEliminarUsuario.getItemCount() == 0) { 
                cmbEliminarUsuario.addItem("No hay usuarios eliminables");
                if (btnEliminarUsuario != null) { 
                    btnEliminarUsuario.setEnabled(false); 
                }
            } else {
                if (btnEliminarUsuario != null) { 
                    btnEliminarUsuario.setEnabled(true); 
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar la lista de usuarios para eliminar: " + e.getMessage(), "Error de BD", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ocurrió un error inesperado al cargar usuarios: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarUsuarioSeleccionado() {
        String nombreLoginAEliminar = (String) cmbEliminarUsuario.getSelectedItem();

        if (nombreLoginAEliminar == null || nombreLoginAEliminar.isEmpty() || "No hay usuarios eliminables".equals(nombreLoginAEliminar)) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un usuario válido para eliminar.", "Selección Inválida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "¿Está seguro de que desea eliminar al usuario '" + nombreLoginAEliminar + "'?",
            "Confirmar Eliminación", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int idUsuarioAEliminar = usuarioDAO.obtenerIdPorNombre(nombreLoginAEliminar);
                
                if (mainApp.getNombreLoginActual() != null && mainApp.getNombreLoginActual().equalsIgnoreCase(nombreLoginAEliminar)) { 
                     JOptionPane.showMessageDialog(this, "No puedes eliminar tu propio usuario mientras estás logueado.", "Error de Eliminación", JOptionPane.ERROR_MESSAGE);
                     return;
                }
                
                boolean esUsuarioProtegido = false;
                for (String usuarioProtegido : NOMBRES_LOGIN_PROTEGIDOS) {
                    if (nombreLoginAEliminar.equalsIgnoreCase(usuarioProtegido)) {
                        esUsuarioProtegido = true;
                        break;
                    }
                }
                if (esUsuarioProtegido) {
                    JOptionPane.showMessageDialog(this, "El usuario '" + nombreLoginAEliminar + "' es un usuario predefinido/protegido y no puede ser eliminado.", "Error de Eliminación", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                boolean exito = usuarioDAO.eliminarUsuario(idUsuarioAEliminar);
                if (exito) {
                    JOptionPane.showMessageDialog(this, "Usuario '" + nombreLoginAEliminar + "' eliminado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    cargarUsuariosParaEliminar(); 
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar el usuario. Inténtelo de nuevo.", "Error de Eliminación", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error de base de datos al eliminar usuario: " + e.getMessage(), "Error de BD", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Ocurrió un error inesperado al eliminar usuario: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Nested static utility class for SpringLayout.
     * This version is robust to handle incomplete last rows by checking component existence.
     */
    static class SpringUtilities {
        /**
         * A debugging utility that prints the sizes of components.
         */
        public static void printSizes(Component c) {
            System.out.println("Minimum size of " + c.getClass().getName() + ": " + c.getMinimumSize());
            System.out.println("Preferred size of " + c.getClass().getName() + ": " + c.getPreferredSize());
            System.out.println("Maximum size of " + c.getClass().getName() + ": " + c.getMaximumSize());
        }

        /**
         * Aligns the components of `parent` in a grid.
         *
         * @param parent The container whose components are to be laid out in a grid.
         * @param rows   The number of rows in the grid.
         * @param cols   The number of columns in the grid.
         * @param initialX The x location of the first component.
         * @param initialY The y location of the first component.
         * @param xPad     The x padding between cells.
         * @param yPad     The y padding between cells.
         */
        public static void makeCompactGrid(Container parent,
                                           int rows, int cols,
                                           int initialX, int initialY,
                                           int xPad, int yPad) {
            SpringLayout layout;
            try {
                layout = (SpringLayout)parent.getLayout();
            } catch (ClassCastException exc) {
                System.err.println("The first argument to makeCompactGrid must use SpringLayout.");
                return;
            }

            int numComponents = parent.getComponentCount();

            // Align all cells in each column and make them the same width.
            Spring x = Spring.constant(initialX);
            for (int c = 0; c < cols; c++) {
                Spring width = Spring.constant(0);
                for (int r = 0; r < rows; r++) {
                    int index = r * cols + c;
                    if (index < numComponents) { // Check if component exists
                        SpringLayout.Constraints constraints = layout.getConstraints(parent.getComponent(index));
                        width = Spring.max(width, constraints.getWidth());
                    }
                }
                for (int r = 0; r < rows; r++) {
                    int index = r * cols + c;
                    if (index < numComponents) { // Check if component exists
                        SpringLayout.Constraints constraints = layout.getConstraints(parent.getComponent(index));
                        constraints.setX(x);
                        constraints.setWidth(width);
                    }
                }
                x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
            }

            // Align all cells in each row and make them the same height.
            Spring y = Spring.constant(initialY);
            for (int r = 0; r < rows; r++) {
                Spring height = Spring.constant(0);
                for (int c = 0; c < cols; c++) {
                    int index = r * cols + c;
                    if (index < numComponents) { // Check if component exists
                        SpringLayout.Constraints constraints = layout.getConstraints(parent.getComponent(index));
                        height = Spring.max(height, constraints.getHeight());
                    }
                }
                for (int c = 0; c < cols; c++) {
                    int index = r * cols + c;
                    if (index < numComponents) { // Check if component exists
                        SpringLayout.Constraints constraints = layout.getConstraints(parent.getComponent(index));
                        constraints.setY(y);
                        constraints.setHeight(height);
                    }
                }
                y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
            }

            //Set the parent's size.
            SpringLayout.Constraints pCons = layout.getConstraints(parent);
            pCons.setConstraint(SpringLayout.EAST, x);
            pCons.setConstraint(SpringLayout.SOUTH, y);
        }
    }
}