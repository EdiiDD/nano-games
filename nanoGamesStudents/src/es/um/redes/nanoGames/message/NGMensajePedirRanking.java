package es.um.redes.nanoGames.message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NGMensajePedirRanking {
	private String constOperacion = "PEDIR_RANKING";
	private String format = "(<mensaje><operacion>)("+constOperacion+")(</operacion></mensaje>)";
	private String data;
	private String operation;
	private Pattern pattern = Pattern.compile(format);
	
	public String createNGMensajePedirRanking() {
		return "<mensaje><operacion>"+this.constOperacion+"</operacion></mensaje>";
	}
	
	public void processNGMensajePedirRanking(String data) {
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
		NGMensajePedirRanking mpr_emisor = new NGMensajePedirRanking();
		String datos_emisor = mpr_emisor.createNGMensajePedirRanking();
		System.out.println("Enviando por DOS: "+datos_emisor);
		System.out.println("- - - Recibiendo - - - ");
		NGMensajePedirRanking mpr_receptor = new NGMensajePedirRanking();
		mpr_receptor.processNGMensajePedirRanking(datos_emisor);
		System.out.println("Operacion Recibida: "+mpr_receptor.getOperacion());
	}
}
