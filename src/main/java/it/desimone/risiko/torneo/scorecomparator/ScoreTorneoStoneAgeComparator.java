package it.desimone.risiko.torneo.scorecomparator;

import it.desimone.risiko.torneo.scoreplayer.ScorePlayer;

import java.util.Comparator;

public class ScoreTorneoStoneAgeComparator implements Comparator<ScorePlayer> {

	public int compare(ScorePlayer scorePlayer1, ScorePlayer scorePlayer2) {
		Float differenza = scorePlayer2.getPunteggio(false) - scorePlayer1.getPunteggio(false);	
		int result = compareDifferenza(differenza);
		
		return result;
	}
	
	private int compareDifferenza(Float differenza){
		int result = 0;
		if (differenza >  0f){
			result = 1;
		}else if (differenza < 0f){
			result = -1;
		}else{
			result = 0;
		}
		return result;
	}

}
