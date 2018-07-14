package es.um.redes.nanoGames.server;

import es.um.redes.nanoGames.client.application.NGController;
import es.um.redes.nanoGames.client.application.NanoGame;
import es.um.redes.nanoGames.server.roomManager.NGRoomStatus;

public class NGPlayerInfo {
    // TODO Include additional fields if required

    private String nick; // Nickname of the user

    private NGRoomStatus satusPlayer; // Current status of the user (according to the automata)

    private int score; // Current score of the user

    private int jugadasHechas;

    private boolean esTurno;

    public NGPlayerInfo(String nick, int score) {
        this.nick = nick;
        this.score = score;
        this.esTurno = false;
    }

    // Constructor to make copies
    public NGPlayerInfo(NGPlayerInfo p) {
        this.nick = new String(p.nick);
        this.score = p.score;
        this.satusPlayer = new NGRoomStatus(NGRoomStatus.SIN_TOKEN, "Token sin validar");
        this.jugadasHechas = 0;
        this.esTurno = p.esTurno;
    }


    public NGRoomStatus getSatusPlayer() {
        return satusPlayer;
    }

    public void setSatusPlayer(NGRoomStatus satusPlayer) {
        this.satusPlayer = satusPlayer;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public int getScore() {
        return score;
    }

    // Metodo para actulizar el score del jugador cuando sea necesario.
    public void actulizarScore(int score) {
        this.score += score;
    }

    public int getJugadasHechas() {
        return jugadasHechas;
    }

    public void jugadaHecha() {
        this.jugadasHechas++;
    }

    @Override
    public String toString() {
        return "NGPlayerInfo{" +
                "nick='" + nick + '\'' +
                ", satusPlayer=" + satusPlayer +
                ", score=" + score +
                ", jugadasHechas=" + jugadasHechas +
                ", esTurno=" + esTurno +
                '}';
    }

    public boolean isEsTurno() {
        return esTurno;
    }

    public void setEsTurno(boolean esTurno) {
        this.esTurno = esTurno;
    }

    public void setJugadasHechas(int jugadasHechas) {
        this.jugadasHechas = jugadasHechas;
    }
}
