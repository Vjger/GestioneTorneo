package it.desimone.risiko.torneo.utils;

import it.desimone.risiko.torneo.dto.Partita;

public class TavoliVuotiCreator {
	
	public static Partita[] genera(TipoTavoli tipoTavoli, int numeroGiocatori){
		Partita[] result = null;
		switch(tipoTavoli){
		case DA_4_ED_EVENTUALMENTE_DA_5:
			result = generaDa4EdEventualmenteDa5(numeroGiocatori);
			break;
		case DA_4_ED_EVENTUALMENTE_DA_3:
			result = generaDa4EdEventualmenteDa3(numeroGiocatori);
			break;
		case DA_4_ED_EVENTUALMENTE_DA_5_SE_UNO_SOLO_ALTRIMENTI_DA_3:
		case DA_4_ED_EVENTUALMENTE_DA_5_SE_UNO_SOLO_ALTRIMENTI_DA_3_COL_MORTO:
			result = generaDa4EdEventualmenteDa5SeUnoSoloAltrimentiDa3(numeroGiocatori);
			break;
		case DA_3_ED_EVENTUALMENTE_DA_2:
		case DA_3_ED_EVENTUALMENTE_DA_2_COL_MORTO:
			result = generaDa3EdEventualmenteDa2(numeroGiocatori);
			break;
		case DA_2_ED_EVENTUALMENTE_DA_3:
			result = generaDa2EdEventualmenteDa3(numeroGiocatori);
			break;
		case DA_2_ED_EVENTUALMENTE_DA_2_COL_MORTO:
			result = generaDa2EdEventualmenteDa2ColMorto(numeroGiocatori);
			break;			
		case DA_5_ED_EVENTUALMENTE_DA_4:
		case DA_5_ED_EVENTUALMENTE_DA_4_COL_MORTO:
			result = generaDa5EdEventualmenteDa4(numeroGiocatori);
			break;
		default:
			throw new IllegalArgumentException("TipoTavolo non previsto: "+tipoTavoli);
		}
		
		for (int k=0; k<result.length; k++){
			result[k].setNumeroTavolo(k+1);
		}
		return result;
	}
	
	private static Partita[] generaDa4EdEventualmenteDa5(int numeroGiocatori){
		if (numeroGiocatori < 4 || numeroGiocatori == 6 || numeroGiocatori == 7 || numeroGiocatori == 11){
			throw new IllegalArgumentException("Il numero di giocatori non consente di fare i tavoli richiesti: "+numeroGiocatori);
		}
		Partita[] result = null;
		int numeroTavolida5 = numeroGiocatori%4;
		int numeroTavolida4 = (numeroGiocatori - numeroTavolida5*5)/4;
		result = new Partita[numeroTavolida4+numeroTavolida5];
		for (int i = 0; i < numeroTavolida4; i++){
			result[i] = new Partita(4);
		}
		for (int j = 0; j < numeroTavolida5; j++){
			result[j+numeroTavolida4] = new Partita(5);
		}
		return result;
	}

	
	private static Partita[] generaDa4EdEventualmenteDa3(int numeroGiocatori){
		if (numeroGiocatori < 2 || numeroGiocatori == 5){
			throw new IllegalArgumentException("Il numero di giocatori non consente di fare nemmeno un tavolo: "+numeroGiocatori);
		}
		Partita[] result = null;
		switch (numeroGiocatori) {
		case 3:
			result = new Partita[1];
			result[0] = new Partita(3);
			break;
		default:
			int numeroTavolida3 = numeroGiocatori%4 == 0 ? 0 : 4 - numeroGiocatori%4;
			int numeroTavolida4 = (numeroGiocatori - numeroTavolida3*3)/4;
			result = new Partita[numeroTavolida4+numeroTavolida3];
			for (int i = 0; i < numeroTavolida4; i++){
				result[i] = new Partita(4);
			}
			for (int j = 0; j < numeroTavolida3; j++){
				result[j+numeroTavolida4] = new Partita(3);
			}
			break;
		}
		return result;
	}
	
