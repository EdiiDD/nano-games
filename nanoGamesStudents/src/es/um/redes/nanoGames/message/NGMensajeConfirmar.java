package es.um.redes.nanoGames.message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NGMensajeConfirmar {
	private String constOperacion = "CONFIRMAR";
	private String format = "(<mensaje><operacion>"+constOperacion+"</operacion><parametro>)"+"(.*?)"+"(</parametro></mensaje>)";
	private String data;
	private boolean value;
	private Pattern pattern = Pattern.compile(format);
	
	public String createNGMensajeConfirmar(boolean value) {
		return "<mensaje><operacion>"+this.constOperacion+"</operacion><parametro>"+String.valueOf(value)+"</parametro></mensaje>";
	}
	
	public void processNGMensajeConfirmar(String data) {
		this.data = data;
		Matcher mat = pattern.matcher(this.data); 
		if (mat.find()){
			String s1 = mat.group(2);
			this.value = new Boolean(s1);
		} 
		
	}
	
	public boolean isConfirmated() {
		return this.value;
	}
	
	public static void main(String[] args) {
		NGMensajeConfirmar mc_emisor = new NGMensajeConfirmar();
		String datos_emisor = mc_emisor.createNGMensajeConfirmar(true);
		System.out.println("Enviando por DOS: "+datos_emisor);
		System.out.println("- - - Recibiendo - - - ");
		NGMensajeConfirmar mc_receptor = new NGMensajeConfirmar();
		mc_receptor.processNGMensajeConfirmar(datos_emisor);
		System.out.println("�Confirmado?: "+mc_receptor.isConfirmated());
	}
	
}
