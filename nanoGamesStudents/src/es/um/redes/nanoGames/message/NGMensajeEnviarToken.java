package es.um.redes.nanoGames.message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NGMensajeEnviarToken {
	private String constOperacion = "ENVIAR_TOKEN";
	private String format = "(<mensaje><operacion>"+constOperacion+"</operacion><parametro>)"+"(.*?)"+"(</parametro></mensaje>)";
	private String data;
	private long token;
	private Pattern pattern = Pattern.compile(format);
	
	public String createNGMensajeEnviarToken(long token) {
		return "<mensaje><operacion>"+this.constOperacion+"</operacion><parametro>"+String.valueOf(token)+"</parametro></mensaje>";
	}
	
	public void processNGMensajeEnviarToken(String data) {
		this.data = data;
		Matcher mat = pattern.matcher(this.data); 
		if (mat.find()){
			String s1 = mat.group(2);
			this.token = new Long(s1);
		} 
		
	}
	
	public long getToken() {
		return this.token;
	}
	
	public static void main(String[] args) {
		NGMensajeEnviarToken met_emisor = new NGMensajeEnviarToken();
		String datos_emisor = met_emisor.createNGMensajeEnviarToken(1213123123);
		System.out.println("Enviando por DOS: "+datos_emisor);
		System.out.println("- - - Recibiendo - - - ");
		NGMensajeEnviarToken met_receptor = new NGMensajeEnviarToken();
		met_receptor.processNGMensajeEnviarToken(datos_emisor);
		System.out.println("Token Recibido: "+met_receptor.getToken());
	}
}
