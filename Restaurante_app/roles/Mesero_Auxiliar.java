package Restaurante_app.roles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File; 
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList; 
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.stream.Collectors; 

import Restaurante_app.InicioRoles;
import Restaurante_app.DAO.PedidoDAO;
import Restaurante_app.DAO.DetallePedidoDAO; 
import Restaurante_app.Model.Pedido;
import Restaurante_app.Model.DetallePedidoConNombre; 

public class Mesero_Auxiliar extends JPanel {
    private static final long serialVersionUID = 1L;

    private JPanel panelCentral;
    private CardLayout cardLayout;
    private InicioRoles mainApp; 
    private String panelRegreso;

    private PedidoDAO pedidoDao = new PedidoDAO();
    private DetallePedidoDAO detallePedidoDao = new DetallePedidoDAO();

    private static String getRutaBaseImagenes() {
        String appDir = System.getProperty("user.dir");
        return appDir + File.separator + "imagenes";
    }

    public Mesero_Auxiliar(InicioRoles mainApp, String panelRegreso) {
        this.mainApp = mainApp; 
        this.panelRegreso = panelRegreso; 

        setBackground(new Color(255, 248, 240)); 
        setLayout(new BorderLayout(0, 0)); 
        
        JPanel topInfoPanel = new JPanel();
        topInfoPanel.setBackground(new Color(255, 248, 240));
        topInfoPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 10)); 
        
        JLabel lblTitle = new JLabel("GESTIÓN DE PEDIDOS - MESERO AUXILIAR");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(new Color(60, 44, 30));
        topInfoPanel.add(lblTitle);
        
        add(topInfoPanel, BorderLayout.NORTH);

        panelCentral = new JPanel();
        panelCentral.setBackground(new Color(255, 248, 240)); 
        cardLayout = new CardLayout();
        panelCentral.setLayout(cardLayout);
        
        JScrollPane mainScroll = new JScrollPane(panelCentral);
        mainScroll.setBorder(BorderFactory.createEmptyBorder()); 
        mainScroll.getVerticalScrollBar().setUnitIncrement(16); 
        add(mainScroll, BorderLayout.CENTER);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                cargarPedidos();
            }
        });

        Timer timer = new Timer(5000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Mesero_Auxiliar.this.isVisible()) { 
                    cargarPedidos();
                }
            }
        });
        timer.start();

        JPanel bottomButtonPanel = new JPanel();
        bottomButtonPanel.setBackground(new Color(255, 248, 240));
        bottomButtonPanel.setLayout(new BoxLayout(bottomButtonPanel, BoxLayout.Y_AXIS)); 
        bottomButtonPanel.setBorder(new EmptyBorder(10, 0, 10, 0)); 
        
        JButton btnCerrarSesion = new JButton("CERRAR SESIÓN");
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
        bottomButtonPanel.add(btnCerrarSesion);

        add(bottomButtonPanel, BorderLayout.SOUTH); 
    }

    public Mesero_Auxiliar() {
        this(null, "Login"); 
    }

    private void cargarPedidos() {
        panelCentral.removeAll(); 
        JPanel panelLista = new JPanel();
        panelLista.setBackground(new Color(255, 248, 240)); 
        panelLista.setLayout(new BoxLayout(panelLista, BoxLayout.Y_AXIS));
        panelLista.setBorder(new EmptyBorder(10, 10, 10, 10)); 

        try {
            List<Pedido> pedidos = pedidoDao.listarPedidosNoEntregados(); 
            
            if (pedidos.isEmpty()) {
                JLabel noPedidosLabel = new JLabel("No hay pedidos activos listos o pendientes en este momento.");
                noPedidosLabel.setFont(new Font("Arial", Font.ITALIC, 16));
                noPedidosLabel.setForeground(new Color(100, 100, 100));
                noPedidosLabel.setAlignmentX(Component.CENTER_ALIGNMENT); 
                panelLista.add(Box.createVerticalGlue()); 
                panelLista.add(noPedidosLabel);
                panelLista.add(Box.createVerticalGlue()); 
            } else {
                for (Pedido pedido : pedidos) {
                    if (!pedido.getEstado().equalsIgnoreCase("entregado") && !pedido.getEstado().equalsIgnoreCase("cancelado")) {
                        JPanel tarjeta = crearTarjetaPedido(pedido);
                        panelLista.add(tarjeta);
                        panelLista.add(Box.createVerticalStrut(15)); 
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JLabel errorLabel = new JLabel("Error al cargar pedidos: " + e.getMessage());
            errorLabel.setForeground(Color.RED);
            errorLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            panelLista.add(errorLabel);
        }

        panelCentral.add(panelLista, "lista");
        cardLayout.show(panelCentral, "lista");
        revalidate();
        repaint();
    }

    /**
     * Crea la tarjeta visual para un pedido individual, incluyendo sus detalles y checkboxes de entrega.
     * @param pedido El objeto Pedido a mostrar.
     * @return Un JPanel que representa la tarjeta del pedido.
     */
    private JPanel crearTarjetaPedido(Pedido pedido) {
        JPanel tarjeta = new JPanel(new BorderLayout(10, 10)); 
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(getPedidoBorderColor(pedido.getEstado()), 2), 
            new EmptyBorder(10, 15, 10, 15) 
        ));
        tarjeta.setMaximumSize(new Dimension(800, Short.MAX_VALUE)); 
        tarjeta.setAlignmentX(Component.CENTER_ALIGNMENT); 

        Color bgColor;
        switch (pedido.getEstado().toLowerCase()) { 
            case "listo":
                bgColor = new Color(255, 240, 200); 
                break;
            case "pendiente":
            default:
                bgColor = new Color(245, 245, 245); 
                break;
        }
        tarjeta.setBackground(bgColor);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false); 
        
        JLabel lblInfo = new JLabel("<html><b>Pedido # " + pedido.getIdPedido() + " | Mesa: " + pedido.getNombreMesa() + "</b></html>");
        lblInfo.setFont(new Font("Arial", Font.BOLD, 18));
        lblInfo.setForeground(new Color(60, 44, 30));
        headerPanel.add(lblInfo, BorderLayout.WEST);

        JLabel lblEstado = new JLabel("Estado: " + pedido.getEstado().toUpperCase());
        lblEstado.setFont(new Font("Arial", Font.ITALIC, 16));
        lblEstado.setForeground(getPedidoTextColor(pedido.getEstado()));
        headerPanel.add(lblEstado, BorderLayout.EAST);
        
        tarjeta.add(headerPanel, BorderLayout.NORTH);

        JPanel detallesPanel = new JPanel();
        detallesPanel.setOpaque(false);
        detallesPanel.setLayout(new BoxLayout(detallesPanel, BoxLayout.Y_AXIS));
        detallesPanel.setBorder(new EmptyBorder(5, 0, 5, 0)); 

        // Obtener la última versión de los detalles del pedido desde la BD
        List<DetallePedidoConNombre> detallesCompletos = null;
        try {
            detallesCompletos = detallePedidoDao.obtenerDetallesConNombrePorPedido(pedido.getIdPedido());
        } catch (SQLException e) {
            e.printStackTrace();
            JLabel errorLabel = new JLabel("Error al cargar detalles para el pedido " + pedido.getIdPedido() + ": " + e.getMessage());
            errorLabel.setForeground(Color.RED);
            errorLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            detallesPanel.add(errorLabel);
            detallesCompletos = new ArrayList<>(); 
        }
        
        List<JCheckBox> checkboxesSopas = new ArrayList<>();
        List<JCheckBox> checkboxesPlatos = new ArrayList<>();

        if (detallesCompletos.isEmpty()) {
            JLabel noDetalles = new JLabel("Este pedido no tiene detalles de productos.");
            noDetalles.setFont(new Font("Arial", Font.ITALIC, 12));
            noDetalles.setForeground(new Color(150, 150, 150));
            detallesPanel.add(noDetalles);
        } else {
            for (DetallePedidoConNombre detalle : detallesCompletos) {
                JPanel itemCard = crearItemProductoParaMeseroAuxiliar(detalle, pedido.getIdPedido(), checkboxesSopas, checkboxesPlatos);
                detallesPanel.add(itemCard);
            }
        }

        tarjeta.add(detallesPanel, BorderLayout.CENTER);

        JPanel bottomControls = new JPanel();
        bottomControls.setOpaque(false);
        bottomControls.setLayout(new BoxLayout(bottomControls, BoxLayout.Y_AXIS));
        bottomControls.setBorder(new EmptyBorder(10, 0, 0, 0));

        JCheckBox chkSopasEntregadas; 
        
        Map<String, Integer> sopaCounts = detallesCompletos.stream()
            .filter(DetallePedidoConNombre::isSopaIncluida)
            .collect(Collectors.groupingBy(
                d -> {
                    String key = "Sopa";
                    if (d.getTamanoSopa() != null && !d.getTamanoSopa().isEmpty()) {
                        key += " " + d.getTamanoSopa();
                    } else {
                        key += " (tamaño no especificado)";
                    }
                    return key;
                },
                Collectors.summingInt(DetallePedidoConNombre::getCantidad)
            ));

        if (!sopaCounts.isEmpty()) {
            String soupSummaryText = sopaCounts.entrySet().stream()
                .map(entry -> capitalizeFirstLetter(entry.getKey()) + " x" + entry.getValue())
                .collect(Collectors.joining(", "));
            
            chkSopasEntregadas = new JCheckBox("Todas las SOPAS entregadas (" + soupSummaryText + ")");
            chkSopasEntregadas.setFont(new Font("Arial", Font.BOLD, 14));
            chkSopasEntregadas.setForeground(new Color(200, 100, 0)); 
            chkSopasEntregadas.setOpaque(false);
            chkSopasEntregadas.setAlignmentX(Component.LEFT_ALIGNMENT);
            bottomControls.add(chkSopasEntregadas);
        } else {
            chkSopasEntregadas = new JCheckBox("Todas las SOPAS entregadas"); 
            chkSopasEntregadas.setVisible(false); 
        }

        bottomControls.add(Box.createVerticalStrut(10)); 

        JCheckBox chkPlatosEntregados = new JCheckBox("Todos los PLATOS entregados"); 
        chkPlatosEntregados.setFont(new Font("Arial", Font.BOLD, 14));
        chkPlatosEntregados.setForeground(new Color(0, 100, 0)); 
        chkPlatosEntregados.setOpaque(false);
        chkPlatosEntregados.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        boolean hasMainDishes = detallesCompletos.stream().anyMatch(d -> !d.isSopaIncluida());
        if (hasMainDishes) {
            bottomControls.add(chkPlatosEntregados);
        } else {
            chkPlatosEntregados.setVisible(false); 
        }
        
        bottomControls.add(Box.createVerticalStrut(10)); 
        
        // Listener para actualizar los checkboxes agregados (solo lectura, no controlan individuales)
        // Ya no necesitamos un ActionListener para chkSopasEntregadas y chkPlatosEntregados para controlar los individuales.
        // Se actualizarán visualmente al llamar a cargarPedidos() en el actionListener del chkItemServido.
        
        // Inicializar el estado de los checkboxes agregados basados en el estado actual de los ítems
        boolean allSoupsInitiallyServed = checkboxesSopas.isEmpty() || checkboxesSopas.stream().allMatch(JCheckBox::isSelected);
        boolean allPlatosInitiallyServed = checkboxesPlatos.isEmpty() || checkboxesPlatos.stream().allMatch(JCheckBox::isSelected);
        chkSopasEntregadas.setSelected(allSoupsInitiallyServed);
        chkPlatosEntregados.setSelected(allPlatosInitiallyServed);

        tarjeta.add(bottomControls, BorderLayout.SOUTH); 

        return tarjeta;
    }

    /**
     * Crea un JPanel para mostrar un ítem de producto individual para el Mesero Auxiliar.
     * Incluye validación de prioridad de sopas y actualización de estado de servicio.
     *
     * @param detalle Objeto DetallePedidoConNombre con la información del producto.
     * @param idPedido ID del pedido al que pertenece el detalle.
     * @param allSopasCheckboxes Lista de checkboxes de sopas para ese pedido (para inicialización).
     * @param allPlatosCheckboxes Lista de checkboxes de platos para ese pedido (para inicialización).
     * @return JPanel que representa el ítem del producto.
     */
    private JPanel crearItemProductoParaMeseroAuxiliar(DetallePedidoConNombre detalle, int idPedido, List<JCheckBox> allSopasCheckboxes, List<JCheckBox> allPlatosCheckboxes) {
        JPanel itemPanel = new JPanel(new BorderLayout(10, 0));
        itemPanel.setBackground(new Color(250, 250, 250)); 
        itemPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); 
        itemPanel.setMaximumSize(new Dimension(740, 90)); 
        itemPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

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
        itemPanel.add(lblImagen, BorderLayout.WEST);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(0, 5, 0, 0));

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

        JCheckBox chkItemServido = new JCheckBox("Producto Entregado");
        chkItemServido.setFont(new Font("Arial", Font.PLAIN, 12));
        chkItemServido.setBackground(new Color(250, 250, 250)); 
        chkItemServido.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        chkItemServido.putClientProperty("detalle", detalle); 

        // Inicializa el estado del checkbox basado en el estado de SERVICIO del producto en la DB
        boolean isServed = detalle.getEstadoServicio() != null && detalle.getEstadoServicio().equalsIgnoreCase("servido");
        chkItemServido.setSelected(isServed);

        // Añadir el checkbox a la lista correspondiente (sopas o platos) para el control agregado
        if (detalle.isSopaIncluida()) {
            allSopasCheckboxes.add(chkItemServido);
        } else {
            allPlatosCheckboxes.add(chkItemServido);
        }

        chkItemServido.addActionListener(e -> {
            boolean isChecked = chkItemServido.isSelected();
            
            try {
                // Obtener el estado actual del detalle desde la base de datos para la validación
                DetallePedidoConNombre currentDetalleInDb = detallePedidoDao.obtenerDetallePedidoPorId(detalle.getIdDetalle());

                if (currentDetalleInDb == null) {
                    JOptionPane.showMessageDialog(this, "Error: No se encontró el detalle del producto en la base de datos.", "Error de Datos", JOptionPane.ERROR_MESSAGE);
                    chkItemServido.setSelected(!isChecked); 
                    return;
                }

                if (isChecked) { // El Mesero Auxiliar intenta marcar como SERVIDO
                    // --- VALIDACIÓN DE PRIORIDAD DE SOPAS ---
                    if (!currentDetalleInDb.isSopaIncluida()) { // Si es un plato (no sopa)
                        List<DetallePedidoConNombre> todosDetallesDelPedido = detallePedidoDao.obtenerDetallesConNombrePorPedido(idPedido);
                        boolean allSoupsServedForOrder = todosDetallesDelPedido.stream()
                                                            .filter(DetallePedidoConNombre::isSopaIncluida)
                                                            .allMatch(d -> "servido".equalsIgnoreCase(d.getEstadoServicio()));
                        if (!allSoupsServedForOrder && todosDetallesDelPedido.stream().anyMatch(DetallePedidoConNombre::isSopaIncluida)) {
                            JOptionPane.showMessageDialog(this, 
                                "Todas las sopas de este pedido deben ser entregadas primero antes de servir los platos.", 
                                "Prioridad de Sopa", JOptionPane.WARNING_MESSAGE);
                            chkItemServido.setSelected(false); // Revertir el estado del checkbox en la UI
                            return; 
                        }
                    }
                    // --- FIN VALIDACIÓN DE PRIORIDAD DE SOPAS ---

                    // --- VALIDACIÓN DE ESTADO DE COCINA (debe estar listo o ya servido) ---
                    if ("listo".equalsIgnoreCase(currentDetalleInDb.getEstadoCocina())) {
                        detallePedidoDao.actualizarEstadoServicio(detalle.getIdDetalle(), "servido"); // Actualizar estado de SERVICIO
                        System.out.println("DEBUG: Producto '" + detalle.getNombreProducto() + "' (ID: " + detalle.getIdDetalle() + ") marcado como SERVIDO.");
                    } else if ("servido".equalsIgnoreCase(currentDetalleInDb.getEstadoServicio())) { // Ya servido por el auxiliar
                        System.out.println("DEBUG: Producto '" + detalle.getNombreProducto() + "' (ID: " + detalle.getIdDetalle() + ") ya estaba SERVIDO por el Mesero Auxiliar. No hay cambio.");
                        return; 
                    } else if ("servido".equalsIgnoreCase(currentDetalleInDb.getEstadoCocina())) { // Cocinero ya lo marcó como servido (debería ser "listo" para el mesero auxiliar)
                        // Esto podría indicar un estado inconsistente si el Cocinero marcó como "servido" y Mesero Auxiliar también intenta
                        // La lógica ideal es que Cocinero marque "listo", Mesero Auxiliar marque "servido".
                        detallePedidoDao.actualizarEstadoServicio(detalle.getIdDetalle(), "servido"); // Aún así, lo marcamos como servido por el Auxiliar
                        System.out.println("DEBUG: Producto '" + detalle.getNombreProducto() + "' (ID: " + detalle.getIdDetalle() + ") Cocinero lo tenía como servido, Mesero Auxiliar lo marca como SERVIDO.");
                    }
                    else {
                        // El producto no está listo por parte del cocinero
                        JOptionPane.showMessageDialog(this, 
                            "El producto '" + detalle.getNombreProducto() + "' (ID: " + detalle.getIdProducto() + ") aún no está listo en cocina. Estado actual del Cocinero: " + currentDetalleInDb.getEstadoCocina() + ".", 
                            "Producto no Listo", JOptionPane.WARNING_MESSAGE);
                        chkItemServido.setSelected(false); // Revertir el estado del checkbox en la UI
                        return; 
                    }
                } else { // El Mesero Auxiliar desmarca (intenta cambiar de SERVIDO a PENDIENTE de servicio)
                    // Siempre se permite al Mesero Auxiliar "des-servir" un producto, volviendo a "pendiente" de servicio
                    detallePedidoDao.actualizarEstadoServicio(detalle.getIdDetalle(), "pendiente"); // Cambiar estado de SERVICIO
                    System.out.println("DEBUG: Producto '" + detalle.getNombreProducto() + "' (ID: " + detalle.getIdDetalle() + ") desmarcado y estado de SERVICIO en BD cambiado a PENDIENTE.");
                }

                // Después de cualquier actualización exitosa de un ítem, verificar el estado del pedido completo
                verificarEstadoPedidoCompleto(idPedido); 
                // Y luego recargar todos los pedidos para actualizar la lista del Mesero Auxiliar
                cargarPedidos(); 

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(Mesero_Auxiliar.this, 
                    "Error al actualizar estado del producto '" + detalle.getNombreProducto() + "': " + ex.getMessage(), 
                    "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
                chkItemServido.setSelected(!isChecked); 
            }
        });


        contentPanel.add(lblProductoNombre);
        contentPanel.add(Box.createVerticalStrut(3));
        contentPanel.add(lblObs);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(chkItemServido);

        itemPanel.add(contentPanel, BorderLayout.CENTER);

        return itemPanel;
    }

    /**
     * Verifica el estado completo de un pedido basado en si todos sus detalles están "servido".
     * Si todos los detalles están servidos, actualiza el pedido a "entregado".
     * Si el pedido estaba "entregado" pero un detalle pasa a no estar "servido", lo revierte a "listo".
     * @param idPedido ID del pedido a verificar.
     */
    private void verificarEstadoPedidoCompleto(int idPedido) {
        try {
            Pedido pedido = pedidoDao.obtenerPedidoPorId(idPedido);
            if (pedido == null) return;

            // Obtener la lista más reciente de detalles de pedido directamente de la BD
            List<DetallePedidoConNombre> detallesActualizados = detallePedidoDao.obtenerDetallesConNombrePorPedido(idPedido);

            // Determinar si hay sopas o platos principales en el pedido
            boolean hasSoupsInOrder = detallesActualizados.stream().anyMatch(DetallePedidoConNombre::isSopaIncluida);
            boolean hasMainDishesInOrder = detallesActualizados.stream().anyMatch(d -> !d.isSopaIncluida());

            // Verificar si todos los ítems relevantes están en estado "servido" (por el mesero auxiliar)
            boolean allSoupsServed = true;
            if (hasSoupsInOrder) {
                allSoupsServed = detallesActualizados.stream()
                                    .filter(DetallePedidoConNombre::isSopaIncluida)
                                    .allMatch(d -> "servido".equalsIgnoreCase(d.getEstadoServicio()));
            }

            boolean allPlatosServed = true;
            if (hasMainDishesInOrder) {
                allPlatosServed = detallesActualizados.stream()
                                     .filter(d -> !d.isSopaIncluida())
                                     .allMatch(d -> "servido".equalsIgnoreCase(d.getEstadoServicio()));
            }

            // Lógica para actualizar el estado del pedido principal
            String nuevoEstadoPedido = pedido.getEstado(); 
            if (allSoupsServed && allPlatosServed) {
                if (!"entregado".equalsIgnoreCase(pedido.getEstado())) {
                    nuevoEstadoPedido = "entregado";
                }
            } else if ("entregado".equalsIgnoreCase(pedido.getEstado())) {
                nuevoEstadoPedido = "listo"; 
            }
            
            if (!nuevoEstadoPedido.equalsIgnoreCase(pedido.getEstado())) {
                pedidoDao.actualizarEstadoPedido(idPedido, nuevoEstadoPedido);
                System.out.println("DEBUG: Pedido " + idPedido + " estado cambiado a '" + nuevoEstadoPedido + "'.");
            } else {
                System.out.println("DEBUG: Pedido " + idPedido + " estado (" + pedido.getEstado() + ") no necesita actualización. Se mantiene igual.");
            }
        } catch (SQLException e) {
            System.err.println("ERROR al verificar/actualizar estado del pedido " + idPedido + ": " + e.getMessage());
            e.printStackTrace();
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

    private Color getPedidoTextColor(String estado) {
        return switch (estado.toLowerCase()) {
            case "pendiente" -> new Color(180, 0, 0);
            case "listo" -> new Color(0, 100, 0); 
            case "entregado" -> new Color(0, 80, 0); 
            default -> Color.BLACK;
        };
    }

    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}