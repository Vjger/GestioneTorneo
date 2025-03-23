package it.desimone.risiko.torneo.batch;

import it.desimone.risiko.torneo.dto.ClubDTO;
import it.desimone.risiko.torneo.dto.GiocatoreDTO;
import it.desimone.risiko.torneo.dto.Partita;
import it.desimone.risiko.torneo.dto.RegioneDTO;
import it.desimone.risiko.torneo.dto.SchedaClassifica;
import it.desimone.risiko.torneo.dto.SchedaTorneo;
import it.desimone.risiko.torneo.dto.ScorePlayer;
import it.desimone.risiko.torneo.dto.ScorePlayerCampionatoGufo;
import it.desimone.risiko.torneo.dto.ScorePlayerNazionaleRisiko;
import it.desimone.risiko.torneo.dto.ScorePlayerOpen;
import it.desimone.risiko.torneo.dto.ScorePlayerQualificazioniNazionale;
import it.desimone.risiko.torneo.dto.ScorePlayerRaduno;
import it.desimone.risiko.torneo.dto.ScorePlayerTorneoBGL;
import it.desimone.risiko.torneo.dto.ScorePlayerTorneoGufo;
import it.desimone.risiko.torneo.dto.SchedaClassifica.RigaClassifica;
import it.desimone.risiko.torneo.utils.ClubLoader;
import it.desimone.risiko.torneo.utils.RegioniLoader;
import it.desimone.risiko.torneo.utils.ScoreCampionatoComparator;
import it.desimone.risiko.torneo.utils.ScoreNazionaleRisikoComparator;
import it.desimone.risiko.torneo.utils.ScoreQualificazioniNazionaleComparator;
import it.desimone.risiko.torneo.utils.ScoreRadunoComparator;
import it.desimone.risiko.torneo.utils.ScoreSemifinalistiRadunoComparator;
import it.desimone.risiko.torneo.utils.ScoreTorneoOpenComparator;
import it.desimone.risiko.torneo.utils.TipoTorneo;
import it.desimone.risiko.torneo.utils.TorneiUtils;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;



//import org.apache.commons.lang.exception.NestableException;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//import org.apache.poi.hssf.usermodel.contrib.HSSFCellUtil;
//import org.apache.poi.hssf.usermodel.contrib.HSSFRegionUtil;
import org.apache.poi.hssf.util.HSSFCellUtil;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.util.HSSFRegionUtil;
import org.apache.poi.hssf.util.Region;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;

public class ExcelAccessHSSF {

	private final ClubLoader clubLoader = new ClubLoader();
	private final RegioniLoader regioniLoader = new RegioniLoader();
	
	public static final String SCHEDA_TORNEO 		= "TORNEO";
	public static final String SCHEDA_ISCRITTI 		= "Iscritti";
	public static final String SCHEDA_LOG 			= "Log";
	public static final String SCHEDA_CLASSIFICA	= "Classifica";
	public static final String SCHEDA_CLASSIFICA_RIDOTTA	= "Classifica Ridotta";
	public static final String SCHEDA_TURNO_SUFFIX	= "° Turno";
		
	String pathFileExcel;
	short posizioneId 			= 0;
	short posizioneNome 		= 1;
	short posizioneCognome 		= 2;
	short posizioneNick 		= 3;
	short posizioneClub 		= 4;
	short posizionePresenza 	= 5;
	short posizioneRegione 		= 6;
	short posizioneEmail 		= 7;

	private HSSFWorkbook foglioTorneo;
	private HSSFCellStyle styleCell;
	private HSSFCellStyle styleCellPoints;
	private HSSFCellStyle styleCellId;
	private HSSFCellStyle styleIntestazione;
	
	private HSSFCellStyle styleCellClassODD;
	private HSSFCellStyle styleCellClassEVEN;
	private HSSFCellStyle styleCellClassWinODD;
	private HSSFCellStyle styleCellClassWinEVEN;
	
	private static short indiceFormatTreDecimali = -1;
		
	public ExcelAccess(File fileExcel){
		try {
			pathFileExcel = fileExcel.getPath();
		}finally{}
	}
	
	
	public void openFileExcel(){
		if (pathFileExcel != null){
			try{
				POIFSFileSystem fs = null;
				fs = new POIFSFileSystem(new FileInputStream(pathFileExcel));
				foglioTorneo = new HSSFWorkbook(fs);
				HSSFDataFormat df = foglioTorneo.createDataFormat();
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
			}
		}
	}
	
	private void creaStili(){
		styleCellId = foglioTorneo.createCellStyle();
		styleCellId.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);

