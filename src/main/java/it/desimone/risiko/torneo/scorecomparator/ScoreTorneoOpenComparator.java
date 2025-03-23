package it.desimone.risiko.torneo.scorecomparator;

import it.desimone.risiko.torneo.scoreplayer.ScorePlayer;

import java.util.Comparator;

public class ScoreTorneoOpenComparator implements Comparator<ScorePlayer> {

	public int compare(ScorePlayer scorePlayer1, ScorePlayer scorePlayer2) {
		int result = 0;

		Float differenza = scorePlayer2.getPunteggio(false) - scorePlayer1.getPunteggio(false);
		
		if (differenza >  0f){
			result = 1;
		}else if (differenza < 0f){
			result = -1;
		}else{
			result = 0;
		}
		
		if (result == 0){
			result = scorePlayer2.getNumeroVittorie() - scorePlayer1.getNumeroVittorie();
		}
		if (result == 0){
			differenza = scorePlayer2.getPunteggio(false) - scorePlayer1.getPunteggio(false);
			if (differenza >  0f){
				result = 1;
			}else if (differenza < 0f){
				result = -1;
			}else{
				result = 0;
			}
		}
		if (result == 0){
			differenza = scorePlayer2.getPunteggioMassimo() - scorePlayer1.getPunteggioMassimo();
			if (differenza >  0f){
				result = 1;
			}else if (differenza < 0f){
				result = -1;
			}else{
				result = 0;
			}
		}
		
		return result;
	}

}
