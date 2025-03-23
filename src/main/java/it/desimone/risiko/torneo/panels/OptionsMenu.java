package it.desimone.risiko.torneo.panels;

import it.desimone.risiko.torneo.utils.TorneiUtils;
import it.desimone.utils.Configurator;
import it.desimone.utils.MyLogger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.logging.Level;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

public class OptionsMenu extends JMenu {
	
	private JMenu logItem;
	private JMenu logConsoleMenuItem;
	private JMenu logStreamMenuItem;
	private JMenu logFileMenuItem;

	private JRadioButtonMenuItem highLogLevelConsoleMenuItem;
	private JRadioButtonMenuItem lowLogLevelConsoleMenuItem;
	private JRadioButtonMenuItem offLogLevelConsoleMenuItem;
	private ButtonGroup logLevelConsoleGroup;
	
	private JRadioButtonMenuItem highLogLevelStreamMenuItem;
	private JRadioButtonMenuItem lowLogLevelStreamMenuItem;
	private JRadioButtonMenuItem offLogLevelStreamMenuItem;
	private ButtonGroup logLevelStreamGroup;
	
	private JRadioButtonMenuItem highLogLevelFileMenuItem;
	private JRadioButtonMenuItem lowLogLevelFileMenuItem;
	private JRadioButtonMenuItem offLogLevelFileMenuItem;
	private ButtonGroup logLevelFileGroup;

	private static final String HIGH_LEVEL = "Livello alto";
	private static final String LOW_LEVEL = "Livello basso";
	private static final String OFF_LEVEL = "Disabilitato";
	
	private static final String CONSOLE 	= "console";
	private static final String STREAM 		= "stream";
	private static final String FILE 		= "file";
	
	private JCheckBoxMenuItem vincitoriUniciCheckBoxMenuItem;
	private JCheckBoxMenuItem memorizzaCredenzialiCheckBoxMenuItem;
	private JCheckBoxMenuItem stampaRidottaCheckBoxMenuItem;
	
