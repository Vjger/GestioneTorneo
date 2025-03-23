package it.desimone.risiko.torneo.panels;

import it.desimone.risiko.torneo.batch.ExcelAccess;
import it.desimone.risiko.torneo.batch.RadGester;
import it.desimone.risiko.torneo.batch.Sorteggiatore;
import it.desimone.risiko.torneo.dto.Partita;
import it.desimone.risiko.torneo.utils.PdfUtils;
import it.desimone.risiko.torneo.utils.TipoTorneo;
import it.desimone.utils.MyException;
import it.desimone.utils.MyLogger;
import it.desimone.utils.TextException;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

public class MainPanel extends JFrame implements ActionListener {

	private final int WIDTH = 230;
	private final int HEIGHT = 230;
	
	private JComboBox<TipoTorneo> tipoTornei = new JComboBox<TipoTorneo>(TipoTorneo.getTipiAbilitati()); //values());
	private JSlider numeroTurno = new NumeroTurnoSlider();
	private JCheckBox stampaTurno = new JCheckBox("Stampa");
	
	private JButton classificaButton = getClassificaButton();
	
	private JMenuBar 			menuBar;
	private FileMenu fileMenu	  = new FileMenu();
	private JMenu optionsMenu 	= new OptionsMenu();
	private JMenu helpMenu    = new HelpMenu();
	
	public MainPanel(){
		MyLogger.getLogger().entering("MainPanel", "MainPanel");
		
		setTitle("GestioneTorneo "+ HelpMenu.VERSIONE);

		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screen.width-WIDTH)/2;
		int y = (screen.height-HEIGHT)/2;
		Rectangle r = new Rectangle(x,y,WIDTH+50,HEIGHT+50);
		setBounds(r);
		setResizable(false);
		
		componiMenu();
		setJMenuBar(menuBar);
		
		tipoTornei.setRenderer(new BasicComboBoxRenderer() {
		    public Component getListCellRendererComponent(JList list, Object value,
		            int index, boolean isSelected, boolean cellHasFocus) {
		          if (isSelected) {
		            setBackground(list.getSelectionBackground());
		            setForeground(list.getSelectionForeground());
		            if (-1 < index) {
		            TipoTorneo valore = (TipoTorneo) value;
		              list.setToolTipText(TipoTorneo.getDescrizione(valore));
		            }
		          } else {
		            setBackground(list.getBackground());
		            setForeground(list.getForeground());
		          }
		          setFont(list.getFont());
		          setText((value == null) ? "" : value.toString());
		          return this;
		        }
		      }
				
				
		);
		
		
		setLayout(new BorderLayout());
		//add(fileButton,BorderLayout.NORTH);
		add(getVoidPanel(),BorderLayout.EAST);
		add(getVoidPanel(),BorderLayout.WEST);
		add(getChecksPanel(),BorderLayout.CENTER);
		add(classificaButton,BorderLayout.SOUTH);
		
		addWindowListener(new WindowAdapter() {
			  public void windowClosing(WindowEvent we) {
				MyLogger.closeFileHandler();
			    System.exit(0);
			  }
			});

		
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		 //setSize(95,95);
		
