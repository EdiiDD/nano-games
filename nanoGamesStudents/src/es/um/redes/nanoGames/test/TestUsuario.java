package es.um.redes.nanoGames.test;

import java.util.List;
import java.util.Scanner;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import es.um.redes.nanoGames.server.NGPlayerInfo;

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

		manager.getTransaction().begin();

		manager.persist(player);

		manager.getTransaction().commit();
	}

	public static void main(String[] args) {

		s = new Scanner(System.in);
		
		String nick;
		int score;
		emf = Persistence.createEntityManagerFactory("persistence");
		manager = emf.createEntityManager();

		while (true) {
			System.out.println("AÑADIR");

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
