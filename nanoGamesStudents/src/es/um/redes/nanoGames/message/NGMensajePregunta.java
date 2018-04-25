package es.um.redes.nanoGames.message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NGMensajePregunta {
    /*Mensaje Utilizado unica y exclusivamente por el servidor tras que el usuario haya dicho que quiere jugar */
    private String constOperacion = "PREGUNTA";
    private String format = "(<mensaje><operacion>)" + "(" + constOperacion + ")" + "(</operacion><info>)" + "(.*?)" + "(</info><parametro>)" + "(.*?)" + "(</parametro></mensaje>)";
    private String data;
    private String operation;
    private String info;
    private String mensajeJugador;
    private Pattern pattern = Pattern.compile(format);
    private boolean esFin = false;

    public String createNGMensajePregunta(String info, String mensajeJugador) {
        return "<mensaje><operacion>" + this.constOperacion + "</operacion><info>" + info + "</info><parametro>" + mensajeJugador + "</parametro></mensaje>";
    }


    public void processNGMensajePregunta(String data) {
        this.data = data;
        Matcher mat = pattern.matcher(this.data);
        if (mat.find()) {
            String s1 = mat.group(2);
            this.operation = new String(s1);
            s1 = mat.group(4);
            this.info = s1;
            s1 = mat.group(6);
            this.mensajeJugador = s1;
        }
        //Aqui solo se metera si recibe el mensaje de confirmar ni si quiera hay que tratarlo porque en el flujo de mensajes
        //el unico mensaje especial que puede entrar es el de confirmar+true
        else {
            this.esFin = true;
        }

    }

    public String getOperacion() {
        return this.operation;
    }

    public String getInfo() {
        return this.info;
    }

    public String getMensajeJugador() {
        return this.mensajeJugador;
    }

    public boolean getEsFin() {
        return this.esFin;
    }

    public static void main(String[] args) {
        //El jugador solicita jugar y se queda esperando la confirmacion
        //El servidor espera a que hayan al menos el n� min de jugadores
        //Cuando los hay, les envia un confirmar y deben de jugar
        //El primer jugador responde con un umero cualquiera (supongamos que no acierta y se queda corto)
        //entonces el servidor dice:
        NGMensajePregunta mp_enviado = new NGMensajePregunta();
        String pregunta = mp_enviado.createNGMensajePregunta("El objetivo del juego es adivinar un numero en el menor tiempo posible", "Te has quedao corto");
        //ahora lo envia por el dataoutput e interviene el jugador
        NGMensajePregunta mp_recibido = new NGMensajePregunta();
        mp_recibido.processNGMensajePregunta(pregunta);
        //Como ha fallado debe de sacar por pantalla sus datos (pa quel jugador lo vea):
        System.out.println(mp_recibido.getInfo() + "\n\t" + mp_recibido.getMensajeJugador());


        //Supongamos ahora que el Jugador acierta en la respuesta, �como reacciona el servidor y el jugador despues?
        //Identico salvo que el servidor responde con un confirmar y no con una pregunta
        NGMensajeConfirmar c_enviado = new NGMensajeConfirmar();
        String confirmacion = c_enviado.createNGMensajeConfirmar(true);
        //ahora lo envia pero el jugador NO sabe que es un confirmar
        NGMensajePregunta mp_recibido_2 = new NGMensajePregunta();
        mp_recibido_2.processNGMensajePregunta(confirmacion);
        if (mp_recibido_2.esFin) System.out.println("�He acertao, yupi!");

    }
}
