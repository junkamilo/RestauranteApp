package Restaurante_app.roles;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import Restaurante_app.InicioRoles; 
import Restaurante_app.GestionPaneles.Nuevo_usuario; 

public class Administrador extends JPanel {
    private static final long serialVersionUID = 1L;

    // Referencia a la ventana principal (InicioRoles)
    private InicioRoles mainApp;
    private String panelRegreso;

    public Administrador(InicioRoles mainApp, String panelRegreso) {
        this.mainApp = mainApp; 
        this.panelRegreso = panelRegreso;

        // Configuración de diseño del panel Administrador
        setBackground(new Color(255, 248, 240));
        setSize(360, 640);
        setLayout(null); 

        // Configuración de UIManager para estilos de botones
        try {
            UIManager.put("Button.background", new Color(219, 168, 86));
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Button.font", new Font("Arial", Font.BOLD, 12));
            UIManager.put("Button.arc", 15);
            UIManager.put("Button.hoverBackground", new Color(235, 180, 95));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Título de bienvenida
        JLabel titulo_bienvenida = new JLabel("ADMINISTRADOR");
        titulo_bienvenida.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 18));
        titulo_bienvenida.setForeground(new Color(60, 44, 30));
        titulo_bienvenida.setHorizontalAlignment(SwingConstants.CENTER);
        titulo_bienvenida.setBounds(80, 20, 200, 40);
        add(titulo_bienvenida);

        // Panel que contiene los botones de navegación
        JPanel panelBotones = new JPanel();
        panelBotones.setBounds(30, 80, 300, 450); // Reducir altura para dejar espacio al botón de Cerrar Sesión
        panelBotones.setLayout(new GridLayout(6, 1, 0, 15)); 
        panelBotones.setOpaque(false); 
        add(panelBotones);

        // Botones de navegación principal (usan el método crearBoton)
        JButton btn_gestionMenu = crearBoton("GESTIÓN DEL MENÚ", "Gestion_menu");
        panelBotones.add(btn_gestionMenu);
        
        JButton btn_tomarPedido = crearBoton("TOMAR PEDIDO", "Tomar_pedido");
        panelBotones.add(btn_tomarPedido);
        
        JButton btn_detallePedido = crearBoton("DETALLES DEL PEDIDO", "Detalles_pedido");
        panelBotones.add(btn_detallePedido);
        
        JButton btn_controlVentas = crearBoton("CONTROL DE VENTAS", "Control_ventas");
        panelBotones.add(btn_controlVentas);
        
        JButton btn_inventario = crearBoton("INVENTARIO", "Inventario");
        panelBotones.add(btn_inventario);
        
        JButton btn_agregarUsuario = new JButton("NUEVO USUARIO");
        btn_agregarUsuario.setFocusPainted(false);
        btn_agregarUsuario.setPreferredSize(new Dimension(140, 50)); 
        btn_agregarUsuario.addActionListener(e -> {
            mainApp.mostrarPanel("Nuevo_usuario"); 
        });
        panelBotones.add(btn_agregarUsuario);

        // --- Nuevo Botón de Cerrar Sesión ---
        JButton btnCerrarSesion = new JButton("CERRAR SESIÓN");
        btnCerrarSesion.setFont(new Font("Arial", Font.BOLD, 14));
        btnCerrarSesion.setBackground(new Color(180, 50, 50)); // Color rojo para cerrar sesión
        btnCerrarSesion.setForeground(Color.WHITE);
        btnCerrarSesion.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btnCerrarSesion.putClientProperty("JButton.buttonType", "roundRect");
        btnCerrarSesion.putClientProperty("JButton.arc", 10);
        btnCerrarSesion.setBounds(100, 550, 160, 40); // Posición y tamaño ajustados
        btnCerrarSesion.addActionListener(e -> {
            mainApp.volverAlPanelDeRol(); // Llama al método para regresar a InicioRoles
        });
        add(btnCerrarSesion);

        // Decoración inferior (ajustada para el nuevo botón)
        JLabel decoracion = new JLabel("🍽️ LA ESQUINA 🍽️", SwingConstants.CENTER);
        decoracion.setFont(new Font("Arial", Font.ITALIC, 14));
        decoracion.setForeground(new Color(150, 100, 50));
        decoracion.setBounds(0, 600, 360, 20); 
        add(decoracion);
    }
    
    public Administrador(InicioRoles mainApp) { 
        this(mainApp, "Login"); 
    }

    private JButton crearBoton(String texto, String panelDestino) {
        JButton boton = new JButton(texto);
        boton.setFocusPainted(false);
        boton.setPreferredSize(new Dimension(140, 50)); 
        
        boton.addActionListener(e -> {
            mainApp.mostrarPanel(panelDestino);
        });
        
        return boton;
    }
}