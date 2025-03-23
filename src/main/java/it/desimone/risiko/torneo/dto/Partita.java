package it.desimone.risiko.torneo.dto;

import it.desimone.utils.MapUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Partita implements Cloneable{

	public static final int TAVOLO_PERFETTO = 4;
	public static final int TAVOLO_DA_5 = 5;
	public static final int TAVOLO_DA_3 = 3;
	public static final int TAVOLO_DA_2 = 2;
	private int numeroGiocatori;
	
	private int numeroTavolo;
	
	private Map<GiocatoreDTO,Float> tavolo = new LinkedHashMap<GiocatoreDTO,Float>();
	private Map<GiocatoreDTO,Object[]> datiAggiuntiviTavolo = new LinkedHashMap<GiocatoreDTO,Object[]>();
	private Map<GiocatoreDTO,Float> tavoloTrascodificato = null;
	private Map<GiocatoreDTO,Object> tavoloTrascodificatoB = null;
	
	private GiocatoreDTO vincitore;
	
	public Partita(){}
	
	public Partita(int numeroGiocatori){
		this.numeroGiocatori = numeroGiocatori;
	}
	
	public Partita(Partita partita){
		this.numeroTavolo = partita.getNumeroTavolo();
		this.numeroGiocatori = partita.getNumeroGiocatori();
		this.tavolo = new LinkedHashMap<GiocatoreDTO, Float>(partita.getTavolo());
	}
	
	public boolean addGiocatore(GiocatoreDTO giocatore, Float punteggio){
		boolean incompleto = isNotComplete();		
		if (incompleto){
			tavolo.put(giocatore, punteggio==null?0f:punteggio);
			//tavolo.put(giocatore, punteggio==null?getPunteggioCasualePerTest():punteggio);
		}
		return incompleto;
	}

	public void addDatiAggiuntiviGiocatore(GiocatoreDTO giocatore, Object[] dati){
		if (tavolo.containsKey(giocatore)){
			datiAggiuntiviTavolo.put(giocatore, dati);
		}
	}
	
	public Float setPunteggio(GiocatoreDTO giocatore, Float punteggio){
		Float result = null;
		if (tavolo.containsKey(giocatore)){
			result = tavolo.put(giocatore, punteggio==null?0f:punteggio);
		}
		return result;
	}
	
	public void setPunteggioTrascodificato(GiocatoreDTO giocatore, Float punteggio){
		if (tavolo.containsKey(giocatore)){
			if (tavoloTrascodificato == null){
				tavoloTrascodificato = (Map) ((LinkedHashMap)tavolo).clone();
			}
			tavoloTrascodificato.put(giocatore, punteggio==null?0f:punteggio);
		}
	}
	
	public void setPunteggioTrascodificatoB(GiocatoreDTO giocatore, BigDecimal punteggio){
		if (tavolo.containsKey(giocatore)){
			if (tavoloTrascodificatoB == null){
				tavoloTrascodificatoB = (Map) ((LinkedHashMap)tavolo).clone();
			}
			tavoloTrascodificatoB.put(giocatore, punteggio==null?0f:punteggio);
		}
	}
	
	public boolean removeGiocatore(GiocatoreDTO giocatore){
		return tavolo.remove(giocatore)!=null;
	}
	
	public int getNumeroTavolo() {
		return numeroTavolo;
	}
	
	public Float getPunteggio(GiocatoreDTO giocatore){
		return tavolo.get(giocatore);
	}
	
	public Float getPunteggioTrascodificato(GiocatoreDTO giocatore){
		return tavoloTrascodificato.get(giocatore);
	}
	
	public BigDecimal getPunteggioTrascodificatoB(GiocatoreDTO giocatore){
		return (BigDecimal) tavoloTrascodificatoB.get(giocatore);
	}
	
	public Float getPunteggioVincitore(){
		Float punteggioPrimo = getPunteggio(getGiocatoriOrdinatiPerPunteggio().iterator().next());
		return punteggioPrimo;
	}
	
	public boolean isVincitore(GiocatoreDTO giocatore){
		boolean result = false;
		if (giocatore != null){
			if (vincitore != null){
				result = vincitore.equals(giocatore);
			}else{
				Float punteggioGiocatore = getPunteggio(giocatore);
				if (punteggioGiocatore != null){
					Float punteggioPrimo = getPunteggioVincitore();
					result = punteggioGiocatore.equals(punteggioPrimo);
				}
			}
		}
		return result;
	}

	public int numeroVincitori(){
		int result = 1;
		Iterator<GiocatoreDTO> iterator = getGiocatoriOrdinatiPerPunteggio().iterator();
		Float punteggioPrimo = getPunteggio(iterator.next());
		while (iterator.hasNext()){
			if (punteggioPrimo.equals(getPunteggio(iterator.next()))){
				result++;
			}
		}
		return result;
	}
	
	
	public Map<GiocatoreDTO, Float> getTavolo() {
		return tavolo;
	}
	
	public Map<GiocatoreDTO, Object[]> getDatiAggiuntiviTavolo() {
		return datiAggiuntiviTavolo;
	}
	
	public boolean isNotComplete(){
		if (numeroGiocatori != 0){
			return tavolo.size() < numeroGiocatori;
		}else{
			return true; //tavolo.size() < TAVOLO_PERFETTO;
		}
	}

	public void setNumeroTavolo(int numeroTavolo) {
		this.numeroTavolo = numeroTavolo;
	}
	
	public Set<GiocatoreDTO> getGiocatori(){
		return tavolo.keySet();
	}
	
	public Set<GiocatoreDTO> getGiocatoriOrdinatiPerPunteggio(){
		return MapUtils.sortByValue(tavolo,true).keySet();
	}
	
	public int getPosizioneOld(GiocatoreDTO giocatore){
		int result = 0;
		int index = 0;
		for (GiocatoreDTO player: getGiocatoriOrdinatiPerPunteggio()){
			index++;
			if (player.equals(giocatore)){
				result = index;
				break;
			}
		}
		return result;
	}
	
	public int getPosizione(GiocatoreDTO giocatore){
		int posizione = 0;
		int index = 0;
		List<GiocatoreDTO> giocatoriOrdinatiPerPunteggio = new ArrayList<GiocatoreDTO>(getGiocatoriOrdinatiPerPunteggio());
		for (GiocatoreDTO player: giocatoriOrdinatiPerPunteggio){
			if (player.equals(giocatore)){
				for (int indice = index; indice >=0; indice--){
					if (getPunteggio(giocatore).equals(getPunteggio(giocatoriOrdinatiPerPunteggio.get(indice)))){
						posizione = indice+1;
					}
				}
				break;
			}
			index++;
		}
		return posizione;
	}
	
	public boolean eAlTavolo(GiocatoreDTO giocatore){
		boolean result = tavolo.containsKey(giocatore);
		return result;
	}
	
	public boolean isClubGiocatoreAlTavolo(GiocatoreDTO giocatore){
		Set<GiocatoreDTO> giocatoriAlTavolo = getGiocatori();
		boolean found = false;
		Iterator<GiocatoreDTO> iterator = giocatoriAlTavolo.iterator();
		while (iterator.hasNext() && !found){
			GiocatoreDTO giocatoreAlTavolo = iterator.next();
			found = giocatore.getClubProvenienza() != null && giocatoreAlTavolo.getClubProvenienza() != null && giocatore.getClubProvenienza().equals(giocatoreAlTavolo.getClubProvenienza());
		}
		return found;
	}
	
	public GiocatoreDTO isClubGiocatoreAlTavolo(ClubDTO club){
		Set<GiocatoreDTO> giocatoriAlTavolo = getGiocatori();
		boolean found = false;
		Iterator<GiocatoreDTO> iterator = giocatoriAlTavolo.iterator();
		GiocatoreDTO result = null;
		while (iterator.hasNext() && !found){
			GiocatoreDTO giocatoreAlTavolo = iterator.next();
			found = club != null && giocatoreAlTavolo.getClubProvenienza() != null && club.equals(giocatoreAlTavolo.getClubProvenienza());
			if (found){
				result = giocatoreAlTavolo;
				break;
			}
		}
		return result;
	}
	
	public String toString(){
		StringBuilder result = new StringBuilder();
		result.append("\nTavolo n°"+numeroTavolo+": "+numeroGiocatori+" giocatori\n");
		for (GiocatoreDTO giocatore: tavolo.keySet()){
			result.append(giocatore+" = "+tavolo.get(giocatore)+"\t");
		}
		return result.toString();
	}
	
	public String toStringForRanking(){
		Set<GiocatoreDTO> giocatori = getGiocatoriOrdinatiPerPunteggio();
		String result = "";
		for (GiocatoreDTO giocatore: giocatori){
			String cognome = giocatore.getCognome()!=null?giocatore.getCognome():"";
			String nome = giocatore.getNome()!=null?giocatore.getNome():"";
			String nick = giocatore.getNick()!=null?giocatore.getNick():"";
			String tripla = cognome+","+nome+","+nick;
			if (isVincitore(giocatore)){
				result = tripla+result;
			}else{
				result = result+","+tripla;
			}
		}
		return result;
	}

	public int getNumeroGiocatori() {
		return numeroGiocatori;
	}

	public void setNumeroGiocatori(int numeroGiocatori) {
		this.numeroGiocatori = numeroGiocatori;
	}

	public GiocatoreDTO getVincitore() {
		return vincitore;
	}

	public void setVincitore(GiocatoreDTO vincitore) {
		this.vincitore = vincitore;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + numeroTavolo;
		result = prime * result + ((tavolo == null) ? 0 : tavolo.hashCode());
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
		Partita other = (Partita) obj;
		if (numeroTavolo != other.numeroTavolo)
			return false;
		if (tavolo == null) {
			if (other.tavolo != null)
				return false;
		} else if (!tavolo.equals(other.tavolo))
			return false;
		return true;
	}

	public Object clone(){
		Partita clone = new Partita(this);
		
		return clone;
	}
	
}
