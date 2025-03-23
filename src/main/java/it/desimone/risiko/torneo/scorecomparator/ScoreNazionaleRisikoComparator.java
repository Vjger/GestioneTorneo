package it.desimone.risiko.torneo.scorecomparator;

import it.desimone.risiko.torneo.dto.GiocatoreDTO;
import it.desimone.risiko.torneo.dto.Partita;
import it.desimone.risiko.torneo.scoreplayer.ScorePlayer;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;

public class ScoreNazionaleRisikoComparator implements Comparator<ScorePlayer> {

	public int compare(ScorePlayer scorePlayer1, ScorePlayer scorePlayer2) {
		int result = 0;

		result = scorePlayer2.getPunteggioB(false).compareTo(scorePlayer1.getPunteggioB(false));

		if (result == 0){ //va calcolata la classifica al turno precedente e così via
			Partita[] partiteGiocatore1 = scorePlayer1.getPartite();
			Partita[] partiteGiocatore2 = scorePlayer2.getPartite();
			int turnoMassimo = Math.max(partiteGiocatore1.length, partiteGiocatore2.length);
			for (int index = 1; index < turnoMassimo && result == 0; index++){
				Partita[] subPartiteGiocatore1 = null;
				Partita[] subPartiteGiocatore2 = null;
				if (partiteGiocatore1.length > index){
					subPartiteGiocatore1 = Arrays.copyOf(partiteGiocatore1, partiteGiocatore1.length - index);
				}
				if (partiteGiocatore2.length > index){
					subPartiteGiocatore2 = Arrays.copyOf(partiteGiocatore2, partiteGiocatore2.length - index);
				}
				result = compareClassificaAltroTurnoB(scorePlayer1.getGiocatore(), subPartiteGiocatore1, scorePlayer2.getGiocatore(), subPartiteGiocatore2);
			}	
		}
		
//		if (result == 0){ //va calcolata la classifica al turno precedente e così via
//			Partita[] partiteGiocatore1 = scorePlayer1.getPartite();
//			Partita[] partiteGiocatore2 = scorePlayer2.getPartite();
//			for (int index = partiteGiocatore1.length -1; index >=1 && result == 0; index--){
//				Partita[] subPartiteGiocatore1 = Arrays.copyOf(partiteGiocatore1, index);
//				Partita[] subPartiteGiocatore2 = Arrays.copyOf(partiteGiocatore2, index);
//				result = compareClassificaAltroTurnoB(scorePlayer1.getGiocatore(), subPartiteGiocatore1, scorePlayer2.getGiocatore(), subPartiteGiocatore2);
//			}	
//		}
		
//		if (result == 0){
//			Float differenza = scorePlayer2.getPunteggio(false) - scorePlayer1.getPunteggio(false);
//			if (differenza >  0f){
//				result = 1;
//			}else if (differenza < 0f){
//				result = -1;
//			}else{
//				result = 0;
//			}
//		}
		
		if (result == 0){ //va calcolata la classifica dal turno in canna e così via
			Partita[] partiteGiocatore1 = scorePlayer1.getPartite();
			Partita[] partiteGiocatore2 = scorePlayer2.getPartite();
			int turnoMassimo = Math.max(partiteGiocatore1.length, partiteGiocatore2.length);
			for (int index = 0; index < turnoMassimo && result == 0; index++){
				Partita[] subPartiteGiocatore1 = null;
				Partita[] subPartiteGiocatore2 = null;
				if (partiteGiocatore1.length > index){
					subPartiteGiocatore1 = Arrays.copyOf(partiteGiocatore1, partiteGiocatore1.length - index);
				}
				if (partiteGiocatore2.length > index){
					subPartiteGiocatore2 = Arrays.copyOf(partiteGiocatore2, partiteGiocatore2.length - index);
				}
				result = compareClassificaAltroTurno(scorePlayer1.getGiocatore(), subPartiteGiocatore1, scorePlayer2.getGiocatore(), subPartiteGiocatore2);
			}	
		}
		
//		if (result == 0){ //va calcolata la classifica dal turno in canna e così via
//			Partita[] partiteGiocatore1 = scorePlayer1.getPartite();
//			Partita[] partiteGiocatore2 = scorePlayer2.getPartite();
//			for (int index = partiteGiocatore1.length ; index >=1 && result == 0; index--){
//				Partita[] subPartiteGiocatore1 = Arrays.copyOf(partiteGiocatore1, index);
//				Partita[] subPartiteGiocatore2 = Arrays.copyOf(partiteGiocatore2, index);
//				result = compareClassificaAltroTurno(scorePlayer1.getGiocatore(), subPartiteGiocatore1, scorePlayer2.getGiocatore(), subPartiteGiocatore2);
//			}	
//		}
		
//		if (result == 0 && scorePlayer1.getPartite().length > 1){
//			throw new MyException("Deve essere risolta la parità tra "+scorePlayer1.getGiocatore()+" e "+scorePlayer2.getGiocatore());
//		}
		return result;
	}
	
	
	private int compareClassificaAltroTurnoB(GiocatoreDTO giocatore1, Partita[] partiteGiocatore1, GiocatoreDTO giocatore2, Partita[] partiteGiocatore2){
		int result = 0;
		BigDecimal punteggioGiocatore1 = BigDecimal.ZERO;
		BigDecimal punteggioGiocatore2 = BigDecimal.ZERO;
		if (partiteGiocatore1 != null){
			for (int index = 0; index < partiteGiocatore1.length; index++){
				if (partiteGiocatore1[index] != null){
					punteggioGiocatore1 = punteggioGiocatore1.add(partiteGiocatore1[index].getPunteggioTrascodificatoB(giocatore1));
				}
			}		
		}
		if (partiteGiocatore2 != null){
			for (int index = 0; index < partiteGiocatore2.length; index++){
				if (partiteGiocatore2[index] != null){
					punteggioGiocatore2 = punteggioGiocatore2.add(partiteGiocatore2[index].getPunteggioTrascodificatoB(giocatore2));
				}
			}
		}
		
		result = punteggioGiocatore2.compareTo(punteggioGiocatore1);
		return result;
	}

	
	
