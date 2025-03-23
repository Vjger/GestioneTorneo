package it.desimone.risiko.torneo.utils;

import it.desimone.risiko.torneo.dto.GiocatoreDTO;
import it.desimone.risiko.torneo.dto.Partita;
import it.desimone.risiko.torneo.scoreplayer.ScorePlayer;
import it.desimone.utils.MyException;
import it.desimone.utils.MyLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class TorneiUtils {
	
	public static boolean vincitoriUnici = Boolean.FALSE;
	
	public static List<GiocatoreDTO> fromScorePlayersToPlayers(List<ScorePlayer> scorePlayers){
		List<GiocatoreDTO> players = null;
		if (scorePlayers != null){
			players = new ArrayList<GiocatoreDTO>();
			for (ScorePlayer scorePlayer: scorePlayers){
				if (scorePlayer != null){
					players.add(scorePlayer.getGiocatore());
				}
			}
		}
		return players;
	}	

	public static boolean isGiocatorePartecipante(List<GiocatoreDTO> partecipanti, GiocatoreDTO giocatore){
		return partecipanti.contains(giocatore);
	}	
	
	public static boolean isPartecipante(List<ScorePlayer> scorePlayers, GiocatoreDTO giocatore){
		List<GiocatoreDTO> partecipanti = fromScorePlayersToPlayers(scorePlayers);
		return partecipanti.contains(giocatore);
	}	
	
	
	private static List<Partita> trovaPartiteConPiuVincitori(List<Partita> partite){
		List<Partita> result = new ArrayList<Partita>(partite);
		Iterator<Partita> iterator = result.iterator();
		while (iterator.hasNext()){
			Partita partita = iterator.next();
			if (partita == null || partita.numeroVincitori() == 1){
				iterator.remove();
			}
		}
		return result;
	}
	
	
	public static void checkPartiteConPiuVincitori(List<Partita> partite){
		if (!vincitoriUnici) return;
		if (partite != null){
			List<Partita> partiteConPiuVincitori = trovaPartiteConPiuVincitori(partite);
			if (partiteConPiuVincitori != null && !partiteConPiuVincitori.isEmpty()){
				StringBuilder buffer = new StringBuilder("C'è più di un vincitore nelle partite");
				for (Partita partita: partiteConPiuVincitori){
					buffer.append("\n"+partita);
				}
				throw new MyException(buffer.toString());
			}
		}
	}
	
	public static void checksPartiteConPiuVincitori(List<Partita[]> listaPartiteArray){
		if (!vincitoriUnici) return;
		if (listaPartiteArray != null && !listaPartiteArray.isEmpty()){
			for (Partita[] partite: listaPartiteArray){
				if (partite != null){
					checkPartiteConPiuVincitori(Arrays.asList(partite));
				}
			}
		}
	}
	
	public static void checksPartiteConPiuVincitori(Partita[] partiteArray){
		if (!vincitoriUnici) return;
		if (partiteArray != null){
			checkPartiteConPiuVincitori(Arrays.asList(partiteArray));
		}
	}
	
	
	public static List<GiocatoreDTO> listaDeiGiocatoriSconfittiOrdinataPerPosizioneAlTavoloEClassificaDopo2Partite(List<ScorePlayer> scorePlayersInClassifica, Partita[] partiteTurnoDaCuiRecuperare){
		class GiocatoreSconfitto{
			private GiocatoreDTO giocatore;
			private short posizioneAlTavolo;
			private int posizioneInClassificaDopoDuePartite;
			GiocatoreSconfitto(GiocatoreDTO giocatore, short posizioneAlTavolo, int posizioneInClassificaDopoDuePartite){
				this.giocatore = giocatore;
				this.posizioneAlTavolo = posizioneAlTavolo;
				this.posizioneInClassificaDopoDuePartite = posizioneInClassificaDopoDuePartite;
			}

			public GiocatoreDTO getGiocatore(){
				return giocatore;
			}
			public short getPosizioneAlTavolo(){
				return posizioneAlTavolo;
			}
			public int getPosizioneInClassificaDopoDuePartite(){
				return posizioneInClassificaDopoDuePartite;
			}
		}

		List<GiocatoreDTO> giocatoriInClassifica = TorneiUtils.fromScorePlayersToPlayers(scorePlayersInClassifica);
		
		List<GiocatoreSconfitto> giocatoriSconfittiAlTurnoEliminatorio = new ArrayList<GiocatoreSconfitto>();
		for (Partita partiteEliminatorie: partiteTurnoDaCuiRecuperare){
			Set<GiocatoreDTO> elencoGiocatoriOrdinatiPerPunteggio = partiteEliminatorie.getGiocatoriOrdinatiPerPunteggio();
			short posizione = 0;
			for (GiocatoreDTO giocatore: elencoGiocatoriOrdinatiPerPunteggio){
				posizione++;
				if (posizione != 1){
					int posizioneInClassifica = giocatoriInClassifica.indexOf(giocatore) + 1; 
					if (posizioneInClassifica != 0){ //caso in cui un giocatore sconfitto ai quarti si ritira e quindi non viene trovato in elenco
						GiocatoreSconfitto giocatoreSconfitto = new GiocatoreSconfitto(giocatore, posizione, posizioneInClassifica);
						giocatoriSconfittiAlTurnoEliminatorio.add(giocatoreSconfitto);
					}
				}
			}
		}

		Comparator<GiocatoreSconfitto> sconfittiComparator = new Comparator<GiocatoreSconfitto>(){
			public int compare(GiocatoreSconfitto sconfitto1, GiocatoreSconfitto sconfitto2) {
				int result = 0;

				if (sconfitto1.getPosizioneAlTavolo() < sconfitto2.getPosizioneAlTavolo()){
					result = -1;
				}else if (sconfitto1.getPosizioneAlTavolo() > sconfitto2.getPosizioneAlTavolo()){
					result = +1;
				}else{
					if (sconfitto1.getPosizioneInClassificaDopoDuePartite() < sconfitto2.getPosizioneInClassificaDopoDuePartite()){
						result = -1;
					}else{
						result = +1;
					}
				}							
				return result;
			}
		};

		List<GiocatoreDTO> giocatoriSconfitti = new ArrayList<GiocatoreDTO>();
		if (giocatoriSconfittiAlTurnoEliminatorio != null && !giocatoriSconfittiAlTurnoEliminatorio.isEmpty()){
			Collections.sort(giocatoriSconfittiAlTurnoEliminatorio, sconfittiComparator);
			for (GiocatoreSconfitto giocatoreSconfitto: giocatoriSconfittiAlTurnoEliminatorio){
				giocatoriSconfitti.add(giocatoreSconfitto.getGiocatore());
			}
		}

		MyLogger.getLogger().fine("Lista sconfitti ordinati: "+giocatoriSconfitti);
		return giocatoriSconfitti;
	}
}
