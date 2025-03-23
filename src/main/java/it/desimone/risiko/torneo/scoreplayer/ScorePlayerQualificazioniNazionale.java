package it.desimone.risiko.torneo.scoreplayer;

import it.desimone.risiko.torneo.dto.GiocatoreDTO;
import it.desimone.risiko.torneo.dto.Partita;
import it.desimone.utils.MyLogger;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;

public class ScorePlayerQualificazioniNazionale extends AbstractScorePlayer{

	private final static int NUMERO_PARTITE_VALIDE = 5;

	private final static BigDecimal MILLE = new BigDecimal(1000);
	private final static BigDecimal CENTO = new BigDecimal(100);
	private static final BigDecimal UNO_V_25 = new BigDecimal(1.25); 
	
	private int numeroVittorie = 0;
	private Float massimoPunteggio = 0f;
	private GiocatoreDTO giocatore;
	Comparator<Partita> comparator;
	
	{
		comparator = new Comparator<Partita>(){
			public int compare(Partita partita1, Partita partita2){
				return Float.compare(partita2==null?0f:partita2.getPunteggioTrascodificato(giocatore),partita1==null?0f:partita1.getPunteggioTrascodificato(giocatore));
			}
		};
		
	}
	
	private Partita[] partite;
	public ScorePlayerQualificazioniNazionale(GiocatoreDTO giocatore, Partita[] partite){
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
		Partita[] clonedPartite = new Partita[partite.length];
		for (int i = 0; i < clonedPartite.length; i++){
			clonedPartite[i] = partite[i];
		}
		Arrays.sort(clonedPartite,getComparatorPartite());
		int index = 0;
		for (Partita partita: clonedPartite){
			if (conScarto && index++ == NUMERO_PARTITE_VALIDE){break;}
			BigDecimal punteggio = partita==null?BigDecimal.ZERO:partita.getPunteggioTrascodificatoB(giocatore);
			//System.out.println("Giocatore: "+giocatore+" punteggio: "+punteggio);
			totale = totale.add(punteggio);
		}
		//System.out.println("Giocatore: "+giocatore+" Totale: "+totale);
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
		Float punteggioTrascodificato;
		BigDecimal punteggioTrascodificatoB;
		if (partita != null){
			punteggioB = new BigDecimal(partita.getPunteggio(giocatore));
			punteggioB = punteggioB.setScale(0,BigDecimal.ROUND_DOWN);
			punteggio  = punteggioB.floatValue();

			if (partita.getNumeroGiocatori() == 5){
				if (punteggio >= 80){
					punteggio = 100f;
					punteggioB = CENTO;
				}else{
					punteggio = punteggio*1.25f;
					punteggioB = punteggioB.multiply(UNO_V_25);
				}
			}
			if (partita.isVincitore(giocatore)){
				numeroVittorie++;
				punteggioTrascodificato = calcolaPunteggioTrascodificato(punteggio, true);
				punteggioTrascodificatoB = calcolaPunteggioTrascodificatoB(punteggioB, true);
			}else{
				punteggioTrascodificato = calcolaPunteggioTrascodificato(punteggio, false);
				punteggioTrascodificatoB = calcolaPunteggioTrascodificatoB(punteggioB, false);
			}
			partita.setPunteggioTrascodificato(giocatore, punteggioTrascodificato);
			partita.setPunteggioTrascodificatoB(giocatore, punteggioTrascodificatoB);
			MyLogger.getLogger().fine("Giocatore: "+giocatore+" Punteggio: "+punteggio+" Punteggio Trascodificato: "+punteggioTrascodificato+" Punteggio TrascodificatoB: "+punteggioTrascodificatoB);
		}
	}

	public int getNumeroVittorie() {
		return numeroVittorie;
	}

	public Float getPunteggioMassimo() {
		return massimoPunteggio;
	}
	
	private static Float calcolaPunteggioTrascodificato(float punteggio, boolean vincitore){
		BigDecimal punteggioB = new BigDecimal(punteggio);
		BigDecimal punteggioTrascodificato;
		if (vincitore){
			punteggioTrascodificato = (BigDecimal.ONE.add(punteggioB.divide(MILLE))).setScale(3, BigDecimal.ROUND_UP);
		}else{
			punteggioTrascodificato = (punteggioB.divide(MILLE)).setScale(3, BigDecimal.ROUND_UP);
		}
		return punteggioTrascodificato.floatValue();
	}
	
	private BigDecimal calcolaPunteggioTrascodificatoB(BigDecimal punteggioB, boolean vincitore){
		BigDecimal punteggioTrascodificato;
		if (vincitore){
			punteggioTrascodificato = (BigDecimal.ONE.add(punteggioB.divide(MILLE))).setScale(3, BigDecimal.ROUND_UP);
		}else{
			punteggioTrascodificato = (punteggioB.divide(MILLE)).setScale(3, BigDecimal.ROUND_UP);
		}
		return punteggioTrascodificato;
	}
	
	public static void main (String[] s){
		Float punteggioF = 29.0f;
		BigDecimal punteggioB = new BigDecimal(punteggioF);
		BigDecimal punteggioTrascodificatoB = (punteggioB.divide(MILLE)); //.setScale(3, BigDecimal.ROUND_UP);
		Float punteggioTrascodificatoF = punteggioTrascodificatoB.floatValue();
		BigDecimal punteggioTotaleB = BigDecimal.ZERO.add(punteggioTrascodificatoB);
		BigDecimal punteggioTotaleBFromFloat = BigDecimal.ZERO.add(new BigDecimal(punteggioTrascodificatoF));
		
		System.out.println("punteggioF: "+punteggioF);
		System.out.println("punteggioB: "+punteggioB);
		System.out.println("punteggioTrascodificatoB: "+punteggioTrascodificatoB);
		System.out.println("punteggioTrascodificatoF: "+punteggioTrascodificatoF);
		System.out.println("punteggioTotaleB: "+punteggioTotaleB);
		System.out.println("punteggioTotaleBFromFloat: "+punteggioTotaleBFromFloat);
	}
	
	public String toString(){
		String result = "";
		if (giocatore != null){
			result += giocatore.toString();
		}
		if (getPunteggioB(false) != null){
			result += " "+getPunteggioB(false).toString();
		}
		return result;

	}
}
