package it.desimone.risiko.torneo.scorecomparator;

import it.desimone.risiko.torneo.scoreplayer.ScorePlayer;
import it.desimone.risiko.torneo.scoreplayer.ScorePlayerTorneoColoni;

import java.util.Comparator;

public class ScoreTorneoColoniComparator implements Comparator<ScorePlayer> {

	public int compare(ScorePlayer scorePlayer1, ScorePlayer scorePlayer2) {
		int result = 0;

		result = scorePlayer2.getNumeroVittorie() - scorePlayer1.getNumeroVittorie();
		
		if (result == 0){
			Float differenza = scorePlayer2.getPunteggio(true) - scorePlayer1.getPunteggio(true);	
			result = compareDifferenza(differenza);
		}
		if (result == 0){
			Float perc1 = ((ScorePlayerTorneoColoni) scorePlayer1).getPercentualePuntiVittoria();
			Float perc2 = ((ScorePlayerTorneoColoni) scorePlayer2).getPercentualePuntiVittoria();
			Float differenza = perc2 - perc1;
			result = compareDifferenza(differenza);
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
