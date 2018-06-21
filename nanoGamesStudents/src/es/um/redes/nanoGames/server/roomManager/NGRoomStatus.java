package es.um.redes.nanoGames.server.roomManager;

public class NGRoomStatus {

    public static final byte SIN_TOKEN = 0;
    public static final byte EN_JUEGO = 1;
    public static final byte FIN_JUEGO = 2;
    public static final byte PEDIR_RANKING = 3;
    public static final byte EN_ESPERA = 4;
    public static final byte GANADOR = 5;
    public static final byte NO_GANADOR = 9;
    public static final byte NO_CONTESTA = 10;


    public short statusNumber;
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

    @Override
    public String toString() {
        return "NGRoomStatus{" +
                "statusNumber=" + statusNumber +
                ", status='" + status + '\'' +
                '}';
    }
}
