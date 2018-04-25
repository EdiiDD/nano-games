package es.um.redes.nanoGames.server;

import java.util.HashMap;
import java.util.Map;

import es.um.redes.nanoGames.server.roomManager.NGRoomAdivinarNumero;
import es.um.redes.nanoGames.server.roomManager.NGRoomManager;

/**
 * This class contains the general status of the whole server (without the logic related to particular games)
 */
class NGServerManager {

    // Players registered in this server, Key-> String Value-> NGPlayerInfo
    private HashMap<String, NGPlayerInfo> players = new HashMap<String, NGPlayerInfo>();
    private Map<Integer, NGRoomManager> salasServidor = new HashMap<Integer, NGRoomManager>();
    // Current rooms and their related RoomManagers
    // TODO Data structure to relate rooms and RoomManagers

    NGServerManager() {
        // Dar de alta las salas que existirán por defecto
        salasServidor.put(1, new NGRoomAdivinarNumero());
    }

    public void registerRoomManager(NGRoomManager rm) {
        // When a new room manager is registered we assigned it to a room
        // TODO
    }

    // Returns the set of existing rooms
    // public synchronized getRoomList() {
    // TODO
    // }

    // Given a room it returns the description
    public synchronized String getRoomDescription(NGRoomManager ngrm) {
        // We make use of the RoomManager to obtain an updated description of the room
        // return ngrm.get(ngrm).getDescription();
        return ngrm.getDescription();
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
    public synchronized NGRoomManager enterRoom(NGPlayerInfo p, NGRoomManager ngrm) {
        // TODO Check if the room exists
        if (ngrm.registerPlayer(p)) {
            return ngrm;
        } else
            return null;
    }

    // A player leaves the room
    public synchronized void leaveRoom(NGPlayerInfo p, byte room) {
        // TODO Check if the room exists
        // room.removePlayer(p);
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
