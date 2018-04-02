package es.um.redes.nanoGames.server;

import javax.persistence.*;

@Entity
@Table(name = "PLAYERINFO")
public class NGPlayerInfo {
	// TODO Include additional fields if required
	
	@Id
	@Column(name = "Nick")
	private String nick; // Nickname of the user
	
	@Column(name = "Status")
	private byte status; // Current status of the user (according to the automata)
	
	@Column(name = "Score")
	private int score; // Current score of the user

	// Contructor de la clase
	public NGPlayerInfo(String nick, int score) {
		this.nick = nick;
		this.score = score;
	}
	
	// Constructor to make copies
	public NGPlayerInfo(NGPlayerInfo p) {
		this.nick = new String(p.nick);
		this.status = p.status;
		this.score = p.score;
	}

	// Default constructor, usado para Hibernate
	public NGPlayerInfo() {

	}
		
	public void setStatus(byte status) {
		this.status = status;
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

	public byte getStatus() {
		return status;
	}
	

}
