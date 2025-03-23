package it.desimone.risiko.torneo.batch;

import it.desimone.risiko.torneo.dto.ClubDTO;
import it.desimone.risiko.torneo.dto.GiocatoreDTO;
import it.desimone.risiko.torneo.dto.Partita;
import it.desimone.risiko.torneo.dto.RegioneDTO;
import it.desimone.risiko.torneo.dto.SchedaClassifica;
import it.desimone.risiko.torneo.dto.SchedaClassifica.RigaClassifica;
import it.desimone.risiko.torneo.dto.SchedaTorneo;
import it.desimone.risiko.torneo.dto.SchedaTurno;
import it.desimone.risiko.torneo.dto.Torneo;
import it.desimone.risiko.torneo.scorecomparator.ScoreCNSComparator;
import it.desimone.risiko.torneo.scorecomparator.ScoreCampionatoComparator;
import it.desimone.risiko.torneo.scorecomparator.ScoreNazionaleRisikoComparator;
import it.desimone.risiko.torneo.scorecomparator.ScoreQualificazioniNazionaleComparator;
import it.desimone.risiko.torneo.scorecomparator.ScoreRadunoComparator;
import it.desimone.risiko.torneo.scorecomparator.ScoreRadunoNazionale2020Comparator;
import it.desimone.risiko.torneo.scorecomparator.ScoreSemifinalistiRadunoComparator;
import it.desimone.risiko.torneo.scorecomparator.ScoreTorneoOpenComparator;
import it.desimone.risiko.torneo.scoreplayer.ScorePlayer;
import it.desimone.risiko.torneo.scoreplayer.ScorePlayerCampionatoGufo;
import it.desimone.risiko.torneo.scoreplayer.ScorePlayerClassificator;
import it.desimone.risiko.torneo.scoreplayer.ScorePlayerNazionaleRisiko;
import it.desimone.risiko.torneo.scoreplayer.ScorePlayerOpen;
import it.desimone.risiko.torneo.scoreplayer.ScorePlayerQualificazioniNazionale;
import it.desimone.risiko.torneo.scoreplayer.ScorePlayerRaduno;
import it.desimone.risiko.torneo.scoreplayer.ScorePlayerTorneoBGL;
import it.desimone.risiko.torneo.scoreplayer.ScorePlayerTorneoGufo;
import it.desimone.risiko.torneo.scoreplayer.ScoreTeam;
import it.desimone.risiko.torneo.scoreplayer.ScoreTeamCNS;
import it.desimone.risiko.torneo.scoreplayer.ScoreTeamClassificator;
import it.desimone.risiko.torneo.utils.ClubLoader;
import it.desimone.risiko.torneo.utils.MatchAnalyzer;
import it.desimone.risiko.torneo.utils.MatchAnalyzer.AnomaliaConfrontiClub;
import it.desimone.risiko.torneo.utils.MatchAnalyzer.MatchAnomali;
import it.desimone.risiko.torneo.utils.MatchAnalyzer.MatchGrids;
import it.desimone.risiko.torneo.utils.RegioniLoader;
import it.desimone.risiko.torneo.utils.TipoTorneo;
import it.desimone.risiko.torneo.utils.TorneiUtils;
import it.desimone.utils.DateUtils;
import it.desimone.utils.MyException;
import it.desimone.utils.MyLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.ss.util.RegionUtil;


public class ExcelAccess{

	private final ClubLoader clubLoader = new ClubLoader();
	private final RegioniLoader regioniLoader = new RegioniLoader();
	
	public static final String SCHEDA_TORNEO 		= "TORNEO";
	public static final String SCHEDA_ISCRITTI 		= "Iscritti";
	public static final String SCHEDA_LOG 			= "Log";
	public static final String SCHEDA_CLASSIFICA	= "Classifica";
	public static final String SCHEDA_CLASSIFICA_RIDOTTA	= "Classifica Ridotta";
	public static final String SCHEDA_CLASSIFICA_A_SQUADRE	= "Classifica a Squadre";
	public static final String SCHEDA_TURNO_SUFFIX	= "° Turno";
	public static final String SCHEDA_STATISTICHE	= "STATISTICHE";
		
	String pathFileExcel;
	private String fileName;
	short posizioneId 			= 0;
	short posizioneNome 		= 1;
	short posizioneCognome 		= 2;
	short posizioneNick 		= 3;
	short posizioneClub 		= 4;
	short posizionePresenza 	= 5;
	short posizioneRegione 		= 6;
	//short posizioneEmail 		= 7;
	short posizioneDataDiNascita= 7;
	short posizioneIdNazionale	= 8;

	private Workbook foglioTorneo;
	private CreationHelper creationHelper;
	private CellStyle styleCell;
	private CellStyle styleCellPoints;
	private CellStyle styleCellId;
	private CellStyle styleIntestazione;
	
	private CellStyle styleCellClassODD;
	private CellStyle styleCellClassEVEN;
	private CellStyle styleCellClassWinODD;
	private CellStyle styleCellClassWinEVEN;
	
	private CellStyle styleCellClass0;
	private CellStyle styleCellClass1;
	private CellStyle styleCellClass2;
	private CellStyle styleCellClassD;
	private CellStyle styleCellClassIntestStat;
	
	private static short indiceFormatTreDecimali = -1;
		
	public ExcelAccess(File fileExcel){
		try {
			pathFileExcel = fileExcel.getPath();
			fileName = fileExcel.getName();
		}finally{}
	}
	
	public String getFileName() {
		return fileName;
	}

	public void openFileExcel(){
		if (pathFileExcel != null){
			try{
				foglioTorneo = WorkbookFactory.create(new FileInputStream(pathFileExcel));
				creationHelper = foglioTorneo.getCreationHelper();
				DataFormat df = foglioTorneo.createDataFormat();
				short formato = df.getFormat("0.000");
				if (formato != -1){
					indiceFormatTreDecimali = formato;
					MyLogger.getLogger().finer("Trovato indice per formato 0.000: "+String.valueOf(df.getFormat("0.000")));
				}
				creaStili();
			}catch(FileNotFoundException fe){
				throw new MyException("Non trovato il file Excel "+pathFileExcel+": "+fe.getMessage());
			}catch(IOException ioe){
				throw new MyException("Impossibile accedere al file Excel "+pathFileExcel+": "+ioe.getMessage());
			}catch(InvalidFormatException ife){
				throw new MyException("Formato non valido del file Excel "+pathFileExcel+": "+ife.getMessage());
			}
		}
	}
	
	private void creaStili(){
		styleCellId = foglioTorneo.createCellStyle();
		styleCellId.setVerticalAlignment(VerticalAlignment.TOP);

		styleCell = foglioTorneo.createCellStyle();
		styleCell.setAlignment(HorizontalAlignment.LEFT);
		Font font = foglioTorneo.createFont();
		font.setFontName(HSSFFont.FONT_ARIAL);
		font.setColor(IndexedColors.BLACK.getIndex());
		styleCell.setFont(font);
		styleCell.setWrapText(true);
		styleCell.setVerticalAlignment(VerticalAlignment.TOP);
		styleCell.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
		styleCell.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		
		styleCellPoints = foglioTorneo.createCellStyle();
		styleCellPoints.setAlignment(HorizontalAlignment.RIGHT);
		styleCellPoints.setFont(font);
		styleCellPoints.setWrapText(true);
		styleCellPoints.setVerticalAlignment(VerticalAlignment.TOP);
		styleCellPoints.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
		styleCellPoints.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styleCellPoints.setLocked(false);
		
		styleIntestazione = foglioTorneo.createCellStyle();		
		styleIntestazione.setAlignment(HorizontalAlignment.CENTER);
		styleIntestazione.setBorderBottom(BorderStyle.THICK);
		styleIntestazione.setBorderTop(BorderStyle.THICK);
		styleIntestazione.setBorderLeft(BorderStyle.THIN);
		styleIntestazione.setBorderRight(BorderStyle.THIN);
		Font fontIntestazione = foglioTorneo.createFont();
		font.setBold(true);
		styleIntestazione.setFont(fontIntestazione);
		styleIntestazione.setWrapText(false);
		styleIntestazione.setVerticalAlignment(VerticalAlignment.CENTER);
		styleIntestazione.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
		styleIntestazione.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	}
	
	public List<GiocatoreDTO> getListaGiocatori(boolean partecipanti){
		//List<GiocatoreDTO> listaGiocatori = new ArrayList<GiocatoreDTO>();
		Set<GiocatoreDTO> listaGiocatori = new HashSet<GiocatoreDTO>();
		Sheet sheet = foglioTorneo.getSheet(SCHEDA_ISCRITTI);
		
		//int ultimaRiga = sheet.getLastRowNum();
		//System.out.println("Ultima riga: "+ultimaRiga);

		for (int i = 3; i<=sheet.getLastRowNum(); i++){
			Row row = sheet.getRow(i);
		//Iterator it = sheet.rowIterator();
		//for (int i = 0; it.hasNext() ; i++){		
			//HSSFRow row = (HSSFRow) it.next();
			if (row != null && i >= 3){
				GiocatoreDTO giocatore = null;
				try{
					giocatore = getGiocatoreFromRow(row);
				}catch(Exception me){
					throw new MyException(me,"Riga n° "+(i+1)+" della scheda "+SCHEDA_ISCRITTI);
				}
				if (giocatore != null && giocatore.getId() != null && giocatore.getNome() != null && giocatore.getNome().length() >0){
					if (!partecipanti || giocatore.getPresenteTorneo()){
						boolean nonGiaPresente = listaGiocatori.add(giocatore);
						if (!nonGiaPresente){
							throw new MyException("E' presente l'ID "+giocatore.getId()+ " duplicato sulla scheda "+SCHEDA_ISCRITTI);
						}
					}
				}
			}
		}
		
		return new ArrayList<GiocatoreDTO>(listaGiocatori);
	}
	
	public GiocatoreDTO getGiocatore(Integer id){
		Sheet sheet = foglioTorneo.getSheet(SCHEDA_ISCRITTI);
		
		for (int i = 3; i<sheet.getLastRowNum(); i++){
			Row row = sheet.getRow(i);
			if (row != null){
				GiocatoreDTO giocatore = null;
				try{
					giocatore = getGiocatoreFromRow(row);
				}catch(Exception me){
					throw new MyException(me," Riga n° "+(i+1)+" della scheda "+SCHEDA_ISCRITTI);
				}
				if (giocatore != null && giocatore.getId() != null && giocatore.getId().equals(id)){
					return giocatore;
				}
			}
		}
		
		return null;
	}
	
	public boolean checkSheet(String nomeSheet){
		return foglioTorneo.getSheet(nomeSheet) != null;
	}
	
