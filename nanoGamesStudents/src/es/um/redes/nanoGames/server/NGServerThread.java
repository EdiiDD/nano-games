package es.um.redes.nanoGames.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.hibernate.Session;
import org.hibernate.Transaction;

import es.um.redes.nanoGames.broker.BrokerClient;
import es.um.redes.nanoGames.client.application.NGController;
import es.um.redes.nanoGames.message.NGMensajeConfirmar;
import es.um.redes.nanoGames.message.NGMensajeEntrarSala;
import es.um.redes.nanoGames.message.NGMensajeEnviarNickname;
import es.um.redes.nanoGames.message.NGMensajeEnviarToken;
import es.um.redes.nanoGames.message.NGMensajeListaSalas;
import es.um.redes.nanoGames.message.NGMensajeListarSalas;
import es.um.redes.nanoGames.message.NGMensajePregunta;
import es.um.redes.nanoGames.message.NGMensajeRespuesta;
import es.um.redes.nanoGames.server.roomManager.NGRoomManager;
import es.um.redes.nanoGames.utils.HibernateUtil;

/**
 * A new thread runs for each connected client
 */
public class NGServerThread extends Thread {

	// Possible states of the connected client
	private static final byte PRE_TOKEN = 1;
	private static final byte PRE_REGISTRATION = 2;
	private static final byte OFF_ROOM = 3;
	private static final byte IN_ROOM = 4;

	// Time difference between the token provided by the client and the one obtained
	// from the broker directly
	private static final long TOKEN_THRESHOLD = 1500; // 15 seconds
	// Socket to exchange messages with the client
	private Socket socket = null;
	// Global and shared manager between the threads
	private NGServerManager serverManager = null;
	// Input and Output Streams
	private DataInputStream dis;
	private DataOutputStream dos;
	// Utility class to communicate with the Broker
	BrokerClient brokerClient;
	// Current player
	NGPlayerInfo player;
	// Current RoomManager (it depends on the room the user enters)
	NGRoomManager roomManager;
	private int numberOfTries = 0;
	private int lastAnswer;
	private boolean gameEnded= false;
	
	// TODO Add additional fields
	private static final int MAXIMUM_TCP_SIZE = 65535;

	public NGServerThread(NGServerManager manager, Socket socket, String brokerHostname) {
		this.socket = socket; // el socket de este hilo es el que nos pasen
		try {
			this.dis = new DataInputStream(socket.getInputStream());
			this.dos = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Either the dis or the dos couldn't be made.");
		}
		this.brokerClient = new BrokerClient(brokerHostname); // el brokerClient uno nuevo con ese nombre
		this.serverManager = manager; // el manager del server, pues ese

	}

	// Main loop
	public void run() {
		try {
			// We obtain the streams from the socket
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			// The first step is to receive and to verify the token
			receiveAndVerifyToken();
			// The second step is to receive and to verify the nick name
			receiveAndVerifyNickname();
			// While the connection is alive...
			while (true) {
				sendRoomList();
				if (reciveAndVerifyJoinToRoom()) {
					/*Si se verifica no se envia un confirmar, se enviara una pregunta cuando todos esten dentro*/
					waitToOtherPlayers();
					/*Todos estan listos, este mensaje será su equivalente a 'confirmar'*/

					sendPregunta();		//enviamos la pregunta
					do {
						recieveRespuesta();	//vemos la respuesta
						sendPregunta();		//preguntamos si falló o le enviamos un confirmar si acertó
					} while(!gameEnded);	
					
				}
				
			}
		} catch (Exception e) {
			// If an error occurs with the communications the user is removed from all the
			// managers and the connection is closed
			// TODO
		}
		// TODO Close the socket
	}

