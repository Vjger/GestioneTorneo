package it.desimone.risiko.torneo.scoreplayer;

import it.desimone.risiko.torneo.dto.GiocatoreDTO;
import it.desimone.risiko.torneo.dto.Partita;

import java.math.BigDecimal;

public class ScorePlayerTorneoDominion extends AbstractScorePlayer{

	
	private int numeroVittorie = 0;
	private int numeroSecondiPosti = 0;
	private int numeroTerziPosti = 0;
	private int numeroOri = 0;
	private int numeroMonete = 0;
	private int numeroAzioni = 0;
	
	private Float massimoPunteggio = 0f;
	private GiocatoreDTO giocatore;
	
	private Partita[] partite;
	
	public ScorePlayerTorneoDominion(GiocatoreDTO giocatore, Partita[] partite){
		this.giocatore = giocatore;
		this.partite = new Partita[partite.length];
		
		for (int i=0; i < partite.length; i++){
			if (partite[i] != null){
				this.partite[i] = partite[i];
				
				/* Si presuppone che sia solo il punteggio a decidere la posizione: quindi in caso di parità inserire punteggi con decimali */
				switch (partite[i].getPosizione(giocatore)) {
				case 1:
					numeroVittorie++;
					break;
				case 2:
					numeroSecondiPosti++;
					break;
				case 3:
					numeroTerziPosti++;
					break;
				default:
					break;
				}
				Object[] datiAggiuntivi = partite[i].getDatiAggiuntiviTavolo().get(giocatore);
				numeroOri 		= numeroOri 	+ ((Integer) datiAggiuntivi[0]);
				numeroMonete 	= numeroMonete 	+ ((Integer) datiAggiuntivi[1]);
				numeroAzioni 	= numeroAzioni 	+ ((Integer) datiAggiuntivi[2]);
			}
		}
		for (Partita partita: partite){
			if (partita != null){
				trascodificaPunteggio(partita, giocatore);
			}
		}
	}
	
	public BigDecimal getPunteggioB(boolean conScarto){
		BigDecimal totale = BigDecimal.ZERO;
		for (Partita partita: partite){
			BigDecimal punteggio = partita==null?BigDecimal.ZERO:partita.getPunteggioTrascodificatoB(giocatore);
			totale = totale.add(punteggio);
		}
		return totale;
	}
	
	public Float getPunteggio(boolean conScarto){
		Float totale = 0f;
		for (Partita partita: partite){
			Float punteggio = partita==null?0f:partita.getPunteggioTrascodificato(giocatore);
			totale += punteggio;
		}
		return totale;
	}
	
	private void trascodificaPunteggio(Partita partita, GiocatoreDTO giocatore){
		Float punteggio = partita==null?0f:partita.getPunteggio(giocatore).intValue();
		BigDecimal punteggioB = partita==null?BigDecimal.ZERO:new BigDecimal(partita.getPunteggio(giocatore));
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

	public int getNumeroSecondiPosti() {
		return numeroSecondiPosti;
	}

	public int getNumeroTerziPosti() {
		return numeroTerziPosti;
	}

	public int getNumeroOri() {
		return numeroOri;
	}

	public int getNumeroMonete() {
		return numeroMonete;
	}

	public int getNumeroAzioni() {
		return numeroAzioni;
	}

	public Float getMassimoPunteggio() {
		return massimoPunteggio;
	}


}
