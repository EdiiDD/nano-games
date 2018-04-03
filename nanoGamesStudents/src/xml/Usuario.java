package xml;


public class Usuario {
	private String nombreUsuario;
	private String password;
	
	public Usuario() {
	}
	
	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}
	
	public void setContraseña(String password) {
		this.password = password;
	}
	
	public String getNombreUsuario() {
		return this.nombreUsuario;
	}
	
	public String getContraseña() {
		return this.password;
	}	
}
