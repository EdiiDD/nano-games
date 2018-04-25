package es.um.redes.nanoGames.server;

import es.um.redes.nanoGames.client.application.NanoGame;

public class NGPlayerInfo {
    // TODO Include additional fields if required

    private int id;

    private String nick; // Nickname of the user

    private byte status; // Current status of the user (according to the automata)

    private int score; // Current score of the user

    // Contructor de la clase
    public NGPlayerInfo(int id, String nick, int score) {
        this.id = id;
        this.nick = nick;
        this.score = score;
    }

    public NGPlayerInfo(String nick, int score) {
        this.nick = nick;
        this.score = score;
    }

    // Constructor to make copies
    public NGPlayerInfo(NGPlayerInfo p) {
        this.id = p.id;
        this.nick = new String(p.nick);
        this.status = p.status;
        this.score = p.score;
    }

    // Default constructor, usado para Hibernate
    public NGPlayerInfo() {

    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public byte getStatus() {
        return status;
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

    @Override
    public String toString() {
        return "NGPlayerInfo [nick=" + nick + ", status=" + status + ", score=" + score + "]";
    }


}
