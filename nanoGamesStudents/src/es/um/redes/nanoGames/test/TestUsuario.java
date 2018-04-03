package es.um.redes.nanoGames.test;

import java.util.List;
import java.util.Scanner;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.hibernate.Session;
import org.hibernate.Transaction;

import es.um.redes.nanoGames.server.NGPlayerInfo;
import es.um.redes.nanoGames.utils.HibernateUtil;


public class TestUsuario {

	// Interfaz que contiene los metodos para trabajar con los elementos.
	private static EntityManager manager;

	private static EntityManagerFactory emf;

	private static Scanner s;

	@SuppressWarnings("unchecked")
	private static void imprimirTodo() {
		List<NGPlayerInfo> player = (List<NGPlayerInfo>) manager.createQuery("FROM NGPlayerInfo").getResultList();
		System.out.println("Hay " + player.size() + " usuarios en el sistema");
		for (NGPlayerInfo player1 : player) {
			System.out.println(player1.toString());
		}

	}

	public static void añadirProfesor(NGPlayerInfo player) {
		
		Session session= HibernateUtil.getSessionFactory().openSession();
		Transaction trans = null;
		
		try {
			trans = session.beginTransaction();
			session.save(player);
			session.getTransaction().commit();
		} catch (Exception e) {
			if(trans != null) {
				trans.rollback();
				System.err.println("Nick de usuario no valido");
			}
		} finally {
			session.close();
		}
	}

	public static void main(String[] args) {

		s = new Scanner(System.in);
		
		String nick;
		int score,id;
		emf = Persistence.createEntityManagerFactory("persistence");
		manager = emf.createEntityManager();

		while (true) {
			System.out.println("AÑADIR:");
			
			System.out.println("Nick: ");
			nick = s.next();

			System.out.println("Score: ");
			score = s.nextInt();
			
			NGPlayerInfo p = new NGPlayerInfo(nick, score);
			añadirProfesor(p);
			imprimirTodo();
			

		}

	}

}
