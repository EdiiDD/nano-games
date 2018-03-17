package es.um.redes.nanoGames.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import es.um.redes.nanoGames.broker.BrokerClient;
import es.um.redes.nanoGames.message.NGMensajeConfirmar;
import es.um.redes.nanoGames.message.NGMensajeEnviarToken;
import es.um.redes.nanoGames.server.roomManager.NGRoomManager;

/**
 * A new thread runs for each connected client
 */
public class NGServerThread extends Thread {
	
	//Possible states of the connected client
	private static final byte PRE_TOKEN = 1;
	private static final byte PRE_REGISTRATION = 2;
	private static final byte OFF_ROOM = 3;
	private static final byte IN_ROOM = 4;
	
	//Time difference between the token provided by the client and the one obtained from the broker directly
	private static final long TOKEN_THRESHOLD = 1500; //15 seconds
	//Socket to exchange messages with the client
	private Socket socket = null;
	//Global and shared manager between the threads
	private NGServerManager serverManager = null;
	//Input and Output Streams
	private DataInputStream dis;
	private DataOutputStream dos;
	//Utility class to communicate with the Broker
	BrokerClient brokerClient;
	//Current player
	NGPlayerInfo player;
	//Current RoomManager (it depends on the room the user enters)
	NGRoomManager roomManager;
	//TODO Add additional fields
	private static final int MAXIMUM_TCP_SIZE =65535;

	public NGServerThread(NGServerManager manager, Socket socket, String brokerHostname) {
		this.socket = socket;															//el socket de este hilo es el que nos pasen
		try {
			this.dis = new DataInputStream(socket.getInputStream());
			this.dos = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Either the dis or the dos couldn't be made.");
		}
		this.brokerClient = new BrokerClient(brokerHostname);							//el brokerClient uno nuevo con ese nombre
		this.serverManager = manager;													//el manager del server, pues ese	
	
	}

	//Main loop
	public void run() {
		try {
			//We obtain the streams from the socket
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			//The first step is to receive and to verify the token
			receiveAndVerifyToken();
			//The second step is to receive and to verify the nick name
			receiveAndVerifyNickname();
			//While the connection is alive...
			while (true) {
				//TODO Rest of messages according to the automata
			}
		} catch (Exception e) {
			//If an error occurs with the communications the user is removed from all the managers and the connection is closed
			//TODO
		}
		//TODO Close the socket
	}

	//Receive and verify Token
	//TODO
	private void receiveAndVerifyToken() throws IOException {
		boolean tokenVerified = false;
		while (!tokenVerified) {
			/*long tokenCliente = this.dis .readLong();
			if (!(brokerClient.getToken()-tokenCliente>=TOKEN_THRESHOLD)) //si su token - el token que lee del cliente no es mayorigual
					tokenVerified=true;									  //se verifica
			this.dos.writeBoolean(tokenVerified);*/				//si esto no se da, el cliente tendra que volver a enviar el token
			
			//We extract the token from the message
				//now we obtain a new token from the broker
				//We check the token and send an answer to the client
			long tokenPropio = brokerClient.getToken();
			byte[] arrayBytes = new byte[MAXIMUM_TCP_SIZE];
			dis.read(arrayBytes);
			String token_recibido = new String(arrayBytes);
			NGMensajeEnviarToken met_recibido = new NGMensajeEnviarToken();
			met_recibido.processNGMensajeEnviarToken(token_recibido);
			NGMensajeConfirmar mc_enviar = new NGMensajeConfirmar();
			if (tokenPropio - met_recibido.getToken() <= TOKEN_THRESHOLD) 
				tokenVerified=true;
			String mensaje_confirmar = mc_enviar.createNGMensajeConfirmar(tokenVerified);
			dos.write(mensaje_confirmar.getBytes());
		}
	}

	//We obtain the nick and we request the server manager to verify if it is duplicated
	//TODO
	private void receiveAndVerifyNickname() throws IOException {
		boolean nickVerified = false;
		//this loop runs until the nick provided is not duplicated
		while (!nickVerified) {
				//We obtain the nick from the message
				//we try to add the player in the server manager
				//if success we send to the client the NICK_OK message
				//otherwise we send DUPLICATED_NICK
		}
	}

	//We send to the client the room list
	//TODO
	private void sendRoomList() throws IOException {
		//The room list is obtained from the server manager
		//Then we build all the required data to send the message to the client 
	}

	//Method to process messages received when the player is in the room
	//TODO
	private void processRoomMessages() throws IOException {
		//First we send the rules and the initial status
		//Now we check for incoming messages, status updates and new challenges
		boolean exit = false;
		while (!exit) {
			//TODO
		}
	}

}
