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
    private DataInputStream dis1;
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
    private NGRoomStatus estadoJugador;
    // Mensaje que le lleva del cliente
    private String mensajeCliente;
    // Operacion del mensaje del cliente
    private String opMensajeCliente;
    // Partida en Juego.
    private boolean enJuego;


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
        this.estadoJugador = new NGRoomStatus(NGRoomStatus.SIN_TOKEN, "Sin token");
        this.enJuego = true;

    }

    // Main loop
    public void run() {
        try {
            // We obtain the streams from the socket
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            // The first step is to receive and to verify the token
            // The second step is to receive and to verify the nick name
            while (enJuego) {
                //System.out.println("Estado 1: " + currentPlayer.getNick() + ": " + this.estadoJugador.toString());
                while ((this.estadoJugador.getStatusNumber() != NGRoomStatus.EN_JUEGO || this.currentPlayer.getSatusPlayer().statusNumber == NGServerThread.OFF_ROOM) && enJuego) {
                    //System.out.println("Estado 2: " + currentPlayer.getNick() + ": " + this.estadoJugador.toString());
                    // Si un usuario se sale de la sala.
                    if (this.currentPlayer.getSatusPlayer().statusNumber == NGRoomStatus.NO_CONTESTA)
                        this.currentPlayer.setSatusPlayer(new NGRoomStatus(NGRoomStatus.PEDIR_SALA, " "));

                    byte[] arrayBytes = new byte[MAXIMUM_TCP_SIZE];
                    dis.read(arrayBytes);
                    this.mensajeCliente = new String(arrayBytes);
                    this.opMensajeCliente = getOperacionMensaje(this.mensajeCliente);
                    System.out.println("Mensaje: " + this.opMensajeCliente);
                    if (this.opMensajeCliente.equalsIgnoreCase("FIN_JUEGO")) {
                        enJuego = false;
                    } else {
                        System.out.println("1");
                        if (this.estadoJugador.statusNumber == NGRoomStatus.SIN_TOKEN && this.opMensajeCliente.equalsIgnoreCase("ENVIAR_TOKEN")) {
                            receiveAndVerifyToken();
                            break;
                        } else if (this.estadoJugador.getStatusNumber() == NGRoomStatus.SIN_NOMBRE && this.opMensajeCliente.equalsIgnoreCase("ENVIAR_NICKNAME")) {
                            receiveAndVerifyNickname();
                            break;
                        } else if ((this.estadoJugador.getStatusNumber() == NGRoomStatus.PEDIR_SALA || this.estadoJugador.getStatusNumber() == NGRoomStatus.EN_ESPERA) && this.opMensajeCliente.equalsIgnoreCase("LISTAR_SALAS")) {
                            sendRoomList();
                            break;
                        } else if (this.estadoJugador.getStatusNumber() == NGRoomStatus.EN_ESPERA && this.opMensajeCliente.equalsIgnoreCase("ENTRAR_SALA")) {
                            enterTheRoom();
                            break;
                        } else if (!this.opMensajeCliente.equalsIgnoreCase("FIN_JUEGO")) {
                            errorEntrada(this.opMensajeCliente);
                        }
                    }
                }
                while (this.estadoJugador.getStatusNumber() == NGRoomStatus.EN_JUEGO && this.currentPlayer.getSatusPlayer().statusNumber != NGServerThread.OFF_ROOM) {
                    processRoomMessages();
                }
            }
            // Salimos del Juego
            closeConection();
        } catch (Exception e) {
            // If an error occurs with the communications the user is removed from all the
            // managers and the connection is closed
            // TODO
        }
        // TODO Close the socket
        try {
            dis.close();
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            //byte[] arrayBytes = new byte[MAXIMUM_TCP_SIZE];
            //dis.read(arrayBytes);
            //String token_recibido = new String(arrayBytes);
            // Creamos el mensaje, NGMensajeEnviarToken.
            NGMensajeEnviarToken met_recibido = new NGMensajeEnviarToken();
            // Procesamos el mensaje anteriormente creado("guardamos el token a enviar").
            met_recibido.processNGMensajeEnviarToken(this.mensajeCliente);
            // Creamos el mensaje de confirmacion
            NGMensajeConfirmar mc_enviar = new NGMensajeConfirmar();
            // Verificamoso el token.
            if (tokenPropio - met_recibido.getToken() <= TOKEN_THRESHOLD)
                tokenVerified = true;
            // Creacion del mensaje de respuesta, dicha confirmacion depende de la
            // verificacion del token.
            String mensaje_confirmar = mc_enviar.createNGMensajeConfirmar(tokenVerified);
            this.currentPlayer.setSatusPlayer(new NGRoomStatus(PRE_REGISTRATION, "Pre Registration"));
            this.estadoJugador = new NGRoomStatus(NGRoomStatus.SIN_NOMBRE, " Jugador sin nombre");
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
            NGMensajeEnviarNickname men_recibido = new NGMensajeEnviarNickname();
            men_recibido.processNGMensajeEnviarNickname(this.mensajeCliente);
            NGMensajeConfirmar mc_enviar = new NGMensajeConfirmar();

            currentPlayer.setNick(men_recibido.getNickname());
            currentPlayer.setSatusPlayer(new NGRoomStatus(REGISTRATION, "Registrado"));
            nickVerified = serverManager.addPlayer(currentPlayer);

            String mensaje_confirmar = mc_enviar.createNGMensajeConfirmar(nickVerified);
            System.out.println("*El jugador " + currentPlayer.getNick() + " se ha registrado en el servidor");
            if (this.estadoJugador.getStatusNumber() == NGRoomStatus.SIN_NOMBRE)
                this.estadoJugador = new NGRoomStatus(NGRoomStatus.PEDIR_SALA, " Jugador debe pedir la sala");
            dos.write(mensaje_confirmar.getBytes());

        }
    }

    // We send to the client the room list
    // TODO
    private void sendRoomList() throws IOException {

        NGMensajeListarSalas mls_recibido = new NGMensajeListarSalas();
        mls_recibido.processNGMensajeListarSalas(this.mensajeCliente);

        int numSalas = serverManager.getSalasServidor().size();
        String descripcionSalas = "";

        for (Integer rm : serverManager.getSalasServidor().keySet()) {

            descripcionSalas += serverManager.getRoomDescription(serverManager.getSalasServidor().get(rm));
            descripcionSalas += " & ";
        }
        NGMensajeListaSalas mls_enviar = new NGMensajeListaSalas();
        String listaSalas = mls_enviar.createNGMensajeListaSalas(numSalas, descripcionSalas);
        if (this.estadoJugador.getStatusNumber() == NGRoomStatus.PEDIR_SALA)
            this.estadoJugador = new NGRoomStatus(NGRoomStatus.EN_ESPERA, " Jugador en espera de entrar en una sala");
        dos.write(listaSalas.getBytes());

    }

    private void enterTheRoom() {
        NGMensajeConfirmar mensajeConfirmarEnviar = new NGMensajeConfirmar();
        String confirmarSala;
        boolean sePuedeEntrar = false;
        try {

            NGMensajeEntrarSala mensajeEntrarSalaRecibido = new NGMensajeEntrarSala();
            mensajeEntrarSalaRecibido.processNGMensajeEntrarSala(this.mensajeCliente);

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
                currentPlayer.setJugadasHechas(0);
                if (this.estadoJugador.getStatusNumber() == NGRoomStatus.EN_ESPERA)
                    this.estadoJugador = new NGRoomStatus(NGRoomStatus.EN_JUEGO, " Jugador dentro de una sala!!");
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
    private void processRoomMessages() throws IOException {
        // First we send the rules and the initial status
        this.estadoJugador = currentRoomManager.checkStatus(currentPlayer);
        //sendRules();
        // Now we check for incoming messages, status updates and new challenges
        boolean exit = false;
        NGChallenge challenge = currentRoomManager.checkChallenge(currentPlayer);
        System.out.println("*El jugador " + currentPlayer.getNick() + " tiene el reto " + challenge.toString());
        while (!exit) {
            // Si no se recibe ninguna respuesta, es decir, se ha perddo la conexión con el cliente.
            exit = proceessNewChallenge(challenge);

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

    private boolean proceessNewChallenge(NGChallenge challenge) throws IOException {
        //We send the challenge to the client
        //TODO
        //Now we set the timeout
        Timer timer = null;
        timeout_triggered.set(false);
        timer = new Timer();
        timer.schedule(new Timeout(), currentRoomManager.getTimeout(), currentRoomManager.getTimeout());

        boolean answerProvided = false;
        boolean salirSala = false;
        boolean noContestado = false;
        //Loop until an answer is provided or the timeout expires
        while (!answerProvided && !salirSala && !noContestado) {

            while (!timeout_triggered.get() && !salirSala) {
                byte[] arrayBytes = new byte[MAXIMUM_TCP_SIZE];
                dis.read(arrayBytes);
                this.mensajeCliente = new String(arrayBytes);
                this.opMensajeCliente = getOperacionMensaje(this.mensajeCliente);
                if (this.mensajeCliente.length() > 0) {
                    System.out.println("Entra 1");
                    if (getOperacionMensaje(this.mensajeCliente).equalsIgnoreCase("RESPUESTA")) {
                        processAnswer(challenge);
                    } else if (getOperacionMensaje(this.mensajeCliente).equalsIgnoreCase("SALIR_SALA")) {
                        exitRoomGame();
                        salirSala = true;
                        answerProvided = true;

                    }

                } else
                    try {
                        //To avoid a CPU-consuming busy wait
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        //Ignore
                    }
            }

            if (!answerProvided) {
                System.out.println("Entra 2");
                timer.cancel();
                //TODO call currentRoomManager.noAnswer() and proceed
                System.out.println("*El player " + currentPlayer.getNick() + " no ha respondido pasado un tiempo!!");
                currentRoomManager.noAnswer(currentPlayer);
                answerProvided = true;

            }
        }
        return true;
    }

    private void sendRules() {
        try {
            NGMensajeRespuesta mrRecibido = new NGMensajeRespuesta();
            mrRecibido.processNGMensajeRespuesta(this.mensajeCliente);

            // "Logica del juego".
            NGMensajePregunta mpEnviar = new NGMensajePregunta();
            String respuestaEnviar = mpEnviar.createNGMensajePregunta("Las reglas son: ", currentRoomManager.getRules());
            dos.write(respuestaEnviar.getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void processAnswer(NGChallenge challenge) {
        try {
            NGMensajeRespuesta mrRecibido = new NGMensajeRespuesta();
            NGMensajePregunta mpEnviar = new NGMensajePregunta();
            mrRecibido.processNGMensajeRespuesta(this.mensajeCliente);

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


            } else if (numSalaActual == 1) {
                currentRoomManager.answer(currentPlayer, getRespuestaMensaje(this.mensajeCliente), challenge);
                String respuestaEnviar = mpEnviar.createNGMensajePregunta(currentPlayer.getSatusPlayer().getStatus(), currentPlayer.getNick());
                System.out.println("El jugador " + this.currentPlayer.getNick() + " responde: " + getRespuestaMensaje(this.mensajeCliente) + " y es su " + this.currentPlayer.getJugadasHechas() + " jugada");
                dos.write(respuestaEnviar.getBytes());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void closeConection() {
        System.out.println("Salir");
        try {
            String s = new String(this.mensajeCliente);
            System.out.println("Recibimos S1: " + s);
            NGMensajeFinJuego mfj_recibido = new NGMensajeFinJuego();
            mfj_recibido.processNGMensajeFinJuego(s);

            System.out.println("Recibimos S2: " + mfj_recibido.getOperacion());
            // Eliminamos el jugador de nuestra "base de datos".
            serverManager.removePlayer(currentPlayer);
            System.out.println("El usuario " + currentPlayer.getNick() + " se ha desconectado del servidor");

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
        try {
            NGMensajeSalirSala mssRecibido = new NGMensajeSalirSala();
            mssRecibido.processNGMensajeSalirSala(this.mensajeCliente);
            boolean salirSala = serverManager.leaveRoom(currentPlayer, currentRoomManager, numSalaActual);
            NGMensajeConfirmar mccEnviar = new NGMensajeConfirmar();
            String datosEnviar = mccEnviar.createNGMensajeConfirmar(salirSala);
            dos.write(datosEnviar.getBytes());
            this.currentPlayer.setSatusPlayer(new NGRoomStatus(OFF_ROOM, "Fuera de sala"));
            this.estadoJugador = new NGRoomStatus(NGRoomStatus.PEDIR_SALA, " Jugador debe pedir la sala");
            return salirSala;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private String getOperacionMensaje(String mensajeCliente) {
        String[] parts = mensajeCliente.split("</operacion>");
        String[] parts1 = parts[0].split("<operacion>");
        return parts1[1];
    }

    private String getRespuestaMensaje(String mensajeCliente) {
        String[] parts = mensajeCliente.split("</parametro>");
        String[] parts1 = parts[0].split("<parametro>");
        return parts1[1];
    }

    private String getParametroMensaje(String mensajeCliente) {
        String[] parts = mensajeCliente.split("</parametro>");
        String[] parts1 = parts[0].split("<parametro>");
        return parts1[1];
    }


    private void errorEntrada(String operacionCliente) throws IOException {
        System.out.println("Error con la operacion: " + operacionCliente);
        switch (operacionCliente) {
            case "LISTAR_SALAS":
                NGMensajeListaSalas mls_enviar = new NGMensajeListaSalas();
                String listaSalas = mls_enviar.createNGMensajeListaSalas(-1, "");
                dos.write(listaSalas.getBytes());
                break;
            case "ENTRAR_SALA":
                NGMensajeConfirmar errorEntrarSala = new NGMensajeConfirmar();
                String entrarSala = errorEntrarSala.createNGMensajeConfirmar(false);
                dos.write(entrarSala.getBytes());
                break;

        }
    }
}
