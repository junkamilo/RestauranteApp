package Restaurante_app.GestionPaneles;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter; // Importación necesaria para ComponentAdapter
import java.awt.event.ComponentEvent;   // Importación necesaria para ComponentEvent
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;

import Restaurante_app.InicioRoles;
import Restaurante_app.DAO.ProductoDao;
import Restaurante_app.Model.Producto;

public class Gestion_menu extends JPanel {
    private static final long serialVersionUID = 1L;

    private final ProductoDao productoDao = new ProductoDao();
    private final Map<String, JTable> tablas = new HashMap<>();
    private static final String[] CATEGORIAS = {"especial", "corriente", "sopa", "principio"};
    private Tomar_pedido panelTomarPedido; // Referencia para actualizar si es necesario
    private InicioRoles mainApp;
    private String panelRegreso;

    // Constructor que recibe mainApp y panelRegreso
    public Gestion_menu(InicioRoles mainApp, String panelRegreso) {
        this.mainApp = mainApp;
        this.panelRegreso = panelRegreso;
        initComponents();
    }

    // Constructor que recibe panelTomarPedido, mainApp y panelRegreso
    public Gestion_menu(Tomar_pedido panelTomarPedido, InicioRoles mainApp, String panelRegreso) {
        this(mainApp, panelRegreso); // Llama al constructor anterior
        this.panelTomarPedido = panelTomarPedido;
    }

    // Constructor por defecto (para evitar errores si se llama sin argumentos)
    public Gestion_menu() {
        this(null, "Login");
    }

    // Constructor que recibe solo mainApp (el que tu InicioRoles usa)
    public Gestion_menu(InicioRoles mainApp) {
        this(mainApp, "Administrador"); // Por defecto, si viene de InicioRoles, regresa a Administrador
    }


