package it.desimone.risiko.torneo.batch;

import it.desimone.risiko.torneo.panels.MainPanel;
import it.desimone.utils.ExceptionUtils;
import it.desimone.utils.MyLogger;
import it.desimone.utils.TextException;

import javax.swing.JOptionPane;

public class RadGester {

	/**
	 * @param args
	 */
	
	
	public static void main(String[] args) {
		
		try{
			//new StartPanelTornei();
			new MainPanel();
		}catch (Throwable e) {
			writeException(e);
			JOptionPane.showMessageDialog(null, new TextException(e),"Orrore!",JOptionPane.ERROR_MESSAGE);
		}

	}
	
	public static void writeException (Throwable e){
		String logString = ExceptionUtils.parseException(e);
		MyLogger.getLogger().severe(logString);
	}
	
//	public static void writeException (Throwable e){
//		File log = new File(".\\RadGester.log");
//		String logString = ExceptionUtils.parseException(e);
//		try {
//			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(log));
//			bufferedWriter.write(logString);
//			bufferedWriter.close();
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//	}

}
