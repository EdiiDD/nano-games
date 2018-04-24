package es.um.redes.nanoGames.message;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NGMensajeRespuesta {
	private String constOperacion = "RESPUESTA";
	private String format = "(<mensaje><operacion>"+constOperacion+"</operacion><parametro>)"+"(.*?)"+"(</parametro></mensaje>)";
	private String data;
	private String respuesta;
	private Pattern pattern = Pattern.compile(format);
	
	public String createNGMensajeRespuesta(String respuesta) {
		return "<mensaje><operacion>"+this.constOperacion+"</operacion><parametro>"+respuesta+"</parametro></mensaje>";
	}
	
	public void processNGMensajeRespuesta(String data) {
		this.data = data;
		Matcher mat = pattern.matcher(this.data); 
		if (mat.find()){
			String s1 = mat.group(2);
			this.respuesta = s1;
		} 
		
	}
	
	public String getRespuesta() {
		return this.respuesta;
	}
	
	

	public static void main(String[] args) {
		//El servidor le dice al jugador despues de que pida unirse 'confirmar - true' asi que el responde con normalidad
		NGMensajeRespuesta mr_enviado = new NGMensajeRespuesta();
		String respuesta = mr_enviado.createNGMensajeRespuesta("1");
		//ahora lo envia y lo procesa el servidor, lo que conecta con el ejemplo 1 de la clase 'NGMensajePregunta'
		NGMensajeRespuesta mr_recibido = new NGMensajeRespuesta();
		System.out.println("El jugador ha dicho que el numero era: "+mr_recibido.getRespuesta());
	
		//Si recibe un mensaje NGMensajePregunta cuyo isFin = false, saca los datos por pantalla antes de pedirle al usuario que escriba otro msj
		//Si es isFin = true, entonces se pasa al siguiente estado
		
	}
}
