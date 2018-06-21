package es.um.redes.nanoGames.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import es.um.redes.nanoGames.server.roomManager.NGRoomAdivinarNumero;
import es.um.redes.nanoGames.server.roomManager.NGRoomAdivinarNumeroTurno;
import es.um.redes.nanoGames.server.roomManager.NGRoomManager;

public class NanoGameServer implements Runnable {

    public static final int PORT = 6969;

    private InetSocketAddress socketAddress;
    private ServerSocket serverSocket = null;
    private NGServerManager manager;

    private static String brokerHostname;

    // Estructura para asociar cada jugador a su socket
    protected static Map<Integer,Map<NGPlayerInfo,Socket>> mapaSockets;

    public static NanoGameServer create(int port) {
        return new NanoGameServer(new InetSocketAddress(port));
    }

    private NanoGameServer(InetSocketAddress a) {
        this.socketAddress = a;

        //This will be the Status shared among all the Threads
        manager = new NGServerManager();
        mapaSockets = new HashMap<>();
        // Añadimos al servidor las sala de juegos disponibles
        NGRoomManager san = new NGRoomAdivinarNumero();
        NGRoomManager sant = new NGRoomAdivinarNumeroTurno();
        manager.registerRoomManager(1,san);
        manager.registerRoomManager(2,sant);
        // Actualizamos el mapa de sockets
        mapaSockets.put(1, new HashMap<>());
        mapaSockets.put(2, new HashMap<>());
    }

    /**
     * Función del hilo principal del servidor.
     *
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            while (true) {
                // Waits for a new connection
                // accept returns a socket to exchange messages with the incoming client
                Socket s = serverSocket.accept();
                System.out.println("New client connected from " + s.getInetAddress().toString() + ":" + s.getPort());
                // A new thread is started and it receives the shared status, the socket and the broker address
                new NGServerThread(manager, s, brokerHostname).start();
            }
        } catch (IOException e) {
            // Do nothing
        }
    }

    /**
     * Inicio del hilo del servidor.
     */
    public void init() {
        try {
            // We create the server socket and it is bound to the address and port specified by socketAddress
            serverSocket = new ServerSocket();
            serverSocket.bind(socketAddress);
            serverSocket.setReuseAddress(true);
        } catch (IOException e) {
            System.err.println("Could not listen on port: "
                    + socketAddress.getPort() + ".");
            System.exit(-1);
        }

        //We start this class as a thread in order to run in background
        new Thread(this).start();

        System.out.println("Server running on port " +
                socketAddress.getPort() + ".");
    }

    public static void main(String[] args) {
        NanoGameServer server = NanoGameServer.create(PORT);
        if (args.length != 1) {
            System.out.println("* Correct use: java NanoGameServer <broker>");
            return;
        } else
            brokerHostname = args[0];
        server.init();
    }


}
