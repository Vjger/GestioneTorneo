package it.desimone.risiko.torneo.utils;

import it.desimone.risiko.torneo.dto.ClubDTO;
import it.desimone.risiko.torneo.dto.GiocatoreDTO;
import it.desimone.risiko.torneo.dto.Partita;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MatchAnalyzer {

	public static class MatchGrids{
		private Map<ClubDTO, Map<ClubDTO, Integer>> 			mapClubVsClub 			;
		private Map<GiocatoreDTO, Map<ClubDTO, Integer>> 		mapGiocatoreVsClub 		;
		private Map<GiocatoreDTO, Map<GiocatoreDTO, Integer>> 	mapGiocatoreVsGiocatore ;
		public Map<ClubDTO, Map<ClubDTO, Integer>> getMapClubVsClub() {
			return mapClubVsClub;
		}
		public void setMapClubVsClub(Map<ClubDTO, Map<ClubDTO, Integer>> mapClubVsClub) {
			this.mapClubVsClub = mapClubVsClub;
		}
		public Map<GiocatoreDTO, Map<ClubDTO, Integer>> getMapGiocatoreVsClub() {
			return mapGiocatoreVsClub;
		}
		public void setMapGiocatoreVsClub(
				Map<GiocatoreDTO, Map<ClubDTO, Integer>> mapGiocatoreVsClub) {
			this.mapGiocatoreVsClub = mapGiocatoreVsClub;
		}
		public Map<GiocatoreDTO, Map<GiocatoreDTO, Integer>> getMapGiocatoreVsGiocatore() {
			return mapGiocatoreVsGiocatore;
		}
		public void setMapGiocatoreVsGiocatore(
				Map<GiocatoreDTO, Map<GiocatoreDTO, Integer>> mapGiocatoreVsGiocatore) {
			this.mapGiocatoreVsGiocatore = mapGiocatoreVsGiocatore;
		}
	}
	
	public static class MatchAnomali{
		private int sogliaMinima;
		private int sogliaMassima;
		private AnomaliaConfrontiClub anomaliaValutata;
		private Map<ClubDTO, Map<ClubDTO, Integer>> matchClubVsClubAnomali;
		public int getSogliaMinima() {
			return sogliaMinima;
		}
		public void setSogliaMinima(int sogliaMinima) {
			this.sogliaMinima = sogliaMinima;
		}
		public int getSogliaMassima() {
			return sogliaMassima;
		}
		public void setSogliaMassima(int sogliaMassima) {
			this.sogliaMassima = sogliaMassima;
		}
		public AnomaliaConfrontiClub getAnomaliaValutata() {
			return anomaliaValutata;
		}
		public void setAnomaliaValutata(AnomaliaConfrontiClub anomaliaValutata) {
			this.anomaliaValutata = anomaliaValutata;
		}
		public Map<ClubDTO, Map<ClubDTO, Integer>> getMatchClubVsClubAnomali() {
			return matchClubVsClubAnomali;
		}
		public void setMatchClubVsClubAnomali(
				Map<ClubDTO, Map<ClubDTO, Integer>> matchClubVsClubAnomali) {
			this.matchClubVsClubAnomali = matchClubVsClubAnomali;
		}
		@Override
		public String toString() {
			return "MatchAnomali [sogliaMinima=" + sogliaMinima
					+ ", sogliaMassima=" + sogliaMassima
					+ ", anomaliaValutata=" + anomaliaValutata
					+ ", matchClubVsClubAnomali=" + matchClubVsClubAnomali
					+ "]";
		}
	}
	
	public static MatchGrids calcolaGriglie(List<Partita> partite){
		Map<ClubDTO, Map<ClubDTO, Integer>> 			mapClubVsClub 			= new HashMap<ClubDTO, Map<ClubDTO,Integer>>();
		Map<GiocatoreDTO, Map<ClubDTO, Integer>> 		mapGiocatoreVsClub 		= new HashMap<GiocatoreDTO, Map<ClubDTO,Integer>>();
		Map<GiocatoreDTO, Map<GiocatoreDTO, Integer>> 	mapGiocatoreVsGiocatore = new HashMap<GiocatoreDTO, Map<GiocatoreDTO,Integer>>();
		
		for (Partita partita: partite){
			GiocatoreDTO[] giocatoriInConfronto = partita.getGiocatori().toArray(new GiocatoreDTO[0]);		
			for (int i=0; i<giocatoriInConfronto.length-1; i++){
				for (int j=i+1; j<giocatoriInConfronto.length; j++){
					GiocatoreDTO giocatoreI = giocatoriInConfronto[i];
					GiocatoreDTO giocatoreJ = giocatoriInConfronto[j];
					
					mappaConfrontiTraClub(mapClubVsClub, giocatoreI, giocatoreJ);
					
					Map<ClubDTO, Integer> mappaI2 = mapGiocatoreVsClub.get(giocatoreI);
					if (mappaI2 == null){
						mappaI2 = new HashMap<ClubDTO,Integer>();
					}
					Integer confrontiI2 = mappaI2.get(giocatoreJ.getClubProvenienza());
					if (confrontiI2 == null){
						confrontiI2 = 0;
					}
					confrontiI2++;
					mappaI2.put(giocatoreJ.getClubProvenienza(), confrontiI2);
					
					Map<ClubDTO, Integer> mappaJ2 = mapGiocatoreVsClub.get(giocatoreJ);
					if (mappaJ2 == null){
						mappaJ2 = new HashMap<ClubDTO,Integer>();
					}
					Integer confrontiJ2 = mappaJ2.get(giocatoreI.getClubProvenienza());
					if (confrontiJ2 == null){
						confrontiJ2 = 0;
					}
					confrontiJ2++;
					mappaJ2.put(giocatoreI.getClubProvenienza(), confrontiJ2);
					
					mapGiocatoreVsClub.put(giocatoreI, mappaI2);
					mapGiocatoreVsClub.put(giocatoreJ, mappaJ2);
					
					Map<GiocatoreDTO, Integer> mappaI3 = mapGiocatoreVsGiocatore.get(giocatoreI);
					if (mappaI3 == null){
						mappaI3 = new HashMap<GiocatoreDTO,Integer>();
					}
					Integer confrontiI3 = mappaI3.get(giocatoreJ);
					if (confrontiI3 == null){
						confrontiI3 = 0;
					}
					confrontiI3++;
					mappaI3.put(giocatoreJ, confrontiI3);
					
					Map<GiocatoreDTO, Integer> mappaJ3 = mapGiocatoreVsGiocatore.get(giocatoreJ);
					if (mappaJ3 == null){
						mappaJ3 = new HashMap<GiocatoreDTO,Integer>();
					}
					Integer confrontiJ3 = mappaJ3.get(giocatoreI);
					if (confrontiJ3 == null){
						confrontiJ3 = 0;
					}
					confrontiJ3++;
					mappaJ3.put(giocatoreI, confrontiJ3);
					
					mapGiocatoreVsGiocatore.put(giocatoreI, mappaI3);
					mapGiocatoreVsGiocatore.put(giocatoreJ, mappaJ3);
				}
			}
		}
		
		MatchGrids matchGrids = new MatchGrids();
		matchGrids.setMapClubVsClub(mapClubVsClub);
		matchGrids.setMapGiocatoreVsClub(mapGiocatoreVsClub);
		matchGrids.setMapGiocatoreVsGiocatore(mapGiocatoreVsGiocatore);

		return matchGrids;
	}
	
	public static Map<ClubDTO, Map<ClubDTO, Integer>> calcolaGrigliaClubVsClub(List<Partita> partite){
		Map<ClubDTO, Map<ClubDTO, Integer>> mapClubVsClub = new HashMap<ClubDTO, Map<ClubDTO,Integer>>();
				
		for (Partita partita: partite){
			GiocatoreDTO[] giocatoriInConfronto = partita.getGiocatori().toArray(new GiocatoreDTO[0]);		
			for (int i=0; i<giocatoriInConfronto.length-1; i++){
				for (int j=i+1; j<giocatoriInConfronto.length; j++){
					GiocatoreDTO giocatoreI = giocatoriInConfronto[i];
					GiocatoreDTO giocatoreJ = giocatoriInConfronto[j];
					
					mappaConfrontiTraClub(mapClubVsClub, giocatoreI, giocatoreJ);										
				}
			}
		}
		//Vanno aggiunti gli zeri, cioè quei casi in cui due club non si affrontano mai.
		Set<ClubDTO> clubs = mapClubVsClub.keySet();
		
		for (ClubDTO club: clubs){
			Map<ClubDTO, Integer> scontri = mapClubVsClub.get(club);
			for (ClubDTO club2: clubs){
				if (!scontri.containsKey(club2)) {
					scontri.put(club2, 0);
				}
			}				
		}
		return mapClubVsClub;
	}
	
	public static enum AnomaliaConfrontiClub{
		UP, DOWN, BOTH
	}
	
	public static MatchAnomali calcolaConfrontiClubAnomali(List<Partita> partite, AnomaliaConfrontiClub anomaliaConfrontiClub){
		Map<ClubDTO, Map<ClubDTO, Integer>> mapClubVsClubAnomali = null;
		
		Map<ClubDTO, Map<ClubDTO, Integer>> mapClubVsClub = calcolaGrigliaClubVsClub(partite);
		
		int numeroClubInGioco = mapClubVsClub.size(); //Va migliorato: è vero solo nell'ipotesi che nessun club si ritiri dopo il 1° turno o che nessun club si aggiunga
		int minAvversari = 0;
		int maxAvversari = 0;
		
		Iterator<Map.Entry<ClubDTO, Map<ClubDTO, Integer>>> iterClub = mapClubVsClub.entrySet().iterator();
		while (iterClub.hasNext()){
		//for (ClubDTO club: mapClubVsClub.keySet()){//Andrebbe fatto solo sui club sicuramente in gioco nel turno in linea
			Map.Entry<ClubDTO, Map<ClubDTO, Integer>> entry = iterClub.next();
			ClubDTO club = entry.getKey();
			int numeroAvversariPerClub = 0;
			for (int value: mapClubVsClub.get(club).values()){
				numeroAvversariPerClub += value;
			}
			minAvversari = (numeroAvversariPerClub / numeroClubInGioco);
			if (numeroClubInGioco % numeroAvversariPerClub != 0){
				maxAvversari = (numeroAvversariPerClub / numeroClubInGioco) +1;
			}else{
				maxAvversari = (numeroAvversariPerClub / numeroClubInGioco) ;
			}

			Iterator<Map.Entry<ClubDTO, Integer>> iterScontriDiretti = mapClubVsClub.get(club).entrySet().iterator();
			//Map<ClubDTO, Integer> scontriDiretti = mapClubVsClub.get(club);
			while (iterScontriDiretti.hasNext()){
			//for (ClubDTO clubAvversario: scontriDiretti.keySet()){
				Map.Entry<ClubDTO, Integer> entryScontri = iterScontriDiretti.next();
				ClubDTO clubAvversario = entryScontri.getKey();
				if (club != null && !club.equals(clubAvversario)){
					Integer numeroScontriClubVsClub = entryScontri.getValue();
					if (!(numeroScontriClubVsClub > maxAvversari || numeroScontriClubVsClub < minAvversari)){
						iterScontriDiretti.remove();
					}else if (numeroScontriClubVsClub > maxAvversari && (anomaliaConfrontiClub == AnomaliaConfrontiClub.DOWN)){
						iterScontriDiretti.remove();
					}else if (numeroScontriClubVsClub < minAvversari && (anomaliaConfrontiClub == AnomaliaConfrontiClub.UP)){
						iterScontriDiretti.remove();
					}
				}else{
					iterScontriDiretti.remove();
				}
			}
			if (mapClubVsClub.get(club).isEmpty()){
				iterClub.remove();
			}
		}
		mapClubVsClubAnomali = mapClubVsClub;
		
		MatchAnomali matchAnomali = new MatchAnomali();
		matchAnomali.setSogliaMinima(minAvversari);
		matchAnomali.setSogliaMassima(maxAvversari);
		matchAnomali.setAnomaliaValutata(anomaliaConfrontiClub);
		matchAnomali.setMatchClubVsClubAnomali(mapClubVsClubAnomali);
		
		return matchAnomali;
	}
	
	private static void mappaConfrontiTraClub(Map<ClubDTO, Map<ClubDTO, Integer>> mapClubVsClub, GiocatoreDTO giocatoreI, GiocatoreDTO giocatoreJ){
		Map<ClubDTO, Integer> mappaI = mapClubVsClub.get(giocatoreI.getClubProvenienza());
		if (mappaI == null){
			mappaI = new HashMap<ClubDTO,Integer>();
		}
		Integer confrontiI = mappaI.get(giocatoreJ.getClubProvenienza());
		if (confrontiI == null){
			confrontiI = 0;
		}
		confrontiI++;
		mappaI.put(giocatoreJ.getClubProvenienza(), confrontiI);
		
		Map<ClubDTO, Integer> mappaJ = mapClubVsClub.get(giocatoreJ.getClubProvenienza());
		if (mappaJ == null){
			mappaJ = new HashMap<ClubDTO,Integer>();
		}
		Integer confrontiJ = mappaJ.get(giocatoreI.getClubProvenienza());
		if (confrontiJ == null){
			confrontiJ = 0;
		}
		confrontiJ++;
		mappaJ.put(giocatoreI.getClubProvenienza(), confrontiJ);
		
		mapClubVsClub.put(giocatoreI.getClubProvenienza(), mappaI);
		mapClubVsClub.put(giocatoreJ.getClubProvenienza(), mappaJ);
	}
	
	private static void printMatchGrids(MatchGrids matchGrids){
		Map<ClubDTO, Map<ClubDTO, Integer>> 			mapClubVsClub 			= matchGrids.getMapClubVsClub();
		Map<GiocatoreDTO, Map<ClubDTO, Integer>> 		mapGiocatoreVsClub 		= matchGrids.getMapGiocatoreVsClub();
		Map<GiocatoreDTO, Map<GiocatoreDTO, Integer>> 	mapGiocatoreVsGiocatore = matchGrids.getMapGiocatoreVsGiocatore();
		
		Set<ClubDTO> clubsSet = mapClubVsClub.keySet();
		List clubsList = new ArrayList<ClubDTO>(clubsSet);
		Collections.sort(clubsList);
		
		Set<GiocatoreDTO> giocatoriSet = mapGiocatoreVsClub.keySet();
		List giocatoriList = new ArrayList<GiocatoreDTO>(giocatoriSet);
		Collections.sort(giocatoriList);
		
		String firstRow = "\t";
		for (Object o: clubsList){
			ClubDTO club = (ClubDTO) o;
			firstRow += club.getDenominazione()+" ";
		}
		System.out.println(firstRow);
		for (Object o: clubsList){
			ClubDTO club = (ClubDTO) o;
			String otherRow = club.getDenominazione()+"\t";
			Map<ClubDTO,Integer> mappaI = mapClubVsClub.get(club);
			for (Object o2: clubsList){
				ClubDTO club2 = (ClubDTO) o2;
				Integer confronti = mappaI.get(club2);
				if (confronti == null) confronti = 0;
				otherRow += confronti+" ";
			}
			System.out.println(otherRow);
		}
		
		System.out.println();
		
		firstRow = "\t";
		for (Object o: clubsList){
			ClubDTO club = (ClubDTO) o;
			firstRow += club.getDenominazione()+" ";
		}
		System.out.println(firstRow);
		for (Object o: giocatoriList){
			GiocatoreDTO giocatore = (GiocatoreDTO) o;
			String otherRow = giocatore.getCognome()+giocatore.getClubProvenienza()+"\t";
			Map<ClubDTO,Integer> mappaI = mapGiocatoreVsClub.get(giocatore);
			for (Object o2: clubsList){
				ClubDTO club2 = (ClubDTO) o2;
				Integer confronti = mappaI.get(club2);
				if (confronti == null) confronti = 0;
				otherRow += confronti+" ";
			}
			System.out.println(otherRow);
		}
		
		System.out.println();
		
		firstRow = "\t";
		for (Object o: giocatoriList){
			GiocatoreDTO giocatore = (GiocatoreDTO) o;
			firstRow += giocatore.getCognome()+giocatore.getClubProvenienza()+"\t";
		}
		System.out.println(firstRow);
		for (Object o: giocatoriList){
			GiocatoreDTO giocatore = (GiocatoreDTO) o;
			String otherRow = giocatore.getCognome()+giocatore.getClubProvenienza()+"\t";
			Map<GiocatoreDTO,Integer> mappaI = mapGiocatoreVsGiocatore.get(giocatore);
			for (Object o2: giocatoriList){
				GiocatoreDTO giocatore2 = (GiocatoreDTO) o2;
				Integer confronti = mappaI.get(giocatore2);
				if (confronti == null) confronti = 0;
				otherRow += confronti+"\t";
			}
			System.out.println(otherRow);
		}
	}
}
