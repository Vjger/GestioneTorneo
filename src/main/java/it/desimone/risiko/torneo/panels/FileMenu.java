package it.desimone.risiko.torneo.panels;

import it.desimone.gdrive.GoogleDrivePublisher;
import it.desimone.risiko.torneo.batch.ExcelAccess;
import it.desimone.risiko.torneo.batch.ExcelValidator;
import it.desimone.risiko.torneo.batch.ExcelValidator.ExcelValidatorData;
import it.desimone.risiko.torneo.batch.ExcelValidator.ExcelValidatorMessages;
import it.desimone.risiko.torneo.batch.RadGester;
import it.desimone.risiko.torneo.dto.GiocatoreDTO;
import it.desimone.risiko.torneo.dto.Partita;
import it.desimone.risiko.torneo.dto.SchedaTorneo;
import it.desimone.risiko.torneo.utils.PdfUtils;
import it.desimone.risiko.torneo.utils.TipoTorneo;
import it.desimone.utils.MyException;
import it.desimone.utils.TextException;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class FileMenu extends JMenu {
	
	private JMenuItem menuOpenFile= new JMenuItem("Open");
	private JMenu menuExportFile= new JMenu("Esporta");
	private JMenuItem menuExportTxt = new JMenuItem("Testo");
	private JMenuItem menuExportVbc = new JMenuItem("vB");
	private JMenuItem menuExportHtml = new JMenuItem("Html");
	private JMenuItem menuStampaFile= new JMenuItem("Stampa Report Risiko");
	private JMenu menuPublishOnline = new JMenu("Pubblica on line");
	private JMenuItem menuPublishReport = new JMenuItem("Pubblica");
	private JMenuItem menuResetCredentials = new JMenuItem("Reset credenziali Google");
	private JFileChooser fileChooser = new JFileChooser(".\\");
	private File excelFile;
	
	private enum TipoFile{
		TEXT
		,VBC
		,HTML;
		
		public static String getFileExtension(TipoFile tipo){
			String result = null;
			switch (tipo) {
			case TEXT:
				result = "txt";
				break;
			case VBC:
				result = "vbc";
				break;
			case HTML:
				result = "html";
				break;
			default:
				throw new MyException("TIPO_FILE non previsto: "+tipo);
			}
			return result;
		}
	}
	
	public FileMenu(){
		super("File");
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.addChoosableFileFilter(new ExcelFileFilter());	
		menuOpenFile.addActionListener(getOpenActionListener());
		this.add(menuOpenFile);
		
		menuExportTxt.addActionListener(getEsportaActionListener(TipoFile.TEXT));
		menuExportVbc.addActionListener(getEsportaActionListener(TipoFile.VBC));
		menuExportHtml.addActionListener(getEsportaActionListener(TipoFile.HTML));
		menuExportFile.add(menuExportTxt);
		menuExportFile.add(menuExportVbc);
		menuExportFile.add(menuExportHtml);
		this.add(menuExportFile);
		
		menuStampaFile.addActionListener(getStampaActionListener());
		this.add(menuStampaFile);
		
		menuPublishReport.addActionListener(getPublishListener());
		menuPublishOnline.add(menuPublishReport);
		menuResetCredentials.addActionListener(getResetCredentialsListener());
		menuPublishOnline.add(menuResetCredentials);
		this.add(menuPublishOnline);
	}

	private ActionListener getOpenActionListener(){
		 ActionListener listOpenFile = new ActionListener(){
				public void actionPerformed (ActionEvent e){
					int returnVal = fileChooser.showOpenDialog(null);
					if (returnVal == JFileChooser.APPROVE_OPTION){
						excelFile = fileChooser.getSelectedFile();
					}
				}
			};
			return listOpenFile;
	}
	
	private ActionListener getEsportaActionListener(final TipoFile tipoFile){
		 ActionListener listExport = new ActionListener(){
			public void actionPerformed (ActionEvent e){
				if (excelFile == null){
					JOptionPane.showMessageDialog(null, "Selezionare il foglio Excel con le partite da esportare","Attenzione!",JOptionPane.INFORMATION_MESSAGE);
				}else{
					try{
						String excelFileName = excelFile.getPath();
						String excelPrefix = excelFileName.substring(0, excelFileName.lastIndexOf('.'));
						String exportFileName = excelPrefix+"."+TipoFile.getFileExtension(tipoFile);
	                    File file = new File(exportFileName);
	                    FileWriter writer = new FileWriter(file);
	                    
						ExcelAccess excelAccess = new ExcelAccess(excelFile);
						excelAccess.openFileExcel();
						int i = 1;
						while (true){
							Partita[] partiteTurnoi = excelAccess.loadPartite(i,false,TipoTorneo.RadunoNazionale);
							if (partiteTurnoi == null){
								break;
							}else{
								switch (tipoFile) {
								case TEXT:
									for (Partita partita: partiteTurnoi){
										writer.write(partita.toStringForRanking()+"\n");
									}									
									break;
								case HTML:
									String html = generaHtmlPartite(partiteTurnoi, i);
									writer.write(html+"\n");
									break;								
								case VBC:
									String vbc = generavBcPartite(partiteTurnoi, i);
									writer.write(vbc+"\n");
									break;
								}
								i++;
							}
						}
						writer.close();
						excelAccess.closeFileExcel();
						JOptionPane.showMessageDialog(null, "Esportazione conclusa: il file esportato è \n"+exportFileName, "Risultato Finale", JOptionPane.INFORMATION_MESSAGE);
					}catch(Exception ex){
						RadGester.writeException(ex);
						JOptionPane.showMessageDialog(null, new TextException(ex),"Orrore!",JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		 };
		return listExport;
	}
	
	private ActionListener getStampaActionListener(){
		 ActionListener listExport = new ActionListener(){
			public void actionPerformed (ActionEvent e){
				if (excelFile == null){
					JOptionPane.showMessageDialog(null, "Selezionare il foglio Excel con le partite da stampare","Attenzione!",JOptionPane.INFORMATION_MESSAGE);
				}else{
					try{
						String excelFileName = excelFile.getPath();
						String excelPrefix = excelFileName.substring(0, excelFileName.lastIndexOf('.'));
						String stampaFileName = excelPrefix+".pdf";
	                    
						ExcelAccess excelAccess = new ExcelAccess(excelFile);
						excelAccess.openFileExcel();
						int i = 1;
						PdfUtils pdfUtils = new PdfUtils();
						pdfUtils.openDocument(stampaFileName);
						while (true){
							Partita[] partiteTurnoi = excelAccess.loadPartite(i,false,TipoTorneo.RadunoNazionale);
							if (partiteTurnoi == null){
								pdfUtils.closeDocument();
								break;
							}else{
								pdfUtils.stampaPartiteRisiko(partiteTurnoi, String.valueOf(i), excelAccess.leggiSchedaTorneo());
								i++;
								pdfUtils.newPageDocument();
							}
						}
						excelAccess.closeFileExcel();
						JOptionPane.showMessageDialog(null, "Stampa conclusa: il file prodotto è \n"+stampaFileName, "Risultato Finale", JOptionPane.INFORMATION_MESSAGE);
					}catch(Exception ex){
						RadGester.writeException(ex);
						JOptionPane.showMessageDialog(null, new TextException(ex),"Orrore!",JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		 };
		return listExport;
	}
	
	private ActionListener getPublishListener(){
		 ActionListener listExport = new ActionListener(){
			public void actionPerformed (ActionEvent e){
				if (excelFile == null){
					JOptionPane.showMessageDialog(null, "Selezionare il foglio Excel con le partite da pubblicare","Attenzione!",JOptionPane.INFORMATION_MESSAGE);
				}else{
					try{
						ExcelValidator excelValidator = new ExcelValidator(excelFile);
						ExcelValidatorData excelValidatorData = excelValidator.validaFoglioExcel();
						if (excelValidatorData != null && !excelValidatorData.containsErrors()){	
							if (!excelValidatorData.containsWarnings() || pubblicaWarningsDiValidazione(excelValidatorData.getWarnings())){
								GoogleDrivePublisher googleDrivePublisher = new GoogleDrivePublisher(excelFile.getPath());
								Thread t = new Thread(googleDrivePublisher);
								t.start();			
							}
						}else if (excelValidatorData != null && excelValidatorData.containsErrors()){
							pubblicaErroriDiValidazione(excelValidatorData.getErrors());
						}
					}catch(Exception ex){
						RadGester.writeException(ex);
						JOptionPane.showMessageDialog(null, new TextException(ex),"Orrore!",JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		 };
		return listExport;
	}

	private ActionListener getResetCredentialsListener(){
		 ActionListener listExport = new ActionListener(){
			public void actionPerformed (ActionEvent e){
				GoogleDrivePublisher.resetCredentials();
				JOptionPane.showMessageDialog(null, "Reset effettuato", "Risultato Finale", JOptionPane.INFORMATION_MESSAGE);
			}
		 };
		return listExport;
	}

	
	private void pubblicaErroriDiValidazione(List<ExcelValidatorMessages> messaggiDiValidazione){
		Object[] header = new String[]{"Scheda", "Messaggio di errore"};
		
		Object[][] rows = new String[messaggiDiValidazione.size()][2];
		int indexRows = 0;
		for (ExcelValidatorMessages excelValidatorMessages: messaggiDiValidazione){
			if (excelValidatorMessages.getSchedaDiRiferimento() != null){
				rows[indexRows][0] = excelValidatorMessages.getSchedaDiRiferimento().name();
			}
			rows[indexRows][1] = excelValidatorMessages.getMessage();
			indexRows++;
		}
		JTable errorTable = new JTable(rows, header);
		//errorTable.setSize(400, 400);
		errorTable.setPreferredSize(new Dimension(1000, 500));
		//errorTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		//errorTable.doLayout();
		TableColumnModel jTableColumnModel = errorTable.getColumnModel();
		TableColumn columnScheda = jTableColumnModel.getColumn(0);
		columnScheda.setPreferredWidth(150);
		TableColumn columnMessage = jTableColumnModel.getColumn(1);
		columnMessage.setPreferredWidth(850);
		JScrollPane scrollPane = new JScrollPane(errorTable);
		scrollPane.setPreferredSize(new Dimension(1000, 500));
		errorTable.setFillsViewportHeight(true);
		JOptionPane.showMessageDialog(null, scrollPane,"Foglio Excel non completo",JOptionPane.ERROR_MESSAGE);
	}
	
	private boolean pubblicaWarningsDiValidazione(List<ExcelValidatorMessages> messaggiDiValidazione){
		Object[] header = new String[]{"Scheda", "Messaggio di errore"};
		
		Object[][] rows = new String[messaggiDiValidazione.size()][2];
		int indexRows = 0;
		for (ExcelValidatorMessages excelValidatorMessages: messaggiDiValidazione){
			if (excelValidatorMessages.getSchedaDiRiferimento() != null){
				rows[indexRows][0] = excelValidatorMessages.getSchedaDiRiferimento().name();
			}
			rows[indexRows][1] = excelValidatorMessages.getMessage();
			indexRows++;
		}
		JTable errorTable = new JTable(rows, header);
		//errorTable.setSize(400, 400);
		errorTable.setPreferredSize(new Dimension(1000, 500));
		//errorTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		//errorTable.doLayout();
		TableColumnModel jTableColumnModel = errorTable.getColumnModel();
		TableColumn columnScheda = jTableColumnModel.getColumn(0);
		columnScheda.setPreferredWidth(150);
		TableColumn columnMessage = jTableColumnModel.getColumn(1);
		columnMessage.setPreferredWidth(850);
		JScrollPane scrollPane = new JScrollPane(errorTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setPreferredSize(new Dimension(1200, 500));
		errorTable.setFillsViewportHeight(true);
		//Integer response = JOptionPane.showConfirmDialog(null, scrollPane, "Confermi di voler procedere nonostante i messaggi indicati?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		Integer response = JOptionPane.showOptionDialog(null, scrollPane, "Confermi di voler procedere nonostante i messaggi indicati?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, new String[]{"Pubblica ugualmente", "Annulla"}, null);
		return response == JOptionPane.OK_OPTION;
	}
	
	public File getExcelFile() {
		return excelFile;
	}
	
	private String generaHtmlPartite(Partita[] partite, int numeroTurno){
		StringBuilder buffer = new StringBuilder();
		buffer.append("<HTML>");
		buffer.append("<STYLE>");
		buffer.append(".tr1{background-color: #FF6600; font: 12pt Arial}");
		buffer.append(".tr2{background-color: orange; font: 12pt Arial}");
		buffer.append("</STYLE>");
		buffer.append("<BODY>");
		buffer.append("<TABLE>");
		for (int numero = 0; numero < partite.length; numero++){
			int numeroTavolo = partite[numero].getNumeroTavolo();
			int numeroGiocatori = partite[numero].getNumeroGiocatori();
			buffer.append("<TR class=\"tr1\"><TD colspan=\""+numeroGiocatori*2+"\" align=\"center\">Turno nï¿½ "+numeroTurno+" - Tavolo nï¿½ "+numeroTavolo+"</TD></TR>");
			buffer.append("<TR class=\"tr2\">");
			for (GiocatoreDTO giocatore: partite[numero].getGiocatori()){
				String nominativo = giocatore.getNome()+" "+giocatore.getCognome();
				if (giocatore.getClubProvenienza() != null){
					nominativo +="</br>"+giocatore.getClubProvenienza();
				}
				Float punteggio = partite[numero].getPunteggio(giocatore);
				buffer.append("<TD style = \"padding:5px 5px 5px 5px;\">"+nominativo+"</TD><TD style = \"padding:10px 10px 10px 10px;\">"+punteggio.intValue()+"</TD>");
			}
			buffer.append("</TR>");
		}
		buffer.append("</TABLE>");
		buffer.append("</BODY>");
		buffer.append("</HTML>");
		buffer.append("</br></br></br>");
		return buffer.toString();
	}
	
	private String generavBcPartite(Partita[] partite, int numeroTurno){
		StringBuilder buffer = new StringBuilder();
		buffer.append("[FONT=Arial][SIZE=3][COLOR=Red][B]Turno nï¿½ "+numeroTurno+"[/B][/COLOR][/SIZE][/FONT]");
		buffer.append("[TABLE]");
		for (int numero = 0; numero < partite.length; numero++){
			int numeroTavolo = partite[numero].getNumeroTavolo();
			int numeroGiocatori = partite[numero].getNumeroGiocatori();
			buffer.append("[TR]");
			buffer.append("[TD][FONT=Arial][SIZE=3][COLOR=Red][B]"+numeroTavolo+"[/B][/COLOR][/SIZE][/FONT][/TD]");
			for (GiocatoreDTO giocatore: partite[numero].getGiocatori()){
				String nominativo = giocatore.getNome()+" "+giocatore.getCognome();
				if (giocatore.getClubProvenienza() != null){
					nominativo +="\n"+giocatore.getClubProvenienza();
				}
				Float punteggio = partite[numero].getPunteggio(giocatore);
				buffer.append("[TD][FONT=Arial][SIZE=3][COLOR=Sienna][B]"+nominativo+"[/B][/COLOR][/SIZE][/FONT][/TD][TD][FONT=Arial][SIZE=3][COLOR=Sienna][B]"+punteggio.intValue()+"[/B][/COLOR][/SIZE][/FONT][/TD]");
			}
			buffer.append("[/TR]");
		}
		buffer.append("[/TABLE]");
		buffer.append("\n\n\n");
		return buffer.toString();
	}
}
