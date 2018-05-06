 package es.um.redes.nanoGames.server.roomManager;

public class NGChallenge {
	public int challengeNumber;

	//TODO Change the challenge to represent accurately your game challenge
	public String challenge;
	
	//Status initialization
	public NGChallenge() {
		challengeNumber = 0;
		challenge = null;
	}

	public NGChallenge(short currentChallengeNumber, String currentChallenge) {
		this.challengeNumber = currentChallengeNumber;
		challenge = currentChallenge;
	}


	public int getChallengeNumber() {
		return challengeNumber;
	}

	public void setChallengeNumber(int challengeNumber) {
		this.challengeNumber = challengeNumber;
	}

	public String getChallenge() {
		return challenge;
	}

	public void setChallenge(String challenge) {
		this.challenge = challenge;
	}

	@Override
	public String toString() {
		return "[pointsChallenge=" + challengeNumber + " challenge=" + challenge + "]\n";
	}
	
	
	

}
