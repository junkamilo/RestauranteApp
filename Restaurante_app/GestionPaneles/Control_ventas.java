package Restaurante_app.GestionPaneles;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*; 
import java.util.Comparator; 
import java.util.HashMap;    
import java.util.LinkedHashMap; 
import java.util.Map;      
import java.util.Set;
import java.util.List;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList; 
import java.util.HashSet; 
import java.util.stream.Collectors; // Para Collectors en el resumen de sopas

import Restaurante_app.InicioRoles;
import Restaurante_app.DAO.VentaDao;
import Restaurante_app.Model.PlatoVendido;

public class Control_ventas extends JPanel {

    private static final long serialVersionUID = 1L;

    private JLabel lblMontoTotal;
    private JLabel lblPlatosVendidos;
    private JLabel lblPlatoMasVendido; 
    private JPanel panelDetalleVentas;
    private VentaDao ventaDAO; 

    private InicioRoles mainApp; // Referencia a InicioRoles
    private String panelRegreso; // Variable para almacenar el panel al que debe regresar

    private Timer refreshTimer; 

    public Control_ventas(InicioRoles mainApp) { 
        this.mainApp = mainApp; // Asigna la referencia a InicioRoles
        this.ventaDAO = new VentaDao(); 
        
        setBackground(new Color(245, 245, 245)); 
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(360, 640)); 

