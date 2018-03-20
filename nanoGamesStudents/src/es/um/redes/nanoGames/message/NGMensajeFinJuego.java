package es.um.redes.nanoGames.message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***
 * Mensaje enviado solo por el servidor.
 * @author Mariano
 *
 */
public class NGMensajeFinJuego {
	private String constOperacion = "FIN_JUEGO";
	private String format = "(<mensaje><operacion>)("+constOperacion+")(</operacion></mensaje>)";
	private String data;
	private String operation;
	private Pattern pattern = Pattern.compile(format);
	
	public String createNGMensajeFinJuego() {
		return "<mensaje><operacion>"+this.constOperacion+"</operacion></mensaje>";
	}
	
	public void processNGMensajeFinJuego(String data) {
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
		NGMensajeFinJuego mfj_emisor = new NGMensajeFinJuego();
		String datos_emisor = mfj_emisor.createNGMensajeFinJuego();
		System.out.println("Enviando por DOS: "+datos_emisor);
		System.out.println("- - - Recibiendo - - - ");
		NGMensajeFinJuego mfj_receptor = new NGMensajeFinJuego();
		mfj_receptor.processNGMensajeFinJuego(datos_emisor);
		System.out.println("Operacion Recibida: "+mfj_receptor.getOperacion());
	}
}
