package it.desimone.risiko.torneo.batch;

import it.desimone.risiko.torneo.dto.GiocatoreDTO;
import it.desimone.risiko.torneo.dto.Partita;
import it.desimone.risiko.torneo.scoreplayer.ScorePlayer;
import it.desimone.risiko.torneo.utils.PrioritaSorteggio;
import it.desimone.risiko.torneo.utils.RegioniLoader;
import it.desimone.risiko.torneo.utils.TavoliVuotiCreator;
import it.desimone.risiko.torneo.utils.TipoTavoli;
import it.desimone.risiko.torneo.utils.TipoTorneo;
import it.desimone.risiko.torneo.utils.TorneiUtils;
import it.desimone.utils.ArrayUtils;
import it.desimone.utils.Configurator;
import it.desimone.utils.MyException;
import it.desimone.utils.MyLogger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Sorteggiatore {
	
	public static final Integer SOGLIA_PER_RADUNO_CON_QUARTI = null; //Configurator.getSogliaRadunoConQuarti();
	public static final Integer SOGLIA_PER_MASTER_SPLITTATO = null; //Configurator.getSogliaMasterSplittato();
	public static final List<Integer> SOGLIE_PER_MASTER = Configurator.getSoglieMaster();
	public static final List<Integer> SOGLIE_PER_RADUNI = Configurator.getSoglieRaduni();
	public static final Short SEMIFINALISTI_QUALIFICATI_DIRETTAMENTE = 10;
	public static final Short SEMIFINALISTI = 16;
	
	public static Partita[] getPartiteSorteggiate(ExcelAccess excelAccess, TipoTorneo tipoTorneo, int numeroTurno){
		MyLogger.getLogger().info("Inizio Sorteggio per modalità "+tipoTorneo+" per turno n° "+numeroTurno);
		Partita[] partiteTurno = null;
		switch (tipoTorneo) {
		case NazionaleRisiKo:
			partiteTurno = getPartiteSorteggiateNazionaleRisiko(excelAccess, numeroTurno);
			break;
		case RadunoNazionale:
//			partiteTurno = getPartiteSorteggiateRadunoNazionale(excelAccess, numeroTurno);
//			break;
//		case RadunoNazionale_con_quarti:
//			partiteTurno = getPartiteSorteggiateRadunoNazionaleConQuarti(excelAccess, numeroTurno);
			partiteTurno = getPartiteSorteggiateRadunoNazionale2020(excelAccess, numeroTurno);
			break;
		case SantEufemia:
			partiteTurno = getPartiteSorteggiateRadunoNazionaleConTavoliDa5(excelAccess, numeroTurno);
			break;
		case MasterRisiko:
			partiteTurno = getPartiteSorteggiateMasterRisiko2020(excelAccess, numeroTurno);
			//partiteTurno = getPartiteSorteggiateMasterRisiko2016(excelAccess, numeroTurno);
			break;			
		case MasterRisiko2015:
			partiteTurno = getPartiteSorteggiateMasterRisiko2015(excelAccess, numeroTurno);
			break;
		case TorneoGufo:
		case Open:
		case Victory:
			partiteTurno = getPartiteSorteggiateTorneoGufo(excelAccess, numeroTurno);
			break;
		case CampionatoGufo:
			partiteTurno = getPartiteSorteggiateCampionatoGufo(excelAccess, numeroTurno);
			break;
		case BGL:
			partiteTurno = getPartiteSorteggiateBGL(excelAccess, numeroTurno);
			break;
		case BGL_SVIZZERA:
			partiteTurno = getPartiteSorteggiateBGLSvizzera(excelAccess, numeroTurno);
			break;			
		case _1vs1_:
			partiteTurno = getPartiteSorteggiate1vs1(excelAccess, numeroTurno);
			break;
		case _1vs1_SVIZZERA:
			partiteTurno = getPartiteSorteggiate1VS1Svizzera(excelAccess, numeroTurno);
			break;	
		case TorneoASquadre:
			partiteTurno = getPartiteSorteggiateTorneoASquadre(excelAccess, numeroTurno);
			break;				
		default:
			MyLogger.getLogger().severe("Tipo di Torneo non previsto: "+tipoTorneo);
			throw new MyException("Tipo di Torneo non previsto: "+tipoTorneo);
		}
		
		MyLogger.getLogger().exiting("Sorteggiatore", "getPartiteSorteggiate",  ArrayUtils.fromPartiteToString(partiteTurno));
		return partiteTurno;		
	}
	
	
	private static Partita[] getPartiteSorteggiateRadunoNazionale(ExcelAccess excelAccess, int numeroTurno){
		MyLogger.getLogger().entering("Sorteggiatore", "getPartiteSorteggiateRadunoNazionale");
		Partita[] partiteTurno = null;
		List<GiocatoreDTO> giocatoriPartecipanti = excelAccess.getListaGiocatori(true);
		List<PrioritaSorteggio> priorita = new ArrayList<PrioritaSorteggio>();
		switch (numeroTurno) {
		case 1:
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB);
			priorita.add(PrioritaSorteggio.IMPEDITA_STESSA_REGIONE);
			partiteTurno = GeneratoreTavoliNew.generaPartite(giocatoriPartecipanti, null, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);
			break;
		case 2:
			List<Partita> listaPartitePrecedenti = new ArrayList<Partita>();
			for (int i = 1; i < numeroTurno; i++){
				Partita[] partiteTurnoi = excelAccess.loadPartite(i,false,TipoTorneo.RadunoNazionale);
				if (partiteTurnoi == null){
					throw new MyException("E' stato richiesto il sorteggio per il turno "+numeroTurno+" ma non esiste il turno "+i);
				}
				listaPartitePrecedenti.addAll(Arrays.asList(partiteTurnoi));
			}
			
			TorneiUtils.checkPartiteConPiuVincitori(listaPartitePrecedenti);
			
			Partita[] partitePrecedenti = listaPartitePrecedenti.toArray(new Partita[0]);

			priorita.add(PrioritaSorteggio.VINCITORI_SEPARATI);
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB);
			priorita.add(PrioritaSorteggio.MINIMIZZAZIONE_SCONTRI_DIRETTI);
			partiteTurno = GeneratoreTavoliNew.generaPartite(giocatoriPartecipanti, partitePrecedenti, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);
			break;
		case 3:
			List<ScorePlayer> scores = excelAccess.getClassificaRaduno(true);
			List<GiocatoreDTO> semifinalisti = new ArrayList<GiocatoreDTO>();
			if (scores.size() < SEMIFINALISTI){
				MyLogger.getLogger().severe("Impossibile elaborare le semifinali; meno di 16 giocatori: "+scores.size());
				throw new MyException("Impossibile elaborare le semifinali; meno di 16 giocatori: "+scores.size());
			}
			for (int i = 0; i < SEMIFINALISTI; i++){ //suddivisione in 4 fasce di 4 giocatori
				GiocatoreDTO giocatore = scores.get(i).getGiocatore();
				if (i <= 3){
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA1);
				}else if (i > 3 && i<=7){
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA2);
				}else if (i > 7 && i<=11){
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA3);
				}else{
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA4);
				}
				semifinalisti.add(giocatore);
			}

			MyLogger.getLogger().finest("Semifinalisti: "+semifinalisti.toString());
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB);
			priorita.add(PrioritaSorteggio.IMPEDITA_STESSA_REGIONE);
			partiteTurno = GeneratoreTavoliNew.generaPartite(semifinalisti, null, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);
			break;
		case 4:
			Partita finale = new Partita();
			finale.setNumeroGiocatori(4);
			finale.setNumeroTavolo(1);
			Partita[] semifinali = excelAccess.loadPartite(3, false, TipoTorneo.RadunoNazionale);
			
			TorneiUtils.checksPartiteConPiuVincitori(semifinali);
			
			if (semifinali == null){
				throw new MyException("E' stato richiesto il sorteggio per il turno 4 ma non esiste il turno 3");
			}
			for (Partita semifinale: semifinali){
				for (GiocatoreDTO semifinalista: semifinale.getGiocatori()){
					if (semifinale.isVincitore(semifinalista)){
						finale.addGiocatore(semifinalista, null);
						break;
					}
				}
			}
			partiteTurno = new Partita[]{finale};
			MyLogger.getLogger().finest("Finale: "+finale);
			break;
		default:
			MyLogger.getLogger().severe("Turno per il tipo di Torneo Raduno Nazionale non previsto: "+numeroTurno);
			throw new MyException("Turno per il tipo di Torneo Raduno Nazionale non previsto: "+numeroTurno);
		}	
		MyLogger.getLogger().exiting("Sorteggiatore", "getPartiteSorteggiateRadunoNazionale", ArrayUtils.fromPartiteToString(partiteTurno));
		return partiteTurno;
	}
	
	private static Partita[] getPartiteSorteggiateRadunoNazionale2020(ExcelAccess excelAccess, int numeroTurno){
		MyLogger.getLogger().entering("Sorteggiatore", "getPartiteSorteggiateRadunoNazionale");
		Partita[] partiteTurno = null;
		List<GiocatoreDTO> giocatoriPartecipanti = excelAccess.getListaGiocatori(true);
		List<PrioritaSorteggio> priorita = new ArrayList<PrioritaSorteggio>();
		switch (numeroTurno) {
		case 1:
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB);
			priorita.add(PrioritaSorteggio.IMPEDITA_STESSA_REGIONE);
			partiteTurno = GeneratoreTavoliNew.generaPartite(giocatoriPartecipanti, null, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);
			break;
		case 2:
			List<Partita> listaPartitePrecedenti = new ArrayList<Partita>();
			for (int i = 1; i < numeroTurno; i++){
				Partita[] partiteTurnoi = excelAccess.loadPartite(i,false,TipoTorneo.RadunoNazionale);
				if (partiteTurnoi == null){
					throw new MyException("E' stato richiesto il sorteggio per il turno "+numeroTurno+" ma non esiste il turno "+i);
				}
				listaPartitePrecedenti.addAll(Arrays.asList(partiteTurnoi));
			}
			
			TorneiUtils.checkPartiteConPiuVincitori(listaPartitePrecedenti);
			
			Partita[] partitePrecedenti = listaPartitePrecedenti.toArray(new Partita[0]);

			priorita.add(PrioritaSorteggio.VINCITORI_SEPARATI);
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB);
			priorita.add(PrioritaSorteggio.MINIMIZZAZIONE_SCONTRI_DIRETTI);
			partiteTurno = GeneratoreTavoliNew.generaPartite(giocatoriPartecipanti, partitePrecedenti, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);
			break;
		case 3:
			if (isLowestRaduno(excelAccess)){
				List<GiocatoreDTO> semifinalisti = estraiSemifinalistiLowestMasterORaduni(excelAccess, TipoTorneo.RadunoNazionale);
				MyLogger.getLogger().finest("Semifinalisti: "+semifinalisti.toString());
				priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB);
				priorita.add(PrioritaSorteggio.IMPEDITA_STESSA_REGIONE);
				partiteTurno = GeneratoreTavoliNew.generaPartite(semifinalisti, null, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);
			}else{
				List<List<GiocatoreDTO>> semifinalisti = estraiSemifinalistiHigherMasterORaduni(excelAccess, TipoTorneo.RadunoNazionale);
				MyLogger.getLogger().finest("Semifinalisti: "+semifinalisti.toString());
				priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB);
				priorita.add(PrioritaSorteggio.IMPEDITA_STESSA_REGIONE);
				List<Partita> semifinali = new ArrayList<Partita>();
				for (List<GiocatoreDTO> semifinale: semifinalisti){
					Partita[] partitaDiSemifinale = GeneratoreTavoliNew.generaPartite(semifinale, null, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);
					semifinali.addAll(Arrays.asList(partitaDiSemifinale));
				}
				partiteTurno = semifinali.toArray(new Partita[semifinali.size()]);	
				for (int index = 0; index < partiteTurno.length; index++){
					partiteTurno[index].setNumeroTavolo(index+1);
				}
			}
			break;
		case 4:
			Partita[] semifinali = excelAccess.loadPartite(3, false, TipoTorneo.RadunoNazionale);
			if (semifinali == null){
				throw new MyException("E' stato richiesto il sorteggio per il turno 4 ma non esiste il turno 3");
			}
			
			TorneiUtils.checksPartiteConPiuVincitori(semifinali);
			
			switch (semifinali.length) {
			case 2:
				partiteTurno = estrazioneFinaleLowestMasterORaduni(excelAccess, semifinali, TipoTorneo.RadunoNazionale);
				break;
			default:
				if (semifinali.length % 4 != 0){
					throw new MyException("Il numero di semifinali trovate non è multiplo di 4: "+semifinali.length);
				}else{
					int numeroFinali = semifinali.length / 4;
					partiteTurno = new Partita[numeroFinali];
					for (int indexFinale = 1; indexFinale <= numeroFinali; indexFinale++){
						int indexArray = 1+ (indexFinale -1)*4;
						Partita[] semifinaliStessoGruppo = Arrays.copyOfRange(semifinali, indexArray -1, indexArray+3);
						Partita finaleGruppo = estrazioneFinaliMasterORaduni(excelAccess, semifinaliStessoGruppo, TipoTorneo.RadunoNazionale);
						partiteTurno[indexFinale -1] = finaleGruppo;
						partiteTurno[indexFinale -1].setNumeroTavolo(indexFinale);
					}
				}
			}
			break;
		default:
			MyLogger.getLogger().severe("Turno per il tipo di Torneo Raduno Nazionale non previsto: "+numeroTurno);
			throw new MyException("Turno per il tipo di Torneo Raduno Nazionale non previsto: "+numeroTurno);
		}	
		MyLogger.getLogger().exiting("Sorteggiatore", "getPartiteSorteggiateRadunoNazionale", ArrayUtils.fromPartiteToString(partiteTurno));
		return partiteTurno;
	}
	
	private static boolean isLowestRaduno(ExcelAccess excelAccess){
		//Si devono verificare 2 condizioni: 
		//1) numero dei giocatori che hanno disputato 2 partite è sulla soglia o sotto
		//2) rimangono in gioco almeno 10 giocatori (nella vita non si sa mai) 
		boolean result = false;
		Set<GiocatoreDTO> scoresTutti = excelAccess.getPartecipantiTurnoN(2);
		List<ScorePlayer> scoresPartecipanti = excelAccess.getClassificaRadunoAlSecondoTurno(true);
		result = scoresTutti.size() <= SOGLIE_PER_RADUNI.get(0) && scoresPartecipanti.size() >= 10;
		
		return result;
	}
	
	
	private static Partita[] getPartiteSorteggiateRadunoNazionaleConTavoliDa5(ExcelAccess excelAccess, int numeroTurno){
		MyLogger.getLogger().entering("Sorteggiatore", "getPartiteSorteggiateRadunoNazionale");
		Partita[] partiteTurno = null;
		List<GiocatoreDTO> giocatoriPartecipanti = excelAccess.getListaGiocatori(true);
		List<PrioritaSorteggio> priorita = new ArrayList<PrioritaSorteggio>();
		switch (numeroTurno) {
		case 1:
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB_NON_CONSIDERANDO_I_FISSI);
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB_CONSIDERANDO_I_FISSI);
			partiteTurno = GeneratoreTavoliNew.generaPartite(giocatoriPartecipanti, null, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);
			break;
		case 2:
			List<Partita> listaPartitePrecedenti = new ArrayList<Partita>();
			for (int i = 1; i < numeroTurno; i++){
				Partita[] partiteTurnoi = excelAccess.loadPartite(i,false,TipoTorneo.RadunoNazionale);
				if (partiteTurnoi == null){
					throw new MyException("E' stato richiesto il sorteggio per il turno "+numeroTurno+" ma non esiste il turno "+i);
				}
				listaPartitePrecedenti.addAll(Arrays.asList(partiteTurnoi));
			}
			
			TorneiUtils.checkPartiteConPiuVincitori(listaPartitePrecedenti);
			
			Partita[] partitePrecedenti = listaPartitePrecedenti.toArray(new Partita[0]);
			priorita.add(PrioritaSorteggio.MINIMIZZAZIONE_PARTECIPAZIONE_TAVOLO_DA_5);
			priorita.add(PrioritaSorteggio.VINCITORI_SEPARATI);
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB_NON_CONSIDERANDO_I_FISSI);
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB_CONSIDERANDO_I_FISSI);
			priorita.add(PrioritaSorteggio.MINIMIZZAZIONE_SCONTRI_DIRETTI);
			partiteTurno = GeneratoreTavoliNew.generaPartite(giocatoriPartecipanti, partitePrecedenti, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);
			break;
		case 3:
			List<ScorePlayer> scores = excelAccess.getClassificaTorneoOpen();
			List<GiocatoreDTO> semifinalisti = new ArrayList<GiocatoreDTO>();
			if (scores.size() < SEMIFINALISTI){
				MyLogger.getLogger().severe("Impossibile elaborare le semifinali; meno di 16 giocatori: "+scores.size());
				throw new MyException("Impossibile elaborare le semifinali; meno di 16 giocatori: "+scores.size());
			}
			for (int i = 0; i < SEMIFINALISTI; i++){ //suddivisione in 4 fasce di 4 giocatori
				GiocatoreDTO giocatore = scores.get(i).getGiocatore();
				if (i <= 3){
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA1);
				}else if (i > 3 && i<=7){
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA2);
				}else if (i > 7 && i<=11){
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA3);
				}else{
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA4);
				}
				semifinalisti.add(giocatore);
			}

			MyLogger.getLogger().finest("Semifinalisti: "+semifinalisti.toString());
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB);
			priorita.add(PrioritaSorteggio.IMPEDITA_STESSA_REGIONE);
			partiteTurno = GeneratoreTavoliNew.generaPartite(semifinalisti, null, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);
			break;
		case 4:
			Partita finale = new Partita();
			finale.setNumeroGiocatori(4);
			finale.setNumeroTavolo(1);
			Partita[] semifinali = excelAccess.loadPartite(3, false, TipoTorneo.RadunoNazionale);
			
			TorneiUtils.checksPartiteConPiuVincitori(semifinali);
			
			if (semifinali == null){
				throw new MyException("E' stato richiesto il sorteggio per il turno 4 ma non esiste il turno 3");
			}
			for (Partita semifinale: semifinali){
				for (GiocatoreDTO semifinalista: semifinale.getGiocatori()){
					if (semifinale.isVincitore(semifinalista)){
						finale.addGiocatore(semifinalista, null);
						break;
					}
				}
			}
			partiteTurno = new Partita[]{finale};
			MyLogger.getLogger().finest("Finale: "+finale);
			break;
		default:
			MyLogger.getLogger().severe("Turno per il tipo di Torneo Raduno Nazionale non previsto: "+numeroTurno);
			throw new MyException("Turno per il tipo di Torneo Raduno Nazionale non previsto: "+numeroTurno);
		}	
		MyLogger.getLogger().exiting("Sorteggiatore", "getPartiteSorteggiateRadunoNazionale", ArrayUtils.fromPartiteToString(partiteTurno));
		return partiteTurno;
	}
	
	private static Partita[] getPartiteSorteggiateRadunoNazionaleConQuarti(ExcelAccess excelAccess, int numeroTurno){
		MyLogger.getLogger().entering("Sorteggiatore", "getPartiteSorteggiateRadunoNazionale");
		Partita[] partiteTurno = null;
		List<GiocatoreDTO> giocatoriPartecipanti = excelAccess.getListaGiocatori(true);
		List<PrioritaSorteggio> priorita = new ArrayList<PrioritaSorteggio>();
		switch (numeroTurno) {
		case 1:
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB);
			priorita.add(PrioritaSorteggio.IMPEDITA_STESSA_REGIONE);
			partiteTurno = GeneratoreTavoliNew.generaPartite(giocatoriPartecipanti, null, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);
			break;
		case 2:
			List<Partita> listaPartitePrecedenti = new ArrayList<Partita>();
			for (int i = 1; i < numeroTurno; i++){
				Partita[] partiteTurnoi = excelAccess.loadPartite(i,false,TipoTorneo.RadunoNazionale);
				if (partiteTurnoi == null){
					throw new MyException("E' stato richiesto il sorteggio per il turno "+numeroTurno+" ma non esiste il turno "+i);
				}
				listaPartitePrecedenti.addAll(Arrays.asList(partiteTurnoi));
			}
			
			TorneiUtils.checkPartiteConPiuVincitori(listaPartitePrecedenti);
			
			Partita[] partitePrecedenti = listaPartitePrecedenti.toArray(new Partita[0]);

			priorita.add(PrioritaSorteggio.VINCITORI_SEPARATI);
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB);
			priorita.add(PrioritaSorteggio.MINIMIZZAZIONE_SCONTRI_DIRETTI);
			partiteTurno = GeneratoreTavoliNew.generaPartite(giocatoriPartecipanti, partitePrecedenti, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);
			break;
		case 3:
			List<ScorePlayer> scores = excelAccess.getClassificaRadunoAlSecondoTurno(true);
			List<GiocatoreDTO> semiquartisti = new ArrayList<GiocatoreDTO>();
			if (haIQuartidiFinale(excelAccess)){
				short posizioneRelativa = 0;
				for (int i = SEMIFINALISTI_QUALIFICATI_DIRETTAMENTE; i < ((SEMIFINALISTI - SEMIFINALISTI_QUALIFICATI_DIRETTAMENTE))*4 + SEMIFINALISTI_QUALIFICATI_DIRETTAMENTE; i++){ //suddivisione in 4 fasce di 6 giocatori
					posizioneRelativa++;
					GiocatoreDTO giocatore = scores.get(i).getGiocatore();
					if (posizioneRelativa <= 6){
						giocatore.setRegioneProvenienza(RegioniLoader.FASCIA1);
					}else if (posizioneRelativa > 6 && posizioneRelativa <= 12){
						giocatore.setRegioneProvenienza(RegioniLoader.FASCIA2);
					}else if (posizioneRelativa > 12 && posizioneRelativa <= 18){
						giocatore.setRegioneProvenienza(RegioniLoader.FASCIA3);
					}else{
						giocatore.setRegioneProvenienza(RegioniLoader.FASCIA4);
					}
					semiquartisti.add(giocatore);
				}
				MyLogger.getLogger().finest("Quartisti: "+semiquartisti.toString());
			}else{
				if (scores.size() < SEMIFINALISTI){
					MyLogger.getLogger().severe("Impossibile elaborare le semifinali; meno di 16 giocatori: "+scores.size());
					throw new MyException("Impossibile elaborare le semifinali; meno di 16 giocatori: "+scores.size());
				}
				for (int i = 0; i < SEMIFINALISTI; i++){ //suddivisione in 4 fasce di 4 giocatori
					GiocatoreDTO giocatore = scores.get(i).getGiocatore();
					if (i <= 3){
						giocatore.setRegioneProvenienza(RegioniLoader.FASCIA1);
					}else if (i > 3 && i<=7){
						giocatore.setRegioneProvenienza(RegioniLoader.FASCIA2);
					}else if (i > 7 && i<=11){
						giocatore.setRegioneProvenienza(RegioniLoader.FASCIA3);
					}else{
						giocatore.setRegioneProvenienza(RegioniLoader.FASCIA4);
					}
					semiquartisti.add(giocatore);
				}
				MyLogger.getLogger().finest("Semifinalisti: "+semiquartisti.toString());
			}


			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB);
			priorita.add(PrioritaSorteggio.IMPEDITA_STESSA_REGIONE);
			partiteTurno = GeneratoreTavoliNew.generaPartite(semiquartisti, null, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);
			break;
		case 4:			
			if (haIQuartidiFinale(excelAccess)){
				List<ScorePlayer> scorePlayersInClassifica = excelAccess.getClassificaRadunoAlSecondoTurno(true);
				List<GiocatoreDTO> giocatoriInClassifica = TorneiUtils.fromScorePlayersToPlayers(scorePlayersInClassifica);
				List<GiocatoreDTO> semifinalisti = new ArrayList<GiocatoreDTO>();
				
				List<GiocatoreDTO> giocatoriDeiQuartiDiFinale = new ArrayList<GiocatoreDTO>();
				
				Partita[] quartiDifinale = excelAccess.loadPartite(3, false, TipoTorneo.RadunoNazionale);
				if (quartiDifinale == null){
					throw new MyException("E' stato richiesto il sorteggio per il turno 4 ma non esiste il turno 3");
				}
				
				TorneiUtils.checksPartiteConPiuVincitori(quartiDifinale);
				
				for (Partita quartoDifinale: quartiDifinale){
					giocatoriDeiQuartiDiFinale.addAll(quartoDifinale.getGiocatori());
				}
				
				//Scorro la classifica alla fine del 2° Turno fino a riempire la lista dei semifinalisti con i primi 10 controllando che gli stessi non 
				//abbiano giocato i quarti di finale: se così è significa che qualche semifinalista si è ritirato a quarti di finale già iniziati e quindi
				//si dovrà ripescare qualcuno dagli sconfitti.
				
				//Ordino gli sconfitti ai quarti di finale in base alla posizione al loro tavolo e poi alla classifica dopo le prime 2 partite
				List<GiocatoreDTO> giocatoriSconfittiAiQuartiENonRitirati = listaDeiGiocatoriSconfittiAiQuartiOrdinataPerPosizioneAlTavoloEClassificaDopo2Partite(excelAccess);
				
				for (GiocatoreDTO semifinalistaDiretto: giocatoriInClassifica){
					if (!giocatoriDeiQuartiDiFinale.contains(semifinalistaDiretto)){
						semifinalisti.add(semifinalistaDiretto); //Qui entrano non solo i semifinalisti diretti ma anche quelli che dovessero essere ripescati
																//senza aver disputato i quarti per l'abbandono di tutti i quartisti 
					}else{ //ritiro di un semifinalista diretto: ripescaggio dai quarti
						/*
						 * Se un giocatore qualificato direttamente alle semifinali comunica la sua rinuncia dopo l’inizio dei quarti di finale, 
						 * il suo posto verrà preso dal secondo migliore fra i tavoli di quarti di finale disputati. 
						 * Nel fare tale ripescaggio si dovrà tenere conto della classifica parziale dopo le prime due partite 
						 * (e non dei punti tavolo realizzati durante i quarti di finale o del distacco dal vincitore della propria partita). 
						 */
						Iterator<GiocatoreDTO> iterator = giocatoriSconfittiAiQuartiENonRitirati.iterator();
						if (iterator.hasNext()){
							GiocatoreDTO primoRipescato = iterator.next();
							semifinalisti.add(primoRipescato);
							MyLogger.getLogger().info("Ripescato per l'abbandono di un semifinalista diretto "+primoRipescato+" in base all'ordine di piazzamento al tavolo dei quarti ed alla classifica");
							iterator.remove(); //Dopo averlo ripescato lo cancello dalla lista dei papabili
						}else{
							MyLogger.getLogger().severe("Nessun quartista è ripescabile per rimpiazzare un semifinalista diretto");
						}
						

					}
					if (semifinalisti.size() == SEMIFINALISTI_QUALIFICATI_DIRETTAMENTE) break;
				}
				
				//Qui assegno i vincitori dei quarti alle semifinali
				/*
				 * Se dopo aver vinto un eventuale quarto di finale, un giocatore comunica di non voler disputare la semifinale, al suo posto 
				 * subentrerà il secondo di quel tavolo (se anche questo rinuncia subentra il terzo ed eventualmente il quarto. 
				 * Se tutti i giocatori di un tavolo rinunciano verrà ripescato il secondo migliore degli altri tavoli. 
				 * Se anche questi rinunciano subentra il terzo migliore degli altri tavoli ed eventualmente il quarto migliore degli altri tavoli). 
				 * In caso di ripescaggio fra i secondi arrivati ai tavoli dei quarti di finale disputati (ed eventualmente fra i terzi ed i quarti), 
				 * si dovrà tenere conto della classifica parziale dopo le prime due partite (e non dei punti tavolo realizzati durante i quarti 
				 * di finale o del distacco dal vincitore della propria partita).
				 */
				short quartistiAssegnati = 0;
				for (Partita quartoDifinale: quartiDifinale){
					for (GiocatoreDTO quartista: quartoDifinale.getGiocatoriOrdinatiPerPunteggio()){
						if (giocatoriInClassifica.contains(quartista)){ 
							semifinalisti.add(quartista); //o è il vincitore o se ritirato uno del suo tavolo
							quartistiAssegnati++;
							break;
						}else{
							MyLogger.getLogger().info("Il giocatore "+quartista+" ha giocato i quarti di finale ma poi si è ritirato");
						}
					}
				}
				
				MyLogger.getLogger().info("Assegnati "+ quartistiAssegnati+" quartisti alle semifinali");
				
				if (semifinalisti.size() < SEMIFINALISTI){//qualcuno non si è riusciti a recuperare
					Iterator<GiocatoreDTO> iterator = giocatoriSconfittiAiQuartiENonRitirati.iterator();
					while (iterator.hasNext() && semifinalisti.size() < 16){
						GiocatoreDTO primoRipescato = iterator.next();
						semifinalisti.add(primoRipescato);
						MyLogger.getLogger().info("Ripescato come semifinalista per l'abbandono di tutti i giocatori di un quarto di finale "+primoRipescato+" in base all'ordine di piazzamento al tavolo dei quarti ed alla classifica");
						iterator.remove(); //Dopo averlo ripescato lo cancello dalla lista dei papabili
					}
				}
				
				if (semifinalisti.size() < SEMIFINALISTI){//Manca ancora qualcuno: ripesco dalla classifica generale
					for (GiocatoreDTO semifinalistaMiracolato: giocatoriInClassifica){
						if (!semifinalisti.contains(semifinalistaMiracolato)){
							MyLogger.getLogger().info("Ripescato come semifinalista miracolato "+semifinalistaMiracolato);
							semifinalisti.add(semifinalistaMiracolato);
						}
					}
				}
				
				if (semifinalisti.size() < SEMIFINALISTI){
					MyLogger.getLogger().severe("Impossibile elaborare le semifinali; meno di 16 giocatori disponibili: "+semifinalisti.size());
					throw new MyException("Impossibile elaborare le semifinali; meno di 16 giocatori disponibili: "+semifinalisti.size());
				}
				
				/*
				 * Nel caso si siano disputati i quarti di finale, le prime dieci posizioni rimangono quelle calcolate dopo le prime due partite del sabato, 
				 * mentre le posizioni dall’11° posto al 16° posto saranno quelle relative ai sei vincitori dei quarti di finale, calcolate tenendo conto 
				 * dei punteggi totali ottenuti nelle loro tre partite giocate. 
				 */			
				
				List<ScorePlayer> semifinalistiOrdinati = excelAccess.ordinaSemifinalistiRaduno(semifinalisti);
				List<GiocatoreDTO> semifinalistiFasciati = new ArrayList<GiocatoreDTO>();
				for (int i = 0; i < SEMIFINALISTI; i++){ //suddivisione in 4 fasce di 4 giocatori
					GiocatoreDTO giocatore = semifinalistiOrdinati.get(i).getGiocatore();
					MyLogger.getLogger().finest("Semifinalista: "+giocatore);
					if (i <= 3){
						giocatore.setRegioneProvenienza(RegioniLoader.FASCIA1);
					}else if (i > 3 && i<=7){
						giocatore.setRegioneProvenienza(RegioniLoader.FASCIA2);
					}else if (i > 7 && i<=11){
						giocatore.setRegioneProvenienza(RegioniLoader.FASCIA3);
					}else{
						giocatore.setRegioneProvenienza(RegioniLoader.FASCIA4);
					}
					semifinalistiFasciati.add(giocatore);
				}
				
				priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB);
				priorita.add(PrioritaSorteggio.IMPEDITA_STESSA_REGIONE);
				partiteTurno = GeneratoreTavoliNew.generaPartite(semifinalistiFasciati, null, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);
				
			}else{
				Partita finale = new Partita();
				finale.setNumeroGiocatori(4);
				finale.setNumeroTavolo(1);
				Partita[] semifinali = excelAccess.loadPartite(3, false, TipoTorneo.RadunoNazionale);
				if (semifinali == null){
					throw new MyException("E' stato richiesto il sorteggio per il turno4 ma non esiste il turno 3");
				}
				
				TorneiUtils.checksPartiteConPiuVincitori(semifinali);
				
				for (Partita semifinale: semifinali){
					for (GiocatoreDTO semifinalista: semifinale.getGiocatori()){
						if (semifinale.isVincitore(semifinalista)){
							finale.addGiocatore(semifinalista, null);
							break;
						}
					}
				}
				partiteTurno = new Partita[]{finale};
				MyLogger.getLogger().finest("Finale: "+finale);
			}
			break;
		case 5:
			Partita finale130 = new Partita();
			finale130.setNumeroGiocatori(4);
			finale130.setNumeroTavolo(1);
			Partita[] semifinali130 = excelAccess.loadPartite(4, false, TipoTorneo.RadunoNazionale);
			if (semifinali130 == null){
				throw new MyException("E' stato richiesto il sorteggio per il turno 5 ma non esiste il turno 4");
			}
			
			TorneiUtils.checksPartiteConPiuVincitori(semifinali130);
			
			for (Partita semifinale: semifinali130){
				for (GiocatoreDTO semifinalista: semifinale.getGiocatori()){
					if (semifinale.isVincitore(semifinalista)){
						finale130.addGiocatore(semifinalista, null);
						break;
					}
				}
			}
			partiteTurno = new Partita[]{finale130};
			MyLogger.getLogger().finest("Finale: "+finale130);
			break;
		default:
			MyLogger.getLogger().severe("Turno per il tipo di Torneo Raduno Nazionale non previsto: "+numeroTurno);
			throw new MyException("Turno per il tipo di Torneo Raduno Nazionale non previsto: "+numeroTurno);
		}	
		MyLogger.getLogger().exiting("Sorteggiatore", "getPartiteSorteggiateRadunoNazionale", ArrayUtils.fromPartiteToString(partiteTurno));
		return partiteTurno;
	}
	
	private static List<GiocatoreDTO> listaDeiGiocatoriSconfittiAiQuartiOrdinataPerPosizioneAlTavoloEClassificaDopo2Partite(ExcelAccess excelAccess){
		class GiocatoreSconfitto{
			private GiocatoreDTO giocatore;
			private short posizioneAlTavolodeiQuarti;
			private int posizioneInClassificaDopoDuePartite;
			GiocatoreSconfitto(GiocatoreDTO giocatore, short posizioneAlTavolodeiQuarti, int posizioneInClassificaDopoDuePartite){
				this.giocatore = giocatore;
				this.posizioneAlTavolodeiQuarti = posizioneAlTavolodeiQuarti;
				this.posizioneInClassificaDopoDuePartite = posizioneInClassificaDopoDuePartite;
			}

			public GiocatoreDTO getGiocatore(){
				return giocatore;
			}
			public short getPosizioneAlTavoloDeiQuarti(){
				return posizioneAlTavolodeiQuarti;
			}
			public int getPosizioneInClassificaDopoDuePartite(){
				return posizioneInClassificaDopoDuePartite;
			}
		}

		List<ScorePlayer> scorePlayersInClassifica = excelAccess.getClassificaRadunoAlSecondoTurno(true);
		List<GiocatoreDTO> giocatoriInClassifica = TorneiUtils.fromScorePlayersToPlayers(scorePlayersInClassifica);
		Partita[] quartiDifinale = excelAccess.loadPartite(3, false, TipoTorneo.RadunoNazionale);
		if (quartiDifinale == null){
			throw new MyException("Non è stato trovato il turno 3");
		}
		
		List<GiocatoreSconfitto> giocatoriSconfittiAiQuarti = new ArrayList<GiocatoreSconfitto>();
		for (Partita quartoDiFinale: quartiDifinale){
			Set<GiocatoreDTO> elencoGiocatoriOrdinatiPerPunteggio = quartoDiFinale.getGiocatoriOrdinatiPerPunteggio();
			short posizione = 0;
			for (GiocatoreDTO quartista: elencoGiocatoriOrdinatiPerPunteggio){
				posizione++;
				if (posizione != 1){
					int posizioneInClassifica = giocatoriInClassifica.indexOf(quartista) + 1; 
					if (posizioneInClassifica != 0){ //caso in cui un giocatore sconfitto ai quarti si ritira e quindi non viene trovato in elenco
						GiocatoreSconfitto giocatoreSconfitto = new GiocatoreSconfitto(quartista, posizione, posizioneInClassifica);
						giocatoriSconfittiAiQuarti.add(giocatoreSconfitto);
					}
				}
			}
		}

		Comparator<GiocatoreSconfitto> sconfittiComparator = new Comparator<GiocatoreSconfitto>(){
			public int compare(GiocatoreSconfitto sconfitto1, GiocatoreSconfitto sconfitto2) {
				int result = 0;

				if (sconfitto1.getPosizioneAlTavoloDeiQuarti() < sconfitto2.getPosizioneAlTavoloDeiQuarti()){
					result = -1;
				}else if (sconfitto1.getPosizioneAlTavoloDeiQuarti() > sconfitto2.getPosizioneAlTavoloDeiQuarti()){
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

		Collections.sort(giocatoriSconfittiAiQuarti, sconfittiComparator);
		
		List<GiocatoreDTO> giocatoriSconfitti = new ArrayList<GiocatoreDTO>();
		for (GiocatoreSconfitto giocatoreSconfitto: giocatoriSconfittiAiQuarti){
			giocatoriSconfitti.add(giocatoreSconfitto.getGiocatore());
		}
		MyLogger.getLogger().fine("Lista quartisti sconfitti ordinati: "+giocatoriSconfitti);
		return giocatoriSconfitti;
	}
	
	private static boolean haIQuartidiFinale(ExcelAccess excelAccess){
		boolean result = false;
		
		Integer numeroPartecipantiPrimoTurno = 0;
		Partita[] partiteTurno1 = excelAccess.loadPartite(1,false,TipoTorneo.RadunoNazionale);
		if (partiteTurno1 != null){
			for (Partita partita: partiteTurno1){
				if (partita != null){
					numeroPartecipantiPrimoTurno += partita.getNumeroGiocatori();
				}
			}
		}
		result = numeroPartecipantiPrimoTurno > SOGLIA_PER_RADUNO_CON_QUARTI;
		return result;

	}
	
	private static Partita[] getPartiteSorteggiateMasterRisiko2015(ExcelAccess excelAccess, int numeroTurno){
		MyLogger.getLogger().entering("Sorteggiatore", "getPartiteSorteggiateMasterRisiko2015");
		Partita[] partiteTurno = null;
		List<GiocatoreDTO> giocatoriPartecipanti = excelAccess.getListaGiocatori(true);
		List<PrioritaSorteggio> priorita = new ArrayList<PrioritaSorteggio>();
		switch (numeroTurno) {
		case 1:
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB);
			partiteTurno = GeneratoreTavoliNew.generaPartite(giocatoriPartecipanti, null, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);
			break;
		case 2:
			List<Partita> listaPartitePrecedenti = new ArrayList<Partita>();
			for (int i = 1; i < numeroTurno; i++){
				Partita[] partiteTurnoi = excelAccess.loadPartite(i,false,TipoTorneo.RadunoNazionale);
				if (partiteTurnoi == null){
					throw new MyException("E' stato richiesto il sorteggio per il turno "+numeroTurno+" ma non esiste il turno "+i);
				}
				listaPartitePrecedenti.addAll(Arrays.asList(partiteTurnoi));
			}
			
			TorneiUtils.checkPartiteConPiuVincitori(listaPartitePrecedenti);
			
			Partita[] partitePrecedenti = listaPartitePrecedenti.toArray(new Partita[0]);
			priorita.add(PrioritaSorteggio.MINIMIZZAZIONE_PARTECIPAZIONE_TAVOLO_DA_5);
			priorita.add(PrioritaSorteggio.VINCITORI_SEPARATI);
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB);
			priorita.add(PrioritaSorteggio.MINIMIZZAZIONE_SCONTRI_DIRETTI);
			partiteTurno = GeneratoreTavoliNew.generaPartite(giocatoriPartecipanti, partitePrecedenti, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);
			break;
		case 3:
			//Estraggo anche i ritirati perchè tra essi ci potrebbe essere il primo in classifica; fatto il test rileggo i giocatori che giocano
			//e verifico che il primo sia ancora lo stesso di prima: in caso negativo vorrà dire che il 1° si è ritirato e si faranno 4 semifinali
			List<ScorePlayer> scores = excelAccess.getClassificaQualificazioniNazionale(false, false);
			ScorePlayer primoInClassifica = scores.get(0);
			ScorePlayer secondoInClassifica = scores.get(1);
			List<GiocatoreDTO> semifinalisti = new ArrayList<GiocatoreDTO>();
			boolean primoConDuevittorieESolitario = primoInClassifica.getNumeroVittorie() == 2 && primoInClassifica.getPunteggioB(false).compareTo(secondoInClassifica.getPunteggioB(false)) == 1;
			scores = excelAccess.getClassificaQualificazioniNazionale(true, false);
			boolean primoNonRitirato = primoInClassifica.getGiocatore().equals(scores.get(0).getGiocatore());
			if (!primoNonRitirato){
				MyLogger.getLogger().info("Il primo giocatore risulta ritirato: "+primoInClassifica+" adesso il primo è "+scores.get(0));
			}
			if (primoConDuevittorieESolitario && primoNonRitirato){
				MyLogger.getLogger().info("Tre semifinali");
				if (scores.size() <= 12){
					MyLogger.getLogger().severe("Impossibile elaborare le semifinali; meno di 13 giocatori: "+scores.size());
					throw new MyException("Impossibile elaborare le semifinali; meno di 13 giocatori: "+scores.size());
				}
				for (int i = 1; i <=12; i++){
					GiocatoreDTO giocatore = scores.get(i).getGiocatore();
					if (i <= 3){ //suddivisione in 4 fasce di 3 giocatori
						giocatore.setRegioneProvenienza(RegioniLoader.FASCIA1);
					}else if (i > 3 && i<=6){
						giocatore.setRegioneProvenienza(RegioniLoader.FASCIA2);
					}else if (i > 6 && i<=9){
						giocatore.setRegioneProvenienza(RegioniLoader.FASCIA3);
					}else{
						giocatore.setRegioneProvenienza(RegioniLoader.FASCIA4);
					}
					semifinalisti.add(giocatore);
				}
			}else{
				MyLogger.getLogger().info("Quattro semifinali: "+(!primoConDuevittorieESolitario?"Non c'è un vincitore solitario con 2 vittorie":"")+(!primoNonRitirato?"Il Primo si è ritirato":"") );
				if (scores.size() <= 15){
					MyLogger.getLogger().severe("Impossibile elaborare le semifinali; meno di 16 giocatori: "+scores.size());
					throw new MyException("Impossibile elaborare le semifinali; meno di 16 giocatori: "+scores.size());
				}
				for (int i = 0; i < 16; i++){ //suddivisione in 4 fasce di 4 giocatori
					GiocatoreDTO giocatore = scores.get(i).getGiocatore();
					if (i <= 3){
						giocatore.setRegioneProvenienza(RegioniLoader.FASCIA1);
					}else if (i > 3 && i<=7){
						giocatore.setRegioneProvenienza(RegioniLoader.FASCIA2);
					}else if (i > 7 && i<=11){
						giocatore.setRegioneProvenienza(RegioniLoader.FASCIA3);
					}else{
						giocatore.setRegioneProvenienza(RegioniLoader.FASCIA4);
					}
					semifinalisti.add(giocatore);
				}
			}
			MyLogger.getLogger().finest("Semifinalisti: "+semifinalisti.toString());
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB);
			priorita.add(PrioritaSorteggio.IMPEDITA_STESSA_REGIONE);
			partiteTurno = GeneratoreTavoliNew.generaPartite(semifinalisti, null, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);
			break;
		case 4:
			Partita finale = new Partita();
			finale.setNumeroGiocatori(4);
			finale.setNumeroTavolo(1);
			Partita[] semifinali = excelAccess.loadPartite(3, false, TipoTorneo.MasterRisiko2015);
			if (semifinali == null){
				throw new MyException("E' stato richiesto il sorteggio per il turno 4 ma non esiste il turno 3");
			}
			
			TorneiUtils.checksPartiteConPiuVincitori(semifinali);
			
			for (Partita semifinale: semifinali){
				for (GiocatoreDTO semifinalista: semifinale.getGiocatori()){
					if (semifinale.isVincitore(semifinalista)){
						finale.addGiocatore(semifinalista, null);
						break;
					}
				}
			}
			if (semifinali != null && semifinali.length < 4){
				//c'è da aggiungere il finalista diretto
				scores = excelAccess.getClassificaQualificazioniNazionale(false, false);
				primoInClassifica = scores.get(0);
				scores = excelAccess.getClassificaQualificazioniNazionale(true, false);
				primoNonRitirato = primoInClassifica.getGiocatore().equals(scores.get(0).getGiocatore());
				if (primoNonRitirato){
					GiocatoreDTO giocatore = scores.get(0).getGiocatore();
					finale.addGiocatore(giocatore, null);
				}//altrimenti dovrebbe essere ripescato il miglior secondo tra i semifinalisti				
			}
			partiteTurno = new Partita[]{finale};
			MyLogger.getLogger().finest("Finale: "+finale);
			break;
		default:
			MyLogger.getLogger().severe("Turno per il tipo di Torneo Qualificazioni Risiko non previsto: "+numeroTurno);
			throw new MyException("Turno per il tipo di Torneo Qualificazioni Risiko non previsto: "+numeroTurno);
		}	
		MyLogger.getLogger().exiting("Sorteggiatore", "getPartiteSorteggiateMasterRisiko2015", ArrayUtils.fromPartiteToString(partiteTurno));
		return partiteTurno;
	}
	
	
	private static Partita[] getPartiteSorteggiateMasterRisiko2016(ExcelAccess excelAccess, int numeroTurno){
		MyLogger.getLogger().entering("Sorteggiatore", "getPartiteSorteggiateMasterRisiko2016");
		Partita[] partiteTurno = null;
		List<GiocatoreDTO> giocatoriPartecipanti = excelAccess.getListaGiocatori(true);
		List<PrioritaSorteggio> priorita = new ArrayList<PrioritaSorteggio>();
		switch (numeroTurno) {
		case 1:
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB_NON_CONSIDERANDO_I_FISSI);
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB_CONSIDERANDO_I_FISSI);
			partiteTurno = GeneratoreTavoliNew.generaPartite(giocatoriPartecipanti, null, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);
			break;
		case 2:
			List<Partita> listaPartitePrecedenti = new ArrayList<Partita>();
			for (int i = 1; i < numeroTurno; i++){
				Partita[] partiteTurnoi = excelAccess.loadPartite(i,false,TipoTorneo.RadunoNazionale);
				if (partiteTurnoi == null){
					throw new MyException("E' stato richiesto il sorteggio per il turno "+numeroTurno+" ma non esiste il turno "+i);
				}
				listaPartitePrecedenti.addAll(Arrays.asList(partiteTurnoi));
			}
			
			TorneiUtils.checkPartiteConPiuVincitori(listaPartitePrecedenti);
			
			Partita[] partitePrecedenti = listaPartitePrecedenti.toArray(new Partita[0]);
			priorita.add(PrioritaSorteggio.MINIMIZZAZIONE_PARTECIPAZIONE_TAVOLO_DA_5);
			priorita.add(PrioritaSorteggio.VINCITORI_SEPARATI);
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB_NON_CONSIDERANDO_I_FISSI);
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB_CONSIDERANDO_I_FISSI);
			priorita.add(PrioritaSorteggio.MINIMIZZAZIONE_SCONTRI_DIRETTI);
			partiteTurno = GeneratoreTavoliNew.generaPartite(giocatoriPartecipanti, partitePrecedenti, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);
			break;
		case 3:
			if (isSuperMaster(excelAccess)){
				List<List<GiocatoreDTO>> semifinalisti = estraiSemifinalistiMasterOver100(excelAccess);
				MyLogger.getLogger().finest("Semifinalisti: "+semifinalisti.toString());
				priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB);
				priorita.add(PrioritaSorteggio.IMPEDITA_STESSA_REGIONE);
				Partita[] semifinaliDispari = GeneratoreTavoliNew.generaPartite(semifinalisti.get(0), null, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);				
				Partita[] semifinaliPari = GeneratoreTavoliNew.generaPartite(semifinalisti.get(1), null, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);
				partiteTurno = new Partita[semifinaliDispari.length+semifinaliPari.length];
				System.arraycopy(semifinaliDispari, 0, partiteTurno, 0, semifinaliDispari.length);
				System.arraycopy(semifinaliPari, 0, partiteTurno, semifinaliDispari.length, semifinaliPari.length);
			}else{
				List<GiocatoreDTO> semifinalisti = estraiSemifinalistiMasterUnder100(excelAccess);
				MyLogger.getLogger().finest("Semifinalisti: "+semifinalisti.toString());
				priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB);
				priorita.add(PrioritaSorteggio.IMPEDITA_STESSA_REGIONE);
				partiteTurno = GeneratoreTavoliNew.generaPartite(semifinalisti, null, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);
			}
			break;
		case 4:
			Partita[] semifinali = excelAccess.loadPartite(3, false, TipoTorneo.MasterRisiko);
			if (semifinali == null){
				throw new MyException("E' stato richiesto il sorteggio per il turno 4 ma non esiste il turno 3");
			}
			
			TorneiUtils.checksPartiteConPiuVincitori(semifinali);
			
			switch (semifinali.length) {
			case 3:
			case 4:
				partiteTurno = estrazioneDiSoloUnaFinale(excelAccess, semifinali);
				break;
			case 6:
			case 7:
			case 8:
				partiteTurno = estrazioneDiDueFinali(excelAccess, semifinali);
				break;
			default:
				throw new MyException("Numero di semifinali diverso da quelle previste: "+semifinali.length);
			}
			break;
		default:
			MyLogger.getLogger().severe("Turno per il tipo di Torneo Qualificazioni Risiko non previsto: "+numeroTurno);
			throw new MyException("Turno per il tipo di Torneo Qualificazioni Risiko non previsto: "+numeroTurno);
		}	
		MyLogger.getLogger().exiting("Sorteggiatore", "getPartiteSorteggiateMasterRisiko2016", ArrayUtils.fromPartiteToString(partiteTurno));
		return partiteTurno;
	}
	
	
	private static Partita[] estrazioneDiSoloUnaFinale(ExcelAccess excelAccess, Partita[] semifinali){
		List<ScorePlayer> partecipanti = excelAccess.getClassificaQualificazioniNazionale(true, false);
		Partita finale = new Partita();
		finale.setNumeroGiocatori(4);
		finale.setNumeroTavolo(1);
		for (Partita semifinale: semifinali){
			for (GiocatoreDTO semifinalista: semifinale.getGiocatoriOrdinatiPerPunteggio()){
				if (TorneiUtils.isPartecipante(partecipanti, semifinalista)){
					MyLogger.getLogger().info("Recuperato per la finale "+semifinalista+" dalla semifinale "+semifinale.getNumeroTavolo());
					finale.addGiocatore(semifinalista, null);
					break;
				}
			}
		}
		if (semifinali != null && semifinali.length < 4){
			//c'è da aggiungere il finalista diretto
			List<ScorePlayer> scores = excelAccess.getClassificaQualificazioniNazionale(false, false);
			ScorePlayer primoInClassifica = scores.get(0);
			boolean primoNonRitirato = primoInClassifica.getGiocatore().equals(partecipanti.get(0).getGiocatore());
			if (primoNonRitirato){
				GiocatoreDTO giocatore = partecipanti.get(0).getGiocatore();
				MyLogger.getLogger().info("Qualificato direttamente per la finale "+giocatore);
				finale.addGiocatore(giocatore, null);
			}
		}
		
		//Potrebbe essere che la finale ancora non è completa perchè tutti i semifinalisti di una semifinale si sono ritirati
		//oppure si è ritirato il finalista diretto: in tal caso va ripescato il miglior secondo (terzo o quarto) tra tutti i semifinalisti
		if (finale.isNotComplete()){
			List<GiocatoreDTO> giocatoriRecuperabiliOrdinati = TorneiUtils.listaDeiGiocatoriSconfittiOrdinataPerPosizioneAlTavoloEClassificaDopo2Partite(partecipanti, semifinali);
			GiocatoreDTO semifinalistaRecuperato = null;
			do{
				semifinalistaRecuperato = getMigliorSemifinalista(finale, giocatoriRecuperabiliOrdinati);
				if (semifinalistaRecuperato != null){
					MyLogger.getLogger().info("Recuperato per la finale il semifinalista "+semifinalistaRecuperato);
					finale.addGiocatore(semifinalistaRecuperato, null);
				}
			}while(finale.isNotComplete() && semifinalistaRecuperato != null);
		}
		
		
		Partita[] result = new Partita[]{finale};
		MyLogger.getLogger().finest("Finale: "+finale);
		return result;
	}
	
	private static Partita[] estrazioneFinaleLowestMasterORaduni(ExcelAccess excelAccess, Partita[] semifinali, TipoTorneo tipoTorneo){
		List<ScorePlayer> partecipanti = null;
		List<ScorePlayer> scores = null;
		if (tipoTorneo == TipoTorneo.MasterRisiko){
			scores = excelAccess.getClassificaMaster2020(false, false);
			partecipanti = excelAccess.getClassificaMaster2020(true, false);
		}else if (tipoTorneo == TipoTorneo.RadunoNazionale){
			scores = excelAccess.getClassificaRadunoAlSecondoTurno(false);
			partecipanti = excelAccess.getClassificaRadunoAlSecondoTurno(true);
		}
		Partita finale = new Partita();
		finale.setNumeroGiocatori(4);
		finale.setNumeroTavolo(1);
		for (Partita semifinale: semifinali){
			for (GiocatoreDTO semifinalista: semifinale.getGiocatoriOrdinatiPerPunteggio()){
				if (TorneiUtils.isPartecipante(partecipanti, semifinalista)){
					MyLogger.getLogger().info("Recuperato per la finale "+semifinalista+" dalla semifinale "+semifinale.getNumeroTavolo());
					finale.addGiocatore(semifinalista, null);
					break;
				}
			}
		}

		//c'è da aggiungere il o i finalisti diretti

		ScorePlayer primoInClassifica = scores.get(0);
		boolean primoNonRitirato = primoInClassifica.getGiocatore().equals(partecipanti.get(0).getGiocatore());
		if (primoNonRitirato){
			GiocatoreDTO giocatore = partecipanti.get(0).getGiocatore();
			MyLogger.getLogger().info("Qualificato direttamente per la finale "+giocatore);
			finale.addGiocatore(giocatore, null);
		}
		boolean secondoNonRitirato = true;
		ScorePlayer secondoInClassifica = scores.get(1);
		if (primoNonRitirato){
			secondoNonRitirato = secondoInClassifica.getGiocatore().equals(partecipanti.get(1).getGiocatore());
		}else{
			secondoNonRitirato = secondoInClassifica.getGiocatore().equals(partecipanti.get(0).getGiocatore());
		}
		if (secondoNonRitirato && semifinali.length == 2){
			GiocatoreDTO giocatore = secondoInClassifica.getGiocatore();
			MyLogger.getLogger().info("Qualificato direttamente per la finale "+giocatore);
			finale.addGiocatore(giocatore, null);
		}
		
		//Potrebbe essere che la finale ancora non è completa perchè tutti i semifinalisti di una semifinale si sono ritirati
		//oppure si è ritirato il o i finalisti diretti: in tal caso va ripescato il miglior secondo (terzo o quarto) tra tutti i semifinalisti
		if (finale.isNotComplete()){
			List<GiocatoreDTO> giocatoriRecuperabiliOrdinati = TorneiUtils.listaDeiGiocatoriSconfittiOrdinataPerPosizioneAlTavoloEClassificaDopo2Partite(partecipanti, semifinali);
			GiocatoreDTO semifinalistaRecuperato = null;
			do{
				semifinalistaRecuperato = getMigliorSemifinalista(finale, giocatoriRecuperabiliOrdinati);
				if (semifinalistaRecuperato != null){
					MyLogger.getLogger().info("Recuperato per la finale il semifinalista "+semifinalistaRecuperato);
					finale.addGiocatore(semifinalistaRecuperato, null);
				}
			}while(finale.isNotComplete() && semifinalistaRecuperato != null);
		}
		
		
		Partita[] result = new Partita[]{finale};
		MyLogger.getLogger().finest("Finale: "+finale);
		return result;
	}
	
	private static GiocatoreDTO getMigliorSemifinalista(Partita finale, List<GiocatoreDTO> giocatoriRecuperabiliOrdinati){
		GiocatoreDTO migliorSemifinalista = null;
		if (giocatoriRecuperabiliOrdinati != null){
			for (GiocatoreDTO giocatoreRecuperabile: giocatoriRecuperabiliOrdinati){
				if (!finale.eAlTavolo(giocatoreRecuperabile)){
					migliorSemifinalista = giocatoreRecuperabile;
					break;
				}
			}
		}	
		return migliorSemifinalista;
	}
	
	private static Partita[] estrazioneDiDueFinali(ExcelAccess excelAccess, Partita[] semifinali){
		List<ScorePlayer> partecipanti = excelAccess.getClassificaQualificazioniNazionale(true, false);
		List<ScorePlayer> scores = excelAccess.getClassificaQualificazioniNazionale(false, false);
		ScorePlayer primoInClassifica = scores.get(0);
		boolean primoNonRitirato = primoInClassifica.getGiocatore().equals(partecipanti.get(0).getGiocatore());
		
		boolean secondoNonRitirato = true;
		ScorePlayer secondoInClassifica = scores.get(1);
		if (primoNonRitirato){
			secondoNonRitirato = secondoInClassifica.getGiocatore().equals(partecipanti.get(1).getGiocatore());
		}else{
			secondoNonRitirato = secondoInClassifica.getGiocatore().equals(partecipanti.get(0).getGiocatore());
		}
		
		Partita[] semifinaliDispari = null;
		Partita[] semifinaliPari = null;
		if (semifinali.length % 2 == 0){
			semifinaliDispari = new Partita[semifinali.length / 2];
			semifinaliPari = new Partita[semifinali.length / 2];
		}else{ //vuol dire che sono 7. Uno dei due finalisti si è ritirato (oppure in futuro, secondo e terzo sono a pari merito)
			if (!primoNonRitirato){
				semifinaliDispari = new Partita[(semifinali.length / 2) + 1];
				semifinaliPari = new Partita[semifinali.length / 2];
			}else{
				semifinaliPari = new Partita[(semifinali.length / 2) + 1];
				semifinaliDispari = new Partita[semifinali.length / 2];
			}
		}

		System.arraycopy(semifinali, 0, semifinaliDispari, 0, semifinaliDispari.length);
		System.arraycopy(semifinali, semifinaliDispari.length, semifinaliPari, 0, semifinaliPari.length);
		
		Partita finaleDispari = new Partita();
		finaleDispari.setNumeroGiocatori(4);
		finaleDispari.setNumeroTavolo(1);
		for (int i = 0; i < semifinaliDispari.length; i++){
			Partita semifinale = semifinaliDispari[i];
			for (GiocatoreDTO semifinalista: semifinale.getGiocatoriOrdinatiPerPunteggio()){
				if (TorneiUtils.isPartecipante(partecipanti, semifinalista)){
					MyLogger.getLogger().info("Recuperato per la finale dispari "+semifinalista+" dalla semifinale "+semifinale.getNumeroTavolo());
					finaleDispari.addGiocatore(semifinalista, null);
					break;
				}
			}
		}

		if (finaleDispari.isNotComplete() && semifinaliDispari.length < 4){
			//c'è da aggiungere il finalista diretto
			if (primoNonRitirato){
				GiocatoreDTO giocatore = partecipanti.get(0).getGiocatore();
				MyLogger.getLogger().info("Qualificato direttamente per la finale dispari "+giocatore);
				finaleDispari.addGiocatore(giocatore, null);
			}		
		}
		
		
		//Potrebbe essere che la finale ancora non è completa perchè tutti i semifinalisti di una semifinale si sono ritirati
		//oppure si è ritirato il finalista diretto: in tal caso va ripescato il miglior secondo (terzo o quarto) tra tutti i semifinalisti
		if (finaleDispari.isNotComplete()){
			List<GiocatoreDTO> giocatoriRecuperabiliOrdinati = TorneiUtils.listaDeiGiocatoriSconfittiOrdinataPerPosizioneAlTavoloEClassificaDopo2Partite(partecipanti, semifinaliDispari);
			GiocatoreDTO semifinalistaRecuperato = null;
			do{
				semifinalistaRecuperato = getMigliorSemifinalista(finaleDispari, giocatoriRecuperabiliOrdinati);
				if (semifinalistaRecuperato != null){
					MyLogger.getLogger().info("Recuperato per la finale dispari il semifinalista "+semifinalistaRecuperato);
					finaleDispari.addGiocatore(semifinalistaRecuperato, null);
				}
			}while(finaleDispari.isNotComplete() && semifinalistaRecuperato != null);
		}
		
		
		Partita finalePari = new Partita();
		finalePari.setNumeroGiocatori(4);
		finalePari.setNumeroTavolo(1);
		for (int i = 0; i < semifinaliPari.length; i++){
			Partita semifinale = semifinaliPari[i];
			for (GiocatoreDTO semifinalista: semifinale.getGiocatoriOrdinatiPerPunteggio()){
				if (TorneiUtils.isPartecipante(partecipanti, semifinalista)){
					MyLogger.getLogger().info("Recuperato per la finale pari "+semifinalista+" dalla semifinale "+semifinale.getNumeroTavolo());
					finalePari.addGiocatore(semifinalista, null);
					break;
				}
			}
		}
		if (finalePari.isNotComplete() && semifinaliPari.length < 4){
			//c'è da aggiungere il finalista diretto
			if (secondoNonRitirato){
				if (primoNonRitirato){
					GiocatoreDTO giocatore = partecipanti.get(1).getGiocatore();
					MyLogger.getLogger().info("Qualificato direttamente per la finale pari "+giocatore);
					finalePari.addGiocatore(giocatore, null);
				}else{
					GiocatoreDTO giocatore = partecipanti.get(0).getGiocatore();
					MyLogger.getLogger().info("Qualificato direttamente per la finale pari "+giocatore);
					finalePari.addGiocatore(giocatore, null);
				}
			}		
		}
		
		//Potrebbe essere che la finale ancora non è completa perchè tutti i semifinalisti di una semifinale si sono ritirati
		//oppure si è ritirato il finalista diretto: in tal caso va ripescato il miglior secondo (terzo o quarto) tra tutti i semifinalisti
		if (finalePari.isNotComplete()){
			List<GiocatoreDTO> giocatoriRecuperabiliOrdinati = TorneiUtils.listaDeiGiocatoriSconfittiOrdinataPerPosizioneAlTavoloEClassificaDopo2Partite(partecipanti, semifinaliPari);
			GiocatoreDTO semifinalistaRecuperato = null;
			do{
				semifinalistaRecuperato = getMigliorSemifinalista(finalePari, giocatoriRecuperabiliOrdinati);
				if (semifinalistaRecuperato != null){
					MyLogger.getLogger().info("Recuperato per la finale Pari il semifinalista "+semifinalistaRecuperato);
					finalePari.addGiocatore(semifinalistaRecuperato, null);
				}
			}while(finalePari.isNotComplete() && semifinalistaRecuperato != null);
		}
		
		Partita[] result = new Partita[]{finaleDispari, finalePari};
		MyLogger.getLogger().finest("Finale Dispari: "+finaleDispari+"Finale Pari: "+finalePari);
		return result;
	}
	
	private static boolean isSuperMaster(ExcelAccess excelAccess){
		//Si devono verificare 2 condizioni: 
		//1) numero dei giocatori che hanno disputato 2 partite è sulla soglia o sopra
		//2) rimangono in gioco almeno 32 giocatori (nella vita non si sa mai) 
		boolean result = false;
		List<ScorePlayer> scoresTutti = excelAccess.getClassificaQualificazioniNazionale(false, false);
		List<ScorePlayer> scoresPartecipanti = excelAccess.getClassificaQualificazioniNazionale(true, false);
		result = scoresTutti.size() >= SOGLIA_PER_MASTER_SPLITTATO && scoresPartecipanti.size() >= 32;
		
		return result;
	}
	
	private static List<GiocatoreDTO> estraiSemifinalistiMasterUnder100(ExcelAccess excelAccess){
		//Estraggo anche i ritirati perchè tra essi ci potrebbe essere il primo in classifica; fatto il test rileggo i giocatori che giocano
		//e verifico che il primo sia ancora lo stesso di prima: in caso negativo vorrà dire che il 1° si è ritirato e si faranno 4 semifinali
		List<ScorePlayer> scores = excelAccess.getClassificaQualificazioniNazionale(false, false);
		ScorePlayer primoInClassifica = scores.get(0);
		ScorePlayer secondoInClassifica = scores.get(1);
		List<GiocatoreDTO> semifinalisti = new ArrayList<GiocatoreDTO>();
		boolean primoConDuevittorieESolitario = primoInClassifica.getNumeroVittorie() == 2 && primoInClassifica.getPunteggioB(false).compareTo(secondoInClassifica.getPunteggioB(false)) == 1;
		scores = excelAccess.getClassificaQualificazioniNazionale(true, false);
		boolean primoNonRitirato = primoInClassifica.getGiocatore().equals(scores.get(0).getGiocatore());
		if (!primoNonRitirato){
			MyLogger.getLogger().info("Il primo giocatore risulta ritirato: "+primoInClassifica+" adesso il primo è "+scores.get(0));
		}
		if (primoConDuevittorieESolitario && primoNonRitirato){
			MyLogger.getLogger().info("Tre semifinali");
			if (scores.size() <= 12){
				MyLogger.getLogger().severe("Impossibile elaborare le semifinali; meno di 13 giocatori: "+scores.size());
				throw new MyException("Impossibile elaborare le semifinali; meno di 13 giocatori: "+scores.size());
			}
			for (int i = 1; i <=12; i++){
				GiocatoreDTO giocatore = scores.get(i).getGiocatore();
				if (i <= 3){ //suddivisione in 4 fasce di 3 giocatori
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA1);
				}else if (i > 3 && i<=6){
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA2);
				}else if (i > 6 && i<=9){
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA3);
				}else{
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA4);
				}
				semifinalisti.add(giocatore);
			}
		}else{
			MyLogger.getLogger().info("Quattro semifinali: "+(!primoConDuevittorieESolitario?"Non c'è un vincitore solitario con 2 vittorie":"")+(!primoNonRitirato?"Il Primo si è ritirato":"") );
			if (scores.size() <= 15){
				MyLogger.getLogger().severe("Impossibile elaborare le semifinali; meno di 16 giocatori: "+scores.size());
				throw new MyException("Impossibile elaborare le semifinali; meno di 16 giocatori: "+scores.size());
			}
			for (int i = 0; i < 16; i++){ //suddivisione in 4 fasce di 4 giocatori
				GiocatoreDTO giocatore = scores.get(i).getGiocatore();
				if (i <= 3){
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA1);
				}else if (i > 3 && i<=7){
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA2);
				}else if (i > 7 && i<=11){
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA3);
				}else{
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA4);
				}
				semifinalisti.add(giocatore);
			}
		}
		
		return semifinalisti;
	}
	
	
	private static List<List<GiocatoreDTO>> estraiSemifinalistiMasterOver100(ExcelAccess excelAccess){
		//Estraggo anche i ritirati perchè tra essi ci potrebbero essere il primo o il secondo in classifica; 
		//fatto il test rileggo i giocatori che giocano
		//e verifico che il primo e il secondo siano ancora gli stessi di prima: in caso negativo vorrà dire 
		//che il 1° o il 2° si è ritirato e si faranno 4 semifinali
		List<ScorePlayer> scores = excelAccess.getClassificaQualificazioniNazionale(false, false);
		ScorePlayer primoInClassifica = scores.get(0);
		ScorePlayer secondoInClassifica = scores.get(1);
		ScorePlayer terzoInClassifica = scores.get(2);
		List<GiocatoreDTO> semifinalistiPari = new ArrayList<GiocatoreDTO>();
		List<GiocatoreDTO> semifinalistiDispari = new ArrayList<GiocatoreDTO>();
		boolean primoConDuevittorieESolitario = primoInClassifica.getNumeroVittorie() == 2 && primoInClassifica.getPunteggioB(false).compareTo(terzoInClassifica.getPunteggioB(false)) == 1;
		boolean secondoConDuevittorieESolitario = secondoInClassifica.getNumeroVittorie() == 2 && secondoInClassifica.getPunteggioB(false).compareTo(terzoInClassifica.getPunteggioB(false)) == 1;
		List<ScorePlayer> partecipanti = excelAccess.getClassificaQualificazioniNazionale(true, false);
		boolean primoNonRitirato = primoInClassifica.getGiocatore().equals(partecipanti.get(0).getGiocatore());
		boolean secondoNonRitirato = true;
		if (primoNonRitirato){
			secondoNonRitirato = secondoInClassifica.getGiocatore().equals(partecipanti.get(1).getGiocatore());
		}else{
			secondoNonRitirato = secondoInClassifica.getGiocatore().equals(partecipanti.get(0).getGiocatore());
		}
		if (!primoNonRitirato){
			MyLogger.getLogger().info("Il primo giocatore risulta ritirato: "+primoInClassifica+" adesso il primo è "+partecipanti.get(0));
		}
		if (!secondoNonRitirato){
			MyLogger.getLogger().info("Il secondo giocatore risulta ritirato: "+secondoInClassifica+" adesso il secondo è "+partecipanti.get(1));
		}
		
//		if (primoConDuevittorieESolitario && primoNonRitirato && secondoConDuevittorieESolitario){
//			MyLogger.getLogger().info("Tre semifinali per il torneo dispari");
//			if (partecipanti.size() <= 25){
//				MyLogger.getLogger().severe("Impossibile elaborare le semifinali; meno di 26 giocatori: "+partecipanti.size());
//				throw new MyException("Impossibile elaborare le semifinali; meno di 26 giocatori: "+partecipanti.size());
//			}
//			for (int i = 2; i <=24; i+=2){
//				GiocatoreDTO giocatore = partecipanti.get(i).getGiocatore();
//				if (i <= 6){ //suddivisione in 4 fasce di 3 giocatori
//					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA1);
//				}else if (i > 6 && i<=12){
//					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA2);
//				}else if (i > 12 && i<=18){
//					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA3);
//				}else{
//					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA4);
//				}
//				semifinalistiDispari.add(giocatore);
//			}
//		}else{
//			MyLogger.getLogger().info("Quattro semifinali per il torneo dispari");
//			if (partecipanti.size() <= 31){
//				MyLogger.getLogger().severe("Impossibile elaborare le semifinali; meno di 32 giocatori: "+partecipanti.size());
//				throw new MyException("Impossibile elaborare le semifinali; meno di 32 giocatori: "+partecipanti.size());
//			}
//			for (int i = 0; i < 32; i+=2){ //suddivisione in 4 fasce di 4 giocatori
//				GiocatoreDTO giocatore = partecipanti.get(i).getGiocatore();
//				if (i <= 6){
//					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA1);
//				}else if (i > 6 && i<=14){
//					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA2);
//				}else if (i > 14 && i<=22){
//					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA3);
//				}else{
//					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA4);
//				}
//				semifinalistiDispari.add(giocatore);
//			}
//		}
//		
//		if (secondoConDuevittorieESolitario && secondoNonRitirato){
//			MyLogger.getLogger().info("Tre semifinali per il torneo pari");
//			if (partecipanti.size() <= 25){
//				MyLogger.getLogger().severe("Impossibile elaborare le semifinali; meno di 26 giocatori: "+partecipanti.size());
//				throw new MyException("Impossibile elaborare le semifinali; meno di 26 giocatori: "+partecipanti.size());
//			}
//			for (int i = 3; i <=25; i+=2){
//				GiocatoreDTO giocatore = partecipanti.get(i).getGiocatore();
//				if (i <= 7){ //suddivisione in 4 fasce di 3 giocatori
//					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA1);
//				}else if (i > 7 && i<=13){
//					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA2);
//				}else if (i > 13 && i<=19){
//					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA3);
//				}else{
//					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA4);
//				}
//				semifinalistiPari.add(giocatore);
//			}
//		}else{
//			MyLogger.getLogger().info("Quattro semifinali per il torneo pari");
//			if (partecipanti.size() <= 31){
//				MyLogger.getLogger().severe("Impossibile elaborare le semifinali; meno di 32 giocatori: "+partecipanti.size());
//				throw new MyException("Impossibile elaborare le semifinali; meno di 32 giocatori: "+partecipanti.size());
//			}
//			for (int i = 1; i < 32; i+=2){ //suddivisione in 4 fasce di 4 giocatori
//				GiocatoreDTO giocatore = partecipanti.get(i).getGiocatore();
//				if (i <= 7){
//					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA1);
//				}else if (i > 7 && i<=15){
//					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA2);
//				}else if (i > 15 && i<=23){
//					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA3);
//				}else{
//					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA4);
//				}
//				semifinalistiPari.add(giocatore);
//			}
//		}
		
		if (primoConDuevittorieESolitario && secondoConDuevittorieESolitario && primoNonRitirato && secondoNonRitirato){
			MyLogger.getLogger().info("Coppia di tre semifinali");
			if (partecipanti.size() <= 25){
				MyLogger.getLogger().severe("Impossibile elaborare le semifinali; meno di 26 giocatori: "+partecipanti.size());
				throw new MyException("Impossibile elaborare le semifinali; meno di 26 giocatori: "+partecipanti.size());
			}
			for (int i = 2; i <=24; i+=2){
				GiocatoreDTO giocatore = partecipanti.get(i).getGiocatore();
				if (i <= 6){ //suddivisione in 4 fasce di 3 giocatori
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA1);
				}else if (i > 6 && i<=12){
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA2);
				}else if (i > 12 && i<=18){
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA3);
				}else{
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA4);
				}
				semifinalistiDispari.add(giocatore);
			}
			for (int i = 3; i <=25; i+=2){
				GiocatoreDTO giocatore = partecipanti.get(i).getGiocatore();
				if (i <= 7){ //suddivisione in 4 fasce di 3 giocatori
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA1);
				}else if (i > 7 && i<=13){
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA2);
				}else if (i > 13 && i<=19){
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA3);
				}else{
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA4);
				}
				semifinalistiPari.add(giocatore);
			}
		}else{
			MyLogger.getLogger().info("Coppia di quattro semifinali");
			if (partecipanti.size() <= 31){
				MyLogger.getLogger().severe("Impossibile elaborare le semifinali; meno di 32 giocatori: "+partecipanti.size());
				throw new MyException("Impossibile elaborare le semifinali; meno di 32 giocatori: "+partecipanti.size());
			}
			for (int i = 0; i < 32; i+=2){ //suddivisione in 4 fasce di 4 giocatori
				GiocatoreDTO giocatore = partecipanti.get(i).getGiocatore();
				if (i <= 6){
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA1);
				}else if (i > 6 && i<=14){
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA2);
				}else if (i > 14 && i<=22){
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA3);
				}else{
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA4);
				}
				semifinalistiDispari.add(giocatore);
			}
			for (int i = 1; i < 32; i+=2){ //suddivisione in 4 fasce di 4 giocatori
				GiocatoreDTO giocatore = partecipanti.get(i).getGiocatore();
				if (i <= 7){
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA1);
				}else if (i > 7 && i<=15){
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA2);
				}else if (i > 15 && i<=23){
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA3);
				}else{
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA4);
				}
				semifinalistiPari.add(giocatore);
			}
		}
		List<List<GiocatoreDTO>> semifinalisti = new ArrayList<List<GiocatoreDTO>>();
		semifinalisti.add(semifinalistiDispari);
		semifinalisti.add(semifinalistiPari);
		return semifinalisti;
	}
	

	//Determino il numero di semifinali che dipendono da due fattori
	//1) Il numero di semifinali previste in base alle soglie ed agli iscritti
	//2) Il numero di partecipanti (qualora la soglia prevista dal punto 1) sia troppo alta).
	private static int numeroSemiFinaliMasterORaduni(TipoTorneo tipoTorneo, int iscritti, int partecipanti){
		List<Integer> soglie = null;
		if (tipoTorneo == TipoTorneo.RadunoNazionale){
			soglie = SOGLIE_PER_RADUNI;
		}else if (tipoTorneo == TipoTorneo.MasterRisiko){
			soglie = SOGLIE_PER_MASTER;
		}else{
			throw new IllegalArgumentException(tipoTorneo.name());
		}
		int result = 0;
		boolean overbooking = true;
		for (int index = soglie.size() -1; index >= 1; index--){
			if (iscritti <= soglie.get(index)){
				overbooking = false;
				if (partecipanti >= index*16){
					result = index;
				}
			}else if (partecipanti >= result*16){
				break;
			}
		}
		if (overbooking) result = soglie.size();

		return result*4;
	}
	
	private static List<GiocatoreDTO> estraiSemifinalistiLowestMasterORaduni(ExcelAccess excelAccess, TipoTorneo tipoTorneo){
		//Estraggo anche i ritirati perchè tra essi ci potrebbero essere il primo o il secondo in classifica; 
		//fatto il test rileggo i giocatori che giocano
		//e verifico che il primo e il secondo siano ancora gli stessi di prima: in caso negativo vorrà dire 
		//che il 1° o il 2° si è ritirato e si faranno 4 semifinali
		List<ScorePlayer> scores = null;
		List<ScorePlayer> partecipanti = null;
		if (tipoTorneo == TipoTorneo.MasterRisiko){
			scores  = excelAccess.getClassificaMaster2020(false, false);
			partecipanti = excelAccess.getClassificaMaster2020(true, false);
		}else if (tipoTorneo == TipoTorneo.RadunoNazionale){
			scores  = excelAccess.getClassificaRadunoAlSecondoTurno(false);
			partecipanti = excelAccess.getClassificaRadunoAlSecondoTurno(true);
		}
		ScorePlayer primoInClassifica = scores.get(0);
		ScorePlayer secondoInClassifica = scores.get(1);
		ScorePlayer terzoInClassifica = scores.get(2);
		List<GiocatoreDTO> semifinalisti = new ArrayList<GiocatoreDTO>();
		boolean primoConDuevittorieESolitario = primoInClassifica.getNumeroVittorie() == 2 && primoInClassifica.getPunteggioB(false).compareTo(terzoInClassifica.getPunteggioB(false)) == 1;
		boolean secondoConDuevittorieESolitario = secondoInClassifica.getNumeroVittorie() == 2 && secondoInClassifica.getPunteggioB(false).compareTo(terzoInClassifica.getPunteggioB(false)) == 1;
		boolean primoNonRitirato = primoInClassifica.getGiocatore().equals(partecipanti.get(0).getGiocatore());
		boolean secondoNonRitirato = true;
		if (primoNonRitirato){
			secondoNonRitirato = secondoInClassifica.getGiocatore().equals(partecipanti.get(1).getGiocatore());
		}else{
			secondoNonRitirato = secondoInClassifica.getGiocatore().equals(partecipanti.get(0).getGiocatore());
		}
		if (!primoNonRitirato){
			MyLogger.getLogger().info("Il primo giocatore risulta ritirato: "+primoInClassifica+" adesso il primo è "+partecipanti.get(0));
		}
		if (!secondoNonRitirato){
			MyLogger.getLogger().info("Il secondo giocatore risulta ritirato: "+secondoInClassifica+" adesso il secondo è "+partecipanti.get(1));
		}

		MyLogger.getLogger().info("Tipo Torneo: "+tipoTorneo);
		MyLogger.getLogger().info("primoConDuevittorieESolitario: "+primoConDuevittorieESolitario);
		MyLogger.getLogger().info("primoNonRitirato: "+primoNonRitirato);
		MyLogger.getLogger().info("secondoConDuevittorieESolitario: "+secondoConDuevittorieESolitario);
		MyLogger.getLogger().info("secondoNonRitirato: "+secondoNonRitirato);
		MyLogger.getLogger().info("Numero Vittorie Secondo in Classifica: "+secondoInClassifica.getNumeroVittorie());
		MyLogger.getLogger().info("Numero partecipanti: "+partecipanti.size());
		
		int numeroSemifinali = calcoloNumeroSemifinaliMicroMaster(primoInClassifica.getPunteggioB(false), secondoInClassifica.getPunteggioB(false), terzoInClassifica.getPunteggioB(false), primoNonRitirato, secondoNonRitirato, primoInClassifica.getNumeroVittorie(), secondoInClassifica.getNumeroVittorie());
		
		switch (numeroSemifinali) {
		case 3:
			MyLogger.getLogger().info("Tre semifinali");
			if (partecipanti.size() <= 12){
				MyLogger.getLogger().severe("Impossibile elaborare le semifinali; meno di 13 giocatori: "+partecipanti.size());
				throw new MyException("Impossibile elaborare le semifinali; meno di 13 giocatori: "+partecipanti.size());
			}
			for (int i = 1; i <=12; i++){
				GiocatoreDTO giocatore = partecipanti.get(i).getGiocatore();
				if (i <= 3){ //suddivisione in 4 fasce di 3 giocatori
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA1);
				}else if (i > 3 && i<=6){
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA2);
				}else if (i > 6 && i<=9){
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA3);
				}else{
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA4);
				}
				semifinalisti.add(giocatore);
			}
			break;
		case 2:
			MyLogger.getLogger().info("Due semifinali");
			if (partecipanti.size() <= 9){
				MyLogger.getLogger().severe("Impossibile elaborare le semifinali; meno di 10 giocatori: "+partecipanti.size());
				throw new MyException("Impossibile elaborare le semifinali; meno di 10 giocatori: "+partecipanti.size());
			}
			for (int i = 2; i <=9; i++){
				GiocatoreDTO giocatore = partecipanti.get(i).getGiocatore();
				if (i <= 3){
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA1);
				}else if (i > 3 && i<=5){
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA2);
				}else if (i > 5 && i<=7){
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA3);
				}else{
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA4);
				}
				semifinalisti.add(giocatore);
			}
			break;
		case 4:
			MyLogger.getLogger().info("Quattro semifinali");
			if (partecipanti.size() < 16){
				MyLogger.getLogger().severe("Impossibile elaborare le semifinali; meno di 16 giocatori: "+partecipanti.size());
				throw new MyException("Impossibile elaborare le semifinali; meno di 16 giocatori: "+partecipanti.size());
			}
			for (int i = 0; i < 16; i++){ //suddivisione in 4 fasce di 4 giocatori
				GiocatoreDTO giocatore = partecipanti.get(i).getGiocatore();
				if (i <= 3){
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA1);
				}else if (i > 3 && i<=7){
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA2);
				}else if (i > 7 && i<=11){
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA3);
				}else{
					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA4);
				}
				semifinalisti.add(giocatore);
			}
			break;
		default:
			break;
		}
		
