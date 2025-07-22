package Restaurante_app.GestionPaneles;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.SQLException;
import java.util.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import Restaurante_app.DAO.ProductoDao;
import Restaurante_app.DAO.DetallePedidoDAO;
import Restaurante_app.DAO.MesaDao;
import Restaurante_app.DAO.PedidoDAO;
import Restaurante_app.Model.Producto;
import Restaurante_app.Model.Venta;
import Restaurante_app.Model.DetallePedidoConNombre;
import Restaurante_app.Model.GestorVentas;
import Restaurante_app.Model.Mesa;
import Restaurante_app.Model.Pedido;
import Restaurante_app.InicioRoles;

import java.util.List;
import java.math.BigDecimal;

public class Tomar_pedido extends JPanel {

    private static final long serialVersionUID = 1L;
    private final ProductoDao productoDao = new ProductoDao();
    private final MesaDao mesaDao = new MesaDao();
    private final PedidoDAO pedidoDao = new PedidoDAO();
    private final DetallePedidoDAO detalleDAO = new DetallePedidoDAO();

    private final String[] CATEGORIAS = {"corriente", "especial"};

    private JTabbedPane tabs;
    private JComboBox<String> comboBox_mesas;
    private JLabel lblMesaSeleccionadaDisplay;
    private JButton btnCambiarMesa;
    private JPanel panel_cards;
    private JPanel panel_corrientes, panel_especiales;
    private List<JCheckBox> productosCheckboxes = new ArrayList<>();
     
    private int userId;
    private Control_ventas controlVentasPanel;
    private GestorVentas gestorVentas = new GestorVentas();
    
    private InicioRoles mainApp;

    private Mesa mesaSeleccionadaParaPedido = null;

    private JPanel panelMenuDiaPrincipal;
    private Map<String, JPanel> panelesCategoriaMenuDia = new HashMap<>(); 


    /**
     * Obtiene la ruta base donde deberían estar las imágenes de los productos.
     * Esta ruta es relativa al directorio de ejecución de la aplicación (donde está el JAR).
     * @return La ruta absoluta al directorio 'imagenes'.
     */
    private static String getRutaBaseImagenes() {
        String appDir = System.getProperty("user.dir");
        return appDir + File.separator + "imagenes";
    }
    

 // Constructor de la clase Tomar_pedido
    public Tomar_pedido(int userId, Control_ventas controlVentasPanel, InicioRoles mainApp) {
        this.userId = userId;
        this.controlVentasPanel = controlVentasPanel;
        this.mainApp = mainApp;

        setBackground(Color.WHITE);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(360, 640));

        tabs = new JTabbedPane();
        tabs.addTab("Menú del Día", crearPanelMenuDelDia());
        tabs.addTab("Tomar Pedido", crearPanelTomarPedido());

        tabs.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                String titulo = tabs.getTitleAt(tabs.getSelectedIndex());

