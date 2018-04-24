package xml;


public class Usuario {
	private String nombreUsuario;
	private String password;
	
	public Usuario() {
	}
	
	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}

	public void setContrasena(String password) {
		this.password = password;
	}
	
	public String getNombreUsuario() {
		return this.nombreUsuario;
	}
	
	public String getContrasena() {
		return this.password;
	}	
}
