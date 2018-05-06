package es.um.redes.nanoGames.server;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import es.um.redes.nanoGames.server.roomManager.NGRoomAdivinarNumero;
import es.um.redes.nanoGames.server.roomManager.NGRoomManager;

/**
 * This class contains the general status of the whole server (without the logic related to particular games)
 */
class NGServerManager {

    // Players registered in this server, Key-> String Value-> NGPlayerInfo
    private HashMap<String, NGPlayerInfo> players;
    private Map<Integer, NGRoomManager> salasServidor;
    // Current rooms and their related RoomManagers
    // TODO Data structure to relate rooms and RoomManagers

    NGServerManager() {
        // Dar de alta las salas que existirán por defecto
        players = new HashMap<>();
        // Sala disponibles en el servidor.
        salasServidor = new HashMap<>();
    }

    public void registerRoomManager(int numSala, NGRoomManager rm) {
        salasServidor.put(numSala, rm);
    }

    // Returns the set of existing rooms
    public synchronized List<NGRoomManager> getRoomList() {

        LinkedList<NGRoomManager> salasDisponibles = new LinkedList<>();
        for (Integer sala : salasServidor.keySet()) {
            salasDisponibles.add(salasServidor.get(sala));
        }
        return salasDisponibles;

    }

    // Given a room it returns the description
    public synchronized String getRoomDescription(NGRoomManager ngrm) {
        // We make use of the RoomManager to obtain an updated description of the room
        // return ngrm.get(ngrm).getDescription();
        return ngrm.toString();
    }

    // False is returned if the nickname is already registered, True otherwise and
    // the player is registered
    public synchronized boolean addPlayer(NGPlayerInfo player) {
        if (!this.players.containsKey(player.getNick())) {
            this.players.put(player.getNick(), player);
            return true;
        }
        return false;
    }

    // The player is removed from the list
    public synchronized void removePlayer(NGPlayerInfo player) {
        this.players.remove(player.getNick());
    }

    // A player request to enter in a room. If the access is granted the RoomManager
    // is returned
    public synchronized NGRoomManager enterRoom(NGPlayerInfo p, NGRoomManager ngrm, int numSala) {
        // Comprobamos si la sala existe y si se puede registrar en la sala.
        if (ngrm.registerPlayer(p) & salasServidor.containsKey(numSala)) {
            return ngrm;
        } else
            return null;
    }

    // A player leaves the room
    public synchronized boolean leaveRoom(NGPlayerInfo p, NGRoomManager ngrm, int numSala) {
        // TODO Check if the room exists
        if (salasServidor.containsKey(numSala)) {
            ngrm.removePlayer(p);
            return true;
        }
        return false;
    }

    public HashMap<String, NGPlayerInfo> getPlayers() {
        return players;
    }

    public void setPlayers(HashMap<String, NGPlayerInfo> players) {
        this.players = players;
    }

    public Map<Integer, NGRoomManager> getSalasServidor() {
        return salasServidor;
    }

    public void setSalasServidor(Map<Integer, NGRoomManager> salasServidor) {
        this.salasServidor = salasServidor;
    }
}
