package it.desimone.risiko.torneo.scoreplayer;

import it.desimone.risiko.torneo.dto.ClubDTO;
import it.desimone.risiko.torneo.dto.GiocatoreDTO;
import it.desimone.risiko.torneo.dto.Partita;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ScoreTeamCNS implements ScoreTeam{

	private ClubDTO club;
	private int position;
	private int numeroVittorie = 0;
	private Map<GiocatoreDTO,List<Partita>> partitePerGiocatori = new HashMap<GiocatoreDTO,List<Partita>>();
	private List<Partita> partite = new ArrayList<Partita>();
	
	private static final BigDecimal UNO_V_25 = new BigDecimal(1.25); 
	private static final BigDecimal ZERO_V_75 = new BigDecimal(0.75); 
	private static final BigDecimal CENTO = new BigDecimal(100); 

	public ScoreTeamCNS(ClubDTO club){
		this.club = club;
	}
	
	public void addPartitaPerGiocatore(GiocatoreDTO giocatore, Partita partita){
		trascodificaPunteggio(partita, giocatore);
		
		partite.add(partita);
		List<Partita> partitePerGiocatore = partitePerGiocatori.get(giocatore);
		if (partitePerGiocatore == null){
			partitePerGiocatore = new ArrayList<Partita>();
		}
		partitePerGiocatore.add(partita);
		partitePerGiocatori.put(giocatore, partitePerGiocatore);
	}
	
	public BigDecimal getPunteggioB(int numeroPartiteValide){
		BigDecimal totale = BigDecimal.ZERO;
		Set<GiocatoreDTO> giocatori = getGiocatori();
		List<BigDecimal> risultati = new ArrayList<BigDecimal>();
		for (GiocatoreDTO giocatore: giocatori){
			List<Partita> partitePerGiocatore = getPartitePerGiocatore(giocatore);
			for (Partita partita: partitePerGiocatore){
				BigDecimal punteggio = partita==null?BigDecimal.ZERO:partita.getPunteggioTrascodificatoB(giocatore);
				risultati.add(punteggio);
			}
		}
		Collections.sort(risultati, Collections.reverseOrder());
		int risultatiValidi = Math.min(numeroPartiteValide, risultati.size());
		for (int index = 0; index < risultatiValidi; index++){
			totale = totale.add(risultati.get(index));
		}
		return totale;
	}
	
	public Float getPunteggio(int numeroPartiteValide){
		Float totale = 0f;
		Set<GiocatoreDTO> giocatori = getGiocatori();
		List<Float> risultati = new ArrayList<Float>();
		for (GiocatoreDTO giocatore: giocatori){
			List<Partita> partitePerGiocatore = getPartitePerGiocatore(giocatore);
			for (Partita partita: partitePerGiocatore){
				Float punteggio = partita==null?0f:partita.getPunteggioTrascodificato(giocatore);
				risultati.add(punteggio);
			}
		}
		Collections.sort(risultati, Collections.reverseOrder());
		int risultatiValidi = Math.min(numeroPartiteValide, risultati.size());
		for (int index = 0; index < risultatiValidi; index++){
			totale += risultati.get(index);
		}
		return totale;
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

	@Override
	public ClubDTO getTeam() {
		return club;
	}

	@Override
	public Set<GiocatoreDTO> getGiocatori() {
		return partitePerGiocatori.keySet();
	}

	@Override
	public void setPosition(int position) {
		this.position = position;
	}

	@Override
	public int getPosition() {
		return position;
	}

	@Override
	public List<Partita> getPartite() {
		return partite;
	}

	@Override
	public List<Partita> getPartitePerGiocatore(GiocatoreDTO giocatore) {
		return partitePerGiocatori.get(giocatore);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((club == null) ? 0 : club.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScoreTeamCNS other = (ScoreTeamCNS) obj;
		if (club == null) {
			if (other.club != null)
				return false;
		} else if (!club.equals(other.club))
			return false;
		return true;
	}
	
	
}