//		if (
//			primoConDuevittorieESolitario 
//		 && primoNonRitirato 
//		 //&& secondoInClassifica.getNumeroVittorie() != 2 
//		 && tipoTorneo == TipoTorneo.MasterRisiko
//		 ){
//			MyLogger.getLogger().info("Tre semifinali");
//			if (scores.size() <= 12){
//				MyLogger.getLogger().severe("Impossibile elaborare le semifinali; meno di 13 giocatori: "+scores.size());
//				throw new MyException("Impossibile elaborare le semifinali; meno di 13 giocatori: "+scores.size());
//			}
//			for (int i = 1; i <=12; i++){
//				GiocatoreDTO giocatore = scores.get(i).getGiocatore();
//				if (i <= 3){ //suddivisione in 4 fasce di 3 giocatori
//					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA1);
//				}else if (i > 3 && i<=6){
//					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA2);
//				}else if (i > 6 && i<=9){
//					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA3);
//				}else{
//					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA4);
//				}
//				semifinalisti.add(giocatore);
//			}
//		}else if (tipoTorneo == TipoTorneo.RadunoNazionale 
//				||(primoConDuevittorieESolitario && secondoConDuevittorieESolitario && primoNonRitirato && secondoNonRitirato)
//			    || partecipanti.size() < 16
//			){
//			MyLogger.getLogger().info("Due semifinali");
//			if (partecipanti.size() <= 9){
//				MyLogger.getLogger().severe("Impossibile elaborare le semifinali; meno di 10 giocatori: "+partecipanti.size());
//				throw new MyException("Impossibile elaborare le semifinali; meno di 10 giocatori: "+partecipanti.size());
//			}
//			for (int i = 2; i <=9; i++){
//				GiocatoreDTO giocatore = partecipanti.get(i).getGiocatore();
//				if (i <= 3){
//					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA1);
//				}else if (i > 3 && i<=5){
//					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA2);
//				}else if (i > 5 && i<=7){
//					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA3);
//				}else{
//					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA4);
//				}
//				semifinalisti.add(giocatore);
//			}
//		}else{
//			MyLogger.getLogger().info("Quattro semifinali");
//			if (partecipanti.size() < 16){
//				MyLogger.getLogger().severe("Impossibile elaborare le semifinali; meno di 16 giocatori: "+partecipanti.size());
//				throw new MyException("Impossibile elaborare le semifinali; meno di 16 giocatori: "+partecipanti.size());
//			}
//			for (int i = 0; i < 16; i++){ //suddivisione in 4 fasce di 4 giocatori
//				GiocatoreDTO giocatore = partecipanti.get(i).getGiocatore();
//				if (i <= 3){
//					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA1);
//				}else if (i > 3 && i<=7){
//					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA2);
//				}else if (i > 7 && i<=11){
//					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA3);
//				}else{
//					giocatore.setRegioneProvenienza(RegioniLoader.FASCIA4);
//				}
//				semifinalisti.add(giocatore);
//			}
//		}
		return semifinalisti;
	}
	
	private static int calcoloNumeroSemifinaliMicroMaster(BigDecimal punteggioPrimo
												, BigDecimal punteggioSecondo
												, BigDecimal punteggioTerzo
												, boolean primoNonRitirato
												, boolean secondoNonRitirato
												, int numeroVittoriePrimo
												, int numeroVittorieSecondo){
		int result = 2;
		
		boolean pariMeritoTraSecondoETerzo = punteggioSecondo.compareTo(punteggioTerzo) == 0;
		boolean soloIlPrimoConDueVittorie = numeroVittoriePrimo == 2 && numeroVittorieSecondo < 2;
		boolean pariMeritoTraPrimoSecondoETerzo = punteggioSecondo.compareTo(punteggioPrimo) == 0 && pariMeritoTraSecondoETerzo;
		
		if (pariMeritoTraPrimoSecondoETerzo || numeroVittoriePrimo < 2 || !primoNonRitirato || !secondoNonRitirato){
			result = 4;
		}else if (soloIlPrimoConDueVittorie || pariMeritoTraSecondoETerzo){
			result = 3;
		}
			
		return result;
	}
	
	private static List<List<GiocatoreDTO>> estraiSemifinalistiHigherMasterORaduni(ExcelAccess excelAccess, TipoTorneo tipoTorneo){
		Set<GiocatoreDTO> scoresTutti = excelAccess.getPartecipantiEffettiviConNPartite(2);
		List<ScorePlayer> partecipanti = null;
		
		if (tipoTorneo == TipoTorneo.MasterRisiko){
			partecipanti = excelAccess.getClassificaMaster2020(true, false);
		}else if (tipoTorneo == TipoTorneo.RadunoNazionale){
			partecipanti = excelAccess.getClassificaRadunoAlSecondoTurno(true);
		}
		
		int numeroSemifinali = numeroSemiFinaliMasterORaduni(tipoTorneo, scoresTutti.size(), partecipanti.size());
		
		MyLogger.getLogger().info("Numero di semifinali previste: "+numeroSemifinali+" Hanno giocato ["+scoresTutti.size()+"] - partecipanti ["+partecipanti.size()+"]");
		
		int numeroGruppiSemifinali = numeroSemifinali / 4;
		int numeroSemifinalisti = numeroSemifinali * 4;
		
		List<GiocatoreDTO>[] semifinaliArray = new List[numeroGruppiSemifinali];
		
		for (int indexArray = 0; indexArray < semifinaliArray.length; indexArray++){
			semifinaliArray[indexArray] = new ArrayList<GiocatoreDTO>();
		}
		//Arrays.fill(semifinaliArray, new ArrayList<GiocatoreDTO>());
		
		for (int index = 0; index < numeroSemifinalisti; index++){
			int semifinaleIndex = index%numeroGruppiSemifinali;
			List<GiocatoreDTO> semifinale = semifinaliArray[semifinaleIndex];
			GiocatoreDTO giocatore = partecipanti.get(index).getGiocatore();
			
			int fascia = (index / numeroSemifinali) +1;
			
			switch (fascia) {
			case 1:
				giocatore.setRegioneProvenienza(RegioniLoader.FASCIA1);
				break;
			case 2:
				giocatore.setRegioneProvenienza(RegioniLoader.FASCIA2);
				break;			
			case 3:
				giocatore.setRegioneProvenienza(RegioniLoader.FASCIA3);
				break;			
			case 4:
				giocatore.setRegioneProvenienza(RegioniLoader.FASCIA4);
				break;
			default:
				MyLogger.getLogger().severe("Fascia imprevista: "+fascia+" numeroSemifinalisti: ["+numeroSemifinalisti+"]");
				break;
			}
			
			semifinale.add(giocatore);
		}

		return Arrays.asList(semifinaliArray);
	}
	
	private static Partita[] getPartiteSorteggiateMasterRisiko2020(ExcelAccess excelAccess, int numeroTurno){
		MyLogger.getLogger().entering("Sorteggiatore", "getPartiteSorteggiateMasterRisiko2020");
		Partita[] partiteTurno = null;
		List<GiocatoreDTO> giocatoriPartecipanti = excelAccess.getListaGiocatori(true);
		List<PrioritaSorteggio> priorita = new ArrayList<PrioritaSorteggio>();
		switch (numeroTurno) {
		case 1:
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB_NON_CONSIDERANDO_I_FISSI);
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB_CONSIDERANDO_I_FISSI);
			partiteTurno = GeneratoreTavoliNew.generaPartite(giocatoriPartecipanti, null, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);
			break;
		case 2:
			List<Partita> listaPartitePrecedenti = new ArrayList<Partita>();
			for (int i = 1; i < numeroTurno; i++){
				Partita[] partiteTurnoi = excelAccess.loadPartite(i,false,TipoTorneo.RadunoNazionale);
				if (partiteTurnoi == null){
					throw new MyException("E' stato richiesto il sorteggio per il turno "+numeroTurno+" ma non esiste il turno "+i);
				}
				listaPartitePrecedenti.addAll(Arrays.asList(partiteTurnoi));
			}
			
			TorneiUtils.checkPartiteConPiuVincitori(listaPartitePrecedenti);
			
			Partita[] partitePrecedenti = listaPartitePrecedenti.toArray(new Partita[0]);
			priorita.add(PrioritaSorteggio.MINIMIZZAZIONE_PARTECIPAZIONE_TAVOLO_DA_5);
			priorita.add(PrioritaSorteggio.VINCITORI_SEPARATI);
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB_NON_CONSIDERANDO_I_FISSI);
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB_CONSIDERANDO_I_FISSI);
			priorita.add(PrioritaSorteggio.MINIMIZZAZIONE_SCONTRI_DIRETTI);
			partiteTurno = GeneratoreTavoliNew.generaPartite(giocatoriPartecipanti, partitePrecedenti, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);
			break;
		case 3:
			if (isLowestMasterORaduno(excelAccess)){
				List<GiocatoreDTO> semifinalisti = estraiSemifinalistiLowestMasterORaduni(excelAccess, TipoTorneo.MasterRisiko);
				MyLogger.getLogger().finest("Semifinalisti: "+semifinalisti.toString());
				priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB);
				priorita.add(PrioritaSorteggio.IMPEDITA_STESSA_REGIONE);
				partiteTurno = GeneratoreTavoliNew.generaPartite(semifinalisti, null, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);
			}else{
				List<List<GiocatoreDTO>> semifinalisti = estraiSemifinalistiHigherMasterORaduni(excelAccess, TipoTorneo.MasterRisiko);
				MyLogger.getLogger().finest("Semifinalisti: "+semifinalisti.toString());
				priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB);
				priorita.add(PrioritaSorteggio.IMPEDITA_STESSA_REGIONE);
				List<Partita> semifinali = new ArrayList<Partita>();
				for (List<GiocatoreDTO> semifinale: semifinalisti){
					Partita[] partitaDiSemifinale = GeneratoreTavoliNew.generaPartite(semifinale, null, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);
					semifinali.addAll(Arrays.asList(partitaDiSemifinale));
				}
				partiteTurno = semifinali.toArray(new Partita[semifinali.size()]);		
				for (int index = 0; index < partiteTurno.length; index++){
					partiteTurno[index].setNumeroTavolo(index+1);
				}
			}
			break;
		case 4:
			Partita[] semifinali = excelAccess.loadPartite(3, false, TipoTorneo.MasterRisiko);
			if (semifinali == null){
				throw new MyException("E' stato richiesto il sorteggio per il turno 4 ma non esiste il turno 3");
			}
			
			TorneiUtils.checksPartiteConPiuVincitori(semifinali);
			
			switch (semifinali.length) {
			case 2:
			case 3:
				partiteTurno = estrazioneFinaleLowestMasterORaduni(excelAccess, semifinali, TipoTorneo.MasterRisiko);
				break;
			default:
				if (semifinali.length % 4 != 0){
					throw new MyException("Il numero di semifinali trovate non è multiplo di 4: "+semifinali.length);
				}else{
					int numeroFinali = semifinali.length / 4;
					partiteTurno = new Partita[numeroFinali];
					for (int indexFinale = 1; indexFinale <= numeroFinali; indexFinale++){
						int indexArray = 1+ (indexFinale -1)*4;
						Partita[] semifinaliStessoGruppo = Arrays.copyOfRange(semifinali, indexArray -1, indexArray+3);
						Partita finaleGruppo = estrazioneFinaliMasterORaduni(excelAccess, semifinaliStessoGruppo, TipoTorneo.MasterRisiko);
						partiteTurno[indexFinale -1] = finaleGruppo;
						partiteTurno[indexFinale -1].setNumeroTavolo(indexFinale);
					}
				}
			}
			break;
		default:
			MyLogger.getLogger().severe("Turno per il tipo di Torneo Qualificazioni Risiko non previsto: "+numeroTurno);
			throw new MyException("Turno per il tipo di Torneo Qualificazioni Risiko non previsto: "+numeroTurno);
		}	
		MyLogger.getLogger().exiting("Sorteggiatore", "getPartiteSorteggiateMasterRisiko2020", ArrayUtils.fromPartiteToString(partiteTurno));
		return partiteTurno;
	}
	
	
	private static Partita estrazioneFinaliMasterORaduni(ExcelAccess excelAccess, Partita[] semifinali, TipoTorneo tipoTorneo){
		List<ScorePlayer> partecipanti = null;
		switch (tipoTorneo) {
		case MasterRisiko:
			partecipanti = excelAccess.getClassificaMaster2020(true, false);
			break;
		case RadunoNazionale:
			partecipanti = excelAccess.getClassificaRadunoAlSecondoTurno(true);
			break;
		default:
			break;
		}

		Partita finale = new Partita();
		finale.setNumeroGiocatori(4);
		finale.setNumeroTavolo(1);
		for (Partita semifinale: semifinali){
			for (GiocatoreDTO semifinalista: semifinale.getGiocatoriOrdinatiPerPunteggio()){
				if (TorneiUtils.isPartecipante(partecipanti, semifinalista)){
					MyLogger.getLogger().info("Recuperato per la finale "+semifinalista+" dalla semifinale "+semifinale.getNumeroTavolo());
					finale.addGiocatore(semifinalista, null);
					break;
				}
			}
		}

		//Potrebbe essere che la finale ancora non è completa perchè tutti i semifinalisti di una semifinale si sono ritirati
		//In tal caso va ripescato il miglior secondo (terzo o quarto) tra tutti i semifinalisti
		if (finale.isNotComplete()){
			List<GiocatoreDTO> giocatoriRecuperabiliOrdinati = TorneiUtils.listaDeiGiocatoriSconfittiOrdinataPerPosizioneAlTavoloEClassificaDopo2Partite(partecipanti, semifinali);
			GiocatoreDTO semifinalistaRecuperato = null;
			do{
				semifinalistaRecuperato = getMigliorSemifinalista(finale, giocatoriRecuperabiliOrdinati);
				if (semifinalistaRecuperato != null){
					MyLogger.getLogger().info("Recuperato per la finale il semifinalista "+semifinalistaRecuperato);
					finale.addGiocatore(semifinalistaRecuperato, null);
				}
			}while(finale.isNotComplete() && semifinalistaRecuperato != null);
		}
		
		MyLogger.getLogger().finest("Finale: "+finale);
		return finale;
	}
	
	private static boolean isLowestMasterORaduno(ExcelAccess excelAccess){
		//Si devono verificare 2 condizioni: 
		//1) numero dei giocatori che hanno disputato 2 partite è sulla soglia o sotto
		//2) rimangono in gioco almeno 10 giocatori (nella vita non si sa mai) 
		boolean result = false;
		Set<GiocatoreDTO> scoresTutti = excelAccess.getPartecipantiEffettiviConNPartite(2);
		List<ScorePlayer> scoresPartecipanti = excelAccess.getClassificaMaster2020(true, false);
		result = scoresTutti.size() <= SOGLIE_PER_MASTER.get(0) && scoresPartecipanti.size() >= 10;
		
		return result;
	}

	private static Partita[] getPartiteSorteggiateNazionaleRisiko(ExcelAccess excelAccess, int numeroTurno){
		MyLogger.getLogger().entering("Sorteggiatore", "getPartiteSorteggiateNazionaleRisiko");
		Partita[] partiteTurno = null;
		List<GiocatoreDTO> giocatoriPartecipanti = excelAccess.getListaGiocatori(true);
		List<PrioritaSorteggio> priorita = new ArrayList<PrioritaSorteggio>();
		switch (numeroTurno) {
		case 1:
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB);
			priorita.add(PrioritaSorteggio.IMPEDITA_STESSA_REGIONE);
			partiteTurno = GeneratoreTavoliNew.generaPartite(giocatoriPartecipanti, null, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);
			break;
		case 2:
			List<Partita> listaPartitePrecedenti = new ArrayList<Partita>();
			for (int i = 1; i < numeroTurno; i++){
				Partita[] partiteTurnoi = excelAccess.loadPartite(i,false,TipoTorneo.NazionaleRisiKo);
				if (partiteTurnoi == null){
					MyLogger.getLogger().severe("E' stato richiesto il sorteggio per il turno "+numeroTurno+" ma non esiste il turno "+i);
					throw new MyException("E' stato richiesto il sorteggio per il turno "+numeroTurno+" ma non esiste il turno "+i);
				}
				listaPartitePrecedenti.addAll(Arrays.asList(partiteTurnoi));
			}
			
			Partita[] partitePrecedenti = listaPartitePrecedenti.toArray(new Partita[0]);
			priorita.add(PrioritaSorteggio.ALLA_GRECA);
			partiteTurno = GeneratoreTavoliNew.generaPartite(giocatoriPartecipanti, partitePrecedenti, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);
			break;
		case 3:
//			List<ScorePlayer> classificaAlTerzoTurno = excelAccess.getClassificaNazionaleRisiko();
//			partiteTurno = TavoliVuotiCreator.genera(TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, classificaAlTerzoTurno.size());
//			for (int index = 0; index < classificaAlTerzoTurno.size(); index++){
//				int resto = index % 4;
//				ScorePlayer scorePlayer = classificaAlTerzoTurno.get(index);
//				partiteTurno[resto].addGiocatore(scorePlayer.getGiocatore(), 0F);
//			}
			listaPartitePrecedenti = new ArrayList<Partita>();
			for (int i = 1; i < numeroTurno; i++){
				Partita[] partiteTurnoi = excelAccess.loadPartite(i,false,TipoTorneo.NazionaleRisiKo);
				if (partiteTurnoi == null){
					MyLogger.getLogger().severe("E' stato richiesto il sorteggio per il turno "+numeroTurno+" ma non esiste il turno "+i);
					throw new MyException("E' stato richiesto il sorteggio per il turno "+numeroTurno+" ma non esiste il turno "+i);
				}
				listaPartitePrecedenti.addAll(Arrays.asList(partiteTurnoi));
			}
			
			partitePrecedenti = listaPartitePrecedenti.toArray(new Partita[0]);
			priorita.add(PrioritaSorteggio.MINIMIZZAZIONE_SCONTRI_DIRETTI);
			partiteTurno = GeneratoreTavoliNew.generaPartite(giocatoriPartecipanti, partitePrecedenti, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);
			MyLogger.getLogger().fine(""+partiteTurno.length);
			break;
		}	
		MyLogger.getLogger().exiting("Sorteggiatore", "getPartiteSorteggiateNazionaleRisiko", ArrayUtils.fromPartiteToString(partiteTurno));
		return partiteTurno;
	}
	
	private static Partita[] getPartiteSorteggiateNazionaleRisikoSperimentale(ExcelAccess excelAccess, int numeroTurno){
		MyLogger.getLogger().entering("Sorteggiatore", "getPartiteSorteggiateNazionaleRisikoSperimentale");
		Partita[] partiteTurno = null;
		List<GiocatoreDTO> giocatoriPartecipanti = excelAccess.getListaGiocatori(true);
		List<PrioritaSorteggio> priorita = new ArrayList<PrioritaSorteggio>();
		switch (numeroTurno) {
		case 1:
			partiteTurno = GeneratoreTavoliNew.generaPartite(giocatoriPartecipanti, null, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);
			break;
		case 2:
		case 3:
			List<Partita> listaPartitePrecedenti = new ArrayList<Partita>();
			for (int i = 1; i < numeroTurno; i++){
				Partita[] partiteTurnoi = excelAccess.loadPartite(i,false,TipoTorneo.NazionaleRisiKo);
				if (partiteTurnoi == null){
					MyLogger.getLogger().severe("E' stato richiesto il sorteggio per il turno "+numeroTurno+" ma non esiste il turno "+i);
					throw new MyException("E' stato richiesto il sorteggio per il turno "+numeroTurno+" ma non esiste il turno "+i);
				}
				listaPartitePrecedenti.addAll(Arrays.asList(partiteTurnoi));
			}
			
			Partita[] partitePrecedenti = listaPartitePrecedenti.toArray(new Partita[0]);
			priorita.add(PrioritaSorteggio.MINIMIZZAZIONE_SCONTRI_DIRETTI);
			partiteTurno = GeneratoreTavoliNew.generaPartite(giocatoriPartecipanti, partitePrecedenti, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);
			break;
		}	
		MyLogger.getLogger().exiting("Sorteggiatore", "getPartiteSorteggiateNazionaleRisikoSperimentale", ArrayUtils.fromPartiteToString(partiteTurno));
		return partiteTurno;
	}
	
	
	private static Partita[] getPartiteSorteggiateTorneoGufo(ExcelAccess excelAccess, int numeroTurno){
		MyLogger.getLogger().entering("Sorteggiatore", "getPartiteSorteggiateTorneoGufo");
		Partita[] partiteTurno = null;
		List<GiocatoreDTO> giocatoriPartecipanti = excelAccess.getListaGiocatori(true);
		List<PrioritaSorteggio> priorita = new ArrayList<PrioritaSorteggio>();
		switch (numeroTurno) {
		case 1:
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB);
			partiteTurno = GeneratoreTavoliNew.generaPartite(giocatoriPartecipanti, null, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);
			break;
		default:
			List<Partita> listaPartitePrecedenti = new ArrayList<Partita>();
			for (int i = 1; i < numeroTurno; i++){
				Partita[] partiteTurnoi = excelAccess.loadPartite(i,false,TipoTorneo.TorneoGufo);
				if (partiteTurnoi == null){
					MyLogger.getLogger().severe("E' stato richiesto il sorteggio per il turno "+numeroTurno+" ma non esiste il turno "+i);
					throw new MyException("E' stato richiesto il sorteggio per il turno "+numeroTurno+" ma non esiste il turno "+i);
				}
				listaPartitePrecedenti.addAll(Arrays.asList(partiteTurnoi));
			}
			
			TorneiUtils.checkPartiteConPiuVincitori(listaPartitePrecedenti);
			
			Partita[] partitePrecedenti = listaPartitePrecedenti.toArray(new Partita[0]);
			priorita.add(PrioritaSorteggio.MINIMIZZAZIONE_PARTECIPAZIONE_TAVOLO_DA_5);
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB);
			priorita.add(PrioritaSorteggio.MINIMIZZAZIONE_SCONTRI_DIRETTI);
			partiteTurno = GeneratoreTavoliNew.generaPartite(giocatoriPartecipanti, partitePrecedenti, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);
			break;
		}	
		MyLogger.getLogger().exiting("Sorteggiatore", "getPartiteSorteggiateTorneoGufo", ArrayUtils.fromPartiteToString(partiteTurno));
		return partiteTurno;
	}
	
	private static Partita[] getPartiteSorteggiateCampionatoGufo(ExcelAccess excelAccess, int numeroTurno){
		MyLogger.getLogger().entering("Sorteggiatore", "getPartiteSorteggiateCampionatoGufo");
		Partita[] partiteTurno = null;
		List<GiocatoreDTO> giocatoriPartecipanti = excelAccess.getListaGiocatori(true);
		List<PrioritaSorteggio> priorita = new ArrayList<PrioritaSorteggio>();
		switch (numeroTurno) {
		case 1:
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB);
			partiteTurno = GeneratoreTavoliNew.generaPartite(giocatoriPartecipanti, null, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5_SE_UNO_SOLO_ALTRIMENTI_DA_3_COL_MORTO, priorita);
			break;
		default:
			List<Partita> listaPartitePrecedenti = new ArrayList<Partita>();
			for (int i = 1; i < numeroTurno; i++){
				Partita[] partiteTurnoi = excelAccess.loadPartite(i,false,TipoTorneo.CampionatoGufo);
				if (partiteTurnoi == null){
					throw new MyException("E' stato richiesto il sorteggio per il turno "+numeroTurno+" ma non esiste il turno "+i);
				}
				listaPartitePrecedenti.addAll(Arrays.asList(partiteTurnoi));
			}
			
			TorneiUtils.checkPartiteConPiuVincitori(listaPartitePrecedenti);
			
			Partita[] partitePrecedenti = listaPartitePrecedenti.toArray(new Partita[0]);
			priorita.add(PrioritaSorteggio.MINIMIZZAZIONE_PARTECIPAZIONE_TAVOLO_DA_3);
			priorita.add(PrioritaSorteggio.MINIMIZZAZIONE_PARTECIPAZIONE_TAVOLO_DA_5);
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB);
			priorita.add(PrioritaSorteggio.MINIMIZZAZIONE_SCONTRI_DIRETTI);
			partiteTurno = GeneratoreTavoliNew.generaPartite(giocatoriPartecipanti, partitePrecedenti, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5_SE_UNO_SOLO_ALTRIMENTI_DA_3_COL_MORTO, priorita);
			break;
		}	
		MyLogger.getLogger().exiting("Sorteggiatore", "getPartiteSorteggiateCampionatoGufo", ArrayUtils.fromPartiteToString(partiteTurno));
		return partiteTurno;
	}
	
	private static Partita[] getPartiteSorteggiateBGL(ExcelAccess excelAccess, int numeroTurno){
		MyLogger.getLogger().entering("Sorteggiatore", "getPartiteSorteggiateBGL");
		Partita[] partiteTurno = null;
		List<GiocatoreDTO> giocatoriPartecipanti = excelAccess.getListaGiocatori(true);
		List<PrioritaSorteggio> priorita = new ArrayList<PrioritaSorteggio>();
		switch (numeroTurno) {
		case 1:
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB);
			partiteTurno = GeneratoreTavoliNew.generaPartite(giocatoriPartecipanti, null, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_3, priorita);
			break;
		default:
			List<Partita> listaPartitePrecedenti = new ArrayList<Partita>();
			for (int i = 1; i < numeroTurno; i++){
				Partita[] partiteTurnoi = excelAccess.loadPartite(i,false,TipoTorneo.CampionatoGufo);
				if (partiteTurnoi == null){
					throw new MyException("E' stato richiesto il sorteggio per il turno "+numeroTurno+" ma non esiste il turno "+i);
				}
				listaPartitePrecedenti.addAll(Arrays.asList(partiteTurnoi));
			}
			Partita[] partitePrecedenti = listaPartitePrecedenti.toArray(new Partita[0]);
			priorita.add(PrioritaSorteggio.MINIMIZZAZIONE_PARTECIPAZIONE_TAVOLO_DA_3);
			priorita.add(PrioritaSorteggio.MINIMIZZAZIONE_SCONTRI_DIRETTI);
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB);
			partiteTurno = GeneratoreTavoliNew.generaPartite(giocatoriPartecipanti, partitePrecedenti, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_3, priorita);
			break;
		}	
		MyLogger.getLogger().exiting("Sorteggiatore", "getPartiteSorteggiateBGL", ArrayUtils.fromPartiteToString(partiteTurno));
		return partiteTurno;
	}

	private static Partita[] getPartiteSorteggiateBGLSvizzera(ExcelAccess excelAccess, int numeroTurno){
		MyLogger.getLogger().entering("Sorteggiatore", "getPartiteSorteggiateBGLSvizzera");
		Partita[] partiteTurno = null;
		List<GiocatoreDTO> giocatoriPartecipanti = excelAccess.getListaGiocatori(true);
		List<PrioritaSorteggio> priorita = new ArrayList<PrioritaSorteggio>();
		switch (numeroTurno) {
		case 1:
			excelAccess.getListaGiocatori(true);
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB);
			partiteTurno = GeneratoreTavoliNew.generaPartite(giocatoriPartecipanti, null, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_3, priorita);
			break;
		default:
			List<ScorePlayer> scores = excelAccess.getClassificaBGL();
			List<GiocatoreDTO> giocatoriInClassifica = new ArrayList<GiocatoreDTO>();
			for (ScorePlayer scorePlayer: scores){
				giocatoriInClassifica.add(scorePlayer.getGiocatore());
			}
			giocatoriInClassifica.retainAll(giocatoriPartecipanti);
			priorita.add(PrioritaSorteggio.ALLA_SVIZZERA);

			partiteTurno = GeneratoreTavoliNew.generaPartite(giocatoriInClassifica, null, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_3, priorita);
			break;
		}	
		MyLogger.getLogger().exiting("Sorteggiatore", "getPartiteSorteggiateBGLSvizzera", ArrayUtils.fromPartiteToString(partiteTurno));
		return partiteTurno;
	}
	
	private static Partita[] getPartiteSorteggiate1vs1(ExcelAccess excelAccess, int numeroTurno){
		MyLogger.getLogger().entering("Sorteggiatore", "getPartiteSorteggiate1vs1");
		Partita[] partiteTurno = null;
		List<GiocatoreDTO> giocatoriPartecipanti = excelAccess.getListaGiocatori(true);
		List<PrioritaSorteggio> priorita = new ArrayList<PrioritaSorteggio>();
		switch (numeroTurno) {
		case 1:
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB);
			partiteTurno = GeneratoreTavoliNew.generaPartite(giocatoriPartecipanti, null, TipoTavoli.DA_2_ED_EVENTUALMENTE_DA_3, priorita);
			break;
		default:
			List<Partita> listaPartitePrecedenti = new ArrayList<Partita>();
			for (int i = 1; i < numeroTurno; i++){
				Partita[] partiteTurnoi = excelAccess.loadPartite(i,false,TipoTorneo._1vs1_);
				if (partiteTurnoi == null){
					throw new MyException("E' stato richiesto il sorteggio per il turno "+numeroTurno+" ma non esiste il turno "+i);
				}
				listaPartitePrecedenti.addAll(Arrays.asList(partiteTurnoi));
			}
			Partita[] partitePrecedenti = listaPartitePrecedenti.toArray(new Partita[0]);
			priorita.add(PrioritaSorteggio.MINIMIZZAZIONE_PARTECIPAZIONE_TAVOLO_DA_3);
			priorita.add(PrioritaSorteggio.MINIMIZZAZIONE_SCONTRI_DIRETTI);
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB);
			partiteTurno = GeneratoreTavoliNew.generaPartite(giocatoriPartecipanti, partitePrecedenti, TipoTavoli.DA_2_ED_EVENTUALMENTE_DA_3, priorita);
			break;
		}	
		MyLogger.getLogger().exiting("Sorteggiatore", "getPartiteSorteggiate1vs1", ArrayUtils.fromPartiteToString(partiteTurno));
		return partiteTurno;
	}
	
	private static Partita[] getPartiteSorteggiate1VS1Svizzera(ExcelAccess excelAccess, int numeroTurno){
		MyLogger.getLogger().entering("Sorteggiatore", "getPartiteSorteggiate1VS1Svizzera");
		Partita[] partiteTurno = null;
		List<GiocatoreDTO> giocatoriPartecipanti = excelAccess.getListaGiocatori(true);
		List<PrioritaSorteggio> priorita = new ArrayList<PrioritaSorteggio>();
		switch (numeroTurno) {
		case 1:
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB);
			partiteTurno = GeneratoreTavoliNew.generaPartite(giocatoriPartecipanti, null, TipoTavoli.DA_2_ED_EVENTUALMENTE_DA_2_COL_MORTO, priorita);
			break;
		default:
			List<ScorePlayer> scores = excelAccess.getClassificaBGL();
			List<GiocatoreDTO> giocatoriInClassifica = new ArrayList<GiocatoreDTO>();
			for (ScorePlayer scorePlayer: scores){
				giocatoriInClassifica.add(scorePlayer.getGiocatore());
			}
			giocatoriInClassifica.retainAll(giocatoriPartecipanti);
			priorita.add(PrioritaSorteggio.ALLA_SVIZZERA);

			partiteTurno = GeneratoreTavoliNew.generaPartite(giocatoriInClassifica, null, TipoTavoli.DA_2_ED_EVENTUALMENTE_DA_2_COL_MORTO, priorita);
			break;
		}	
		MyLogger.getLogger().exiting("Sorteggiatore", "getPartiteSorteggiate1VS1Svizzera", ArrayUtils.fromPartiteToString(partiteTurno));
		return partiteTurno;
	}
	
	private static Partita[] getPartiteSorteggiateTorneoASquadre(ExcelAccess excelAccess, int numeroTurno){
		MyLogger.getLogger().entering("Sorteggiatore", "getPartiteSorteggiateTorneoASquadre");
		Partita[] partiteTurno = null;
		List<GiocatoreDTO> giocatoriPartecipanti = excelAccess.getListaGiocatori(true);
		List<PrioritaSorteggio> priorita = new ArrayList<PrioritaSorteggio>();
		switch (numeroTurno) {
		case 1:
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB);
			priorita.add(PrioritaSorteggio.MINIMIZZAZIONE_SCONTRI_DIRETTI_TRA_CLUB);
			partiteTurno = GeneratoreTavoliNew.generaPartite(giocatoriPartecipanti, null, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);
			break;
		default:
			List<Partita> listaPartitePrecedenti = new ArrayList<Partita>();
			for (int i = 1; i < numeroTurno; i++){
				Partita[] partiteTurnoi = excelAccess.loadPartite(i,false,TipoTorneo.TorneoASquadre);
				if (partiteTurnoi == null){
					MyLogger.getLogger().severe("E' stato richiesto il sorteggio per il turno "+numeroTurno+" ma non esiste il turno "+i);
					throw new MyException("E' stato richiesto il sorteggio per il turno "+numeroTurno+" ma non esiste il turno "+i);
				}
				listaPartitePrecedenti.addAll(Arrays.asList(partiteTurnoi));
			}
			
			//TorneiUtils.checkPartiteConPiuVincitori(listaPartitePrecedenti);
			
			Partita[] partitePrecedenti = listaPartitePrecedenti.toArray(new Partita[0]);
			priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB);
			priorita.add(PrioritaSorteggio.MINIMIZZAZIONE_SCONTRI_DIRETTI_TRA_CLUB);
			priorita.add(PrioritaSorteggio.MINIMIZZAZIONE_SCONTRI_DIRETTI);
			priorita.add(PrioritaSorteggio.MINIMIZZAZIONE_SCONTRI_DIRETTI_GIOCATORE_CLUB);			
			partiteTurno = GeneratoreTavoliNew.generaPartite(giocatoriPartecipanti, partitePrecedenti, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);
			break;
		}	
		MyLogger.getLogger().exiting("Sorteggiatore", "getPartiteSorteggiateTorneoGufo", ArrayUtils.fromPartiteToString(partiteTurno));
		return partiteTurno;
	}
	
}
