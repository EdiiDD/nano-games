package es.um.redes.nanoGames.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


import es.um.redes.nanoGames.broker.BrokerClient;
import es.um.redes.nanoGames.message.*;
import es.um.redes.nanoGames.server.roomManager.NGChallenge;
import es.um.redes.nanoGames.server.roomManager.NGRoomAdivinarNumero;
import es.um.redes.nanoGames.server.roomManager.NGRoomManager;
import es.um.redes.nanoGames.server.roomManager.NGRoomStatus;

/**
 * A new thread runs for each connected client
 */
public class NGServerThread extends Thread {

    // Possible states of the connected client
    private static final byte PRE_TOKEN = 1;
    private static final byte PRE_REGISTRATION = 2;
    private static final byte REGISTRATION = 3;
    private static final byte OFF_ROOM = 4;
    private static final byte IN_ROOM = 5;

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
    private volatile BrokerClient brokerClient;
    // Current currentPlayer
    private NGPlayerInfo currentPlayer;
    // Current RoomManager (it depends on the room the user enters)
    private NGRoomManager currentRoomManager;
    // Current RoomManager;
    private int numSalaActual;
    // Estado de la sala.
    private NGRoomStatus estadoSala;


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
        this.brokerClient = new BrokerClient(brokerHostname);
        this.serverManager = manager;
        this.currentPlayer = new NGPlayerInfo("Jugador", 0);
        this.currentPlayer.setSatusPlayer(new NGRoomStatus(PRE_TOKEN, "Sin Token"));

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
            // Pedimos la lista de salas
            sendRoomList();
            // While the connection is alive...
            // Entramos a una sala
            enterTheRoom();
            while (true) {
                // Dentro de la sala
                processRoomMessages();

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
            this.currentPlayer.setSatusPlayer(new NGRoomStatus(PRE_REGISTRATION, "Pre Registration"));
            dos.write(mensaje_confirmar.getBytes());

        }
    }

    // We obtain the nick and we request the server manager to verify if it is
    // duplicated
    // TODO
    private void receiveAndVerifyNickname() throws IOException {
        boolean nickVerified = false;
        // this loop runs until the nick provided is not duplicated
        while (!nickVerified) {
            // We obtain the nick from the message
            // we try to add the currentPlayer in the server manager
            // if success we send to the client the NICK_OK message
            // otherwise we send DUPLICATED_NICK

            byte[] arrayBytes = new byte[MAXIMUM_TCP_SIZE];
            dis.read(arrayBytes);
            String s = new String(arrayBytes);
            NGMensajeEnviarNickname men_recibido = new NGMensajeEnviarNickname();
            men_recibido.processNGMensajeEnviarNickname(s);
            NGMensajeConfirmar mc_enviar = new NGMensajeConfirmar();

            currentPlayer.setNick(men_recibido.getNickname());
            currentPlayer.setSatusPlayer(new NGRoomStatus(REGISTRATION, "Registrado"));
            nickVerified = serverManager.addPlayer(currentPlayer);

            String mensaje_confirmar = mc_enviar.createNGMensajeConfirmar(nickVerified);
            System.out.println("*El jugador " + currentPlayer.getNick() + " se ha registrado en el servidor");
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

        int numSalas = serverManager.getSalasServidor().size();
        String descripcionSalas = "";

        for (Integer rm : serverManager.getSalasServidor().keySet()) {

            descripcionSalas += serverManager.getRoomDescription(serverManager.getSalasServidor().get(rm));
            descripcionSalas += " & ";
        }
        NGMensajeListaSalas mls_enviar = new NGMensajeListaSalas();
        String listaSalas = mls_enviar.createNGMensajeListaSalas(numSalas, descripcionSalas);
        dos.write(listaSalas.getBytes());

    }

    private void enterTheRoom() {

        NGMensajeConfirmar mensajeConfirmarEnviar = new NGMensajeConfirmar();
        String confirmarSala;
        boolean sePuedeEntrar = false;
        try {
            byte[] arrayBytes = new byte[MAXIMUM_TCP_SIZE];
            dis.read(arrayBytes);
            String s = new String(arrayBytes);

            NGMensajeEntrarSala mensajeEntrarSalaRecibido = new NGMensajeEntrarSala();
            mensajeEntrarSalaRecibido.processNGMensajeEntrarSala(s);

            currentRoomManager = serverManager.getSalasServidor().get(mensajeEntrarSalaRecibido.getNumSala());
            if (serverManager.enterRoom(currentPlayer, currentRoomManager, mensajeEntrarSalaRecibido.getNumSala()) != null) {

                System.out.println("*El jugador " + currentPlayer.getNick() + " ha entrada a la sala " + currentRoomManager.getRegistrationName() + " el numero ha adivinar es: " + NGRoomAdivinarNumero.getNumeroAleatorio());
                currentPlayer.setSatusPlayer(new NGRoomStatus(IN_ROOM, "En Sala"));
                this.numSalaActual = mensajeEntrarSalaRecibido.getNumSala();
                // El mapa de los jugadores asociado a su socket, dentro de la sala actual.
                Map<NGPlayerInfo, Socket> listaSockets = NanoGameServer.mapaSockets.get(this.numSalaActual);
                listaSockets.put(currentPlayer, this.socket);
                NanoGameServer.mapaSockets.put(mensajeEntrarSalaRecibido.getNumSala(), listaSockets);
                sePuedeEntrar = true;
            } else {
                System.out.println("*El jugador " + currentPlayer.getNick() + " no puede entrar a la sala ");

            }

            confirmarSala = mensajeConfirmarEnviar.createNGMensajeConfirmar(sePuedeEntrar);
            dos.write(confirmarSala.getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    // Method to process messages received when the currentPlayer is in the room
    // TODO
    private void processRoomMessages() throws IOException {
        // First we send the rules and the initial status
        this.estadoSala = currentRoomManager.checkStatus(currentPlayer);
        sendRules();
        // Now we check for incoming messages, status updates and new challenges
        boolean exit = false;
        NGChallenge challenge = currentRoomManager.checkChallenge(currentPlayer);
        System.out.println("*El jugador " + currentPlayer.getNick() + " tiene el reto " + challenge.toString());
        while (!exit) {
            // Si no se recibe ninguna respuesta, es decir, se ha perddo la conexión con el cliente.
            proceessNewChallenge(challenge);
        }
    }

    private AtomicBoolean timeout_triggered = new AtomicBoolean();

    //Private class to implement a very simple timer
    private class Timeout extends TimerTask {
        @Override
        public void run() {
            timeout_triggered.set(true);
        }
    }

    private void proceessNewChallenge(NGChallenge challenge) throws IOException {
        //We send the challenge to the client
        //TODO
        //Now we set the timeout
        Timer timer = null;
        timeout_triggered.set(false);
        timer = new Timer();
        timer.schedule(new Timeout(), currentRoomManager.getTimeout(), currentRoomManager.getTimeout());

        boolean answerProvided = false;

        //Loop until an answer is provided or the timeout expires
        while (!timeout_triggered.get() && !answerProvided) {
            if (dis.available() > 0) {
                //The client sent a message VER EL STATUS QUE DEVUELVE ESTE!!
                //IF ANSWER Then call currentRoomManager.answer() and proceed
                processAnswer(challenge);


            } else
                try {
                    //To avoid a CPU-consuming busy wait
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    //Ignore
                }
        }

        // TODO Avisar a todos los jugadores de la sala que hay un ganador.


        if (!answerProvided) {
            //The timeout expired
            timer.cancel();
            //TODO call currentRoomManager.noAnswer() and proceed
            System.out.println("*El player " + currentPlayer.getNick() + " no ha respondido pasado un tiempo!!");
            //currentRoomManager.noAnswer(currentPlayer);
        }
    }

    private void sendRules() {

        try {
            byte[] arrayBytes = new byte[MAXIMUM_TCP_SIZE];
            dis.read(arrayBytes);
            String datosRecibidos = new String(arrayBytes);
            NGMensajeRespuesta mrRecibido = new NGMensajeRespuesta();
            mrRecibido.processNGMensajeRespuesta(datosRecibidos);

            // "Logica del juego".
            NGMensajePregunta mpEnviar = new NGMensajePregunta();
            String respuestaEnviar = mpEnviar.createNGMensajePregunta("Las reglas son: ", currentRoomManager.getRules());
            dos.write(respuestaEnviar.getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * TODO La magra del asunto
     * -Falta: Informar a los demas de que hay un ganador!!
     */

    private void processAnswer(NGChallenge challenge) {
        try {
            byte[] arrayBytes = new byte[MAXIMUM_TCP_SIZE];
            dis.read(arrayBytes);
            String datosRecibidos = new String(arrayBytes);
            NGMensajeRespuesta mrRecibido = new NGMensajeRespuesta();
            NGMensajePregunta mpEnviar = new NGMensajePregunta();
            mrRecibido.processNGMensajeRespuesta(datosRecibidos);

            // "Logica del juego", tenemos que enviar el mensaje por todos los sockets

            // Sala por turno
            if (numSalaActual == 2) {

                // Recupero el mapa de los player-socket asociados a la sala.
                Map<NGPlayerInfo, Socket> ppa = NanoGameServer.mapaSockets.get(numSalaActual);
                String respuestas = "";
                currentRoomManager.answer(currentPlayer, mrRecibido.getRespuesta(), challenge);
                // Recorro todas las respuestas de los jugadores.
                for (NGPlayerInfo player : ppa.keySet()) {
                    respuestas += player.getSatusPlayer().getStatus() + "       ";
                }

                // Envio todas las respuestas a todos los jugadores.
                for (NGPlayerInfo player : ppa.keySet()) {
                    DataOutputStream dos = new DataOutputStream(ppa.get(player).getOutputStream());
                    String respuestaEnviar = mpEnviar.createNGMensajePregunta(respuestas, currentPlayer.getNick());
                    dos.write(respuestaEnviar.getBytes());
                }


            } else {
                currentRoomManager.answer(currentPlayer, mrRecibido.getRespuesta(), challenge);
                String respuestaEnviar = mpEnviar.createNGMensajePregunta(currentPlayer.getSatusPlayer().getStatus(), currentPlayer.getNick());
                dos.write(respuestaEnviar.getBytes());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void closeConection() {
        try {
            byte[] arrayBytes = new byte[MAXIMUM_TCP_SIZE];
            dis.read(arrayBytes);
            String s = new String(arrayBytes);
            System.out.println("Recibimos S1: " + s);
            NGMensajeFinJuego mfj_recibido = new NGMensajeFinJuego();
            mfj_recibido.processNGMensajeFinJuego(s);

            System.out.println("Recibimos S2: " + mfj_recibido.getOperacion());
            // Eliminamos el jugador de nuestra "base de datos".
            if (mfj_recibido.getOperacion().equals("FIN_JUEGO")) {
                serverManager.removePlayer(currentPlayer);
                System.out.println("El usuario " + currentPlayer.getNick() + " se ha desconectado del servidor");
            }
            NGMensajeSalir ms = new NGMensajeSalir();
            String ms_enviar = ms.createNGMensajeSalir();
            System.out.println("Enviamos S: " + ms_enviar);
            dos.write(ms_enviar.getBytes());

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private boolean exitRoomGame() {
        System.out.println("Salir Sala");
        try {
            byte[] arrayBytes = new byte[MAXIMUM_TCP_SIZE];
            dis.read(arrayBytes);
            String s = new String(arrayBytes);
            System.out.println("S recibe: " + s);
            NGMensajeSalirSala mssRecibido = new NGMensajeSalirSala();
            mssRecibido.processNGMensajeSalirSala(s);
            boolean salirSala = serverManager.leaveRoom(currentPlayer, currentRoomManager, numSalaActual);
            NGMensajeConfirmar mccEnviar = new NGMensajeConfirmar();
            String datosEnviar = mccEnviar.createNGMensajeConfirmar(salirSala);
            System.out.println("S envia: " + datosEnviar);
            dos.write(datosEnviar.getBytes());
            this.currentPlayer.setSatusPlayer(new NGRoomStatus(OFF_ROOM, "Fuera de sala"));
            return salirSala;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}