	public OptionsMenu(){
		super("Opzioni");
		
		logItem = new JMenu("Log"); 
		
		logConsoleMenuItem = new JMenu("Console");
		
		offLogLevelConsoleMenuItem = new JRadioButtonMenuItem(OFF_LEVEL);
		offLogLevelConsoleMenuItem.addActionListener(getLogLevelListener(Level.OFF, CONSOLE));
		lowLogLevelConsoleMenuItem = new JRadioButtonMenuItem(LOW_LEVEL);
		lowLogLevelConsoleMenuItem.addActionListener(getLogLevelListener(Level.INFO, CONSOLE));
		highLogLevelConsoleMenuItem = new JRadioButtonMenuItem(HIGH_LEVEL);
		highLogLevelConsoleMenuItem.addActionListener(getLogLevelListener(Level.FINEST, CONSOLE));
		offLogLevelConsoleMenuItem.doClick(); //default
		
		logLevelConsoleGroup = new ButtonGroup();
		logLevelConsoleGroup.add(highLogLevelConsoleMenuItem);
		logLevelConsoleGroup.add(lowLogLevelConsoleMenuItem);
		logLevelConsoleGroup.add(offLogLevelConsoleMenuItem);
		
		logConsoleMenuItem.add(highLogLevelConsoleMenuItem);
		logConsoleMenuItem.add(lowLogLevelConsoleMenuItem);
		logConsoleMenuItem.add(offLogLevelConsoleMenuItem);
		
		logItem.add(logConsoleMenuItem);
		
		//logItem.addSeparator();
		
		logStreamMenuItem = new JMenu("Foglio Excel");
		
		offLogLevelStreamMenuItem = new JRadioButtonMenuItem(OFF_LEVEL);
		offLogLevelStreamMenuItem.addActionListener(getLogLevelListener(Level.OFF, STREAM));
		lowLogLevelStreamMenuItem = new JRadioButtonMenuItem(LOW_LEVEL);
		lowLogLevelStreamMenuItem.addActionListener(getLogLevelListener(Level.INFO, STREAM));
		lowLogLevelStreamMenuItem.doClick(); //default
		highLogLevelStreamMenuItem = new JRadioButtonMenuItem(HIGH_LEVEL);
		highLogLevelStreamMenuItem.addActionListener(getLogLevelListener(Level.FINEST, STREAM));
		
		logLevelStreamGroup = new ButtonGroup();
		logLevelStreamGroup.add(highLogLevelStreamMenuItem);
		logLevelStreamGroup.add(lowLogLevelStreamMenuItem);
		logLevelStreamGroup.add(offLogLevelStreamMenuItem);
		
		logStreamMenuItem.add(highLogLevelStreamMenuItem);
		logStreamMenuItem.add(lowLogLevelStreamMenuItem);
		logStreamMenuItem.add(offLogLevelStreamMenuItem);
		
		logItem.add(logStreamMenuItem);
		
		//logItem.addSeparator();
		
		logFileMenuItem = new JMenu("File");
		
		offLogLevelFileMenuItem = new JRadioButtonMenuItem(OFF_LEVEL);
		offLogLevelFileMenuItem.addActionListener(getLogLevelListener(Level.OFF, FILE));
		lowLogLevelFileMenuItem = new JRadioButtonMenuItem(LOW_LEVEL);
		lowLogLevelFileMenuItem.addActionListener(getLogLevelListener(Level.INFO, FILE));
		highLogLevelFileMenuItem = new JRadioButtonMenuItem(HIGH_LEVEL);
		highLogLevelFileMenuItem.addActionListener(getLogLevelListener(Level.FINEST, FILE));
		highLogLevelFileMenuItem.doClick(); //default
		
		logLevelFileGroup = new ButtonGroup();
		logLevelFileGroup.add(highLogLevelFileMenuItem);
		logLevelFileGroup.add(lowLogLevelFileMenuItem);
		logLevelFileGroup.add(offLogLevelFileMenuItem);
		
		logFileMenuItem.add(highLogLevelFileMenuItem);
		logFileMenuItem.add(lowLogLevelFileMenuItem);
		logFileMenuItem.add(offLogLevelFileMenuItem);
		
		logItem.add(logFileMenuItem);
		
		this.add(logItem);
		
		vincitoriUniciCheckBoxMenuItem = new JCheckBoxMenuItem("Unico vincitore");
		vincitoriUniciCheckBoxMenuItem.addItemListener(getVincitoriUniciListener());
		if (Configurator.getDefaultVincitoreUnico()){
			vincitoriUniciCheckBoxMenuItem.doClick();
		}
		this.add(vincitoriUniciCheckBoxMenuItem);
		
		memorizzaCredenzialiCheckBoxMenuItem = new JCheckBoxMenuItem("Memorizza credenziali Google");
		memorizzaCredenzialiCheckBoxMenuItem.addItemListener(getMemorizzaCredenzialiListener());
		if (Configurator.getMemorizzaCredenziali()){
			memorizzaCredenzialiCheckBoxMenuItem.doClick();
		}
		this.add(memorizzaCredenzialiCheckBoxMenuItem);
		
		stampaRidottaCheckBoxMenuItem = new JCheckBoxMenuItem("Stampa Ridotta");
		stampaRidottaCheckBoxMenuItem.addItemListener(getStampaRidottaListener());
		if (Configurator.getStampaRidotta()){
			stampaRidottaCheckBoxMenuItem.doClick();
		}
		this.add(stampaRidottaCheckBoxMenuItem);
		
	}

	private ActionListener getLogLevelListener(final Level level, final String handler){
		 ActionListener logLevelListener = new ActionListener(){
				public void actionPerformed (ActionEvent e){
					if (handler.equals(CONSOLE)){
						MyLogger.setConsoleLogLevel(level);
					}else if (handler.equals(STREAM)){
						MyLogger.setStreamLogLevel(level);
					}else if (handler.equals(FILE)){
						MyLogger.setFileLogLevel(level);
					}
				}
			};
		return logLevelListener;
	}
	
	private ItemListener getVincitoriUniciListener(){
		ItemListener checkVincitoriUniciListener = new ItemListener(){
				public void itemStateChanged(ItemEvent e){
					if (e.getStateChange() == ItemEvent.SELECTED){
						TorneiUtils.vincitoriUnici = Boolean.TRUE;
					}else{
						TorneiUtils.vincitoriUnici = Boolean.FALSE;
					}
				}
			};
		return checkVincitoriUniciListener;
	}
	
	private ItemListener getMemorizzaCredenzialiListener(){
		ItemListener checkMemorizzaCredenzialiListener = new ItemListener(){
				public void itemStateChanged(ItemEvent e){
					Boolean memorizzaCredenziali = e.getStateChange() == ItemEvent.SELECTED;
					Configurator.setMemorizzaCredenziali(memorizzaCredenziali);
				}
			};
		return checkMemorizzaCredenzialiListener;
	}
	
	private ItemListener getStampaRidottaListener(){
		ItemListener checkStampaRidottaListener = new ItemListener(){
				public void itemStateChanged(ItemEvent e){
					Boolean stampaRidotta = e.getStateChange() == ItemEvent.SELECTED;
					Configurator.setStampaRidotta(stampaRidotta);
				}
			};
		return checkStampaRidottaListener;
	}
}
