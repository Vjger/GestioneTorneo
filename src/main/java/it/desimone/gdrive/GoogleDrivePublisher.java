package it.desimone.gdrive;

import it.desimone.risiko.torneo.batch.RadGester;
import it.desimone.utils.ResourceLoader;
import it.desimone.utils.TextException;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.Popup;
import javax.swing.PopupFactory;

public class GoogleDrivePublisher implements Runnable {
	
	private String excelFileNameToPublish;
	
	public GoogleDrivePublisher(String excelFileNameToPublish){
		this.excelFileNameToPublish = excelFileNameToPublish;
	}

	public static void resetCredentials(){
		GoogleDriveAccess.resetGoogleAccess();
	}
	
	public void run() {
		JLabel label = new JLabel(new ImageIcon(ResourceLoader.getLoadingIconPath()));
		label.setVisible(true);
		
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screen.width-50)/2;
		int y = (screen.height-50)/2;
		Popup popup = PopupFactory.getSharedInstance().getPopup(null, label, x, y);
		popup.show();
		
		try{
			GoogleDriveAccess googleDriveAccess = new GoogleDriveAccess();
			boolean uploaded = googleDriveAccess.uploadReportOnAvailablesFolders(new File(excelFileNameToPublish));
			
			popup.hide();
			String finalMessage = null;
			if (uploaded){
				finalMessage = "Pubblicazione correttamente effettuata";
			}else{
				finalMessage = "Non è stato possibile effettuare la pubblicazione: verificare i log e riprovare";
			}
			
			JOptionPane.showMessageDialog(null, finalMessage, "Risultato Finale", JOptionPane.INFORMATION_MESSAGE);
		}catch(Exception ex){
			RadGester.writeException(ex);
			JOptionPane.showMessageDialog(null, new TextException(ex),"Orrore!",JOptionPane.ERROR_MESSAGE);
		}finally{
			popup.hide();
		}

	}

}
