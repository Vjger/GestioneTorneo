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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.BorderFactory;
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
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

public class MainPanelNew extends JFrame implements ActionListener {

	private final int WIDTH = 530;
	private final int HEIGHT = 530;
	
	private JComboBox<TipoTorneo> tipoTornei = new JComboBox<TipoTorneo>(TipoTorneo.getTipiAbilitati()); //values());
	private JSlider numeroTurno = new NumeroTurnoSlider();
	private JCheckBox stampaTurno = new JCheckBox("Stampa");
	
	private JButton classificaButton = getClassificaButton();
	
	private JMenuBar 			menuBar;
	private FileMenu fileMenu	  = new FileMenu();
	private JMenu optionsMenu 	= new OptionsMenu();
	private JMenu helpMenu    = new HelpMenu();

    private JTextArea textArea;

	public MainPanelNew(){
		MyLogger.getLogger().entering("MainPanel", "MainPanel");
		
		setTitle("GestioneTorneo "+ HelpMenu.VERSIONE);

		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screen.width-WIDTH)/2;
		int y = (screen.height-HEIGHT)/2;
		Rectangle r = new Rectangle(x,y,WIDTH+50,HEIGHT+50);
		setBounds(r);
		//setResizable(false);
		
		componiMenu();
		setJMenuBar(menuBar);
		
        textArea = new JTextArea();//(200, 10);
        textArea.setEditable(false);
    	MyLogger.setConsoleOutputStream(new CustomOutputStream(textArea));

        
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
		//add(classificaButton,BorderLayout.SOUTH);
        add(getLogPanel(), BorderLayout.SOUTH);
		
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
		checksPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		checksPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		//checksPanel.setSize(new Dimension(10,10));
		//checksPanel.setPreferredSize(new Dimension(10,10));
        JLabel tipoTorneoLabel = new JLabel("Tipo Torneo", JLabel.CENTER);
        tipoTorneoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        //c.insets = new Insets(50, 50, 50, 50);
        //c.anchor = GridBagConstraints.PAGE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.9;
        c.weighty = 0.9;
        checksPanel.add(tipoTorneoLabel, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 1;
        checksPanel.add(tipoTornei, c);

        JLabel sliderLabel = new JLabel("Numero Turno", JLabel.CENTER);
        sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 2;
        checksPanel.add(sliderLabel, c);
        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 3;
        checksPanel.add(numeroTurno, c);

        JPanel goPanel = new JPanel(new FlowLayout());
        goPanel.add(getConfirmButton());
        goPanel.add(stampaTurno);
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 4;
		checksPanel.add(goPanel,c);
				
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 4;
		checksPanel.add(classificaButton, c);
		checksPanel.setVisible(true);
		return checksPanel;
	}
	
	private JPanel getChecksPanel_Old(){
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
		checksPanel.add(classificaButton);
		checksPanel.setVisible(true);
		return checksPanel;
	}
	
	private JPanel getLogPanel(){
		JPanel checksPanel = new JPanel();
		//checksPanel.setSize(new Dimension(WIDTH,50));
		//checksPanel.setPreferredSize(new Dimension(WIDTH,50));
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setSize(new Dimension(WIDTH,250));
		scrollPane.setPreferredSize(new Dimension(WIDTH,250));
		checksPanel.add(scrollPane);
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
		execute(event);
	}

    private void execute(final ActionEvent event) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
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
        					}
        					//excelAccess.scriviStatistiche();
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
        						pdfUtils.stampaPartiteRisiko(partiteTurno, String.valueOf(numeroTurno));
        						pdfUtils.closeDocument();
        					}
        					//excelAccess.scriviStatistiche();
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
        });
        thread.start();
    }
    
	private int getNumeroTurno(){
		int numeroTurno = this.numeroTurno.getValue();
		return numeroTurno;
	}
	
	private TipoTorneo getTipoTorneo(){
		return (TipoTorneo)tipoTornei.getSelectedItem();
	}
}