        JPanel mainContentPanel = new JPanel(new GridBagLayout());
        mainContentPanel.setBackground(new Color(245, 245, 245));
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); 

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH; 
        gbc.insets = new Insets(8, 0, 8, 0); 


        JPanel panelResumenGeneral = new JPanel(new GridLayout(2, 1, 0, 5));
        panelResumenGeneral.setBackground(new Color(255, 255, 255)); 
        panelResumenGeneral.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1), 
            BorderFactory.createEmptyBorder(15, 15, 15, 15) 
        ));
        panelResumenGeneral.putClientProperty("FlatLaf.style", "arc: 15");

        lblMontoTotal = new JLabel("Monto total del día: $0.00");
        lblMontoTotal.setFont(new Font("Arial", Font.BOLD, 18));
        lblMontoTotal.setForeground(new Color(0, 100, 0)); 

        lblPlatosVendidos = new JLabel("Total de platos vendidos: 0");
        lblPlatosVendidos.setFont(new Font("Arial", Font.PLAIN, 15));
        lblPlatosVendidos.setForeground(new Color(80, 80, 80));

        panelResumenGeneral.add(lblMontoTotal);
        panelResumenGeneral.add(lblPlatosVendidos);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0; 
        gbc.weighty = 0.15; 
        mainContentPanel.add(panelResumenGeneral, gbc);


        JPanel panelMasVendido = new JPanel(new BorderLayout(10, 0));
        panelMasVendido.setBackground(new Color(255, 240, 220)); 
        panelMasVendido.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 180, 100), 1), 
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panelMasVendido.putClientProperty("FlatLaf.style", "arc: 15");

        JLabel iconoTrofeo = new JLabel("🏆"); 
        iconoTrofeo.setFont(new Font("Arial", Font.PLAIN, 28));
        panelMasVendido.add(iconoTrofeo, BorderLayout.WEST);

        JPanel contentMasVendido = new JPanel(new GridLayout(2, 1, 0, 3));
        contentMasVendido.setOpaque(false); 
        JLabel lblTituloMasVendido = new JLabel("¡El Plato Más Vendido Hoy!");
        lblTituloMasVendido.setFont(new Font("Arial", Font.BOLD, 16));
        lblTituloMasVendido.setForeground(new Color(150, 80, 0)); 

        lblPlatoMasVendido = new JLabel("N/A"); 
        lblPlatoMasVendido.setFont(new Font("Arial", Font.ITALIC, 14));
        lblPlatoMasVendido.setForeground(new Color(100, 50, 0));

        contentMasVendido.add(lblTituloMasVendido);
        contentMasVendido.add(lblPlatoMasVendido);
        panelMasVendido.add(contentMasVendido, BorderLayout.CENTER);

        gbc.gridy = 1;
        gbc.weighty = 0.15;
        mainContentPanel.add(panelMasVendido, gbc);


        panelDetalleVentas = new JPanel();
        panelDetalleVentas.setLayout(new BoxLayout(panelDetalleVentas, BoxLayout.Y_AXIS));
        panelDetalleVentas.setBackground(new Color(255, 255, 255)); 

        JScrollPane scrollPane = new JScrollPane(panelDetalleVentas);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
            "<html><b><font color='#3C2C1E' size='4'>Listado de Platos Vendidos (Hoy)</font></b></html>",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12), new Color(60, 44, 30)
        )); 
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        gbc.gridy = 2;
        gbc.weighty = 0.70; 
        mainContentPanel.add(scrollPane, gbc);

        add(mainContentPanel, BorderLayout.CENTER); 

        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBoton.setBackground(new Color(245, 245, 245));
        panelBoton.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); 
        JButton btnVolver = new JButton("← Volver a Menú");
        btnVolver.setForeground(Color.WHITE);
        btnVolver.setBackground(new Color(180, 50, 50)); 
        btnVolver.setFocusPainted(false);
        btnVolver.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20)); 
        btnVolver.putClientProperty("JButton.buttonType", "roundRect");
        btnVolver.putClientProperty("JButton.arc", 10);
        btnVolver.addActionListener(e -> {
            // Usa la variable panelRegreso para volver al panel correcto
            if (panelRegreso != null && !panelRegreso.isEmpty()) {
                mainApp.mostrarPanel(panelRegreso); 
            } else {
                mainApp.mostrarPanel("Login"); // Fallback si no hay panelRegreso seteado
            }
        });
        panelBoton.add(btnVolver);
        add(panelBoton, BorderLayout.SOUTH);

        refreshTimer = new Timer(3000, e -> { 
            if (this.isVisible()) { 
                actualizarDatosVentas();
            }
        });
        refreshTimer.start();

        this.addComponentListener(new ComponentListener() { 
            @Override
            public void componentShown(ComponentEvent e) {
                actualizarDatosVentas(); 
                refreshTimer.start();
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                refreshTimer.stop();
            }

            @Override
            public void componentResized(ComponentEvent e) {}
            @Override
            public void componentMoved(ComponentEvent e) {}
        });
    }

    /**
     * Establece el nombre del panel al que se debe regresar al pulsar "Volver".
     * @param panelRegreso El nombre de la clave del panel en InicioRoles.
     */
    public void setPanelRegreso(String panelRegreso) {
        this.panelRegreso = panelRegreso;
    }

    public void actualizarDatosVentas() {
        limpiarDetalleVentas(); 

        try {
            List<PlatoVendido> ventas = ventaDAO.obtenerPlatosVendidosHoy();

            BigDecimal montoTotalDia = BigDecimal.ZERO;
            int totalPlatosVendidos = 0;

            if (ventas.isEmpty()) {
                lblPlatoMasVendido.setText("No hay ventas hoy.");
                JLabel noPlatosLabel = new JLabel("No hay platos vendidos en el día.");
                noPlatosLabel.setFont(new Font("Arial", Font.ITALIC, 13));
                noPlatosLabel.setForeground(new Color(120, 120, 120));
                noPlatosLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                panelDetalleVentas.add(Box.createVerticalGlue());
                panelDetalleVentas.add(noPlatosLabel);
                panelDetalleVentas.add(Box.createVerticalGlue());
            } else {
                for (PlatoVendido plato : ventas) {
                    montoTotalDia = montoTotalDia.add(plato.getTotal());
                    totalPlatosVendidos += plato.getCantidad();
                }

                PlatoVendido platoMasVendidoHoy = ventaDAO.obtenerPlatoMasVendidoHoy();
                if (platoMasVendidoHoy != null) {
                    lblPlatoMasVendido.setText("<html>" + platoMasVendidoHoy.getNombre() + " (Total vendidos: " + platoMasVendidoHoy.getCantidad() + ")</html>");
                } else {
                    lblPlatoMasVendido.setText("No hay plato más vendido hoy.");
                }

                Map<String, List<PlatoVendido>> platosPorCategoria = new LinkedHashMap<>();
                for (PlatoVendido plato : ventas) {
                    platosPorCategoria.computeIfAbsent(plato.getCategoria().toLowerCase(), k -> new ArrayList<>()).add(plato);
                }

                List<String> ordenCategorias = new ArrayList<>();
                ordenCategorias.add("especial");
                ordenCategorias.add("plato fuerte");
                ordenCategorias.add("sopa");       
                ordenCategorias.add("bebida");     
                ordenCategorias.add("postre");     

                Set<String> categoriasYaAgregadas = new HashSet<>();

                for (String categoriaOrdenada : ordenCategorias) {
                    if (platosPorCategoria.containsKey(categoriaOrdenada)) {
                        JLabel lblCategoria = new JLabel("<html><b style='color:#3C2C1E;'>" + capitalizeFirstLetter(categoriaOrdenada) + "</b></html>");
                        lblCategoria.setFont(new Font("Arial", Font.BOLD, 14));
                        lblCategoria.setBorder(new EmptyBorder(10, 10, 5, 10)); 
                        lblCategoria.setAlignmentX(Component.LEFT_ALIGNMENT);
                        panelDetalleVentas.add(lblCategoria);
                        
                        List<PlatoVendido> platosEnEstaCategoria = platosPorCategoria.get(categoriaOrdenada);
                        platosEnEstaCategoria.sort(Comparator.comparing(PlatoVendido::getNombre));

                        for (PlatoVendido plato : platosEnEstaCategoria) {
                            agregarPlatoEnListado(plato.getNombre(), plato.getCantidad(), plato.getTotal());
                        }
                        categoriasYaAgregadas.add(categoriaOrdenada); 
                    }
                }

                for (Map.Entry<String, List<PlatoVendido>> entry : platosPorCategoria.entrySet()) {
                    if (!categoriasYaAgregadas.contains(entry.getKey())) { 
                        JLabel lblCategoria = new JLabel("<html><b style='color:#3C2C1E;'>" + capitalizeFirstLetter(entry.getKey()) + "</b></html>");
                        lblCategoria.setFont(new Font("Arial", Font.BOLD, 14));
                        lblCategoria.setBorder(new EmptyBorder(10, 10, 5, 10)); 
                        lblCategoria.setAlignmentX(Component.LEFT_ALIGNMENT);
                        panelDetalleVentas.add(lblCategoria);
                        
                        List<PlatoVendido> platosEnEstaCategoria = entry.getValue();
                        platosEnEstaCategoria.sort(Comparator.comparing(PlatoVendido::getNombre));

                        for (PlatoVendido plato : platosEnEstaCategoria) {
                            agregarPlatoEnListado(plato.getNombre(), plato.getCantidad(), plato.getTotal());
                        }
                    }
                }
            }

            actualizarResumen(montoTotalDia, totalPlatosVendidos);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar datos de ventas: " + e.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
            lblMontoTotal.setText("Monto total del día: Error");
            lblPlatosVendidos.setText("Total de platos vendidos: Error");
            lblPlatoMasVendido.setText("Error al cargar plato más vendido.");
        }
        revalidate();
        repaint();
    }


    public void actualizarResumen(BigDecimal montoTotal, int totalPlatos) {
        lblMontoTotal.setText("Monto total del día: $" + String.format("%,.2f", montoTotal)); 
        lblPlatosVendidos.setText("Total de platos vendidos: " + totalPlatos);
    }

    public void agregarPlatoEnListado(String nombrePlato, int cantidad, BigDecimal total) {
        JPanel card = new JPanel(new BorderLayout(10, 0)); 
        card.setBackground(new Color(250, 250, 250)); 
        card.setBorder(new CompoundBorder(
                new EmptyBorder(5, 10, 5, 10), 
                new LineBorder(new Color(220, 220, 220), 1) 
        ));
        card.putClientProperty("FlatLaf.style", "arc: 10"); 
        card.setAlignmentX(Component.CENTER_ALIGNMENT); 

        JLabel lblNombreCantidad = new JLabel(nombrePlato + " (" + cantidad + ")");
        lblNombreCantidad.setFont(new Font("Arial", Font.PLAIN, 14));
        lblNombreCantidad.setForeground(new Color(60, 44, 30));

        JLabel lblPrecio = new JLabel("$" + String.format("%,.2f", total)); 
        lblPrecio.setFont(new Font("Arial", Font.BOLD, 14));
        lblPrecio.setForeground(new Color(0, 120, 0)); 

        card.add(lblNombreCantidad, BorderLayout.WEST);
        card.add(lblPrecio, BorderLayout.EAST);

        panelDetalleVentas.add(card);
        panelDetalleVentas.add(Box.createRigidArea(new Dimension(0, 5))); 
    }

    public void limpiarDetalleVentas() {
        panelDetalleVentas.removeAll();
        panelDetalleVentas.revalidate();
        panelDetalleVentas.repaint();
    }

    // Método auxiliar para capitalizar la primera letra
    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}