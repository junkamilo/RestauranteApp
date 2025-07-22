package Restaurante_app.GestionPaneles;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import Restaurante_app.InicioRoles;
import Restaurante_app.DAO.PedidoDAO;
import Restaurante_app.DAO.DetallePedidoDAO;
import Restaurante_app.DAO.ProductoDao;
import Restaurante_app.Model.Pedido;
import Restaurante_app.Model.DetallePedidoConNombre;
import Restaurante_app.Model.Producto;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.stream.Collectors; 

public class Detalles_pedido extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JPanel panelListarPedidos;
    private final JPanel panelDetallePedido;
    private final CardLayout cardLayout;
    private final JPanel panelCentral;
    
    private InicioRoles mainApp;
    private String panelRegreso;
    
    private final PedidoDAO pedidoDao = new PedidoDAO();
    private final DetallePedidoDAO detallePedidoDao = new DetallePedidoDAO();
    private final ProductoDao productoDao = new ProductoDao();

    private Timer refreshTimer;
    private String currentCardName = "lista"; // Para saber qué vista está activa ("lista" o "detalle")
    private Pedido currentDisplayedPedido; // Variable para almacenar el Pedido actual cuando se muestra en detalle

    public Detalles_pedido(InicioRoles mainApp, String panelRegreso) {
        this.mainApp = mainApp;
        this.panelRegreso = panelRegreso;

        try {
            // Configuración de estilo global para botones (si es necesario)
            UIManager.put("Button.background", new Color(219, 168, 86));
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Button.font", new Font("Arial", Font.BOLD, 12));
            UIManager.put("Button.arc", 15);
            UIManager.put("Button.hoverBackground", new Color(235, 180, 95));
            UIManager.put("JButton.buttonType", "roundRect");
            UIManager.put("JButton.arc", 15); 
        } catch (Exception e) {
            e.printStackTrace();
        }

        setLayout(new BorderLayout());
        setBackground(new Color(255, 248, 240));
        setPreferredSize(new Dimension(360, 640));
        setName("Detalles_pedido");

        // Panel superior con botón de volver
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(new Color(255, 248, 240));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton btnVolverGlobal = new JButton("← VOLVER");
        btnVolverGlobal.addActionListener(e -> {
            System.out.println("DEBUG (Detalles_pedido): Botón 'VOLVER GLOBAL' presionado. Volviendo a " + this.panelRegreso);
            mainApp.mostrarPanel(this.panelRegreso);
        });
        topPanel.add(btnVolverGlobal);
        add(topPanel, BorderLayout.NORTH);

        // Panel central con CardLayout para alternar entre lista de pedidos y detalle de un pedido
        cardLayout = new CardLayout();
        panelCentral = new JPanel(cardLayout);
        panelCentral.setBackground(new Color(255, 248, 240));

        // Panel para la lista de todos los pedidos activos
        panelListarPedidos = new JPanel();
        panelListarPedidos.setLayout(new BoxLayout(panelListarPedidos, BoxLayout.Y_AXIS));
        panelListarPedidos.setBackground(new Color(255, 248, 240));
        panelListarPedidos.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPedidos = new JScrollPane(panelListarPedidos);
        scrollPedidos.setBorder(null);
        panelCentral.add(scrollPedidos, "lista");

        // Panel para mostrar los detalles de un pedido específico
        panelDetallePedido = new JPanel();
        panelDetallePedido.setLayout(new BoxLayout(panelDetallePedido, BoxLayout.Y_AXIS));
        panelDetallePedido.setBackground(new Color(255, 248, 240));
        panelDetallePedido.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollDetalle = new JScrollPane(panelDetallePedido);
        scrollDetalle.setBorder(null);
        panelCentral.add(scrollDetalle, "detalle");

        add(panelCentral, BorderLayout.CENTER);

        // --- Configuración del Timer para refresco dinámico ---
        refreshTimer = new Timer(3000, e -> { // Refresca cada 3 segundos
            // Si el panel no está visible, no tiene sentido seguir refrescando
            if (!this.isVisible()) {
                System.out.println("DEBUG (Detalles_pedido Timer): Panel no visible, deteniendo refresco.");
                return; 
            }
            if ("lista".equals(currentCardName)) {
                System.out.println("DEBUG (Detalles_pedido Timer): Refrescando lista de pedidos.");
                cargarPedidos();
            } else if ("detalle".equals(currentCardName) && currentDisplayedPedido != null) {
                System.out.println("DEBUG (Detalles_pedido Timer): Refrescando detalles del pedido ID: " + currentDisplayedPedido.getIdPedido());
                try {
                    // Obtener la última versión del pedido y sus detalles de la BD
                    Pedido latestPedido = pedidoDao.obtenerPedidoPorId(currentDisplayedPedido.getIdPedido());
                    if (latestPedido != null) {
                        // Re-renderizar el panel de detalle con los datos más recientes
                        mostrarDetallePedido(latestPedido); 
                    } else {
                        // Si el pedido ya no existe (ej. fue cancelado o entregado y ya no está activo), volver a la lista
                        System.out.println("DEBUG (Detalles_pedido Timer): Pedido ID " + currentDisplayedPedido.getIdPedido() + " ya no existe o no está activo, volviendo a la lista.");
                        cardLayout.show(panelCentral, "lista");
                        currentCardName = "lista";
                        currentDisplayedPedido = null; // Limpiar la referencia
                        cargarPedidos(); // Recargar la lista de pedidos activos
                    }
                } catch (SQLException ex) {
                    System.err.println("ERROR (Detalles_pedido Timer): Error al refrescar detalle del pedido: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
        // El timer se inicia/detiene en componentShown/Hidden para optimización
        // Es importante que el timer SOLO esté activo cuando el panel sea visible
        // para evitar llamadas innecesarias a la BD cuando el usuario está en otra vista.

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                System.out.println("DEBUG (Detalles_pedido): Panel Detalles_pedido se hizo visible.");
                cargarPedidos(); // Siempre carga la lista al mostrar el panel principal
                cardLayout.show(panelCentral, "lista"); 
                currentCardName = "lista";
                currentDisplayedPedido = null; // Resetear al volver a la lista
                refreshTimer.start(); // Iniciar el timer al mostrar el panel
                System.out.println("DEBUG (Detalles_pedido): Timer iniciado.");
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                System.out.println("DEBUG (Detalles_pedido): Panel Detalles_pedido se ocultó. Deteniendo refresco.");
                refreshTimer.stop(); // Detener el timer al ocultar el panel
            }
        });
    }

    public void setPanelRegreso(String panelRegreso) {
        this.panelRegreso = panelRegreso;
    }

    public void cargarPedidos() {
        panelListarPedidos.removeAll();
        panelListarPedidos.revalidate(); // Asegura que el panel se limpie antes de añadir
        panelListarPedidos.repaint();

        List<Pedido> pedidos;
        try {
            pedidos = pedidoDao.listarTodosActivos(); 
            System.out.println("DEBUG (Detalles_pedido - cargarPedidos): Pedidos activos cargados. Cantidad: " + pedidos.size());
        } catch (Exception ex) {
            ex.printStackTrace();
            JLabel errorLabel = new JLabel("Error al cargar pedidos: " + ex.getMessage());
            errorLabel.setForeground(Color.RED);
            errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelListarPedidos.add(Box.createVerticalGlue());
            panelListarPedidos.add(errorLabel);
            panelListarPedidos.add(Box.createVerticalGlue());
            panelListarPedidos.revalidate();
            panelListarPedidos.repaint();
            System.err.println("ERROR (Detalles_pedido): Error durante la carga de pedidos: " + ex.getMessage());
            return;
        }

        if (pedidos.isEmpty()) {
            JLabel noPedidosLabel = new JLabel("No hay pedidos activos en este momento.");
            noPedidosLabel.setFont(new Font("Arial", Font.ITALIC, 14));
            noPedidosLabel.setForeground(new Color(120, 120, 120));
            noPedidosLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelListarPedidos.add(Box.createVerticalGlue());
            panelListarPedidos.add(noPedidosLabel);
            panelListarPedidos.add(Box.createVerticalGlue());
            System.out.println("DEBUG (Detalles_pedido - cargarPedidos): No hay pedidos activos para mostrar.");
        } else {
            for (Pedido pedido : pedidos) {
                panelListarPedidos.add(crearTarjetaPedido(pedido));
                panelListarPedidos.add(Box.createRigidArea(new Dimension(0, 15)));
            }
        }
        
        panelListarPedidos.revalidate();
        panelListarPedidos.repaint();
        System.out.println("DEBUG (Detalles_pedido): Carga de lista de pedidos finalizada y UI refrescada.");
    }

    private JPanel crearTarjetaPedido(Pedido pedido) {
        JPanel tarjeta = new JPanel(new BorderLayout());
        tarjeta.setPreferredSize(new Dimension(320, 100));
        tarjeta.setMaximumSize(new Dimension(320, 100));
        tarjeta.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        Color borderColor = getPedidoBorderColor(pedido.getEstado());
        Color backgroundColor = getPedidoBackgroundColor(pedido.getEstado());

        tarjeta.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor, 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        tarjeta.setAlignmentX(Component.CENTER_ALIGNMENT);
        tarjeta.setBackground(backgroundColor);

        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setOpaque(false);

        JLabel lblNumero = new JLabel("Pedido #" + pedido.getIdPedido());
        lblNumero.setFont(new Font("Arial", Font.BOLD, 14));
        lblNumero.setForeground(new Color(60, 44, 30));
        lblNumero.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblMesa = new JLabel("Mesa: " + pedido.getNombreMesa()); 
        lblMesa.setFont(new Font("Arial", Font.PLAIN, 12));
        lblMesa.setForeground(new Color(60, 44, 30));
        lblMesa.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblEstado = new JLabel("Estado: " + pedido.getEstado());
        lblEstado.setFont(new Font("Arial", Font.BOLD, 12));
        lblEstado.setForeground(getPedidoTextColor(pedido.getEstado()));
        lblEstado.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTotal = new JLabel("Total: $" + (pedido.getTotalPedido() != null ? String.format("%.2f", pedido.getTotalPedido()) : "0.00"));
        lblTotal.setFont(new Font("Arial", Font.BOLD, 14));
        lblTotal.setForeground(new Color(0, 100, 0));
        lblTotal.setAlignmentX(Component.LEFT_ALIGNMENT);


        contenido.add(lblNumero);
        contenido.add(Box.createVerticalStrut(5));
        contenido.add(lblMesa);
        contenido.add(Box.createVerticalStrut(5));
        contenido.add(lblEstado);
        contenido.add(Box.createVerticalStrut(5));
        contenido.add(lblTotal);

        tarjeta.add(contenido, BorderLayout.CENTER);
        
        JPanel panelAccionesPedido = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        panelAccionesPedido.setOpaque(false);
        
        JButton btnCancelarPedido = new JButton("Cancelar");
        btnCancelarPedido.setFont(new Font("Arial", Font.BOLD, 10));
        btnCancelarPedido.setBackground(new Color(200, 70, 70));
        btnCancelarPedido.setForeground(Color.WHITE);
        btnCancelarPedido.putClientProperty("JButton.buttonType", "roundRect");
        btnCancelarPedido.putClientProperty("JButton.arc", 8);
        
        btnCancelarPedido.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de cancelar el Pedido #" + pedido.getIdPedido() + "? Esta acción no se puede deshacer.", "Confirmar Cancelación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                cancelarPedidoCompleto(pedido.getIdPedido());
            }
        });
        
        if (!"Cancelado".equalsIgnoreCase(pedido.getEstado()) && !"Entregado".equalsIgnoreCase(pedido.getEstado())) {
            panelAccionesPedido.add(btnCancelarPedido);
        }

        tarjeta.add(panelAccionesPedido, BorderLayout.EAST);

        tarjeta.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getSource() == tarjeta && !(e.getSource() instanceof JButton)) { // Asegura que no se active al hacer clic en un botón dentro de la tarjeta
                     System.out.println("DEBUG (Detalles_pedido): Tarjeta de pedido #" + pedido.getIdPedido() + " clickeada. Mostrando detalles.");
                     mostrarDetallePedido(pedido);
                }
            }
        });

        return tarjeta;
    }
    
    private void cancelarDetalleProducto(int idDetalle, int idPedidoPadre) {
        try {
            boolean exito = detallePedidoDao.eliminarDetalle(idDetalle);
            if (exito) {
                JOptionPane.showMessageDialog(this, "Producto cancelado del pedido.", "Producto Cancelado", JOptionPane.INFORMATION_MESSAGE);
                // Recargar el detalle del pedido para reflejar los cambios
                // Se recalcula el total automáticamente en eliminarDetalle
                Pedido pedidoActualizado = pedidoDao.obtenerPedidoPorId(idPedidoPadre); // Obtener el pedido más reciente
                if (pedidoActualizado != null) {
                    mostrarDetallePedido(pedidoActualizado); // Refrescar la vista de detalle con el pedido actualizado
                } else {
                    // Si el pedido padre ya no existe (ej. se vació y se eliminó), volver a la lista
                    cargarPedidos(); 
                    cardLayout.show(panelCentral, "lista");
                    currentCardName = "lista";
                    currentDisplayedPedido = null; // Limpiar la referencia
                }
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo cancelar el producto.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error de base de datos al cancelar producto: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelarPedidoCompleto(int idPedido) {
        try {
            pedidoDao.actualizarEstadoPedido(idPedido, "Cancelado");
            JOptionPane.showMessageDialog(this, "Pedido #" + idPedido + " ha sido cancelado.", "Pedido Cancelado", JOptionPane.INFORMATION_MESSAGE);
            cargarPedidos(); 
            cardLayout.show(panelCentral, "lista"); // Volver a la lista después de cancelar
            currentCardName = "lista";
            currentDisplayedPedido = null; // Limpiar la referencia
        }
        catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cancelar pedido: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void mostrarDetallePedido(Pedido pedido) {
        panelDetallePedido.removeAll(); // Limpiar el panel antes de añadir nuevos componentes
        currentDisplayedPedido = pedido; // Almacenar el pedido que se está mostrando
        System.out.println("DEBUG (Detalles_pedido): Mostrando detalles para Pedido ID: " + pedido.getIdPedido() + ". Almacenado para refresco.");

        // Botón de volver a la lista de pedidos
        JButton btnVolverDetalle = new JButton("← VOLVER A PEDIDOS");
        btnVolverDetalle.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnVolverDetalle.addActionListener(e -> {
            System.out.println("DEBUG (Detalles_pedido): Botón 'VOLVER A PEDIDOS' presionado. Volviendo a la lista.");
            cardLayout.show(panelCentral, "lista");
            currentCardName = "lista";
            currentDisplayedPedido = null; // Limpiar la referencia del pedido en detalle
            cargarPedidos(); // Recargar la lista de pedidos al volver
        });
        panelDetallePedido.add(btnVolverDetalle);
        panelDetallePedido.add(Box.createRigidArea(new Dimension(0, 15)));

        // Títulos e información general del pedido
        JLabel titulo = new JLabel("Detalles del Pedido #" + pedido.getIdPedido());
        titulo.setFont(new Font("Arial", Font.BOLD, 16));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        titulo.setForeground(new Color(60, 44, 30));
        panelDetallePedido.add(titulo);
        panelDetallePedido.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel infoMesa = new JLabel("Mesa: " + pedido.getNombreMesa() + " | Estado del Pedido: " + pedido.getEstado());
        infoMesa.setFont(new Font("Arial", Font.PLAIN, 14));
        infoMesa.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoMesa.setForeground(getPedidoTextColor(pedido.getEstado()));
        panelDetallePedido.add(infoMesa);
        panelDetallePedido.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel lblTotalPedidoDetalle = new JLabel("Total del Pedido: $" + (pedido.getTotalPedido() != null ? String.format("%.2f", pedido.getTotalPedido()) : "0.00"));
        lblTotalPedidoDetalle.setFont(new Font("Arial", Font.BOLD, 16));
        lblTotalPedidoDetalle.setForeground(new Color(0, 120, 0));
        lblTotalPedidoDetalle.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelDetallePedido.add(lblTotalPedidoDetalle);
        panelDetallePedido.add(Box.createRigidArea(new Dimension(0, 15)));

        List<DetallePedidoConNombre> detalles;
        try {
            detalles = detallePedidoDao.obtenerDetallesConNombrePorPedido(pedido.getIdPedido());
            System.out.println("DEBUG (Detalles_pedido - mostrarDetallePedido): Detalles cargados para Pedido ID: " + pedido.getIdPedido() + ". Cantidad de detalles: " + detalles.size());
        } catch (Exception ex) {
            ex.printStackTrace();
            JLabel errorDetalle = new JLabel("Error al cargar detalles: " + ex.getMessage());
            errorDetalle.setForeground(Color.RED);
            errorDetalle.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelDetallePedido.add(errorDetalle);
            panelDetallePedido.revalidate();
            panelDetallePedido.repaint();
            cardLayout.show(panelCentral, "detalle"); // Aún así mostrar la tarjeta de detalle aunque haya error
            currentCardName = "detalle";
            System.err.println("ERROR (Detalles_pedido): Error durante la carga de detalles de pedido: " + ex.getMessage());
            return;
        }

        if (detalles.isEmpty()) {
            JLabel noDetallesLabel = new JLabel("Este pedido no tiene detalles de productos.");
            noDetallesLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            noDetallesLabel.setForeground(new Color(120, 120, 120));
            noDetallesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelDetallePedido.add(noDetallesLabel);
            System.out.println("DEBUG (Detalles_pedido - mostrarDetallePedido): No hay detalles para el Pedido ID: " + pedido.getIdPedido());
        } else {
            for (DetallePedidoConNombre d : detalles) {
                panelDetallePedido.add(crearTarjetaProductoDetalle(d, pedido)); // Pasa el pedido padre para la lógica de cancelación/edición
                panelDetallePedido.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }
        
        panelDetallePedido.revalidate();
        panelDetallePedido.repaint();
        cardLayout.show(panelCentral, "detalle"); // Cambiar a la vista de detalle
        currentCardName = "detalle"; // Actualizar el nombre de la tarjeta actual
        System.out.println("DEBUG (Detalles_pedido): Vista de detalle de pedido ID " + pedido.getIdPedido() + " mostrada y UI refrescada.");
    }

    private JPanel crearTarjetaProductoDetalle(DetallePedidoConNombre detalle, Pedido parentPedido) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        
        Color backgroundColor;
        Color textColor;
        
        // Lógica de color de fondo y texto para la tarjeta de detalle del producto
        // priorizando "servido" para sopas y platos, luego el estado de cocina.
        if ("servido".equalsIgnoreCase(detalle.getEstadoServicio())) {
            backgroundColor = new Color(190, 255, 190); // Verde claro si el producto está servido (Mesero Auxiliar)
            textColor = new Color(0, 100, 0); // Texto más oscuro para contraste
            System.out.println("DEBUG (Detalles_pedido - crearTarjetaProductoDetalle): Producto '" + detalle.getNombreProducto() + "' (ID Detalle: " + detalle.getIdDetalle() + ") está SERVIDO. Coloreando verde claro.");
        } else if ("listo".equalsIgnoreCase(detalle.getEstadoCocina())) {
            backgroundColor = new Color(255, 240, 200); // Amarillo claro si está listo (Cocinero)
            textColor = new Color(180, 120, 0); 
            System.out.println("DEBUG (Detalles_pedido - crearTarjetaProductoDetalle): Producto '" + detalle.getNombreProducto() + "' (ID Detalle: " + detalle.getIdDetalle() + ") está LISTO en cocina. Coloreando amarillo claro.");
        } else if ("en preparacion".equalsIgnoreCase(detalle.getEstadoCocina())) {
            backgroundColor = new Color(255, 220, 180); // Naranja claro si está en preparación
            textColor = new Color(150, 90, 0);
             System.out.println("DEBUG (Detalles_pedido - crearTarjetaProductoDetalle): Producto '" + detalle.getNombreProducto() + "' (ID Detalle: " + detalle.getIdDetalle() + ") está EN PREPARACION. Coloreando naranja claro.");
        }
        else {
            backgroundColor = new Color(245, 245, 245); // Color por defecto (pendiente)
            textColor = new Color(60, 44, 30);
            System.out.println("DEBUG (Detalles_pedido - crearTarjetaProductoDetalle): Producto '" + detalle.getNombreProducto() + "' (ID Detalle: " + detalle.getIdDetalle() + ") está PENDIENTE. Coloreando gris claro.");
        }

        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        card.setBackground(backgroundColor);
        card.setMaximumSize(new Dimension(320, 180));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblProducto = new JLabel(detalle.getNombreProducto());
        lblProducto.setFont(new Font("Arial", Font.BOLD, 14));
        lblProducto.setForeground(new Color(60, 44, 30));
        lblProducto.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblCantidad = new JLabel("Cantidad: " + detalle.getCantidad());
        lblCantidad.setFont(new Font("Arial", Font.PLAIN, 12));
        lblCantidad.setForeground(new Color(60, 44, 30)); // Consistencia en el color del texto
        lblCantidad.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Mostrar estado de servicio si es relevante para el tipo de producto
        String sopaInfo = "Sopa: " + (detalle.isSopaIncluida() ? detalle.getTamanoSopa() : "No");
        JLabel lblSopa = new JLabel(sopaInfo);
        lblSopa.setFont(new Font("Arial", Font.PLAIN, 12));
        lblSopa.setForeground(new Color(60, 44, 30));
        lblSopa.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblObs = new JLabel("Observaciones: " + (detalle.getEspecificaciones() != null && !detalle.getEspecificaciones().isEmpty() ? detalle.getEspecificaciones() : "Ninguna"));
        lblObs.setFont(new Font("Arial", Font.ITALIC, 12));
        lblObs.setForeground(new Color(60, 44, 30));
        lblObs.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Mostrar Estado Cocina y Estado Servicio
        JLabel lblEstadoCocina = new JLabel("Estado Cocina: " + detalle.getEstadoCocina());
        lblEstadoCocina.setFont(new Font("Arial", Font.BOLD, 12));
        lblEstadoCocina.setForeground(getProductoStateTextColor(detalle.getEstadoCocina())); // Color basado en estado cocina
        lblEstadoCocina.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblEstadoServicio = new JLabel("Estado Servicio: " + detalle.getEstadoServicio());
        lblEstadoServicio.setFont(new Font("Arial", Font.BOLD, 12));
        lblEstadoServicio.setForeground(getProductoStateTextColor(detalle.getEstadoServicio())); // Color basado en estado servicio
        lblEstadoServicio.setAlignmentX(Component.LEFT_ALIGNMENT);


        card.add(lblProducto);
        card.add(Box.createVerticalStrut(5));
        card.add(lblCantidad);
        card.add(Box.createVerticalStrut(5));
        card.add(lblSopa);
        card.add(Box.createVerticalStrut(5));
        card.add(lblObs);
        card.add(Box.createVerticalStrut(5));
        card.add(lblEstadoCocina);
        card.add(Box.createVerticalStrut(5)); // Espacio entre estados
        card.add(lblEstadoServicio);
        card.add(Box.createVerticalStrut(10));

        JPanel panelAccionesDetalle = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        panelAccionesDetalle.setOpaque(false);
        panelAccionesDetalle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnEditarDetalle = new JButton("Editar");
        btnEditarDetalle.setFont(new Font("Arial", Font.BOLD, 10));
        btnEditarDetalle.setBackground(new Color(70, 130, 180));
        btnEditarDetalle.setForeground(Color.WHITE);
        btnEditarDetalle.putClientProperty("JButton.buttonType", "roundRect");
        btnEditarDetalle.putClientProperty("JButton.arc", 8);
        
        // Solo permitir editar si el pedido no ha sido cancelado o entregado
        if (!"Cancelado".equalsIgnoreCase(parentPedido.getEstado()) && !"Entregado".equalsIgnoreCase(parentPedido.getEstado())) {
            btnEditarDetalle.addActionListener(e -> editarDetalleProducto(detalle, parentPedido));
            panelAccionesDetalle.add(btnEditarDetalle);
        }


        JButton btnCancelarDetalle = new JButton("Cancelar Producto");
        btnCancelarDetalle.setFont(new Font("Arial", Font.BOLD, 10));
        btnCancelarDetalle.setBackground(new Color(200, 70, 70));
        btnCancelarDetalle.setForeground(Color.WHITE);
        btnCancelarDetalle.putClientProperty("JButton.buttonType", "roundRect");
        btnCancelarDetalle.putClientProperty("JButton.arc", 8);
        
        // Solo permitir cancelar si el pedido no ha sido cancelado o entregado
        if (!"Cancelado".equalsIgnoreCase(parentPedido.getEstado()) && !"Entregado".equalsIgnoreCase(parentPedido.getEstado())) {
            btnCancelarDetalle.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de cancelar este producto del pedido?", "Confirmar Cancelación de Producto", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    cancelarDetalleProducto(detalle.getIdDetalle(), parentPedido.getIdPedido());
                }
            });
            panelAccionesDetalle.add(btnCancelarDetalle);
        }

        card.add(panelAccionesDetalle);

        return card;
    }

    // Método para abrir el diálogo de edición de un detalle de producto
    private void editarDetalleProducto(DetallePedidoConNombre detalleToEdit, Pedido parentPedido) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Editar Producto del Pedido", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setBackground(new Color(255, 248, 240));
        dialog.setPreferredSize(new Dimension(400, 580)); 

        JPanel panelContenido = new JPanel(new GridBagLayout()); 
        panelContenido.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); 
        panelContenido.setBackground(new Color(255, 248, 240));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5); 
        gbc.fill = GridBagConstraints.HORIZONTAL; 

        // --- Título del Diálogo ---
        JLabel lblTitulo = new JLabel("Editar Detalles del Pedido", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setForeground(new Color(60, 44, 30));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; 
        gbc.anchor = GridBagConstraints.CENTER;
        panelContenido.add(lblTitulo, gbc);

        // --- Separador visual ---
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(180, 140, 100));
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 0, 20, 0); 
        panelContenido.add(separator, gbc);
        gbc.insets = new Insets(8, 5, 8, 5); 

        // --- Campo Producto (¡NUEVO!) ---
        JLabel lblProductoActual = new JLabel("<html><b>Producto Actual:</b> " + detalleToEdit.getNombreProducto() + "</html>");
        lblProductoActual.setFont(new Font("Arial", Font.BOLD, 14));
        lblProductoActual.setForeground(new Color(60, 44, 30));
        gbc.gridy = 2;
        panelContenido.add(lblProductoActual, gbc);
        
        JLabel lblSeleccionarProducto = new JLabel("<html><b>Seleccionar Nuevo Plato:</b></html>");
        lblSeleccionarProducto.setFont(new Font("Arial", Font.BOLD, 12));
        lblSeleccionarProducto.setForeground(new Color(90, 70, 50));
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panelContenido.add(lblSeleccionarProducto, gbc);

        JComboBox<Producto> comboProductos = new JComboBox<>();
        comboProductos.setFont(new Font("Arial", Font.PLAIN, 14));
        comboProductos.setBackground(Color.WHITE);
        comboProductos.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Producto) {
                    setText(((Producto) value).getNombre());
                }
                return this;
            }
        });
        
        List<Producto> menuProductos;
        try {
            // Filtrar productos para no mostrar sopas si estamos editando un plato, o viceversa,
            // aunque el diálogo permite cambiar tipo de producto. Por simplicidad, se listan todos.
            menuProductos = productoDao.listarMenuDelDia(); 
            for (Producto p : menuProductos) {
                comboProductos.addItem(p);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(dialog, "Error al cargar los productos del menú: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Seleccionar el producto actual en el combo box si está disponible
        boolean foundCurrentProduct = false;
        for (int i = 0; i < comboProductos.getItemCount(); i++) {
            Producto p = comboProductos.getItemAt(i);
            if (p.getId() == detalleToEdit.getIdProducto()) {
                comboProductos.setSelectedIndex(i);
                foundCurrentProduct = true;
                break;
            }
        }
        // Si el producto original no está en el menú del día (ej. fue descontinuado)
        if (!foundCurrentProduct && detalleToEdit.getIdProducto() != 0) {
            try {
                Producto originalProduct = productoDao.obtenerProductoPorId(detalleToEdit.getIdProducto());
                if (originalProduct != null) {
                    comboProductos.insertItemAt(originalProduct, 0); // Añadirlo al inicio
                    comboProductos.setSelectedIndex(0); // Seleccionarlo
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panelContenido.add(comboProductos, gbc);
        
        // --- Campo Cantidad ---
        JLabel lblCantidad = new JLabel("Cantidad:");
        lblCantidad.setFont(new Font("Arial", Font.BOLD, 12));
        lblCantidad.setForeground(new Color(90, 70, 50));
        gbc.gridy = 5;
        gbc.gridwidth = 1; 
        gbc.anchor = GridBagConstraints.WEST;
        panelContenido.add(lblCantidad, gbc);
        
        JTextField txtCantidad = new JTextField(String.valueOf(detalleToEdit.getCantidad()));
        txtCantidad.setFont(new Font("Arial", Font.PLAIN, 14));
        txtCantidad.setPreferredSize(new Dimension(80, 30)); 
        txtCantidad.setMaximumSize(new Dimension(100, 30)); 
        txtCantidad.setBorder(BorderFactory.createLineBorder(new Color(180, 140, 100)));
        txtCantidad.putClientProperty("JTextField.roundRect", true);
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.EAST; 
        panelContenido.add(txtCantidad, gbc);
        gbc.gridx = 0; 

        // --- Campo Sopa ---
        JLabel lblSopa = new JLabel("Sopa:");
        lblSopa.setFont(new Font("Arial", Font.BOLD, 12));
        lblSopa.setForeground(new Color(90, 70, 50));
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panelContenido.add(lblSopa, gbc);

        JCheckBox chkSopaIncluida = new JCheckBox("Incluir Sopa");
        chkSopaIncluida.setSelected(detalleToEdit.isSopaIncluida());
        chkSopaIncluida.setBackground(new Color(255, 248, 240));
        chkSopaIncluida.setFont(new Font("Arial", Font.PLAIN, 12));
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.EAST;
        panelContenido.add(chkSopaIncluida, gbc);
        gbc.gridx = 0;

        JComboBox<String> comboSopa = new JComboBox<>();
        comboSopa.addItem("Sin sopa"); // Opción predeterminada
        try {
            // Listar solo las sopas de la categoría "sopa"
            List<Producto> sopas = productoDao.listarMenuDelDiaPorCategoria("sopa");
            for (Producto sopa : sopas) {
                comboSopa.addItem("Sopa " + sopa.getNombre() + " completa");
                comboSopa.addItem("Sopa " + sopa.getNombre() + " mediana");
                comboSopa.addItem("Sopa " + sopa.getNombre() + " pequeña");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(dialog, "Error al cargar sopas: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        // Seleccionar el tamaño de sopa actual si ya estaba incluida
        if (detalleToEdit.isSopaIncluida() && detalleToEdit.getTamanoSopa() != null && !detalleToEdit.getTamanoSopa().isEmpty()) {
            boolean found = false;
            for (int i = 0; i < comboSopa.getItemCount(); i++) {
                if (comboSopa.getItemAt(i).contains(detalleToEdit.getTamanoSopa())) {
                    comboSopa.setSelectedIndex(i);
                    found = true;
                    break;
                }
            }
            if (!found) comboSopa.setSelectedItem("Sin sopa");
        } else {
            comboSopa.setSelectedItem("Sin sopa");
        }
        
        // Habilitar/deshabilitar el combo de sopa basado en el checkbox
        comboSopa.setEnabled(chkSopaIncluida.isSelected());
        
        // Listener para habilitar/deshabilitar el combo de sopa
        chkSopaIncluida.addActionListener(e -> {
            boolean selected = chkSopaIncluida.isSelected();
            comboSopa.setEnabled(selected);
            if (!selected) { // Si se desmarca, seleccionar "Sin sopa"
                comboSopa.setSelectedItem("Sin sopa");
            }
        });
        
        gbc.gridy = 7;
        gbc.gridx = 0;
        gbc.gridwidth = 2; 
        gbc.anchor = GridBagConstraints.WEST;
        panelContenido.add(comboSopa, gbc);

        // --- Campo Observaciones ---
        JLabel lblObs = new JLabel("Observaciones:");
        lblObs.setFont(new Font("Arial", Font.BOLD, 12));
        lblObs.setForeground(new Color(90, 70, 50));
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panelContenido.add(lblObs, gbc);

        JTextArea txtObservaciones = new JTextArea(detalleToEdit.getEspecificaciones());
        txtObservaciones.setLineWrap(true);
        txtObservaciones.setWrapStyleWord(true);
        txtObservaciones.setFont(new Font("Arial", Font.PLAIN, 14));
        txtObservaciones.setBorder(BorderFactory.createLineBorder(new Color(180, 140, 100)));
        JScrollPane scrollObs = new JScrollPane(txtObservaciones);
        scrollObs.setPreferredSize(new Dimension(300, 80)); 
        scrollObs.putClientProperty("JScrollPane.roundRect", true);
        gbc.gridy = 9;
        panelContenido.add(scrollObs, gbc);

        // --- Espaciador para empujar los botones hacia abajo ---
        gbc.gridy = 10;
        gbc.weighty = 1.0; // Hace que este componente empuje los demás hacia arriba
        panelContenido.add(Box.createVerticalGlue(), gbc);
        gbc.weighty = 0; // Resetear el peso para el siguiente componente

        // --- Botones Guardar y Cancelar ---
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBotones.setOpaque(false);
        panelBotones.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0)); 

        JButton btnGuardar = new JButton("Guardar Cambios");
        btnGuardar.setFont(new Font("Arial", Font.BOLD, 12));
        btnGuardar.setBackground(new Color(80, 180, 100)); 
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.putClientProperty("JButton.buttonType", "roundRect");
        btnGuardar.putClientProperty("JButton.arc", 10);
        btnGuardar.addActionListener(e -> {
            try {
                Producto nuevoProductoSeleccionado = (Producto) comboProductos.getSelectedItem();
                if (nuevoProductoSeleccionado == null) {
                    JOptionPane.showMessageDialog(dialog, "Debe seleccionar un plato.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                int nuevaCantidad = Integer.parseInt(txtCantidad.getText().trim());
                if (nuevaCantidad <= 0) {
                    JOptionPane.showMessageDialog(dialog, "La cantidad debe ser mayor que cero.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                boolean nuevaSopaIncluida = chkSopaIncluida.isSelected();
                String nuevoTamanoSopa = nuevaSopaIncluida ? (String) comboSopa.getSelectedItem() : null;
                if (nuevaSopaIncluida && "Sin sopa".equals(nuevoTamanoSopa)) {
                    JOptionPane.showMessageDialog(dialog, "Debe seleccionar un tamaño de sopa si ha marcado 'Incluir Sopa'.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                String nuevasEspecificaciones = txtObservaciones.getText().trim();

                // Crear un nuevo objeto DetallePedidoConNombre con los datos actualizados
                DetallePedidoConNombre detalleActualizado = new DetallePedidoConNombre(
                    nuevoProductoSeleccionado.getNombre(), // Nombre del producto del nuevo producto seleccionado
                    nuevaCantidad,
                    nuevaSopaIncluida,
                    nuevoTamanoSopa,
                    nuevasEspecificaciones
                );
                detalleActualizado.setIdDetalle(detalleToEdit.getIdDetalle()); // Mantener el ID del detalle original
                detalleActualizado.setIdProducto(nuevoProductoSeleccionado.getId()); // Actualizar el ID del producto
                detalleActualizado.setIdPedido(parentPedido.getIdPedido()); // Mantener el ID del pedido padre
                
                // Actualizar en la base de datos
                boolean exito = detallePedidoDao.actualizarDetalle(detalleActualizado);
                if (exito) {
                    JOptionPane.showMessageDialog(dialog, "Detalles del producto actualizados exitosamente.", "Actualización Exitosa", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose(); // Cerrar el diálogo
                    
                    // Recargar el detalle del pedido para reflejar los cambios en la UI
                    // Esto asegurará que los colores y la información se actualicen
                    Pedido pedidoRecargado = pedidoDao.obtenerPedidoPorId(parentPedido.getIdPedido());
                    if (pedidoRecargado != null) {
                        mostrarDetallePedido(pedidoRecargado);
                    } else {
                        // Si el pedido ya no existe, volver a la lista principal
                        cargarPedidos();
                        cardLayout.show(panelCentral, "lista");
                        currentCardName = "lista";
                        currentDisplayedPedido = null;
                    }
                } else {
                    JOptionPane.showMessageDialog(dialog, "No se pudieron actualizar los detalles del producto.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(dialog, "La cantidad debe ser un número válido.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException sqle) {
                sqle.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error de base de datos al guardar cambios: " + sqle.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Ocurrió un error inesperado: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panelBotones.add(btnGuardar);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Arial", Font.BOLD, 12));
        btnCancelar.setBackground(new Color(180, 50, 50)); 
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.putClientProperty("JButton.buttonType", "roundRect");
        btnCancelar.putClientProperty("JButton.arc", 10);
        btnCancelar.addActionListener(e -> dialog.dispose()); 
        panelBotones.add(btnCancelar);

        gbc.gridx = 0;
        gbc.gridy = 11;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        panelContenido.add(panelBotones, gbc);

        dialog.add(panelContenido, BorderLayout.CENTER);
        dialog.pack(); 
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this)); 
        dialog.setVisible(true); 
    }

    // --- Métodos de Ayuda para Colores ---
    private Color getPedidoBorderColor(String estado) {
        return switch (estado.toLowerCase()) {
            case "pendiente" -> new Color(255, 150, 150);
            case "listo" -> new Color(150, 200, 150);
            case "entregado" -> new Color(50, 180, 50); 
            default -> new Color(200, 200, 200);
        };
    }

    private Color getPedidoBackgroundColor(String estado) {
        return switch (estado.toLowerCase()) {
            case "pendiente" -> new Color(255, 245, 245); 
            case "listo" -> new Color(255, 240, 200); 
            case "entregado" -> new Color(220, 255, 220); 
            default -> new Color(245, 245, 245);
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

    private Color getProductoBackgroundColor(String estadoCocina) {
        return switch (estadoCocina.toLowerCase()) {
            case "pendiente" -> new Color(245, 245, 245); 
            case "en preparacion" -> new Color(255, 220, 180); 
            case "listo" -> new Color(255, 240, 200); 
            default -> new Color(245, 245, 245);
        };
    }

    private Color getProductoStateTextColor(String estado) { 
        return switch (estado.toLowerCase()) {
            case "pendiente" -> new Color(180, 50, 50);
            case "en preparacion" -> new Color(180, 120, 0);
            case "listo" -> new Color(0, 120, 0);
            case "servido" -> new Color(0, 80, 0); 
            default -> Color.BLACK;
        };
    }
}