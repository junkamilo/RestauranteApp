package Restaurante_app.GestionPaneles;

import Restaurante_app.DAO.ProductoDao;
import Restaurante_app.Model.Producto;
import Restaurante_app.InicioRoles;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.UUID;

public class Inventario extends JPanel {
    private JTextField txtNombre, txtPrecio;
    private JTextArea txtDescripcion;
    private JComboBox<String> comboCategoriaAgregar, comboCategoriaModificar;
    private JTable tablaModificar, tablaEliminar;
    private DefaultTableModel modeloModificar, modeloEliminar;
    private ProductoDao productoDao;

    private InicioRoles mainApp;
    private String panelRegreso;

    private JLabel lblPreviewImagen;
    private String rutaImagenTemporalParaGuardar = null;

    private static final Color PRIMARY_BG_COLOR = new Color(255, 248, 240);
    private static final Color ACCENT_COLOR = new Color(180, 140, 100);
    private static final Color DARK_TEXT_COLOR = new Color(60, 44, 30);
    private static final Color LIGHT_TEXT_COLOR = new Color(100, 100, 100);
    private static final Color BUTTON_ADD_COLOR = new Color(80, 180, 100);
    private static final Color BUTTON_MODIFY_COLOR = new Color(219, 168, 86);
    private static final Color BUTTON_DELETE_COLOR = new Color(200, 70, 70);
    private static final Color BUTTON_FILTER_COLOR = new Color(70, 130, 180);
    private static final Color BUTTON_SELECT_IMAGE_COLOR = new Color(100, 150, 200);
    private static final Color CARD_BG_COLOR = new Color(255, 253, 245);
    private static final Color SELECTED_CARD_COLOR = new Color(230, 245, 230);

    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 20);
    private static final Font SUBTITLE_FONT = new Font("Arial", Font.BOLD, 15);
    private static final Font LABEL_FONT = new Font("Arial", Font.BOLD, 12);
    private static final Font INPUT_FONT = new Font("Arial", Font.PLAIN, 13);
    private static final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 13);
    private static final Font TABLE_HEADER_FONT = new Font("Arial", Font.BOLD, 12);
    private static final Font TABLE_CELL_FONT = new Font("Arial", Font.PLAIN, 11);

    public Inventario(InicioRoles mainApp, String panelRegreso) {
        this.mainApp = mainApp;
        this.panelRegreso = panelRegreso;
        initComponents();
    }

    public Inventario(InicioRoles mainApp) {
        this(mainApp, "Administrador");
    }

    public Inventario() {
        this(null, "Login");
    }

    private static String getRutaBaseImagenes() {
        String appDir = System.getProperty("user.dir");
        return appDir + File.separator + "imagenes";
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(PRIMARY_BG_COLOR);
        setPreferredSize(new Dimension(375, 678));

        productoDao = new ProductoDao();

        JLabel titulo = new JLabel("Gestión de Inventario", SwingConstants.CENTER);
        titulo.setFont(TITLE_FONT);
        titulo.setForeground(DARK_TEXT_COLOR);
        titulo.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        add(titulo, BorderLayout.NORTH);

        JTabbedPane pestañas = new JTabbedPane();
        pestañas.setFont(SUBTITLE_FONT);
        pestañas.setBackground(PRIMARY_BG_COLOR);
        pestañas.setForeground(DARK_TEXT_COLOR);
        pestañas.putClientProperty("JTabbedPane.tabAreaAlignment", "center");
        pestañas.putClientProperty("JTabbedPane.selectedTabPad", 5);
        pestañas.putClientProperty("JTabbedPane.tabHeight", 35);

        pestañas.addTab("Agregar Productos", crearPanelAgregar());
        pestañas.addTab("Modificar Productos", crearPanelModificar());
        pestañas.addTab("Eliminar Productos", crearPanelEliminar());
        
        pestañas.addChangeListener(e -> {
            int selected = pestañas.getSelectedIndex();
            if (selected == 1) { // Modificar Productos
                cargarTablaModificar("Todos");
            } else if (selected == 2) { // Eliminar Productos
                cargarTablaEliminar();
            }
        });

        add(pestañas, BorderLayout.CENTER);

        JButton btnVolver = new JButton("← Volver al Menú Principal");
        btnVolver.setFont(BUTTON_FONT);
        btnVolver.setForeground(DARK_TEXT_COLOR);
        btnVolver.setBackground(new Color(220, 220, 220));
        btnVolver.setFocusPainted(false);
        btnVolver.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnVolver.putClientProperty("JButton.buttonType", "roundRect"); 

        btnVolver.addActionListener(e -> {
            if (mainApp != null && panelRegreso != null) {
                mainApp.mostrarPanel(panelRegreso);
            } else {
                JOptionPane.showMessageDialog(this, "No se puede volver. Falta la configuración de navegación.", "Error de Navegación", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelSur.setBackground(PRIMARY_BG_COLOR);
        panelSur.add(btnVolver);
        add(panelSur, BorderLayout.SOUTH);
    }

    private JPanel crearPanelAgregar() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        panel.setBackground(PRIMARY_BG_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 3, 6, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL; 

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel lblNombre = new JLabel("Nombre del Plato:");
        lblNombre.setFont(LABEL_FONT);
        lblNombre.setForeground(DARK_TEXT_COLOR);
        panel.add(lblNombre, gbc);

        gbc.gridy = 1;
        txtNombre = new JTextField(20);
        txtNombre.setFont(INPUT_FONT);
        txtNombre.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_COLOR.darker(), 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        txtNombre.putClientProperty("JTextField.roundRect", true);
        panel.add(txtNombre, gbc);
        
        gbc.gridy = 2;
        JLabel lblCategoria = new JLabel("Categoría:");
        lblCategoria.setFont(LABEL_FONT);
        lblCategoria.setForeground(DARK_TEXT_COLOR);
        panel.add(lblCategoria, gbc);

        gbc.gridy = 3;
        comboCategoriaAgregar = new JComboBox<>(new String[]{"Corriente", "Especial", "Sopa", "Principio"});
        comboCategoriaAgregar.setFont(INPUT_FONT);
        comboCategoriaAgregar.setBackground(Color.WHITE);
        comboCategoriaAgregar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_COLOR.darker(), 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        comboCategoriaAgregar.putClientProperty("JComboBox.roundRect", true);
        panel.add(comboCategoriaAgregar, gbc);
        
        gbc.gridy = 4;
        JLabel lblPrecio = new JLabel("Precio:");
        lblPrecio.setFont(LABEL_FONT);
        lblPrecio.setForeground(DARK_TEXT_COLOR);
        panel.add(lblPrecio, gbc);

        gbc.gridy = 5;
        txtPrecio = new JTextField(10);
        txtPrecio.setFont(INPUT_FONT);
        txtPrecio.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_COLOR.darker(), 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        txtPrecio.putClientProperty("JTextField.roundRect", true);
        panel.add(txtPrecio, gbc);
        
        gbc.gridy = 6;
        JLabel lblDescripcion = new JLabel("Descripción:");
        lblDescripcion.setFont(LABEL_FONT);
        lblDescripcion.setForeground(DARK_TEXT_COLOR);
        panel.add(lblDescripcion, gbc);

        gbc.gridy = 7;
        txtDescripcion = new JTextArea(8, 25);
        txtDescripcion.setFont(INPUT_FONT);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        txtDescripcion.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_COLOR.darker(), 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        JScrollPane scrollDesc = new JScrollPane(txtDescripcion);
        scrollDesc.setPreferredSize(new Dimension(280, 120));
        scrollDesc.setMinimumSize(new Dimension(250, 100));
        scrollDesc.putClientProperty("JScrollPane.roundRect", true);
        gbc.weighty = 0.8;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(scrollDesc, gbc);
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridy = 8;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(12, 3, 3, 3);
        JLabel lblImagenProducto = new JLabel("Imagen (Opcional):");
        lblImagenProducto.setFont(LABEL_FONT);
        lblImagenProducto.setForeground(DARK_TEXT_COLOR);
        panel.add(lblImagenProducto, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        lblPreviewImagen = new JLabel("<html><i>Vista previa<br>sin imagen</i></html>", SwingConstants.CENTER);
        lblPreviewImagen.setPreferredSize(new Dimension(70, 70));
        lblPreviewImagen.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR, 1));
        lblPreviewImagen.setFont(new Font("Arial", Font.ITALIC, 9));
        lblPreviewImagen.setForeground(LIGHT_TEXT_COLOR);
        lblPreviewImagen.setVerticalAlignment(SwingConstants.CENTER);
        lblPreviewImagen.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lblPreviewImagen, gbc);

        gbc.gridy = 9;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 3, 12, 3);

        JButton btnSeleccionarImagen = new JButton("Seleccionar Imagen");
        btnSeleccionarImagen.setFont(BUTTON_FONT);
        btnSeleccionarImagen.setBackground(BUTTON_SELECT_IMAGE_COLOR);
        btnSeleccionarImagen.setForeground(Color.WHITE);
        btnSeleccionarImagen.setFocusPainted(false);
        btnSeleccionarImagen.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnSeleccionarImagen.putClientProperty("JButton.buttonType", "roundRect");
        btnSeleccionarImagen.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Seleccionar imagen del producto");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Imágenes", "png", "jpg", "jpeg", "gif"));

            int userSelection = fileChooser.showOpenDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                rutaImagenTemporalParaGuardar = selectedFile.getAbsolutePath();

                ImageIcon icon = new ImageIcon(rutaImagenTemporalParaGuardar);
                Image imagen = icon.getImage().getScaledInstance(lblPreviewImagen.getWidth(), lblPreviewImagen.getHeight(), Image.SCALE_SMOOTH);
                lblPreviewImagen.setIcon(new ImageIcon(imagen));
                lblPreviewImagen.setText("");
            }
        });
        panel.add(btnSeleccionarImagen, gbc);
        
        gbc.gridy = 10;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 3, 5, 3);

        JButton btnAgregar = new JButton("Agregar Producto");
        btnAgregar.setFont(BUTTON_FONT);
        btnAgregar.setBackground(BUTTON_ADD_COLOR);
        btnAgregar.setForeground(Color.WHITE);
        btnAgregar.setFocusPainted(false);
        btnAgregar.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        btnAgregar.putClientProperty("JButton.buttonType", "roundRect");
        btnAgregar.addActionListener(e -> agregarProducto());
        panel.add(btnAgregar, gbc);
        
        return panel;
    }

    private JPanel crearPanelModificar() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(PRIMARY_BG_COLOR);

        JPanel filtros = new JPanel();
        filtros.setLayout(new BoxLayout(filtros, BoxLayout.Y_AXIS));
        filtros.setBackground(PRIMARY_BG_COLOR);
        filtros.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel lblCategoria = new JLabel("Filtrar por Categoría:");
        lblCategoria.setFont(LABEL_FONT);
        lblCategoria.setForeground(DARK_TEXT_COLOR);
        lblCategoria.setAlignmentX(Component.LEFT_ALIGNMENT);
        filtros.add(lblCategoria);
        filtros.add(Box.createVerticalStrut(5));

        comboCategoriaModificar = new JComboBox<>(new String[]{"Todos", "Corriente", "Especial", "Sopa", "Principio"});
        comboCategoriaModificar.setFont(INPUT_FONT);
        comboCategoriaModificar.setBackground(Color.WHITE);
        comboCategoriaModificar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_COLOR.darker(), 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        comboCategoriaModificar.putClientProperty("JComboBox.roundRect", true);
        comboCategoriaModificar.setAlignmentX(Component.LEFT_ALIGNMENT);
        comboCategoriaModificar.setMaximumSize(new Dimension(250, comboCategoriaModificar.getPreferredSize().height));
        filtros.add(comboCategoriaModificar);
        filtros.add(Box.createVerticalStrut(10));

        JButton btnFiltrar = new JButton("Aplicar Filtro");
        btnFiltrar.setFont(BUTTON_FONT);
        btnFiltrar.setBackground(BUTTON_FILTER_COLOR);
        btnFiltrar.setForeground(Color.WHITE);
        btnFiltrar.setFocusPainted(false);
        btnFiltrar.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnFiltrar.putClientProperty("JButton.buttonType", "roundRect");
        btnFiltrar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnFiltrar.addActionListener(e -> {
            String tipo = (String) comboCategoriaModificar.getSelectedItem();
            cargarTablaModificar(tipo);
        });
        filtros.add(btnFiltrar); 
        panel.add(filtros, BorderLayout.NORTH); 

        modeloModificar = new DefaultTableModel(new String[]{"ID", "Nombre", "Precio", "Descripción", "Categoría", "Imagen", "Disponibilidad", "En Menú Día"}, 0) { 
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 6 || column == 7) return Boolean.class; 
                return super.getColumnClass(column);
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1 || column == 2 || column == 3 || column == 4; 
            }
        };
        tablaModificar = new JTable(modeloModificar);
        tablaModificar.setRowHeight(25);
        tablaModificar.setFont(TABLE_CELL_FONT);
        tablaModificar.getTableHeader().setFont(TABLE_HEADER_FONT);
        tablaModificar.getTableHeader().setBackground(ACCENT_COLOR.brighter());
        tablaModificar.getTableHeader().setForeground(DARK_TEXT_COLOR);
        tablaModificar.setSelectionBackground(SELECTED_CARD_COLOR);
        tablaModificar.setGridColor(ACCENT_COLOR.brighter());
        tablaModificar.setFillsViewportHeight(true);

        tablaModificar.getColumnModel().getColumn(1).setPreferredWidth(80);
        tablaModificar.getColumnModel().getColumn(2).setPreferredWidth(50);
        tablaModificar.getColumnModel().getColumn(3).setPreferredWidth(100);
        tablaModificar.getColumnModel().getColumn(4).setPreferredWidth(60);

        tablaModificar.getColumnModel().getColumn(0).setMinWidth(0); tablaModificar.getColumnModel().getColumn(0).setMaxWidth(0); tablaModificar.getColumnModel().getColumn(0).setPreferredWidth(0);
        tablaModificar.getColumnModel().getColumn(5).setMinWidth(0); tablaModificar.getColumnModel().getColumn(5).setMaxWidth(0); tablaModificar.getColumnModel().getColumn(5).setPreferredWidth(0);
        tablaModificar.getColumnModel().getColumn(6).setMinWidth(0); tablaModificar.getColumnModel().getColumn(6).setMaxWidth(0); tablaModificar.getColumnModel().getColumn(6).setPreferredWidth(0);
        tablaModificar.getColumnModel().getColumn(7).setMinWidth(0); tablaModificar.getColumnModel().getColumn(7).setMaxWidth(0); tablaModificar.getColumnModel().getColumn(7).setPreferredWidth(0);


        JScrollPane scrollPaneTabla = new JScrollPane(tablaModificar);
        scrollPaneTabla.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR, 1));
        scrollPaneTabla.putClientProperty("JScrollPane.roundRect", true);
        panel.add(scrollPaneTabla, BorderLayout.CENTER);

        JButton btnEditar = new JButton("Guardar Cambios Seleccionados");
        btnEditar.setFont(BUTTON_FONT);
        btnEditar.setBackground(BUTTON_MODIFY_COLOR);
        btnEditar.setForeground(Color.WHITE);
        btnEditar.setFocusPainted(false);
        btnEditar.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        btnEditar.putClientProperty("JButton.buttonType", "roundRect");
        btnEditar.addActionListener(e -> modificarProducto());
        
        JPanel panelBotonModificar = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        panelBotonModificar.setBackground(PRIMARY_BG_COLOR);
        panelBotonModificar.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panelBotonModificar.add(btnEditar);
        panel.add(panelBotonModificar, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelEliminar() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(PRIMARY_BG_COLOR);

        modeloEliminar = new DefaultTableModel(new String[]{"ID", "Nombre", "Precio", "Categoría", "Imagen", "Disponibilidad", "En Menú Día"}, 0) { 
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaEliminar = new JTable(modeloEliminar);
        tablaEliminar.setRowHeight(25);
        tablaEliminar.setFont(TABLE_CELL_FONT);
        tablaEliminar.getTableHeader().setFont(TABLE_HEADER_FONT);
        tablaEliminar.getTableHeader().setBackground(ACCENT_COLOR.brighter());
        tablaEliminar.getTableHeader().setForeground(DARK_TEXT_COLOR);
        tablaEliminar.setSelectionBackground(SELECTED_CARD_COLOR);
        tablaEliminar.setGridColor(ACCENT_COLOR.brighter());
        tablaEliminar.setFillsViewportHeight(true);

        tablaEliminar.getColumnModel().getColumn(1).setPreferredWidth(100);
        tablaEliminar.getColumnModel().getColumn(2).setPreferredWidth(60);
        tablaEliminar.getColumnModel().getColumn(3).setPreferredWidth(80);

        tablaEliminar.getColumnModel().getColumn(0).setMinWidth(0); tablaEliminar.getColumnModel().getColumn(0).setMaxWidth(0); tablaEliminar.getColumnModel().getColumn(0).setPreferredWidth(0);
        tablaEliminar.getColumnModel().getColumn(4).setMinWidth(0); tablaEliminar.getColumnModel().getColumn(4).setMaxWidth(0); tablaEliminar.getColumnModel().getColumn(4).setPreferredWidth(0);
        tablaEliminar.getColumnModel().getColumn(5).setMinWidth(0); tablaEliminar.getColumnModel().getColumn(5).setMaxWidth(0); tablaEliminar.getColumnModel().getColumn(5).setPreferredWidth(0);
        tablaEliminar.getColumnModel().getColumn(6).setMinWidth(0); tablaEliminar.getColumnModel().getColumn(6).setMaxWidth(0); tablaEliminar.getColumnModel().getColumn(6).setPreferredWidth(0);


        JScrollPane scrollPaneTabla = new JScrollPane(tablaEliminar);
        scrollPaneTabla.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR, 1));
        scrollPaneTabla.putClientProperty("JScrollPane.roundRect", true);
        panel.add(scrollPaneTabla, BorderLayout.CENTER);

        JButton btnEliminar = new JButton("Eliminar Producto Seleccionado");
        btnEliminar.setFont(BUTTON_FONT);
        btnEliminar.setBackground(BUTTON_DELETE_COLOR);
        btnEliminar.setForeground(Color.WHITE);
        btnEliminar.setFocusPainted(false);
        btnEliminar.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        btnEliminar.putClientProperty("JButton.buttonType", "roundRect");
        btnEliminar.addActionListener(e -> eliminarProducto());
        
        JPanel panelBotonEliminar = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        panelBotonEliminar.setBackground(PRIMARY_BG_COLOR);
        panelBotonEliminar.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panelBotonEliminar.add(btnEliminar);
        panel.add(panelBotonEliminar, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Lógica para agregar un nuevo producto a la base de datos.
     * Incluye la gestión de la imagen del producto y la notificación a Gestion_menu.
     */
    private void agregarProducto() {
        String nombre = txtNombre.getText().trim();
        String precioStr = txtPrecio.getText().trim();
        String descripcion = txtDescripcion.getText().trim();
        String categoria = (String) comboCategoriaAgregar.getSelectedItem();

        if (nombre.isEmpty() || precioStr.isEmpty() || categoria == null || categoria.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nombre, Precio y Categoría son obligatorios.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String nombreArchivoImagen = null;

        if (rutaImagenTemporalParaGuardar != null && !rutaImagenTemporalParaGuardar.isEmpty()) {
            File origen = new File(rutaImagenTemporalParaGuardar);
            String destinoDir = getRutaBaseImagenes();
            File destinoCarpeta = new File(destinoDir);

            if (!destinoCarpeta.exists()) {
                boolean created = destinoCarpeta.mkdirs();
                if (!created) {
                    JOptionPane.showMessageDialog(this, "Error: No se pudo crear la carpeta de imágenes: " + destinoDir + ". El producto se guardará sin imagen.", "Error de Archivo", JOptionPane.ERROR_MESSAGE);
                    rutaImagenTemporalParaGuardar = null;
                }
            }
            
            String extension = "";
            int i = origen.getName().lastIndexOf('.');
            if (i > 0) {
                extension = origen.getName().substring(i);
            }
            nombreArchivoImagen = UUID.randomUUID().toString() + extension;
            
            File destinoArchivo = new File(destinoCarpeta, nombreArchivoImagen);

            try {
                Files.copy(origen.toPath(), destinoArchivo.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("DEBUG (Inventario - agregarProducto): Imagen copiada a: " + destinoArchivo.getAbsolutePath());
            } catch (java.io.IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al copiar la imagen: " + ex.getMessage() + ". El producto se guardará sin imagen.", "Error de Archivo", JOptionPane.WARNING_MESSAGE);
                ex.printStackTrace();
                nombreArchivoImagen = null;
            }
        }

        try {
            BigDecimal precio = new BigDecimal(precioStr);
            if (precio.compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(this, "El precio no puede ser negativo.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Producto nuevo = new Producto(nombre, precio, descripcion, categoria, nombreArchivoImagen); 
            productoDao.insertarProducto(nuevo);
            JOptionPane.showMessageDialog(this, "Producto agregado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            
            txtNombre.setText("");
            txtPrecio.setText("");
            txtDescripcion.setText("");
            comboCategoriaAgregar.setSelectedIndex(0);
            rutaImagenTemporalParaGuardar = null;
            lblPreviewImagen.setIcon(null);
            lblPreviewImagen.setText("<html><i>Vista previa<br>sin imagen</i></html>");
            
            // --- NUEVO: Refrescar Gestion_menu después de agregar ---
            if (mainApp != null) {
                mainApp.refrescarGestionMenu(); 
            }
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El precio debe ser un número válido.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error de base de datos al agregar: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ocurrió un error inesperado al agregar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Carga los productos en la tabla de la pestaña "Modificar Productos"
     * Permite filtrar por categoría.
     * @param categoria La categoría por la cual filtrar, o "Todos" para mostrar todos.
     */
    private void cargarTablaModificar(String categoria) {
        modeloModificar.setRowCount(0); 
        List<Producto> productos = new ArrayList<>();
        try {
            if ("Todos".equals(categoria)) {
                productos = productoDao.listarTodos();
            } else {
                productos = productoDao.listarPorCategoria(categoria);
            }

            for (Producto p : productos) {
                modeloModificar.addRow(new Object[]{
                    p.getId(),
                    p.getNombre(),
                    p.getPrecio(),
                    p.getDescripcion(),
                    p.getCategoria(),
                    p.getImagen(),
                    p.isDisponibilidad(), 
                    p.isEnMenuDia()      
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar productos para modificar: " + e.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Carga todos los productos en la tabla de la pestaña "Eliminar Productos".
     */
    private void cargarTablaEliminar() {
        modeloEliminar.setRowCount(0); 
        try {
            List<Producto> productos = productoDao.listarTodos();
            for (Producto p : productos) {
                modeloEliminar.addRow(new Object[]{
                    p.getId(),
                    p.getNombre(),
                    p.getPrecio(),
                    p.getCategoria(),
                    p.getImagen(),
                    p.isDisponibilidad(), 
                    p.isEnMenuDia()      
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar productos para eliminar: " + e.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Lógica para modificar un producto seleccionado en la tabla.
     * Recupera los datos de la fila seleccionada y actualiza la base de datos,
     * luego notifica a Gestion_menu.
     */
    private void modificarProducto() {
        int fila = tablaModificar.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un producto de la tabla para guardar los cambios.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int id = (Integer) modeloModificar.getValueAt(fila, 0);
            String nuevoNombre = (String) modeloModificar.getValueAt(fila, 1);
            
            BigDecimal nuevoPrecio;
            Object precioObj = modeloModificar.getValueAt(fila, 2);
            if (precioObj instanceof BigDecimal) {
                nuevoPrecio = (BigDecimal) precioObj;
            } else if (precioObj != null) {
                try {
                    nuevoPrecio = new BigDecimal(precioObj.toString());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "El precio en la fila seleccionada no es un número válido.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                JOptionPane.showMessageDialog(this, "El precio no puede estar vacío.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String nuevaDescripcion = (String) modeloModificar.getValueAt(fila, 3);
            String nuevaCategoria = (String) modeloModificar.getValueAt(fila, 4);
            String nombreImagenActual = (String) modeloModificar.getValueAt(fila, 5);
            boolean disponibilidadActual = (Boolean) modeloModificar.getValueAt(fila, 6); 
            boolean enMenuDiaActual = (Boolean) modeloModificar.getValueAt(fila, 7);     


            if (nuevoNombre.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre del producto no puede estar vacío.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (nuevoPrecio.compareTo(BigDecimal.ZERO) < 0) { 
                JOptionPane.showMessageDialog(this, "El precio debe ser un número positivo.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (nuevaCategoria == null || nuevaCategoria.isEmpty()) {
                JOptionPane.showMessageDialog(this, "La categoría no puede estar vacía.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Producto actualizado = new Producto(
                id,
                nuevoNombre,
                nuevoPrecio,
                nuevaDescripcion,
                nuevaCategoria,
                nombreImagenActual, 
                disponibilidadActual, 
                enMenuDiaActual       
            );
            
            productoDao.actualizarProducto(actualizado); 

            JOptionPane.showMessageDialog(this, "Producto modificado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            String categoriaFiltroActual = (String) comboCategoriaModificar.getSelectedItem();
            cargarTablaModificar(categoriaFiltroActual);
            
            // --- NUEVO: Refrescar Gestion_menu después de modificar ---
            if (mainApp != null) {
                mainApp.refrescarGestionMenu(); 
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error de base de datos al modificar: " + e.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ocurrió un error inesperado al modificar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Lógica para eliminar un producto seleccionado de la tabla.
     * Incluye la eliminación del archivo de imagen y la notificación a Gestion_menu.
     */
    private void eliminarProducto() {
        int fila = tablaEliminar.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto para eliminar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (Integer) modeloEliminar.getValueAt(fila, 0);
        String nombreProducto = (String) modeloEliminar.getValueAt(fila, 1);
        String nombreImagenAEliminar = (String) modeloEliminar.getValueAt(fila, 4);

        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de eliminar el producto '" + nombreProducto + "'? Esta acción es irreversible.", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                productoDao.eliminarProducto(id);

                if (nombreImagenAEliminar != null && !nombreImagenAEliminar.isEmpty()) {
                    File imagenFile = new File(getRutaBaseImagenes() + File.separator + nombreImagenAEliminar);
                    if (imagenFile.exists()) {
                        if (imagenFile.delete()) {
                            System.out.println("DEBUG (Inventario - eliminarProducto): Imagen eliminada del disco: " + imagenFile.getAbsolutePath());
                        } else {
                            System.err.println("Advertencia (Inventario - eliminarProducto): No se pudo eliminar la imagen del disco: " + imagenFile.getAbsolutePath());
                        }
                    } else {
                        System.out.println("DEBUG (Inventario - eliminarProducto): Imagen no encontrada en disco para eliminar: " + imagenFile.getAbsolutePath());
                    }
                }

                JOptionPane.showMessageDialog(this, "Producto '" + nombreProducto + "' eliminado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarTablaEliminar();
                
                // --- NUEVO: Refrescar Gestion_menu después de eliminar ---
                if (mainApp != null) {
                    mainApp.refrescarGestionMenu(); 
                }

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error de base de datos al eliminar: " + e.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Ocurrió un error inesperado al eliminar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
}