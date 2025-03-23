package it.desimone.risiko.torneo.scorecomparator;

import it.desimone.risiko.torneo.dto.Partita;
import it.desimone.risiko.torneo.scoreplayer.ScorePlayer;

import java.util.Comparator;

public class ScoreSemifinalistiRadunoComparator implements Comparator<ScorePlayer> {

	/**
	 * Dò priorità a chi ha disputato meno partite (cioè non ha disputato i quarti di finale) ed a parità priorità al punteggio complessivo
	 */
	
	public int compare(ScorePlayer scorePlayer1, ScorePlayer scorePlayer2) {
		int result = 0;
		
		Short partiteDisputate1 = partiteDisputate(scorePlayer1);
		Short partiteDisputate2 = partiteDisputate(scorePlayer2);
		
		if (partiteDisputate1 > partiteDisputate2){
			result = 1;
		}else if (partiteDisputate2 > partiteDisputate1){
			result = -1;
		}
		
		if (result == 0){
			Float differenza = scorePlayer2.getPunteggio(false) - scorePlayer1.getPunteggio(false);
			
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
	
	
	private Short partiteDisputate(ScorePlayer scorePlayer){
		Short result = 0;
		Partita[] partite = scorePlayer.getPartite();
		if (partite != null){
			for (Partita partita: partite){
				if (partita != null){
					result++;
				}
			}
		}
		
		return result;
	}

}