	// Receive and verify Token (token enviado por el cliente)
	// TODO
	private void receiveAndVerifyToken() throws IOException {
		boolean tokenVerified = false;
		while (!tokenVerified) {

			// We extract the token from the message
			// now we obtain a new token from the broker
			// We check the token and send an answer to the client
			long tokenPropio = brokerClient.getToken();
			// Recibir(leemos) por DataInputStream, lo leido lo guardamos en el buffer
			// arrayBytes.
			byte[] arrayBytes = new byte[MAXIMUM_TCP_SIZE];
			dis.read(arrayBytes);
			String token_recibido = new String(arrayBytes);
			// Creamos el mensaje, NGMensajeEnviarToken.
			NGMensajeEnviarToken met_recibido = new NGMensajeEnviarToken();
			// Procesamos el mensaje anteriormente creado("guardamos el token a enviar").
			met_recibido.processNGMensajeEnviarToken(token_recibido);
			// Creamos el mensaje de confirmacion
			NGMensajeConfirmar mc_enviar = new NGMensajeConfirmar();
			// Verificamoso el token.
			if (tokenPropio - met_recibido.getToken() <= TOKEN_THRESHOLD)
				tokenVerified = true;
			// Creacion del mensaje de respuesta, dicha confirmacion depende de la
			// verificacion del token.
			String mensaje_confirmar = mc_enviar.createNGMensajeConfirmar(tokenVerified);
			dos.write(mensaje_confirmar.getBytes());
			
		}
	}

	// We obtain the nick and we request the server manager to verify if it is
	// duplicated
	// TODO
	private void receiveAndVerifyNickname() throws IOException {
		boolean nickVerified = false;
		NGPlayerInfo ngp;
		// this loop runs until the nick provided is not duplicated
		while (!nickVerified) {
			// We obtain the nick from the message
			// we try to add the player in the server manager
			// if success we send to the client the NICK_OK message
			// otherwise we send DUPLICATED_NICK

			// Recibir(leemos) por DataInputStream, lo leido lo guardamos en el buffer
			// arrayBytes.
			// TODO No le llega ningun nombre, tengo que pillarlo del mensaje
			byte[] arrayBytes = new byte[MAXIMUM_TCP_SIZE];
			// TODO es en este dis donde se escribe pero lo del token
			dis.read(arrayBytes);
			String s = new String(arrayBytes);
			// Creamos el mensaje, NGMensajeEnviarNickname.
			NGMensajeEnviarNickname men_recibido = new NGMensajeEnviarNickname();
			// Procesamos el mensaje anteriormente creado("guardamos el token a enviar").
			men_recibido.processNGMensajeEnviarNickname(s);
			// Creamos el mensaje de confirmacion
			NGMensajeConfirmar mc_enviar = new NGMensajeConfirmar();
			ngp = new NGPlayerInfo(men_recibido.getNickname(), 0);
			// Verificamos el NGPlayerInfo.
			nickVerified = create(ngp);
			String mensaje_confirmar = mc_enviar.createNGMensajeConfirmar(nickVerified);
			dos.write(mensaje_confirmar.getBytes());

		}
	}

	// We send to the client the room list
	// TODO
	private void sendRoomList() throws IOException {
		// The room list is obtained from the server manager
		// Then we build all the required data to send the message to the client
		byte[] arrayBytes = new byte[MAXIMUM_TCP_SIZE];
		dis.read(arrayBytes);
		String s = new String(arrayBytes);
		NGMensajeListarSalas mls_recibido = new NGMensajeListarSalas();
		mls_recibido.processNGMensajeListarSalas(s);
		int numSalas = NanoGameServer.salasServidor.size();
		String descripcionSalas = "";
		for (Integer rm : NanoGameServer.salasServidor.keySet()) {
			descripcionSalas +=NanoGameServer.salasServidor.get(rm);
		}
		NGMensajeListaSalas mls_enviar = new NGMensajeListaSalas();
		String listaSalas = mls_enviar.createNGMensajeListaSalas(numSalas, descripcionSalas);
		System.out.println("Enviamos de roomList: "+listaSalas);
		dos.write(listaSalas.getBytes());
		
	}
	
	
	private boolean reciveAndVerifyJoinToRoom() throws IOException {
		byte[] arrayBytes = new byte[MAXIMUM_TCP_SIZE];
		dis.read(arrayBytes);
		String s = new String(arrayBytes);
		NGMensajeEntrarSala entrar_sala = new NGMensajeEntrarSala();
		entrar_sala.processNGMensajeEntrarSala(s);
		int sala = entrar_sala.getNumSala();
		int salasTotales = NanoGameServer.salasServidor.size();
		boolean condition = false;
		if ((sala>0) && (salasTotales>=sala)) {
		//la sala solicitada existe
			NGRoomManager rm = NanoGameServer.salasServidor.get(sala);
			this.roomManager = rm;
			
			/* NECESITO PODER SABER si el juego ha comenzado o no para poder permitirle que se una 
			 * lo del max players in room lo he añadido yo pero hace falta lo otro esto es necesario
			 * para saber cuando ha empezado el juego 
			 * if ((this.roomManager.aempezaoono)&&(this.roomManager.playersInRoom()<this.roomManager.maxPlayersInRoom)) 
			 * condition = true*/
		}
		if (condition==false) { /*Caso especial, enviar un denegar, sino seguimos por otros metodos*/
			NGMensajeConfirmar denegar = new NGMensajeConfirmar();
			String data = denegar.createNGMensajeConfirmar(false);
			this.dos.write(data.getBytes());
		}
		return condition;
	}

