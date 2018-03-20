package es.um.redes.nanoGames.message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NGMensajeListaRanking {
	private String constOperacion = "LISTA_RANKING";
	private String format = "(<mensaje><operacion>"+constOperacion+"</operacion><parametro>)"+"(.*?)"+"(</parametro></mensaje>)";
	private String data;
	private String[] descrRanking;
	private int numRanking;
	private Pattern pattern = Pattern.compile(format);
	
	/**
	 * @param descrRanking (para dividir la descripcion de los rankings hay que usar los caracter "{space}&{space}")
	 * @return
	 */
	public String createNGMensajeListaRanking(String descrRanking) {
		return "<mensaje><operacion>"+this.constOperacion+"</operacion><parametro>"+descrRanking+"</parametro></mensaje>";
	}
	
	public void processNGMensajeListaRanking(String data) {
		this.data = data;
		Matcher mat = pattern.matcher(this.data); 
		if (mat.find()){
			String s1 = mat.group(2);
			this.descrRanking = s1.split(" & ");
			this.numRanking = this.descrRanking.length;
		} 
		
	}
	
	public String getRanking(int i) {
		return this.descrRanking[i];
	}
	
	public int getNumRanking() {
		return this.numRanking;
	}
	
	public static void main(String[] args) {
		NGMensajeListaRanking mlr_emisor = new NGMensajeListaRanking();
		String datos_emisor = mlr_emisor.createNGMensajeListaRanking("shurmanito69 - 20 puntos - 0.5 secs & mis25centimetros - 19 puntos - 0.4 secs");
		System.out.println("Enviando por DOS: "+datos_emisor);
		System.out.println("- - - Recibiendo - - - ");
		NGMensajeListaRanking mlr_receptor = new NGMensajeListaRanking();
		mlr_receptor.processNGMensajeListaRanking(datos_emisor);
		for (int i=0; i<mlr_receptor.getNumRanking(); i++)
			System.out.println("\t"+(i+1)+"º: "+mlr_receptor.getRanking(i));
	}
}
