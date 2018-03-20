package es.um.redes.nanoGames.message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NGMensajeEnviarNickname {
	private String constOperacion = "ENVIAR_NICKNAME";
	private String format = "(<mensaje><operacion>"+constOperacion+"</operacion><parametro>)"+"(.*?)"+"(</parametro></mensaje>)";
	private String data;
	private String nickname;
	private Pattern pattern = Pattern.compile(format);
	
	public String createNGMensajeEnviarNickname(String nickname) {
		return "<mensaje><operacion>"+this.constOperacion+"</operacion><parametro>"+nickname+"</parametro></mensaje>";
	}
	
	public void processNGMensajeEnviarNickname(String data) {
		this.data = data;
		Matcher mat = pattern.matcher(this.data); 
		if (mat.find()){
			String s1 = mat.group(2);
			this.nickname = new String(s1);
		} 
		
	}
	
	public String getNickname() {
		return this.nickname;
	}
	
	public static void main(String[] args) {
		NGMensajeEnviarNickname men_emisor = new NGMensajeEnviarNickname();
		String datos_emisor = men_emisor.createNGMensajeEnviarNickname("surmanito69");
		System.out.println("Enviando por DOS: "+datos_emisor);
		System.out.println("- - - Recibiendo - - - ");
		NGMensajeEnviarNickname men_receptor = new NGMensajeEnviarNickname();
		men_receptor.processNGMensajeEnviarNickname(datos_emisor);
		System.out.println("Nickname Recibido: "+men_receptor.getNickname());
	}
}
