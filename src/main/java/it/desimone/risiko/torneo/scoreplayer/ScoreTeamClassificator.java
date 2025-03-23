package it.desimone.risiko.torneo.scoreplayer;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ScoreTeamClassificator {

	public static List<ScoreTeam> scoreTeamSorter(List<ScoreTeam> scores, Comparator comparator){
		if (scores == null || scores.isEmpty()){
			return scores;
		}
		Collections.sort(scores, comparator);
		scorePlayerPositioner(scores, comparator);
		return scores;
	}
	
	
	public static List<ScoreTeam> scorePlayerPositioner(List<ScoreTeam> scores, Comparator comparator){
		if (scores == null || scores.isEmpty()){
			return scores;
		}
		ScoreTeam first = scores.get(0);
		first.setPosition(1);
		for (int index = 1; index < scores.size(); index++){
			ScoreTeam scoreTeam = scores.get(index);
			ScoreTeam scorePlayerBefore = scores.get(index-1);
			if (scoreTeam.getPosition() == 0){ //Non ancora assegnata
				int comparation = comparator.compare(scorePlayerBefore, scoreTeam);
				if (comparation == 0){
					scoreTeam.setPosition(scorePlayerBefore.getPosition());
				}else{
					scoreTeam.setPosition(index+1);
				}
			}
		}
		return scores;
	}
}
