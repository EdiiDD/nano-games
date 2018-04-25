package es.um.redes.nanoGames.message;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***
 * Este Mensaje solo lo envia el Servidor.
 * @author Mariano
 *
 */
public class NGMensajeListaSalas {
	private String constOperacion = "LISTA_SALAS";
	private String format = "(<mensaje><operacion>"+constOperacion+"</operacion><parametro>)"+"(.*?)"+"(</parametro><parametro>)"+"(.*?)"+"(</parametro></mensaje>)";
	private String data;
	private int numSalas;
	private String[] desSalas;
	private Pattern pattern = Pattern.compile(format);
	
	/**
	 * 
	 * @param numSalas
	 * @param descrSalas (para dividir la descripcion de las salas hay que usar los caracter "{space}&{space}")
	 * @return
	 */
	public String createNGMensajeListaSalas(int numSalas, String descrSalas) {
		return "<mensaje><operacion>"+this.constOperacion+"</operacion><parametro>"+String.valueOf(numSalas)+"</parametro>"
																	+ "<parametro>"+descrSalas+"</parametro></mensaje>";
	}
	
	// TODO Ver este mensaje, no se 
	public void processNGMensajeListaSalas(String data) {
		this.data = data;
		Matcher mat = pattern.matcher(this.data); 
		// No hace bien el find
		if (mat.find()){
			String s1 = mat.group(2);
			this.numSalas = Integer.parseInt(s1);
			String s2 = mat.group(4);
			this.desSalas = s2.split(" & ");

		} 
		
	}
	
	public int getNumSalas() {
		return this.numSalas;
	}
	
	public String getSala(int i) {
		return this.desSalas[i];
	}
	
	
	@Override
	public String toString() {
		return "NGMensajeListaSalas [numSalas=" + numSalas + ", desSalas=" + Arrays.toString(desSalas) + "]";
	}

	public static void main(String[] args) {
		NGMensajeListaSalas mls_emisor = new NGMensajeListaSalas();
		String datos_emisor = mls_emisor.createNGMensajeListaSalas(2, "Sala 0 - Adivina el numero - 2 jugadores & Sala 1 - TicTacToe - 2 Jugadores ");
		System.out.println("Enviando por DOS: "+datos_emisor);
		System.out.println("- - - Recibiendo - - - ");
		NGMensajeListaSalas mls_receptor = new NGMensajeListaSalas();
		mls_receptor.processNGMensajeListaSalas(datos_emisor);
		System.out.println("Num Salas: "+mls_receptor.getNumSalas());
		for (int i=0; i<mls_receptor.getNumSalas(); i++)
			System.out.println("\t Info: "+mls_receptor.getSala(i));
	}
	
}