	private void waitToOtherPlayers() {
		boolean condition = false;
		while (!condition) {
			if (this.roomManager.playersInRoom() == this.roomManager.maxPlayersInRoom())
				condition = true;
		}
	}
	
	private void sendPregunta() {
		String data_to_send;
		String closeOrFar;
			/*Necesitaremos un metodo que nos de el numero a adivinar para el servidor, esto podria hacerse de la siguiente manera: cuando 
			 * entra un usuario al room manager y se mete como jugador se genera un numero aleatorio, asi nunca se sabra cual es y cambiara
			 * para cualquier partida en dicha sala pero no entre rondas pues un jugador no puede entrar mientras este en curso*/
		if (this.roomManager.getNumber>this.lastAnswer) {
			closeOrFar ="Te has ido a cuenca";
		} else if (this.roomManager.getNumber<this.lastAnswer) {
			closeOrFar = "Cerca pero no";
		}
		else {
			/*Enviamos confirmar porque ha acertado, siguiendo el automata*/
			NGMensajeConfirmar confirmar = new NGMensajeConfirmar();
			data_to_send =confirmar.createNGMensajeConfirmar(true);
			this.gameEnded=true;
			this.dos.write(data_to_send.getBytes());
			return;
		}
		NGMensajePregunta pregunta = new NGMensajePregunta();
		/*La primera pregunta no tiene un mensaje real para el jugador*/
		if (numberOfTries == 0)
			data_to_send = pregunta.createNGMensajePregunta(this.roomManager.getRules(), "Te diré si estas cerca o te pasaste, gg campeon");
		else {
			data_to_send = pregunta.createNGMensajePregunta(this.roomManager.getRules(), closeOrFar);
		}
	}
	
	private void recieveRespuesta() throws IOException {
		byte[] array_bytes = new byte[MAXIMUM_TCP_SIZE];
		this.dis.read(array_bytes);
		NGMensajeRespuesta respuesta = new NGMensajeRespuesta();
		respuesta.processNGMensajeRespuesta(new String(array_bytes));
		this.numberOfTries++;
		this.lastAnswer = Integer.parseInt(respuesta.getRespuesta());
	}
	
	// Method to process messages received when the player is in the room
	// TODO
	private void processRoomMessages() throws IOException {
		// First we send the rules and the initial status
		// Now we check for incoming messages, status updates and new challenges
		boolean exit = false;
		while (!exit) {
			// TODO
		}
	}
	
	// AÃ±adir un player.
		public boolean create(Object obj) {
			boolean nickValido = true;
			Session session = HibernateUtil.getSessionFactory().openSession();			
			Transaction trans = null;
			
			try {
				trans = session.beginTransaction();
				session.save((NGPlayerInfo) obj);
				session.getTransaction().commit();
			} catch (RuntimeException e) {
				if (trans != null) {
					trans.rollback();
					nickValido = false;
					System.err.println("Nick no valido.");
				}
			} finally {
				session.close();
			}
			return nickValido;
		}

}
