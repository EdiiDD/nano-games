package es.um.redes.nanoGames.server.roomManager;


import java.util.HashMap;
import java.util.Map;

import es.um.redes.nanoGames.server.NGPlayerInfo;

public class NGRoomAdivinarNumero extends NGRoomManager {

	private static final int NUM_MAX_PLAYER = 2;
	private static final String NAME_ROOM = "ADVIVINZANZA NUMERO";
	private static final int NUM_MAX_TRY = 7;
	private static int numJugadores = 0;
	private Map<Integer, NGChallenge> mapasChallenge;

	public NGRoomAdivinarNumero() {
		super();
		// Reglas de la sala.
		rules = rulesRoom();
		// Registration Name.
		registrationName = NAME_ROOM;
		description = descriptionRoom();
		// El timeout se actuliza cada vez que el usuario anterior responde.
		// Si no responde pasado el timeout el jugador ha perdido.
		mapasChallenge = new HashMap<Integer, NGChallenge>();
		mapasChallenge = crearNGChallenge();
	}

	@Override
	public boolean registerPlayer(NGPlayerInfo p) {
		// Tenemos que modificar el numJugadores cada vez que se añade un player.
		return numJugadores <= NUM_MAX_PLAYER;
	}

	@Override
	public String getRules() {
		return rules;
	}

	@Override
	public NGRoomStatus checkStatus(NGPlayerInfo p) {
		// TODO Auto-generated method stub
		return null;
	}

	// Devolvemos todos los challenges de la sala. No se contruye ninguno despues de
	// conseguir uno,
	// simplemente se alcanza los indicados en la ROOM.
	@Override
	public NGChallenge checkChallenge(NGPlayerInfo p) {
		// TODO Auto-generated method stub
		// Añadimos a este usuario su nuevo score.
		mapasChallenge.get(1);
		return null;
	}

	@Override
	public NGRoomStatus noAnswer(NGPlayerInfo p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NGRoomStatus answer(NGPlayerInfo p, String answer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removePlayer(NGPlayerInfo p) {
		// TODO Auto-generated method stub

	}

	@Override
	public NGRoomManager duplicate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRegistrationName() {
		return registrationName;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public int playersInRoom() {
		return numJugadores;
	}

	private String rulesRoom() {
		return "El servidor ha pensado un numero aleatorio entre 1 y 20, y tienes que adivinarlo.El servidor te dirá si cada intento es muy alto o muy bajo.";
	}

	private String descriptionRoom() {
		return "Tú ganas si adivinas el número en " + NUM_MAX_TRY + " o menos intentos";
	}

	// Challenges de la sala de juego.
	private Map<Integer, NGChallenge> crearNGChallenge() {
		HashMap<Integer, NGChallenge> mapa = new HashMap<Integer, NGChallenge>();
		NGChallenge adivinarNumero = new NGChallenge(Short.valueOf("1"), "Adivinar el numero en mas de 4 intentos");
		NGChallenge adivinarNumero4Intento = new NGChallenge(Short.valueOf("2"), "Adivinar el numero al 4º intento");
		NGChallenge adivinarNumero3Intento = new NGChallenge(Short.valueOf("3"), "Adivinar el numero al 3º intento");
		NGChallenge adivinarNumero2Intento = new NGChallenge(Short.valueOf("4"), "Adivinar el numero al 2º intento");
		NGChallenge adivinarNumeroAlaPrimera = new NGChallenge(Short.valueOf("5"), "Adivinar el numero a la primera");

		mapa.put(1, adivinarNumeroAlaPrimera);
		mapa.put(2, adivinarNumero2Intento);
		mapa.put(3, adivinarNumero3Intento);
		mapa.put(4, adivinarNumero4Intento);
		mapa.put(5, adivinarNumero);
		mapa.put(6, adivinarNumero);
		mapa.put(7, adivinarNumero);

		return mapa;
	}

	public void printNGChallenge() {
		System.out.println("\nChallenges de NGRoomAdivinarNumero");
		for (Integer key : mapasChallenge.keySet()) {
			System.out.println(mapasChallenge.get(key).toString());
		}
	}

	@Override
	public String toString() {
		return "NGRoomAdivinarNumero \nRules=" + rules + ""
				+ "\nDescription=" + description;
	}
	
	public void imprimirTodo() {
		System.out.println(this.toString());
		printNGChallenge();
		
	}

}