	private static Partita[] generaDa4EdEventualmenteDa5SeUnoSoloAltrimentiDa3(int numeroGiocatori){
		if (numeroGiocatori < 2){
			throw new IllegalArgumentException("Il numero di giocatori non consente di fare nemmeno un tavolo: "+numeroGiocatori);
		}
		Partita[] result = null;
		switch (numeroGiocatori) {
		case 3:
			result = new Partita[1];
			result[0] = new Partita(3);
			break;
		default:
			int restoDa4 = numeroGiocatori%4;
			int numeroTavolida5 = 0;
			int numeroTavolida3 = 0;
			if (restoDa4 == 1){
				numeroTavolida5 = 1;
			}else if (restoDa4 == 2){
				numeroTavolida3 = 2;
			}else if (restoDa4 == 3){
				numeroTavolida3 = 1;
			}
			int numeroTavolida4 = (numeroGiocatori - numeroTavolida5*5 - numeroTavolida3*3)/4;
			result = new Partita[numeroTavolida4+numeroTavolida5+numeroTavolida3];
			for (int k = 0; k < numeroTavolida3; k++){
				result[k] = new Partita(3);
			}
			for (int i = 0; i < numeroTavolida4; i++){
				result[i+numeroTavolida3] = new Partita(4);
			}
			for (int j = 0; j < numeroTavolida5; j++){
				result[j+numeroTavolida4+numeroTavolida3] = new Partita(5);
			}
			break;
		}
		return result;
	}
	
	private static Partita[] generaDa2EdEventualmenteDa3(int numeroGiocatori){
		if (numeroGiocatori < 2){
			throw new IllegalArgumentException("Il numero di giocatori non consente di fare i tavoli richiesti: "+numeroGiocatori);
		}
		Partita[] result = null;
		int numeroTavolida3 = numeroGiocatori%2;
		int numeroTavolida2 = (numeroGiocatori - numeroTavolida3*3)/2;
		result = new Partita[numeroTavolida2+numeroTavolida3];
		for (int i = 0; i < numeroTavolida2; i++){
			result[i] = new Partita(2);
		}
		for (int j = 0; j < numeroTavolida3; j++){
			result[j+numeroTavolida2] = new Partita(3);
		}
		return result;
	}
	
	private static Partita[] generaDa2EdEventualmenteDa2ColMorto(int numeroGiocatori){
		if (numeroGiocatori < 2){
			throw new IllegalArgumentException("Il numero di giocatori non consente di fare i tavoli richiesti: "+numeroGiocatori);
		}
		int numeroTavoliColMorto = numeroGiocatori%2;
		int numeroTavoli = numeroGiocatori/2 + numeroTavoliColMorto;
		Partita[] result = new Partita[numeroTavoli];
		for (int i = 0; i < (numeroGiocatori/2); i++){
			result[i] = new Partita(2);
		}
		if (numeroTavoliColMorto != 0){
			result[result.length -1] = new Partita(1);
		}
		return result;
	}

	private static Partita[] generaDa3EdEventualmenteDa2(int numeroGiocatori){
		if (numeroGiocatori < 2){
			throw new IllegalArgumentException("Il numero di giocatori non consente di fare i tavoli richiesti: "+numeroGiocatori);
		}
		Partita[] result = null;
		int restoDa3 = numeroGiocatori%3;
		int numeroTavoliDa2 = 0;
		if (restoDa3 == 1){
			numeroTavoliDa2 = 2;
		}else if (restoDa3 == 2){
			numeroTavoliDa2 = 1;
		}
		int numeroTavolida3 = (numeroGiocatori - numeroTavoliDa2*2)/3;
		result = new Partita[numeroTavoliDa2+numeroTavolida3];
		for (int i = 0; i < numeroTavolida3; i++){
			result[i] = new Partita(3);
		}
		for (int j = 0; j < numeroTavoliDa2; j++){
			result[j+numeroTavolida3] = new Partita(2);
		}
		return result;
	}
	
	private static Partita[] generaDa5EdEventualmenteDa4(int numeroGiocatori){
		if (numeroGiocatori < 4 || numeroGiocatori == 6 || numeroGiocatori == 7 || numeroGiocatori == 11){
			throw new IllegalArgumentException("Il numero di giocatori non consente di fare i tavoli richiesti: "+numeroGiocatori);
		}
		Partita[] result = null;
		int numeroGiocatoriRimanenti = numeroGiocatori;
		int restoDa5 = -1;
		int numeroTavoliDa4 = 0;
		while (restoDa5 != 0 && numeroGiocatoriRimanenti > 0){
			restoDa5 = numeroGiocatoriRimanenti%5;
			if (restoDa5 != 0){
				numeroTavoliDa4++;
				numeroGiocatoriRimanenti = numeroGiocatoriRimanenti -4;
			}
		}		
		int numeroTavolida5 = (numeroGiocatori - numeroTavoliDa4*4)/5;
		result = new Partita[numeroTavolida5+numeroTavoliDa4];
		for (int i = 0; i < numeroTavolida5; i++){
			result[i] = new Partita(5);
		}
		for (int j = 0; j < numeroTavoliDa4; j++){
			result[j+numeroTavolida5] = new Partita(4);
		}
		return result;
	}

}
