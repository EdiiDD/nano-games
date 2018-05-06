package es.um.redes.nanoGames.server.roomManager;

public class NGRoomStatus {
    public short statusNumber;
    //TODO Change the status to represent accurately your game status
    /*
     * Sirve para codificar un cambio en el estado del juego, donde por estado se puede entender:
     * entrada o salida de jugadores, cambios en la puntuación, respuestas de otros jugadores,
     * consecuencia de un movimiento, finalización de una partida, etc.
     */
    public String status;

    //Status initialization
    public NGRoomStatus() {
        statusNumber = 0;
        status = null;
    }

    public NGRoomStatus(short currentStatus, String message) {
        statusNumber = currentStatus;
        this.status = message;
    }

    public short getStatusNumber() {
        return statusNumber;
    }

    public void setStatusNumber(short statusNumber) {
        this.statusNumber = statusNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
