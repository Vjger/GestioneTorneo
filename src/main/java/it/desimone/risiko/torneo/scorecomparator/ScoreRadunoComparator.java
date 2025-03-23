package it.desimone.risiko.torneo.scorecomparator;

import it.desimone.risiko.torneo.dto.Partita;
import it.desimone.risiko.torneo.scoreplayer.ScorePlayer;
import it.desimone.utils.MyException;

import java.util.Comparator;

public class ScoreRadunoComparator implements Comparator<ScorePlayer> {

	/*
	 * La classifica finale del torneo sarà determinata dall’esito della finale, poi seguiranno i giocatori sconfitti in semifinale sommando 
	 * i punti ottenuti nelle prime due partite e nella semifinale. A seguire i giocatori sconfitti negli eventuali quarti di finale sommando 
	 * i punti ottenuti nelle loro tre partite giocate. Infine ci saranno i giocatori che hanno giocato solo le due partite del sabato.
	 */
	
	public int compare(ScorePlayer scorePlayer1, ScorePlayer scorePlayer2) {
		int result = 0;

		//Prima cosa da capire è se si sono giocati i quarti oppure no: lo si fa in base alla lunghezza degli array (che equivale al numero di turni disputati)
		
		int numeroTurniDisputati = scorePlayer1.getPartite().length;
		
		int turnoMassimoDisputato1 = 0;
		int turnoMassimoDisputato2 = 0;
		
		for (int numeroTurno = numeroTurniDisputati-1; numeroTurno >= 0; numeroTurno--){
			if (turnoMassimoDisputato1 == 0 && scorePlayer1.getPartite()[numeroTurno] != null){
				turnoMassimoDisputato1 = numeroTurno+1;
			}
			if (turnoMassimoDisputato2 == 0 && scorePlayer2.getPartite()[numeroTurno] != null){
				turnoMassimoDisputato2 = numeroTurno+1;
			}
		}

		switch (turnoMassimoDisputato2) {
		case 5:
			//Mi chiedo se uno dei due giocatori è un finalista: se lo è sta sopra: se lo sono entrambi vedo la posizione al tavolo.
			if (turnoMassimoDisputato2 > turnoMassimoDisputato1){
				result = 1;
			}else{
				Partita finale = scorePlayer2.getPartite()[4];
				int posizione2 = finale.getPosizione(scorePlayer2.getGiocatore());
				int posizione1 = finale.getPosizione(scorePlayer1.getGiocatore());
				if (posizione2 < posizione1){
					result = 1;
				}else{
					result = -1;
				}
			}
			break;
		case 4:
			if (turnoMassimoDisputato2 > turnoMassimoDisputato1){
				result = 1;
			}else if (turnoMassimoDisputato1 > turnoMassimoDisputato2){
				result = -1;
			}else if (turnoMassimoDisputato2 == numeroTurniDisputati){ //potrebbe essere la finale: verifico che entrambi siano nella stessa partita
				Partita presuntaFinale = scorePlayer2.getPartite()[3];
				if (presuntaFinale.eAlTavolo(scorePlayer1.getGiocatore())){//potrebbe essere la finale: ordino comunque in base alla posizione al tavolo
					int posizione2 = presuntaFinale.getPosizione(scorePlayer2.getGiocatore());
					int posizione1 = presuntaFinale.getPosizione(scorePlayer1.getGiocatore());
					if (posizione2 < posizione1){
						result = 1;
					}else{
						result = -1;
					}
				}else{//sicuramente è una semifinale: se uno dei due l'ha vinta è davanti altrimenti ordino in base alla somma punti come indicato in testa
					result = comparaSemifinalisti(scorePlayer1, scorePlayer2);
				}
			}else{
				result = comparaSemifinalisti(scorePlayer1, scorePlayer2);
			}
			break;
		case 3: //Non ha nessun senso fare la classifica considerando una situazione spuria di quarti o semifinali disputate quindi la faccio sempre
				//in base alle prime due partite a meno che il giocatore1 non sia due turni avanti.
			//TODO E' ancora da sistemare: i ritirati possono passare avanti
		case 2:
		case 1:
			if (turnoMassimoDisputato2 == 3 && turnoMassimoDisputato2 > turnoMassimoDisputato1){
				result = 1;
			}else if (turnoMassimoDisputato1 > turnoMassimoDisputato2 && turnoMassimoDisputato2 >= 2){
				result = -1;
			}else{
				Partita[] partite1 = scorePlayer1.getPartite();
				Partita[] partite2 = scorePlayer2.getPartite();
				Float punteggio1 = 0f;
				if (partite1 != null && partite1.length >= 1 && partite1[0] != null){
					punteggio1 += partite1[0].getPunteggioTrascodificato(scorePlayer1.getGiocatore());
				}
				if (partite1 != null && partite1.length >= 2 && partite1[1] != null){
					punteggio1 += partite1[1].getPunteggioTrascodificato(scorePlayer1.getGiocatore());
				}
				Float punteggio2 = 0f;
				if (partite2 != null && partite2.length >= 1 && partite2[0] != null){
					punteggio2 += partite2[0].getPunteggioTrascodificato(scorePlayer2.getGiocatore());
				}
				if (partite2 != null && partite2.length >= 2 && partite2[1] !=null){
					punteggio2 += partite2[1].getPunteggioTrascodificato(scorePlayer2.getGiocatore());
				}
				if (turnoMassimoDisputato1 == 3 && turnoMassimoDisputato2 == 3){
					punteggio1 += partite1[2].getPunteggioTrascodificato(scorePlayer1.getGiocatore());
					punteggio2 += partite2[2].getPunteggioTrascodificato(scorePlayer2.getGiocatore());
				}
	
				Float differenza = punteggio2 - punteggio1;

				if (differenza >  0f){
					result = 1;
				}else if (differenza < 0f){
					result = -1;
				}else{
					result = 0;
				}
			}

			if (result == 0){
				Float differenza = scorePlayer2.getPunteggioMassimo() - scorePlayer1.getPunteggioMassimo();
				if (differenza >  0f){
					result = 1;
				}else if (differenza < 0f){
					result = -1;
				}else{
					//result = 0;
					Partita primaPartita1 = (scorePlayer1.getPartite() == null ||  scorePlayer1.getPartite().length == 0)?null:scorePlayer1.getPartite()[0];
					Partita primaPartita2 = (scorePlayer2.getPartite() == null ||  scorePlayer2.getPartite().length == 0)?null:scorePlayer2.getPartite()[0];
					if (primaPartita1 != null && primaPartita2 != null){
						Float punteggio1 = primaPartita1.getPunteggioTrascodificato(scorePlayer1.getGiocatore());
						Float punteggio2 = primaPartita2.getPunteggioTrascodificato(scorePlayer2.getGiocatore());
						differenza = punteggio2 - punteggio1;
						if (differenza >  0f){
							result = 1;
						}else if (differenza < 0f){
							result = -1;
						}
					}
				}
			}
			break;	
		case 0:
			result = -1;
			break;
		default:
			throw new MyException("Richiesto un calcolo della classifica per un numero massimo di turni disputati non previsto: "+turnoMassimoDisputato2);
		}
		
		return result;
	}
	
