package it.desimone.risiko.torneo.scoreplayer;

import it.desimone.risiko.torneo.dto.GiocatoreDTO;
import it.desimone.risiko.torneo.dto.Partita;

import java.math.BigDecimal;

public class ScorePlayerTorneoColoni extends AbstractScorePlayer{

	public final static float PUNTEGGIO_VITTORIA = 10f;
	private int numeroVittorie = 0;
	private Float massimoPunteggio = 0f;
	private Float percentualePuntiVittoria = 0f;
	private GiocatoreDTO giocatore;
	
	private Partita[] partite;
	public ScorePlayerTorneoColoni(GiocatoreDTO giocatore, Partita[] partite){
		this.giocatore = giocatore;
		this.partite = new Partita[partite.length];
		for (int i=0; i < partite.length; i++){
			if (partite[i] != null){
				this.partite[i] = partite[i];
				if (partite[i].isVincitore(giocatore)){
					if (partite[i].getPunteggio(giocatore) >= PUNTEGGIO_VITTORIA){
						numeroVittorie = numeroVittorie + 10;  //Moltiplicato per 10 per evitare di cambiare il tipo
					}else{
						numeroVittorie = numeroVittorie + 9;  //Moltiplicato per 10 per evitare di cambiare il tipo
					}
					massimoPunteggio = Math.max(massimoPunteggio,partite[i].getPunteggio(giocatore));
				}
			}
		}
		for (Partita partita: partite){
			if (partita != null){
				trascodificaPunteggio(partita, giocatore);
				calcolaPercentualePuntiVittoria(partita, giocatore);
			}
		}
	}
	
	public BigDecimal getPunteggioB(boolean conScarto){
		BigDecimal totale = BigDecimal.ZERO;
		for (Partita partita: partite){
			BigDecimal punteggio = partita==null?BigDecimal.ZERO:partita.getPunteggioTrascodificatoB(giocatore);
			if (punteggio.compareTo(BigDecimal.TEN) == 1){punteggio = BigDecimal.TEN;}
			totale = totale.add(punteggio);
		}
		return totale;
	}
	
	public Float getPunteggio(boolean conScarto){
		Float totale = 0f;
		for (Partita partita: partite){
			Float punteggio = partita==null?0f:partita.getPunteggio(giocatore);
			if (punteggio > PUNTEGGIO_VITTORIA){punteggio = PUNTEGGIO_VITTORIA;}
			totale += punteggio;
		}
		return totale;
	}
	
	private void calcolaPercentualePuntiVittoria(Partita partita, GiocatoreDTO giocatore){
		int punteggioTotale = 0;
		for (GiocatoreDTO player: partita.getGiocatori()){
			punteggioTotale += partita.getPunteggio(player);
		}
		percentualePuntiVittoria += ((float) partita.getPunteggio(giocatore)) / punteggioTotale;
	}
	
	private void trascodificaPunteggio(Partita partita, GiocatoreDTO giocatore){
		Float punteggio = partita==null?0f:partita.getPunteggio(giocatore);
		BigDecimal punteggioB = partita==null?BigDecimal.ZERO:new BigDecimal(partita.getPunteggio(giocatore));
		if (punteggio > PUNTEGGIO_VITTORIA){punteggio = PUNTEGGIO_VITTORIA;}
		if (punteggioB.compareTo(BigDecimal.TEN) == 1){punteggioB = BigDecimal.TEN;}
		partita.setPunteggioTrascodificato(giocatore, punteggio);
		partita.setPunteggioTrascodificatoB(giocatore, punteggioB);
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
	
	public int getNumeroVittorie() {
		return numeroVittorie;
	}

	public Float getPunteggioMassimo() {
		return massimoPunteggio;
	}

	public Float getPercentualePuntiVittoria() {
		return percentualePuntiVittoria;
	}

	public void setPercentualePuntiVittoria(Float percentualePuntiVittoria) {
		this.percentualePuntiVittoria = percentualePuntiVittoria;
	}
}
