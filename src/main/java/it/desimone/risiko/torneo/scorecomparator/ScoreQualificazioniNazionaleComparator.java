package it.desimone.risiko.torneo.scorecomparator;

import it.desimone.risiko.torneo.dto.GiocatoreDTO;
import it.desimone.risiko.torneo.dto.Partita;
import it.desimone.risiko.torneo.scoreplayer.ScorePlayer;
import it.desimone.risiko.torneo.scoreplayer.ScorePlayerQualificazioniNazionale;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ScoreQualificazioniNazionaleComparator implements Comparator<ScorePlayer> {
	
	/**
	 * La classifica è ovviamente determinata dal totale dei punti torneo ed in caso di parità verranno utilizzati i seguenti criteri:
	 * - punteggio torneo più alto nella migliore prestazione
	 * - punteggio torneo più alto nella seconda migliore prestazione
	 * - punteggio torneo più alto nella terza eventuale prestazione
	 * - piazzamento in classifica al turno precedente (chi era davanti resta davanti).
	 */
	
	public int compare(ScorePlayer scorePlayer1, ScorePlayer scorePlayer2) {
		int result = 0;

		ScorePlayerQualificazioniNazionale sc1 = (ScorePlayerQualificazioniNazionale) scorePlayer1;
		ScorePlayerQualificazioniNazionale sc2 = (ScorePlayerQualificazioniNazionale) scorePlayer2;
		
//		if (sc2.getPosition() > 0 && sc1.getPosition() == 0){
//			result = 1;
//		}
//		if (result == 0 && sc2.getPosition() == 0 && sc1.getPosition() > 0){
//			result = -1;
//		}			
//		if (result == 0 && sc2.getPosition() > 0 && sc1.getPosition() > 0){
//			if (sc2.getPosition() < sc1.getPosition()){
//				result = 1;
//			}else if (sc2.getPosition() > sc1.getPosition()){
//				result = -1;
//			}
//		}		
		/* Confronto tra punteggi complessivi */
		if (result == 0){
			result = sc2.getPunteggioB(false).compareTo(sc1.getPunteggioB(false));
		}
		
		/* Confronto tra punteggi ordinati in base al valore */
		if (result == 0){
			List<BigDecimal> punteggiOrdinati1 = getPunteggiOrdinati(sc1);
			List<BigDecimal> punteggiOrdinati2 = getPunteggiOrdinati(sc2);
			if (punteggiOrdinati1.size() > punteggiOrdinati2.size()){
				for (int i = 0; i < punteggiOrdinati2.size(); i++){
					result = punteggiOrdinati2.get(i).compareTo(punteggiOrdinati1.get(i));
					if (result != 0){break;}
				}
			}else{
				for (int i = 0; i < punteggiOrdinati1.size(); i++){
					result = punteggiOrdinati2.get(i).compareTo(punteggiOrdinati1.get(i));
					if (result != 0){break;}
				}
			}
		}
				
		/* Confronto tra punteggi ordinati in base al tempo */
		if (result == 0){
			GiocatoreDTO giocatore1 = sc1.getGiocatore();
			GiocatoreDTO giocatore2 = sc2.getGiocatore();
			Partita[] partite1 = sc1.getPartite();
			Partita[] partite2 = sc2.getPartite();
//			System.out.println("giocatore1: "+giocatore1);
//			System.out.println("partite1: "+ArrayUtils.toString(partite1));
//			System.out.println("giocatore2: "+giocatore2);
//			System.out.println("partite2: "+ArrayUtils.toString(partite2));
			//Per come vengono caricati gli array hanno sempre stessa dimensione e sono già ordinati in senso temporale
			if (partite1 != null && partite2 != null && partite1.length == partite2.length){
				for (int i = 0; i< partite1.length; i++){
					if (partite1[i] != null && partite2[i] != null){
						BigDecimal punti1 = partite1[i].getPunteggioTrascodificatoB(giocatore1);
						BigDecimal punti2 = partite2[i].getPunteggioTrascodificatoB(giocatore2);
						result = punti2.compareTo(punti1);
						if (result != 0){break;}
					}
				}
			}
		}
		
		return result;
	}
	
	private List<BigDecimal> getPunteggiOrdinati(ScorePlayerQualificazioniNazionale scorePlayer){
		List<BigDecimal> list = new ArrayList<BigDecimal>();
		for (Partita partita: scorePlayer.getPartite()){
			if (partita != null){
				list.add(partita.getPunteggioTrascodificatoB(scorePlayer.getGiocatore()));
			}
		}
		Comparator<BigDecimal> comp = Collections.reverseOrder();
		Collections.sort(list, comp);
		return list;
	}
	
	public static void main (String[] s){
		List<BigDecimal> list = new ArrayList<BigDecimal>();
		list.add(BigDecimal.TEN);
		list.add(new BigDecimal(3));
		list.add(new BigDecimal(5));
		list.add(new BigDecimal(1));
		//Collections.sort(list);
		System.out.println(list);
		Comparator<BigDecimal> comp = Collections.reverseOrder();
		Collections.sort(list, comp);
		System.out.println(list);
	}
}
