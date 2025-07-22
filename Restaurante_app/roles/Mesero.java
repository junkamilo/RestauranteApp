package Restaurante_app.roles;

import javax.swing.*;
import Restaurante_app.InicioRoles; 
import java.awt.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class Mesero extends JPanel {

    private static final long serialVersionUID = 1L;

    private InicioRoles mainApp; 
    private String panelRegreso;
    
    public Mesero(InicioRoles mainApp, String panelRegreso) {
        this.mainApp = mainApp; 
        this.panelRegreso = panelRegreso;
        
        // Configuraciones básicas del panel Mesero
        this.setName("Mesero");
        this.setLayout(new BorderLayout()); 
        setBackground(new Color(255, 248, 240)); 

        // Panel para el título
        JPanel panelTitulo = new JPanel();
        panelTitulo.setBackground(new Color(255, 248, 240));
        JLabel titulo_mesero = new JLabel("Panel del Mesero");
        titulo_mesero.setHorizontalAlignment(SwingConstants.CENTER);
        titulo_mesero.setFont(new Font("Arial", Font.BOLD, 22));
        titulo_mesero.setForeground(new Color(60, 44, 30));
        panelTitulo.add(titulo_mesero);
        add(panelTitulo, BorderLayout.NORTH);

        // Panel central para los botones de navegación
        JPanel panelBotones = new JPanel();
        panelBotones.setLayout(new GridLayout(4, 1, 10, 20)); 
        panelBotones.setBorder(BorderFactory.createEmptyBorder(50, 60, 50, 60));
        panelBotones.setOpaque(false); // Para que el color de fondo se vea

        // Botón "Tomar Pedido"
        JButton btnTomarPedido = new JButton("Tomar Pedido");
        btnTomarPedido.setFont(new Font("Arial", Font.BOLD, 16));
        btnTomarPedido.setBackground(new Color(219, 168, 86));
        btnTomarPedido.setForeground(Color.WHITE);
        btnTomarPedido.setFocusPainted(false);
        btnTomarPedido.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnTomarPedido.putClientProperty("JButton.buttonType", "roundRect");
        btnTomarPedido.putClientProperty("JButton.arc", 15);
        btnTomarPedido.addActionListener(e -> {
            mainApp.mostrarPanel("Tomar_pedido");
        });

        // Botón "Ver Pedidos"
        JButton btnDetalles = new JButton("Ver Pedidos Activos");
        btnDetalles.setFont(new Font("Arial", Font.BOLD, 16));
        btnDetalles.setBackground(new Color(219, 168, 86));
        btnDetalles.setForeground(Color.WHITE);
        btnDetalles.setFocusPainted(false);
        btnDetalles.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnDetalles.putClientProperty("JButton.buttonType", "roundRect");
        btnDetalles.putClientProperty("JButton.arc", 15);
        btnDetalles.addActionListener(e -> {
            mainApp.mostrarPanel("Detalles_pedido");
        });

        panelBotones.add(btnTomarPedido);
        panelBotones.add(btnDetalles);
        add(panelBotones, BorderLayout.CENTER);

        // --- Nuevo Botón de Cerrar Sesión ---
        JPanel panelCerrarSesion = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelCerrarSesion.setBackground(new Color(255, 248, 240)); 
        panelCerrarSesion.setBorder(new EmptyBorder(10, 0, 20, 0)); // Padding
        
        JButton btnCerrarSesion = new JButton("CERRAR SESIÓN");
        btnCerrarSesion.setFont(new Font("Arial", Font.BOLD, 14));
        btnCerrarSesion.setBackground(new Color(180, 50, 50)); 
        btnCerrarSesion.setForeground(Color.WHITE);
        btnCerrarSesion.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btnCerrarSesion.putClientProperty("JButton.buttonType", "roundRect");
        btnCerrarSesion.putClientProperty("JButton.arc", 10);
        btnCerrarSesion.addActionListener(e -> {
            mainApp.volverAlPanelDeRol(); // Llama al método para regresar a InicioRoles
        });
        panelCerrarSesion.add(btnCerrarSesion);
        add(panelCerrarSesion, BorderLayout.SOUTH);
    }
    
    public Mesero(InicioRoles mainApp) { 
        this(mainApp, "Login"); 
    }
}