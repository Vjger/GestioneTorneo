package it.desimone.risiko.torneo.scoreplayer;

import it.desimone.risiko.torneo.dto.GiocatoreDTO;
import it.desimone.risiko.torneo.dto.Partita;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;

public class ScorePlayerCampionatoGufo extends AbstractScorePlayer{

	private static final BigDecimal UNO_V_25 = new BigDecimal(1.25); 
	private static final BigDecimal CENTO = new BigDecimal(100); 
	
	private final static int NUMERO_PARTITE_VALIDE = 50;
	private int numeroVittorie = 0;
	private Float massimoPunteggio = 0f;
	private GiocatoreDTO giocatore;
	Comparator<Partita> comparator;
	Comparator<Partita> comparatorNonTrascodificato;
	
	{
		comparator = new Comparator<Partita>(){
			public int compare(Partita partita1, Partita partita2){
				return Float.compare(partita2==null?0f:partita2.getPunteggioTrascodificato(giocatore),partita1==null?0f:partita1.getPunteggioTrascodificato(giocatore));
			}
		};
		
		comparatorNonTrascodificato = new Comparator<Partita>(){
			public int compare(Partita partita1, Partita partita2){
				return Float.compare(partita2==null?0f:partita2.getPunteggio(giocatore),partita1==null?0f:partita1.getPunteggio(giocatore));
			}
		};
	}
	
	private Partita[] partite;
	public ScorePlayerCampionatoGufo(GiocatoreDTO giocatore, Partita[] partite){
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
	
	public Float getPunteggioNonTrascodificato(boolean conScarto){
		Float totale = 0f;
		Partita[] clonedPartite = new Partita[partite.length];
		for (int i = 0; i < clonedPartite.length; i++){
			clonedPartite[i] = partite[i];
		}
		Arrays.sort(clonedPartite,getComparatorPartiteNonTrascodificate());
		int index = 0;
		for (Partita partita: clonedPartite){
			if (conScarto && index++ == NUMERO_PARTITE_VALIDE){break;}
			Float punteggio = partita==null?0f:partita.getPunteggio(giocatore);
			totale += punteggio;
		}
		return totale;
	}
	
	public BigDecimal getPunteggioB(boolean conScarto){
		BigDecimal totale = BigDecimal.ZERO;
		Partita[] clonedPartite = new Partita[partite.length];
		for (int i = 0; i < clonedPartite.length; i++){
			clonedPartite[i] = partite[i];
		}
		Arrays.sort(clonedPartite,getComparatorPartite());
		int index = 0;
		for (Partita partita: clonedPartite){
			if (conScarto && index++ == NUMERO_PARTITE_VALIDE){break;}
			BigDecimal punteggio = partita==null?BigDecimal.ZERO:partita.getPunteggioTrascodificatoB(giocatore);
			totale = totale.add(punteggio);
		}
		return totale;
	}
	
	public Float getPunteggio(boolean conScarto){
		Float totale = 0f;
		Partita[] clonedPartite = new Partita[partite.length];
		for (int i = 0; i < clonedPartite.length; i++){
			clonedPartite[i] = partite[i];
		}
		Arrays.sort(clonedPartite,getComparatorPartite());
		int index = 0;
		for (Partita partita: clonedPartite){
			if (conScarto && index++ == NUMERO_PARTITE_VALIDE){break;}
			Float punteggio = partita==null?0f:partita.getPunteggioTrascodificato(giocatore);
			totale += punteggio;
		}
		return totale;
	}
	
	private Comparator<Partita> getComparatorPartite(){
		return comparator;
	}
	private Comparator<Partita> getComparatorPartiteNonTrascodificate(){
		return comparatorNonTrascodificato;
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
		Float punteggioTrascodificato;
		BigDecimal punteggioB;
		if (partita != null){
			punteggioB = new BigDecimal(partita.getPunteggio(giocatore));
			punteggioB = punteggioB.setScale(0,BigDecimal.ROUND_DOWN);
			punteggio  = punteggioB.floatValue();

			if (partita.isVincitore(giocatore)){
				numeroVittorie++;
				if (partita.getNumeroGiocatori() == 5){
					punteggio = punteggio*1.25f;
					punteggioB = punteggioB.multiply(UNO_V_25);
				}
				punteggioTrascodificato = 100+punteggio;
				punteggioB = punteggioB.add(CENTO);
			}else{
				BigDecimal punteggioVincB = new BigDecimal(partita.getPunteggioVincitore());
				punteggioVincB = punteggioVincB.setScale(0,BigDecimal.ROUND_DOWN);
				Float punteggioVinc  = punteggioVincB.floatValue();
				punteggioTrascodificato = 100 - ((punteggioVinc < 86? punteggioVinc:100)-punteggio);
				punteggioB = CENTO.subtract((punteggioVinc < 86 ? punteggioVincB:CENTO).subtract(punteggioB));
			}
			partita.setPunteggioTrascodificato(giocatore, punteggioTrascodificato);
			partita.setPunteggioTrascodificatoB(giocatore, punteggioB);
		}
	}

	public int getNumeroVittorie() {
		return numeroVittorie;
	}

	public Float getPunteggioMassimo() {
		return massimoPunteggio;
	}
}
