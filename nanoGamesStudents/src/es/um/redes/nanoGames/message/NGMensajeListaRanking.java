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

}
