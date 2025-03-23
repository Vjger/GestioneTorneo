package it.desimone.risiko.torneo.scorecomparator;

import it.desimone.risiko.torneo.scoreplayer.ScorePlayer;
import it.desimone.risiko.torneo.scoreplayer.ScorePlayerTorneoDominion;

import java.util.Comparator;

public class ScoreTorneoDominionComparator implements Comparator<ScorePlayer> {

	public int compare(ScorePlayer scorePlayer1, ScorePlayer scorePlayer2) {
		int result = 0;

		ScorePlayerTorneoDominion scp1 = (ScorePlayerTorneoDominion) scorePlayer1;
		ScorePlayerTorneoDominion scp2 = (ScorePlayerTorneoDominion) scorePlayer2;
		
		result = scp2.getNumeroVittorie() - scp1.getNumeroVittorie();
		
		if (result == 0){
			result = scp2.getNumeroSecondiPosti() - scp1.getNumeroSecondiPosti();
		}
		
		if (result == 0){
			result = scp2.getNumeroTerziPosti() - scp1.getNumeroTerziPosti();
		}
		
		if (result == 0){
			Float differenza = scorePlayer2.getPunteggio(true) - scorePlayer1.getPunteggio(true);	
			result = compareDifferenza(differenza);
		}
		if (result == 0){
			result = scp2.getNumeroOri() - scp1.getNumeroOri();
		}
		if (result == 0){
			result = scp2.getNumeroMonete() - scp1.getNumeroMonete();
		}
		if (result == 0){
			result = scp2.getNumeroAzioni() - scp1.getNumeroAzioni();
		}
		
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