                if ("Menú del Día".equals(titulo)) {
                    cargarMenuDelDia();
                } 
                else if ("Tomar Pedido".equals(titulo)) {
                    cargarProductosEnPanel(panel_corrientes, "corriente");
                    cargarProductosEnPanel(panel_especiales, "especial");
                    cargarMesas();
                }
            }
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);

        JButton btnVolver = new JButton("← Volver");

        btnVolver.addActionListener((ActionEvent e) -> {
            InicioRoles frame = (InicioRoles) SwingUtilities.getWindowAncestor(Tomar_pedido.this);
            String rol = frame.getRolActual();

            switch (rol.toLowerCase()) {
                case "administrador":
                    frame.mostrarPanel("Administrador");
                    break;
                case "mesero":
                    frame.mostrarPanel("Mesero");
                    break;
                case "mesero auxiliar":
                    frame.mostrarPanel("Mesero Auxiliar");
                    break;
                case "cocinero":
                    frame.mostrarPanel("Cocinero");
                    break;
                default:
                    JOptionPane.showMessageDialog(Tomar_pedido.this, "Rol no reconocido: " + rol);
            }
        });

        topPanel.add(btnVolver);
        add(topPanel, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    
    /**
     * Crea y devuelve un JScrollPane que contiene el "Menú del Día"
     * El menú se organiza por categorías (especial, corriente, sopa, principio)
     * y muestra los productos asociados a cada una en un formato de tarjeta elegante.
     */
    private JScrollPane crearPanelMenuDelDia() {
        panelMenuDiaPrincipal = new JPanel();
        panelMenuDiaPrincipal.setLayout(new BoxLayout(panelMenuDiaPrincipal, BoxLayout.Y_AXIS));
        panelMenuDiaPrincipal.setBackground(new Color(255, 248, 240));
        panelMenuDiaPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        for (String categoria : new String[]{"especial", "corriente", "sopa", "principio"}) {
            JLabel lblCategoria = new JLabel("<html><b style='font-size:16px; color:#3C2C1E;'>" + capitalizar(categoria) + "</b></html>");
            lblCategoria.setFont(new Font("Arial", Font.BOLD, 18));
            lblCategoria.setForeground(new Color(60, 44, 30));
            lblCategoria.setAlignmentX(Component.LEFT_ALIGNMENT);
            panelMenuDiaPrincipal.add(lblCategoria);
            panelMenuDiaPrincipal.add(Box.createVerticalStrut(10));

            JPanel categoryProductsPanel = new JPanel();
            categoryProductsPanel.setLayout(new BoxLayout(categoryProductsPanel, BoxLayout.Y_AXIS)); // Changed from FlowLayout to BoxLayout.Y_AXIS
            categoryProductsPanel.setBackground(new Color(255, 248, 240));
            categoryProductsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            categoryProductsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

            panelesCategoriaMenuDia.put(categoria, categoryProductsPanel);
            panelMenuDiaPrincipal.add(categoryProductsPanel);
            panelMenuDiaPrincipal.add(Box.createVerticalStrut(20));
        }

        cargarMenuDelDia();

        JScrollPane scrollPane = new JScrollPane(panelMenuDiaPrincipal);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        return scrollPane;
    }

    /**
     * Crea una tarjeta visualmente atractiva para un producto del menú.
     * Incluye imagen, nombre, descripción y precio.
     * @param p El objeto Producto con los datos.
     * @return Un JPanel que representa la tarjeta del producto.
     */
    private JPanel crearTarjetaProductoMenu(Producto p) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setPreferredSize(new Dimension(290, 100)); // Adjusted width for better fit
        card.setMaximumSize(new Dimension(290, 100)); // Added to prevent horizontal stretch
        card.setMinimumSize(new Dimension(290, 100)); // Added to maintain minimum size
        card.setBackground(new Color(255, 253, 245));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 180, 150), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.putClientProperty("FlatLaf.style", "arc: 12");
        card.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the card if container is wider

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);

        // Imagen (izquierda)
        JLabel lblImagen = new JLabel();
        lblImagen.setPreferredSize(new Dimension(70, 70));
        lblImagen.setHorizontalAlignment(SwingConstants.CENTER);
        lblImagen.setBorder(BorderFactory.createLineBorder(new Color(220, 200, 180), 1));
        lblImagen.putClientProperty("FlatLaf.style", "arc: 8");

        String nombreImagen = p.getImagen();
        File archivoImagen = null;
        if (nombreImagen != null && !nombreImagen.isEmpty()) {
            String rutaCompleta = getRutaBaseImagenes() + File.separator + nombreImagen;
            archivoImagen = new File(rutaCompleta);
        }

        if (archivoImagen != null && archivoImagen.exists()) {
            ImageIcon icon = new ImageIcon(archivoImagen.getAbsolutePath()); // CORRECTED LINE HERE
            Image imagen = icon.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
            lblImagen.setIcon(new ImageIcon(imagen));
            lblImagen.setText("");
        } else {
            lblImagen.setIcon(null);
            lblImagen.setText("<html><center>Sin<br>foto</center></html>");
            lblImagen.setHorizontalAlignment(SwingConstants.CENTER);
            lblImagen.setVerticalAlignment(SwingConstants.CENTER);
            lblImagen.setForeground(new Color(180, 180, 180));
            lblImagen.setFont(new Font("Arial", Font.ITALIC, 9));
        }
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        card.add(lblImagen, gbc);

        // Nombre (arriba-derecha)
        JLabel lblNombre = new JLabel("<html><b>" + p.getNombre() + "</b></html>");
        lblNombre.setFont(new Font("Arial", Font.BOLD, 14));
        lblNombre.setForeground(new Color(60, 44, 30));
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        card.add(lblNombre, gbc);

        // Descripción (medio-derecha)
        JTextArea txtDescripcion = new JTextArea(p.getDescripcion() != null ? p.getDescripcion() : "Sin descripción.");
        txtDescripcion.setFont(new Font("Arial", Font.PLAIN, 10));
        txtDescripcion.setForeground(new Color(100, 100, 100));
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        txtDescripcion.setEditable(false);
        txtDescripcion.setOpaque(false);
        txtDescripcion.setBorder(BorderFactory.createEmptyBorder());
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        card.add(txtDescripcion, gbc);

        // Precio (abajo-derecha)
        JLabel lblPrecio = new JLabel("<html><b>$ " + String.format("%.2f", p.getPrecio()) + "</b></html>");
        lblPrecio.setFont(new Font("Arial", Font.BOLD, 14));
        lblPrecio.setForeground(new Color(200, 70, 70));
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        card.add(lblPrecio, gbc);

        return card;
    }


    /**
     * Carga el menú del día y lo muestra en el nuevo formato de tarjetas.
     */
    public void cargarMenuDelDia() {
        try {
            for (JPanel panel : panelesCategoriaMenuDia.values()) {
                panel.removeAll();
            }

            for (String categoria : new String[]{"especial", "corriente", "sopa", "principio"}) {
                List<Producto> productos = productoDao.listarMenuDelDiaPorCategoria(categoria);
                JPanel targetPanel = panelesCategoriaMenuDia.get(categoria);

                if (productos.isEmpty()) {
                    JLabel noProductsLabel = new JLabel("<html><i>- No hay productos de " + capitalizar(categoria) + " en el menú del día -</i></html>");
                    noProductsLabel.setFont(new Font("Arial", Font.ITALIC, 11));
                    noProductsLabel.setForeground(new Color(150, 150, 150));
                    noProductsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    targetPanel.add(noProductsLabel);
                    targetPanel.add(Box.createVerticalStrut(10));
                } else {
                    for (Producto p : productos) {
                        JPanel productCard = crearTarjetaProductoMenu(p);
                        targetPanel.add(productCard);
                        targetPanel.add(Box.createVerticalStrut(10));
                    }
                }
                targetPanel.revalidate();
                targetPanel.repaint();
            }
            panelMenuDiaPrincipal.revalidate();
            panelMenuDiaPrincipal.repaint();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar el menú del día: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    /**
     * Crea y configura el panel para tomar pedidos, incluyendo la selección de mesas
     * y la visualización de productos por categorías (corrientes, especiales).
     */
    private JPanel crearPanelTomarPedido() {
        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(255, 248, 240));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        panel.setLayout(null);

        JLabel titulo_mesa = new JLabel("Mesa:");
        titulo_mesa.setBounds(10, 11, 50, 14);
        panel.add(titulo_mesa);

        comboBox_mesas = new JComboBox<>();
        comboBox_mesas.setBounds(60, 7, 100, 22);
        panel.add(comboBox_mesas);

        lblMesaSeleccionadaDisplay = new JLabel("Mesa no seleccionada");
        lblMesaSeleccionadaDisplay.setBounds(10, 35, 300, 25);
        lblMesaSeleccionadaDisplay.setFont(new Font("Arial", Font.BOLD, 14));
        lblMesaSeleccionadaDisplay.setForeground(Color.RED);
        lblMesaSeleccionadaDisplay.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lblMesaSeleccionadaDisplay);

        btnCambiarMesa = new JButton("Cambiar Mesa");
        btnCambiarMesa.setBounds(240, 7, 100, 22);
        btnCambiarMesa.setEnabled(false);
        btnCambiarMesa.addActionListener(e -> resetMesaSeleccion());
        panel.add(btnCambiarMesa);

        comboBox_mesas.addActionListener(e -> {
            if (comboBox_mesas.getSelectedItem() != null && !"Seleccionar Mesa".equals(comboBox_mesas.getSelectedItem())) {
                try {
                    String nombreMesa = (String) comboBox_mesas.getSelectedItem();
                    Mesa mesa = mesaDao.obtenerMesaPorNombre(nombreMesa);
                    if (mesa != null) {
                        mesaSeleccionadaParaPedido = mesa;
                        lblMesaSeleccionadaDisplay.setText("Mesa Seleccionada: " + mesa.getNombre());
                        lblMesaSeleccionadaDisplay.setForeground(new Color(0, 120, 0));
                        comboBox_mesas.setEnabled(false);
                        btnCambiarMesa.setEnabled(true);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error al seleccionar la mesa: " + ex.getMessage(), "Error de BD", JOptionPane.ERROR_MESSAGE);
                    resetMesaSeleccion();
                }
            }
        });

        JButton btn_agregarMesa = new JButton("Agregar Nueva Mesa");
        btn_agregarMesa.setBounds(10, 65, 180, 25);
        btn_agregarMesa.addActionListener(e -> agregarMesa());
        panel.add(btn_agregarMesa);

        JButton btn_enviarPedido = new JButton("Enviar Pedido");
        btn_enviarPedido.setBounds(110, 546, 140, 22);
        btn_enviarPedido.addActionListener(e -> {
            validarYEnviarPedido();
        });
        panel.add(btn_enviarPedido);

        JButton btn_corrientes = new JButton("Corrientes");
        btn_corrientes.setBounds(10, 100, 120, 25);
        panel.add(btn_corrientes);

        JButton btn_especiales = new JButton("Especiales");
        btn_especiales.setBounds(140, 100, 120, 25);
        panel.add(btn_especiales);

        panel_cards = new JPanel(new CardLayout());
        panel_cards.setBounds(10, 130, 335, 400);
        panel.add(panel_cards);

        panel_corrientes = new JPanel();
        panel_corrientes.setLayout(new BoxLayout(panel_corrientes, BoxLayout.Y_AXIS));
        JScrollPane scrollCorrientes = new JScrollPane(panel_corrientes);
        panel_cards.add(scrollCorrientes, "corriente");

        panel_especiales = new JPanel();
        panel_especiales.setLayout(new BoxLayout(panel_especiales, BoxLayout.Y_AXIS));
        JScrollPane scrollEspeciales = new JScrollPane(panel_especiales);
        panel_cards.add(scrollEspeciales, "especial");

        btn_corrientes.addActionListener(e -> ((CardLayout) panel_cards.getLayout()).show(panel_cards, "corriente"));
        btn_especiales.addActionListener(e -> ((CardLayout) panel_cards.getLayout()).show(panel_cards, "especial"));

        cargarMesas();
        cargarProductosEnPanel(panel_corrientes, "corriente");
        cargarProductosEnPanel(panel_especiales, "especial");

        return panel;
    }

    /**
     * Resetea el estado de selección de la mesa.
     * Habilita el JComboBox de mesas y reinicia las etiquetas.
     */
    private void resetMesaSeleccion() {
        mesaSeleccionadaParaPedido = null;
        comboBox_mesas.setEnabled(true);
        comboBox_mesas.setSelectedItem("Seleccionar Mesa");
        lblMesaSeleccionadaDisplay.setText("Mesa no seleccionada");
        lblMesaSeleccionadaDisplay.setForeground(Color.RED);
        btnCambiarMesa.setEnabled(false);
        cargarMesas();
    }


    /**
     * Carga los productos de una categoría específica en el panel proporcionado.
     * Añade checkboxes, campos de cantidad, opciones de sopa (con un nuevo checkbox
     * para activarlas) y campos de detalles para cada producto.
     * @param panel El JPanel donde se cargarán los productos.
     * @param categoria La categoría de productos a cargar (e.g., "corriente", "especial").
     */
    private void cargarProductosEnPanel(JPanel panel, String categoria) {
        Border lineBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 140, 100)),
            BorderFactory.createEmptyBorder(2, 5, 2, 5)
        );

        try {
            panel.removeAll();

            List<Producto> productos = productoDao.listarMenuDelDiaPorCategoria(categoria);
            List<Producto> sopas = productoDao.listarMenuDelDiaPorCategoria("sopa");

            int count = productos.size();

            for (int i = 0; i < count; i++) {
                Producto p = productos.get(i);

                JPanel card = new JPanel(null);
                card.setPreferredSize(new Dimension(310, 150));
                card.setMaximumSize(new Dimension(310, 150));
                card.setMinimumSize(new Dimension(310, 150));
                card.setBackground(new Color(255, 253, 245));

                JCheckBox checkBox = new JCheckBox();
                checkBox.setBounds(280, 5, 20, 20);
                checkBox.setBackground(new Color(255, 253, 245));
                card.add(checkBox);

                checkBox.addActionListener(e -> {
                    boolean seleccionado = checkBox.isSelected();
                    if (seleccionado) {
                        card.setBackground(new Color(230, 245, 230));
                        card.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(100, 180, 100), 2),
                            BorderFactory.createEmptyBorder(8, 8, 8, 8)
                        ));
                    } else {
                        card.setBackground(new Color(255, 253, 245));
                        card.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(200, 180, 150), 1),
                            BorderFactory.createEmptyBorder(10, 10, 10, 10)
                        ));
                    }
                });

                JLabel lblImagen = new JLabel();
                lblImagen.setBounds(10, 20, 60, 80);
                lblImagen.setHorizontalAlignment(SwingConstants.CENTER);
                lblImagen.setBorder(BorderFactory.createLineBorder(new Color(200, 180, 150), 1));
                
                String nombreImagen = p.getImagen();

                File archivoImagen = null;
                if (nombreImagen != null && !nombreImagen.isEmpty()) {
                    String rutaCompleta = getRutaBaseImagenes() + File.separator + nombreImagen;
                    archivoImagen = new File(rutaCompleta);
                }
                
                System.out.println("Buscando imagen para producto '" + p.getNombre() + "' en: " + (archivoImagen != null ? archivoImagen.getAbsolutePath() : "Nombre de imagen nulo/vacío en BD"));

                if (archivoImagen != null && archivoImagen.exists()) {
                    ImageIcon icon = new ImageIcon(archivoImagen.getAbsolutePath()); // CORRECTED LINE HERE
                    Image imagen = icon.getImage().getScaledInstance(60, 80, Image.SCALE_SMOOTH);
                    lblImagen.setIcon(new ImageIcon(imagen));
                    lblImagen.setText("");
                } else {
                    lblImagen.setIcon(null);
                    lblImagen.setText("Sin imagen");
                    lblImagen.setHorizontalAlignment(SwingConstants.CENTER);
                    lblImagen.setVerticalAlignment(SwingConstants.CENTER);
                    lblImagen.setForeground(new Color(150, 150, 150));
                    lblImagen.setFont(new Font("Arial", Font.ITALIC, 10));
                    System.err.println("Advertencia: Imagen no encontrada o nombre vacío para el producto: '" + p.getNombre() + "'. Ruta esperada: " + (archivoImagen != null ? archivoImagen.getAbsolutePath() : "N/A"));
                }
                card.add(lblImagen);

                JLabel lblNombre = new JLabel(p.getNombre());
                lblNombre.putClientProperty("tipo", "nombre_producto");
                lblNombre.setBounds(75, 10, 180, 20);
                lblNombre.setFont(new Font("Arial", Font.BOLD, 12));
                lblNombre.setForeground(new Color(60, 44, 30));
                card.add(lblNombre);

                JLabel lblCantidad = new JLabel("Cantidad:");
                lblCantidad.setBounds(75, 40, 60, 20);
                lblCantidad.setFont(new Font("Arial", Font.PLAIN, 11));
                card.add(lblCantidad);

                JTextField txtCantidad = new JTextField("1");
                txtCantidad.setBounds(135, 40, 30, 20);
                txtCantidad.setBorder(lineBorder);
                txtCantidad.setBackground(Color.WHITE);
                card.add(txtCantidad);

                JCheckBox chkSopaIncluida = new JCheckBox("Incluir Sopa:");
                chkSopaIncluida.setBounds(75, 70, 100, 20);
                chkSopaIncluida.setBackground(new Color(255, 253, 245));
                card.add(chkSopaIncluida);

                JLabel lblSopa = new JLabel("Sopa:");
                lblSopa.putClientProperty("component_type", "lblSopa");
                lblSopa.setBounds(175, 70, 40, 20);
                lblSopa.setFont(new Font("Arial", Font.PLAIN, 11));
                lblSopa.setEnabled(false);
                card.add(lblSopa);

                JComboBox<String> comboSopa = new JComboBox<>();
                comboSopa.setBounds(215, 70, 90, 22);
                comboSopa.setBackground(Color.WHITE);
                comboSopa.addItem("Sin sopa");
                comboSopa.setEnabled(false);

                for (Producto sopa : sopas) {
                    comboSopa.addItem("Sopa " + sopa.getNombre() + " completa");
                    comboSopa.addItem("Sopa " + sopa.getNombre() + " mediana");
                    comboSopa.addItem("Sopa " + sopa.getNombre() + " pequeña");
                }
                card.add(comboSopa);

                chkSopaIncluida.addActionListener(e -> {
                    boolean selected = chkSopaIncluida.isSelected();
                    lblSopa.setEnabled(selected);
                    comboSopa.setEnabled(selected);
                    if (!selected) {
                        comboSopa.setSelectedItem("Sin sopa");
                    }
                });

                JLabel lblDetalles = new JLabel("Detalles:");
                lblDetalles.setBounds(75, 100, 60, 20);
                lblDetalles.setFont(new Font("Arial", Font.PLAIN, 11));
                card.add(lblDetalles);

                JTextArea txtDetalles = new JTextArea();
                txtDetalles.setBounds(135, 100, 170, 20);
                txtDetalles.setLineWrap(true);
                txtDetalles.setWrapStyleWord(true);
                txtDetalles.setBorder(lineBorder);
                txtDetalles.setBackground(Color.WHITE);
                card.add(txtDetalles);
                
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 180, 150), 1),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));

                panel.add(card);
                if (i < count - 1) {
                    panel.add(Box.createVerticalStrut(10));
                }
            }

            panel.revalidate();
            panel.repaint();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Carga las mesas disponibles desde la base de datos en el JComboBox de mesas.
     * También maneja el estado inicial de la selección de mesa.
     */
    private void cargarMesas() {
        try {
            String currentSelection = (mesaSeleccionadaParaPedido != null) ? mesaSeleccionadaParaPedido.getNombre() : null;

            comboBox_mesas.removeAllItems();
            comboBox_mesas.addItem("Seleccionar Mesa");

            List<Mesa> mesas = mesaDao.listarMesas();
            for (Mesa mesa : mesas) {
                comboBox_mesas.addItem(mesa.getNombre());
            }
            
            if (currentSelection != null) {
                comboBox_mesas.setSelectedItem(currentSelection);
            } else {
                comboBox_mesas.setEnabled(true);
                lblMesaSeleccionadaDisplay.setText("Mesa no seleccionada");
                lblMesaSeleccionadaDisplay.setForeground(Color.RED);
                btnCambiarMesa.setEnabled(false);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar las mesas: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Permite al usuario agregar una nueva mesa al sistema a través de un JOptionPane.
     */
    public void agregarMesa() {
        String nombreMesa = JOptionPane.showInputDialog(null, "Ingrese el nombre de la mesa:", "Nueva Mesa", JOptionPane.QUESTION_MESSAGE);

        if (nombreMesa != null && !nombreMesa.trim().isEmpty()) {
            try {
                mesaDao.agregarMesa(nombreMesa.trim());

                JOptionPane.showMessageDialog(null, "Mesa agregada correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarMesas();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error al agregar la mesa: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(null, "No se ingresó ningún nombre válido.", "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Valida la selección del pedido y lo envía a la base de datos.
     * Incluye la creación del pedido principal y sus detalles, así así como el registro de ventas.
     */
    private void validarYEnviarPedido() {
        if (mesaSeleccionadaParaPedido == null) {
            JOptionPane.showMessageDialog(this, "Por favor, selecciona una mesa para el pedido.", "Error de Mesa", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int idMesa = mesaSeleccionadaParaPedido.getId();
        String nombreMesaSeleccionada = mesaSeleccionadaParaPedido.getNombre();
        int idUsuario = this.userId;

        List<DetallePedidoConNombre> productosPedido = new ArrayList<>();
        boolean haySeleccion = false;
        boolean errorEnProductos = false;

        List<JPanel> panelesDeProductos = new ArrayList<>();
        if (panel_corrientes != null) panelesDeProductos.add(panel_corrientes);
        if (panel_especiales != null) panelesDeProductos.add(panel_especiales);

        for (JPanel panelCategoria : panelesDeProductos) {
            for (Component compCard : panelCategoria.getComponents()) {
                if (!(compCard instanceof JPanel)) continue;
                JPanel card = (JPanel) compCard;

                JCheckBox checkBox = null;
                JTextField txtCantidad = null;
                JCheckBox chkSopaIncluida = null;
                JComboBox<String> comboSopa = null;
                JTextArea txtDetalles = null;

                int checkBoxCount = 0;
                for (Component c : card.getComponents()) {
                    if (c instanceof JCheckBox) {
                        if (checkBoxCount == 0) checkBox = (JCheckBox) c;
                        else if (checkBoxCount == 1) chkSopaIncluida = (JCheckBox) c;
                        checkBoxCount++;
                    } else if (c instanceof JTextField) txtCantidad = (JTextField) c;
                    else if (c instanceof JComboBox) comboSopa = (JComboBox<String>) c;
                    else if (c instanceof JTextArea) txtDetalles = (JTextArea) c;
                }

                if (checkBox != null && checkBox.isSelected()) {
                    haySeleccion = true;

                    String nombreProducto = "Producto Desconocido";
                    for (Component c : card.getComponents()) {
                        if (c instanceof JLabel) {
                            JLabel lbl = (JLabel) c;
                            Object tipo = lbl.getClientProperty("tipo");
                            if ("nombre_producto".equals(tipo)) {
                                nombreProducto = lbl.getText();
                                break;
                            }
                        }
                    }

                    int cantidad = 0;
                    if (txtCantidad != null) {
                        String cantidadStr = txtCantidad.getText().trim();
                        if (cantidadStr.isEmpty()) {
                            JOptionPane.showMessageDialog(this, "La cantidad del producto '" + nombreProducto + "' no puede estar vacía.", "Error de Cantidad", JOptionPane.ERROR_MESSAGE);
                            errorEnProductos = true;
                        } else {
                            try {
                                cantidad = Integer.parseInt(cantidadStr);
                                if (cantidad <= 0) {
                                    JOptionPane.showMessageDialog(this, "La cantidad del producto '" + nombreProducto + "' debe ser mayor que cero.", "Error de Cantidad", JOptionPane.ERROR_MESSAGE);
                                    errorEnProductos = true;
                                }
                            } catch (NumberFormatException e) {
                                JOptionPane.showMessageDialog(this, "La cantidad del producto '" + nombreProducto + "' debe ser un número válido.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
                                errorEnProductos = true;
                            }
                        }
                    } else { 
                         JOptionPane.showMessageDialog(this, "Error interno: Campo de cantidad no encontrado para '" + nombreProducto + "'.", "Error Interno", JOptionPane.ERROR_MESSAGE);
                         errorEnProductos = true;
                    }

                    boolean sopaIncluida = false;
                    String tamanoSopa = null;

                    if (chkSopaIncluida != null && chkSopaIncluida.isSelected()) {
                        sopaIncluida = true;
                        if (comboSopa != null) {
                            String selectedSoupText = (String) comboSopa.getSelectedItem();
                            if (selectedSoupText == null || selectedSoupText.equalsIgnoreCase("Sin sopa") || selectedSoupText.trim().isEmpty()) {
                                JOptionPane.showMessageDialog(this, "Ha marcado 'Incluir Sopa' para '" + nombreProducto + "', pero no ha seleccionado un tamaño de sopa válido.", "Error de Sopa", JOptionPane.ERROR_MESSAGE);
                                errorEnProductos = true;
                            } else {
                                String[] partes = selectedSoupText.split(" ");
                                if (partes.length > 0) {
                                    tamanoSopa = partes[partes.length - 1];
                                }
                            }
                        } else {
                            JOptionPane.showMessageDialog(this, "Error interno: Combo de sopa no encontrado para '" + nombreProducto + "'.", "Error Interno", JOptionPane.ERROR_MESSAGE);
                            errorEnProductos = true;
                        }
                    }

                    if (!errorEnProductos && cantidad > 0) {
                        String detallesStr = txtDetalles != null ? txtDetalles.getText().trim() : "";
                        productosPedido.add(new DetallePedidoConNombre(nombreProducto, cantidad, sopaIncluida, tamanoSopa, detallesStr));
                    }
                }
            }
        }

        if (errorEnProductos) {
            return;
        }

        if (!haySeleccion) {
            JOptionPane.showMessageDialog(this, "Debes seleccionar al menos un producto para enviar el pedido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            Pedido nuevoPedido = new Pedido(idUsuario, idMesa, "Pendiente", null);

            System.out.println("Guardando pedido...");
            int idPedido = pedidoDao.guardarPedido(nuevoPedido);
            System.out.println("Pedido guardado con ID: " + idPedido);

            if (idPedido != -1) {
                nuevoPedido.setIdPedido(idPedido);
                
                System.out.println("Mesa: " + nombreMesaSeleccionada);
                System.out.println("ID Usuario: " + idUsuario);
                System.out.println("Detalles del pedido:");
                
                for (DetallePedidoConNombre pp : productosPedido) {
                    int idProducto = -1;
                    try {
                        Producto productoEncontrado = productoDao.obtenerProductoPorNombre(pp.getNombreProducto());
                        if (productoEncontrado != null) {
                            idProducto = productoEncontrado.getId();
                        } else {
                            System.err.println("Advertencia: Producto '" + pp.getNombreProducto() + "' no encontrado por nombre. El ID del producto será -1.");
                            JOptionPane.showMessageDialog(this, "Error interno: Producto '" + pp.getNombreProducto() + "' no encontrado en el sistema. Contacte a soporte.", "Error de Datos", JOptionPane.ERROR_MESSAGE);
                            continue;
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Error al obtener ID de producto para: " + pp.getNombreProducto() + ". " + e.getMessage(), "Error de BD", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }

                    detalleDAO.insertarDetalle(
                        idPedido,
                        idProducto,
                        pp.getCantidad(),
                        pp.isSopaIncluida(),
                        pp.getTamanoSopa(),
                        pp.getEspecificaciones()
                    );
                    
                    int precioUnitario = obtenerPrecioUnitario(pp.getNombreProducto());
                    int totalVenta = precioUnitario * pp.getCantidad();

                    String categoria = obtenerCategoriaProducto(pp.getNombreProducto());

                    Venta venta = new Venta(categoria, pp.getNombreProducto(), pp.getCantidad(), totalVenta);
                    gestorVentas.registrarVenta(venta);
                }

                pedidoDao.recalcularTotalPedido(idPedido);

                JOptionPane.showMessageDialog(this, "Pedido enviado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                
                resetFormularioParaNuevoPedido();

            } else {
                JOptionPane.showMessageDialog(this, "Error al guardar el pedido.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al guardar el pedido: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Limpia y resetea todos los campos del formulario de pedido para uno nuevo.
     */
    private void resetFormularioParaNuevoPedido() {
        resetMesaSeleccion();

        List<JPanel> panelesDeProductos = new ArrayList<>();
        if (panel_corrientes != null) panelesDeProductos.add(panel_corrientes);
        if (panel_especiales != null) panelesDeProductos.add(panel_especiales);

        for (JPanel panelCategoria : panelesDeProductos) {
            for (Component compCard : panelCategoria.getComponents()) {
                if (!(compCard instanceof JPanel)) continue;
                JPanel card = (JPanel) compCard;

                JCheckBox checkBox = null;
                JTextField txtCantidad = null;
                JCheckBox chkSopaIncluida = null;
                JComboBox<String> comboSopa = null;
                JTextArea txtDetalles = null;
                JLabel lblSopaEncontrado = null;

                int checkBoxCount = 0;
                for (Component c : card.getComponents()) {
                    if (c instanceof JCheckBox) {
                        if (checkBoxCount == 0) checkBox = (JCheckBox) c;
                        else if (checkBoxCount == 1) chkSopaIncluida = (JCheckBox) c;
                        checkBoxCount++;
                    } else if (c instanceof JTextField) txtCantidad = (JTextField) c;
                    else if (c instanceof JComboBox) comboSopa = (JComboBox<String>) c;
                    else if (c instanceof JTextArea) txtDetalles = (JTextArea) c;
                    else if (c instanceof JLabel) {
                        JLabel currentLabel = (JLabel) c;
                        if ("lblSopa".equals(currentLabel.getClientProperty("component_type"))) {
                            lblSopaEncontrado = currentLabel;
                        }
                    }
                }
                
                if (checkBox != null) {
                    checkBox.setSelected(false);
                    card.setBackground(new Color(255, 253, 245));
                    card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(200, 180, 150), 1),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                    ));
                }
                if (txtCantidad != null) {
                    txtCantidad.setText("1");
                }
                if (chkSopaIncluida != null) {
                    chkSopaIncluida.setSelected(false);
                }
                if (lblSopaEncontrado != null) {
                    lblSopaEncontrado.setEnabled(false);
                }
                if (comboSopa != null) {
                    comboSopa.setSelectedItem("Sin sopa");
                    comboSopa.setEnabled(false);
                }
                if (txtDetalles != null) {
                    txtDetalles.setText("");
                }
            }
        }
        panel_corrientes.revalidate();
        panel_corrientes.repaint();
        panel_especiales.revalidate();
        panel_especiales.repaint();
    }
    
    /**
     * Obtiene la categoría de un producto dado su nombre.
     * @param nombreProducto El nombre del producto.
     * @return La categoría del producto o "Desconocida" si no se encuentra.
     */
    public String obtenerCategoriaProducto(String nombreProducto) {
        Producto producto = null;

        try {
            producto = productoDao.obtenerProductoPorNombre(nombreProducto);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al cargar la categoría del producto: " + e.getMessage(), "Error de Datos", JOptionPane.ERROR_MESSAGE);
            return "Error";
        }
        return (producto != null) ? producto.getCategoria() : "Desconocida";
    }

    /**
     * Obtiene el precio unitario de un producto dado su nombre.
     * @param nombreProducto El nombre del producto.
     * @return El precio unitario del producto.
     * @throws RuntimeException si hay un error de base de datos o el producto no se encuentra.
     */
    private int obtenerPrecioUnitario(String nombreProducto) {
        Producto producto = null;

        try {
            producto = productoDao.obtenerProductoPorNombre(nombreProducto);
        } catch (SQLException e) {
            e.printStackTrace();
            String mensajeError = "Error de base de datos al obtener el precio de '" + nombreProducto + "'. Por favor, contacte a soporte.";
            System.err.println(mensajeError);
            throw new RuntimeException(mensajeError, e);
        }

        if (producto != null) {
            return producto.getPrecio().intValue();
        } else {
        	String mensajeAdvertencia = "Advertencia: Producto '" + nombreProducto + "' no encontrado en el inventario.";
            System.err.println(mensajeAdvertencia);
            JOptionPane.showMessageDialog(null, mensajeAdvertencia, "Producto No Encontrado", JOptionPane.WARNING_MESSAGE);
            return 0;
        }
    }

    private String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }
        return texto.substring(0, 1).toUpperCase() + texto.substring(1);
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public void recargarProductosDelMenu() {
        cargarProductosEnPanel(panel_corrientes, "corriente");
        cargarProductosEnPanel(panel_especiales, "especial");
    }
    
   
}