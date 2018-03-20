package es.um.redes.nanoGames.message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NGMensajeSalirSala {
	private String constOperacion = "SALIR_SALA";
	private String format = "(<mensaje><operacion>)("+constOperacion+")(</operacion></mensaje>)";
	private String data;
	private String operation;
	private Pattern pattern = Pattern.compile(format);
	
	public String createNGMensajeSalirSala() {
		return "<mensaje><operacion>"+this.constOperacion+"</operacion></mensaje>";
	}
	
	public void processNGMensajeSalirSala(String data) {
		this.data = data;
		Matcher mat = pattern.matcher(this.data); 
		if (mat.find()){
			String s1 = mat.group(2);
			this.operation = new String(s1);
		} 
		
	}
	
	public String getOperacion() {
		return this.operation;
	}
	
	public static void main(String[] args) {
		NGMensajeSalirSala mss_emisor = new NGMensajeSalirSala();
		String datos_emisor = mss_emisor.createNGMensajeSalirSala();
		System.out.println("Enviando por DOS: "+datos_emisor);
		System.out.println("- - - Recibiendo - - - ");
		NGMensajeSalirSala mss_receptor = new NGMensajeSalirSala();
		mss_receptor.processNGMensajeSalirSala(datos_emisor);
		System.out.println("Operacion Recibida: "+mss_receptor.getOperacion());
	}
}
