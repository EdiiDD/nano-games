package es.um.redes.nanoGames.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


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
    BrokerClient brokerClient;
    // Current player
    NGPlayerInfo player;
    // Current RoomManager (it depends on the room the user enters)
    NGRoomManager roomManager;
    // Current RoomManager;
    private int numSalaActual;
    // Respuesta correcta por parte del jugador.
    private boolean respuestaOk;

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
        this.player = new NGPlayerInfo("Jugador", 0);
        this.player.setSatusPlayer(new NGRoomStatus(PRE_TOKEN, "Sin Token"));

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
                //processRoomMessages();

            }

        } catch (Exception e) {
            // If an error occurs with the communications the user is removed from all the
            // managers and the connection is closed
            // TODO


        }
        // TODO Close the socket
        exitRoomGame();

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
            this.player.setSatusPlayer(new NGRoomStatus(PRE_REGISTRATION, "Pre Registration"));
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
            // we try to add the player in the server manager
            // if success we send to the client the NICK_OK message
            // otherwise we send DUPLICATED_NICK

            byte[] arrayBytes = new byte[MAXIMUM_TCP_SIZE];
            dis.read(arrayBytes);
            String s = new String(arrayBytes);
            NGMensajeEnviarNickname men_recibido = new NGMensajeEnviarNickname();
            men_recibido.processNGMensajeEnviarNickname(s);
            NGMensajeConfirmar mc_enviar = new NGMensajeConfirmar();

            player.setNick(men_recibido.getNickname());
            player.setSatusPlayer(new NGRoomStatus(REGISTRATION, "Registrado"));
            nickVerified = serverManager.addPlayer(player);

            String mensaje_confirmar = mc_enviar.createNGMensajeConfirmar(nickVerified);
            System.out.println("*El jugador " + player.getNick() + " se ha registrado en el servidor");
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

            roomManager = serverManager.getSalasServidor().get(mensajeEntrarSalaRecibido.getNumSala());
            if (serverManager.enterRoom(player, roomManager, mensajeEntrarSalaRecibido.getNumSala()) != null) {

                System.out.println("*El jugador " + player.getNick() + " ha entrada a la sala " + roomManager.getRegistrationName() + " el numero ha adivinar es: " + NGRoomAdivinarNumero.getNumeroAleatorio());
                player.setSatusPlayer(new NGRoomStatus(IN_ROOM, "En Sala"));
                this.numSalaActual = mensajeEntrarSalaRecibido.getNumSala();
                sePuedeEntrar = true;
            } else {
                System.out.println("*El jugador " + player.getNick() + " no puede entrar a la sala ");

            }

            confirmarSala = mensajeConfirmarEnviar.createNGMensajeConfirmar(sePuedeEntrar);
            dos.write(confirmarSala.getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    // Method to process messages received when the player is in the room
    // TODO
    private void processRoomMessages() throws IOException {
        // First we send the rules and the initial status
        sendRules();
        // Now we check for incoming messages, status updates and new challenges
        boolean exit = false;
        NGChallenge challenge = roomManager.checkChallenge(player);
        System.out.println("Challenger de la sala: " + challenge.getChallenge());
        while (!exit) {
            processNewChallenge(challenge);

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
            String respuestaEnviar = mpEnviar.createNGMensajePregunta("Las reglas son: ", roomManager.getRules());
            dos.write(respuestaEnviar.getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void processAnswer(NGChallenge challenge) {
        // TODO si uno de los jugadores acierta, se informa a los demas.
        try {
            byte[] arrayBytes = new byte[MAXIMUM_TCP_SIZE];
            dis.read(arrayBytes);
            String datosRecibidos = new String(arrayBytes);
            NGMensajeRespuesta mrRecibido = new NGMensajeRespuesta();
            mrRecibido.processNGMensajeRespuesta(datosRecibidos);
            // "Logica del juego".
            NGRoomStatus ngRoomStatus = roomManager.answer(player, mrRecibido.getRespuesta(), challenge);
            if (ngRoomStatus.getStatusNumber() == NGRoomManager.GANADOR) {
                respuestaOk = true;
            }
            NGMensajePregunta mpEnviar = new NGMensajePregunta();
            String respuestaEnviar = mpEnviar.createNGMensajePregunta(player.getSatusPlayer().getStatus(), player.getNick());
            dos.write(respuestaEnviar.getBytes());

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
                serverManager.removePlayer(player);
                System.out.println("El usuario " + player.getNick() + " se ha desconectado del servidor");
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
            boolean salirSala = serverManager.leaveRoom(player, roomManager, numSalaActual);
            NGMensajeConfirmar mccEnviar = new NGMensajeConfirmar();
            String datosEnviar = mccEnviar.createNGMensajeConfirmar(salirSala);
            System.out.println("S envia: " + datosEnviar);
            dos.write(datosEnviar.getBytes());
            this.player.setSatusPlayer(new NGRoomStatus(OFF_ROOM, "Fuera de sala"));
            return salirSala;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }


    private void processNewChallenge(NGChallenge challenge) throws IOException {
        //We send the challenge to the client
        //TODO
        //Now we set the timeout
        Timer timer = new Timer();
        boolean timeout_triggered = false;
        timer.schedule(new TimerTask() {
                           @Override
                           public void run() {
                               System.out.println("Time's up!");
                               timer.cancel();
                           }
                       }, roomManager.getTimeout(),
                roomManager.getTimeout());
        boolean answerProvided = false;
        //Loop until an answer is provided or the timeout expires
        while (!timeout_triggered && !respuestaOk) {
            if (dis.available() > 0) {
                //The client sent a message
                //TODO Process the message
                //IF ANSWER Then call roomManager.answer() and proceed
                processAnswer(challenge);
            } else
                try {
                    //To avoid a CPU-consuming busy wait
                    Thread.sleep(50);
                } catch (InterruptedException e) {
//Ignore }
                }
            if (!respuestaOk) {
                //The timeout expired
                timer.cancel();
                //answerProvided = true;
                //TODO call roomManager.noAnswer() and proceed
                roomManager.noAnswer(player);
            }
        }
    }
}