		MyLogger.getLogger().exiting("MainPanel", "MainPanel");
	}
	
	
	private void componiMenu(){
		menuBar 		= new JMenuBar();
		menuBar.add(fileMenu);
		menuBar.add(optionsMenu);
		menuBar.add(Box.createHorizontalGlue());
		menuBar.add(helpMenu);
	}
	
	private JPanel getVoidPanel(){
		JPanel voidPanel = new JPanel();
		voidPanel.setPreferredSize(new Dimension(15,15));
		return voidPanel;
	}
	
	private JPanel getChecksPanel(){
		JPanel checksPanel = new JPanel();
		checksPanel.setSize(new Dimension(10,10));
		//checksPanel.setPreferredSize(new Dimension(10,10));
        JLabel tipoTorneoLabel = new JLabel("Tipo Torneo", JLabel.CENTER);
        tipoTorneoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        checksPanel.add(tipoTorneoLabel);
		checksPanel.add(tipoTornei);
        JLabel sliderLabel = new JLabel("Numero Turno", JLabel.CENTER);
        sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        checksPanel.add(sliderLabel);
		checksPanel.add(numeroTurno);
		checksPanel.add(getConfirmButton());
		checksPanel.add(stampaTurno);
		checksPanel.setVisible(true);
		return checksPanel;
	}
	
	private JButton getClassificaButton(){
		JButton button = new JButton("Classifica");
		button.setPreferredSize(new Dimension(60,25));
		button.addActionListener(this);
		return button;
	}
	
	private JButton getConfirmButton(){
		JButton button = new JButton("GO!");
		button.setPreferredSize(new Dimension(60,25));
		button.addActionListener(this);
		return button;
	}
	
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		try{
			File excelFile = fileMenu.getExcelFile();
			if (source == classificaButton){
				if (excelFile == null){
					throw new MyException("Selezionare il foglio Excel con i partecipanti");
				}else{
					ExcelAccess excelAccess = new ExcelAccess(excelFile);
					excelAccess.openFileExcel();
					excelAccess.scriviClassifica(getTipoTorneo());
					if (getTipoTorneo() == TipoTorneo.TorneoASquadre){
						excelAccess.scriviClassificaASquadre(getTipoTorneo());
						//excelAccess.scriviStatistiche();
					}
					excelAccess.closeFileExcel();
					MyLogger.getLogger().info("***  Fine calcolo classifica per tipo Torneo "+getTipoTorneo()+" versione "+HelpMenu.VERSIONE+" ***");
					String message = "Fine Elaborazione"; // \n"+Logger.getLog();
					JOptionPane.showMessageDialog(null, message, "Risultato Finale", JOptionPane.INFORMATION_MESSAGE);
				}
			}else{
				if (excelFile == null){
					throw new MyException("Selezionare il foglio Excel con i partecipanti");
				}else if (getNumeroTurno() == 0){
					throw new MyException("Effettuare la scelta di almeno un turno");
				}else{
					MyLogger.getLogger().info("Versione "+HelpMenu.VERSIONE+": Inizio Sorteggio per modalità "+getTipoTorneo()+" per turno n° "+getNumeroTurno());
					//ExcelAccess excelAccess = new ExcelAccessNew(excelFile);
					ExcelAccess excelAccess = new ExcelAccess(excelFile);
					excelAccess.openFileExcel();
					int numeroTurno = getNumeroTurno();
					Partita[] partiteTurno = Sorteggiatore.getPartiteSorteggiate(excelAccess, getTipoTorneo(), numeroTurno);
					for (Partita partita: partiteTurno){
						excelAccess.scriviPartite(ExcelAccess.getNomeTurno(numeroTurno), partita);
					}
					if (stampaTurno != null && stampaTurno.isSelected()){
						String excelFileName = excelFile.getPath();
						String excelPrefix = excelFileName.substring(0, excelFileName.lastIndexOf('.'));
						String pdfFileName = excelPrefix+"_"+numeroTurno+".pdf";
						PdfUtils pdfUtils = new PdfUtils();
						pdfUtils.openDocument(pdfFileName);
						pdfUtils.stampaPartiteRisiko(partiteTurno, String.valueOf(numeroTurno), excelAccess.leggiSchedaTorneo());
						pdfUtils.closeDocument();
					}
					if (getTipoTorneo() == TipoTorneo.TorneoASquadre){
						excelAccess.scriviStatistiche();
					}
					excelAccess.scriviLog(MyLogger.getListLogStream());
					excelAccess.closeFileExcel();
					String message = "Fine Elaborazione\n";
					JOptionPane.showMessageDialog(null, message, "Risultato Finale", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		}catch (Exception e) {
			RadGester.writeException(e);
			JOptionPane.showMessageDialog(null, new TextException(e),"Orrore!",JOptionPane.ERROR_MESSAGE);
		}
	}
	
	
//	public void actionPerformed(ActionEvent event) {
//		Object source = event.getSource();
//		try{
//			File excelFile = fileMenu.getExcelFile();
//			if (source == classificaButton){
//				if (excelFile == null){
//					throw new MyException("Selezionare il foglio Excel con i partecipanti");
//				}else{
//					ExcelAccess excelAccess = new ExcelAccess(excelFile);
//					excelAccess.openFileExcel();
//					excelAccess.scriviClassifica(getTipoTorneo());
//					excelAccess.closeFileExcel();
//					MyLogger.getLogger().info("***  Fine calcolo classifica per tipo Torneo "+getTipoTorneo()+"  ***");
//					String message = "Fine Elaborazione"; // \n"+Logger.getLog();
//					JOptionPane.showMessageDialog(null, message, "Risultato Finale", JOptionPane.INFORMATION_MESSAGE);
//				}
//			}else{
//				if (excelFile == null){
//					throw new MyException("Selezionare il foglio Excel con i partecipanti");
//				}else if (getNumeroTurno() == 0){
//					throw new MyException("Effettuare la scelta di almeno un turno");
//				}else{
//					MyLogger.getLogger().info("Inizio Sorteggio per modalità "+getTipoTorneo()+" per turno n° "+getNumeroTurno());
//					ExcelAccess excelAccess = new ExcelAccess(excelFile);
//					excelAccess.openFileExcel();
//					int numeroTurno = getNumeroTurno();
//					List<GiocatoreDTO> giocatoriTurno = excelAccess.getListaGiocatori(true);
//					Partita[] partiteTurno = null;
//					if (numeroTurno == 1){
//						switch (getTipoTorneo()) {
//						case RadunoNazionale:
//							partiteTurno = GeneratoreTavoli.tavoliPrimoTurno(giocatoriTurno);
//							break;
//						case CampionatoGufo:
//							partiteTurno = GeneratoreTavoli.tavoliPrimoTurnoCampionatoGufo(giocatoriTurno);
//							break;
//						case ColoniDiCatan:
//						case Dominion:
//						case StoneAge:
//							partiteTurno = GeneratoreTavoli.tavoliPrimoTurnoTorneoColoni(giocatoriTurno);
//							break;
//						default:
//							partiteTurno = GeneratoreTavoli.tavoliPrimoTurnoTorneoGufo(giocatoriTurno);
//							break;
//						} 
//					}else if (numeroTurno == 3 && getTipoTorneo() == TipoTorneo.RadunoNazionale){
//						List<ScorePlayer> scores = excelAccess.getClassificaRaduno(true);
//						List<GiocatoreDTO> semifinalisti = new ArrayList<GiocatoreDTO>();
//						if (scores.size() <= 15){
//							MyLogger.getLogger().severe("Impossibile elaborare le semifinali; meno di 16 giocatori: "+scores.size());
//							throw new MyException("Impossibile elaborare le semifinali; meno di 16 giocatori: "+scores.size());
//						}
//							for (int i = 0; i < 16; i++){ //suddivisione in 4 fasce di 4 giocatori
//								GiocatoreDTO giocatore = scores.get(i).getGiocatore();
//								if (i <= 3){
//									giocatore.setRegioneProvenienza(RegioniLoader.FASCIA1);
//								}else if (i > 3 && i<=7){
//									giocatore.setRegioneProvenienza(RegioniLoader.FASCIA2);
//								}else if (i > 7 && i<=11){
//									giocatore.setRegioneProvenienza(RegioniLoader.FASCIA3);
//								}else{
//									giocatore.setRegioneProvenienza(RegioniLoader.FASCIA4);
//								}
//								semifinalisti.add(giocatore);
//							}
//
//						MyLogger.getLogger().finest("Semifinalisti: "+semifinalisti.toString());
//						partiteTurno = GeneratoreTavoli.tavoliPrimoTurno(semifinalisti);
//					}else if (numeroTurno == 3 && getTipoTorneo() == TipoTorneo.QualificazioniRisiko){
//						List<ScorePlayer> scores = excelAccess.getClassificaQualificazioniNazionale(true, false);
//						ScorePlayer primoInClassifica = scores.get(0);
//						ScorePlayer secondoInClassifica = scores.get(1);
//						List<GiocatoreDTO> semifinalisti = new ArrayList<GiocatoreDTO>();
//						boolean primoConDuevittorieESolitario = primoInClassifica.getNumeroVittorie() == 2 && primoInClassifica.getPunteggio(false).compareTo(secondoInClassifica.getPunteggio(false)) == 1;
//						if (primoConDuevittorieESolitario){
//							if (scores.size() <= 12){
//								MyLogger.getLogger().severe("Impossibile elaborare le semifinali; meno di 13 giocatori: "+scores.size());
//								throw new MyException("Impossibile elaborare le semifinali; meno di 13 giocatori: "+scores.size());
//							}
//							for (int i = 1; i <=12; i++){
//								GiocatoreDTO giocatore = scores.get(i).getGiocatore();
//								if (i <= 3){ //suddivisione in 4 fasce di 3 giocatori
//									giocatore.setRegioneProvenienza(RegioniLoader.FASCIA1);
//								}else if (i > 3 && i<=6){
//									giocatore.setRegioneProvenienza(RegioniLoader.FASCIA2);
//								}else if (i > 6 && i<=9){
//									giocatore.setRegioneProvenienza(RegioniLoader.FASCIA3);
//								}else{
//									giocatore.setRegioneProvenienza(RegioniLoader.FASCIA4);
//								}
//								semifinalisti.add(giocatore);
//							}
//						}else{
//							if (scores.size() <= 15){
//								MyLogger.getLogger().severe("Impossibile elaborare le semifinali; meno di 16 giocatori: "+scores.size());
//								throw new MyException("Impossibile elaborare le semifinali; meno di 16 giocatori: "+scores.size());
//							}
//							for (int i = 0; i < 16; i++){ //suddivisione in 4 fasce di 4 giocatori
//								GiocatoreDTO giocatore = scores.get(i).getGiocatore();
//								if (i <= 3){
//									giocatore.setRegioneProvenienza(RegioniLoader.FASCIA1);
//								}else if (i > 3 && i<=7){
//									giocatore.setRegioneProvenienza(RegioniLoader.FASCIA2);
//								}else if (i > 7 && i<=11){
//									giocatore.setRegioneProvenienza(RegioniLoader.FASCIA3);
//								}else{
//									giocatore.setRegioneProvenienza(RegioniLoader.FASCIA4);
//								}
//								semifinalisti.add(giocatore);
//							}
//						}
//						MyLogger.getLogger().finest("Semifinalisti: "+semifinalisti.toString());
//						partiteTurno = GeneratoreTavoli.tavoliPrimoTurno(semifinalisti);
//					}else if (numeroTurno == 4 && getTipoTorneo() == TipoTorneo.RadunoNazionale){
//						Partita finale = new Partita();
//						finale.setNumeroGiocatori(4);
//						finale.setNumeroTavolo(1);
//						Partita[] semifinali = excelAccess.loadPartite(3, false, getTipoTorneo());
//						for (Partita semifinale: semifinali){
//							for (GiocatoreDTO semifinalista: semifinale.getGiocatori()){
//								if (semifinale.isVincitore(semifinalista)){
//									finale.addGiocatore(semifinalista, null);
//									break;
//								}
//							}
//						}
//						partiteTurno = new Partita[]{finale};
//						MyLogger.getLogger().finest("Finale: "+finale);
//					}else if (numeroTurno == 4 && getTipoTorneo() == TipoTorneo.QualificazioniRisiko){
//						List<ScorePlayer> scores = excelAccess.getClassificaQualificazioniNazionale(true, false);
//						ScorePlayer primoInClassifica = scores.get(0);
//						ScorePlayer secondoInClassifica = scores.get(1);
//						Partita finale = new Partita();
//						finale.setNumeroGiocatori(4);
//						finale.setNumeroTavolo(1);
//						boolean primoConDuevittorieESolitario = primoInClassifica.getNumeroVittorie() == 2 && primoInClassifica.getPunteggio(false).compareTo(secondoInClassifica.getPunteggio(false)) == 1;
//						if (primoConDuevittorieESolitario){
//							GiocatoreDTO giocatore = scores.get(0).getGiocatore();
//							finale.addGiocatore(giocatore, null);
//						}
//						Partita[] semifinali = excelAccess.loadPartite(3, false, getTipoTorneo());
//						for (Partita semifinale: semifinali){
//							for (GiocatoreDTO semifinalista: semifinale.getGiocatori()){
//								if (semifinale.isVincitore(semifinalista)){
//									finale.addGiocatore(semifinalista, null);
//									break;
//								}
//							}
//						}
//						partiteTurno = new Partita[]{finale};
//						MyLogger.getLogger().finest("Finale: "+finale);
//					}else if (
//								(numeroTurno > 4 && getTipoTorneo() == TipoTorneo.QualificazioniRisiko)
//							||	(numeroTurno > 4 && getTipoTorneo() == TipoTorneo.RadunoNazionale)
//							){
//								throw new MyException("Non è previsto il "+numeroTurno+"° Turno per il tipo di Torneo "+getTipoTorneo());
//					}else{
//						List<Partita> listaPartitePrecedenti = new ArrayList<Partita>();
//						for (int i = 1; i < numeroTurno; i++){
//							Partita[] partiteTurnoi = excelAccess.loadPartite(i,false,getTipoTorneo());
//							if (partiteTurnoi == null){
//								throw new MyException("E' stato richiesto il sorteggio per il turno "+numeroTurno+" ma non esiste il turno "+i);
//							}
//							listaPartitePrecedenti.addAll(Arrays.asList(partiteTurnoi));
//						}
//						Partita[] partitePrecedenti = listaPartitePrecedenti.toArray(new Partita[0]);
//						switch (getTipoTorneo()) {
//						case RadunoNazionale:
//							partiteTurno = GeneratoreTavoli.getTavoliSecondoTurno(giocatoriTurno, partitePrecedenti);
//							break;
//						case QualificazioniRisiko:
//							List<PrioritaSorteggio> priorita = new ArrayList<PrioritaSorteggio>();
//							priorita.add(PrioritaSorteggio.MINIMIZZAZIONE_PARTECIPAZIONE_TAVOLO_DA_5);
//							priorita.add(PrioritaSorteggio.VINCITORI_SEPARATI);
//							priorita.add(PrioritaSorteggio.IMPEDITO_STESSO_CLUB);
//							priorita.add(PrioritaSorteggio.MINIMIZZAZIONE_SCONTRI_DIRETTI);
//							partiteTurno = GeneratoreTavoliNew.generaPartite(giocatoriTurno, partitePrecedenti, TipoTavoli.DA_4_ED_EVENTUALMENTE_DA_5, priorita);
//							break;
//						case CampionatoGufo:
//							partiteTurno = GeneratoreTavoli.tavoliTurniSuccessiviCampionatoGufo(giocatoriTurno, partitePrecedenti);
//							break;
//						case ColoniDiCatan:
//							List<ScorePlayer> scores = excelAccess.getClassificaTorneoColoni();
//							List<GiocatoreDTO> players = new ArrayList<GiocatoreDTO>();
//							for (ScorePlayer scorePlayer: scores){
//								if (giocatoriTurno.contains(scorePlayer.getGiocatore())){
//									players.add(scorePlayer.getGiocatore());
//								}
//							}
//							partiteTurno = GeneratoreTavoli.tavoliOrdinatiColoni(players);
//							break;
//						case Dominion:
//							List<ScorePlayer> scoresDominion = excelAccess.getClassificaTorneoDominion();
//							List<GiocatoreDTO> playersDominion = new ArrayList<GiocatoreDTO>();
//							for (ScorePlayer scorePlayer: scoresDominion){
//								if (giocatoriTurno.contains(scorePlayer.getGiocatore())){
//									playersDominion.add(scorePlayer.getGiocatore());
//								}
//							}
//							partiteTurno = GeneratoreTavoli.tavoliOrdinatiColoni(playersDominion);
//							break;
//						case StoneAge:
//							List<ScorePlayer> scoresStoneAge = excelAccess.getClassificaTorneoStoneAge();
//							List<GiocatoreDTO> playersStoneAge = new ArrayList<GiocatoreDTO>();
//							for (ScorePlayer scorePlayer: scoresStoneAge){
//								if (giocatoriTurno.contains(scorePlayer.getGiocatore())){
//									playersStoneAge.add(scorePlayer.getGiocatore());
//								}
//							}
//							partiteTurno = GeneratoreTavoli.tavoliOrdinatiColoni(playersStoneAge);
//							break;
//						default:
//							partiteTurno = GeneratoreTavoli.tavoliTurniSuccessiviTorneoGufo(giocatoriTurno, partitePrecedenti);
//							break;
//						} 
//					}
//					for (Partita partita: partiteTurno){
//						switch (getTipoTorneo()) {
//						case StoneAge:
//							excelAccess.scriviPartiteStoneAge(ExcelAccess.getNomeTurno(numeroTurno), partita);
//							break;
//						case Dominion:
//							excelAccess.scriviPartiteDominion(ExcelAccess.getNomeTurno(numeroTurno), partita);
//							break;
//						case QualificazioniRisiko:
//						case RadunoNazionale:
//							if (numeroTurno > 2){
//								excelAccess.scriviPartiteFaseFinaleQualificazioneRisiko(ExcelAccess.getNomeTurno(numeroTurno), partita);
//							}else{
//								excelAccess.scriviPartite(ExcelAccess.getNomeTurno(numeroTurno), partita);
//							}
//							break;
//						default:
//							excelAccess.scriviPartite(ExcelAccess.getNomeTurno(numeroTurno), partita);
//							break;
//						}
////						if (stampaTurno != null && stampaTurno.isSelected()){
////							excelAccess.stampaPartite("Stampa "+ExcelAccess.getNomeTurno(numeroTurno), partita);
////						}
//					}
//					if (stampaTurno != null && stampaTurno.isSelected()){
//						String excelFileName = excelFile.getPath();
//						String excelPrefix = excelFileName.substring(0, excelFileName.lastIndexOf('.'));
//						String pdfFileName = excelPrefix+"_"+numeroTurno+".pdf";
//						PdfUtils pdfUtils = new PdfUtils();
//						pdfUtils.openDocument(pdfFileName);
//						pdfUtils.stampaPartiteRisiko(partiteTurno, String.valueOf(numeroTurno));
//						pdfUtils.closeDocument();
//					}
//					//excelAccess.scriviLog(Logger.getLog());
//					excelAccess.scriviLog(MyLogger.getListLogStream());
//					excelAccess.closeFileExcel();
//					String message = "Fine Elaborazione\n";//+Logger.getLog();
//					JOptionPane.showMessageDialog(null, message, "Risultato Finale", JOptionPane.INFORMATION_MESSAGE);
//				}
//			}
//		}catch (Exception e) {
//			RadGester.writeException(e);
//			JOptionPane.showMessageDialog(null, new TextException(e),"Orrore!",JOptionPane.ERROR_MESSAGE);
//		}
//	}

	private int getNumeroTurno(){
		int numeroTurno = this.numeroTurno.getValue();
		return numeroTurno;
	}
	
	private TipoTorneo getTipoTorneo(){
		return (TipoTorneo)tipoTornei.getSelectedItem();
	}
}