    private void initComponents() {
        try {
            // Configuración global de UIManager para consistencia visual
            UIManager.put("TabbedPane.background", new Color(255, 248, 240));
            UIManager.put("TabbedPane.foreground", new Color(60, 44, 30));
            UIManager.put("TabbedPane.selected", new Color(219, 168, 86));
            UIManager.put("TabbedPane.borderHightlightColor", new Color(219, 168, 86));
            UIManager.put("TabbedPane.hoverColor", new Color(235, 180, 95));
            UIManager.put("TabbedPane.focusColor", new Color(219, 168, 86));
            UIManager.put("Table.background", Color.WHITE);
            UIManager.put("Table.gridColor", new Color(240, 240, 240));
            UIManager.put("TableHeader.background", new Color(250, 250, 250));
            UIManager.put("TableHeader.foreground", new Color(60, 44, 30));
            UIManager.put("Table.selectionBackground", new Color(219, 168, 86, 100));
            UIManager.put("Table.selectionForeground", new Color(60, 44, 30));
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 8);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setBackground(new Color(255, 248, 240));
        setPreferredSize(new Dimension(360, 640)); // Tamaño preferido del panel
        setLayout(null); // Usando null layout como en tu código original

        JLabel titulo = new JLabel("GESTIÓN DE MENÚ", SwingConstants.CENTER);
        titulo.setBounds(0, 10, 360, 40);
        titulo.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 20));
        titulo.setForeground(new Color(60, 44, 30));
        add(titulo);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBounds(10, 60, 340, 520); // Posición y tamaño de las pestañas
        tabs.setFont(new Font("Arial", Font.BOLD, 12));
        
        // Crear una pestaña para cada categoría de producto
        for (String cat : CATEGORIAS) {
            String nombreCategoria = cat.substring(0, 1).toUpperCase() + cat.substring(1); // Capitalizar primera letra
            tabs.addTab(nombreCategoria, crearPanelCategoria(cat));
        }
        add(tabs);

        // Panel inferior para los botones de acción
        JPanel footer = new JPanel();
        footer.setBounds(10, 585, 340, 45); // Posición y tamaño del footer
        footer.setLayout(new GridLayout(1, 2, 10, 0)); // Dos columnas con espacio entre ellas
        footer.setOpaque(false); // Para que se vea el fondo del panel principal si lo hay
        add(footer);

        JButton btnVolver = new JButton("← VOLVER");
        btnVolver.setFont(new Font("Arial", Font.BOLD, 12));
        btnVolver.setForeground(new Color(60, 44, 30));
        btnVolver.setBackground(new Color(240, 240, 240));
        btnVolver.addActionListener(this::onVolver);
        footer.add(btnVolver);

        JButton btnGuardar = new JButton("GUARDAR MENÚ");
        btnGuardar.setFont(new Font("Arial", Font.BOLD, 12));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setBackground(new Color(219, 168, 86));
        btnGuardar.addActionListener(e -> {
            onGuardar(e);
            // Si hay un panel Tomar_pedido asociado, recarga su menú después de guardar
            if (panelTomarPedido != null) {
                panelTomarPedido.cargarMenuDelDia();
            }
            // También se podría llamar a cargarProductosDelMenu() aquí para refrescar este mismo panel
            // Pero el listener del componenteShown ya lo hará, o si se vuelve a este panel.
        });
        footer.add(btnGuardar);

        // --- NUEVO: ComponentListener para refrescar el menú cuando el panel se hace visible ---
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                System.out.println("DEBUG (Gestion_menu): Panel Gestion_menu se hizo visible. Recargando menú.");
                cargarProductosDelMenu(); // Llama al método de recarga completa
            }
        });
    }

    /**
     * Crea un panel para una categoría específica, incluyendo una tabla de productos.
     * @param categoria El nombre de la categoría (ej. "especial", "corriente").
     * @return El JPanel configurado para la categoría.
     */
    private JPanel crearPanelCategoria(String categoria) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelBuscar = new JPanel(new BorderLayout(5, 5));
        panelBuscar.setBackground(Color.WHITE);
        panelBuscar.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel lblBuscar = new JLabel("Buscar:");
        lblBuscar.setFont(new Font("Arial", Font.PLAIN, 12));
        lblBuscar.setForeground(new Color(120, 120, 120));
        panelBuscar.add(lblBuscar, BorderLayout.WEST);

        JTextField txtBuscar = new JTextField();
        txtBuscar.setFont(new Font("Arial", Font.PLAIN, 12));
        panelBuscar.add(txtBuscar, BorderLayout.CENTER);
        panel.add(panelBuscar, BorderLayout.NORTH);

        String[] columnas = {"Seleccionar", "ID", "Nombre", "Precio"};
        DefaultTableModel model = new DefaultTableModel(columnas, 0) {
            @Override public Class<?> getColumnClass(int column) {
                return column == 0 ? Boolean.class : super.getColumnClass(column);
            }
            @Override public boolean isCellEditable(int row, int column) {
                return column == 0; // Solo la columna "Seleccionar" es editable (el checkbox)
            }
        };

        JTable tabla = new JTable(model);
        tabla.setFont(new Font("Arial", Font.PLAIN, 12));
        tabla.setRowHeight(30);
        tabla.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        // Ocultar columna ID (columna 1)
        tabla.getColumnModel().getColumn(1).setMinWidth(0);
        tabla.getColumnModel().getColumn(1).setMaxWidth(0);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(0);

        // --- CAMBIO CRÍTICO: Asegurar que el checkbox se muestre correctamente ---
        // Se elimina la línea que estaba forzando un DefaultTableCellRenderer.
        // La clase `Boolean.class` ya activa automáticamente el renderizador de JCheckBox.
        // tabla.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer()); // ¡ELIMINAR ESTA LÍNEA!
        
        // Ajustar el ancho preferido para la columna "Seleccionar" para que el checkbox se vea bien
        tabla.getColumnModel().getColumn(0).setPreferredWidth(80); // Un ancho razonable para el checkbox
        tabla.getColumnModel().getColumn(0).setMaxWidth(80); // Evita que se estire demasiado
        tabla.getColumnModel().getColumn(0).setMinWidth(80); // Asegura un tamaño mínimo

        tablas.put(categoria, tabla); // Guardar la referencia de la tabla en el mapa

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scroll, BorderLayout.CENTER);

        cargarDatos(categoria); // Cargar datos iniciales para esta categoría
        
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        tabla.setRowSorter(sorter);

        // Listener para el campo de búsqueda
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }

            private void filtrar() {
                String texto = txtBuscar.getText().trim();
                if (texto.isEmpty()) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(texto)));
            }
        });

        return panel;
    }

    /**
     * Carga los datos de productos para una categoría específica en su tabla.
     * @param categoria La categoría de productos a cargar.
     */
    private void cargarDatos(String categoria) {
        try {
            List<Producto> lista = productoDao.listarPorCategoria(categoria);
            DefaultTableModel model = (DefaultTableModel) tablas.get(categoria).getModel();
            model.setRowCount(0); // Limpiar filas existentes
            for (Producto p : lista) {
                // Aquí, el valor `en_menu_dia` del producto se mapea directamente al booleano
                // de la columna "Seleccionar", lo que activará el renderizador de JCheckBox.
                model.addRow(new Object[]{p.isEnMenuDia(), p.getId(), p.getNombre(), p.getPrecio()});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error al cargar productos de " + categoria + ": " + e.getMessage(),
                "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Método PÚBLICO para recargar todas las tablas de productos en Gestion_menu.
     * Este método es llamado por InicioRoles cuando hay cambios en el inventario.
     */
    public void cargarProductosDelMenu() {
        System.out.println("DEBUG (Gestion_menu): Iniciando recarga completa de todas las tablas del menú desde un llamado externo.");
        for (String cat : CATEGORIAS) {
            cargarDatos(cat); // Re-carga los datos para cada categoría
        }
        revalidate(); // Revalidar el layout de todo el panel
        repaint();    // Repintar el panel para mostrar los cambios
        System.out.println("DEBUG (Gestion_menu): Recarga completa de las tablas del menú finalizada.");
    }


    /**
     * Lógica para guardar las selecciones del menú del día en la base de datos.
     * @param e El evento de acción (del botón Guardar).
     */
    private void onGuardar(ActionEvent e) {
        final boolean[] tieneEspecial = {false};
        final boolean[] tieneCorriente = {false};
        final boolean[] tieneSopa = {false};
        final boolean[] tienePrincipio = {false};

        // Verificar que al menos un producto esté seleccionado en cada categoría obligatoria
        tablas.forEach((cat, tabla) -> {
            DefaultTableModel model = (DefaultTableModel) tabla.getModel();
            for (int i = 0; i < model.getRowCount(); i++) {
                Boolean marcado = (Boolean) model.getValueAt(i, 0); // La columna 0 es el checkbox
                if (Boolean.TRUE.equals(marcado)) {
                    switch (cat) {
                        case "especial" -> tieneEspecial[0] = true;
                        case "corriente" -> tieneCorriente[0] = true;
                        case "sopa" -> tieneSopa[0] = true;
                        case "principio" -> tienePrincipio[0] = true;
                    }
                }
            }
        });

        // Si falta alguna categoría obligatoria, mostrar advertencia
        if (!tieneEspecial[0] || !tieneCorriente[0] || !tieneSopa[0] || !tienePrincipio[0]) {
            String msg = "Debe seleccionar al menos un producto en las categorías:\n";
            if (!tieneEspecial[0]) msg += "- Especial\n";
            if (!tieneCorriente[0]) msg += "- Corriente\n";
            if (!tieneSopa[0]) msg += "- Sopa\n";
            if (!tienePrincipio[0]) msg += "- Principio\n";

            JOptionPane.showMessageDialog(this, msg, "Error: Selección incompleta", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Producto> seleccion = new ArrayList<>();
        tablas.forEach((cat, tabla) -> {
            DefaultTableModel model = (DefaultTableModel) tabla.getModel();
            for (int i = 0; i < model.getRowCount(); i++) {
                Boolean marcado = (Boolean) model.getValueAt(i, 0);
                if (Boolean.TRUE.equals(marcado)) {
                    int id = (Integer) model.getValueAt(i, 1); // ID del producto
                    String nombre = (String) model.getValueAt(i, 2);
                    Object precioObj = model.getValueAt(i, 3);
                    java.math.BigDecimal precio = null;
                    if (precioObj instanceof java.math.BigDecimal) {
                        precio = (java.math.BigDecimal) precioObj;
                    } else if (precioObj instanceof Number) {
                        precio = new java.math.BigDecimal(((Number) precioObj).doubleValue());
                    } else if (precioObj instanceof String) {
                        try {
                            precio = new java.math.BigDecimal((String) precioObj);
                        } catch (NumberFormatException ex) {
                            System.err.println("Error de formato de precio para: " + nombre + " - " + precioObj);
                        }
                    }
                    // IMPORTANTE: Al crear el objeto Producto para la selección, solo el ID es realmente necesario
                    // para actualizar `en_menu_dia` en la BD. Los demás campos son para completar el objeto.
                    // Se pasan null para descripción e imagen, y true para disponibilidad y en_menu_dia.
                    seleccion.add(new Producto(id, nombre, precio, null, cat, null, true, true)); 
                }
            }
        });

        try {
            productoDao.actualizarMenuDelDia(seleccion); // Llama al DAO para actualizar
            JOptionPane.showMessageDialog(this,
                "Menú del Día guardado con " + seleccion.size() + " productos.");
            System.out.println("DEBUG (Gestion_menu): Menú del día guardado con " + seleccion.size() + " productos seleccionados.");

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error al guardar el menú del día: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            System.err.println("ERROR (Gestion_menu): Error SQL al guardar el menú del día: " + ex.getMessage());
        }
    }

    /**
     * Maneja la acción del botón "VOLVER".
     * @param e El evento de acción.
     */
    private void onVolver(ActionEvent e) {
        if (mainApp != null && panelRegreso != null) {
            System.out.println("DEBUG (Gestion_menu): Volviendo al panel: " + panelRegreso);
            mainApp.mostrarPanel(panelRegreso);
        } else {
            JOptionPane.showMessageDialog(this, "No se puede volver. Falta la configuración de navegación.", "Error de Navegación", JOptionPane.ERROR_MESSAGE);
            System.err.println("ERROR (Gestion_menu): mainApp o panelRegreso es null. No se puede volver.");
        }
    }
}