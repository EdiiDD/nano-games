package es.um.redes.nanoGames.server.roomManager;


import es.um.redes.nanoGames.server.NGPlayerInfo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NGRoomAdivinarNumero extends NGRoomManager {


    private static final int NUM_MAX_PLAYER = 1;
    private static final int NUM_MAX_TRY = 7;
    private static double numeroAleatorio;

    private final String NAME_ROOM = "ADVIVINZANZA NUMERO";
    private int numJugadores;
    private Map<Integer, NGChallenge> mapasChallenge;
    private List<NGPlayerInfo> jugadoresSala;
    private static NGRoomStatus estadoSala;
    private int numChallenge;

    public NGRoomAdivinarNumero() {
        // Reglas de la sala.
        rules = rulesRoom();
        // Registration Name.
        registrationName = NAME_ROOM;
        description = descriptionRoom();
        mapasChallenge = new HashMap<>();
        mapasChallenge = crearNGChallenge();
        jugadoresSala = new LinkedList<>();
        numeroAleatorio = (int) (Math.random() * 20);
        gameTimeout = 50000;
        estadoSala = new NGRoomStatus(NGRoomStatus.EN_ESPERA, "Sala en espera de jugadores");

    }

    @Override
    public boolean registerPlayer(NGPlayerInfo p) {
        // La sala aun no esta llena
        if (jugadoresSala.size() < NUM_MAX_PLAYER) {
            jugadoresSala.add(p);
            numJugadores = jugadoresSala.size();
            return true;
        }
        return false;
    }

    @Override
    public String getRules() {
        return rules;
    }

    @Override
    public NGRoomStatus checkStatus(NGPlayerInfo p) {
        // Si hay mas de un jugador, se puede realizar la partida
        System.out.println(numJugadores);
        System.out.println(NUM_MAX_PLAYER);
        if (numJugadores == NUM_MAX_PLAYER) {
            System.out.println("*Sala completa para jugar!!");
            setEstadoSala(new NGRoomStatus(NGRoomStatus.NO_GANADOR, "En Juego!!"));
            for (NGPlayerInfo player : jugadoresSala) {
                player.setSatusPlayer(new NGRoomStatus(NGRoomStatus.NO_GANADOR, "No es Ganador!!"));
            }
            return p.getSatusPlayer();
        }else {
            System.out.println("*Sala incompleta para jugar.");
            setEstadoSala(new NGRoomStatus(NGRoomStatus.EN_ESPERA, "En Espera de Jugadores!!"));
            p.setSatusPlayer(new NGRoomStatus(NGRoomStatus.EN_ESPERA, "En Espera de Jugadores!!"));
            return p.getSatusPlayer();
        }
    }

    @Override
    public NGRoomStatus getEstadoSala() {
        return estadoSala;
    }

    @Override
    public NGChallenge checkChallenge(NGPlayerInfo p) {
        // Devolvemos un challenge aleatorio.
        numChallenge= (int) (Math.random() * mapasChallenge.size()) + 1;
        return mapasChallenge.get(numChallenge);
    }

    @Override
    public NGRoomStatus noAnswer(NGPlayerInfo p) {
        // Borro al Jugador de la Sala.
        removePlayer(p);
        // Cambio su estado.
        p.setSatusPlayer(new NGRoomStatus(NGRoomStatus.NO_CONTESTA, "El Jugador no ha conestatado en el tiempo requerido"));
        return p.getSatusPlayer();
    }

    @Override
    public NGRoomStatus answer(NGPlayerInfo p, String answer, NGChallenge challenge) {
        // Tratamiento de la respuesta por parte del cliente
        int numAnswer = Integer.valueOf(answer);
        // El jugador no ha acertado el numero aleatorio
        if (numAnswer != numeroAleatorio && estadoSala.getStatusNumber() == NGRoomStatus.NO_GANADOR) {
            p.jugadaHecha();
            String calienteFrio = "";
            if (numAnswer - numeroAleatorio < 0) {
                calienteFrio = "Te has quedado por debajo, INTENTALO DE NUEVO!!";
            } else {
                calienteFrio = "Te has quedado por encima, INTENTALO DE NUEVO!!";
            }
            p.setSatusPlayer(new NGRoomStatus(NGRoomStatus.NO_GANADOR, calienteFrio));
            setEstadoSala(new NGRoomStatus(NGRoomStatus.NO_GANADOR, calienteFrio));
        } // El Jugador acierta el numero aleatorio.
        else if (numAnswer == numeroAleatorio && estadoSala.getStatusNumber() == NGRoomStatus.NO_GANADOR) {
            p.jugadaHecha();
            p.actulizarScore(puntiacionChallenge(p,challenge));
            p.setSatusPlayer(new NGRoomStatus(NGRoomStatus.GANADOR, "¡¡HAS GANADO FELICIDADES!! " + puntuacionSala()));
            System.out.println("*El jugador " + p.getNick() + " recibe " + puntiacionChallenge(p,challenge) +" pts");
            setEstadoSala(new NGRoomStatus(NGRoomStatus.GANADOR, "HAY UN GANADOR!!"));
        }
        // El numero aleatorio ya fue acertado
        else if (estadoSala.getStatusNumber() == NGRoomStatus.GANADOR) {
            p.jugadaHecha();
            p.actulizarScore(0);
            p.setSatusPlayer(new NGRoomStatus(NGRoomStatus.NO_GANADOR, "¡¡HAS PERDIDO SUERTE LA PROXIMA VEZ!! " + puntuacionSala()));
            setEstadoSala(new NGRoomStatus(NGRoomStatus.GANADOR, "HAY UN GANADOR!!"));
        }
        // No hay jugadores en la sala.
        else if (estadoSala.getStatusNumber() == NGRoomStatus.EN_ESPERA) {
            p.setSatusPlayer(new NGRoomStatus(NGRoomStatus.EN_ESPERA, "¡¡En espera de jugadores!!"));
            setEstadoSala(new NGRoomStatus(NGRoomStatus.EN_ESPERA, "¡¡En espera de jugadores!!"));
        }
        return p.getSatusPlayer();
    }

    @Override
    public void removePlayer(NGPlayerInfo p) {
        jugadoresSala.remove(p);
        if ( jugadoresSala.size() < NUM_MAX_PLAYER)
        p.setSatusPlayer(new NGRoomStatus(NGRoomStatus.FIN_JUEGO, "Salir del Juego!!"));
    }

    @Override
    public NGRoomManager duplicate() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRegistrationName() {
        return registrationName;
    }

    @Override
    public String getDescription() {
        return toString();
    }

    @Override
    public int playersInRoom() {
        return numJugadores;
    }

    public void setEstadoSala(NGRoomStatus estadoSala) {
        this.estadoSala = estadoSala;
    }


    private String rulesRoom() {
        return "El servidor ha pensado un numero aleatorio entre 1 y 20, y tienes que adivinarlo. El servidor te dirá si cada intento es muy alto o muy bajo.";
    }

    private String descriptionRoom() {
        return "Tú ganas si adivinas el número en " + NUM_MAX_TRY + " o menos intentos";
    }

    // Challenges de la sala de juego.
    private Map<Integer, NGChallenge> crearNGChallenge() {
        HashMap<Integer, NGChallenge> mapa = new HashMap<Integer, NGChallenge>();
        NGChallenge adivinarNumero = new NGChallenge(Short.valueOf("1"), "Adivinar el numero en mas de 4 intentos");
        NGChallenge adivinarNumero4Intento = new NGChallenge(Short.valueOf("2"), "Adivinar el numero al 4º intento");
        NGChallenge adivinarNumero3Intento = new NGChallenge(Short.valueOf("3"), "Adivinar el numero al 3º intento");
        NGChallenge adivinarNumero2Intento = new NGChallenge(Short.valueOf("4"), "Adivinar el numero al 2º intento");
        NGChallenge adivinarNumeroAlaPrimera = new NGChallenge(Short.valueOf("5"), "Adivinar el numero a la primera");

        mapa.put(1, adivinarNumeroAlaPrimera);
        mapa.put(2, adivinarNumero2Intento);
        mapa.put(3, adivinarNumero3Intento);
        mapa.put(4, adivinarNumero4Intento);
        mapa.put(5, adivinarNumero);
        mapa.put(6, adivinarNumero);
        mapa.put(7, adivinarNumero);

        return mapa;
    }

    public void printNGChallenge() {
        System.out.println("\nChallenges de NGRoomAdivinarNumero");
        for (Integer key : mapasChallenge.keySet()) {
            System.out.println(mapasChallenge.get(key).toString());
        }
    }

    @Override
    public String toString() {
        return "NGRoomAdivinarNumero \tPlayer:[ " + jugadoresSalaToString(jugadoresSala) + " ]";
    }

    public String jugadoresSalaToString(List<NGPlayerInfo> jugadoresSala) {
        String devolver = " ";
        for (NGPlayerInfo player : jugadoresSala) {
            devolver += "Nick: " + player.getNick() + " Score: " + player.getScore() + " | ";
        }
        return devolver;
    }

    public void imprimirTodo() {
        System.out.println(this.toString());
        printNGChallenge();

    }

    @Override
    public String puntuacionSala() {
        return "Puntuacion: " + jugadoresSalaToString(jugadoresSala);
    }

    public static double getNumeroAleatorio() {
        return numeroAleatorio;
    }

    public int puntiacionChallenge(NGPlayerInfo player,NGChallenge challenge) {
        System.out.println("Num Jugadas: " + player.getJugadasHechas());
        System.out.println("Puntos: " + challenge.getChallengeNumber());
        if (player.getJugadasHechas() == numChallenge)
            return challenge.getChallengeNumber();
        return 1;
    }
}
