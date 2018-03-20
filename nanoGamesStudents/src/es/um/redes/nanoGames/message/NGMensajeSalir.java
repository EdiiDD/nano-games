package es.um.redes.nanoGames.message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NGMensajeSalir {

	private String constOperacion = "SALIR";
	private String format = "(<mensaje><operacion>)("+constOperacion+")(</operacion></mensaje>)";
	private String data;
	private String operation;
	private Pattern pattern = Pattern.compile(format);
	
	public String createNGMensajeSalir() {
		return "<mensaje><operacion>"+this.constOperacion+"</operacion></mensaje>";
	}
	
	public void processNGMensajeSalir(String data) {
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
		NGMensajeSalir ms_emisor = new NGMensajeSalir();
		String datos_emisor = ms_emisor.createNGMensajeSalir();
		System.out.println("Enviando por DOS: "+datos_emisor);
		System.out.println("- - - Recibiendo - - - ");
		NGMensajeSalir ms_receptor = new NGMensajeSalir();
		ms_receptor.processNGMensajeSalir(datos_emisor);
		System.out.println("Operacion Recibida: "+ms_receptor.getOperacion());
	}

}