	private int compareClassificaAltroTurno(GiocatoreDTO giocatore1, Partita[] partiteGiocatore1, GiocatoreDTO giocatore2, Partita[] partiteGiocatore2){
		int result = 0;
		Double punteggioGiocatore1 = 0D;
		Double punteggioGiocatore2 = 0D;
		if (partiteGiocatore1 != null){
			for (int index = 0; index < partiteGiocatore1.length; index++){
				if (index > 0){
					if (partiteGiocatore1[index] != null){
						punteggioGiocatore1 += partiteGiocatore1[index].getPunteggioTrascodificato(giocatore1);
					}
				}else{//serve ad evitare rischi per eventuali decimali al primo turno per determinare l'ordine di arrivo (e che non hanno a che fare con la classifica)
					if (partiteGiocatore1[index] != null){
						punteggioGiocatore1 += Math.floor(partiteGiocatore1[index].getPunteggioTrascodificato(giocatore1));
					}
				}
			}
		}
		if (partiteGiocatore2 != null){
			for (int index = 0; index < partiteGiocatore2.length; index++){
				if (index > 0){
					if (partiteGiocatore2[index] != null){
						punteggioGiocatore2 += partiteGiocatore2[index].getPunteggioTrascodificato(giocatore2);
					}
				}else{//serve ad evitare rischi per eventuali decimali al primo turno per determinare l'ordine di arrivo (e che non hanno a che fare con la classifica)
					if (partiteGiocatore2[index] != null){
						punteggioGiocatore2 += Math.floor(partiteGiocatore2[index].getPunteggioTrascodificato(giocatore2));
					}
				}
			}
		}
		if (punteggioGiocatore2 >  punteggioGiocatore1){
			result = 1;
		}else if (punteggioGiocatore2 < punteggioGiocatore1){
			result = -1;
		}else{
			result = 0;
		}
		return result;
	}

}
