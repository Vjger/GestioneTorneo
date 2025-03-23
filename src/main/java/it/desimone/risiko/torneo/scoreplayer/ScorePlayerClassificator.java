package it.desimone.risiko.torneo.scoreplayer;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ScorePlayerClassificator {

	public static List<ScorePlayer> scorePlayerSorter(List<ScorePlayer> scores, Comparator comparator){
		if (scores == null || scores.isEmpty()){
			return scores;
		}
		Collections.sort(scores, comparator);
		scorePlayerPositioner(scores, comparator);
		return scores;
	}
	
	
	public static List<ScorePlayer> scorePlayerPositioner(List<ScorePlayer> scores, Comparator comparator){
		if (scores == null || scores.isEmpty()){
			return scores;
		}
		ScorePlayer first = scores.get(0);
		first.setPosition(1);
		for (int index = 1; index < scores.size(); index++){
			ScorePlayer scorePlayer = scores.get(index);
			ScorePlayer scorePlayerBefore = scores.get(index-1);
			if (scorePlayer.getPosition() == 0){ //Non ancora assegnata
				int comparation = comparator.compare(scorePlayerBefore, scorePlayer);
				if (comparation == 0){
					scorePlayer.setPosition(scorePlayerBefore.getPosition());
				}else{
					scorePlayer.setPosition(index+1);
				}
			}
		}
		return scores;
	}
}
