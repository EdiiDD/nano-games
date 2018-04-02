package xml;

import java.util.ArrayList;

public class ListaUsuarios {
	private ArrayList<Usuario> database = new ArrayList<Usuario>();
	
	public ListaUsuarios() {}
	
	public ArrayList<Usuario> getDatabase(){ return this.database;}
	public void setDatabase(ArrayList<Usuario> al) { this.database = al;}
}
