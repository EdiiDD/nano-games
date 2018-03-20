package es.um.redes.nanoGames.message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NGMensajeListarSalas {
	private String constOperacion = "LISTAR_SALAS";
	private String format = "(<mensaje><operacion>)("+constOperacion+")(</operacion></mensaje>)";
	private String data;
	private String operation;
	private Pattern pattern = Pattern.compile(format);
	
	public String createNGMensajeListarSalas() {
		return "<mensaje><operacion>"+this.constOperacion+"</operacion></mensaje>";
	}
	
	public void processNGMensajeListarSalas(String data) {
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
		NGMensajeListarSalas mls_emisor = new NGMensajeListarSalas();
		String datos_emisor = mls_emisor.createNGMensajeListarSalas();
		System.out.println("Enviando por DOS: "+datos_emisor);
		System.out.println("- - - Recibiendo - - - ");
		NGMensajeListarSalas mls_receptor = new NGMensajeListarSalas();
		mls_receptor.processNGMensajeListarSalas(datos_emisor);
		System.out.println("Operacion Recibido: "+mls_receptor.getOperacion());
	}
}
