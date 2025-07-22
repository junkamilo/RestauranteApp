package Restaurante_app.Model;

public class Mesa {
	 	private int id_mesa;
	    private String nombre_mesa; // Ej: "Mesa 1", "Mesa 2", etc.
	    private String estado; // Ej: "Libre", "Ocupada", "Reservada"

	    public Mesa() {
	    }

	    public Mesa(int id_mesa, String nombre_mesa, String estado) {
	        this.id_mesa = id_mesa;
	        this.nombre_mesa = nombre_mesa;
	        this.estado = estado;
	    }

	    // Getters y Setters
	    public int getId() {
	        return id_mesa;
	    }

	    public void setId(int id) {
	        this.id_mesa = id_mesa;
	    }

	    public String getNombre() {
	        return nombre_mesa;
	    }

	    public void setNombre(String nombre_mesa) {
	        this.nombre_mesa = nombre_mesa;
	    }

	    public String getEstado() {
	        return estado;
	    }

	    public void setEstado(String estado) {
	        this.estado = estado;
	    }

	    @Override
	    public String toString() {
	        return nombre_mesa; // Para mostrar en el JComboBox
	    }
}
