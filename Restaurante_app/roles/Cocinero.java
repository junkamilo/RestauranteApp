package Restaurante_app.roles;

import javax.swing.*;
import Restaurante_app.DAO.DetallePedidoDAO;
import Restaurante_app.DAO.PedidoDAO;
import Restaurante_app.Model.DetallePedidoConNombre;
import Restaurante_app.Model.Pedido;
import Restaurante_app.InicioRoles; 

import java.awt.*;
import java.awt.event.*;
import java.io.File; 
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Cocinero extends JPanel {
    private static final long serialVersionUID = 1L;

    private JPanel panelPedidosList;
    private JScrollPane scrollPane;
    private JButton btnGuardar; 
    private JButton btnCerrarSesion;

    private List<PedidoCard> currentPedidoCards = new ArrayList<>();

    private final PedidoDAO pedidoDao = new PedidoDAO();
    private final DetallePedidoDAO detallePedidoDao = new DetallePedidoDAO();

    private InicioRoles mainApp; 
    private String panelRegreso;

    public Cocinero(InicioRoles mainApp, String panelRegreso) { 
        this.mainApp = mainApp; 
        this.panelRegreso = panelRegreso; 

        setLayout(new BorderLayout());
        setBackground(new Color(255, 248, 240));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setBackground(new Color(255, 248, 240));
        
        JLabel titulo = new JLabel("GESTIÓN DE COCINA - ÓRDENES PENDIENTES", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 22));
        titulo.setForeground(new Color(60, 44, 30));
        titulo.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0)); 
        topPanel.add(titulo);
        add(topPanel, BorderLayout.NORTH);

        panelPedidosList = new JPanel();
        panelPedidosList.setLayout(new BoxLayout(panelPedidosList, BoxLayout.Y_AXIS));
        panelPedidosList.setBackground(new Color(255, 248, 240));
        panelPedidosList.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        scrollPane = new JScrollPane(panelPedidosList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); 
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); 
        add(scrollPane, BorderLayout.CENTER);


        JPanel bottomButtonsContainer = new JPanel();
        bottomButtonsContainer.setLayout(new BoxLayout(bottomButtonsContainer, BoxLayout.Y_AXIS)); // Apilar verticalmente
        bottomButtonsContainer.setBackground(new Color(255, 248, 240));
        bottomButtonsContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        btnGuardar = new JButton("Actualizar Estados de Pedidos");
        btnGuardar.setFont(new Font("Arial", Font.BOLD, 16));
        btnGuardar.setBackground(new Color(50, 180, 100)); 
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnGuardar.putClientProperty("JButton.buttonType", "roundRect");
        btnGuardar.putClientProperty("JButton.arc", 10);
        btnGuardar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnGuardar.addActionListener(e -> actualizarEstados());
        bottomButtonsContainer.add(btnGuardar);
        
        bottomButtonsContainer.add(Box.createVerticalStrut(10)); 

        btnCerrarSesion = new JButton("CERRAR SESIÓN");
        btnCerrarSesion.setFont(new Font("Arial", Font.BOLD, 14));
        btnCerrarSesion.setBackground(new Color(180, 50, 50));
        btnCerrarSesion.setForeground(Color.WHITE);
        btnCerrarSesion.setFocusPainted(false);
        btnCerrarSesion.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btnCerrarSesion.putClientProperty("JButton.buttonType", "roundRect");
        btnCerrarSesion.putClientProperty("JButton.arc", 10);
        btnCerrarSesion.setAlignmentX(Component.CENTER_ALIGNMENT); 
        btnCerrarSesion.addActionListener(e -> {
            mainApp.volverAlPanelDeRol(); 
        });
        bottomButtonsContainer.add(btnCerrarSesion);

        add(bottomButtonsContainer, BorderLayout.SOUTH); 

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                cargarPedidos();
            }
        });

        new Timer(5000, e -> cargarPedidos()).start();
    }
    public Cocinero() { 
        this(null, "Login"); 
    }
    private static String getRutaBaseImagenes() {
        String appDir = System.getProperty("user.dir");
        return appDir + File.separator + "imagenes";
    }

    private void cargarPedidos() {
        try {
            List<Pedido> pedidosActualizados = pedidoDao.listarPedidosNoEntregados(); 

            currentPedidoCards.clear(); 
            panelPedidosList.removeAll(); 

            if (pedidosActualizados.isEmpty()) {
                JLabel noPedidosLabel = new JLabel("No hay pedidos pendientes o listos en este momento.");
                noPedidosLabel.setFont(new Font("Arial", Font.ITALIC, 16));
                noPedidosLabel.setForeground(new Color(120, 120, 120));
                noPedidosLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                panelPedidosList.add(Box.createVerticalGlue());
                panelPedidosList.add(noPedidosLabel);
                panelPedidosList.add(Box.createVerticalGlue());
            } else {
                for (Pedido pedido : pedidosActualizados) {
                    PedidoCard newCard = crearTarjetaPedido(pedido);
                    currentPedidoCards.add(newCard); 
                    panelPedidosList.add(newCard.panel);
                    panelPedidosList.add(Box.createVerticalStrut(10));
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar pedidos: " + e.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            panelPedidosList.revalidate();
            panelPedidosList.repaint();
        }
    }

    private PedidoCard crearTarjetaPedido(Pedido pedido) {
        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(getPedidoBorderColor(pedido.getEstado()), 2), 
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setMaximumSize(new Dimension(760, Integer.MAX_VALUE));
        tarjeta.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(getPedidoHeaderColor(pedido.getEstado())); 
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JCheckBox checkPedido = new JCheckBox("Pedido N° " + pedido.getIdPedido() + " | Mesa: " + pedido.getNombreMesa());
        checkPedido.setFont(new Font("Arial", Font.BOLD, 16));
        checkPedido.setForeground(Color.BLACK); 
        checkPedido.setOpaque(false); 

        JLabel lblEstadoPedido = new JLabel("Estado: " + pedido.getEstado());
        lblEstadoPedido.setFont(new Font("Arial", Font.ITALIC, 14));
        lblEstadoPedido.setForeground(getPedidoTextColor(pedido.getEstado()));
        lblEstadoPedido.setHorizontalAlignment(SwingConstants.RIGHT);

        headerPanel.add(checkPedido, BorderLayout.WEST);
        headerPanel.add(lblEstadoPedido, BorderLayout.EAST);
        tarjeta.add(headerPanel);
        tarjeta.add(Box.createVerticalStrut(5)); 

        JPanel productosPanel = new JPanel();
        productosPanel.setLayout(new BoxLayout(productosPanel, BoxLayout.Y_AXIS));
        productosPanel.setBackground(new Color(245, 245, 245));
        productosPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 5));
        productosPanel.setAlignmentX(Component.LEFT_ALIGNMENT); 

        List<JCheckBox> checkProductos = new ArrayList<>();
        List<DetallePedidoConNombre> detalles = new ArrayList<>();

        try {
            detalles = detallePedidoDao.obtenerDetallesConNombrePorPedido(pedido.getIdPedido());

            for (DetallePedidoConNombre detalle : detalles) {
                JPanel productoCard = crearTarjetaProducto(detalle, checkProductos, pedido.getIdPedido()); // Pasamos idPedido
                productosPanel.add(productoCard);
            }

        } catch (SQLException e) {
            JLabel errorLabel = new JLabel("Error al cargar detalles de productos: " + e.getMessage());
            errorLabel.setForeground(Color.RED);
            productosPanel.add(errorLabel);
            e.printStackTrace();
        }

        tarjeta.add(productosPanel);


        checkPedido.addActionListener(e -> {
            boolean seleccionado = checkPedido.isSelected();
            for (JCheckBox c : checkProductos) {
                if (c.isEnabled()) { 
                    c.setSelected(seleccionado);
                    for (ActionListener al : c.getActionListeners()) {
                        al.actionPerformed(new ActionEvent(c, ActionEvent.ACTION_PERFORMED, null));
                    }
                }
            }
        });

        actualizarEstadoCheckboxPedido(checkPedido, checkProductos, pedido.getEstado());

        return new PedidoCard(tarjeta, checkPedido, checkProductos, pedido);
    }

    private JPanel crearTarjetaProducto(DetallePedidoConNombre detalle, List<JCheckBox> checkProductosList, int idPedido) {
        JPanel productoPanel = new JPanel(new BorderLayout(10, 0)); 
        productoPanel.setBackground(Color.WHITE); // Default
        productoPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        productoPanel.setMaximumSize(new Dimension(740, 90)); 
        productoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // --- Lógica de color y estado para sopas servidas ---
        boolean isSopaServida = detalle.isSopaIncluida() && 
                                "servido".equalsIgnoreCase(detalle.getEstadoServicio());

        if (isSopaServida) {
            productoPanel.setBackground(new Color(190, 255, 190)); // Un verde claro para sopas servidas
            System.out.println("DEBUG (Cocinero): Sopa '" + detalle.getNombreProducto() + "' (ID Detalle: " + detalle.getIdDetalle() + ") está SERVIDA por el Mesero Auxiliar. Coloreando y deshabilitando.");
        }
        // --- Fin lógica de color y estado ---

        JLabel lblImagen = new JLabel();
        lblImagen.setPreferredSize(new Dimension(80, 80)); 
        lblImagen.setHorizontalAlignment(SwingConstants.CENTER);
        lblImagen.setBorder(BorderFactory.createLineBorder(new Color(200, 180, 150), 1));
        
        String nombreImagen = detalle.getImagen(); 
        File archivoImagen = null;

        if (nombreImagen != null && !nombreImagen.isEmpty()) {
            String rutaCompleta = getRutaBaseImagenes() + File.separator + nombreImagen; 
            archivoImagen = new File(rutaCompleta);
        }
        
        if (archivoImagen != null && archivoImagen.exists()) {
            ImageIcon icon = new ImageIcon(archivoImagen.getAbsolutePath());
            Image imagen = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            lblImagen.setIcon(new ImageIcon(imagen));
            lblImagen.setText(""); 
        } else {
            lblImagen.setIcon(null);
            lblImagen.setText("Sin img");
            lblImagen.setHorizontalAlignment(SwingConstants.CENTER);
            lblImagen.setVerticalAlignment(SwingConstants.CENTER);
            lblImagen.setForeground(new Color(150, 150, 150));
            lblImagen.setFont(new Font("Arial", Font.ITALIC, 10));
            System.err.println("Advertencia: Imagen no encontrada o nombre vacío para el producto: '" + detalle.getNombreProducto() + "'. Ruta esperada: " + (archivoImagen != null ? archivoImagen.getAbsolutePath() : "N/A"));
        }
        productoPanel.add(lblImagen, BorderLayout.WEST); 

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

        String textoProducto = detalle.getCantidad() + "x " + detalle.getNombreProducto();
        if (detalle.isSopaIncluida()) {
            textoProducto += " | Sopa: " + detalle.getTamanoSopa();
        }

        JLabel lblProductoNombre = new JLabel(textoProducto);
        lblProductoNombre.setFont(new Font("Arial", Font.BOLD, 14));
        lblProductoNombre.setForeground(new Color(60, 44, 30));
        lblProductoNombre.setAlignmentX(Component.LEFT_ALIGNMENT);

        String observaciones = (detalle.getEspecificaciones() != null && !detalle.getEspecificaciones().isEmpty()) ?
                                "Obs: " + detalle.getEspecificaciones() : "Sin observaciones";
        JLabel lblObs = new JLabel(observaciones);
        lblObs.setFont(new Font("Arial", Font.ITALIC, 12));
        lblObs.setForeground(new Color(100, 100, 100));
        lblObs.setAlignmentX(Component.LEFT_ALIGNMENT);

        JCheckBox checkProducto = new JCheckBox("Listo para cocinar");
        checkProducto.setFont(new Font("Arial", Font.PLAIN, 12));
        checkProducto.setBackground(new Color(250, 250, 250));
        checkProducto.setAlignmentX(Component.LEFT_ALIGNMENT);

        boolean isListo = detalle.getEstadoCocina() != null && detalle.getEstadoCocina().equalsIgnoreCase("listo");
        checkProducto.setSelected(isListo);
        if (isSopaServida) {
            checkProducto.setEnabled(false);
            checkProducto.setSelected(true);
            System.out.println("DEBUG (Cocinero): Checkbox de sopa '" + detalle.getNombreProducto() + "' deshabilitado y marcado.");
        }


        checkProducto.addActionListener(e -> {
            String nuevoEstado = checkProducto.isSelected() ? "listo" : "pendiente";
            System.out.println("DEBUG (Cocinero): Cambiando estado de '" + detalle.getNombreProducto() + "' (ID Detalle: " + detalle.getIdDetalle() + ") a " + nuevoEstado);
            try {
                detallePedidoDao.actualizarEstadoCocina(detalle.getIdDetalle(), nuevoEstado);
                verificarYActualizarEstadoPedido(idPedido); // Pasa el idPedido
                cargarPedidos();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al actualizar estado del producto: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
                checkProducto.setSelected(!checkProducto.isSelected());
            }
        });

        contentPanel.add(lblProductoNombre);
        contentPanel.add(Box.createVerticalStrut(3));
        contentPanel.add(lblObs);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(checkProducto);

        productoPanel.add(contentPanel, BorderLayout.CENTER);

        if (checkProductosList != null) {
            checkProductosList.add(checkProducto);
        }

        return productoPanel;
    }

 
    private void verificarYActualizarEstadoPedido(int idPedido) {
        try {
            List<DetallePedidoConNombre> detalles = detallePedidoDao.obtenerDetallesConNombrePorPedido(idPedido);
            boolean todosListos = true;
            Pedido pedidoActual = pedidoDao.obtenerPedidoPorId(idPedido);
            if (pedidoActual != null && "entregado".equalsIgnoreCase(pedidoActual.getEstado())) {
                System.out.println("DEBUG (Cocinero): Pedido " + idPedido + " ya entregado. No se actualiza el estado de cocina.");
                return; 
            }

            for (DetallePedidoConNombre detalle : detalles) {
                if (!"servido".equalsIgnoreCase(detalle.getEstadoServicio()) && 
                    !"listo".equalsIgnoreCase(detalle.getEstadoCocina())) {
                    todosListos = false;
                    break;
                }
            }

            Pedido pedido = pedidoDao.obtenerPedidoPorId(idPedido);
            if (pedido == null) return;

            String nuevoEstadoPedido = pedido.getEstado();
            if (todosListos && !"listo".equalsIgnoreCase(pedido.getEstado())) {
                nuevoEstadoPedido = "listo";
                System.out.println("DEBUG (Cocinero): Todos los items del pedido " + idPedido + " están listos/servidos. Cambiando estado a 'listo'.");
            } else if (!todosListos && "listo".equalsIgnoreCase(pedido.getEstado())) {
                nuevoEstadoPedido = "pendiente";
                System.out.println("DEBUG (Cocinero): No todos los items del pedido " + idPedido + " están listos/servidos. Cambiando estado a 'pendiente'.");
            }
            
            if (!nuevoEstadoPedido.equalsIgnoreCase(pedido.getEstado())) {
                pedidoDao.actualizarEstadoPedido(idPedido, nuevoEstadoPedido);
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar/actualizar estado del pedido " + idPedido + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void actualizarEstadoCheckboxPedido(JCheckBox checkPedido, List<JCheckBox> checkProductos, String estadoPedido) {
        boolean allProductsReadyOrServed = true;
        for (JCheckBox cb : checkProductos) {
            if (cb.isEnabled()) {
                if (!cb.isSelected()) {
                    allProductsReadyOrServed = false;
                    break;
                }
            } else {
                if (!cb.isSelected()) {
                    allProductsReadyOrServed = false;
                    break;
                }
            }
        }

        if (allProductsReadyOrServed || "listo".equalsIgnoreCase(estadoPedido)) {
            checkPedido.setSelected(true);
        } else {
            checkPedido.setSelected(false);
        }
    }

    private Color getPedidoBorderColor(String estado) {
        return switch (estado.toLowerCase()) {
            case "pendiente" -> new Color(255, 150, 150);
            case "listo" -> new Color(150, 200, 150);
            case "entregado" -> new Color(50, 180, 50);
            default -> new Color(200, 200, 200);
        };
    }

    private Color getPedidoHeaderColor(String estado) {
        return switch (estado.toLowerCase()) {
            case "pendiente" -> new Color(255, 200, 200);
            case "listo" -> new Color(200, 230, 200);
            case "entregado" -> new Color(100, 220, 100);
            default -> new Color(230, 230, 230);
        };
    }

    private Color getPedidoTextColor(String estado) {
        return switch (estado.toLowerCase()) {
            case "pendiente" -> new Color(180, 0, 0);
            case "listo" -> new Color(0, 100, 0);
            case "entregado" -> new Color(0, 80, 0); 
            default -> Color.BLACK;
        };
    }

    private static class PedidoCard {
        JPanel panel;
        JCheckBox checkPedido;
        List<JCheckBox> productos;
        Pedido pedido;

        PedidoCard(JPanel panel, JCheckBox checkPedido, List<JCheckBox> productos, Pedido pedido) {
            this.panel = panel;
            this.checkPedido = checkPedido;
            this.productos = productos;
            this.pedido = pedido;
        }
    }
    
    private void actualizarEstados() {
        cargarPedidos();
        JOptionPane.showMessageDialog(this, "Estados de pedidos recargados.", "Información", JOptionPane.INFORMATION_MESSAGE);
    }
}