	public SchedaTorneo leggiSchedaTorneo(){
		SchedaTorneo schedaTorneo = null;
		Sheet sheet = foglioTorneo.getSheet(SCHEDA_TORNEO);
		if (sheet != null){
			schedaTorneo = new SchedaTorneo();
			Row rowSede = sheet.getRow(1);
			String sede = determinaValoreCella(rowSede, (short)3);
			Row rowOrganizzatore = sheet.getRow(2);
			String organizzatore = determinaValoreCella(rowOrganizzatore, (short)3);
			Row rowNomeTorneo = sheet.getRow(3);
			String nomeTorneo = determinaValoreCella(rowNomeTorneo, (short)3);
			Row rowTipoTorneo = sheet.getRow(4);
			String tipologiaTorneo = determinaValoreCella(rowTipoTorneo, (short)3);
			Row rowNumeroTurni = sheet.getRow(5);
			String numeroTurni = determinaValoreCella(rowNumeroTurni, (short)3);
			Row rowNote = sheet.getRow(6);
			String note = determinaValoreCella(rowNote, (short)3);
			List<Date> dataTurni = new ArrayList<Date>();
			for (int indexDate = 7; indexDate <=36; indexDate++){
				Row rowDataTurno = sheet.getRow(indexDate);
				if (rowDataTurno != null){
					Cell cellaDataTurno   = rowDataTurno.getCell((short)3);
					if (cellaDataTurno != null){
						try{
							Date dataTurno = cellaDataTurno.getDateCellValue();
							try{
								DateUtils.formatDate(dataTurno);
							}catch(IllegalArgumentException iae){
								MyLogger.getLogger().severe("Errore nel parsing della data del turno "+(indexDate -6)+" della scheda "+SCHEDA_TORNEO+": "+ iae.getMessage());
								throw new MyException("Errore di formato della data del turno "+(indexDate -6)+" della scheda "+SCHEDA_TORNEO+": impostarla nel formato dd/mm/aaaa");
							}
							if (dataTurno != null){
								dataTurni.add(dataTurno);
							}
						}catch(IllegalStateException ise){
							MyLogger.getLogger().severe("Errore nel parsing della data a riga "+(indexDate+1)+" della scheda "+SCHEDA_TORNEO+": "+ ise.getMessage());
						}
					}
				}
			}
			schedaTorneo.setSedeTorneo(sede);
			schedaTorneo.setOrganizzatore(organizzatore);
			schedaTorneo.setNomeTorneo(nomeTorneo);

			SchedaTorneo.TipoTorneo tipoTorneo = SchedaTorneo.TipoTorneo.parseTipoTorneo(tipologiaTorneo);
			schedaTorneo.setTipoTorneo(tipoTorneo);
			
			int numeroTurniInt = 0;
			if (numeroTurni != null && numeroTurni.trim().length() > 0){
				try{
					numeroTurniInt = (int) (Double.valueOf(numeroTurni.trim()) * 1);
				}catch(Exception e){
					MyLogger.getLogger().severe("Errore nel parsing della cella numero Turni: "+numeroTurni);
				}
			}
			schedaTorneo.setNumeroTurni(numeroTurniInt);
			schedaTorneo.setDataTurni(dataTurni);
			schedaTorneo.setNote(note);
		}
		
		return schedaTorneo;
	}
	
	public SchedaClassifica leggiSchedaClassifica(){
		SchedaClassifica schedaClassifica = null;
		Sheet sheet = foglioTorneo.getSheet(SCHEDA_CLASSIFICA_RIDOTTA);
		if (sheet != null){
			schedaClassifica = new SchedaClassifica();
			//Integer nRows = sheet.getLastRowNum();
			Iterator it = sheet.rowIterator();
			it.next(); //Si salta la prima riga che è l'header
			int numeroRiga = 1;
			while (it.hasNext()){
				numeroRiga++;
				Row row = (Row) it.next();
				String posizione = determinaValoreCella(row, (short)0);
				String punteggio = determinaValoreCella(row, (short)3);
				String id = determinaValoreCella(row, (short)4);
				Integer posizioneInt = null;
				BigDecimal punteggioB = null;
				Integer idInt = null;
				if (posizione != null){
					try{
						posizioneInt = Double.valueOf(posizione).intValue();
					}catch(Exception e){
						MyLogger.getLogger().severe("Scheda "+SCHEDA_CLASSIFICA_RIDOTTA+":posizione non numerica alla riga "+numeroRiga);
					}
				}
				if (punteggio != null){
					try{
						punteggioB = new BigDecimal(punteggio);
					}catch(Exception e){
						MyLogger.getLogger().severe("Scheda "+SCHEDA_CLASSIFICA_RIDOTTA+":punteggio non numerico alla riga "+numeroRiga);
					}
				}
				if (id != null){
					try{
						idInt = Double.valueOf(id).intValue();
					}catch(Exception e){
						MyLogger.getLogger().severe("Scheda "+SCHEDA_CLASSIFICA_RIDOTTA+":Id non numerico alla riga "+numeroRiga);
					}
				}
				if (posizioneInt != null || punteggioB != null || idInt != null){
					RigaClassifica rigaClassifica = new RigaClassifica();
					rigaClassifica.setIdGiocatore(idInt);
					rigaClassifica.setPosizioneGiocatore(posizioneInt);
					rigaClassifica.setPunteggioFinaleGiocatore(punteggioB);
					schedaClassifica.addRigaClassifica(rigaClassifica);
				}
			}
		}
		
		return schedaClassifica;
	}
	
	
	private String determinaValoreCella(Row row, short posizioneCella){
		if (row == null) return null;
		Cell cella   = row.getCell(posizioneCella);
		if (cella == null) return null;
		String result 		= "";
		int tipoCella   = cella.getCellType();
		if (tipoCella == Cell.CELL_TYPE_STRING){
			result 		    = cella.getRichStringCellValue().getString();
		}else if (tipoCella == Cell.CELL_TYPE_NUMERIC){
			result			= Double.toString(cella.getNumericCellValue());
		}else if (tipoCella == Cell.CELL_TYPE_FORMULA){
			result			= cella.getRichStringCellValue().getString();
		}else if (tipoCella != Cell.CELL_TYPE_BLANK){
			MyLogger.getLogger().info("Impossibile leggere la cella della colonna in posizione "+posizioneCella+ " della riga "+ (row.getRowNum()+1)+" perchè di tipo imprevisto: "+tipoCella);
		}
		return result;
	}
	
