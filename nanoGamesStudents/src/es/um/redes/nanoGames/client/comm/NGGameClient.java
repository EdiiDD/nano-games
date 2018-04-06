package es.um.redes.nanoGames.client.comm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.omg.CORBA.DATA_CONVERSION;

import es.um.redes.nanoGames.broker.BrokerClient;
import es.um.redes.nanoGames.client.application.NGController;
import es.um.redes.nanoGames.message.NGMensajeConfirmar;
import es.um.redes.nanoGames.message.NGMensajeEnviarNickname;
import es.um.redes.nanoGames.message.NGMensajeEnviarToken;
import es.um.redes.nanoGames.message.NGMensajeListaSalas;
import es.um.redes.nanoGames.message.NGMensajeListarSalas;
import es.um.redes.nanoGames.server.NGPlayerInfo;

//This class provides the functionality required to exchange messages between the client and the game server 
public class NGGameClient {
	private Socket socket;
	protected DataOutputStream dos;
	protected DataInputStream dis;

	private static final int SERVER_PORT = 6969;
	private static final int MAXIMUM_TCP_SIZE = 65535;

	public NGGameClient(String serverName) {
		// Creation of the socket and streams
		try {
			// Creacion de IP Socket Address ( IP + PUERTO), con nombre del servidor y se
			// puerto por defecto
			InetSocketAddress socketServidor = new InetSocketAddress(InetAddress.getByName(serverName), SERVER_PORT);
			// Creacion de socket (para enviar y recivir datagram packets) que escucha en en
			// puerto del servidor -> 6969
			this.socket = new Socket(socketServidor.getHostName(), socketServidor.getPort());
			this.dos = new DataOutputStream(socket.getOutputStream());
			this.dis = new DataInputStream(socket.getInputStream());

		} catch (UnknownHostException e) {
			System.err.println("IP address of a host could not be determined");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public boolean verifyToken(long token, BrokerClient brokerClient) throws IOException {

		/**
		 * De forma cutre: este metodo simplemente cogera el token y lo lanzara hacia el
		 * cliente el servidor le devolvera un booleano si cuando le pida el suyo ve que
		 * es menor de mil de diferencia
		 */

		// Declaracion del mensaje a enviar, NGMensajeEnviarToken.
		NGMensajeEnviarToken met_enviar = new NGMensajeEnviarToken();
		// Crear el mensaje NGMensajeEnviarToken, con la variable token.
		String data_to_send = met_enviar.createNGMensajeEnviarToken(token);
		// Enviamos(escribimos) por DataOutputStream.
		dos.write(data_to_send.getBytes());
		// Creacion del buffer con tamaño maximo.
		byte[] arrayBytes = new byte[MAXIMUM_TCP_SIZE];
		// Recibir(leemos) por DataInputStream
		dis.read(arrayBytes);
		String data_recived = new String(arrayBytes);
		// Declaracion del mensaje, NGMensajeConfirmar.
		NGMensajeConfirmar mc_recived = new NGMensajeConfirmar();
		// Procesamos los datos que nos llega.
		mc_recived.processNGMensajeConfirmar(data_recived);
		// Devolvemos el valor del campo PARAMETRO.
		return mc_recived.isConfirmated();
	}

	public boolean registerNickname(String nick) throws IOException {
		// SND(nick) and RCV(NICK_OK) or RCV(NICK_DUPLICATED)
		// Declaracion del mensaje NGMensajeEnviarNickname.
		NGMensajeEnviarNickname men_enviar = new NGMensajeEnviarNickname();
		// Crear el mensaje NGMensajeEnviarNickname, con la confirmacion del nick.
		String data_to_send = men_enviar.createNGMensajeEnviarNickname(nick);
		// Enviamos(escribimos) por DataOutputStream.
		dos.write(data_to_send.getBytes());
		// Creacion del buffer con tamaño maximo.
		byte[] arrayBytes = new byte[MAXIMUM_TCP_SIZE];
		// Recibir(leemos) por DataInputStream
		dis.read(arrayBytes);
		String data_recived = new String(arrayBytes);
		// Declaracion del mensaje, NGMensajeConfirmar.
		NGMensajeConfirmar mc_recived = new NGMensajeConfirmar();
		// Procesamos los datos que nos llega.
		mc_recived.processNGMensajeConfirmar(data_recived);
		// Devolvemos el valor del campo PARAMETRO.
		return mc_recived.isConfirmated();
	}

	public void seeRoomList() {
		NGMensajeListaSalas mls_recived = new NGMensajeListaSalas();
		try {
			// Declaracion del mensaje NGMensajeListaSalas;
			NGMensajeListarSalas mls_enviar = new NGMensajeListarSalas();
			// Crear el mensaje NGMensajeListaSalas, con la lista de las salas.
			String data_to_send = mls_enviar.createNGMensajeListarSalas();
			// Enviamos(escribimos) por DataOutPutSream.
			dos.write(data_to_send.getBytes());
			// Creacion del buffer con tamaño maximo-
			byte[] arrayBytes = new byte[MAXIMUM_TCP_SIZE];
			// Recibir(leemos) por DataInputStream
			dis.read(arrayBytes);
			String data_recived = new String(arrayBytes);
			// Declaracion del mensaje, NGMensajeConfirmar.
			mls_recived = new NGMensajeListaSalas();
			// Procesamos los datos que nos llega.
			mls_recived.processNGMensajeListaSalas(data_recived);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int numSalas = mls_recived.getNumSalas();
		System.out.println(mls_recived);
		System.out.println("Numero de salas: " + numSalas);
		for (int i = 0; i < numSalas; i++) {
			System.out.println("Sala " + i + " " + mls_recived.getSala(i));
		}

	}
	// TODO
	// add additional methods for all the messages to be exchanged between client
	// and game server

	// Used by the shell in order to check if there is data available to read
	public boolean isDataAvailable() throws IOException {
		return (dis.available() != 0);
	}

	// To close the communication with the server
	public void disconnect() {
		// TODO
	}

}
