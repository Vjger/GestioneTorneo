package it.desimone.risiko.torneo.scorecomparator;

import it.desimone.risiko.torneo.scoreplayer.ScoreTeam;

import java.util.Comparator;

public class ScoreCNSComparator implements Comparator<ScoreTeam> {
	
	public static final int RISULTATI_VALIDI_CNS = 9;

	public int compare(ScoreTeam scoreTeam1, ScoreTeam scoreTeam2) {
		int result = 0;

		/* Confronto tra punteggi complessivi */
		if (result == 0){
			result = scoreTeam2.getPunteggioB(RISULTATI_VALIDI_CNS).compareTo(scoreTeam1.getPunteggioB(RISULTATI_VALIDI_CNS));
		}
		
		if (result == 0){
			result = scoreTeam2.getNumeroVittorie() - scoreTeam2.getNumeroVittorie();
		}
		if (result == 0){
			for (int i = 1; i < RISULTATI_VALIDI_CNS; i++){
				result = scoreTeam2.getPunteggioB(i).compareTo(scoreTeam1.getPunteggioB(i));
				if (result != 0){break;}
			}
		}
		
		return result;
	}

}
