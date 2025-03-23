package it.desimone.risiko.torneo.scoreplayer;

import it.desimone.risiko.torneo.dto.GiocatoreDTO;
import it.desimone.risiko.torneo.dto.Partita;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ScorePlayerTorneoBGL extends AbstractScorePlayer{

	private int numeroVittorie = 0;
	private Float massimoPunteggio = 0f;
	private GiocatoreDTO giocatore;

	private static List<Integer[]> puntiPartecipantiVsPosizione = new ArrayList<Integer[]>();
	
	static{
		puntiPartecipantiVsPosizione.add(new Integer[]{100,55});				//tavolo da 2 (corretto)
		puntiPartecipantiVsPosizione.add(new Integer[]{100,55,10});				//tavolo da 3
		puntiPartecipantiVsPosizione.add(new Integer[]{100,60,40,20});			//tavolo da 4
		puntiPartecipantiVsPosizione.add(new Integer[]{100,65,50,30,15});		//tavolo da 5
		puntiPartecipantiVsPosizione.add(new Integer[]{100,70,55,40,25,10});	//tavolo da 6
		puntiPartecipantiVsPosizione.add(new Integer[]{100,70,50,30,20,10,10});	//tavolo da 7
	}
	
	
	private static final BigDecimal TRE = new BigDecimal(3); 
	private static final BigDecimal ZERONOVE = new BigDecimal(0.9); 
	
	private Partita[] partite;
	public ScorePlayerTorneoBGL(GiocatoreDTO giocatore, Partita[] partite){
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
	

	private void trascodificaPunteggio(Partita partita, GiocatoreDTO giocatore){
		Float punteggio;
		BigDecimal punteggioB;
		if (partita == null){
			punteggio = 0f;
			punteggioB = BigDecimal.ZERO;
		}else{
			BigDecimal parteIntera = getBonusIntero(partita);
			BigDecimal pbg = calcoloPBG(partita);

			punteggio = parteIntera.floatValue() + pbg.floatValue();
			punteggioB = parteIntera.add(pbg);
			if (partita.isVincitore(giocatore)){
				numeroVittorie++;
			}
			partita.setPunteggioTrascodificato(giocatore, punteggio);
			partita.setPunteggioTrascodificatoB(giocatore, punteggioB);
		}
		massimoPunteggio = Math.max(massimoPunteggio, punteggio);

	}

	private BigDecimal getBonusIntero(Partita partita){
		BigDecimal result = BigDecimal.ZERO;

		Partita partitaDaValutare = getPartitaConGiocatoreFantasma(partita);
		
		//TODO implementare il getPosizione reale
		int posizioneGiocatore = partitaDaValutare.getPosizione(giocatore);
		//Valuto se ci sono pari merito e se sì quanti.
		int allaPariColGiocatore = 0;
		for (GiocatoreDTO giocatorePartita: partita.getGiocatori()){
			if (!giocatorePartita.equals(giocatore)
			&&  (!giocatorePartita.equals(GiocatoreDTO.FITTIZIO)
				|| partita.getNumeroGiocatori() == 2)	
			&& posizioneGiocatore == partitaDaValutare.getPosizione(giocatorePartita)){
				allaPariColGiocatore++;
			}
		}
		for (int index = 0 ; index <= allaPariColGiocatore; index++){
			Integer[] puntiPartecipanti = puntiPartecipantiVsPosizione.get(partitaDaValutare.getNumeroGiocatori()-2);
			Integer puntiClassifica = 0;
			puntiClassifica = puntiPartecipanti[posizioneGiocatore-1+index];

			result = result.add(new BigDecimal(puntiClassifica));
		}
		result = result.divide(new BigDecimal(allaPariColGiocatore+1),2, BigDecimal.ROUND_DOWN);
		
		return result;
	}
	
	
	private BigDecimal calcoloPBG(Partita partita){
		BigDecimal result = BigDecimal.ZERO;
		BigDecimal totale = BigDecimal.ZERO;
		Partita partitaDaValutare = getPartitaConGiocatoreFantasma(partita);
		for (GiocatoreDTO giocatorePartita: partitaDaValutare.getGiocatori()){
			BigDecimal punteggioGiocatore = new BigDecimal(partitaDaValutare.getPunteggio(giocatorePartita));
			punteggioGiocatore = punteggioGiocatore.setScale(0,BigDecimal.ROUND_DOWN);
			totale = totale.add(punteggioGiocatore);
		}
		BigDecimal punteggioGiocatore = new BigDecimal(partitaDaValutare.getPunteggio(giocatore));
		result = punteggioGiocatore.divide(totale, 3, BigDecimal.ROUND_DOWN);
		return result;
	}
	
	private Partita getPartitaConGiocatoreFantasma(Partita partita){
		Partita partitaDaValutare = new Partita(partita);
		int numeroGiocatori = partita.getNumeroGiocatori();
		if (numeroGiocatori == 3 || numeroGiocatori == 1){//va aggiunto il giocatore fantasma
			BigDecimal totale = BigDecimal.ZERO;
			for (GiocatoreDTO giocatorePartita: partita.getGiocatori()){
				BigDecimal punteggioGiocatore = new BigDecimal(partita.getPunteggio(giocatorePartita));
				punteggioGiocatore = punteggioGiocatore.setScale(0,BigDecimal.ROUND_DOWN);
				totale = totale.add(punteggioGiocatore);
			}
			BigDecimal punteggioGiocatoreFantasma = ZERONOVE.multiply(totale).divide(new BigDecimal(numeroGiocatori), 1, BigDecimal.ROUND_DOWN);
			partitaDaValutare.setNumeroGiocatori(numeroGiocatori+1);
			partitaDaValutare.addGiocatore(GiocatoreDTO.FITTIZIO, punteggioGiocatoreFantasma.floatValue());
		}
		return partitaDaValutare;
	}
	
	
	public int getNumeroVittorie() {
		return numeroVittorie;
	}

	public Float getPunteggioMassimo() {
		return massimoPunteggio;
	}
}