	private int comparaSemifinalisti(ScorePlayer scorePlayer1, ScorePlayer scorePlayer2){
		int result = 0;
		Partita[] partite1 = scorePlayer1.getPartite();
		Partita[] partite2 = scorePlayer2.getPartite();
		boolean haVinto1 = partite1[3].isVincitore(scorePlayer1.getGiocatore());
		boolean haVinto2 = partite2[3].isVincitore(scorePlayer2.getGiocatore());
		
		if (haVinto2 &&!haVinto1){
			result = 1;
		}else if (haVinto1 &&!haVinto2){
			result = -1;
		}else{
			Float punteggio1 = 0f;
			if (partite1[0] !=null){
				punteggio1 += partite1[0].getPunteggioTrascodificato(scorePlayer1.getGiocatore());
			}
			if (partite1[1] !=null){
				punteggio1 += partite1[1].getPunteggioTrascodificato(scorePlayer1.getGiocatore());
			}
			if (partite1[3] !=null){
				punteggio1 += partite1[3].getPunteggioTrascodificato(scorePlayer1.getGiocatore());
			}
			Float punteggio2 = 0f;
			if (partite2[0] !=null){
				punteggio2 += partite2[0].getPunteggioTrascodificato(scorePlayer2.getGiocatore());
			}
			if (partite2[1] !=null){
				punteggio2 += partite2[1].getPunteggioTrascodificato(scorePlayer2.getGiocatore());
			}
			if (partite2[3] !=null){
				punteggio2 += partite2[3].getPunteggioTrascodificato(scorePlayer2.getGiocatore());
			}
			Float differenza = punteggio2 - punteggio1;
			
			if (differenza >  0f){
				result = 1;
			}else if (differenza < 0f){
				result = -1;
			}else{
				differenza = scorePlayer2.getPunteggioMassimo() - scorePlayer1.getPunteggioMassimo();
				if (differenza >  0f){
					result = 1;
				}else if (differenza < 0f){
					result = -1;
				}else{
					result = 0;
				}
			}
		}
		return result;
	}
	
	public int compare_old(ScorePlayer scorePlayer1, ScorePlayer scorePlayer2) {
		int result = 0;

		Float differenza = scorePlayer2.getPunteggio(false) - scorePlayer1.getPunteggio(false);
		
		if (differenza >  0f){
			result = 1;
		}else if (differenza < 0f){
			result = -1;
		}else{
			result = 0;
		}
		
//		if (result == 0){
//			result = scorePlayer2.getNumeroVittorie() - scorePlayer1.getNumeroVittorie();
//		}
		if (result == 0){
			differenza = scorePlayer2.getPunteggioMassimo() - scorePlayer1.getPunteggioMassimo();
			if (differenza >  0f){
				result = 1;
			}else if (differenza < 0f){
				result = -1;
			}else{
				//result = 0;
				Partita primaPartita1 = (scorePlayer1.getPartite() == null ||  scorePlayer1.getPartite().length == 0)?null:scorePlayer1.getPartite()[0];
				Partita primaPartita2 = (scorePlayer2.getPartite() == null ||  scorePlayer2.getPartite().length == 0)?null:scorePlayer2.getPartite()[0];
				if (primaPartita1 != null && primaPartita2 != null){
					Float punteggio1 = primaPartita1.getPunteggioTrascodificato(scorePlayer1.getGiocatore());
					Float punteggio2 = primaPartita2.getPunteggioTrascodificato(scorePlayer2.getGiocatore());
					differenza = punteggio2 - punteggio1;
					if (differenza >  0f){
						result = 1;
					}else if (differenza < 0f){
						result = -1;
					}
				}
			}
		}
		
		return result;
	}


}