	private GiocatoreDTO getGiocatoreFromRow(Row row){
		GiocatoreDTO giocatore = new GiocatoreDTO();
		Short id = null;
		try{
			Cell cellId = row.getCell(posizioneId);
			if (cellId == null) return null;
			id 			= (short)cellId.getNumericCellValue();
		}catch(NumberFormatException nfe){
			MyLogger.getLogger().info("Colonna ID Nazionale con valore non numerico: "+nfe.getMessage());
			throw new MyException(nfe,"Colonna ID con valore non numerico");
		}catch(IllegalStateException ise){
			MyLogger.getLogger().info("Colonna ID Nazionale con valore non numerico: "+ise.getMessage());
			throw new MyException(ise,"Colonna ID con valore non numerico");
		}
		
		String nome = determinaValoreCella(row, posizioneNome);
		String cognome = determinaValoreCella(row, posizioneCognome);
		//String email = determinaValoreCella(row, posizioneEmail);
		String nick = determinaValoreCella(row, posizioneNick);
		Date dataDiNascita = null;
		try{
			Cell cellaDataDiNascita   = row.getCell(posizioneDataDiNascita);
			if (cellaDataDiNascita != null){
				dataDiNascita = cellaDataDiNascita.getDateCellValue();
				if (dataDiNascita != null){
					try{
						DateUtils.formatDate(dataDiNascita);
					}catch(IllegalArgumentException iae){
						MyLogger.getLogger().severe("Errore nel parsing della data di nascita a riga "+(row.getRowNum()+1)+" della scheda "+SCHEDA_ISCRITTI+": "+ iae.getMessage());
						throw new MyException("Errore di formato della data di nascita a riga "+(row.getRowNum()+1)+" della scheda "+SCHEDA_ISCRITTI+": impostarla nel formato dd/mm/aaaa");
					}
					dataDiNascita = DateUtils.normalizeDate(dataDiNascita);
				}
			}
		}catch(IllegalStateException ise){
			MyLogger.getLogger().severe("Errore nel parsing della data di nascita a riga "+(row.getRowNum()+1)+" della scheda "+SCHEDA_ISCRITTI+": "+ ise.getMessage());
			throw new MyException(ise,"Errore di formato della data di nascita a riga "+(row.getRowNum()+1)+" della scheda "+SCHEDA_ISCRITTI+": impostarla nel formato dd/mm/aaaa");
		}
		String regione = determinaValoreCella(row, posizioneRegione);
		String club = determinaValoreCella(row, posizioneClub);
		String presenza = determinaValoreCella(row, posizionePresenza);
		
		//String regione 		= row.getCell(posizioneRegione).getRichStringCellValue().getString();
		//String club 		= row.getCell(posizioneClub).getRichStringCellValue().getString();
		//String presenza		= row.getCell(posizionePresenza).getRichStringCellValue().getString();
		giocatore.setId(id.intValue());
		giocatore.setNome(nome);
		giocatore.setCognome(cognome);
		//giocatore.setEmail(email);
		giocatore.setDataDiNascita(dataDiNascita);
		giocatore.setNick(nick);
		if (regione != null && regione.length() >0){
			try{
				Field field = RegioniLoader.class.getField(regione.trim());
				RegioneDTO regioneDTO = (RegioneDTO) field.get(regioniLoader);
				giocatore.setRegioneProvenienza(regioneDTO);
			}catch(NoSuchFieldException nfe){
				RegioneDTO regioneDTO = new RegioneDTO(regione);
				giocatore.setRegioneProvenienza(regioneDTO);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		if (club != null && club.length() >0){
			try{
				Field field = ClubLoader.class.getField(club.trim());
				ClubDTO clubDTO = (ClubDTO) field.get(clubLoader);
				giocatore.setClubProvenienza(clubDTO);
			}catch(NoSuchFieldException nfe){
				ClubDTO clubDTO = new ClubDTO(club);
				giocatore.setClubProvenienza(clubDTO);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		giocatore.setPresenteTorneo(presenza!=null&&presenza.equalsIgnoreCase("SI"));		
		
		Integer idNazionale = null;
		try{
			Cell cellIdNazionale = row.getCell(posizioneIdNazionale);
			if (cellIdNazionale != null){
				idNazionale 	= (int)cellIdNazionale.getNumericCellValue();
			}
		}catch(NumberFormatException nfe){
			MyLogger.getLogger().info("Colonna ID Nazionale con valore non numerico: "+nfe.getMessage());
			throw new MyException(nfe,"Colonna ID Nazionale con valore non numerico");
		}catch(IllegalStateException ise){
			//MyLogger.getLogger().finer("Colonna ID Nazionale con valore non numerico al ID "+id+": "+ise.getMessage());
			//throw new MyException(ise,"Colonna ID Nazionale con valore non numerico");
		}
		if (idNazionale == null || idNazionale > 0 || (nome != null && nome.equalsIgnoreCase(GiocatoreDTO.ANONIMO.getNome()) && cognome != null && cognome.equalsIgnoreCase(GiocatoreDTO.ANONIMO.getCognome()))){ //Patch messa perchè quando si legge da un xlsx le API mettono zero come valore se la cella è vuota
			giocatore.setIdNazionale(idNazionale);
		}
		return giocatore;
	}
	
	private Sheet creaSchedaTurno(String nomeScheda, boolean hidden){
		Sheet result = foglioTorneo.getSheet(nomeScheda);
		if (result == null){
			result = foglioTorneo.createSheet(nomeScheda);
			if (hidden){
				result.setColumnHidden((short)0,true);
				result.setColumnHidden((short)3,true);
				result.setColumnHidden((short)6,true);
				result.setColumnHidden((short)9,true);
				result.setColumnHidden((short)12,true);
			}else{
				result.setColumnWidth((short)0,(short)1000);
				result.setColumnWidth((short)3,(short)1000);
				result.setColumnWidth((short)6,(short)1000);
				result.setColumnWidth((short)9,(short)1000);
				result.setColumnWidth((short)12,(short)1000);
			}
			result.setColumnWidth((short)1,(short)6000);
			result.setColumnWidth((short)4,(short)6000);
			result.setColumnWidth((short)7,(short)6000);
			result.setColumnWidth((short)10,(short)6000);
			result.setColumnWidth((short)13,(short)6000);
			result.setColumnWidth((short)2,(short)2000);
			result.setColumnWidth((short)5,(short)2000);
			result.setColumnWidth((short)8,(short)2000);
			result.setColumnWidth((short)11,(short)2000);
			result.setColumnWidth((short)14,(short)2000);
		}
		return result;
	}
	
	public void scriviPartite(String nomeTurno, Partita partita){
			//HSSFSheet schedaTurno = creaSchedaTurno(nomeTurno, true);
			Sheet schedaTurno = creaSchedaTurno(nomeTurno, true);
			int prossimaRiga = schedaTurno.getLastRowNum()+(schedaTurno.getLastRowNum()==0?0:1);
			/* TEST quinta scheda */
			//int prossimaRiga = schedaTurno.getLastRowNum()+1;
			Row rowIntestazione = schedaTurno.createRow(prossimaRiga);
			Cell cellIntestazione = rowIntestazione.createCell((short)0);
			cellIntestazione.setCellStyle(styleIntestazione);
			cellIntestazione.setCellValue("Tavolo N°"+partita.getNumeroTavolo());
			CellRangeAddress region = new CellRangeAddress(prossimaRiga,prossimaRiga,(short)0,(short)(partita.getNumeroGiocatori()*3-1));
			try {
				RegionUtil.setBorderBottom(BorderStyle.THIN,region,schedaTurno);
				RegionUtil.setBorderTop(BorderStyle.THIN,region,schedaTurno);
				RegionUtil.setBorderLeft(BorderStyle.THIN,region,schedaTurno);
				RegionUtil.setBorderRight(BorderStyle.THIN,region,schedaTurno);
			} catch (/*Nestable*/Exception e) {
				throw new MyException("Errore nell'utilizzo della classe RegionUtil: "+e.getMessage());
			}
			schedaTurno.addMergedRegion(region);
			Row row = schedaTurno.createRow(prossimaRiga+1);
			//row.setHeight((short)800);
			short counterCell = 0;
			Sheet sheetIscritti = foglioTorneo.getSheet(SCHEDA_ISCRITTI);
			int numeroIscritti = sheetIscritti == null?0:sheetIscritti.getLastRowNum()+1;
			for (GiocatoreDTO giocatore: partita.getGiocatori()){
				Cell cellId = row.createCell(counterCell++);
				cellId.setCellType(CellType.NUMERIC);
				cellId.setCellValue(giocatore.getId());
				cellId.setCellStyle(styleCellId);
				Cell cellNominativo = row.createCell(counterCell++);
				cellNominativo.setCellStyle(styleCell);
				String nominativo = giocatore.getNome()+" "+giocatore.getCognome();
				if (giocatore.getClubProvenienza() != null){
					nominativo +="\n"+giocatore.getClubProvenienza();
				}
				cellNominativo.setCellValue(creationHelper.createRichTextString(nominativo));
				Cell cellPunteggio = row.createCell(counterCell++);
				cellPunteggio.setCellStyle(styleCellPoints);
				cellPunteggio.setCellType(CellType.NUMERIC);
				cellPunteggio.setCellValue(partita.getTavolo().get(giocatore));
			}
			//foglioTorneo.setActiveSheet(foglioTorneo.getSheetIndex(nomeTurno));
		}

	public void scriviPartiteFaseFinaleQualificazioneRisiko(String nomeTurno, Partita partita){
		Sheet schedaTurno = creaSchedaTurno(nomeTurno, false);
		int prossimaRiga = schedaTurno.getLastRowNum()+(schedaTurno.getLastRowNum()==0?0:1);
		/* TEST quinta scheda */
		//int prossimaRiga = schedaTurno.getLastRowNum()+1;
		Row rowIntestazione = schedaTurno.createRow(prossimaRiga);
		Cell cellIntestazione = rowIntestazione.createCell((short)0);
		cellIntestazione.setCellStyle(styleIntestazione);
		cellIntestazione.setCellValue("Tavolo N°"+partita.getNumeroTavolo());
		CellRangeAddress region = new CellRangeAddress(prossimaRiga,prossimaRiga,(short)0,(short)(partita.getNumeroGiocatori()*3-1));
		try {
			RegionUtil.setBorderBottom(BorderStyle.THIN,region,schedaTurno);
			RegionUtil.setBorderTop(BorderStyle.THIN,region,schedaTurno);
			RegionUtil.setBorderLeft(BorderStyle.THIN,region,schedaTurno);
			RegionUtil.setBorderRight(BorderStyle.THIN,region,schedaTurno);
		} catch (/*Nestable*/Exception e) {
			throw new MyException("Errore nell'utilizzo della classe RegionUtil: "+e.getMessage());
		}
		schedaTurno.addMergedRegion(region);
		Row row = schedaTurno.createRow(prossimaRiga+1);
		short counterCell = 0;
		Sheet sheetIscritti = foglioTorneo.getSheet(SCHEDA_ISCRITTI);
		int numeroIscritti = sheetIscritti == null?0:sheetIscritti.getLastRowNum()+1;
		for (GiocatoreDTO giocatore: partita.getGiocatori()){
			Cell cellId = row.createCell(counterCell++);
			cellId.setCellType(CellType.NUMERIC);
			cellId.setCellValue(giocatore.getId());
			cellId.setCellStyle(styleCellId);
			Cell cellNominativo = row.createCell(counterCell++);
			cellNominativo.setCellStyle(styleCell);
			String nominativo = giocatore.getNome()+" "+giocatore.getCognome();
			if (giocatore.getClubProvenienza() != null){
				nominativo +="\n"+giocatore.getClubProvenienza();
			}
			char indiceColonnaId = trascodificaIndiceColonna(cellId.getColumnIndex());
			int  indiceRigaId = prossimaRiga+2;
			String formula = "VLOOKUP("+indiceColonnaId+indiceRigaId+",Iscritti!A4:E"+numeroIscritti+",2,FALSE) & \" \" & VLOOKUP("+indiceColonnaId+indiceRigaId+",Iscritti!A4:E"+numeroIscritti+",3,FALSE) & \" \" & CHAR(10) & VLOOKUP("+indiceColonnaId+indiceRigaId+",Iscritti!A4:E"+numeroIscritti+",5,FALSE)";
			cellNominativo.setCellFormula(formula);
			cellNominativo.setCellValue(creationHelper.createRichTextString(nominativo));

			Cell cellPunteggio = row.createCell(counterCell++);
			cellPunteggio.setCellStyle(styleCell);
			cellPunteggio.setCellType(CellType.NUMERIC);
			cellPunteggio.setCellValue(partita.getTavolo().get(giocatore));
		}
		foglioTorneo.setActiveSheet(foglioTorneo.getSheetIndex(nomeTurno));
	}
	
	private char trascodificaIndiceColonna(int index){
		char result = ' ';
		switch (index) {
		case 0:
			result = 'A';
			break;
		case 1:
			result = 'B';
			break;
		case 2:
			result = 'C';
			break;
		case 3:
			result = 'D';
			break;
		case 4:
			result = 'E';
			break;
		case 5:
			result = 'F';
			break;
		case 6:
			result = 'G';
			break;
		case 7:
			result = 'H';
			break;
		case 8:
			result = 'I';
			break;
		case 9:
			result = 'J';
			break;
		case 10:
			result = 'K';
			break;
		case 11:
			result = 'L';
			break;
		case 12:
			result = 'M';
			break;
		case 13:
			result = 'N';
			break;
		case 14:
			result = 'O';
			break;
		case 15:
			result = 'P';
			break;
		case 16:
			result = 'Q';
			break;
		case 17:
			result = 'R';
			break;
		case 18:
			result = 'S';
			break;
		case 19:
			result = 'T';
			break;
		case 20:
			result = 'U';
			break;
		case 21:
			result = 'V';
			break;
		case 22:
			result = 'W';
			break;
		case 23:
			result = 'X';
			break;
		case 24:
			result = 'Y';
			break;
		case 25:
			result = 'Z';
			break;
		default:
			break;
		}
		
		return result;
	}
	
	public List<SchedaTurno> leggiSchedeTurno(){
		List<SchedaTurno> turni = null;
		String[] sheetNames = getSheetNames();
		
		if (sheetNames != null){
			for (String sheetName: sheetNames){
				if (sheetName != null && sheetName.endsWith(ExcelAccess.SCHEDA_TURNO_SUFFIX)){
					try{
						String numeroTurno = sheetName.substring(0, sheetName.indexOf(ExcelAccess.SCHEDA_TURNO_SUFFIX));
						if (numeroTurno != null){
							numeroTurno = numeroTurno.trim();
							Integer numeroTurnoInt = Integer.valueOf(numeroTurno);
							Partita[] partite = loadPartite(numeroTurnoInt, true, null);
							if (turni == null){
								turni = new ArrayList<SchedaTurno>();
							}
							SchedaTurno schedaTurno = new SchedaTurno();
							schedaTurno.setNumeroTurno(numeroTurnoInt);
							schedaTurno.setPartite(partite);
							turni.add(schedaTurno);
						}
					}catch(Exception e){
						MyLogger.getLogger().severe("Errore nella lettura delle schede Turni: "+e.getMessage());
						throw new MyException(e);
					}
				}
			}
		}
		return turni;
	}
	
	public SchedaTurno leggiSchedaTurno(String sheetName){
		SchedaTurno schedaTurno = null;
		try{
			String numeroTurno = sheetName.substring(0, sheetName.indexOf(ExcelAccess.SCHEDA_TURNO_SUFFIX));
			if (numeroTurno != null){
				numeroTurno = numeroTurno.trim();
				Integer numeroTurnoInt = Integer.valueOf(numeroTurno);
				Partita[] partite = loadPartite(numeroTurnoInt, true, null);
				schedaTurno = new SchedaTurno();
				schedaTurno.setNumeroTurno(numeroTurnoInt);
				schedaTurno.setPartite(partite);

			}
		}catch(Exception e){
			MyLogger.getLogger().severe("Errore nella lettura delle schede Turni: "+e.getMessage());
			throw new MyException(e);
		}
		return schedaTurno;
	}
	
	private Partita[] loadPartite(String nomeTurno, boolean withGhost, TipoTorneo tipoTorneo){
		List<GiocatoreDTO> giocatori = getListaGiocatori(false);
		Collections.sort(giocatori); //Serve perchï¿½ poi su di essa verrï¿½ fatta una binarySearch
		Partita[] partite = null;
		Sheet schedaTurno = foglioTorneo.getSheet(nomeTurno);
		if (schedaTurno != null){
			/* Modificato dopo che nella quinta scheda misteriosamente la prima riga viene letta */
			//partite = new Partita[schedaTurno.getLastRowNum()/2];
			int ultimaRiga = schedaTurno.getLastRowNum();
			if (ultimaRiga%2 ==0){ultimaRiga++;} //Nel caso in cui ci siano righe sporche in numero dispari
			partite = new Partita[(ultimaRiga+1)/2];
			Iterator it = schedaTurno.rowIterator();
			//it.next(); //Si salta la prima riga

			for (int i=0; it.hasNext(); i++){
				try{
					Row row = (Row) it.next();
					if (i%2 == 0){
						partite[i/2] = new Partita();
						partite[i/2].setNumeroTavolo(i/2+1);
					}else{
						try{
							setPartitaFromRow(partite[(i-1)/2], row, giocatori, withGhost);
						}catch(MyException me){
							throw new MyException(me,me.getMessage()+" Scheda "+nomeTurno+" riga "+(i+1));
						}
					}
				}catch(MyException me){
					throw me;
				}catch(Exception e){
					throw new MyException(e,"Scheda "+nomeTurno+" riga "+(i+1)+ " "+e.getClass());
				}
			}
			partite = shrinkPartite(partite);

		//}else{
			//throw new MyException("Non ï¿½ stata trovata la scheda con il "+nomeTurno);
		}
		return partite;
	}
	
	//Puï¿½ darsi a volte che la lib POI dichiari piï¿½ righe di quelle reali e quindi che io abbia Partite vuote: questo metodo le cancella
	private Partita[] shrinkPartite(Partita[] partite){
		List<Partita> partiteShrinked = Arrays.asList(partite);
		partiteShrinked = new ArrayList<Partita>(partiteShrinked);
		Iterator<Partita> iterator = partiteShrinked.iterator();
		while (iterator.hasNext()){
			Partita partita = iterator.next();
			if (partita == null || partita.getNumeroGiocatori() == 0) iterator.remove();
		}
		return partiteShrinked.toArray(new Partita[partiteShrinked.size()]);
	}
	
	public Partita[] loadPartite(int numeroTurno, boolean withGhost, TipoTorneo tipoTorneo){
		return loadPartite(getNomeTurno(numeroTurno), withGhost, tipoTorneo);
	}
	
	public Set<GiocatoreDTO> getPartecipantiEffettivi(){
		Set<GiocatoreDTO> partecipantiEffettivi = new HashSet<GiocatoreDTO>();
		for(int numeroTurno = 1; ; numeroTurno++){
			Partita[] partite = loadPartite(numeroTurno, true, null);
			if (partite != null){
				for (Partita partita: partite){
					if (partita != null){
						partecipantiEffettivi.addAll(partita.getGiocatori());
					}
				}
			}else{
				break;
			}
		}
		return partecipantiEffettivi;
	}
	
	public Set<GiocatoreDTO> getPartecipantiEffettiviConNPartite(Integer numeroPartite){
		Map<GiocatoreDTO, Integer> mappaPartecipantiEffettivi = new HashMap<GiocatoreDTO, Integer>();
		for(int numeroTurno = 1; numeroTurno <= numeroPartite; numeroTurno++){
			Partita[] partite = loadPartite(numeroTurno, true, null);
			if (partite != null){
				for (Partita partita: partite){
					if (partita != null){
						Set<GiocatoreDTO> giocatori = partita.getGiocatori();
						for (GiocatoreDTO giocatore: giocatori){
							if (mappaPartecipantiEffettivi.containsKey(giocatore)){
								Integer turniGiocati = mappaPartecipantiEffettivi.get(giocatore);
								turniGiocati++;
								mappaPartecipantiEffettivi.put(giocatore, turniGiocati);
							}else{
								mappaPartecipantiEffettivi.put(giocatore, 1);
							}
						}
					}
				}
			}else{
				break;
			}
		}
		Set<GiocatoreDTO> partecipantiEffettivi = new HashSet<GiocatoreDTO>();
		Set<Map.Entry<GiocatoreDTO, Integer>> entries = mappaPartecipantiEffettivi.entrySet(); 
		for (Map.Entry<GiocatoreDTO, Integer> entry: entries){
			if (entry.getValue() == numeroPartite){
				partecipantiEffettivi.add(entry.getKey());
			}
		}
		return partecipantiEffettivi;
	}
	
	public Set<GiocatoreDTO> getPartecipantiTurnoN(Integer numeroTurno){
		Set<GiocatoreDTO> partecipantiEffettivi = new HashSet<GiocatoreDTO>();
		Partita[] partite = loadPartite(numeroTurno, true, null);
		if (partite != null){
			for (Partita partita: partite){
				if (partita != null){
					Set<GiocatoreDTO> giocatori = partita.getGiocatori();
					partecipantiEffettivi.addAll(giocatori);
				}
			}
		}
		return partecipantiEffettivi;
	}
	
	public Set<GiocatoreDTO> getPartecipantiEffettivi(boolean withGhost){
		Set<GiocatoreDTO> result = getPartecipantiEffettivi();
		if (result != null && !withGhost){
			result.remove(GiocatoreDTO.FITTIZIO);
		}
		return result;
	}
	
	
	public static String getNomeTurno(int numeroTurno){
		return numeroTurno+SCHEDA_TURNO_SUFFIX;
	}
	private void setPartitaFromRow(Partita partita, Row row, List giocatori, boolean withGhost){
		int numeroGiocatori = 0;
		int numeroTripletteCelle = 5; //row.getLastCellNum()/3;
		for (int j=0; j<numeroTripletteCelle;j++){
			Cell cellId = row.getCell((short)(j*3));
			if (cellId != null){
				Short id = null;
				try{
					id 	= (short)cellId.getNumericCellValue();
				}catch(Exception nfe){
					throw new MyException(nfe,"Colonna ID ("+((j*3)+1)+"°) con valore non numerico");
				}
				if (id != null && id.intValue() != 0){
					Cell cellPunteggio = row.getCell((short)((j*3)+2));
					Float punteggio = null;
					try{
						punteggio = (float) cellPunteggio.getNumericCellValue();
					}catch(Exception nfe){
						throw new MyException(nfe,"Colonna Punteggio ("+((j*3)+2)+"°) con valore non numerico");
					}
					GiocatoreDTO giocatore = new GiocatoreDTO();
					giocatore.setId(id.intValue());
					int position = Collections.binarySearch(giocatori,giocatore);
					if (position >=0 || (giocatore.equals(GiocatoreDTO.FITTIZIO) && withGhost)){
						if (giocatore.equals(GiocatoreDTO.FITTIZIO)){
							giocatore = GiocatoreDTO.FITTIZIO;
						}else{
							giocatore = (GiocatoreDTO)giocatori.get(position);
						}
						partita.addGiocatore(giocatore, punteggio!=null?punteggio:0);
						numeroGiocatori++;
//						HSSFCell cellName = row.getCell((short)((j*3)+1));
//						String name = cellName.getRichStringCellValue().toString();
//						if (name.contains("*")){
//							partita.setVincitore(giocatore);
//						}
					}else if (!giocatore.equals(GiocatoreDTO.FITTIZIO)){
						throw new MyException("Nella lista dei giocatori non è presente quello con indice "+id);
					}
				}
			}
		}
		partita.setNumeroGiocatori(numeroGiocatori);
	}
	
	
	public void scriviLog(String log){
		Sheet logSheet = foglioTorneo.getSheet(SCHEDA_LOG);
		if (logSheet == null){
			logSheet = foglioTorneo.createSheet(SCHEDA_LOG);
			logSheet.setColumnWidth((short)0,Short.MAX_VALUE);
		}
		Row rowLog = logSheet.createRow(logSheet.getLastRowNum()+1);
		Cell cellLog = rowLog.createCell((short)0);
		cellLog.setCellStyle(styleCell);
		cellLog.setCellValue(creationHelper.createRichTextString(Calendar.getInstance().getTime()+" "+log));
	}
	

	public void scriviLog(List<String> log){
		Sheet logSheet = foglioTorneo.getSheet(SCHEDA_LOG);
		if (logSheet == null){
			logSheet = foglioTorneo.createSheet(SCHEDA_LOG);
			logSheet.setColumnWidth((short)0,Short.MAX_VALUE);
		}
		if (log != null){
			Row rowLog = logSheet.createRow(logSheet.getLastRowNum()+1);
			Cell cellLog = rowLog.createCell((short)0);
			cellLog.setCellStyle(styleCell);
			cellLog.setCellValue(creationHelper.createRichTextString(Calendar.getInstance().getTime()+""));
			for (String rigaLog: log){
				rowLog = logSheet.createRow(logSheet.getLastRowNum()+1);
				cellLog = rowLog.createCell((short)0);
				cellLog.setCellStyle(styleCell);
				cellLog.setCellValue(creationHelper.createRichTextString(rigaLog));
			}
		}
	}
	
	public void scriviClassificaRidotta(TipoTorneo tipoTorneo){
		if (!checkSheet(SCHEDA_CLASSIFICA)){
			scriviClassifica(tipoTorneo);
		}
		int index = foglioTorneo.getSheetIndex(SCHEDA_CLASSIFICA_RIDOTTA);
		if (index >=0){foglioTorneo.removeSheetAt(index);}
		Sheet schedaClassificaRidotta = foglioTorneo.cloneSheet(foglioTorneo.getSheetIndex(SCHEDA_CLASSIFICA));
		List<String> colonneDaTenere = Arrays.asList(new String[]{"Pos.", "Nome", "Cognome", "pt_tot", "id"});
		ExcelUtils.keepOnlyColumnsWithHeaders(schedaClassificaRidotta, colonneDaTenere);
		
		for (int indiceCella = 0; indiceCella < 5; indiceCella++){
			schedaClassificaRidotta.autoSizeColumn(indiceCella);
		}
		int sheetIndex = foglioTorneo.getSheetIndex(schedaClassificaRidotta);
		foglioTorneo.setSheetName(sheetIndex, SCHEDA_CLASSIFICA_RIDOTTA);
	}
	
	private void creaStiliClassifica(){
		styleCellClassODD  = foglioTorneo.createCellStyle();
		styleCellClassEVEN = foglioTorneo.createCellStyle();
		styleCellClassODD.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styleCellClassEVEN.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styleCellClassODD.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
		styleCellClassEVEN.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
		Font font = foglioTorneo.createFont();
		font.setBold(true);
		styleCellClassEVEN.setFont(font);
		styleCellClassODD.setFont(font);

		styleCellClassWinODD  = foglioTorneo.createCellStyle();
		styleCellClassWinEVEN = foglioTorneo.createCellStyle();
		styleCellClassWinODD.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styleCellClassWinEVEN.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styleCellClassWinODD.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
		styleCellClassWinEVEN.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
		styleCellClassWinODD.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
		styleCellClassWinEVEN.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
		styleCellClassWinEVEN.setFont(font);
		styleCellClassWinODD.setFont(font);
//		styleCellClassWinEVEN.setBorderTop((short)5);
//		styleCellClassWinEVEN.setBorderBottom((short)5);
//		styleCellClassWinEVEN.setBorderLeft((short)5);
//		styleCellClassWinEVEN.setBorderRight((short)5);
//		styleCellClassWinEVEN.setLeftBorderColor(HSSFColor.DARK_GREEN.index);
//		styleCellClassWinEVEN.setRightBorderColor(HSSFColor.DARK_GREEN.index);
//		styleCellClassWinEVEN.setTopBorderColor(HSSFColor.DARK_GREEN.index);
//		styleCellClassWinEVEN.setBottomBorderColor(HSSFColor.DARK_GREEN.index);
//		styleCellClassWinODD.setBorderTop((short)5);
//		styleCellClassWinODD.setBorderBottom((short)5);
//		styleCellClassWinODD.setBorderLeft((short)5);
//		styleCellClassWinODD.setBorderRight((short)5);
//		styleCellClassWinODD.setLeftBorderColor(HSSFColor.DARK_GREEN.index);
//		styleCellClassWinODD.setRightBorderColor(HSSFColor.DARK_GREEN.index);
//		styleCellClassWinODD.setTopBorderColor(HSSFColor.DARK_GREEN.index);
//		styleCellClassWinODD.setBottomBorderColor(HSSFColor.DARK_GREEN.index);
		
	}
	
	public void scriviClassifica(TipoTorneo tipoTorneo){
		int index = foglioTorneo.getSheetIndex(SCHEDA_CLASSIFICA);
		if (index >=0){
			foglioTorneo.removeSheetAt(index);
		}
		creaStiliClassifica();
		CellStyle styleCellClass, styleCellClassWin;
		
		Sheet schedaClassifica = foglioTorneo.createSheet(SCHEDA_CLASSIFICA);

		Row intestazione = schedaClassifica.createRow(0);
		short indexCell = 0;
		CellUtil.createCell(intestazione,  indexCell++, "Pos.",styleIntestazione);
		CellUtil.createCell(intestazione,  indexCell++, "Nome",styleIntestazione);
		CellUtil.createCell(intestazione,  indexCell++, "Cognome",styleIntestazione);
		CellUtil.createCell(intestazione,  indexCell++, "Nick",styleIntestazione);
		CellUtil.createCell(intestazione,  indexCell++, "Club o Famiglia",styleIntestazione);
//		if (tipoTorneo != TipoTorneo.MasterRisiko2015 && tipoTorneo != TipoTorneo.MasterRisiko){
			for (int i = 1; ; i++){
				Partita[] partiteTurnoi = loadPartite(i,false,tipoTorneo);
				if (partiteTurnoi == null){break;}
				CellUtil.createCell(intestazione,  indexCell++, "pt"+i,styleIntestazione);
			}
//		}else{
//			for (int i = 1; i <= 3; i++){
//				Partita[] partiteTurnoi = loadPartite(i,false,tipoTorneo);
//				if (partiteTurnoi == null){break;}
//				CellUtil.createCell(intestazione,  indexCell++, "pt"+i,styleIntestazione);
//			}
//		}
		CellUtil.createCell(intestazione,  indexCell++, "v_tot",styleIntestazione);
		CellUtil.createCell(intestazione,  indexCell++, "pt_tot",styleIntestazione);
		CellUtil.createCell(intestazione,  indexCell++, "id",styleIntestazione);
		
		List<ScorePlayer>scores = null;
		switch (tipoTorneo) {
		case NazionaleRisiKo:
			scores = getClassificaNazionaleRisiko();
			break;
		case TorneoGufo:
		case TorneoASquadre:
			scores = getClassificaTorneoGufo();
			break;
		case CampionatoGufo:
			scores = getClassificaCampionatoGufo();
			break;
		case RadunoNazionale:
		case RadunoNazionale_con_quarti:
			//scores = getClassificaRaduno(false);
			scores = getClassificaRadunoNazionale2020(false, true);
			break;
		case MasterRisiko2015:
		case MasterRisiko:
			//scores = getClassificaQualificazioniNazionale(false, true);
			scores = getClassificaMaster2020(false, true);
			break;
		case Open:
		case SantEufemia:
			scores = getClassificaTorneoOpen();
			break;
		case OpenMaster:
			scores = getClassificaMasterRisikoSenzaFinale();
			break;
		case BGL:
		case BGL_SVIZZERA:
		case _1vs1_SVIZZERA:
			scores = getClassificaBGL();
			break;
		default:
			throw new MyException("Classifica non prevista per questo tipo di Torneo: "+tipoTorneo);
		}
		
		int position = 1;
		for (ScorePlayer scorePlayer: scores){
			indexCell = 0;
			GiocatoreDTO giocatore = scorePlayer.getGiocatore();
			int numeroRiga = schedaClassifica.getLastRowNum()+1;
			if (numeroRiga%2==0){
				styleCellClass = styleCellClassEVEN;
				styleCellClassWin = styleCellClassWinEVEN;
			}else{
				styleCellClass = styleCellClassODD;
				styleCellClassWin = styleCellClassWinODD;
			}
			Row rowScore = schedaClassifica.createRow(numeroRiga);
			//CellUtil.createCell(rowScore,  indexCell++, String.valueOf(position++), styleCellClass);
			CellUtil.createCell(rowScore,  indexCell++, String.valueOf(scorePlayer.getPosition()), styleCellClass);
			CellUtil.createCell(rowScore,  indexCell++, giocatore.getNome(), styleCellClass);
			CellUtil.createCell(rowScore,  indexCell++, giocatore.getCognome(), styleCellClass);
			CellUtil.createCell(rowScore,  indexCell++, giocatore.getNick(), styleCellClass);
			CellUtil.createCell(rowScore,  indexCell++, giocatore.getClubProvenienza()!=null?giocatore.getClubProvenienza().getDenominazione():"", styleCellClass);
			for (Partita partita: scorePlayer.getPartite()){
				Cell punti	 	= rowScore.createCell((short)indexCell++, CellType.NUMERIC);
				if ((tipoTorneo == TipoTorneo.MasterRisiko2015 || tipoTorneo == TipoTorneo.MasterRisiko) && indiceFormatTreDecimali != -1){
					CellStyle cs = foglioTorneo.createCellStyle();
					cs.cloneStyleFrom(styleCellClass);
					cs.setDataFormat(indiceFormatTreDecimali);
					punti.setCellStyle(cs);
				}else{
					punti.setCellStyle(styleCellClass);
				}
				if(partita != null){
					if(partita.isVincitore(giocatore)){
						if ((tipoTorneo == TipoTorneo.MasterRisiko2015 || tipoTorneo == TipoTorneo.MasterRisiko) && indiceFormatTreDecimali != -1){
							CellStyle cs = foglioTorneo.createCellStyle();
							cs.cloneStyleFrom(styleCellClassWin);
							cs.setDataFormat(indiceFormatTreDecimali);
							punti.setCellStyle(cs);
						}else{
							punti.setCellStyle(styleCellClassWin);
						}
					}
					double puntid = partita.getPunteggioTrascodificatoB(giocatore).doubleValue();
					punti.setCellValue(puntid);
				}				
			}
			Cell totVittorieCell 	= rowScore.createCell((short)indexCell++, CellType.NUMERIC);
			totVittorieCell.setCellStyle(styleCellClass);
			totVittorieCell.setCellValue(scorePlayer.getNumeroVittorie());
			
			
			Cell punteggioCell 	= rowScore.createCell((short)indexCell++, CellType.NUMERIC);

			if ((tipoTorneo == TipoTorneo.MasterRisiko2015 || tipoTorneo == TipoTorneo.MasterRisiko) && indiceFormatTreDecimali != -1){
				CellStyle cs = foglioTorneo.createCellStyle();
				cs.cloneStyleFrom(styleCellClass);
				cs.setDataFormat(indiceFormatTreDecimali);
				punteggioCell.setCellStyle(cs);
			}else{
				punteggioCell.setCellStyle(styleCellClass);
			}
			punteggioCell.setCellValue(scorePlayer.getPunteggioB(false).doubleValue());

			Cell id = rowScore.createCell((short)indexCell, CellType.STRING);
			id.setCellValue(String.valueOf(giocatore.getId()));
			id.setCellStyle(styleCellClass);
			//schedaClassifica.setColumnHidden((short)indexCell++,true);
		}
		for (int indiceCella = 0; indiceCella < indexCell; indiceCella++){
			schedaClassifica.autoSizeColumn(indiceCella);
		}
		foglioTorneo.setActiveSheet(foglioTorneo.getSheetIndex(SCHEDA_CLASSIFICA));
		
		scriviClassificaRidotta(tipoTorneo);
	}
	
	public void scriviClassificaASquadre(TipoTorneo tipoTorneo){
		int index = foglioTorneo.getSheetIndex(SCHEDA_CLASSIFICA_A_SQUADRE);
		if (index >=0){
			foglioTorneo.removeSheetAt(index);
		}
		creaStiliClassifica();
		CellStyle styleCellClass, styleCellClassWin;
		
		Sheet schedaClassifica = foglioTorneo.createSheet(SCHEDA_CLASSIFICA_A_SQUADRE);

		List<ScoreTeam>scores = null;
		switch (tipoTorneo) {
		case TorneoASquadre:
			scores = getClassificaTorneoCNS();
			break;
		default:
			throw new MyException("Classifica non prevista per questo tipo di Torneo: "+tipoTorneo);
		}
		
		Row intestazione = schedaClassifica.createRow(0);
		short indexCell = 0;
		CellUtil.createCell(intestazione,  indexCell++, "Pos.",styleIntestazione);
		CellUtil.createCell(intestazione,  indexCell++, "Club o Famiglia",styleIntestazione);
		for (int i = 1; i <= scores.get(0).getPartite().size(); i++){
			CellUtil.createCell(intestazione,  indexCell++, "pt"+i,styleIntestazione);
		}
		CellUtil.createCell(intestazione,  indexCell++, "v_tot",styleIntestazione);
		CellUtil.createCell(intestazione,  indexCell++, "pt_tot",styleIntestazione);
		CellUtil.createCell(intestazione,  indexCell++, "pt_class",styleIntestazione);
		
		for (ScoreTeam scoreTeam: scores){
			indexCell = 0;
			int numeroRiga = schedaClassifica.getLastRowNum()+1;
			if (numeroRiga%2==0){
				styleCellClass = styleCellClassEVEN;
				styleCellClassWin = styleCellClassWinEVEN;
			}else{
				styleCellClass = styleCellClassODD;
				styleCellClassWin = styleCellClassWinODD;
			}
			Row rowScore = schedaClassifica.createRow(numeroRiga);
			//CellUtil.createCell(rowScore,  indexCell++, String.valueOf(position++), styleCellClass);
			CellUtil.createCell(rowScore,  indexCell++, String.valueOf(scoreTeam.getPosition()), styleCellClass);
			String teamName = scoreTeam.getTeam()!=null?scoreTeam.getTeam().getDenominazione():"";
			CellUtil.createCell(rowScore,  indexCell++, teamName, styleCellClass);
			int numeroPartite = 0;
			for (GiocatoreDTO giocatoreTeam: scoreTeam.getGiocatori()){
				for (Partita partita: scoreTeam.getPartitePerGiocatore(giocatoreTeam)){
					numeroPartite++;
					Cell punti	 	= rowScore.createCell((short)indexCell++, CellType.NUMERIC);
					punti.setCellStyle(styleCellClass);
					if(partita != null){
						if(partita.isVincitore(giocatoreTeam)){
							punti.setCellStyle(styleCellClassWin);
						}
						double puntid = partita.getPunteggioTrascodificatoB(giocatoreTeam).doubleValue();
						punti.setCellValue(puntid);
					}	
				}
			}
			Cell totVittorieCell 	= rowScore.createCell((short)indexCell++, CellType.NUMERIC);
			totVittorieCell.setCellStyle(styleCellClass);
			totVittorieCell.setCellValue(scoreTeam.getNumeroVittorie());		
			
			Cell punteggioTotCell 	= rowScore.createCell((short)indexCell++, CellType.NUMERIC);
			punteggioTotCell.setCellStyle(styleCellClass);
			punteggioTotCell.setCellValue(scoreTeam.getPunteggioB(numeroPartite).doubleValue());

			Cell punteggioClassCell 	= rowScore.createCell((short)indexCell++, CellType.NUMERIC);
			punteggioClassCell.setCellStyle(styleCellClass);
			punteggioClassCell.setCellValue(scoreTeam.getPunteggioB(ScoreCNSComparator.RISULTATI_VALIDI_CNS).doubleValue());
		}
		for (int indiceCella = 0; indiceCella < indexCell; indiceCella++){
			schedaClassifica.autoSizeColumn(indiceCella);
		}
		foglioTorneo.setActiveSheet(foglioTorneo.getSheetIndex(SCHEDA_CLASSIFICA_A_SQUADRE));

	}
	
	public List<ScorePlayer> getClassificaRaduno(boolean partecipanti){
		List<ScorePlayer> scores = new ArrayList<ScorePlayer>();
		List<GiocatoreDTO>giocatori = getListaGiocatori(partecipanti);
		//Set<GiocatoreDTO> giocatori = getPartecipantiEffettivi(false);
		List<Partita[]> listaPartiteTotali = new ArrayList<Partita[]>();
		for (int i = 1; ; i++){
			Partita[] partiteTurnoi = loadPartite(i,false,TipoTorneo.RadunoNazionale);
			if (partiteTurnoi == null){break;}
			listaPartiteTotali.add(partiteTurnoi);
		}
		
		TorneiUtils.checksPartiteConPiuVincitori(listaPartiteTotali);
		
		Partita[] partiteGiocatore = null;
		for (GiocatoreDTO giocatore: giocatori){
			partiteGiocatore = new Partita[listaPartiteTotali.size()];
			int indexTurno = 0;
			for (Partita[] partite: listaPartiteTotali){
				partiteGiocatore[indexTurno++] = inspectPartite(partite, giocatore);
			}
			ScorePlayer scorePlayer = new ScorePlayerRaduno(giocatore,partiteGiocatore);
			scores.add(scorePlayer);
		}
		//Collections.sort(scores, new ScoreRadunoComparator());
		scores = ScorePlayerClassificator.scorePlayerSorter(scores, new ScoreRadunoComparator());
		return scores;
	}
	
	public List<ScorePlayer> getClassificaRadunoAlSecondoTurno(boolean partecipanti){
		List<ScorePlayer> scores = new ArrayList<ScorePlayer>();
		List<GiocatoreDTO>giocatori = getListaGiocatori(partecipanti);
		//Set<GiocatoreDTO> giocatori = getPartecipantiEffettivi(false);
		List<Partita[]> listaPartiteTotali = new ArrayList<Partita[]>();
		for (int i = 1; i <=2; i++){
			Partita[] partiteTurnoi = loadPartite(i,false,TipoTorneo.RadunoNazionale);
			listaPartiteTotali.add(partiteTurnoi);
		}
		
		TorneiUtils.checksPartiteConPiuVincitori(listaPartiteTotali);
		
		Partita[] partiteGiocatore = null;
		for (GiocatoreDTO giocatore: giocatori){
			partiteGiocatore = new Partita[listaPartiteTotali.size()];
			int indexTurno = 0;
			for (Partita[] partite: listaPartiteTotali){
				partiteGiocatore[indexTurno++] = inspectPartite(partite, giocatore);
			}
			if (partiteGiocatore[1] != null){//Introdotta novità nel 2020 in base alla quale solo chi ha giocato il 2° turno può andare in semifinale
				ScorePlayer scorePlayer = new ScorePlayerRaduno(giocatore,partiteGiocatore);
				scores.add(scorePlayer);
			}
		}
		//Collections.sort(scores, new ScoreRadunoComparator());
		scores = ScorePlayerClassificator.scorePlayerSorter(scores, new ScoreRadunoComparator());
		return scores;
	}
	
	public List<ScorePlayer> ordinaSemifinalistiRaduno(List<GiocatoreDTO> semifinalisti){
		List<ScorePlayer> scores = new ArrayList<ScorePlayer>();
		List<Partita[]> listaPartiteTotali = new ArrayList<Partita[]>();
		for (int i = 1; ; i++){
			Partita[] partiteTurnoi = loadPartite(i,false,TipoTorneo.RadunoNazionale);
			if (partiteTurnoi == null){break;}
			listaPartiteTotali.add(partiteTurnoi);
		}
		Partita[] partiteGiocatore = null;
		for (GiocatoreDTO giocatore: semifinalisti){
			partiteGiocatore = new Partita[listaPartiteTotali.size()];
			int indexTurno = 0;
			for (Partita[] partite: listaPartiteTotali){
				partiteGiocatore[indexTurno++] = inspectPartite(partite, giocatore);
			}
			ScorePlayer scorePlayer = new ScorePlayerRaduno(giocatore,partiteGiocatore);
			scores.add(scorePlayer);
		}
		Collections.sort(scores, new ScoreSemifinalistiRadunoComparator());
		return scores;
	}
	
	
	public List<ScorePlayer> getClassificaTorneoOpen(){
		List<ScorePlayer> scores = new ArrayList<ScorePlayer>();
		List<GiocatoreDTO>giocatori = getListaGiocatori(false);
		//Set<GiocatoreDTO> giocatori = getPartecipantiEffettivi(false);
		List<Partita[]> listaPartiteTotali = new ArrayList<Partita[]>();
		for (int i = 1; ; i++){
			Partita[] partiteTurnoi = loadPartite(i,false,TipoTorneo.Open);
			if (partiteTurnoi == null){break;}
			listaPartiteTotali.add(partiteTurnoi);
		}
		
		TorneiUtils.checksPartiteConPiuVincitori(listaPartiteTotali);
		
		Partita[] partiteGiocatore = null;
		for (GiocatoreDTO giocatore: giocatori){
			partiteGiocatore = new Partita[listaPartiteTotali.size()];
			int indexTurno = 0;
			for (Partita[] partite: listaPartiteTotali){
				partiteGiocatore[indexTurno++] = inspectPartite(partite, giocatore);
			}
			ScorePlayer scorePlayer = new ScorePlayerOpen(giocatore,partiteGiocatore);
			scores.add(scorePlayer);
		}
		//Collections.sort(scores, new ScoreTorneoOpenComparator());
		scores = ScorePlayerClassificator.scorePlayerSorter(scores, new ScoreTorneoOpenComparator());
		return scores;
	}
	
	private List<ScorePlayer> getClassificaMasterRisikoSenzaFinale(){
		List<ScorePlayer> scores = new ArrayList<ScorePlayer>();
		List<GiocatoreDTO>giocatori = getListaGiocatori(false);
		//Set<GiocatoreDTO> giocatori = getPartecipantiEffettivi(false);
		List<Partita[]> listaPartiteTotali = new ArrayList<Partita[]>();
		for (int i = 1; ; i++){
			Partita[] partiteTurnoi = loadPartite(i,false,TipoTorneo.Open);
			if (partiteTurnoi == null){break;}
			listaPartiteTotali.add(partiteTurnoi);
		}
		
		TorneiUtils.checksPartiteConPiuVincitori(listaPartiteTotali);
		
		Partita[] partiteGiocatore = null;
		for (GiocatoreDTO giocatore: giocatori){
			partiteGiocatore = new Partita[listaPartiteTotali.size()];
			int indexTurno = 0;
			for (Partita[] partite: listaPartiteTotali){
				partiteGiocatore[indexTurno++] = inspectPartite(partite, giocatore);
			}
			ScorePlayer scorePlayer = new ScorePlayerQualificazioniNazionale(giocatore,partiteGiocatore);
			scores.add(scorePlayer);
		}
		//Collections.sort(scores, new ScoreTorneoOpenComparator());
		scores = ScorePlayerClassificator.scorePlayerSorter(scores, new ScoreTorneoOpenComparator());
		return scores;
	}
	
	public List<ScorePlayer> getClassificaNazionaleRisiko(){
		List<ScorePlayer> scores = new ArrayList<ScorePlayer>();
		List<GiocatoreDTO>giocatori = getListaGiocatori(true);
		//Set<GiocatoreDTO> giocatori = getPartecipantiEffettivi(false);
		List<Partita[]> listaPartiteTotali = new ArrayList<Partita[]>();
		for (int i = 1; ; i++){
			Partita[] partiteTurnoi = loadPartite(i,false,TipoTorneo.NazionaleRisiKo);
			if (partiteTurnoi == null){break;}
			listaPartiteTotali.add(partiteTurnoi);
		}
		
		Partita[] partiteGiocatore = null;
		for (GiocatoreDTO giocatore: giocatori){
			partiteGiocatore = new Partita[listaPartiteTotali.size()];
			int indexTurno = 0;
			for (Partita[] partite: listaPartiteTotali){
				partiteGiocatore[indexTurno++] = inspectPartite(partite, giocatore);
			}
			ScorePlayer scorePlayer = new ScorePlayerNazionaleRisiko(giocatore,partiteGiocatore);
			scores.add(scorePlayer);
		}
		//Collections.sort(scores, new ScoreNazionaleRisikoComparator());
		scores = ScorePlayerClassificator.scorePlayerSorter(scores, new ScoreNazionaleRisikoComparator());
		return scores;
	}
	
	
	
	private List<ScorePlayer> getClassificaTorneoGufo(){
		List<ScorePlayer> scores = new ArrayList<ScorePlayer>();
		List<GiocatoreDTO>giocatori = getListaGiocatori(false);
		//Set<GiocatoreDTO> giocatori = getPartecipantiEffettivi(false);
		List<Partita[]> listaPartiteTotali = new ArrayList<Partita[]>();
		for (int i = 1; ; i++){
			Partita[] partiteTurnoi = loadPartite(i,true,TipoTorneo.TorneoGufo);
			if (partiteTurnoi == null){break;}
			listaPartiteTotali.add(partiteTurnoi);
		}
		
		TorneiUtils.checksPartiteConPiuVincitori(listaPartiteTotali);
		
		Partita[] partiteGiocatore = null;
		for (GiocatoreDTO giocatore: giocatori){
			partiteGiocatore = new Partita[listaPartiteTotali.size()];
			int indexTurno = 0;
			for (Partita[] partite: listaPartiteTotali){
				partiteGiocatore[indexTurno++] = inspectPartite(partite, giocatore);
			}
			ScorePlayer scorePlayer = new ScorePlayerTorneoGufo(giocatore,partiteGiocatore);
			scores.add(scorePlayer);
		}
		//Collections.sort(scores, new ScoreTorneoOpenComparator());
		scores = ScorePlayerClassificator.scorePlayerSorter(scores, new ScoreTorneoOpenComparator());
		return scores;
	}
	

	private List<ScoreTeam> getClassificaTorneoCNS(){
		List<ScoreTeam> scores = new ArrayList<ScoreTeam>();
		List<GiocatoreDTO>giocatori = getListaGiocatori(false);
		//Set<GiocatoreDTO> giocatori = getPartecipantiEffettivi(false);
		List<Partita[]> listaPartiteTotali = new ArrayList<Partita[]>();
		for (int i = 1; ; i++){
			Partita[] partiteTurnoi = loadPartite(i,false,TipoTorneo.TorneoGufo);
			if (partiteTurnoi == null){break;}
			listaPartiteTotali.add(partiteTurnoi);
		}
		
		TorneiUtils.checksPartiteConPiuVincitori(listaPartiteTotali);
		
		for (Partita[] partite: listaPartiteTotali){
			for (Partita partita: partite){
				Set<GiocatoreDTO> giocatoriInPartita = partita.getGiocatori();
				for (GiocatoreDTO giocatore: giocatoriInPartita){
					int indexGiocatore = giocatori.indexOf(giocatore);
					if (indexGiocatore >= 0){
						giocatore = giocatori.get(indexGiocatore);
						ScoreTeam scoreTeam = new ScoreTeamCNS(giocatore.getClubProvenienza());
						int indexTeam = scores.indexOf(scoreTeam);
						if (indexTeam >= 0){
							scoreTeam = scores.get(indexTeam);
							scoreTeam.addPartitaPerGiocatore(giocatore, partita);
						}else{
							scoreTeam.addPartitaPerGiocatore(giocatore, partita);
							scores.add(scoreTeam);
						}
					}
				}
			}
		}
		
		scores = ScoreTeamClassificator.scoreTeamSorter(scores, new ScoreCNSComparator());
		return scores;
	}
	
	
	public List<ScorePlayer> getClassificaBGL(){
		List<ScorePlayer> scores = new ArrayList<ScorePlayer>();
		List<GiocatoreDTO>giocatori = getListaGiocatori(false);
		//Set<GiocatoreDTO> giocatori = getPartecipantiEffettivi(false);
		List<Partita[]> listaPartiteTotali = new ArrayList<Partita[]>();
		for (int i = 1; ; i++){
			Partita[] partiteTurnoi = loadPartite(i,true,TipoTorneo.BGL);
			if (partiteTurnoi == null){break;}
			listaPartiteTotali.add(partiteTurnoi);
		}
		
		Partita[] partiteGiocatore = null;
		for (GiocatoreDTO giocatore: giocatori){
			partiteGiocatore = new Partita[listaPartiteTotali.size()];
			int indexTurno = 0;
			for (Partita[] partite: listaPartiteTotali){
				partiteGiocatore[indexTurno++] = inspectPartite(partite, giocatore);
			}
			ScorePlayer scorePlayer = new ScorePlayerTorneoBGL(giocatore,partiteGiocatore);
			scores.add(scorePlayer);
		}
		//Collections.sort(scores, new ScoreTorneoOpenComparator());
		scores = ScorePlayerClassificator.scorePlayerSorter(scores, new ScoreTorneoOpenComparator());
		return scores;
	}
	
	private List<ScorePlayer> getClassificaCampionatoGufo(){
		List<ScorePlayer> scores = new ArrayList<ScorePlayer>();
		List<GiocatoreDTO>giocatori = getListaGiocatori(false);
		//Set<GiocatoreDTO> giocatori = getPartecipantiEffettivi(false);
		List<Partita[]> listaPartiteTotali = new ArrayList<Partita[]>();
		for (int i = 1; ; i++){
			Partita[] partiteTurnoi = loadPartite(i,true,TipoTorneo.CampionatoGufo);
			if (partiteTurnoi == null){break;}
			listaPartiteTotali.add(partiteTurnoi);
		}
		
		TorneiUtils.checksPartiteConPiuVincitori(listaPartiteTotali);
		
		Partita[] partiteGiocatore = null;
		for (GiocatoreDTO giocatore: giocatori){
			partiteGiocatore = new Partita[listaPartiteTotali.size()];
			int indexTurno = 0;
			for (Partita[] partite: listaPartiteTotali){
				partiteGiocatore[indexTurno++] = inspectPartite(partite, giocatore);
			}
			ScorePlayer scorePlayer = new ScorePlayerCampionatoGufo(giocatore,partiteGiocatore);
			scores.add(scorePlayer);
		}
		//Collections.sort(scores, new ScoreCampionatoComparator());
		scores = ScorePlayerClassificator.scorePlayerSorter(scores, new ScoreCampionatoComparator());
		return scores;
	}
	
	public List<ScorePlayer> getClassificaQualificazioniNazionale(boolean partecipanti, boolean compreseSemifinali){
		List<ScorePlayer> scores = new ArrayList<ScorePlayer>();
		List<GiocatoreDTO>giocatori = getListaGiocatori(partecipanti);
		//Set<GiocatoreDTO> giocatori = getPartecipantiEffettivi(false);
		List<Partita[]> listaPartiteTotali = new ArrayList<Partita[]>();
		int numeroTurniDisputati = 0;
		int numeroTurniDaConsiderare = 2;
		if (compreseSemifinali) numeroTurniDaConsiderare++;
		for (int i = 1; i <=numeroTurniDaConsiderare ; i++){
			Partita[] partiteTurnoi = loadPartite(i,true,TipoTorneo.MasterRisiko2015);
			if (partiteTurnoi == null){break;}
			numeroTurniDisputati++;
			listaPartiteTotali.add(partiteTurnoi);
		}
		
		TorneiUtils.checksPartiteConPiuVincitori(listaPartiteTotali);
		
		Partita[] partiteGiocatore = null;
		for (GiocatoreDTO giocatore: giocatori){
			//partiteGiocatore = new Partita[listaPartiteTotali.size()];
			partiteGiocatore = new Partita[numeroTurniDisputati];
			int indexTurno = 0;
			short numeroPartiteDisputate = 0;
			for (Partita[] partite: listaPartiteTotali){
				partiteGiocatore[indexTurno++] = inspectPartite(partite, giocatore);
				if (partiteGiocatore[indexTurno-1] != null){numeroPartiteDisputate++;}
			}
			if (compreseSemifinali || numeroTurniDisputati == 1 || numeroPartiteDisputate >=2){
				ScorePlayer scorePlayer = new ScorePlayerQualificazioniNazionale(giocatore,partiteGiocatore);
				scores.add(scorePlayer);
			}
		}
		Collections.sort(scores, new ScoreQualificazioniNazionaleComparator());

		Partita[] finale = loadPartite(4, false, TipoTorneo.MasterRisiko2015);
		if (compreseSemifinali && finale != null){
			int index = 0;
			Partita finale1 = finale[0];
			Partita finale2 = null;
			if (finale1 != null){
				List<GiocatoreDTO> giocatoriFinale1 = new ArrayList<GiocatoreDTO>(finale1.getGiocatoriOrdinatiPerPunteggio());
				List<GiocatoreDTO> giocatoriFinale2 = null;
				if (finale.length > 1){
					finale2 = finale[1];
					giocatoriFinale2 = new ArrayList<GiocatoreDTO>(finale2.getGiocatoriOrdinatiPerPunteggio());
				}
				for (int indexGiocatori = 0; indexGiocatori < giocatoriFinale1.size(); indexGiocatori++){
					GiocatoreDTO finalista1 = giocatoriFinale1.get(indexGiocatori);
					GiocatoreDTO finalista2 = null;
					if (giocatoriFinale2 != null){
						finalista2 = giocatoriFinale2.get(indexGiocatori);
						aggiornaClassificaConFinalisti(scores, finalista1, index++, indexGiocatori+1);
						aggiornaClassificaConFinalisti(scores, finalista2, index++ , indexGiocatori+1);
					}else{
						aggiornaClassificaConFinalisti(scores, finalista1, index++, indexGiocatori+1);
					}
				}
			}
		}
		scores = ScorePlayerClassificator.scorePlayerPositioner(scores, new ScoreQualificazioniNazionaleComparator());
		return scores;
	}
	

	public List<ScorePlayer> getClassificaMaster2020(boolean partecipanti, boolean compreseSemifinali){
		List<ScorePlayer> scores = new ArrayList<ScorePlayer>();
		List<GiocatoreDTO>giocatori = getListaGiocatori(partecipanti);
		//Set<GiocatoreDTO> giocatori = getPartecipantiEffettivi(false);
		List<Partita[]> listaPartiteTotali = new ArrayList<Partita[]>();
		int numeroTurniDisputati = 0;
		int numeroTurniDaConsiderare = 2;
		//if (compreseSemifinali) numeroTurniDaConsiderare++;
		if (compreseSemifinali) numeroTurniDaConsiderare = numeroTurniDaConsiderare +2;
		for (int i = 1; i <=numeroTurniDaConsiderare ; i++){
			Partita[] partiteTurnoi = loadPartite(i,true,TipoTorneo.MasterRisiko);
			if (partiteTurnoi == null){break;}
			numeroTurniDisputati++;
			listaPartiteTotali.add(partiteTurnoi);
		}
		
		TorneiUtils.checksPartiteConPiuVincitori(listaPartiteTotali);
		
		Partita[] partiteGiocatore = null;
		for (GiocatoreDTO giocatore: giocatori){
			//partiteGiocatore = new Partita[listaPartiteTotali.size()];
			partiteGiocatore = new Partita[numeroTurniDisputati];
			int indexTurno = 0;
			short numeroPartiteDisputate = 0;
			for (Partita[] partite: listaPartiteTotali){
				partiteGiocatore[indexTurno++] = inspectPartite(partite, giocatore);
				if (partiteGiocatore[indexTurno-1] != null){numeroPartiteDisputate++;}
			}
			if (compreseSemifinali || numeroTurniDisputati == 1 || numeroPartiteDisputate >=2){
				ScorePlayer scorePlayer = new ScorePlayerQualificazioniNazionale(giocatore,partiteGiocatore);
				scores.add(scorePlayer);
			}
		}
		Collections.sort(scores, new ScoreQualificazioniNazionaleComparator());

		Partita[] finali = loadPartite(4, false, TipoTorneo.MasterRisiko);
		if (compreseSemifinali && finali != null && finali.length > 0){
			List<List<GiocatoreDTO>> listaFinalisti = new ArrayList<List<GiocatoreDTO>>(); 
			int numeroFinalistiMassimo = 0;
			for (Partita finale: finali){
				List<GiocatoreDTO> giocatoriFinale = new ArrayList<GiocatoreDTO>(finale.getGiocatoriOrdinatiPerPunteggio());
				listaFinalisti.add(giocatoriFinale);
				numeroFinalistiMassimo = Math.max(giocatoriFinale.size(), numeroFinalistiMassimo);
			}
			int index = 0;
			for (int indexGiocatori = 0; indexGiocatori < numeroFinalistiMassimo; indexGiocatori++){
				for (List<GiocatoreDTO> finalisti: listaFinalisti){
					if (indexGiocatori < finalisti.size()){
						GiocatoreDTO finalista = finalisti.get(indexGiocatori);
						aggiornaClassificaConFinalisti(scores, finalista, index++, indexGiocatori+1);
					}
				}
			}
		}
		scores = ScorePlayerClassificator.scorePlayerPositioner(scores, new ScoreQualificazioniNazionaleComparator());
		return scores;
	}
	
	public List<ScorePlayer> getClassificaRadunoNazionale2020(boolean partecipanti, boolean compreseSemifinali){
		List<ScorePlayer> scores = new ArrayList<ScorePlayer>();
		List<GiocatoreDTO>giocatori = getListaGiocatori(partecipanti);
		//Set<GiocatoreDTO> giocatori = getPartecipantiEffettivi(false);
		List<Partita[]> listaPartiteTotali = new ArrayList<Partita[]>();
		int numeroTurniDisputati = 0;
		int numeroTurniDaConsiderare = 2;
		//if (compreseSemifinali) numeroTurniDaConsiderare++;
		if (compreseSemifinali) numeroTurniDaConsiderare = numeroTurniDaConsiderare +2;
		for (int i = 1; i <=numeroTurniDaConsiderare ; i++){
			Partita[] partiteTurnoi = loadPartite(i,true,TipoTorneo.RadunoNazionale);
			if (partiteTurnoi == null){break;}
			numeroTurniDisputati++;
			listaPartiteTotali.add(partiteTurnoi);
		}
		
		TorneiUtils.checksPartiteConPiuVincitori(listaPartiteTotali);
		
		Partita[] partiteGiocatore = null;
		for (GiocatoreDTO giocatore: giocatori){
			//partiteGiocatore = new Partita[listaPartiteTotali.size()];
			partiteGiocatore = new Partita[numeroTurniDisputati];
			int indexTurno = 0;
			short numeroPartiteDisputate = 0;
			for (Partita[] partite: listaPartiteTotali){
				partiteGiocatore[indexTurno++] = inspectPartite(partite, giocatore);
				if (partiteGiocatore[indexTurno-1] != null){numeroPartiteDisputate++;}
			}
			if (compreseSemifinali || numeroTurniDisputati == 1 || numeroPartiteDisputate >=2){
				ScorePlayer scorePlayer = new ScorePlayerRaduno(giocatore,partiteGiocatore);
				scores.add(scorePlayer);
			}
		}
		Collections.sort(scores, new ScoreRadunoNazionale2020Comparator());

		Partita[] finali = loadPartite(4, false, TipoTorneo.RadunoNazionale);
		if (compreseSemifinali && finali != null && finali.length > 0){
			List<List<GiocatoreDTO>> listaFinalisti = new ArrayList<List<GiocatoreDTO>>(); 
			int numeroFinalistiMassimo = 0;
			for (Partita finale: finali){
				List<GiocatoreDTO> giocatoriFinale = new ArrayList<GiocatoreDTO>(finale.getGiocatoriOrdinatiPerPunteggio());
				listaFinalisti.add(giocatoriFinale);
				numeroFinalistiMassimo = Math.max(giocatoriFinale.size(), numeroFinalistiMassimo);
			}
			int index = 0;
			for (int indexGiocatori = 0; indexGiocatori < numeroFinalistiMassimo; indexGiocatori++){
				for (List<GiocatoreDTO> finalisti: listaFinalisti){
					if (indexGiocatori < finalisti.size()){
						GiocatoreDTO finalista = finalisti.get(indexGiocatori);
						aggiornaClassificaConFinalisti(scores, finalista, index++, indexGiocatori+1);
					}
				}
			}
		}
		scores = ScorePlayerClassificator.scorePlayerPositioner(scores, new ScoreRadunoNazionale2020Comparator());
		return scores;
	}
	
	private void aggiornaClassificaConFinalisti(List<ScorePlayer> scores, GiocatoreDTO finalista, int posizione, int posizioneClassifica){
		Iterator<ScorePlayer> iterator = scores.iterator();
		while (iterator.hasNext()){
			ScorePlayer scorePlayer = iterator.next(); 
			if (scorePlayer.getGiocatore().equals(finalista)){
				iterator.remove();
				scorePlayer.setPosition(posizioneClassifica);
				scores.add(posizione, scorePlayer);
				break;
			}
		}
	}
	
	public static Partita inspectPartite(Partita[] partite, GiocatoreDTO giocatore){
		Partita result = null;
		if (partite != null && giocatore != null){
			for (Partita partita: partite){
				if (partita != null && partita.eAlTavolo(giocatore)){
					result = partita;
					break;
				}
			}
		}
		return result;
	}
		
	public String[] getSheetNames(){
		String[] result = null;
		int numberOfSheets = foglioTorneo.getNumberOfSheets();
		if (numberOfSheets > 0){
			result = new String[numberOfSheets];
			for (int i = 0; i < numberOfSheets; i++){
				result[i] = foglioTorneo.getSheetName(i);
			}
		}
		return result;
	}
	
	public Torneo elaboraTorneo(){
		Torneo torneo = new Torneo();
		
		SchedaTorneo schedaTorneo = leggiSchedaTorneo();
		List<GiocatoreDTO> partecipantiEffettivi = null;
		Set<GiocatoreDTO> giocatori = getPartecipantiEffettivi();
		if (giocatori != null){
			partecipantiEffettivi = new ArrayList<GiocatoreDTO>(giocatori);
		}
		
		List<SchedaTurno> schedeTurno = leggiSchedeTurno();
		
		SchedaClassifica schedaClassifica = leggiSchedaClassifica();
		
		torneo.setSchedaTorneo(schedaTorneo);
		torneo.setPartecipanti(partecipantiEffettivi);
		torneo.setSchedeTurno(schedeTurno);
		torneo.setSchedaClassifica(schedaClassifica);
		
		return torneo;
	}
	
	public void scriviStatistiche(){
		int index = foglioTorneo.getSheetIndex(SCHEDA_STATISTICHE);
		if (index >=0){
			foglioTorneo.removeSheetAt(index);
		}
		
		Sheet schedaStatistiche = foglioTorneo.createSheet(SCHEDA_STATISTICHE);

		List<Partita> listaPartitePrecedenti = new ArrayList<Partita>();
		for (int i = 1; ; i++){
			Partita[] partiteTurnoi = loadPartite(i,false,null);
			if (partiteTurnoi == null){break;}
			listaPartitePrecedenti.addAll(Arrays.asList(partiteTurnoi));
		}
		
		creaStiliStatistiche();
		
		MatchGrids matchGrids = MatchAnalyzer.calcolaGriglie(listaPartitePrecedenti);
		
		MatchAnomali matchAnomali = MatchAnalyzer.calcolaConfrontiClubAnomali(listaPartitePrecedenti, AnomaliaConfrontiClub.BOTH);
		MyLogger.getLogger().info(matchAnomali.getMatchClubVsClubAnomali().toString());
		
		
		Map<ClubDTO, Map<ClubDTO, Integer>> mapClubVsClub = matchGrids.getMapClubVsClub();
		Map<GiocatoreDTO, Map<ClubDTO, Integer>> 		mapGiocatoreVsClub 		= matchGrids.getMapGiocatoreVsClub();
		Map<GiocatoreDTO, Map<GiocatoreDTO, Integer>> 	mapGiocatoreVsGiocatore = matchGrids.getMapGiocatoreVsGiocatore();
		
		Set<ClubDTO> clubsSet = mapClubVsClub.keySet();
		List clubsList = new ArrayList<ClubDTO>(clubsSet);
		Collections.sort(clubsList);
		
		Set<GiocatoreDTO> giocatoriSet = mapGiocatoreVsClub.keySet();
		List giocatoriList = new ArrayList<GiocatoreDTO>(giocatoriSet);
		Collections.sort(giocatoriList);
		
		int indexRow = 0;
		Row intestazioneMapClubVsClub = schedaStatistiche.createRow(indexRow);
		short indexCell = 1;
		for (Object o: clubsList){
			ClubDTO club = (ClubDTO) o;
			CellUtil.createCell(intestazioneMapClubVsClub,  indexCell++, club.getDenominazione(), styleCellClassIntestStat);
		}
		for (Object o: clubsList){
			indexCell = 0;
			ClubDTO club = (ClubDTO) o;
			Row rowClub = schedaStatistiche.createRow(++indexRow);
			CellUtil.createCell(rowClub,  indexCell++, club.getDenominazione());
			Map<ClubDTO,Integer> mappaI = mapClubVsClub.get(club);

			for (Object o2: clubsList){
				ClubDTO club2 = (ClubDTO) o2;
				Integer confronti = mappaI.get(club2);
				if (confronti == null) confronti = 0;
				CellUtil.createCell(rowClub,  indexCell++, confronti.toString(), styleByConfronti(confronti));
			}
		}
		indexRow+=2;
		
		Row intestazioneMapGiocatoreVsClub = schedaStatistiche.createRow(indexRow);
		indexCell = 1;
		for (Object o: clubsList){
			ClubDTO club = (ClubDTO) o;
			CellUtil.createCell(intestazioneMapGiocatoreVsClub,  indexCell++, club.getDenominazione(), styleCellClassIntestStat);
		}
		for (Object o: giocatoriList){
			GiocatoreDTO giocatore = (GiocatoreDTO) o;
			indexCell = 0;
			Row rowGiocatore = schedaStatistiche.createRow(++indexRow);
			String nominativo = giocatore.getNome()+" "+giocatore.getCognome();
			if (giocatore.getClubProvenienza() != null){
				nominativo +=" ["+giocatore.getClubProvenienza()+"]";
			}
			CellUtil.createCell(rowGiocatore,  indexCell++, nominativo);
			Map<ClubDTO,Integer> mappaI = mapGiocatoreVsClub.get(giocatore);

			for (Object o2: clubsList){
				ClubDTO club2 = (ClubDTO) o2;
				Integer confronti = mappaI.get(club2);
				if (confronti == null) confronti = 0;
				CellUtil.createCell(rowGiocatore,  indexCell++, confronti.toString(), styleByConfronti(confronti));
			}
		}
		
		indexRow+=2;
		
		Row intestazioneMapGiocatoreVsGiocatore = schedaStatistiche.createRow(indexRow);
		indexCell = 1;
		for (Object o: giocatoriList){
			GiocatoreDTO giocatore = (GiocatoreDTO) o;
			String nominativo = giocatore.getNome()+" "+giocatore.getCognome();
			if (giocatore.getClubProvenienza() != null){
				nominativo +=" ["+giocatore.getClubProvenienza()+"]";
			}
			CellUtil.createCell(intestazioneMapGiocatoreVsGiocatore,  indexCell++, nominativo, styleCellClassIntestStat);
		}
		for (Object o: giocatoriList){
			GiocatoreDTO giocatore = (GiocatoreDTO) o;
			indexCell = 0;
			Row rowGiocatore = schedaStatistiche.createRow(++indexRow);
			String nominativo = giocatore.getNome()+" "+giocatore.getCognome();
			if (giocatore.getClubProvenienza() != null){
				nominativo +=" ["+giocatore.getClubProvenienza()+"]";
			}
			CellUtil.createCell(rowGiocatore,  indexCell++, nominativo);
			Map<GiocatoreDTO,Integer> mappaI = mapGiocatoreVsGiocatore.get(giocatore);

			for (Object o2: giocatoriList){
				GiocatoreDTO giocatore2 = (GiocatoreDTO) o2;
				Integer confronti = mappaI.get(giocatore2);
				if (confronti == null) confronti = 0;
				CellUtil.createCell(rowGiocatore,  indexCell++, confronti.toString(), styleByConfronti(confronti));
			}
		}
		
		for (int indiceCella = 0; indiceCella < indexCell; indiceCella++){
			//schedaStatistiche.autoSizeColumn(indiceCella);
		}
	}
	
	private CellStyle styleByConfronti(Integer confronti){
		switch (confronti) {
		case 0:
			return styleCellClass0;
		case 1:
			return styleCellClass1;
		case 2:
			return styleCellClass2;
		default:
			return styleCellClassD;
		}
	}
	
	private void creaStiliStatistiche(){
		Font font = foglioTorneo.createFont();
		font.setFontName(HSSFFont.FONT_ARIAL);
		font.setColor(IndexedColors.BLACK.getIndex());
		
		styleCellClassIntestStat = foglioTorneo.createCellStyle();
		styleCellClassIntestStat.setAlignment(HorizontalAlignment.CENTER);
		styleCellClassIntestStat.setFont(font);
		styleCellClassIntestStat.setWrapText(true);
		styleCellClassIntestStat.setVerticalAlignment(VerticalAlignment.CENTER);
		styleCellClassIntestStat.setFillForegroundColor(IndexedColors.AQUA.getIndex());
		styleCellClassIntestStat.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		
		styleCellClass0 = foglioTorneo.createCellStyle();
		styleCellClass0.setAlignment(HorizontalAlignment.CENTER);
		styleCellClass0.setFont(font);
		styleCellClass0.setWrapText(true);
		styleCellClass0.setVerticalAlignment(VerticalAlignment.CENTER);
		styleCellClass0.setFillForegroundColor(IndexedColors.WHITE.getIndex());
		styleCellClass0.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		
		styleCellClass1 = foglioTorneo.createCellStyle();
		styleCellClass1.setAlignment(HorizontalAlignment.CENTER);
		styleCellClass1.setFont(font);
		styleCellClass1.setWrapText(true);
		styleCellClass1.setVerticalAlignment(VerticalAlignment.CENTER);
		styleCellClass1.setFillForegroundColor(IndexedColors.GOLD.getIndex());
		styleCellClass1.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		
		styleCellClass2 = foglioTorneo.createCellStyle();
		styleCellClass2.setAlignment(HorizontalAlignment.CENTER);
		styleCellClass2.setFont(font);
		styleCellClass2.setWrapText(true);
		styleCellClass2.setVerticalAlignment(VerticalAlignment.CENTER);
		styleCellClass2.setFillForegroundColor(IndexedColors.ROSE.getIndex());
		styleCellClass2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		
		styleCellClassD = foglioTorneo.createCellStyle();
		styleCellClassD.setAlignment(HorizontalAlignment.CENTER);
		styleCellClassD.setFont(font);
		styleCellClassD.setWrapText(true);
		styleCellClassD.setVerticalAlignment(VerticalAlignment.CENTER);
		styleCellClassD.setFillForegroundColor(IndexedColors.RED.getIndex());
		styleCellClassD.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	}
	
	public void closeFileExcel(){
		if (pathFileExcel != null){
			try{
				FileOutputStream fileOut = new FileOutputStream(pathFileExcel);
				foglioTorneo.write(fileOut);
				fileOut.close();
			}catch(IOException ioe){
				throw new MyException("Impossibile scrivere sul file Excel "+pathFileExcel+":\n "+ioe.getMessage());
			}
		}
	}
}
