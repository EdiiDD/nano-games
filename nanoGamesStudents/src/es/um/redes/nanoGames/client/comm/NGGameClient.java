package es.um.redes.nanoGames.client.comm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import es.um.redes.nanoGames.message.*;

import es.um.redes.nanoGames.broker.BrokerClient;

//This class provides the functionality required to exchange messages between the client and the game server 
public class NGGameClient {
    private Socket socket;
    protected DataOutputStream dos;
    protected DataInputStream dis;

    private static final int SERVER_PORT = 6969;
    public static final int MAXIMUM_TCP_SIZE = 65535;
    private int numSalaActual;

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
        int numSalas = 0;
        try {
            NGMensajeListarSalas mls_enviar = new NGMensajeListarSalas();
            String data_to_send = mls_enviar.createNGMensajeListarSalas();
            dos.write(data_to_send.getBytes());

            byte[] arrayBytes = new byte[MAXIMUM_TCP_SIZE];
            dis.read(arrayBytes);
            String data_recived = new String(arrayBytes);
            mls_recived = new NGMensajeListaSalas();
            mls_recived.processNGMensajeListaSalas(data_recived);

            numSalas = mls_recived.getNumSalas();
            System.out.println("Salas disponibles:");
            for (int i = 0; i < numSalas; i++) {
                System.out.println((i + 1) + " " + mls_recived.getSala(i));
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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

    public boolean enterTheRoom(int numSala) {


        try {
            NGMensajeEntrarSala mesEnviar = new NGMensajeEntrarSala();
            String datosEnviar = mesEnviar.createNGMensajeEntrarSala(numSala);
            dos.write(datosEnviar.getBytes());

            byte[] arrayBytes = new byte[MAXIMUM_TCP_SIZE];
            dis.read(arrayBytes);
            String datosRecibidos = new String(arrayBytes);

            NGMensajeConfirmar mensajeConfirmarRecibido = new NGMensajeConfirmar();
            mensajeConfirmarRecibido.processNGMensajeConfirmar(datosRecibidos);

            if (mensajeConfirmarRecibido.isConfirmated()) {
                System.out.println("Has entrado a la sala " + numSala);
                numSalaActual = numSala;
            } else {
                System.out.println("No se puede entrar a la sala, prueba dentro de unos minutos.");
            }
            return mensajeConfirmarRecibido.isConfirmated();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }


    public void sendAnswer(String answer) {
        NGMensajeRespuesta mrEnviar = new NGMensajeRespuesta();
        NGMensajePregunta mpRecibido = new NGMensajePregunta();
        try {

            String datosEnviar = mrEnviar.createNGMensajeRespuesta(answer);
            dos.write(datosEnviar.getBytes());

            byte[] arrayBytes = new byte[MAXIMUM_TCP_SIZE];
            dis.read(arrayBytes);
            String datosRecibidos = new String(arrayBytes);
            mpRecibido.processNGMensajePregunta(datosRecibidos);
            System.out.println(mpRecibido.getInfo());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // TODO se podría hacer mas generico. sendInfo()
    public void sendRules() {
        NGMensajeRespuesta mrEnviar = new NGMensajeRespuesta();
        NGMensajePregunta mpRecibido = new NGMensajePregunta();
        try {

            // Enviamos una respuesta vacia, pero queremos las reglas de la sala.
            String datosEnviar = mrEnviar.createNGMensajeRespuesta("Reglas");
            dos.write(datosEnviar.getBytes());

            byte[] arrayBytes = new byte[MAXIMUM_TCP_SIZE];
            dis.read(arrayBytes);
            String datosRecibidos = new String(arrayBytes);
            mpRecibido.processNGMensajePregunta(datosRecibidos);
            System.out.println(mpRecibido.getInfo() + mpRecibido.getMensajeJugador());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void exitRoomGame() {

        try {
            NGMensajeSalirSala mssEnviar = new NGMensajeSalirSala();
            String datosEnvia = mssEnviar.createNGMensajeSalirSala();
            dos.write(datosEnvia.getBytes());

            byte[] arraybytes = new byte[MAXIMUM_TCP_SIZE];
            dis.read(arraybytes);
            String datosRecibidos = new String(arraybytes);
            NGMensajeConfirmar mcRecibido = new NGMensajeConfirmar();
            mcRecibido.processNGMensajeConfirmar(datosRecibidos);
            if (mcRecibido.isConfirmated()) {
                System.out.println("Has salido de la sala");
            } else System.out.println("No has poodido salir de la sala");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void processGameMessage() {
        try {
            NGMensajePregunta mpRecibido = new NGMensajePregunta();
            byte[] arrayBytes = new byte[NGGameClient.MAXIMUM_TCP_SIZE];
            dis.read(arrayBytes);
            String respuestaRecibida = new String(arrayBytes);
            mpRecibido.processNGMensajePregunta(respuestaRecibida);
            System.out.println(mpRecibido.getInfo());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DataOutputStream getDos() {
        return dos;
    }

    public DataInputStream getDis() {
        return dis;
    }
}
