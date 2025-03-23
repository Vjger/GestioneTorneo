package it.desimone.risiko.torneo.panels;

import it.desimone.risiko.torneo.batch.RadGester;
import it.desimone.utils.MyLogger;
import it.desimone.utils.ResourceLoader;
import it.desimone.utils.TextException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

public class HelpMenu extends JMenu {
	
	public static final String VERSIONE = "2.3.6";
	
	private JMenuItem menuHelp= new JMenuItem("Help");
	private JMenuItem menuXls= new JMenuItem("Nuovo xls");
	private JMenuItem menuAbout= new JMenuItem("About");
	
	private File fileHelp;
	
	public HelpMenu(){
		super("?");
		
		menuHelp.addActionListener(getHelpActionListener());
		menuXls.addActionListener(getXLSActionListener());
		menuAbout.addActionListener(getAboutActionListener());
		
		this.add(menuHelp);
		this.add(menuXls);
		this.add(menuAbout);
	}

	private ActionListener getHelpActionListener(){
		/* Gestione degli eventi sul menù ? */
		 ActionListener listHelp = new ActionListener(){
			public void actionPerformed (ActionEvent e){
				try{
//					if ( (fileHelp == null) || (!fileHelp.exists()) ) {
//						fileHelp = new File(nomeFileHelp);
//						fileHelp.deleteOnExit();
//						InputStream is = this.getClass().getResourceAsStream("/" +nomeFileHelp);
//						byte [] buff = new byte [1024];
//						OutputStream out = new FileOutputStream(fileHelp);
//						int n;
//						while( (n = is.read(buff, 0, buff.length))!= -1){
//							out.write(buff, 0 , n);
//						}
//						is.close();
//						out.close();
//					}
					ResourceLoader rl = new ResourceLoader();
					fileHelp = rl.estraiManuale();
					if (fileHelp != null){
						String rigaDiComando = "cmd /c "+fileHelp.getPath();
						MyLogger.getLogger().finer("Riga di comando: "+rigaDiComando);
						Runtime.getRuntime().exec(rigaDiComando);
					}else{
						JOptionPane.showMessageDialog(null, "Problemi con l'estrazione del manuale: consultare il log","Orrore!",JOptionPane.ERROR_MESSAGE);
					}
				}catch(Exception ex){
					RadGester.writeException(ex);
					JOptionPane.showMessageDialog(null, new TextException(ex),"Orrore!",JOptionPane.ERROR_MESSAGE);
				}
			}
		 };
		 return listHelp;
	}
	
	private ActionListener getXLSActionListener(){
		/* Gestione degli eventi sul menù nuovo xls */
		 ActionListener listXls = new ActionListener(){
			public void actionPerformed (ActionEvent e){
				try{
					JFileChooser jfc = new JFileChooser();
					int returnVal = jfc.showSaveDialog(null);
					if (returnVal == JFileChooser.APPROVE_OPTION){
	                    File file = jfc.getSelectedFile();
	                    File writeFile = null;
	                    if (!file.getName().toLowerCase().endsWith(".xls") && !file.getName().toLowerCase().endsWith(".xlsx")){
	                    	writeFile = new File(file.getPath()+".xls");
	                    	file.renameTo(writeFile);
	                    }else{
	                    	writeFile = file;
	                    }
						ResourceLoader rl = new ResourceLoader();
						rl.estraiXLSVergine(writeFile);
						if (writeFile.exists()){
							JOptionPane.showMessageDialog(null, "File "+writeFile+" estratto","Operazione terminata",JOptionPane.INFORMATION_MESSAGE);
						}else{
							JOptionPane.showMessageDialog(null, "Problemi con l'estrazione del foglio xls: consultare il log","Orrore!",JOptionPane.ERROR_MESSAGE);
						}
//						InputStream is = this.getClass().getResourceAsStream("/" +nomeFileExcel);
//						byte [] buff = new byte [1024];
//						OutputStream out = new FileOutputStream(file);
//						int n;
//						while( (n = is.read(buff, 0, buff.length))!= -1){
//							out.write(buff, 0 , n);
//						}
//						is.close();
//						out.close();
					}
				}catch(Exception ex){
					RadGester.writeException(ex);
					JOptionPane.showMessageDialog(null, new TextException(ex),"Orrore!",JOptionPane.ERROR_MESSAGE);
				}
			}
		 };
		return listXls;
	}

	private ActionListener getAboutActionListener(){
		/* Gestione degli eventi sul menù About */
		 ActionListener listAbout = new ActionListener(){
			public void actionPerformed (ActionEvent e){
				JOptionPane.showMessageDialog(menuAbout,"GestioneTorneo "+VERSIONE+"\n\n\n Sviluppato da Marco De Simone \n\n\n vjger69@gmail.com");
			}	
		 };
		 return listAbout;
	}
}
