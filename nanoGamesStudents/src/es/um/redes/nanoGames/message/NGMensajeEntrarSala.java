package es.um.redes.nanoGames.message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NGMensajeEntrarSala {
	private String constOperacion = "ENTRAR_SALA";
	private String format = "(<mensaje><operacion>"+constOperacion+"</operacion><parametro>)"+"(.*?)"+"(</parametro></mensaje>)";
	private String data;
	private int numSala;
	private Pattern pattern = Pattern.compile(format);
	
	public String createNGMensajeEntrarSala(int numSalas) {
		return "<mensaje><operacion>"+this.constOperacion+"</operacion><parametro>"+String.valueOf(numSalas)+"</parametro></mensaje>";
	}
	
	public void processNGMensajeEntrarSala(String data) {
		this.data = data;
		Matcher mat = pattern.matcher(this.data); 
		if (mat.find()){
			String s1 = mat.group(2);
			this.numSala = Integer.parseInt(s1);
		} 
		
	}
	
	public int getNumSala() {
		return this.numSala;
	}
	
	public static void main(String[] args) {
		NGMensajeEntrarSala mes_emisor = new NGMensajeEntrarSala();
		String datos_emisor = mes_emisor.createNGMensajeEntrarSala(0);
		System.out.println("Enviando por DOS: "+datos_emisor);
		System.out.println("- - - Recibiendo - - - ");
		NGMensajeEntrarSala mls_receptor = new NGMensajeEntrarSala();
		mls_receptor.processNGMensajeEntrarSala(datos_emisor);
		System.out.println("Num Sala solicitado: "+mls_receptor.getNumSala());
	}
}