		styleCell = foglioTorneo.createCellStyle();
		styleCell.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		HSSFFont font = foglioTorneo.createFont();
		font.setFontName(HSSFFont.FONT_ARIAL);
		font.setColor(HSSFColor.BLACK.index);
		styleCell.setFont(font);
		styleCell.setWrapText(true);
		styleCell.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
		styleCell.setFillForegroundColor(HSSFColor.LIGHT_ORANGE.index);
		styleCell.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		
		styleCellPoints = foglioTorneo.createCellStyle();
		styleCellPoints.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		styleCellPoints.setFont(font);
		styleCellPoints.setWrapText(true);
		styleCellPoints.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
		styleCellPoints.setFillForegroundColor(HSSFColor.LIGHT_ORANGE.index);
		styleCellPoints.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		styleCellPoints.setLocked(false);
		
		styleIntestazione = foglioTorneo.createCellStyle();		
		styleIntestazione.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		styleIntestazione.setBorderBottom(HSSFCellStyle.BORDER_THICK);
		styleIntestazione.setBorderTop(HSSFCellStyle.BORDER_THICK);
		styleIntestazione.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		styleIntestazione.setBorderRight(HSSFCellStyle.BORDER_THIN);
		HSSFFont fontIntestazione = foglioTorneo.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		styleIntestazione.setFont(fontIntestazione);
		styleIntestazione.setWrapText(false);
		styleIntestazione.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		styleIntestazione.setFillForegroundColor(HSSFColor.ORANGE.index);
		styleIntestazione.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
	}
	
	public List<GiocatoreDTO> getListaGiocatori(boolean partecipanti){
		//List<GiocatoreDTO> listaGiocatori = new ArrayList<GiocatoreDTO>();
		Set<GiocatoreDTO> listaGiocatori = new HashSet<GiocatoreDTO>();
		HSSFSheet sheet = foglioTorneo.getSheet(SCHEDA_ISCRITTI);
		
		//int ultimaRiga = sheet.getLastRowNum();
		//System.out.println("Ultima riga: "+ultimaRiga);

		for (int i = 3; i<=sheet.getLastRowNum(); i++){
			HSSFRow row = sheet.getRow(i);
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
		HSSFSheet sheet = foglioTorneo.getSheet(SCHEDA_ISCRITTI);
		
		for (int i = 3; i<sheet.getLastRowNum(); i++){
			HSSFRow row = sheet.getRow(i);
			if (row != null){
				GiocatoreDTO giocatore = null;
				try{
					giocatore = getGiocatoreFromRow(row);
				}catch(Exception me){
					throw new MyException(me,"Riga n° "+(i+1)+" della scheda "+SCHEDA_ISCRITTI);
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
		HSSFSheet sheet = foglioTorneo.getSheet(SCHEDA_TORNEO);
		if (sheet != null){
			schedaTorneo = new SchedaTorneo();
			HSSFRow rowSede = sheet.getRow(1);
			String sede = determinaValoreCella(rowSede, (short)3);
			HSSFRow rowOrganizzatore = sheet.getRow(2);
			String organizzatore = determinaValoreCella(rowOrganizzatore, (short)3);
			HSSFRow rowNomeTorneo = sheet.getRow(3);
			String nomeTorneo = determinaValoreCella(rowNomeTorneo, (short)3);
			HSSFRow rowTipoTorneo = sheet.getRow(4);
			String tipologiaTorneo = determinaValoreCella(rowTipoTorneo, (short)3);
			HSSFRow rowNumeroTurni = sheet.getRow(5);
			String numeroTurni = determinaValoreCella(rowNumeroTurni, (short)3);
			List<Date> dataTurni = new ArrayList<Date>();
			for (int indexDate = 6; indexDate <=35; indexDate++){
				HSSFRow rowDataTurno = sheet.getRow(indexDate);
				if (rowDataTurno != null){
					HSSFCell cellaDataTurno   = rowDataTurno.getCell((short)3);
					if (cellaDataTurno != null){
						Date dataTurno = cellaDataTurno.getDateCellValue();
						dataTurni.add(dataTurno);
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
		}
		
		return schedaTorneo;
	}
	
	public SchedaClassifica leggiSchedaClassifica(){
		SchedaClassifica schedaClassifica = null;
		HSSFSheet sheet = foglioTorneo.getSheet(SCHEDA_CLASSIFICA_RIDOTTA);
		if (sheet != null){
			schedaClassifica = new SchedaClassifica();
			//Integer nRows = sheet.getLastRowNum();
			Iterator it = sheet.rowIterator();
			it.next(); //Si salta la prima riga che è l'header
			int numeroRiga = 1;
			while (it.hasNext()){
				numeroRiga++;
				HSSFRow row = (HSSFRow) it.next();
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
				if (posizioneInt != null){
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
	
	
	private String determinaValoreCella(HSSFRow row, short posizioneCella){
		if (row == null) return null;
		HSSFCell cella   = row.getCell(posizioneCella);
		if (cella == null) return null;
		String result 		= "";
		int tipoCella   = cella.getCellType();
		if (tipoCella == Cell.CELL_TYPE_STRING){
			result 		    = cella.getRichStringCellValue().getString();
		}else if (tipoCella == Cell.CELL_TYPE_NUMERIC){
			result			= Double.toString(cella.getNumericCellValue());
		}else if (tipoCella != Cell.CELL_TYPE_BLANK){
			MyLogger.getLogger().info("Impossibile leggere la cella della colonna in posizione "+posizioneCella+ " della riga "+ (row.getRowNum()+1)+" perchè di tipo imprevisto: "+tipoCella);
		}
		return result;
	}
	
	private GiocatoreDTO getGiocatoreFromRow(HSSFRow row){
		GiocatoreDTO giocatore = new GiocatoreDTO();
		Short id = null;
		try{
			id 			= (short)row.getCell(posizioneId).getNumericCellValue();
		}catch(NumberFormatException nfe){
			throw new MyException(nfe,"Colonna ID con valore non numerico");
		}
		
		String nome = determinaValoreCella(row, posizioneNome);
		String cognome = determinaValoreCella(row, posizioneCognome);
		String email = determinaValoreCella(row, posizioneEmail);
		String nick = determinaValoreCella(row, posizioneNick);
		
		String regione = determinaValoreCella(row, posizioneRegione);
		String club = determinaValoreCella(row, posizioneClub);
		String presenza = determinaValoreCella(row, posizionePresenza);
		
		//String regione 		= row.getCell(posizioneRegione).getRichStringCellValue().getString();
		//String club 		= row.getCell(posizioneClub).getRichStringCellValue().getString();
		//String presenza		= row.getCell(posizionePresenza).getRichStringCellValue().getString();
		giocatore.setId(id.intValue());
		giocatore.setNome(nome);
		giocatore.setCognome(cognome);
		giocatore.setEmail(email);
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
		return giocatore;
	}
	
	private HSSFSheet creaSchedaTurno(String nomeScheda, boolean hidden){
		HSSFSheet result = foglioTorneo.getSheet(nomeScheda);
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
			HSSFSheet schedaTurno = creaSchedaTurno(nomeTurno, true);
			int prossimaRiga = schedaTurno.getLastRowNum()+(schedaTurno.getLastRowNum()==0?0:1);
			/* TEST quinta scheda */
			//int prossimaRiga = schedaTurno.getLastRowNum()+1;
			HSSFRow rowIntestazione = schedaTurno.createRow(prossimaRiga);
			HSSFCell cellIntestazione = rowIntestazione.createCell((short)0);
			cellIntestazione.setCellStyle(styleIntestazione);
			cellIntestazione.setCellValue("Tavolo N°"+partita.getNumeroTavolo());
			Region region = new Region(prossimaRiga,(short)0,prossimaRiga,(short)(partita.getNumeroGiocatori()*3-1));
			try {
				HSSFRegionUtil.setBorderBottom(HSSFCellStyle.BORDER_THIN,region,schedaTurno,foglioTorneo);
				HSSFRegionUtil.setBorderTop(HSSFCellStyle.BORDER_THIN,region,schedaTurno,foglioTorneo);
				HSSFRegionUtil.setBorderLeft(HSSFCellStyle.BORDER_THIN,region,schedaTurno,foglioTorneo);
				HSSFRegionUtil.setBorderRight(HSSFCellStyle.BORDER_THIN,region,schedaTurno,foglioTorneo);
			} catch (/*Nestable*/Exception e) {
				throw new MyException("Errore nell'utilizzo della classe HSSFRegionUtil: "+e.getMessage());
			}
			schedaTurno.addMergedRegion(region);
			HSSFRow row = schedaTurno.createRow(prossimaRiga+1);
			//row.setHeight((short)800);
			short counterCell = 0;
			HSSFSheet sheetIscritti = foglioTorneo.getSheet(SCHEDA_ISCRITTI);
			int numeroIscritti = sheetIscritti == null?0:sheetIscritti.getLastRowNum()+1;
			for (GiocatoreDTO giocatore: partita.getGiocatori()){
				HSSFCell cellId = row.createCell(counterCell++);
				cellId.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
				cellId.setCellValue(giocatore.getId());
				cellId.setCellStyle(styleCellId);
				HSSFCell cellNominativo = row.createCell(counterCell++);
				cellNominativo.setCellStyle(styleCell);
				String nominativo = giocatore.getNome()+" "+giocatore.getCognome();
				if (giocatore.getClubProvenienza() != null){
					nominativo +="\n"+giocatore.getClubProvenienza();
				}
				cellNominativo.setCellValue(new HSSFRichTextString(nominativo));
				HSSFCell cellPunteggio = row.createCell(counterCell++);
				cellPunteggio.setCellStyle(styleCellPoints);
				cellPunteggio.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
				cellPunteggio.setCellValue(partita.getTavolo().get(giocatore));
			}
			//foglioTorneo.setActiveSheet(foglioTorneo.getSheetIndex(nomeTurno));
		}


	public void scriviHelp(String nomeTurno){
		HSSFSheet schedaTurno = foglioTorneo.getSheet(nomeTurno);
		int prossimaRiga = schedaTurno.getLastRowNum()+(schedaTurno.getLastRowNum()==0?0:1);

		HSSFRow rowIntestazione = schedaTurno.createRow(prossimaRiga);
		HSSFCell cellIntestazione = rowIntestazione.createCell((short)0);
		//cellIntestazione.setCellStyle(styleIntestazione);
		
		StringBuilder buffer = new StringBuilder();
		buffer.append("Per cambiare i tavoli serve cambiare gli ID che sono riportati nelle colonne A - D - G - J - M.");
		buffer.append("\nQuindi, si deve scoprire quelle colonne e scrivere il nuovo ID (il nominativo si adeguerï¿½ di conseguenza) ");
		buffer.append("\nSu OpenOffice le colonne nascoste si scoprono evidenziando l'intera scheda e poi con le voci di Menï¿½ Formato -> Colonne -> Mostra;");
		buffer.append("\nsu Excel, evidenziando l'intera scheda e poi pulsante destro -> Scopri.");
		
		cellIntestazione.setCellValue(buffer.toString());
		Region region = new Region(prossimaRiga,(short)0,prossimaRiga,(short)(14));
		try {
			HSSFRegionUtil.setBorderBottom(HSSFCellStyle.BORDER_THIN,region,schedaTurno,foglioTorneo);
			HSSFRegionUtil.setBorderTop(HSSFCellStyle.BORDER_THIN,region,schedaTurno,foglioTorneo);
			HSSFRegionUtil.setBorderLeft(HSSFCellStyle.BORDER_THIN,region,schedaTurno,foglioTorneo);
			HSSFRegionUtil.setBorderRight(HSSFCellStyle.BORDER_THIN,region,schedaTurno,foglioTorneo);
		} catch (/*Nestable*/Exception e) {
			throw new MyException("Errore nell'utilizzo della classe HSSFRegionUtil: "+e.getMessage());
		}
		schedaTurno.addMergedRegion(region);
	}
	
	public void scriviPartiteFaseFinaleQualificazioneRisiko(String nomeTurno, Partita partita){
		HSSFSheet schedaTurno = creaSchedaTurno(nomeTurno, false);
		int prossimaRiga = schedaTurno.getLastRowNum()+(schedaTurno.getLastRowNum()==0?0:1);
		/* TEST quinta scheda */
		//int prossimaRiga = schedaTurno.getLastRowNum()+1;
		HSSFRow rowIntestazione = schedaTurno.createRow(prossimaRiga);
		HSSFCell cellIntestazione = rowIntestazione.createCell((short)0);
		cellIntestazione.setCellStyle(styleIntestazione);
		cellIntestazione.setCellValue("Tavolo NÂ°"+partita.getNumeroTavolo());
		Region region = new Region(prossimaRiga,(short)0,prossimaRiga,(short)(partita.getNumeroGiocatori()*3-1));
		try {
			HSSFRegionUtil.setBorderBottom(HSSFCellStyle.BORDER_THIN,region,schedaTurno,foglioTorneo);
			HSSFRegionUtil.setBorderTop(HSSFCellStyle.BORDER_THIN,region,schedaTurno,foglioTorneo);
			HSSFRegionUtil.setBorderLeft(HSSFCellStyle.BORDER_THIN,region,schedaTurno,foglioTorneo);
			HSSFRegionUtil.setBorderRight(HSSFCellStyle.BORDER_THIN,region,schedaTurno,foglioTorneo);
		} catch (/*Nestable*/Exception e) {
			throw new MyException("Errore nell'utilizzo della classe HSSFRegionUtil: "+e.getMessage());
		}
		schedaTurno.addMergedRegion(region);
		HSSFRow row = schedaTurno.createRow(prossimaRiga+1);
		short counterCell = 0;
		HSSFSheet sheetIscritti = foglioTorneo.getSheet(SCHEDA_ISCRITTI);
		int numeroIscritti = sheetIscritti == null?0:sheetIscritti.getLastRowNum()+1;
		for (GiocatoreDTO giocatore: partita.getGiocatori()){
			HSSFCell cellId = row.createCell(counterCell++);
			cellId.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
			cellId.setCellValue(giocatore.getId());
			cellId.setCellStyle(styleCellId);
			HSSFCell cellNominativo = row.createCell(counterCell++);
			cellNominativo.setCellStyle(styleCell);
			String nominativo = giocatore.getNome()+" "+giocatore.getCognome();
			if (giocatore.getClubProvenienza() != null){
				nominativo +="\n"+giocatore.getClubProvenienza();
			}
			char indiceColonnaId = trascodificaIndiceColonna(cellId.getCellNum());
			int  indiceRigaId = prossimaRiga+2;
			String formula = "VLOOKUP("+indiceColonnaId+indiceRigaId+",Iscritti!A4:E"+numeroIscritti+",2,FALSE) & \" \" & VLOOKUP("+indiceColonnaId+indiceRigaId+",Iscritti!A4:E"+numeroIscritti+",3,FALSE) & \" \" & CHAR(10) & VLOOKUP("+indiceColonnaId+indiceRigaId+",Iscritti!A4:E"+numeroIscritti+",5,FALSE)";
			cellNominativo.setCellFormula(formula);
			cellNominativo.setCellValue(new HSSFRichTextString(nominativo));

			HSSFCell cellPunteggio = row.createCell(counterCell++);
			cellPunteggio.setCellStyle(styleCell);
			cellPunteggio.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
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
	
	private Partita[] loadPartite(String nomeTurno, boolean withGhost, TipoTorneo tipoTorneo){
		List<GiocatoreDTO> giocatori = getListaGiocatori(false);
		Collections.sort(giocatori); //Serve perchï¿½ poi su di essa verrï¿½ fatta una binarySearch
		Partita[] partite = null;
		HSSFSheet schedaTurno = foglioTorneo.getSheet(nomeTurno);
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
					HSSFRow row = (HSSFRow) it.next();
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
	
	
	public static String getNomeTurno(int numeroTurno){
		return numeroTurno+SCHEDA_TURNO_SUFFIX;
	}
	private void setPartitaFromRow(Partita partita, HSSFRow row, List giocatori, boolean withGhost){
		int numeroGiocatori = 0;
		int numeroTripletteCelle = 5; //row.getLastCellNum()/3;
		for (int j=0; j<numeroTripletteCelle;j++){
			HSSFCell cellId = row.getCell((short)(j*3));
			if (cellId != null){
				Short id = null;
				try{
					id 	= (short)cellId.getNumericCellValue();
				}catch(Exception nfe){
					throw new MyException(nfe,"Colonna ID ("+((j*3)+1)+"°) con valore non numerico");
				}
				if (id != null && id.intValue() != 0){
					HSSFCell cellPunteggio = row.getCell((short)((j*3)+2));
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
		HSSFSheet logSheet = foglioTorneo.getSheet(SCHEDA_LOG);
		if (logSheet == null){
			logSheet = foglioTorneo.createSheet(SCHEDA_LOG);
			logSheet.setColumnWidth((short)0,Short.MAX_VALUE);
		}
		HSSFRow rowLog = logSheet.createRow(logSheet.getLastRowNum()+1);
		HSSFCell cellLog = rowLog.createCell((short)0);
		cellLog.setCellStyle(styleCell);
		cellLog.setCellValue(new HSSFRichTextString(Calendar.getInstance().getTime()+" "+log));
	}
	

	public void scriviLog(List<String> log){
		HSSFSheet logSheet = foglioTorneo.getSheet(SCHEDA_LOG);
		if (logSheet == null){
			logSheet = foglioTorneo.createSheet(SCHEDA_LOG);
			logSheet.setColumnWidth((short)0,Short.MAX_VALUE);
		}
		if (log != null){
			HSSFRow rowLog = logSheet.createRow(logSheet.getLastRowNum()+1);
			HSSFCell cellLog = rowLog.createCell((short)0);
			cellLog.setCellStyle(styleCell);
			cellLog.setCellValue(new HSSFRichTextString(Calendar.getInstance().getTime()+""));
			for (String rigaLog: log){
				rowLog = logSheet.createRow(logSheet.getLastRowNum()+1);
				cellLog = rowLog.createCell((short)0);
				cellLog.setCellStyle(styleCell);
				cellLog.setCellValue(new HSSFRichTextString(rigaLog));
			}
		}
	}
	
	public void scriviClassificaRidotta(TipoTorneo tipoTorneo){
		if (!checkSheet(SCHEDA_CLASSIFICA)){
			scriviClassifica(tipoTorneo);
		}
		int index = foglioTorneo.getSheetIndex(SCHEDA_CLASSIFICA_RIDOTTA);
		if (index >=0){foglioTorneo.removeSheetAt(index);}
		HSSFSheet schedaClassificaRidotta = foglioTorneo.cloneSheet(foglioTorneo.getSheetIndex(SCHEDA_CLASSIFICA));
		List<String> colonneDaTenere = Arrays.asList(new String[]{"Pos.", "Nome", "Cognome", "pt_tot", "id"});
		keepOnlyColumnsWithHeaders(schedaClassificaRidotta, colonneDaTenere);
		
		for (int indiceCella = 0; indiceCella < 5; indiceCella++){
			schedaClassificaRidotta.autoSizeColumn(indiceCella);
		}
		int sheetIndex = foglioTorneo.getSheetIndex(schedaClassificaRidotta);
		foglioTorneo.setSheetName(sheetIndex, SCHEDA_CLASSIFICA_RIDOTTA);
	}
	
	private void creaStiliClassifica(){
		styleCellClassODD  = foglioTorneo.createCellStyle();
		styleCellClassEVEN = foglioTorneo.createCellStyle();
		styleCellClassODD.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		styleCellClassEVEN.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		styleCellClassODD.setFillForegroundColor(HSSFColor.YELLOW.index);
		styleCellClassEVEN.setFillForegroundColor(HSSFColor.LIGHT_ORANGE.index);
		HSSFFont font = foglioTorneo.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		styleCellClassEVEN.setFont(font);
		styleCellClassODD.setFont(font);

		styleCellClassWinODD  = foglioTorneo.createCellStyle();
		styleCellClassWinEVEN = foglioTorneo.createCellStyle();
		styleCellClassWinODD.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		styleCellClassWinEVEN.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		styleCellClassWinODD.setFillForegroundColor(HSSFColor.YELLOW.index);
		styleCellClassWinEVEN.setFillForegroundColor(HSSFColor.LIGHT_ORANGE.index);
		styleCellClassWinODD.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
		styleCellClassWinEVEN.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
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
		HSSFCellStyle styleCellClass, styleCellClassWin;
		
		HSSFSheet schedaClassifica = foglioTorneo.createSheet(SCHEDA_CLASSIFICA);

		HSSFRow intestazione = schedaClassifica.createRow(0);
		short indexCell = 0;
		HSSFCellUtil.createCell(intestazione,  indexCell++, "Pos.",styleIntestazione);
		HSSFCellUtil.createCell(intestazione,  indexCell++, "Nome",styleIntestazione);
		HSSFCellUtil.createCell(intestazione,  indexCell++, "Cognome",styleIntestazione);
		HSSFCellUtil.createCell(intestazione,  indexCell++, "Nick",styleIntestazione);
		HSSFCellUtil.createCell(intestazione,  indexCell++, "Club o Famiglia",styleIntestazione);
		if (tipoTorneo != TipoTorneo.MasterRisiko2015 && tipoTorneo != TipoTorneo.MasterRisiko2016){
			for (int i = 1; ; i++){
				Partita[] partiteTurnoi = loadPartite(i,false,tipoTorneo);
				if (partiteTurnoi == null){break;}
				HSSFCellUtil.createCell(intestazione,  indexCell++, "pt"+i,styleIntestazione);
			}
		}else{
			for (int i = 1; i <= 3; i++){
				Partita[] partiteTurnoi = loadPartite(i,false,tipoTorneo);
				if (partiteTurnoi == null){break;}
				HSSFCellUtil.createCell(intestazione,  indexCell++, "pt"+i,styleIntestazione);
			}
		}
		HSSFCellUtil.createCell(intestazione,  indexCell++, "v_tot",styleIntestazione);
		HSSFCellUtil.createCell(intestazione,  indexCell++, "pt_tot",styleIntestazione);
		HSSFCellUtil.createCell(intestazione,  indexCell++, "id",styleIntestazione);
		
		List<ScorePlayer>scores = null;
		switch (tipoTorneo) {
		case NazionaleRisiKo:
			scores = getClassificaNazionaleRisiko();
			break;
		case TorneoGufo:
			scores = getClassificaTorneoGufo();
			break;
		case CampionatoGufo:
			scores = getClassificaCampionatoGufo();
			break;
		case RadunoNazionale:
		case RadunoNazionale_con_quarti:
			scores = getClassificaRaduno(false);
			break;
		case MasterRisiko2015:
		case MasterRisiko2016:
			scores = getClassificaQualificazioniNazionale(false, true);
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
			HSSFRow rowScore = schedaClassifica.createRow(numeroRiga);
			HSSFCellUtil.createCell(rowScore,  indexCell++, String.valueOf(position++), styleCellClass);
			HSSFCellUtil.createCell(rowScore,  indexCell++, giocatore.getNome(), styleCellClass);
			HSSFCellUtil.createCell(rowScore,  indexCell++, giocatore.getCognome(), styleCellClass);
			HSSFCellUtil.createCell(rowScore,  indexCell++, giocatore.getNick(), styleCellClass);
			HSSFCellUtil.createCell(rowScore,  indexCell++, giocatore.getClubProvenienza()!=null?giocatore.getClubProvenienza().getDenominazione():"", styleCellClass);
			for (Partita partita: scorePlayer.getPartite()){
				HSSFCell punti	 	= rowScore.createCell((short)indexCell++, HSSFCell.CELL_TYPE_NUMERIC);
				if ((tipoTorneo == TipoTorneo.MasterRisiko2015 || tipoTorneo == TipoTorneo.MasterRisiko2016) && indiceFormatTreDecimali != -1){
					HSSFCellStyle cs = foglioTorneo.createCellStyle();
					cs.cloneStyleFrom(styleCellClass);
					cs.setDataFormat(indiceFormatTreDecimali);
					punti.setCellStyle(cs);
				}else{
					punti.setCellStyle(styleCellClass);
				}
				if(partita != null){
					if(partita.isVincitore(giocatore)){
						if ((tipoTorneo == TipoTorneo.MasterRisiko2015 || tipoTorneo == TipoTorneo.MasterRisiko2016) && indiceFormatTreDecimali != -1){
							HSSFCellStyle cs = foglioTorneo.createCellStyle();
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
			HSSFCell totVittorieCell 	= rowScore.createCell((short)indexCell++, HSSFCell.CELL_TYPE_NUMERIC);
			totVittorieCell.setCellStyle(styleCellClass);
			totVittorieCell.setCellValue(scorePlayer.getNumeroVittorie());
			
			
			HSSFCell punteggioCell 	= rowScore.createCell((short)indexCell++, HSSFCell.CELL_TYPE_NUMERIC);

			if ((tipoTorneo == TipoTorneo.MasterRisiko2015 || tipoTorneo == TipoTorneo.MasterRisiko2016) && indiceFormatTreDecimali != -1){
				HSSFCellStyle cs = foglioTorneo.createCellStyle();
				cs.cloneStyleFrom(styleCellClass);
				cs.setDataFormat(indiceFormatTreDecimali);
				punteggioCell.setCellStyle(cs);
			}else{
				punteggioCell.setCellStyle(styleCellClass);
			}
			punteggioCell.setCellValue(scorePlayer.getPunteggioB(false).doubleValue());

			HSSFCell id = rowScore.createCell((short)indexCell, HSSFCell.CELL_TYPE_STRING);
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
	
	
	public void deleteColumn(HSSFSheet sheet, int columnToDelete) {
		int maxColumn = 0;
		for (int r = 0; r < sheet.getLastRowNum() + 1; r++) {
			HSSFRow row = sheet.getRow(r);

			// if no row exists here; then nothing to do; next!
			if (row == null)
				continue;

			// if the row doesn't have this many columns then we are good; next!
			int lastColumn = row.getLastCellNum();
			if (lastColumn > maxColumn)
				maxColumn = lastColumn;

			if (lastColumn < columnToDelete)
				continue;

			for (int x = columnToDelete + 1; x < lastColumn + 1; x++) {
				Cell oldCell = row.getCell(x - 1);
				if (oldCell != null)
					row.removeCell(oldCell);

				Cell nextCell = row.getCell(x);
				if (nextCell != null) {
					Cell newCell = row.createCell(x - 1, nextCell.getCellType());
					cloneCell(newCell, nextCell);
				}
			}
		}
	}

	private void cloneCell(Cell cNew, Cell cOld) {
		cNew.setCellComment(cOld.getCellComment());
		cNew.setCellStyle(cOld.getCellStyle());

		switch (cNew.getCellType()) {
		case Cell.CELL_TYPE_BOOLEAN: {
			cNew.setCellValue(cOld.getBooleanCellValue());
			break;
		}
		case Cell.CELL_TYPE_NUMERIC: {
			cNew.setCellValue(cOld.getNumericCellValue());
			break;
		}
		case Cell.CELL_TYPE_STRING: {
			cNew.setCellValue(cOld.getStringCellValue());
			break;
		}
		case Cell.CELL_TYPE_ERROR: {
			cNew.setCellValue(cOld.getErrorCellValue());
			break;
		}
		case Cell.CELL_TYPE_FORMULA: {
			cNew.setCellFormula(cOld.getCellFormula());
			break;
		}
		}

	}

	public void keepOnlyColumnsWithHeaders(HSSFSheet sheet, List<String> columnHeaders) {
		HSSFRow row = sheet.getRow(0);
		if (row == null) {
			return;
		}

		int lastColumn = row.getLastCellNum();

		for (int x = lastColumn; x >= 0; x--) {
			Cell headerCell = row.getCell(x);
			if (headerCell != null && headerCell.getStringCellValue() != null
					&& !columnHeaders.contains(headerCell.getStringCellValue())) {
				deleteColumn(sheet, x);
			}
		}
	}

	public void deleteColumnsWithHeader(HSSFSheet sheet, String columnHeader) {
		HSSFRow row = sheet.getRow(0);
		if (row == null) {
			return;
		}

		int lastColumn = row.getLastCellNum();

		for (int x = lastColumn; x >= 0; x--) {
			Cell headerCell = row.getCell(x);
			if (headerCell != null
					&& headerCell.getStringCellValue() != null
					&& headerCell.getStringCellValue().equalsIgnoreCase(
							columnHeader)) {
				deleteColumn(sheet, x);
			}
		}
	}
	
	public List<ScorePlayer> getClassificaRaduno(boolean partecipanti){
		List<ScorePlayer> scores = new ArrayList<ScorePlayer>();
		List<GiocatoreDTO>giocatori = getListaGiocatori(partecipanti);
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
		Collections.sort(scores, new ScoreRadunoComparator());
		return scores;
	}
	
	public List<ScorePlayer> getClassificaRadunoAlSecondoTurno(boolean partecipanti){
		List<ScorePlayer> scores = new ArrayList<ScorePlayer>();
		List<GiocatoreDTO>giocatori = getListaGiocatori(partecipanti);
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
			ScorePlayer scorePlayer = new ScorePlayerRaduno(giocatore,partiteGiocatore);
			scores.add(scorePlayer);
		}
		Collections.sort(scores, new ScoreRadunoComparator());
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
		Collections.sort(scores, new ScoreTorneoOpenComparator());
		return scores;
	}
	
	private List<ScorePlayer> getClassificaMasterRisikoSenzaFinale(){
		List<ScorePlayer> scores = new ArrayList<ScorePlayer>();
		List<GiocatoreDTO>giocatori = getListaGiocatori(false);
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
		Collections.sort(scores, new ScoreTorneoOpenComparator());
		return scores;
	}
	
	public List<ScorePlayer> getClassificaNazionaleRisiko(){
		List<ScorePlayer> scores = new ArrayList<ScorePlayer>();
		List<GiocatoreDTO>giocatori = getListaGiocatori(true);
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
		Collections.sort(scores, new ScoreNazionaleRisikoComparator());
		return scores;
	}
	
	
	
	private List<ScorePlayer> getClassificaTorneoGufo(){
		List<ScorePlayer> scores = new ArrayList<ScorePlayer>();
		List<GiocatoreDTO>giocatori = getListaGiocatori(false);
		List<Partita[]> listaPartiteTotali = new ArrayList<Partita[]>();
		for (int i = 1; ; i++){
			Partita[] partiteTurnoi = loadPartite(i,false,TipoTorneo.TorneoGufo);
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
		Collections.sort(scores, new ScoreTorneoOpenComparator());
		return scores;
	}
	
	
	public List<ScorePlayer> getClassificaBGL(){
		List<ScorePlayer> scores = new ArrayList<ScorePlayer>();
		List<GiocatoreDTO>giocatori = getListaGiocatori(false);
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
		Collections.sort(scores, new ScoreTorneoOpenComparator());
		return scores;
	}
	
	private List<ScorePlayer> getClassificaCampionatoGufo(){
		List<ScorePlayer> scores = new ArrayList<ScorePlayer>();
		List<GiocatoreDTO>giocatori = getListaGiocatori(false);
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
		Collections.sort(scores, new ScoreCampionatoComparator());
		return scores;
	}
	
	public List<ScorePlayer> getClassificaQualificazioniNazionale(boolean partecipanti, boolean compreseSemifinali){
		List<ScorePlayer> scores = new ArrayList<ScorePlayer>();
		List<GiocatoreDTO>giocatori = getListaGiocatori(partecipanti);
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
						if (finale1.getPunteggio(finalista1) >= finale2.getPunteggio(finalista2)){
							aggiornaClassificaConFinalisti(scores, finalista1, index++);
							aggiornaClassificaConFinalisti(scores, finalista2, index++);
						}else{
							aggiornaClassificaConFinalisti(scores, finalista2, index++);
							aggiornaClassificaConFinalisti(scores, finalista1, index++);
						}
					}else{
						aggiornaClassificaConFinalisti(scores, finalista1, index++);
					}
				}
				
			}
		}
		return scores;
	}
	

	private void aggiornaClassificaConFinalisti(List<ScorePlayer> scores, GiocatoreDTO finalista, int posizione){
		Iterator<ScorePlayer> iterator = scores.iterator();
		while (iterator.hasNext()){
			ScorePlayer scorePlayer = iterator.next(); 
			if (scorePlayer.getGiocatore().equals(finalista)){
				iterator.remove();
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
	
	public void hideMailColumn(){
		HSSFSheet schedaIscritti = foglioTorneo.getSheet(SCHEDA_ISCRITTI);
		if (schedaIscritti != null){
			schedaIscritti.setColumnHidden(2, true);
		}
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
