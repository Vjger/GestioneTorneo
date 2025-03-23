package it.desimone.risiko.torneo.scoreplayer;

import it.desimone.risiko.torneo.dto.GiocatoreDTO;
import it.desimone.risiko.torneo.dto.Partita;

import java.math.BigDecimal;

public class ScorePlayerRaduno extends AbstractScorePlayer{

	public static final Integer BONUS = 50; 
	public static final BigDecimal BONUS_B = new BigDecimal(BONUS); 
	private static final BigDecimal DUE = new BigDecimal(2); 
	
	private int numeroVittorie = 0;
	private Float massimoPunteggio = 0f;
	private GiocatoreDTO giocatore;
	
	private Partita[] partite;
	public ScorePlayerRaduno(GiocatoreDTO giocatore, Partita[] partite){
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
	
	public  BigDecimal getPunteggioB(boolean conScarto){
		BigDecimal totale = BigDecimal.ZERO;
		Float minimo = Float.MAX_VALUE;
		for (Partita partita: partite){
			BigDecimal punteggio = partita==null?BigDecimal.ZERO:partita.getPunteggioTrascodificatoB(giocatore);
			totale = totale.add(punteggio);
			minimo = Math.min(minimo, punteggio.floatValue());
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
			//totale = totale - minimo/2f;
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
			if (partita.isVincitore(giocatore)){
				numeroVittorie++;
				if (partita.numeroVincitori() == 1){
					punteggio = punteggio + BONUS;
					punteggioB = punteggioB.add(BONUS_B);
				}else if (partita.numeroVincitori() == 2){
					punteggio = punteggio + BONUS/2f;
					punteggioB = punteggioB.add(BONUS_B.divide(DUE));
				}
			}
			partita.setPunteggioTrascodificato(giocatore, punteggio);
			partita.setPunteggioTrascodificatoB(giocatore, punteggioB);
		}
		massimoPunteggio = Math.max(massimoPunteggio, punteggio);
	}

	public int getNumeroVittorie() {
		return numeroVittorie;
	}

	public Float getPunteggioMassimo() {
		return massimoPunteggio;
	}
}
