package it.desimone.risiko.torneo.scoreplayer;

import it.desimone.risiko.torneo.dto.GiocatoreDTO;
import it.desimone.risiko.torneo.dto.Partita;

import java.math.BigDecimal;

public class ScorePlayerTorneoGufo extends AbstractScorePlayer{

	private int numeroVittorie = 0;
	private Float massimoPunteggio = 0f;
	private GiocatoreDTO giocatore;
	
	private static final BigDecimal UNO_V_25 = new BigDecimal(1.25); 
	private static final BigDecimal ZERO_V_75 = new BigDecimal(0.75); 
	private static final BigDecimal CENTO = new BigDecimal(100); 
	
	private Partita[] partite;
	public ScorePlayerTorneoGufo(GiocatoreDTO giocatore, Partita[] partite){
		this.giocatore = giocatore;
		this.partite = new Partita[partite.length];
		for (int i=0; i < partite.length; i++){
			if (partite[i] != null){
				this.partite[i] = partite[i];
			}
		}
		for (Partita partita: partite){
			trascodificaPunteggio(partita, giocatore);
		}
	}
	
	public BigDecimal getPunteggioB(boolean conScarto){
		BigDecimal totale = BigDecimal.ZERO;
		Float minimo = Float.MAX_VALUE;
		for (Partita partita: partite){
			BigDecimal punteggio = partita==null?BigDecimal.ZERO:partita.getPunteggioTrascodificatoB(giocatore);
			totale = totale.add(punteggio);
			minimo = Math.min(minimo, punteggio.floatValue());
		}
		if (conScarto){
			totale = totale.subtract(new BigDecimal(minimo));
		}
		return totale;
	}
	
	public Float getPunteggio(boolean conScarto){
		Float totale = 0f;
		Float minimo = Float.MAX_VALUE;
		for (Partita partita: partite){
			Float punteggio = partita==null?0f:partita.getPunteggioTrascodificato(giocatore);
			totale += punteggio;
			minimo = Math.min(minimo, punteggio);
		}
		if (conScarto){
			totale = totale - minimo;
		}
		return totale;
	}
	
	public GiocatoreDTO getGiocatore() {
		return giocatore;
	}
	public void setGiocatore(GiocatoreDTO giocatore) {
		this.giocatore = giocatore;
	}
	public Partita[] getPartite() {
		return partite;
	}
	
//	private void trascodificaPunteggio(Partita partita, GiocatoreDTO giocatore){
//		Float punteggio = partita==null?0f:partita.getPunteggio(giocatore);
//		Integer parteIntera = punteggio.intValue();
//		Float centesimi = punteggio - parteIntera;
//		if (partita != null){
//			if (partita.getNumeroGiocatori() == 5){
//				punteggio = parteIntera + centesimi*1.25f;
//			}else if (partita.getNumeroGiocatori() == 3){
//				punteggio = parteIntera + centesimi*0.75f;
//			}		
//			if (partita.isVincitore(giocatore)){
//				numeroVittorie++;
//			}
//			massimoPunteggio = Math.max(massimoPunteggio, punteggio);
//			partita.setPunteggioTrascodificato(giocatore, punteggio);
//		}
//	}
	
	private void trascodificaPunteggio(Partita partita, GiocatoreDTO giocatore){
		Float punteggio;
		BigDecimal punteggioB;
		if (partita == null){
			punteggio = 0f;
			punteggioB = BigDecimal.ZERO;
		}else{
			punteggioB = new BigDecimal(partita.getPunteggio(giocatore));
			punteggioB = punteggioB.setScale(0,BigDecimal.ROUND_DOWN);
			punteggio  = punteggioB.floatValue();

			Integer parteIntera = getBonusIntero(partita.getPosizione(giocatore), punteggio);
			Float centesimi = 0f;
			BigDecimal centesimiB = BigDecimal.ZERO;
			if (punteggio >= 0f){
				if (partita.getNumeroGiocatori() == 5){
					centesimi = punteggio*1.25f/100f;
					centesimiB = punteggioB.multiply(UNO_V_25).divide(CENTO);
				}else if (partita.getNumeroGiocatori() == 3){
					centesimi = punteggio*0.75f/100f;
					centesimiB = punteggioB.multiply(ZERO_V_75).divide(CENTO);	
				}else{
					centesimi = punteggio/100f;
					centesimiB = punteggioB.divide(CENTO);
				}
			}
			punteggio = parteIntera + centesimi;
			punteggioB = new BigDecimal(parteIntera).add(centesimiB);
			if (partita.isVincitore(giocatore)){
				numeroVittorie++;
			}
			partita.setPunteggioTrascodificato(giocatore, punteggio);
			partita.setPunteggioTrascodificatoB(giocatore, punteggioB);
		}
		massimoPunteggio = Math.max(massimoPunteggio, punteggio);

	}

	private int getBonusIntero(int posizione, Float punteggio){
		int result = 0;
		switch (posizione) {
		case 1:
			result = 12;
			break;
		case 2:
			if (punteggio >= 0){
				result = 7;
			}else{
				result = 3;
			}
			break;
		case 3:
			if (punteggio >= 0){
				result = 5;
			}else{
				result = 2;
			}
			break;
		case 4:
			if (punteggio >= 0){
				result = 3;
			}else{
				result = 1;
			}
			break;
		case 5:
			if (punteggio >= 0){
				result = 2;
			}else{
				result = 0;
			}
			break;

		default:
			throw new IllegalArgumentException("Posizione al tavolo imprevista: "+posizione);
		}
		return result;
	}
	
	public int getNumeroVittorie() {
		return numeroVittorie;
	}

	public Float getPunteggioMassimo() {
		return massimoPunteggio;
	}
}
