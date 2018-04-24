package xml;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class XMLManager{
	private static final String USER_DATABASE = "user_database_xml.xml";
	
	
	public static void SerializeToXML(ListaUsuarios lu, String PATH) {
		XMLEncoder encoder=null;
		try{
		encoder=new XMLEncoder(new BufferedOutputStream(new FileOutputStream(PATH)));
		}catch(FileNotFoundException fileNotFound){
			System.out.println("ERROR: While Creating or Opening the xml");
		}
		System.out.println("You might need to refresh the project on Eclipse!");
		encoder.writeObject(lu);
		encoder.close();
	}
	
	public static ListaUsuarios DeserializeListaUsuariosFromXML(String PATH) {
		XMLDecoder d = null;
		ListaUsuarios result = null;
		try {
			d = new XMLDecoder(
			         new BufferedInputStream(
			             new FileInputStream(PATH)));
			result = (ListaUsuarios) d.readObject();
			d.close();
		} catch (FileNotFoundException e) {
			System.out.println("ERROR: While Opening the XML File");
		}
		 
		 return result;
	}

	
	public static void main(String[] args) {
		Usuario u = new Usuario();
		u.setContrasena("u2");
		u.setNombreUsuario("u1");
		Usuario v = new Usuario();
		v.setContrasena("v2");
		v.setNombreUsuario("v1");
		ListaUsuarios lu = new ListaUsuarios(); 
		ArrayList<Usuario> alu = new ArrayList<Usuario>();
		alu.add(u);
		alu.add(v);
		lu.setDatabase(alu);
		SerializeToXML(lu, USER_DATABASE);
		////
		ListaUsuarios lu_deserialized = DeserializeListaUsuariosFromXML(USER_DATABASE);
		for (int i=0; i<lu_deserialized.getDatabase().size(); i++) {
			System.out.println(lu_deserialized.getDatabase().get(i).getNombreUsuario());
			System.out.println("\t"+lu_deserialized.getDatabase().get(i).getContrasena());
		}
	}
}
