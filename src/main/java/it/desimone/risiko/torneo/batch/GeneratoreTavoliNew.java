package it.desimone.risiko.torneo.batch;

import it.desimone.risiko.torneo.dto.ClubDTO;
import it.desimone.risiko.torneo.dto.GiocatoreDTO;
import it.desimone.risiko.torneo.dto.Partita;
import it.desimone.risiko.torneo.dto.RegioneDTO;
import it.desimone.risiko.torneo.utils.MatchAnalyzer;
import it.desimone.risiko.torneo.utils.MatchAnalyzer.AnomaliaConfrontiClub;
import it.desimone.risiko.torneo.utils.MatchAnalyzer.MatchAnomali;
import it.desimone.risiko.torneo.utils.PrioritaSorteggio;
import it.desimone.risiko.torneo.utils.TavoliVuotiCreator;
import it.desimone.risiko.torneo.utils.TipoTavoli;
import it.desimone.risiko.torneo.utils.TipoTorneo;
import it.desimone.utils.ArrayUtils;
import it.desimone.utils.MapUtils;
import it.desimone.utils.MyException;
import it.desimone.utils.MyLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class GeneratoreTavoliNew {
	
	private static final short NUMERO_MASSIMO_ITERAZIONI = 15;
	
	private static List<Integer> listaTavoli = new ArrayList<Integer>();
	
	public static Partita[] generaPartite(List<GiocatoreDTO> giocatori, Partita[] partitePrecedenti, TipoTavoli tipoTavoli, List<PrioritaSorteggio> listaPriorita){
		MyLogger.getLogger().entering("GeneratoreTavoliNew", "generaPartite");
		Partita[] tavoliVuoti = TavoliVuotiCreator.genera(tipoTavoli, giocatori.size());
		
		Partita[] tavoliIniziali = getTavoliIniziali(giocatori, tavoliVuoti, partitePrecedenti, listaPriorita);
		
		MyLogger.getLogger().info("Estratti "+(tavoliIniziali==null?0:tavoliIniziali.length)+" tavoli iniziali");
		
		Partita[] partiteDaRedistribuire = tavoliIniziali;
		Partita[] partitePrimaDelCiclo = ArrayUtils.clonaPartite(tavoliIniziali);

		boolean partiteTrasformate = true;
		short numeroIterazioniPriorita = 0;
		while(partiteTrasformate && numeroIterazioniPriorita < NUMERO_MASSIMO_ITERAZIONI){
			numeroIterazioniPriorita++;
			MyLogger.getLogger().info("Inizio redistribuzione: Iterazione N° "+numeroIterazioniPriorita);
			for (PrioritaSorteggio priorita: listaPriorita){
				partiteDaRedistribuire = redistribuzionePartite(priorita, listaPriorita, partiteDaRedistribuire, partitePrecedenti);
			}
			partiteTrasformate = !Arrays.equals(partitePrimaDelCiclo, partiteDaRedistribuire);
			if (partiteTrasformate){
				partitePrimaDelCiclo = ArrayUtils.clonaPartite(partiteDaRedistribuire);
			}
		}

		
		if (TipoTavoli.tipoTavoloColMorto(tipoTavoli)){
			for (Partita partita: partiteDaRedistribuire){
				int numeroGiocatori = partita.getNumeroGiocatori();
				if (
					(tipoTavoli == TipoTavoli.DA_3_ED_EVENTUALMENTE_DA_2_COL_MORTO && numeroGiocatori == 2)
				||  (tipoTavoli == TipoTavoli.DA_2_ED_EVENTUALMENTE_DA_2_COL_MORTO && numeroGiocatori == 1)	
				||  (tipoTavoli == TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5_SE_UNO_SOLO_ALTRIMENTI_DA_3_COL_MORTO && numeroGiocatori == 3)		
				||  (tipoTavoli == TipoTavoli.DA_5_ED_EVENTUALMENTE_DA_4_COL_MORTO && numeroGiocatori == 4)
				){
					partita.setNumeroGiocatori(numeroGiocatori+1);
					partita.addGiocatore(GiocatoreDTO.FITTIZIO, null);
				}
			}
		}		
		MyLogger.getLogger().exiting("GeneratoreTavoliNew", "generaPartite");
		return partiteDaRedistribuire;
	}

	
	public static Partita[] generaPartiteConCasualitaTavoloDa5(List<GiocatoreDTO> giocatori, Partita[] partitePrecedenti, TipoTavoli tipoTavoli, List<PrioritaSorteggio> listaPriorita){
		MyLogger.getLogger().entering("GeneratoreTavoliNew", "generaPartiteConCasualitaTavoloDa5");
		
		if (tipoTavoli != TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5){
			throw new RuntimeException("Questo algoritmo funziona solo per tavoli da 4 e da 5");
		}
		
		/*
		 * Il Trucco è:
		 * 1) Prendere i tavoli vuoti e settare gli eventuali da 5 con 4 giocatori.
		 * 2) Sorteggiare tra i giocatori un numero di essi pari al numero di tavoli da 5 previsti: gli sventurati. 
		 * 	  Se è il secondo turno solo tra quelli che non ci hanno già giocato
		 * 3) Popolare i tavoli: saranno quindi tutti tavoli da 4 giocatori.
		 * 4) Ripristinare il numero giocatori a 5 ed effettuare la redistribuzione solita: essendo solo degli scambi anche i tavoli
		 *    da 5 resteranno con 4 giocatori
		 * 5) aggiungere gli sventurati ai tavoli da 5: se è il primo turno verificare il miglioramento delle incompatibilità
		 *    col solo eventuale scambio degli sventurati tra loro; se è il secondo, verificare la possibilità di poter scambiare
		 *    "gli "altri" tra loro e gli sventurati tra loro. Per fare ciò aggiungere un attributo "gruppo" nel dto GiocatoreDTO
		 *    e modificare il metodo verificaSeScambiabili rendendo possibile lo scambio solo di giocatori dello stesso gruppo
		 *    Nel calcolo della minimizzazione per club lo sventurato è però come se non esistesse cioè è come se non avesse club
		 *    e quindi non contribuisce alla possibile violazione per club.
		 *    In realtà forse basta lasciare il programma com'è e levare e poi riaggungere gli sventurati senza club. [FALSO perchè
		 *    posso migliorare un eventuale conflitto di club scambiando gli sventurati quindi, devo sapere qual è il loro club;
		 *    in altri termini non deve risultare il club al fine di un conflitto che comporti l'eventuale spostamento degli altri 4 
		 *    ma deve risultare al fine del loro spostamento]
		 *    Si deve fare quindi una doppia redistribuisciPartitePerCriterioClubDiversi
		 *    Al primo giro gli sventurati son tenuti bloccati al loro tavolo e risultano senza club mentre al secondo son liberi
		 *    di scambiarsi tra loro.
		 *    Forse basta aggiungere come priorità massima la possibilità che gli sventurati siano scambiabili tra loro
		 */
		
		Partita[] tavoliVuoti = TavoliVuotiCreator.genera(tipoTavoli, giocatori.size());
		
		Partita[] tavoliIniziali = getTavoliIniziali(giocatori, tavoliVuoti, partitePrecedenti, listaPriorita);
		
		Partita[] partiteDaRedistribuire = tavoliIniziali;
		for (PrioritaSorteggio priorita: listaPriorita){
			partiteDaRedistribuire = redistribuzionePartite(priorita, listaPriorita, partiteDaRedistribuire, partitePrecedenti);
		}
		
	
		MyLogger.getLogger().exiting("GeneratoreTavoliNew", "generaPartite");
		return partiteDaRedistribuire;
	}
	
	
	private static Partita[] getTavoliIniziali(List<GiocatoreDTO> giocatori, Partita[] tavoliVuoti, Partita[] partitePrecedenti, List<PrioritaSorteggio> prioritaSorteggio){
		MyLogger.getLogger().entering("GeneratoreTavoliNew", "getTavoliIniziali");
		Partita[] result = null;

		if (prioritaSorteggio.contains(PrioritaSorteggio.IMPEDITO_STESSO_CLUB_CONSIDERANDO_I_FISSI) || prioritaSorteggio.contains(PrioritaSorteggio.IMPEDITO_STESSO_CLUB_NON_CONSIDERANDO_I_FISSI)){
			assegnaGliSventuratiAiTavoliDa5(giocatori, tavoliVuoti, partitePrecedenti);
		}
		
		if (prioritaSorteggio.contains(PrioritaSorteggio.ALLA_SVIZZERA)){
			result = getTavoliAllaSvizzera(giocatori, tavoliVuoti);
		}else if (prioritaSorteggio.contains(PrioritaSorteggio.ALLA_GRECA) || prioritaSorteggio.contains(PrioritaSorteggio.VINCITORI_SEPARATI)){
			result = getTavoliAllaGreca(giocatori, tavoliVuoti, partitePrecedenti);
		}else if (prioritaSorteggio.contains(PrioritaSorteggio.IMPEDITA_STESSA_REGIONE) && giocatoriMultiploTavoli(giocatori,tavoliVuoti)){
			result = getTavoliSeparatiPerRegione(giocatori, tavoliVuoti);
		}else if ( (
						prioritaSorteggio.contains(PrioritaSorteggio.IMPEDITO_STESSO_CLUB) 
					||  prioritaSorteggio.contains(PrioritaSorteggio.IMPEDITO_STESSO_CLUB_NON_CONSIDERANDO_I_FISSI) 
					||  prioritaSorteggio.contains(PrioritaSorteggio.IMPEDITO_STESSO_CLUB_CONSIDERANDO_I_FISSI)
					) 
				&& giocatoriMultiploTavoli(giocatori,tavoliVuoti)){
			result = getTavoliSeparatiPerClub(giocatori, tavoliVuoti);
		}else{
			result = getTavoliCasuali(giocatori, tavoliVuoti);
		}
		MyLogger.getLogger().exiting("GeneratoreTavoliNew", "getTavoliIniziali");
		return result;
	}
	
	
	private static boolean giocatoriMultiploTavoli(List<GiocatoreDTO> giocatori, Partita[] tavoli){
		boolean result = true;
		if (giocatori != null && tavoli != null && tavoli.length > 0){
			result = ((giocatori.size()%tavoli.length) == 0);
		}
		return result;
	}
	
	private static void assegnaGliSventuratiAiTavoliDa5(List<GiocatoreDTO> giocatori, Partita[] tavoliVuoti, Partita[] partitePrecedenti){
		int numeroSventurati = 0;
		for (Partita tavoloVuoto: tavoliVuoti){
			if (tavoloVuoto.getNumeroGiocatori() == 5){numeroSventurati++;}
		}
		if (numeroSventurati > 0){
			MyLogger.getLogger().info("Devono essere estratti "+numeroSventurati+" sventurati");
			List<GiocatoreDTO> giocatoriDaEstrarre = new ArrayList<GiocatoreDTO>(giocatori);
			Collections.shuffle(giocatoriDaEstrarre);
			if (partitePrecedenti != null && partitePrecedenti.length > 0){
				Map<GiocatoreDTO, Integer> mappaGiocatoriPerPartecipazioniTavoloDa5 = new LinkedHashMap<GiocatoreDTO, Integer>();
				for (GiocatoreDTO giocatore: giocatoriDaEstrarre){
					int numeroPartecipazioniAlTavoloDa5 = numeroPartecipazioniTavoloda(giocatore, partitePrecedenti, 5);
					mappaGiocatoriPerPartecipazioniTavoloDa5.put(giocatore,numeroPartecipazioniAlTavoloDa5);
				}
				mappaGiocatoriPerPartecipazioniTavoloDa5 = MapUtils.sortByValue(mappaGiocatoriPerPartecipazioniTavoloDa5, false);
				giocatoriDaEstrarre = new ArrayList<GiocatoreDTO>(mappaGiocatoriPerPartecipazioniTavoloDa5.keySet());
			}
			
			for (int index = 1; index <= numeroSventurati; index++){
				GiocatoreDTO sventurato = giocatoriDaEstrarre.get(index-1);
				sventurato.setFissoAlTipoDiTavolo(true);
				boolean assegnato = tavoliVuoti[tavoliVuoti.length-index].addGiocatore(sventurato, null);
				if (assegnato){
					giocatori.remove(sventurato);
					MyLogger.getLogger().fine("Assegnato "+sventurato+" al tavolo "+(tavoliVuoti.length-index+1));
				}
			}
//			for (GiocatoreDTO sventurato: giocatoriDaEstrarre){
//				if (sventurato.getId() == 104){
//					sventurato.setFissoAlTipoDiTavolo(true);
//					boolean assegnato = tavoliVuoti[tavoliVuoti.length-1].addGiocatore(sventurato, null);
//					if (assegnato){
//						giocatori.remove(sventurato);
//						MyLogger.getLogger().info("Assegnato "+sventurato+" al tavolo "+(tavoliVuoti.length-1+1));
//					}	
//				}
//			}
//			for (GiocatoreDTO sventurato: giocatoriDaEstrarre){
//				if (sventurato.getId() == 102){
//					sventurato.setFissoAlTipoDiTavolo(true);
//					boolean assegnato = tavoliVuoti[tavoliVuoti.length-2].addGiocatore(sventurato, null);
//					if (assegnato){
//						giocatori.remove(sventurato);
//						MyLogger.getLogger().info("Assegnato "+sventurato+" al tavolo "+(tavoliVuoti.length-2+1));
//					}	
//				}
//			}
//			for (GiocatoreDTO sventurato: giocatoriDaEstrarre){
//				if (sventurato.getId() == 47){
//					sventurato.setFissoAlTipoDiTavolo(true);
//					boolean assegnato = tavoliVuoti[tavoliVuoti.length-3].addGiocatore(sventurato, null);
//					if (assegnato){
//						giocatori.remove(sventurato);
//						MyLogger.getLogger().info("Assegnato "+sventurato+" al tavolo "+(tavoliVuoti.length-3+1));
//					}	
//				}
//			}
		}
		
	}
	
	//TODO Rivedere questo algoritmo perchè probabilmente è una greca solo per il 2° turno.
	// Quel partitePrimoTurno è in realtà un partite precedenti
	private static Partita[] getTavoliAllaGreca(List<GiocatoreDTO> giocatoriTurnoN, Partita[] tavoli, Partita[] partiteTurno_N_1){
		MyLogger.getLogger().entering("GeneratoreTavoliNew", "getTavoliAllaGreca");
		int numeroTavoloIniziale = 0;
		for (Partita partitaPrimoTurno: partiteTurno_N_1){
			numeroTavoloIniziale++;
			int numeroTavoloInLinea = numeroTavoloIniziale;
			/* Significa che i tavoli del secondo Turno sono meno di quelli del primo */
			if (numeroTavoloIniziale > tavoli.length){
				break;
			}
			for (GiocatoreDTO giocatore: partitaPrimoTurno.getGiocatoriOrdinatiPerPunteggio()){
				if (giocatoriTurnoN.contains(giocatore)){
					if (tavoli[numeroTavoloInLinea-1].addGiocatore(giocatore, null)){
						giocatoriTurnoN.remove(giocatore);
					}
					numeroTavoloInLinea++;
					if (numeroTavoloInLinea > tavoli.length){
						numeroTavoloInLinea = 1;
					}
				}
			}
		}
		
		/* Gestione della rimanenza: giocatori che non hanno giocato il primo turno  o che sono rimasti
		 * fuori dalla ripartizione orizzontale per effetto del fatto che i tavoli del secondo turno sono 
		 * meno di quelli del primo */
		while (giocatoriTurnoN.size() >0){ 
			GiocatoreDTO giocatore = giocatoriTurnoN.get(0);
			for (Partita partita: tavoli){
				if (partita.addGiocatore(giocatore,null)){
					giocatoriTurnoN.remove(0);
					break;
				}
			}
		}		
		MyLogger.getLogger().exiting("GeneratoreTavoliNew", "getTavoliAllaGreca");
		return tavoli;
	}
	
	private static Partita[] getTavoliCasuali(List<GiocatoreDTO> giocatori, Partita[] tavoli){
		Collections.shuffle(giocatori);
		listaTavoli.clear();
		while (giocatori.size() >0){
			Integer numeroTavolo = getTavoloCasuale(tavoli.length);				
			boolean tavoloIncompleto = tavoli[numeroTavolo-1].addGiocatore(giocatori.get(0),null);
			if (tavoloIncompleto){
				giocatori.remove(0);
			}
		}
		return tavoli;
	}
	
	private static Partita[] getTavoliAllaSvizzera(List<GiocatoreDTO> giocatori, Partita[] tavoli){
		MyLogger.getLogger().entering("GeneratoreTavoliNew", "getTavoliAllaSvizzera");
		int numeroTavolo = 0;
		while (giocatori.size() >0){		
			boolean tavoloIncompleto = tavoli[numeroTavolo].addGiocatore(giocatori.get(0),null);
			if (tavoloIncompleto){
				giocatori.remove(0);
			}else{
				numeroTavolo++;
			}
		}
		MyLogger.getLogger().exiting("GeneratoreTavoliNew", "getTavoliAllaSvizzera");
		return tavoli;
	}
	
	private static Partita[] getTavoliSeparatiPerRegione(List<GiocatoreDTO> giocatori, Partita[] tavoli){
		MyLogger.getLogger().entering("GeneratoreTavoliNew", "getTavoliSeparatiPerRegione");
		List<List<GiocatoreDTO>> giocatoriPerRegione = getGiocatoriPerRegione1(giocatori);
		for (List<GiocatoreDTO> giocatoriStessaRegione: giocatoriPerRegione){
			getTavoliCasuali(giocatoriStessaRegione, tavoli);
		}
		MyLogger.getLogger().exiting("GeneratoreTavoliNew", "getTavoliSeparatiPerRegione");
		return tavoli;
	}
	

	private static Partita[] getTavoliSeparatiPerClub(List<GiocatoreDTO> giocatori, Partita[] tavoli){
		MyLogger.getLogger().entering("GeneratoreTavoliNew", "getTavoliSeparatiPerClub");
		List<List<GiocatoreDTO>> giocatoriPerClub = getGiocatoriPerClub(giocatori);
		for (List<GiocatoreDTO> giocatoriStessoClub: giocatoriPerClub){
			getTavoliCasuali(giocatoriStessoClub, tavoli);
		}
		MyLogger.getLogger().exiting("GeneratoreTavoliNew", "getTavoliSeparatiPerClub");
		return tavoli;
	}
		
	private static boolean checkCongruence(boolean inSensoStretto, Partita partita1, GiocatoreDTO giocatore1, Partita partita2, GiocatoreDTO giocatore2, Partita[] partitePrecedenti, PrioritaSorteggio priorita){
		boolean result = false;
		
		switch (priorita) {
		case MINIMIZZAZIONE_PARTECIPAZIONE_TAVOLO_DA_3:
			result = sonoIntercambiabiliInBaseAMinimizzazioneTavoliDaN(inSensoStretto, Partita.TAVOLO_DA_3, partita1, giocatore1, partita2, giocatore2, partitePrecedenti);
			break;
		case MINIMIZZAZIONE_PARTECIPAZIONE_TAVOLO_DA_5:
			result = sonoIntercambiabiliInBaseAMinimizzazioneTavoliDaN(inSensoStretto, Partita.TAVOLO_DA_5, partita1, giocatore1, partita2, giocatore2, partitePrecedenti);
			break;
		case MINIMIZZAZIONE_SCONTRI_DIRETTI:
			result = sonoIntercambiabiliInBaseAPartitePrecedenti(inSensoStretto, partita1, giocatore1, partita2, giocatore2, partitePrecedenti);
			break;
		case MINIMIZZAZIONE_SCONTRI_DIRETTI_TRA_CLUB:
			result = sonoIntercambiabiliInBaseAClubAffrontatiPrecedentiNew(inSensoStretto, partita1, giocatore1, partita2, giocatore2, partitePrecedenti);
			break;	
		case MINIMIZZAZIONE_SCONTRI_DIRETTI_GIOCATORE_CLUB:
			result = sonoIntercambiabiliInBaseAClubAffrontatoPrecedentemente(inSensoStretto, partita1, giocatore1, partita2, giocatore2, partitePrecedenti);
			break;				
		case VINCITORI_SEPARATI:
			result = sonoIntercambiabiliInBaseAVincitoriPrecedenti(inSensoStretto, partita1, giocatore1, partita2, giocatore2, partitePrecedenti);
			break;
		case IMPEDITO_STESSO_CLUB:
			result = sonoIntercambiabiliInBaseAClubDiversi(partita1, giocatore1, partita2, giocatore2, inSensoStretto);
			break;
		case IMPEDITO_STESSO_CLUB_CONSIDERANDO_I_FISSI:
			result = sonoIntercambiabiliInBaseAClubDiversi(partita1, giocatore1, partita2, giocatore2, inSensoStretto, true);
			break;
		case IMPEDITO_STESSO_CLUB_NON_CONSIDERANDO_I_FISSI:
			result = sonoIntercambiabiliInBaseAClubDiversi(partita1, giocatore1, partita2, giocatore2, inSensoStretto, false);
			break;			
		case IMPEDITA_STESSA_REGIONE:
			result = sonoIntercambiabiliInBaseARegioniDiverse(partita1, giocatore1, partita2, giocatore2, inSensoStretto);
			break;
		case ALLA_SVIZZERA:
			result = false;
			break;
		default:
			throw new IllegalArgumentException("Tipo di priorità non gestita: "+priorita);
		}
		return result;
	}
	
	private static boolean verificaSeScambiabili(PrioritaSorteggio priorita, List<PrioritaSorteggio> listaPriorita, Partita partita1, GiocatoreDTO giocatore1, Partita partita2, GiocatoreDTO giocatore2, Partita[] partitePrecedenti){
		
		boolean siPossonoScambiare = giocatore1.isFissoAlTipoDiTavolo() == giocatore2.isFissoAlTipoDiTavolo();
		
		/* Verifica che siano scambiabili in senso stretto per la Priorità che ho in canna*/
		if (siPossonoScambiare){
			siPossonoScambiare = checkCongruence(true, partita1, giocatore1, partita2, giocatore2, partitePrecedenti, priorita);
		}
		
		int indexOfPriority = listaPriorita.indexOf(priorita);
		
		/* Se sono scambiabili per la priorità in canna, verifico che siano scambiabili in senso largo anche per quelle di livello superiore */
		for (int index = indexOfPriority - 1; index >= 0 && siPossonoScambiare; index--){
			siPossonoScambiare = checkCongruence(false, partita1, giocatore1, partita2, giocatore2, partitePrecedenti, listaPriorita.get(index));
		}
		
		return siPossonoScambiare;
	}
	
	
	private static Partita[] redistribuzionePartite(PrioritaSorteggio priorita, List<PrioritaSorteggio> listaPriorita, Partita[] partite, Partita[] partitePrecedenti){
		Partita[] result = null;
		switch (priorita) {
		case IMPEDITA_STESSA_REGIONE:
			result = redistribuisciPartitePerCriterioRegioniDiverse(priorita, listaPriorita, partite, partitePrecedenti);
			break;
		case IMPEDITO_STESSO_CLUB:
			result = redistribuisciPartitePerCriterioClubDiversi(priorita, listaPriorita, partite, partitePrecedenti);
			break;
		case IMPEDITO_STESSO_CLUB_CONSIDERANDO_I_FISSI:
			result = redistribuisciPartitePerCriterioClubDiversiConsiderandoIFissiAlTavolo(priorita, listaPriorita, partite, partitePrecedenti, true);
			break;
		case IMPEDITO_STESSO_CLUB_NON_CONSIDERANDO_I_FISSI:
			result = redistribuisciPartitePerCriterioClubDiversiConsiderandoIFissiAlTavolo(priorita, listaPriorita, partite, partitePrecedenti, false);
			break;			
		case MINIMIZZAZIONE_PARTECIPAZIONE_TAVOLO_DA_3:
			result = redistribuisciPartitePerMinimizzarePartecipazioneTavoloda(priorita, listaPriorita, partite, partitePrecedenti, Partita.TAVOLO_DA_3);
			break;
		case MINIMIZZAZIONE_PARTECIPAZIONE_TAVOLO_DA_5:
			result = redistribuisciPartitePerMinimizzarePartecipazioneTavoloda(priorita, listaPriorita, partite, partitePrecedenti, Partita.TAVOLO_DA_5);
			break;
		case MINIMIZZAZIONE_SCONTRI_DIRETTI:
			result = redistribuisciPartiteMinimizzareScontriMultipli(priorita, listaPriorita, partite, partitePrecedenti);
			break;
		case VINCITORI_SEPARATI:
			result = redistribuisciPartiteMinimizzareScontriTraVincitori(priorita, listaPriorita, partite, partitePrecedenti);
			break;
		case ALLA_SVIZZERA:
			result = partite;
			break;
		case ALLA_GRECA:
			result = partite;
			break;
		case MINIMIZZAZIONE_SCONTRI_DIRETTI_TRA_CLUB:
			result = redistribuisciPartiteMinimizzareScontriMultipliTraClubTer(priorita, listaPriorita, partite, partitePrecedenti);
			break;
		case MINIMIZZAZIONE_SCONTRI_DIRETTI_GIOCATORE_CLUB:
			result = redistribuisciPartiteMinimizzareScontriGiocatoreVersoStessoClub(priorita, listaPriorita, partite, partitePrecedenti);
			break;
		default:
			throw new IllegalArgumentException("Tipo di priorità non gestita: "+priorita);
		}
		return result;
	}
	

	private static Partita[] redistribuisciPartiteMinimizzareScontriMultipli(PrioritaSorteggio priorita, List<PrioritaSorteggio> listaPriorita, Partita[] partite, Partita[] partitePrecedenti){
		short numeroIterazioni = 0; //non serve a nulla ma evita il rischio di loop.
		boolean sostituitoAlmenoUno = false;
		do{
			numeroIterazioni++;
			MyLogger.getLogger().info("Redistribuzione Partite per Minimizzare Scontri Multipli tra Giocatori - Iterazione n° "+numeroIterazioni);
			sostituitoAlmenoUno = false;
			for (int i=0; i < partite.length; i++){
				if (ilTavolohaGiocatoriCheSiSonoGiaAffrontati(partite[i], partitePrecedenti)){
					Set<GiocatoreDTO> giocatoriCheSisonoGiaAffrontati = giocatoriCheSiSonoGiaAffrontati(partite[i], partitePrecedenti);
					Iterator<GiocatoreDTO> iterator =  giocatoriCheSisonoGiaAffrontati.iterator();
					MyLogger.getLogger().fine("Nel tavolo "+partite[i].getNumeroTavolo()+" si sono già affrontati i giocatori "+giocatoriCheSisonoGiaAffrontati);
					boolean sostituito = false;
					/* Ciclo tutti i giocatori del tavolo che hanno già scontri diretti: se avviene uno scambio la lista si rigenera daccapo.*/
					while (iterator.hasNext() && !sostituito){
						GiocatoreDTO giocatore1 = iterator.next();
						for (int j=0; j < partite.length && !sostituito; j++){
							if (i != j){
								GiocatoreDTO[] giocatori = partite[j].getGiocatori().toArray(new GiocatoreDTO[0]);
								for (int k=0; k<giocatori.length && !sostituito; k++){
									GiocatoreDTO giocatore2 = giocatori[k];
									if (verificaSeScambiabili(priorita, listaPriorita, partite[i], giocatore1, partite[j], giocatore2, partitePrecedenti)){
										scambiaGiocatori(partite[i],giocatore1,partite[j],giocatore2);
										sostituito = true;
										if(!sostituitoAlmenoUno){sostituitoAlmenoUno = true;}
										/* Caso in cui il tavolo aveva più di una violazione */
										if (ilTavolohaGiocatoriCheSiSonoGiaAffrontati(partite[i], partitePrecedenti)){
											i--;
										}
									}
								}
							}
						}
					}
				}
			}
		}while(sostituitoAlmenoUno && numeroIterazioni < NUMERO_MASSIMO_ITERAZIONI);
		return partite;
	}
	
	private static Partita[] redistribuisciPartiteMinimizzareScontriMultipliTraClub(PrioritaSorteggio priorita, List<PrioritaSorteggio> listaPriorita, Partita[] partiteTurnoInCorso, Partita[] partitePrecedenti){
		short numeroIterazioni = 0; //non serve a nulla ma evita il rischio di loop.
		boolean sostituitoAlmenoUno = false;
		do{
			numeroIterazioni++;
			MyLogger.getLogger().info("Redistribuzione Partite per Minimizzare Scontri Multipli Tra Club - Iterazione n° "+numeroIterazioni);
			sostituitoAlmenoUno = false;
			for (int i=0; i < partiteTurnoInCorso.length; i++){
				if (ilTavolohaGiocatoriDiClubCheSiSonoGiaAffrontatiOSiStannoPerAffrontare(partiteTurnoInCorso[i], partiteTurnoInCorso, partitePrecedenti)){
					Set<GiocatoreDTO> giocatoriCheSisonoGiaAffrontati = giocatoriDiClubCheSiSonoGiaAffrontati(partiteTurnoInCorso[i], partiteTurnoInCorso, partitePrecedenti);
					Iterator<GiocatoreDTO> iterator =  giocatoriCheSisonoGiaAffrontati.iterator();
					MyLogger.getLogger().fine("Nel tavolo "+partiteTurnoInCorso[i].getNumeroTavolo()+" si sono già affrontati i club dei giocatori "+giocatoriCheSisonoGiaAffrontati);
					boolean sostituito = false;
					/* Ciclo tutti i giocatori del tavolo che hanno già scontri diretti: se avviene uno scambio la lista si rigenera daccapo.*/
					while (iterator.hasNext() && !sostituito){
						GiocatoreDTO giocatore1 = iterator.next();
						for (int j=0; j < partiteTurnoInCorso.length && !sostituito; j++){
							if (i != j){
								GiocatoreDTO[] giocatori = partiteTurnoInCorso[j].getGiocatori().toArray(new GiocatoreDTO[0]);
								for (int k=0; k<giocatori.length && !sostituito; k++){
									GiocatoreDTO giocatore2 = giocatori[k];
									List<Partita> listPartiteTurnoInCorso = new ArrayList<Partita>(Arrays.asList(partiteTurnoInCorso));
									Iterator<Partita> iteratorInCorso = listPartiteTurnoInCorso.iterator();
									while(iteratorInCorso.hasNext()){
										Partita p = iteratorInCorso.next();
										if (p.equals(partiteTurnoInCorso[i]) || p.equals(partiteTurnoInCorso[j])){
											iteratorInCorso.remove();
										}
									}
									Partita[] partite = ArrayUtils.concatenaPartite(listPartiteTurnoInCorso.toArray(new Partita[listPartiteTurnoInCorso.size()]), partitePrecedenti);
									if (verificaSeScambiabili(priorita, listaPriorita, partiteTurnoInCorso[i], giocatore1, partiteTurnoInCorso[j], giocatore2, partite)){
										scambiaGiocatori(partiteTurnoInCorso[i],giocatore1,partiteTurnoInCorso[j],giocatore2);
										sostituito = true;
										if(!sostituitoAlmenoUno){sostituitoAlmenoUno = true;}
										/* Caso in cui il tavolo aveva più di una violazione */
										if (ilTavolohaGiocatoriDiClubCheSiSonoGiaAffrontatiOSiStannoPerAffrontare(partiteTurnoInCorso[i], partiteTurnoInCorso, partitePrecedenti)){
											i--;
										}
									}
								}
							}
						}
					}
				}
			}
		}while(sostituitoAlmenoUno && numeroIterazioni < NUMERO_MASSIMO_ITERAZIONI);
		return partiteTurnoInCorso;
	}
	
	/**
	 * 1) Calcolo numero giocatori per squadra
	 * 2) Calcolo numero squadre in campo
	 * 3) Calcolo numero turno in corso
	 * 4) 1)+2)+3) ==> massimo e minimo scontri diretti tra club
	 * 5) Analisi degli scontri diretti: se si trova che si è sopra il massimo o sotto il minimo ricerca di scontro diretto e tentativo di scambio con altro partita
	 * @param priorita
	 * @param listaPriorita
	 * @param partiteTurnoInCorso
	 * @param partitePrecedenti
	 * @return
	 */
	private static Partita[] redistribuisciPartiteMinimizzareScontriMultipliTraClubNew(PrioritaSorteggio priorita, List<PrioritaSorteggio> listaPriorita, Partita[] partiteTurnoInCorso, Partita[] partitePrecedenti){
		short numeroIterazioni = 0; //non serve a nulla ma evita il rischio di loop.
		boolean sostituitoAlmenoUno = false;
		do{
			numeroIterazioni++;
			MyLogger.getLogger().info("Redistribuzione Partite per Minimizzare Scontri Multipli Tra Club - Iterazione n° "+numeroIterazioni);
			sostituitoAlmenoUno = false;
			
			Partita[] partite = ArrayUtils.concatenaPartite(partitePrecedenti, partiteTurnoInCorso);
			Map<ClubDTO, Map<ClubDTO, Integer>> mapClubVsClub = MatchAnalyzer.calcolaGrigliaClubVsClub(Arrays.asList(partite));

			int numeroClubInGioco = mapClubVsClub.size(); //Va migliorato: è vero solo nell'ipotesi che nessun club si ritiri dopo il 1° turno o che nessun club si aggiunga
			int numeroGiocatoriPerClub = partiteTurnoInCorso.length / numeroClubInGioco;

			for (ClubDTO club: mapClubVsClub.keySet()){//Andrebbe fatto solo sui club sicuramente in gioco nel turno in linea
				int numeroAvversariPerClub = 0;
				for (int value: mapClubVsClub.get(club).values()){
					numeroAvversariPerClub += value;
				}

				int minAvversari = (numeroAvversariPerClub / numeroClubInGioco);
				int maxAvversari = 0;
				if (numeroClubInGioco % numeroAvversariPerClub != 0){
					maxAvversari = (numeroAvversariPerClub / numeroClubInGioco) +1;
				}else{
					maxAvversari = (numeroAvversariPerClub / numeroClubInGioco) ;
				}

				Map<ClubDTO, Integer> scontriDiretti = mapClubVsClub.get(club);
				for (ClubDTO clubAvversario: scontriDiretti.keySet()){
					if (!club.equals(clubAvversario)){
						Integer numeroScontriClubVsClub = scontriDiretti.get(clubAvversario);
						if (numeroScontriClubVsClub > maxAvversari){
							//Cercare nelle partiteTurnoInCorso la coppia e scambiare uno dei due
							for (int i = 0; i < partiteTurnoInCorso.length; i++){
								Partita partitaTurnoInCorso = partiteTurnoInCorso[i];
								GiocatoreDTO giocatoreI = partitaTurnoInCorso.isClubGiocatoreAlTavolo(club);
								GiocatoreDTO giocatoreJ = partitaTurnoInCorso.isClubGiocatoreAlTavolo(clubAvversario);
								if (giocatoreI != null && giocatoreJ != null){
									MyLogger.getLogger().fine("Nel tavolo "+partitaTurnoInCorso.getNumeroTavolo()+" si sono già affrontati "+numeroScontriClubVsClub+" volte i club dei giocatori "+giocatoreI+" e "+giocatoreJ);
									boolean sostituito = false;
									/* Ciclo tutti i giocatori del tavolo che hanno già scontri diretti: se avviene uno scambio la lista si rigenera daccapo.*/
									for (int j=0; j < partiteTurnoInCorso.length && !sostituito; j++){
										if (i != j){
											GiocatoreDTO[] giocatori = partiteTurnoInCorso[j].getGiocatori().toArray(new GiocatoreDTO[0]);
											for (int k=0; k<giocatori.length && !sostituito; k++){
												GiocatoreDTO giocatore2 = giocatori[k];
												List<Partita> listPartiteTurnoInCorso = new ArrayList<Partita>(Arrays.asList(partiteTurnoInCorso));
												Iterator<Partita> iteratorInCorso = listPartiteTurnoInCorso.iterator();
												while(iteratorInCorso.hasNext()){
													Partita p = iteratorInCorso.next();
													if (p.equals(partiteTurnoInCorso[i]) || p.equals(partiteTurnoInCorso[j])){
														iteratorInCorso.remove();
													}
												}
												Partita[] partiteRidotte = ArrayUtils.concatenaPartite(listPartiteTurnoInCorso.toArray(new Partita[listPartiteTurnoInCorso.size()]), partitePrecedenti);
												if (verificaSeScambiabili(priorita, listaPriorita, partiteTurnoInCorso[i], giocatoreI, partiteTurnoInCorso[j], giocatore2, partiteRidotte)){
													scambiaGiocatori(partiteTurnoInCorso[i],giocatoreI,partiteTurnoInCorso[j],giocatore2);
													sostituito = true;
													if(!sostituitoAlmenoUno){sostituitoAlmenoUno = true;}
												}else if (verificaSeScambiabili(priorita, listaPriorita, partiteTurnoInCorso[i], giocatoreJ, partiteTurnoInCorso[j], giocatore2, partiteRidotte)){
													scambiaGiocatori(partiteTurnoInCorso[i],giocatoreJ,partiteTurnoInCorso[j],giocatore2);
													sostituito = true;
													if(!sostituitoAlmenoUno){sostituitoAlmenoUno = true;}
												}
											}
										}
									}
								}
							}

						}else if (numeroScontriClubVsClub < minAvversari){
							//Andrebbe "creato" un accoppiamento
						}
					}
				}
			}
		}while(sostituitoAlmenoUno && numeroIterazioni < NUMERO_MASSIMO_ITERAZIONI);
		return partiteTurnoInCorso;
	}
	
	/**
	 * 1) Calcolo numero giocatori per squadra
	 * 2) Calcolo numero squadre in campo
	 * 3) Calcolo numero turno in corso
	 * 4) 1)+2)+3) ==> massimo e minimo scontri diretti tra club
	 * 5) Analisi degli scontri diretti: se si trova che si è sopra il massimo o sotto il minimo ricerca di scontro diretto e tentativo di scambio con altro partita
	 * @param priorita
	 * @param listaPriorita
	 * @param partiteTurnoInCorso
	 * @param partitePrecedenti
	 * @return
	 */
	private static Partita[] redistribuisciPartiteMinimizzareScontriMultipliTraClubTer(PrioritaSorteggio priorita, List<PrioritaSorteggio> listaPriorita, Partita[] partiteTurnoInCorso, Partita[] partitePrecedenti){
		short numeroIterazioni = 0; //non serve a nulla ma evita il rischio di loop.
		boolean sostituitoAlmenoUno = false;
		do{
			numeroIterazioni++;
			MyLogger.getLogger().info("Redistribuzione Partite per Minimizzare Scontri Multipli Tra Club - Iterazione n° "+numeroIterazioni);
			sostituitoAlmenoUno = false;
			
			Partita[] partite = ArrayUtils.concatenaPartite(partitePrecedenti, partiteTurnoInCorso);
			
			MatchAnomali matchAnomaliUP = MatchAnalyzer.calcolaConfrontiClubAnomali(Arrays.asList(partite), AnomaliaConfrontiClub.UP);
			Map<ClubDTO, Map<ClubDTO, Integer>> confrontiAnomaliUP = matchAnomaliUP.getMatchClubVsClubAnomali();	
			
			MyLogger.getLogger().info("Confronti Anomali UP "+confrontiAnomaliUP);

			for (ClubDTO club: confrontiAnomaliUP.keySet()){//Andrebbe fatto solo sui club sicuramente in gioco nel turno in linea
				Map<ClubDTO, Integer> scontriDiretti = confrontiAnomaliUP.get(club);
				for (ClubDTO clubAvversario: scontriDiretti.keySet()){
					Integer numeroScontriClubVsClub = scontriDiretti.get(clubAvversario);
					//Cercare nelle partiteTurnoInCorso la coppia e scambiare uno dei due
					for (int i = 0; i < partiteTurnoInCorso.length; i++){
						Partita partitaTurnoInCorso = partiteTurnoInCorso[i];
						GiocatoreDTO giocatoreI = partitaTurnoInCorso.isClubGiocatoreAlTavolo(club);
						GiocatoreDTO giocatoreJ = partitaTurnoInCorso.isClubGiocatoreAlTavolo(clubAvversario);
						if (giocatoreI != null && giocatoreJ != null){
							MyLogger.getLogger().fine("Nel tavolo "+partitaTurnoInCorso.getNumeroTavolo()+" si sono già affrontati "+numeroScontriClubVsClub+" volte i club "+club+" e "+clubAvversario);
							boolean sostituito = false;
							/* Ciclo tutti i giocatori del tavolo che hanno già scontri diretti: se avviene uno scambio la lista si rigenera daccapo.*/
							for (int j=0; j < partiteTurnoInCorso.length && !sostituito; j++){
								if (i != j){
									GiocatoreDTO[] giocatori = partiteTurnoInCorso[j].getGiocatori().toArray(new GiocatoreDTO[0]);
									for (int k=0; k<giocatori.length && !sostituito; k++){
										GiocatoreDTO giocatore2 = giocatori[k];
										List<Partita> listPartiteTurnoInCorso = new ArrayList<Partita>(Arrays.asList(partiteTurnoInCorso));
										Iterator<Partita> iteratorInCorso = listPartiteTurnoInCorso.iterator();
										while(iteratorInCorso.hasNext()){
											Partita p = iteratorInCorso.next();
											if (p.equals(partiteTurnoInCorso[i]) || p.equals(partiteTurnoInCorso[j])){
												iteratorInCorso.remove();
											}
										}
										Partita[] partiteRidotte = ArrayUtils.concatenaPartite(listPartiteTurnoInCorso.toArray(new Partita[listPartiteTurnoInCorso.size()]), partitePrecedenti);
										if (verificaSeScambiabili(priorita, listaPriorita, partiteTurnoInCorso[i], giocatoreI, partiteTurnoInCorso[j], giocatore2, partiteRidotte)){
											scambiaGiocatori(partiteTurnoInCorso[i],giocatoreI,partiteTurnoInCorso[j],giocatore2);
											sostituito = true;
											if(!sostituitoAlmenoUno){sostituitoAlmenoUno = true;}
										}else if (verificaSeScambiabili(priorita, listaPriorita, partiteTurnoInCorso[i], giocatoreJ, partiteTurnoInCorso[j], giocatore2, partiteRidotte)){
											scambiaGiocatori(partiteTurnoInCorso[i],giocatoreJ,partiteTurnoInCorso[j],giocatore2);
											sostituito = true;
											if(!sostituitoAlmenoUno){sostituitoAlmenoUno = true;}
										}
									}
								}
							}
						}
					}

				}
			}
			
			MatchAnomali matchAnomaliDOWN = MatchAnalyzer.calcolaConfrontiClubAnomali(Arrays.asList(partite), AnomaliaConfrontiClub.DOWN);
			Map<ClubDTO, Map<ClubDTO, Integer>> confrontiAnomaliDOWN = matchAnomaliDOWN.getMatchClubVsClubAnomali();	
			
			MyLogger.getLogger().info("Confronti Anomali DOWN "+confrontiAnomaliUP);

			for (ClubDTO club: confrontiAnomaliDOWN.keySet()){//Andrebbe fatto solo sui club sicuramente in gioco nel turno in linea
				Map<ClubDTO, Integer> scontriDiretti = confrontiAnomaliDOWN.get(club);
				for (ClubDTO clubAvversario: scontriDiretti.keySet()){
					for (int i = 0; i < partiteTurnoInCorso.length; i++){
						Partita partitaTurnoInCorso = partiteTurnoInCorso[i];
						GiocatoreDTO giocatoreI = partitaTurnoInCorso.isClubGiocatoreAlTavolo(club);
						if (giocatoreI != null){
							boolean sostituito = false;
							/* Ciclo tutti i giocatori del tavolo che hanno già scontri diretti: se avviene uno scambio la lista si rigenera daccapo.*/
							for (int j=0; j < partiteTurnoInCorso.length && !sostituito; j++){
								if (i != j){
									GiocatoreDTO giocatoreJ = partiteTurnoInCorso[j].isClubGiocatoreAlTavolo(clubAvversario);
									if (giocatoreJ != null){
										GiocatoreDTO[] giocatori = partiteTurnoInCorso[j].getGiocatori().toArray(new GiocatoreDTO[0]);
										for (int k=0; k<giocatori.length && !sostituito; k++){
											GiocatoreDTO giocatore2 = giocatori[k];
											if (!giocatore2.equals(giocatoreJ)){
												MyLogger.getLogger().fine("Nel tavolo "+partiteTurnoInCorso[j].getNumeroTavolo()+" si fanno affrontare i club "+club+" e "+clubAvversario);
												List<Partita> listPartiteTurnoInCorso = new ArrayList<Partita>(Arrays.asList(partiteTurnoInCorso));
												Iterator<Partita> iteratorInCorso = listPartiteTurnoInCorso.iterator();
												while(iteratorInCorso.hasNext()){
													Partita p = iteratorInCorso.next();
													if (p.equals(partiteTurnoInCorso[i]) || p.equals(partiteTurnoInCorso[j])){
														iteratorInCorso.remove();
													}
												}
												Partita[] partiteRidotte = ArrayUtils.concatenaPartite(listPartiteTurnoInCorso.toArray(new Partita[listPartiteTurnoInCorso.size()]), partitePrecedenti);
												if (verificaSeScambiabili(priorita, listaPriorita, partiteTurnoInCorso[i], giocatoreI, partiteTurnoInCorso[j], giocatore2, partiteRidotte)){
													scambiaGiocatori(partiteTurnoInCorso[i],giocatoreI,partiteTurnoInCorso[j],giocatore2);
													sostituito = true;
													if(!sostituitoAlmenoUno){sostituitoAlmenoUno = true;}
												}
											}
										}
									}
								}
							}
						}
					}

				}
			}
		}while(sostituitoAlmenoUno && numeroIterazioni < NUMERO_MASSIMO_ITERAZIONI);
		return partiteTurnoInCorso;
	}
	
	private static Partita[] redistribuisciPartiteDiminuireScontriMultipliTraClub(PrioritaSorteggio priorita, List<PrioritaSorteggio> listaPriorita, Partita[] partiteTurnoInCorso, Partita[] partitePrecedenti, ClubDTO club1, ClubDTO club2){
		short numeroIterazioni = 0; //non serve a nulla ma evita il rischio di loop.
		boolean sostituitoAlmenoUno = false;
		do{
			numeroIterazioni++;
			MyLogger.getLogger().info("Redistribuzione Partite per Minimizzare Scontri Multipli Tra Club - Iterazione n° "+numeroIterazioni);
			sostituitoAlmenoUno = false;
			MyLogger.getLogger().fine("Si sono già affrontati troppe volte i club "+club1+" e "+club2);
			for (int i=0; i < partiteTurnoInCorso.length; i++){
					boolean sostituito = false;
					Partita partitaI = partiteTurnoInCorso[i];
					GiocatoreDTO giocatoreI1 = partitaI.isClubGiocatoreAlTavolo(club1);
					GiocatoreDTO giocatoreI2 = partitaI.isClubGiocatoreAlTavolo(club2);
					if (giocatoreI1 != null && giocatoreI2 != null)
						for (int j=0; j < partiteTurnoInCorso.length && !sostituito; j++){
							if (i != j){
								Partita partitaJ = partiteTurnoInCorso[j];
								if (partitaJ.isClubGiocatoreAlTavolo(club1) == null && partitaJ.isClubGiocatoreAlTavolo(club2) == null){
								GiocatoreDTO[] giocatori = partiteTurnoInCorso[j].getGiocatori().toArray(new GiocatoreDTO[0]);
								for (int k=0; k<giocatori.length && !sostituito; k++){
									GiocatoreDTO giocatoreJ = giocatori[k];
									List<Partita> listPartiteTurnoInCorso = new ArrayList<Partita>(Arrays.asList(partiteTurnoInCorso));
									Iterator<Partita> iteratorInCorso = listPartiteTurnoInCorso.iterator();
									while(iteratorInCorso.hasNext()){
										Partita p = iteratorInCorso.next();
										if (p.equals(partiteTurnoInCorso[i]) || p.equals(partiteTurnoInCorso[j])){
											iteratorInCorso.remove();
										}
									}
									Partita[] partite = ArrayUtils.concatenaPartite(listPartiteTurnoInCorso.toArray(new Partita[listPartiteTurnoInCorso.size()]), partitePrecedenti);
									if (verificaSeScambiabili(priorita, listaPriorita, partiteTurnoInCorso[i], giocatoreI1, partiteTurnoInCorso[j], giocatoreJ, partite)){
										scambiaGiocatori(partiteTurnoInCorso[i],giocatoreI1,partiteTurnoInCorso[j],giocatoreJ);
										sostituito = true;
										if(!sostituitoAlmenoUno){sostituitoAlmenoUno = true;}
									}
									if (!sostituito){
										if (verificaSeScambiabili(priorita, listaPriorita, partiteTurnoInCorso[i], giocatoreI2, partiteTurnoInCorso[j], giocatoreJ, partite)){
											scambiaGiocatori(partiteTurnoInCorso[i],giocatoreI2,partiteTurnoInCorso[j],giocatoreJ);
											sostituito = true;
											if(!sostituitoAlmenoUno){sostituitoAlmenoUno = true;}
										}
									}
								}
								}
							}
						}
				}

		}while(sostituitoAlmenoUno && numeroIterazioni < NUMERO_MASSIMO_ITERAZIONI);
		return partiteTurnoInCorso;
	}
	
	private static Partita[] redistribuisciPartiteMinimizzareScontriGiocatoreVersoStessoClub(PrioritaSorteggio priorita, List<PrioritaSorteggio> listaPriorita, Partita[] partite, Partita[] partitePrecedenti){
		short numeroIterazioni = 0; //non serve a nulla ma evita il rischio di loop.
		boolean sostituitoAlmenoUno = false;
		do{
			numeroIterazioni++;
			MyLogger.getLogger().info("Redistribuzione Partite per Minimizzare Scontri Giocatore Verso Stesso Club - Iterazione n° "+numeroIterazioni);
			sostituitoAlmenoUno = false;
			for (int i=0; i < partite.length; i++){
				if (ilTavolohaGiocatoriCheHannoGiaAffrontatoQuelClub(partite[i], partitePrecedenti)){
					Set<GiocatoreDTO> giocatoriCheHannoGiaAffrontatoQuelClub = giocatoriCheHannoGiaAffrontatoQuelClub(partite[i], partitePrecedenti);
					Iterator<GiocatoreDTO> iterator =  giocatoriCheHannoGiaAffrontatoQuelClub.iterator();
					MyLogger.getLogger().fine("Nel tavolo "+partite[i].getNumeroTavolo()+" ha già affrontato almeno uno dei club i giocatori "+giocatoriCheHannoGiaAffrontatoQuelClub);
					boolean sostituito = false;
					/* Ciclo tutti i giocatori del tavolo che hanno già scontri diretti: se avviene uno scambio la lista si rigenera daccapo.*/
					while (iterator.hasNext() && !sostituito){
						GiocatoreDTO giocatore1 = iterator.next();
						for (int j=0; j < partite.length && !sostituito; j++){
							if (i != j){
								GiocatoreDTO[] giocatori = partite[j].getGiocatori().toArray(new GiocatoreDTO[0]);
								for (int k=0; k<giocatori.length && !sostituito; k++){
									GiocatoreDTO giocatore2 = giocatori[k];
									if (verificaSeScambiabili(priorita, listaPriorita, partite[i], giocatore1, partite[j], giocatore2, partitePrecedenti)){
										scambiaGiocatori(partite[i],giocatore1,partite[j],giocatore2);
										sostituito = true;
										if(!sostituitoAlmenoUno){sostituitoAlmenoUno = true;}
										/* Caso in cui il tavolo aveva più di una violazione */
										if (ilTavolohaGiocatoriCheHannoGiaAffrontatoQuelClub(partite[i], partitePrecedenti)){
											i--;
										}
									}
								}
							}
						}
					}
				}
			}
		}while(sostituitoAlmenoUno && numeroIterazioni < NUMERO_MASSIMO_ITERAZIONI);
		return partite;
	}
	
	public static Partita[] redistribuisciPartiteMinimizzareScontriTraVincitori(PrioritaSorteggio priorita, List<PrioritaSorteggio> listaPriorita, Partita[] partite, Partita[] partitePrecedenti){
		short numeroIterazioni = 0; //non serve a nulla ma evita il rischio di loop.
		boolean sostituitoAlmenoUno = false;
		do{
			numeroIterazioni++;
			MyLogger.getLogger().info("Redistribuzione Partite per Minimizzare Scontri Tra Vincitori - Iterazione n° "+numeroIterazioni);
			sostituitoAlmenoUno = false;
			for (int i=0; i < partite.length; i++){
				if (ilTavolohaPiuVincitori(partite[i], partitePrecedenti)){
					Set<GiocatoreDTO> giocatoriCheHannoGiaVinto = giocatoriCheHannoGiaVinto(partite[i], partitePrecedenti);
					Iterator<GiocatoreDTO> iterator =  giocatoriCheHannoGiaVinto.iterator();
					MyLogger.getLogger().fine("Nel tavolo "+partite[i].getNumeroTavolo()+" hanno già vinto i giocatori "+giocatoriCheHannoGiaVinto);
					boolean sostituito = false;
					/* Ciclo tutti i giocatori del tavolo che hanno già vinto: se avviene uno scambio la lista si rigenera daccapo.*/
					while (iterator.hasNext() && !sostituito){
						GiocatoreDTO giocatore1 = iterator.next();
						for (int j=0; j < partite.length && !sostituito; j++){
							if (i != j){
								GiocatoreDTO[] giocatori = partite[j].getGiocatori().toArray(new GiocatoreDTO[0]);
								for (int k=0; k<giocatori.length && !sostituito; k++){
									GiocatoreDTO giocatore2 = giocatori[k];
									if (verificaSeScambiabili(priorita, listaPriorita, partite[i], giocatore1, partite[j], giocatore2, partitePrecedenti)){
										scambiaGiocatori(partite[i],giocatore1,partite[j],giocatore2);
										sostituito = true;
										if(!sostituitoAlmenoUno){sostituitoAlmenoUno = true;}
										/* Caso in cui il tavolo aveva più di una violazione */
										if (ilTavolohaPiuVincitori(partite[i], partitePrecedenti)){
											i--;
										}
									}
								}
							}
						}
					}
				}
			}
		}while(sostituitoAlmenoUno && numeroIterazioni < NUMERO_MASSIMO_ITERAZIONI);
		return partite;
	}
	
	private static Partita[] redistribuisciPartitePerCriterioRegioniDiverse(PrioritaSorteggio priorita, List<PrioritaSorteggio> listaPriorita, Partita[] partite, Partita[] partitePrecedenti){
		short numeroIterazioni = 0; //non serve a nulla ma evita il rischio di loop.
		boolean sostituitoAlmenoUno = false;
		do{
			numeroIterazioni++;
			MyLogger.getLogger().info("Redistribuzione Partite Per Criterio Regioni Diverse - Iterazione n° "+numeroIterazioni);
			sostituitoAlmenoUno = false;
			for (int i=0; i < partite.length; i++){
				Set<GiocatoreDTO> giocatoriAnomali = nonRispettaCriterioStessaRegione(partite[i]);
				Iterator<GiocatoreDTO> iterator = giocatoriAnomali.iterator();
				while (iterator.hasNext() && giocatoriAnomali.size() > 1){				
					GiocatoreDTO giocatore1 = iterator.next();
					MyLogger.getLogger().fine("Nel tavolo "+partite[i].getNumeroTavolo()+" il giocatore "+giocatore1+ " incontrerebbe persone della stessa regione");
					boolean sostituito = false;
					for (int j=0; j < partite.length && !sostituito; j++){
						if (i != j){
							GiocatoreDTO[] giocatori = partite[j].getGiocatori().toArray(new GiocatoreDTO[0]);
							for (int k=0; k<giocatori.length && !sostituito; k++){
								GiocatoreDTO giocatore2 = giocatori[k];
								if (verificaSeScambiabili(priorita, listaPriorita, partite[i], giocatore1, partite[j], giocatore2, partitePrecedenti)){
									scambiaGiocatori(partite[i],giocatore1,partite[j],giocatore2);								
									sostituito = true;
									if(!sostituitoAlmenoUno){sostituitoAlmenoUno = true;}
								}
							}
						}
					}
					if (sostituito) iterator.remove();
				}
			}
		}while(sostituitoAlmenoUno && numeroIterazioni < NUMERO_MASSIMO_ITERAZIONI);
		return partite;
	}
	
	
	private static Partita[] redistribuisciPartitePerCriterioClubDiversi(PrioritaSorteggio priorita, List<PrioritaSorteggio> listaPriorita, Partita[] partite, Partita[] partitePrecedenti){
		short numeroIterazioni = 0; //non serve a nulla ma evita il rischio di loop.
		boolean sostituitoAlmenoUno = false;
		do{
			numeroIterazioni++;
			MyLogger.getLogger().info("Redistribuzione Partite Per Criterio Club Diversi - Iterazione n° "+numeroIterazioni);
			sostituitoAlmenoUno = false;
			for (int i=0; i < partite.length; i++){
				Set<GiocatoreDTO> giocatoriAnomali = nonRispettaCriterioStessoClub(partite[i]);
				Iterator<GiocatoreDTO> iterator = giocatoriAnomali.iterator();
				while (iterator.hasNext() && giocatoriAnomali.size() > 1){				
					GiocatoreDTO giocatore1 = iterator.next();
					MyLogger.getLogger().fine("Nel tavolo "+partite[i].getNumeroTavolo()+" il giocatore "+giocatore1+ " incontrerebbe compagni di Club");
					boolean sostituito = false;
					for (int j=0; j < partite.length && !sostituito; j++){
						if (i != j){
							GiocatoreDTO[] giocatori = partite[j].getGiocatori().toArray(new GiocatoreDTO[0]);
							for (int k=0; k<giocatori.length && !sostituito; k++){
								GiocatoreDTO giocatore2 = giocatori[k];
								if (verificaSeScambiabili(priorita, listaPriorita, partite[i], giocatore1, partite[j], giocatore2, partitePrecedenti)){
									//TODO In base al fatto che adesso la verifica è cambiata, si potrebbe passare da una situazione 1 su un tavolo
									//e tre su un altro a 2 su un tavolo e 2 su un altro.
									//Non è detto, però che non possa esserci uno scambio ancora più profittevole: per verificarlo,
									//ogni volta che c'è uno scambio si dovrebbe ripartire dalla prima partita 
									scambiaGiocatori(partite[i],giocatore1,partite[j],giocatore2);								
									sostituito = true;
									if(!sostituitoAlmenoUno){sostituitoAlmenoUno = true;}
								}
							}
						}
					}
					if (sostituito) iterator.remove();
				}
			}
		}while(sostituitoAlmenoUno && numeroIterazioni < NUMERO_MASSIMO_ITERAZIONI);
		return partite;
	}
	
	
	private static Partita[] redistribuisciPartitePerCriterioClubDiversiConsiderandoIFissiAlTavolo(PrioritaSorteggio priorita, List<PrioritaSorteggio> listaPriorita, Partita[] partite, Partita[] partitePrecedenti, boolean consideraIFissi){
		short numeroIterazioni = 0; //non serve a nulla ma evita il rischio di loop.
		boolean sostituitoAlmenoUno = false;
		do{
			numeroIterazioni++;
			MyLogger.getLogger().info("Redistribuzione Partite per Criterio Club Diversi Considerando I Fissi Al Tavolo: "+consideraIFissi+" - Iterazione n° "+numeroIterazioni);
			sostituitoAlmenoUno = false;
			for (int i=0; i < partite.length; i++){
				Set<GiocatoreDTO> giocatoriAnomali = nonRispettaCriterioStessoClubConsiderandoIFissi(partite[i], consideraIFissi);
				Iterator<GiocatoreDTO> iterator = giocatoriAnomali.iterator();
				while (iterator.hasNext() && giocatoriAnomali.size() > 1){				
					GiocatoreDTO giocatore1 = iterator.next();
					MyLogger.getLogger().fine("Nel tavolo "+partite[i].getNumeroTavolo()+" il giocatore "+giocatore1+ " incontrerebbe "+(giocatoriAnomali.size()-1)+" compagni di Club: "+giocatoriAnomali);
					boolean sostituito = false;
					for (int j=0; j < partite.length && !sostituito; j++){
						if (i != j){
							GiocatoreDTO[] giocatori = partite[j].getGiocatori().toArray(new GiocatoreDTO[0]);
							for (int k=0; k<giocatori.length && !sostituito; k++){
								GiocatoreDTO giocatore2 = giocatori[k];
								if (!consideraIFissi 
								|| (giocatore1.isFissoAlTipoDiTavolo() && giocatore2.isFissoAlTipoDiTavolo())
								//TEST TEST TEST
								|| (!giocatore1.isFissoAlTipoDiTavolo() && !giocatore2.isFissoAlTipoDiTavolo() && partite[i].getNumeroGiocatori() == partite[j].getNumeroGiocatori())	
									){
									if (verificaSeScambiabili(priorita, listaPriorita, partite[i], giocatore1, partite[j], giocatore2, partitePrecedenti)){
										//TODO In base al fatto che adesso la verifica è cambiata, si potrebbe passare da una situazione 1 su un tavolo
										//e tre su un altro a 2 su un tavolo e 2 su un altro.
										//Non è detto, però che non possa esserci uno scambio ancora più profittevole: per verificarlo,
										//ogni volta che c'è uno scambio si dovrebbe ripartire dalla prima partita 
										scambiaGiocatori(partite[i],giocatore1,partite[j],giocatore2);								
										sostituito = true;
										if(!sostituitoAlmenoUno){sostituitoAlmenoUno = true;}
									}
								}
							}
						}
					}
					//TEST Questa IF prima non c'era anche se è logico che ci sia (togli il giocatore solo se è stato scambiato così provi con eventualmente l'altro/i)
					if (sostituito)iterator.remove();
				}
			}
		}while(sostituitoAlmenoUno && numeroIterazioni < NUMERO_MASSIMO_ITERAZIONI);
		return partite;
	}
	
	
	private static Partita[] redistribuisciPartitePerCriterioClubDiversi_old(PrioritaSorteggio priorita, List<PrioritaSorteggio> listaPriorita, Partita[] partite, Partita[] partitePrecedenti){
		for (int i=0; i < partite.length; i++){
			Set<GiocatoreDTO> giocatoriAnomali = nonRispettaCriterioStessoClub(partite[i]);
			Iterator<GiocatoreDTO> iterator = giocatoriAnomali.iterator();
			while (iterator.hasNext() && giocatoriAnomali.size() > 1){				
				GiocatoreDTO giocatore1 = iterator.next();
				MyLogger.getLogger().fine("Nel tavolo "+partite[i].getNumeroTavolo()+" il giocatore "+giocatore1+ " incontrerebbe compagni di Club");
				boolean sostituito = false;
				for (int j=0; j < partite.length && !sostituito; j++){
					if (i != j){
						GiocatoreDTO[] giocatori = partite[j].getGiocatori().toArray(new GiocatoreDTO[0]);
						for (int k=0; k<giocatori.length && !sostituito; k++){
							GiocatoreDTO giocatore2 = giocatori[k];
							if (verificaSeScambiabili(priorita, listaPriorita, partite[i], giocatore1, partite[j], giocatore2, partitePrecedenti)){
								//TODO In base al fatto che adesso la verifica è cambiata, si potrebbe passare da una situazione 1 su un tavolo
								//e tre su un altro a 2 su un tavolo e 2 su un altro.
								//Non è detto, però che non possa esserci uno scambio ancora più profittevole: per verificarlo,
								//ogni volta che c'è uno scambio si dovrebbe ripartire dalla prima partita 
								scambiaGiocatori(partite[i],giocatore1,partite[j],giocatore2);								
								sostituito = true;
							}
						}
					}
				}
				iterator.remove();
			}
		}
		return partite;
	}
	
	private static Partita[] redistribuisciPartitePerMinimizzarePartecipazioneTavoloda(PrioritaSorteggio priorita, List<PrioritaSorteggio> listaPriorita, Partita[] partite, Partita[] partitePrecedenti, int numeroGiocatori){
		short numeroIterazioni = 0; //non serve a nulla ma evita il rischio di loop.
		boolean sostituitoAlmenoUno = false;
		do{
			numeroIterazioni++;
			MyLogger.getLogger().info("Redistribuzione Partite per Minimizzare Partecipazione Tavolo da "+numeroGiocatori+" - Iterazione n° "+numeroIterazioni);
			sostituitoAlmenoUno = false;
			for (int i=0; i < partite.length; i++){
				if(partite[i].getNumeroGiocatori() == numeroGiocatori){
					Set<GiocatoreDTO> giocatori1 = new HashSet<GiocatoreDTO>(partite[i].getGiocatori());
					for (GiocatoreDTO giocatore1: giocatori1){
						int numeroPartecipazioniTavoloda = numeroPartecipazioniTavoloda(giocatore1,partitePrecedenti,numeroGiocatori);
						if (numeroPartecipazioniTavoloda > 0){
							MyLogger.getLogger().fine("Nel tavolo "+partite[i].getNumeroTavolo()+" il giocatore "+giocatore1+ " ha già giocato "+numeroPartecipazioniTavoloda+" volte al tavolo da "+numeroGiocatori);
							boolean sostituito = false;
							for (int j=0; j < partite.length && !sostituito; j++){
								if (i != j && partite[j].getNumeroGiocatori() != numeroGiocatori){
									GiocatoreDTO[] giocatori2 = partite[j].getGiocatori().toArray(new GiocatoreDTO[0]);
									for (int k=0; k<giocatori2.length && !sostituito; k++){
										GiocatoreDTO giocatore2 = giocatori2[k];
										if (verificaSeScambiabili(priorita, listaPriorita, partite[i], giocatore1, partite[j], giocatore2, partitePrecedenti)){
											scambiaGiocatori(partite[i],giocatore1,partite[j],giocatore2);
											sostituito = true;
											if(!sostituitoAlmenoUno){sostituitoAlmenoUno = true;}
										}
									}
								}
							}
						}
					}
				}
			}
		}while(sostituitoAlmenoUno && numeroIterazioni < NUMERO_MASSIMO_ITERAZIONI);
		return partite;
	}
	
	
	public static void main (String[] args){
		MyLogger.setConsoleLogLevel(Level.ALL);
		File file = new File("C:\\Documents and Settings\\Vjger\\Desktop\\XI Campionato_5_test_2.xls");
		//File file = new File("XI Campionato_5_test.xls");
		ExcelAccess excelAccess = new ExcelAccess(file);
		excelAccess.openFileExcel();
		
		Partita[] partiteTurno5 = excelAccess.loadPartite(5,false,TipoTorneo.CampionatoGufo);
		List<Partita> listaPartitePrecedenti = new ArrayList<Partita>();
		for (int i = 1; i < 5; i++){
			Partita[] partiteTurnoi = excelAccess.loadPartite(i,false,TipoTorneo.CampionatoGufo);
			if (partiteTurnoi == null){
				throw new MyException("E' stato richiesto il sorteggio per il turno "+5+" ma non esiste il turno "+i);
			}
			listaPartitePrecedenti.addAll(Arrays.asList(partiteTurnoi));
		}
		Partita[] partitePrecedenti = listaPartitePrecedenti.toArray(new Partita[0]);
		List<PrioritaSorteggio> priorita = new ArrayList<PrioritaSorteggio>();
		priorita.add(PrioritaSorteggio.MINIMIZZAZIONE_PARTECIPAZIONE_TAVOLO_DA_3);
		priorita.add(PrioritaSorteggio.MINIMIZZAZIONE_PARTECIPAZIONE_TAVOLO_DA_5);
		priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB);
		priorita.add(PrioritaSorteggio.MINIMIZZAZIONE_SCONTRI_DIRETTI);
		Partita[] nuovePartite = redistribuisciPartiteMinimizzareScontriMultipli(PrioritaSorteggio.MINIMIZZAZIONE_SCONTRI_DIRETTI, priorita, partiteTurno5, partitePrecedenti);
		//List<GiocatoreDTO> giocatoriPartecipanti = excelAccess.getListaGiocatori(true);
		//Partita[] nuovePartite = GeneratoreTavoliNew.generaPartite2(giocatoriPartecipanti, partitePrecedenti, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5_SE_UNO_SOLO_ALTRIMENTI_DA_3_COL_MORTO, priorita);
		for (int i = 0; i < nuovePartite.length; i++){
			System.out.println(nuovePartite[i]);
		}
	}
	
	private static Partita[] generaPartite2(List<GiocatoreDTO> giocatori, Partita[] partitePrecedenti, TipoTavoli tipoTavoli, List<PrioritaSorteggio> listaPriorita){
		File file = new File("C:\\Documents and Settings\\Vjger\\Desktop\\XI Campionato_5_test_2.xls");
		//File file = new File("XI Campionato_5_test.xls");
		ExcelAccess excelAccess = new ExcelAccess(file);
		excelAccess.openFileExcel();
		Partita[] tavoliIniziali = excelAccess.loadPartite(5,false,TipoTorneo.CampionatoGufo);
		
		Partita[] partiteDaRedistribuire = tavoliIniziali;
		for (PrioritaSorteggio priorita: listaPriorita){
			partiteDaRedistribuire = redistribuzionePartite(priorita, listaPriorita, partiteDaRedistribuire, partitePrecedenti);
		}
		
		if (TipoTavoli.tipoTavoloColMorto(tipoTavoli)){
			for (Partita partita: partiteDaRedistribuire){
				int numeroGiocatori = partita.getNumeroGiocatori();
				if (
					(tipoTavoli == TipoTavoli.DA_3_ED_EVENTUALMENTE_DA_2_COL_MORTO && numeroGiocatori == 2)
				||  (tipoTavoli == TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5_SE_UNO_SOLO_ALTRIMENTI_DA_3_COL_MORTO && numeroGiocatori == 3)		
				||  (tipoTavoli == TipoTavoli.DA_5_ED_EVENTUALMENTE_DA_4_COL_MORTO && numeroGiocatori == 4)
				){
					partita.setNumeroGiocatori(numeroGiocatori+1);
					partita.addGiocatore(GiocatoreDTO.FITTIZIO, null);
				}
			}
		}			
		return partiteDaRedistribuire;
	}
	
	private static Integer numeroPartecipazioniTavoloda(GiocatoreDTO giocatore, Partita[] partitePrecedenti, int numeroGiocatori){
		Integer result = 0;
		for (Partita partita: partitePrecedenti){
			if (partita.eAlTavolo(giocatore) && partita.getNumeroGiocatori() == numeroGiocatori){
				result++;
			}
		}
		return result;
	}
	
	
	private static void scambiaGiocatori(Partita partita1, GiocatoreDTO giocatore1, Partita partita2, GiocatoreDTO giocatore2){
		partita1.removeGiocatore(giocatore1);
		partita1.addGiocatore(giocatore2, null);
		partita2.removeGiocatore(giocatore2);
		partita2.addGiocatore(giocatore1, null);
		MyLogger.getLogger().fine("Sostituito "+giocatore1+ " del tavolo "+partita1.getNumeroTavolo()+" con "+giocatore2+" del tavolo "+partita2.getNumeroTavolo());
	}
	

	private static boolean sonoIntercambiabiliInBaseAPartitePrecedenti(boolean inSensoStretto, Partita partita1, GiocatoreDTO giocatore1, Partita partita2, GiocatoreDTO giocatore2, Partita[] partitePrecedenti){
		boolean result = false;
				
		Partita partita1Bis = new Partita(partita1);
		Partita partita2Bis = new Partita(partita2);
		partita1Bis.removeGiocatore(giocatore1);
		partita1Bis.addGiocatore(giocatore2, null);

		partita2Bis.removeGiocatore(giocatore2);
		partita2Bis.addGiocatore(giocatore1, null);
		
		/* Modifica del 10/11/2010: calcolato il totale degli scontri anzichè quello dei giocatori (inutile quando tutti avranno incontrato tutti) */
	
		int numeroScontriGiocatoriGiaAffrontatiPrima 	= totaleScontriTraGiocatoriCheSiSonoGiaAffrontati(partita1, partitePrecedenti)
												+ totaleScontriTraGiocatoriCheSiSonoGiaAffrontati(partita2, partitePrecedenti);

		int numeroScontriGiocatoriGiaAffrontatiDopo 	= totaleScontriTraGiocatoriCheSiSonoGiaAffrontati(partita1Bis, partitePrecedenti)
												+ totaleScontriTraGiocatoriCheSiSonoGiaAffrontati(partita2Bis, partitePrecedenti);

		if (inSensoStretto){
			result = numeroScontriGiocatoriGiaAffrontatiPrima >  numeroScontriGiocatoriGiaAffrontatiDopo;
		}else{
			result = numeroScontriGiocatoriGiaAffrontatiPrima >= numeroScontriGiocatoriGiaAffrontatiDopo;
		}
				
		return result;
	}

	private static boolean sonoIntercambiabiliInBaseAClubAffrontatiPrecedenti(boolean inSensoStretto, Partita partita1, GiocatoreDTO giocatore1, Partita partita2, GiocatoreDTO giocatore2, Partita[] partitePrecedenti){
		boolean result = false;
				
		Partita partita1Bis = new Partita(partita1);
		Partita partita2Bis = new Partita(partita2);
		partita1Bis.removeGiocatore(giocatore1);
		partita1Bis.addGiocatore(giocatore2, null);

		partita2Bis.removeGiocatore(giocatore2);
		partita2Bis.addGiocatore(giocatore1, null);
	
		int numeroScontriClubGiaAffrontatiPrima 	= totaleScontriTraClubCheSiSonoGiaAffrontati(partita1, partitePrecedenti)
												+ totaleScontriTraClubCheSiSonoGiaAffrontati(partita2, partitePrecedenti);

		int numeroScontriClubGiaAffrontatiDopo 	= totaleScontriTraClubCheSiSonoGiaAffrontati(partita1Bis, partitePrecedenti)
												+ totaleScontriTraClubCheSiSonoGiaAffrontati(partita2Bis, partitePrecedenti);

		if (inSensoStretto){
			result = numeroScontriClubGiaAffrontatiPrima >  numeroScontriClubGiaAffrontatiDopo;
		}else{
			result = numeroScontriClubGiaAffrontatiPrima >= numeroScontriClubGiaAffrontatiDopo;
		}
				
		return result;
	}
	
	private static boolean sonoIntercambiabiliInBaseAClubAffrontatiPrecedentiNew(boolean inSensoStretto, Partita partita1, GiocatoreDTO giocatore1, Partita partita2, GiocatoreDTO giocatore2, Partita[] partitePrecedenti){
		boolean result = false;
				
		Partita partita1Bis = new Partita(partita1);
		Partita partita2Bis = new Partita(partita2);
		partita1Bis.removeGiocatore(giocatore1);
		partita1Bis.addGiocatore(giocatore2, null);

		partita2Bis.removeGiocatore(giocatore2);
		partita2Bis.addGiocatore(giocatore1, null);
	
		int numeroScontriClubGiaAffrontatiPrima = totaleScontriAnomaliTraClub(partita1, partita2, partitePrecedenti);

		int numeroScontriClubGiaAffrontatiDopo 	= totaleScontriAnomaliTraClub(partita1Bis, partita2Bis, partitePrecedenti);

		if (inSensoStretto){
			result = numeroScontriClubGiaAffrontatiPrima >  numeroScontriClubGiaAffrontatiDopo;
		}else{
			result = numeroScontriClubGiaAffrontatiPrima >= numeroScontriClubGiaAffrontatiDopo;
		}
				
		return result;
	}
	
	private static boolean sonoIntercambiabiliInBaseAClubAffrontatoPrecedentemente(boolean inSensoStretto, Partita partita1, GiocatoreDTO giocatore1, Partita partita2, GiocatoreDTO giocatore2, Partita[] partitePrecedenti){
		boolean result = false;
				
		Partita partita1Bis = new Partita(partita1);
		Partita partita2Bis = new Partita(partita2);
		partita1Bis.removeGiocatore(giocatore1);
		partita1Bis.addGiocatore(giocatore2, null);

		partita2Bis.removeGiocatore(giocatore2);
		partita2Bis.addGiocatore(giocatore1, null);
	
		int numeroScontriClubGiaAffrontatiPrima 	= totaleScontriTraGiocatoreEClub(partita1, partitePrecedenti)
												+ totaleScontriTraGiocatoreEClub(partita2, partitePrecedenti);

		int numeroScontriClubGiaAffrontatiDopo 	= totaleScontriTraGiocatoreEClub(partita1Bis, partitePrecedenti)
												+ totaleScontriTraGiocatoreEClub(partita2Bis, partitePrecedenti);

		if (inSensoStretto){
			result = numeroScontriClubGiaAffrontatiPrima >  numeroScontriClubGiaAffrontatiDopo;
		}else{
			result = numeroScontriClubGiaAffrontatiPrima >= numeroScontriClubGiaAffrontatiDopo;
		}
				
		return result;
	}
	
	private static boolean sonoIntercambiabiliInBaseAVincitoriPrecedenti(boolean inSensoStretto, Partita partita1, GiocatoreDTO giocatore1, Partita partita2, GiocatoreDTO giocatore2, Partita[] partitePrecedenti){
		boolean result = false;
				
		int differenzaVittorieOVincitori 	= totaleVittorieOVincitori(partita1, partitePrecedenti, true)
									        - totaleVittorieOVincitori(partita2, partitePrecedenti, true);

		if (inSensoStretto){ //Se senso stressto scambio solo se c'è un livellamento delle vittorie
			result = differenzaVittorieOVincitori >  1;
		}else{ //Se senso largo, lo scambio è sempre possibile se sono entrambi Vincitori o  entrambi sconfitti
			if (unVincitoreUnoSconfittoOentrambiVincitoriEntrambiSconfitti(giocatore1, giocatore2, partitePrecedenti, false)){
				result = true;
			}else{ //altrimenti solo se la differenza di vittorie è almeno di una
				result = differenzaVittorieOVincitori >= 1;
			}
		}
				
		return result;
	}
	
	
	private static boolean sonoIntercambiabiliInBaseAMinimizzazioneTavoliDaN(boolean inSensoStretto, int numeroGiocatori, Partita partita1, GiocatoreDTO giocatore1, Partita partita2, GiocatoreDTO giocatore2, Partita[] partitePrecedenti){
		boolean result = false;
				
		Partita partita1Bis = new Partita(partita1);
		Partita partita2Bis = new Partita(partita2);
		partita1Bis.removeGiocatore(giocatore1);
		partita1Bis.addGiocatore(giocatore2, null);

		partita2Bis.removeGiocatore(giocatore2);
		partita2Bis.addGiocatore(giocatore1, null);

		if (!inSensoStretto){
			/* Se i due tavoli sono uguali lo scambio è sempre fattibile*/
			result = partita1Bis.getNumeroGiocatori() == partita2Bis.getNumeroGiocatori();
			
			/* Se nessuno dei due è il tavolo cercato lo scambio è fattibile */
			if (!result){
				result = partita1Bis.getNumeroGiocatori() != numeroGiocatori && partita2Bis.getNumeroGiocatori() != numeroGiocatori;
			}
		}
		/* Se non sono uguali e uno dei due è il tavolo cercato verifico che il numero di partecipazioni di un giocatore sia superiore a quello dell'altro */
		if (!result){
			if (partita1Bis.getNumeroGiocatori() == numeroGiocatori) {
				if (inSensoStretto){
					result = numeroPartecipazioniTavoloda(giocatore1,partitePrecedenti,numeroGiocatori) >  numeroPartecipazioniTavoloda(giocatore2,partitePrecedenti,numeroGiocatori);
				}else{
					result = numeroPartecipazioniTavoloda(giocatore1,partitePrecedenti,numeroGiocatori) >= numeroPartecipazioniTavoloda(giocatore2,partitePrecedenti,numeroGiocatori);
				}
			}else if (partita2Bis.getNumeroGiocatori() == numeroGiocatori){
				if (inSensoStretto){
					result = numeroPartecipazioniTavoloda(giocatore1,partitePrecedenti,numeroGiocatori) <  numeroPartecipazioniTavoloda(giocatore2,partitePrecedenti,numeroGiocatori);
				}else{
					result = numeroPartecipazioniTavoloda(giocatore1,partitePrecedenti,numeroGiocatori) <= numeroPartecipazioniTavoloda(giocatore2,partitePrecedenti,numeroGiocatori);
				}
			}
		}
		return result;
	}


	private static boolean sonoIntercambiabiliInBaseAClubDiversi(Partita partita1, GiocatoreDTO giocatore1, Partita partita2, GiocatoreDTO giocatore2, boolean inSensoStretto){
		boolean result = false;
				
		Partita partita1Bis = new Partita(partita1);
		Partita partita2Bis = new Partita(partita2);
		partita1Bis.removeGiocatore(giocatore1);
		partita1Bis.addGiocatore(giocatore2, null);

		partita2Bis.removeGiocatore(giocatore2);
		partita2Bis.addGiocatore(giocatore1, null);

		//Prima contavo il numero di giocatori coinvolti, adesso il numero di scontri diretti. Quindi, 2 gg insieme fanno 1 scontro, tre ne fanno 3
		//int sizeAnomaliePrimaDelloScambio = nonRispettaCriterioStessoClub(partita1).size()+ nonRispettaCriterioStessoClub(partita2).size();
		//int sizeAnomalieDopoLoScambio = nonRispettaCriterioStessoClub(partita1Bis).size()+ nonRispettaCriterioStessoClub(partita2Bis).size();
		
		//int sizeAnomaliePrimaDelloScambio = contaCombinazioniScontriDiretti(nonRispettaCriterioStessoClub(partita1).size())+ contaCombinazioniScontriDiretti(nonRispettaCriterioStessoClub(partita2).size());
		//int sizeAnomalieDopoLoScambio = contaCombinazioniScontriDiretti(nonRispettaCriterioStessoClub(partita1Bis).size())+ contaCombinazioniScontriDiretti(nonRispettaCriterioStessoClub(partita2Bis).size());
		
		int sizeAnomaliePrimaDelloScambio = contaCombinazioniScontriDirettiStessoClub(nonRispettaCriterioStessoClub(partita1))+ contaCombinazioniScontriDirettiStessoClub(nonRispettaCriterioStessoClub(partita2));
		int sizeAnomalieDopoLoScambio = contaCombinazioniScontriDirettiStessoClub(nonRispettaCriterioStessoClub(partita1Bis))+ contaCombinazioniScontriDirettiStessoClub(nonRispettaCriterioStessoClub(partita2Bis));
		
		if (inSensoStretto){
			result = sizeAnomaliePrimaDelloScambio >  sizeAnomalieDopoLoScambio;
		}else{
			result = sizeAnomaliePrimaDelloScambio >= sizeAnomalieDopoLoScambio;
		}
		return result;
	}
	
	//TODO Dovrei levare IF ed else visto che ora si fa il test anche per i non fissi ma al tavolo da 5
	private static boolean sonoIntercambiabiliInBaseAClubDiversi(Partita partita1, GiocatoreDTO giocatore1, Partita partita2, GiocatoreDTO giocatore2, boolean inSensoStretto, boolean consideraIFissi){
		boolean result = false;
		
		//if (!consideraIFissi || (giocatore1.isFissoAlTipoDiTavolo() && giocatore2.isFissoAlTipoDiTavolo())){	
			Partita partita1Bis = new Partita(partita1);
			Partita partita2Bis = new Partita(partita2);
			partita1Bis.removeGiocatore(giocatore1);
			partita1Bis.addGiocatore(giocatore2, null);
	
			partita2Bis.removeGiocatore(giocatore2);
			partita2Bis.addGiocatore(giocatore1, null);
			
			int sizeAnomaliePrimaDelloScambio = 
					contaCombinazioniScontriDirettiStessoClub(nonRispettaCriterioStessoClubConsiderandoIFissi(partita1, consideraIFissi))
				  + contaCombinazioniScontriDirettiStessoClub(nonRispettaCriterioStessoClubConsiderandoIFissi(partita2, consideraIFissi));
			int sizeAnomalieDopoLoScambio = 
					contaCombinazioniScontriDirettiStessoClub(nonRispettaCriterioStessoClubConsiderandoIFissi(partita1Bis, consideraIFissi))
				  + contaCombinazioniScontriDirettiStessoClub(nonRispettaCriterioStessoClubConsiderandoIFissi(partita2Bis, consideraIFissi));
			
			if (inSensoStretto){
				result = sizeAnomaliePrimaDelloScambio >  sizeAnomalieDopoLoScambio;
			}else{
				result = sizeAnomaliePrimaDelloScambio >= sizeAnomalieDopoLoScambio;
			}
		//}else if (consideraIFissi && !giocatore1.isFissoAlTipoDiTavolo() && !giocatore2.isFissoAlTipoDiTavolo()){
		//	result = true;
		//}
		return result;
	}

	private static int contaCombinazioniScontriDirettiStessaRegione(Set<GiocatoreDTO> giocatoriAnomali){
		int result = 0;
		List<RegioneDTO> regioniEsaminate = new ArrayList<RegioneDTO>();
		List<GiocatoreDTO> giocatoriInConfronto = new ArrayList<GiocatoreDTO>(giocatoriAnomali); //Mi serve che sia un List
		for (int i=0; i<giocatoriInConfronto.size(); i++){
			RegioneDTO regione = giocatoriInConfronto.get(i).getRegioneProvenienza();
			if (regione != null && !regioniEsaminate.contains(regione)){
				regioniEsaminate.add(regione);
				short numeroGiocatoriPerQuellaRegione = 1;
				for (int j=i+1; j<giocatoriInConfronto.size(); j++){
					GiocatoreDTO giocatoreConfrontato = giocatoriInConfronto.get(j);
					RegioneDTO regioneConfrontata = giocatoreConfrontato.getRegioneProvenienza();
					if(regioneConfrontata != null && regioneConfrontata.equals(regione)){
						numeroGiocatoriPerQuellaRegione++;
					}
				}
				result += contaCombinazioniScontriDiretti(numeroGiocatoriPerQuellaRegione);
			}
		}
		return result;
	}
	
	private static int contaCombinazioniScontriDirettiStessoClub(Set<GiocatoreDTO> giocatoriAnomali){
		int result = 0;
		List<ClubDTO> clubEsaminati = new ArrayList<ClubDTO>();
		List<GiocatoreDTO> giocatoriInConfronto = new ArrayList<GiocatoreDTO>(giocatoriAnomali); //Mi serve che sia un List
		for (int i=0; i<giocatoriInConfronto.size(); i++){
			ClubDTO club = giocatoriInConfronto.get(i).getClubProvenienza();
			if (club != null && !clubEsaminati.contains(club)){
				clubEsaminati.add(club);
				short numeroGiocatoriPerQuelClub = 1;
				for (int j=i+1; j<giocatoriInConfronto.size(); j++){
					GiocatoreDTO giocatoreConfrontato = giocatoriInConfronto.get(j);
					ClubDTO clubConfrontato = giocatoreConfrontato.getClubProvenienza();
					if(clubConfrontato != null && clubConfrontato.equals(club)){
						numeroGiocatoriPerQuelClub++;
					}
				}
				result += contaCombinazioniScontriDiretti(numeroGiocatoriPerQuelClub);
			}
		}
		return result;
	}

	private static int contaCombinazioniScontriDiretti(int numeroGiocatori){
		int result = 0;
		for (int i = 1; i <= numeroGiocatori; i++){
			result += (i - 1);
		}
		return result;
	}
	
	private static Set<GiocatoreDTO> nonRispettaCriterioStessoClub_old(Partita partita){
		Set<GiocatoreDTO> result = new HashSet<GiocatoreDTO>();
		GiocatoreDTO[] giocatoriInConfronto = partita.getGiocatori().toArray(new GiocatoreDTO[0]);
		for (int i=0; i<giocatoriInConfronto.length-1; i++){
			for (int j=i+1; j<giocatoriInConfronto.length; j++){
				if (giocatoriInConfronto[i].getClubProvenienza() != null && giocatoriInConfronto[j].getClubProvenienza() != null){
					if(giocatoriInConfronto[i].getClubProvenienza().equals(giocatoriInConfronto[j].getClubProvenienza())){
						result.add(giocatoriInConfronto[i]);
					}
				}
			}
		}
		return result;
	}

	
	private static Set<GiocatoreDTO> nonRispettaCriterioStessoClub(Partita partita){
		Set<GiocatoreDTO> result = new HashSet<GiocatoreDTO>();
		GiocatoreDTO[] giocatoriInConfronto = partita.getGiocatori().toArray(new GiocatoreDTO[0]);
		for (int i=0; i<giocatoriInConfronto.length; i++){
			for (int j=0; j<giocatoriInConfronto.length; j++){
				ClubDTO clubGiocatoreI = giocatoriInConfronto[i].getClubProvenienza();
				ClubDTO clubGiocatoreJ = giocatoriInConfronto[j].getClubProvenienza();
				if (i !=j && clubGiocatoreI != null && clubGiocatoreJ != null){
					if(clubGiocatoreI.equals(clubGiocatoreJ)){
						result.add(giocatoriInConfronto[i]);
					}
				}
			}
		}
		return result;
	}
	
	private static Set<GiocatoreDTO> nonRispettaCriterioStessoClubConsiderandoIFissi(Partita partita, boolean consideraIFissi){
		Set<GiocatoreDTO> result = new HashSet<GiocatoreDTO>();
		GiocatoreDTO[] giocatoriInConfronto = partita.getGiocatori().toArray(new GiocatoreDTO[0]);
		for (int i=0; i<giocatoriInConfronto.length; i++){
			for (int j=0; j<giocatoriInConfronto.length; j++){
				ClubDTO clubGiocatoreI = giocatoriInConfronto[i].getClubProvenienza();
				ClubDTO clubGiocatoreJ = giocatoriInConfronto[j].getClubProvenienza();
				if (i != j 
				&& clubGiocatoreI != null 
				&& clubGiocatoreJ != null 
				&& ( consideraIFissi 
				  || 
				    (!giocatoriInConfronto[i].isFissoAlTipoDiTavolo() && !giocatoriInConfronto[j].isFissoAlTipoDiTavolo()))){
					if(clubGiocatoreI.equals(clubGiocatoreJ)){
						result.add(giocatoriInConfronto[i]);
					}
				}
			}
		}
		return result;
	}

	private static boolean sonoIntercambiabiliInBaseARegioniDiverse(Partita partita1, GiocatoreDTO giocatore1, Partita partita2, GiocatoreDTO giocatore2, boolean inSensoStretto){
		boolean result = false;
				
		Partita partita1Bis = new Partita(partita1);
		Partita partita2Bis = new Partita(partita2);
		partita1Bis.removeGiocatore(giocatore1);
		partita1Bis.addGiocatore(giocatore2, null);

		partita2Bis.removeGiocatore(giocatore2);
		partita2Bis.addGiocatore(giocatore1, null);
		
		int sizeAnomaliePrimaDelloScambio = 
				contaCombinazioniScontriDirettiStessaRegione(nonRispettaCriterioStessaRegione(partita1))
			  + contaCombinazioniScontriDirettiStessaRegione(nonRispettaCriterioStessaRegione(partita2));
		int sizeAnomalieDopoLoScambio = 
				contaCombinazioniScontriDirettiStessaRegione(nonRispettaCriterioStessaRegione(partita1Bis))
			  + contaCombinazioniScontriDirettiStessaRegione(nonRispettaCriterioStessaRegione(partita2Bis));
		

		//int sizeAnomaliePrimaDelloScambio = nonRispettaCriterioStessaRegione(partita1).size()+ nonRispettaCriterioStessaRegione(partita2).size();
		//int sizeAnomalieDopoLoScambio = nonRispettaCriterioStessaRegione(partita1Bis).size()+ nonRispettaCriterioStessaRegione(partita2Bis).size();
		if (inSensoStretto){
			result = sizeAnomaliePrimaDelloScambio >  sizeAnomalieDopoLoScambio;
		}else{
			result = sizeAnomaliePrimaDelloScambio >= sizeAnomalieDopoLoScambio;
		}
		return result;
	}
	
	private static Set<GiocatoreDTO> nonRispettaCriterioStessaRegione_old(Partita partita){
		Set<GiocatoreDTO> result = new HashSet<GiocatoreDTO>();
		GiocatoreDTO[] giocatoriInConfronto = partita.getGiocatori().toArray(new GiocatoreDTO[0]);
		for (int i=0; i<giocatoriInConfronto.length-1; i++){
			for (int j=i+1; j<giocatoriInConfronto.length; j++){
				if (giocatoriInConfronto[i].getRegioneProvenienza() != null && giocatoriInConfronto[j].getRegioneProvenienza() != null){
					if(giocatoriInConfronto[i].getRegioneProvenienza().equals(giocatoriInConfronto[j].getRegioneProvenienza())){
						result.add(giocatoriInConfronto[i]);
					}
				}
			}
		}
		return result;
	}
	
	private static Set<GiocatoreDTO> nonRispettaCriterioStessaRegione(Partita partita){
		Set<GiocatoreDTO> result = new HashSet<GiocatoreDTO>();
		GiocatoreDTO[] giocatoriInConfronto = partita.getGiocatori().toArray(new GiocatoreDTO[0]);
		for (int i=0; i<giocatoriInConfronto.length; i++){
			for (int j=0; j<giocatoriInConfronto.length; j++){
			if (i !=j && giocatoriInConfronto[i].getRegioneProvenienza() != null && giocatoriInConfronto[j].getRegioneProvenienza() != null){
				if(giocatoriInConfronto[i].getRegioneProvenienza().equals(giocatoriInConfronto[j].getRegioneProvenienza())){
					result.add(giocatoriInConfronto[i]);
				}
				}
			}
		}
		return result;
	}
	
	private static boolean ilTavolohaGiocatoriCheSiSonoGiaAffrontati(Partita partita, Partita[] partitePrecedenti){
		boolean result = false;
		if (partitePrecedenti != null){
			GiocatoreDTO[] giocatoriInConfronto = partita.getGiocatori().toArray(new GiocatoreDTO[0]);		
			for (int i=0; i<giocatoriInConfronto.length-1 && !result; i++){
				for (int j=i+1; j<giocatoriInConfronto.length && !result; j++){
					for (Partita partitaPrecedente: partitePrecedenti){
						result = partitaPrecedente.eAlTavolo(giocatoriInConfronto[i]) && partitaPrecedente.eAlTavolo(giocatoriInConfronto[j]);
						if (result) break;
					}
				}
			}
		}
		return result;
	}

	private static boolean ilTavolohaGiocatoriDiClubCheSiSonoGiaAffrontatiOSiStannoPerAffrontare(Partita partitaInLinea, Partita[] partiteTurnoInCorso, Partita[] partitePrecedenti){
		boolean result = false;
		Partita[] partite = ArrayUtils.concatenaPartite(partiteTurnoInCorso, partitePrecedenti);
		GiocatoreDTO[] giocatoriInConfronto = partitaInLinea.getGiocatori().toArray(new GiocatoreDTO[0]);		
		for (int i=0; i<giocatoriInConfronto.length-1 && !result; i++){
			for (int j=i+1; j<giocatoriInConfronto.length && !result; j++){
				for (Partita partita: partite){
					if (!partita.equals(partitaInLinea)){
						result = partita.isClubGiocatoreAlTavolo(giocatoriInConfronto[i]) && partita.isClubGiocatoreAlTavolo(giocatoriInConfronto[j]);
						if (result){
							break;
						}
					}
				}
			}
		}
		return result;
	}

	private static boolean ilTavolohaGiocatoriCheHannoGiaAffrontatoQuelClub(Partita partita, Partita[] partitePrecedenti){
		boolean result = false;
		if (partitePrecedenti != null){
			GiocatoreDTO[] giocatoriInConfronto = partita.getGiocatori().toArray(new GiocatoreDTO[0]);		
			for (int i=0; i<giocatoriInConfronto.length-1 && !result; i++){
				for (int j=i+1; j<giocatoriInConfronto.length && !result; j++){
					for (Partita partitaPrecedente: partitePrecedenti){
						result = partitaPrecedente.eAlTavolo(giocatoriInConfronto[i]) && partitaPrecedente.isClubGiocatoreAlTavolo(giocatoriInConfronto[j]);
						if (result) break;
					}
				}
			}
		}
		return result;
	}
	
	private static boolean ilTavolohaPiuVincitori(Partita partita, Partita[] partitePrecedenti){
		boolean result = false;
		int numeroVincitori = 0;
		if (partitePrecedenti != null){
			GiocatoreDTO[] giocatoriInConfronto = partita.getGiocatori().toArray(new GiocatoreDTO[0]);		
			for (int i=0; i<giocatoriInConfronto.length && !result; i++){
				for (Partita partitaPrecedente: partitePrecedenti){
					boolean wasVincitore = partitaPrecedente.isVincitore(giocatoriInConfronto[i]);
					if (wasVincitore){
						MyLogger.getLogger().finer("Nel tavolo "+partita+" "+giocatoriInConfronto[i]+" ha già vinto una partita");
						numeroVincitori++;
						break;
					}
				}
				result = numeroVincitori > 1;
			}
		}
		return result;
	}
	
	private static boolean unVincitoreUnoSconfittoOentrambiVincitoriEntrambiSconfitti(GiocatoreDTO giocatore1, GiocatoreDTO giocatore2, Partita[] partitePrecedenti, boolean scoreDiverso){
		boolean result = true;
		if (partitePrecedenti != null){
			boolean vincitore1 = false;
			boolean vincitore2 = false;
			for (Partita partitaPrecedente: partitePrecedenti){
				if (partitaPrecedente.eAlTavolo(giocatore1)){
					vincitore1 = partitaPrecedente.isVincitore(giocatore1);
				}
				if (partitaPrecedente.eAlTavolo(giocatore2)){
					vincitore2 = partitaPrecedente.isVincitore(giocatore2);
				}
			}
			if (scoreDiverso){
				result = (vincitore1 != vincitore2);
			}else{
				result = (vincitore1 == vincitore2);
			}
		}
		return result;
	}
	
	private static Set<GiocatoreDTO> giocatoriCheSiSonoGiaAffrontati(Partita partita, Partita[] partitePrecedenti){
		Set<GiocatoreDTO> result = new HashSet<GiocatoreDTO>();
		if (partitePrecedenti != null){
			GiocatoreDTO[] giocatoriInConfronto = partita.getGiocatori().toArray(new GiocatoreDTO[0]);		
			for (int i=0; i<giocatoriInConfronto.length-1; i++){
				for (int j=i+1; j<giocatoriInConfronto.length; j++){
					for (Partita partitaPrecedente: partitePrecedenti){
						if(partitaPrecedente.eAlTavolo(giocatoriInConfronto[i]) && partitaPrecedente.eAlTavolo(giocatoriInConfronto[j])){
							result.add(giocatoriInConfronto[i]);
							result.add(giocatoriInConfronto[j]);
						}
					}
				}
			}
		}
		return result;
	}
	
	private static Set<GiocatoreDTO> giocatoriDiClubCheSiSonoGiaAffrontati(Partita partitaInLinea, Partita[] partiteTurnoInCorso, Partita[] partitePrecedenti){
		Set<GiocatoreDTO> result = new HashSet<GiocatoreDTO>();
		Partita[] partite = ArrayUtils.concatenaPartite(partiteTurnoInCorso, partitePrecedenti);
		GiocatoreDTO[] giocatoriInConfronto = partitaInLinea.getGiocatori().toArray(new GiocatoreDTO[0]);		
		for (int i=0; i<giocatoriInConfronto.length-1; i++){
			for (int j=i+1; j<giocatoriInConfronto.length; j++){
				for (Partita partita: partite){
					if (!partita.equals(partitaInLinea)){
						if(partita.isClubGiocatoreAlTavolo(giocatoriInConfronto[i]) && partita.isClubGiocatoreAlTavolo(giocatoriInConfronto[j])){
							result.add(giocatoriInConfronto[i]);
							result.add(giocatoriInConfronto[j]);	
						}
					}
				}
			}
		}
		return result;
	}
	
	private static Set<GiocatoreDTO> giocatoriCheHannoGiaAffrontatoQuelClub(Partita partita, Partita[] partitePrecedenti){
		Set<GiocatoreDTO> result = new HashSet<GiocatoreDTO>();
		if (partitePrecedenti != null){
			GiocatoreDTO[] giocatoriInConfronto = partita.getGiocatori().toArray(new GiocatoreDTO[0]);		
			for (int i=0; i<giocatoriInConfronto.length-1; i++){
				for (int j=i+1; j<giocatoriInConfronto.length; j++){
					for (Partita partitaPrecedente: partitePrecedenti){
						if(partitaPrecedente.eAlTavolo(giocatoriInConfronto[i]) && partitaPrecedente.isClubGiocatoreAlTavolo(giocatoriInConfronto[j])){
							result.add(giocatoriInConfronto[i]);
						}
					}
				}
			}
		}
		return result;
	}
	
	private static Set<GiocatoreDTO> giocatoriCheHannoGiaVinto(Partita partita, Partita[] partitePrecedenti){
		Set<GiocatoreDTO> result = new HashSet<GiocatoreDTO>();
		if (partitePrecedenti != null){
			GiocatoreDTO[] giocatoriInConfronto = partita.getGiocatori().toArray(new GiocatoreDTO[0]);		
			for (int i=0; i<giocatoriInConfronto.length; i++){
				for (Partita partitaPrecedente: partitePrecedenti){
					if(partitaPrecedente.isVincitore(giocatoriInConfronto[i])){
						result.add(giocatoriInConfronto[i]);
					}
				}
			}
		}
		return result;
	}
	
	private static int totaleScontriTraGiocatoriCheSiSonoGiaAffrontati(Partita partita, Partita[] partitePrecedenti){
		int result = 0;
		if (partitePrecedenti != null){
			GiocatoreDTO[] giocatoriInConfronto = partita.getGiocatori().toArray(new GiocatoreDTO[0]);		
			for (int i=0; i<giocatoriInConfronto.length-1; i++){
				for (int j=i+1; j<giocatoriInConfronto.length; j++){
					for (Partita partitaPrecedente: partitePrecedenti){
						if(partitaPrecedente.eAlTavolo(giocatoriInConfronto[i]) && partitaPrecedente.eAlTavolo(giocatoriInConfronto[j])){
							result++;
						}
					}
				}
			}
		}
		return result;
	}
	
	
	private static int totaleScontriAnomaliTraClub(Partita partita1, Partita partita2, Partita[] partitePrecedenti){
		int result = 0;
		Partita[] partite = ArrayUtils.concatenaPartite(partitePrecedenti, new Partita[]{partita1, partita2});
		MatchAnomali matchAnomali = MatchAnalyzer.calcolaConfrontiClubAnomali(Arrays.asList(partite), AnomaliaConfrontiClub.BOTH);
		Map<ClubDTO, Map<ClubDTO, Integer>> matchClubVsClubAnomali = matchAnomali.getMatchClubVsClubAnomali();
		int sogliaMinima  = matchAnomali.getSogliaMinima();
		int sogliaMassima = matchAnomali.getSogliaMassima();
		for (ClubDTO club: matchClubVsClubAnomali.keySet()){
			Map<ClubDTO, Integer> confronti = matchClubVsClubAnomali.get(club);
			for (ClubDTO clubAvversario: confronti.keySet()){
				Integer scontriDiretti = confronti.get(clubAvversario);
				if (scontriDiretti > sogliaMassima){
					result += (scontriDiretti - sogliaMassima);
				}else if (scontriDiretti < sogliaMinima){
					result += (sogliaMinima - scontriDiretti);
				}
			}
		}
		return result;
	}
	
	private static int totaleScontriTraClubCheSiSonoGiaAffrontati(Partita partita, Partita[] partitePrecedenti){
		int result = 0;
		if (partitePrecedenti != null){
			GiocatoreDTO[] giocatoriInConfronto = partita.getGiocatori().toArray(new GiocatoreDTO[0]);		
			for (int i=0; i<giocatoriInConfronto.length-1; i++){
				for (int j=i+1; j<giocatoriInConfronto.length; j++){
					for (Partita partitaPrecedente: partitePrecedenti){
						if(partitaPrecedente.isClubGiocatoreAlTavolo(giocatoriInConfronto[i]) && partitaPrecedente.isClubGiocatoreAlTavolo(giocatoriInConfronto[j])){
							result++;
						}
					}
				}
			}
		}
		return result;
	}
	
	private static int totaleScontriTraGiocatoreEClub(Partita partita, Partita[] partitePrecedenti){
		int result = 0;
		if (partitePrecedenti != null){
			GiocatoreDTO[] giocatoriInConfronto = partita.getGiocatori().toArray(new GiocatoreDTO[0]);		
			for (int i=0; i<giocatoriInConfronto.length-1; i++){
				for (int j=i+1; j<giocatoriInConfronto.length; j++){
					for (Partita partitaPrecedente: partitePrecedenti){
						if(partitaPrecedente.eAlTavolo(giocatoriInConfronto[i]) && partitaPrecedente.isClubGiocatoreAlTavolo(giocatoriInConfronto[j])){
							result++;
						}
					}
				}
			}
		}
		return result;
	}
	
	
	private static int totaleVittorieOVincitori(Partita partita, Partita[] partitePrecedenti, boolean vincitori){
		int result = 0;
		if (partitePrecedenti != null){
			GiocatoreDTO[] giocatori = partita.getGiocatori().toArray(new GiocatoreDTO[0]);		
			for (int i=0; i<giocatori.length; i++){
				for (Partita partitaPrecedente: partitePrecedenti){
					if(partitaPrecedente.isVincitore(giocatori[i])){
						result++;
						if (vincitori){break;} //Se sto contando i vincitori blocco appena trovo una vittoria
					}
				}
			}
		}
		return result;
	}
	
	private static List<List<GiocatoreDTO>> getGiocatoriPerRegione1(List<GiocatoreDTO> giocatori){
		
		Comparator<RegioneDTO> comparator = new Comparator(){
			public int compare(Object o1, Object o2){
				Integer i2 = ((List)o2).size();
				Integer i1 = ((List)o1).size();
				return i2.compareTo(i1);
			}
		};
		List giocatoriPerRegione = new ArrayList();
		
		List<GiocatoreDTO> listaDiLavoro = new ArrayList<GiocatoreDTO>(giocatori);
		while (listaDiLavoro.size() >0){
			RegioneDTO regione = listaDiLavoro.get(0).getRegioneProvenienza();
			List<GiocatoreDTO> listaPerRegione = new ArrayList<GiocatoreDTO>();
			Iterator<GiocatoreDTO> iterator = listaDiLavoro.iterator();
			while (iterator.hasNext()){
				GiocatoreDTO giocatore = iterator.next();
				RegioneDTO regioneProvenienza = giocatore.getRegioneProvenienza();
				if ((regioneProvenienza == null && regione == null) 
				|| (regioneProvenienza != null && regioneProvenienza.equals(regione))){
					listaPerRegione.add(giocatore);
					iterator.remove();
				}
			}
			//regione.setPopolazione(listaPerRegione.size());
			Collections.shuffle(listaPerRegione);
			giocatoriPerRegione.add(listaPerRegione);
		}
		Collections.sort(giocatoriPerRegione,comparator);
		return giocatoriPerRegione;
	}
	
	private static List<List<GiocatoreDTO>> getGiocatoriPerClub(List<GiocatoreDTO> giocatori){
		
		Comparator<RegioneDTO> comparator = new Comparator(){
			public int compare(Object o1, Object o2){
				Integer i2 = ((List)o2).size();
				Integer i1 = ((List)o1).size();
				return i2.compareTo(i1);
			}
		};
		List giocatoriPerClub = new ArrayList();
		
		List<GiocatoreDTO> listaDiLavoro = new ArrayList<GiocatoreDTO>(giocatori);
		while (listaDiLavoro.size() >0){
			ClubDTO club = listaDiLavoro.get(0).getClubProvenienza();
			List<GiocatoreDTO> listaPerClub = new ArrayList<GiocatoreDTO>();
			Iterator<GiocatoreDTO> iterator = listaDiLavoro.iterator();
			while (iterator.hasNext()){
				GiocatoreDTO giocatore = iterator.next();
				if ((giocatore.getClubProvenienza() == null && club == null)
				||  (giocatore.getClubProvenienza() != null && giocatore.getClubProvenienza().equals(club))){
					listaPerClub.add(giocatore);
					iterator.remove();
				}
			}
			Collections.shuffle(listaPerClub);
			giocatoriPerClub.add(listaPerClub);
		}
		Collections.sort(giocatoriPerClub,comparator);
		return giocatoriPerClub;
	}
	
	private static Integer getTavoloCasuale(Integer numeroTavoli){
		Integer result;
		if (listaTavoli.size()==0){
			for (int i=1; i<=numeroTavoli; i++){
				listaTavoli.add(i);
			}
			Collections.shuffle(listaTavoli);
		}
		result = listaTavoli.get(0);
		listaTavoli.remove(0);
		return result;
	}
	

}
