package es.um.redes.nanoGames.client.comm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import es.um.redes.nanoGames.broker.BrokerClient;

//This class provides the functionality required to exchange messages between the client and the game server 
public class NGGameClient {
	private Socket socket;
	protected DataOutputStream dos;
	protected DataInputStream dis;
	
	private static final int SERVER_PORT = 6969;

	public NGGameClient(String serverName) {
		//Creation of the socket and streams
		//TODO Tanto el socket como el dos y el dis esta a null.
		try {
			InetSocketAddress socketServidor  = new InetSocketAddress(InetAddress.getByName(serverName), SERVER_PORT);
			this.socket = new Socket(socketServidor.getHostName(),socketServidor.getPort());
			this.dos = new DataOutputStream(socket.getOutputStream());
			this.dis = new DataInputStream(socket.getInputStream());
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public boolean verifyToken(long token,BrokerClient brokerClient) throws IOException {
		//SND(token) and RCV(TOKEN_VALID) or RCV(TOKEN_INVALID)
		long tokenActual = brokerClient.getToken();
		boolean esValido = false;
		
		if((tokenActual - token) < 1000) {
			// Crear mensaje de CONFIRMACION; AJO AJO
			esValido = true;
		} else {
			// Crear mensaje de DENEGACION;
			esValido = false;
		}
		//Make message (NGMessage.makeXXMessage)
		//Send messge (dos.write())
		//this.dos.writeLong(token);
		//Receive response (NGMessage.readMessageFromSocket)
		return esValido;
	}
	
	public boolean registerNickname(String nick) throws IOException {
		//SND(nick) and RCV(NICK_OK) or RCV(NICK_DUPLICATED)
		//TODO
		return true;
	}

	//TODO
	//add additional methods for all the messages to be exchanged between client and game server
	
	
	//Used by the shell in order to check if there is data available to read 
	public boolean isDataAvailable() throws IOException {
		return (dis.available() != 0);
	}
	

	//To close the communication with the server
	public void disconnect() {
		//TODO
	}
}
