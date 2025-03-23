package it.desimone.risiko.torneo.utils;

import it.desimone.risiko.torneo.dto.GiocatoreDTO;
import it.desimone.risiko.torneo.dto.Partita;
import it.desimone.risiko.torneo.dto.SchedaTorneo;
import it.desimone.risiko.torneo.dto.SchedaTorneo.TipoTorneo;
import it.desimone.utils.Configurator;
import it.desimone.utils.MyLogger;
import it.desimone.utils.ResourceLoader;
import it.desimone.utils.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class PdfUtils {
	
	//private InputStream immagineRisikoStream = this.getClass().getResourceAsStream("/image004_rid.png");
	//private URL immagineRisikoURL =  this.getClass().getResource("/image004_rid.png");
	//private static byte[] immagineRisikoByte;
	private Document document;
	private PdfWriter writer;
	
	private Image immagineTestata;
	
	private static final Phrase EMPTY_PHRASE = new Phrase("");
	
	private final DateFormat df = new SimpleDateFormat("EEEE dd/MM/yyyy", Locale.ITALY);
	
	public PdfUtils(){
		
		ResourceLoader rl = new ResourceLoader();
        //URL immagineRisikoURL = rl.getImmagineRisiko();
		//String immagineRisikoURL = rl.getImmagineRisiko();
		byte[] immagineRisikoURL = rl.getImmagineRisiko();
        try {
        	if (immagineRisikoURL != null){
            	MyLogger.getLogger().finer("Ottengo l'istanza dell'immagine della testata");
        		immagineTestata = Image.getInstance(immagineRisikoURL);
            	MyLogger.getLogger().finer("Scalo l'immagine della testata");
	        	immagineTestata.scalePercent(20.0f);
        	}
		} catch (Exception e) {
			MyLogger.getLogger().severe("Eccezione nella creazione dell'immagine per il pdf");
		} 
//		List lista = new ArrayList();
//		byte [] buff = new byte [1024];
//		int n;
//		try {
//			while( (n = immagineRisikoStream.read(buff, 0, buff.length))!= -1){
//				for (byte b: buff){
//					lista.add(b);
//				}
//			}
//			immagineRisikoByte = new byte[lista.size()];
//			Iterator iterator = lista.iterator();
//			int index = 0;
//			while(iterator.hasNext()){
//				immagineRisikoByte[index++] = (Byte)iterator.next();
//			}
//		} catch (IOException e) {
//			MyLogger.getLogger().severe("Eccezione: "+e.getMessage());
//		}
	}
	
	public void openDocument(String fileName){
		try{
			document = new Document(PageSize.A4, 50, 50, 50, 50);
			writer = PdfWriter.getInstance(document, new FileOutputStream(new File(fileName)));
	        document.open();
		} catch (IOException e) {
			MyLogger.getLogger().severe("Eccezione sul file "+fileName+" :"+e.getMessage());
		} catch (DocumentException e) {
			MyLogger.getLogger().severe("Eccezione sul file "+fileName+" :"+e.getMessage());
		}
	}
	
	public void closeDocument(){
		document.close();
	}
	
	public void newPageDocument(){
		document.newPage();
	}
	
	public void stampaPartiteRisiko(Partita[] partite, String nomeTurno){
		try{	
            for (Partita partita: partite){
            	stampaRefertoRisiko(document, partita, nomeTurno, null);
            	document.newPage();
            }
		}catch (IOException e) {
			MyLogger.getLogger().severe("IOException"+ " :"+e.getMessage());
		}catch (DocumentException e) {
			MyLogger.getLogger().severe("DocumentException"+ " :"+e.getMessage());
		}	
	}
	
	public void stampaPartiteRisiko(Partita[] partite, String nomeTurno, SchedaTorneo schedaTorneo){
		try{	
			int counter = 0;
			int divider = Configurator.getStampaRidotta()?2:1;
            for (Partita partita: partite){
            	stampaRefertoRisiko(document, partita, nomeTurno, schedaTorneo);
            	counter++;
            	if (counter%divider == 0){
            		document.newPage();
            	}
            }
		}catch (IOException e) {
			MyLogger.getLogger().severe("IOException"+ " :"+e.getMessage());
		}catch (DocumentException e) {
			MyLogger.getLogger().severe("DocumentException"+ " :"+e.getMessage());
		}	
	}
	
	private void stampaRefertoRisiko(Document document, Partita partita, String numeroTurno, SchedaTorneo schedaTorneo) throws DocumentException, IOException{
		
		MyLogger.getLogger().entering("PdfUtils", "stampaRefertoRisiko(document, "+partita+" "+numeroTurno+")");
		
        int numeroColonne = 5;
        PdfPTable table = new PdfPTable(new float[]{0.4f,0.12f,0.12f,0.12f,0.24f});//(numeroColonne);
        
        if (schedaTorneo != null && !StringUtils.isNullOrEmpty(schedaTorneo.getOrganizzatore()) && schedaTorneo.getTipoTorneo() != null && !StringUtils.isNullOrEmpty(schedaTorneo.getNomeTorneo())){
        	
        	PdfPCell cellLogo = null;
    		ResourceLoader rl = new ResourceLoader();
    		byte[] immagineLogoByte = rl.getImmagineLogo(schedaTorneo.getOrganizzatore());
        	if (immagineLogoByte == null){
        		immagineLogoByte = rl.getImmagineLogo("RCU");
        	}
            try {
            	MyLogger.getLogger().finer("Ottengo l'istanza dell'immagine del logo");
        		Image immagineLogo = Image.getInstance(immagineLogoByte);
        		immagineLogo.scalePercent(20.0f);
        		cellLogo = new PdfPCell(immagineLogo, true);  
                cellLogo.setPadding (5.0f);
        		cellLogo.setFixedHeight(50.0f);
    		} catch (Exception e) {
    			MyLogger.getLogger().severe("Eccezione nella creazione dell'immagine per il pdf");
    		} 
        	
            table.addCell(cellLogo);

        	Font font = new Font(Font.FontFamily.COURIER, 12.0f);
        	font.setColor(getForeGroundColor(schedaTorneo.getTipoTorneo()));
        	//phrase.setFont(font);
        	Phrase phrase = new Phrase (schedaTorneo.getNomeTorneo(), font);
            PdfPCell riga0 = new PdfPCell (phrase);
            riga0.setColspan (numeroColonne-1);
            riga0.setHorizontalAlignment (Element.ALIGN_CENTER);
            riga0.setBackgroundColor (getBackGroundColor(schedaTorneo.getTipoTorneo()));
            riga0.setPadding (10.0f);
            table.addCell(riga0);
        }
        
        String intestazioneTavolo = "Turno "+numeroTurno+" - Tavolo N°"+partita.getNumeroTavolo();
        if (schedaTorneo != null && schedaTorneo.getDataTurni() != null && schedaTorneo.getDataTurni().size() >= Integer.valueOf(numeroTurno) && schedaTorneo.getDataTurni().get(Integer.valueOf(numeroTurno)-1) != null){
        	intestazioneTavolo = df.format(schedaTorneo.getDataTurni().get(Integer.valueOf(numeroTurno)-1))+" - "+intestazioneTavolo;
        }
        PdfPCell riga1 = new PdfPCell (new Paragraph (intestazioneTavolo));
        riga1.setColspan (numeroColonne);
        riga1.setHorizontalAlignment (Element.ALIGN_CENTER);
//        riga1.setBackgroundColor (new BaseColor(128, 200, 128));
        riga1.setPadding (10.0f);
        table.addCell(riga1);
        
        if (!Configurator.getStampaRidotta()){
	        PdfPCell riga2;
	        if (immagineTestata != null){
	        	MyLogger.getLogger().finer("Setto l'immagine della testata");
	            riga2 = new PdfPCell(immagineTestata, true);        	
	        }else{
	        	riga2 = new PdfPCell(new Paragraph (""));
	        }
	        riga2.setColspan(numeroColonne);
	        riga2.setHorizontalAlignment (Element.ALIGN_CENTER);
//	        riga2.setBackgroundColor (new BaseColor(128, 200, 128));
	        riga2.setPadding (10.0f);
	        table.addCell(riga2);
        }
        
        table.addCell(new PdfPCell(new Phrase("Nominativo", new Font(Font.FontFamily.HELVETICA, 11.0f, Font.BOLD))));
        table.addCell(new PdfPCell(new Phrase("Punti", new Font(Font.FontFamily.HELVETICA, 11.0f, Font.BOLD))));
        table.addCell(new PdfPCell(new Phrase("Pti fuori obb.", new Font(Font.FontFamily.HELVETICA, 11.0f, Font.BOLD))));
        table.addCell(new PdfPCell(new Phrase("Punti Torneo", new Font(Font.FontFamily.HELVETICA, 11.0f, Font.BOLD))));
        table.addCell(new PdfPCell(new Phrase("Firma", new Font(Font.FontFamily.HELVETICA, 11.0f, Font.BOLD))));
               
    	MyLogger.getLogger().finest("Ciclo partite");
		for (GiocatoreDTO giocatore: partita.getGiocatori()){
			String nominativo = giocatore.getNome()+" "+giocatore.getCognome();
			if (giocatore.getClubProvenienza() != null){
				nominativo += " - "+giocatore.getClubProvenienza().getDenominazione();
			}
			Phrase nome_club = new Phrase(nominativo, new Font(Font.FontFamily.HELVETICA, 10)); 
			PdfPCell cellNome = new PdfPCell(nome_club);
			cellNome.setFixedHeight(25);
			table.addCell(cellNome);
			Float punteggio = partita.getPunteggio(giocatore);
			if (punteggio != null  && punteggio > 0f){ 
				BigDecimal punteggioB = new BigDecimal(punteggio);
				punteggioB = punteggioB.setScale(0,BigDecimal.ROUND_DOWN);
				PdfPCell cellPunteggio = new PdfPCell(new Phrase(punteggioB.toString()));
				cellPunteggio.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table.addCell(cellPunteggio);
			}else{
				table.addCell(EMPTY_PHRASE);
			}
	        table.addCell(EMPTY_PHRASE);
		    table.addCell(EMPTY_PHRASE);
	        table.addCell(EMPTY_PHRASE);
		}

        table.addCell(EMPTY_PHRASE);
        table.addCell(EMPTY_PHRASE);
        table.addCell(EMPTY_PHRASE);
        PdfPCell cellTotale = new PdfPCell(new Paragraph("Totale Punti: 164"));
        cellTotale.setColspan(2);
        table.addCell(cellTotale);
		
        //PdfPCell rigaN1 = new PdfPCell (new Paragraph("\n\n\nNote: ______________________________________________________"));
        PdfPCell rigaN1 = new PdfPCell (new Paragraph("\nNote: ______________________________________________________"));        
        rigaN1.setColspan (numeroColonne);
        rigaN1.setHorizontalAlignment (Element.ALIGN_LEFT);
        rigaN1.setPadding (1.0f);
        rigaN1.setBorder(0);
        table.addCell(rigaN1);
        
        PdfPCell rigaN2 = new PdfPCell (new Paragraph("\n___________________________________________________________"));
        rigaN2.setColspan (numeroColonne);
        rigaN2.setHorizontalAlignment (Element.ALIGN_LEFT);
        rigaN2.setPadding (1.0f);
        rigaN2.setBorder(0);
        table.addCell(rigaN2);	
        
        PdfPCell rigaN3 = new PdfPCell (new Paragraph("\n___________________________________________________________\n"));
        rigaN3.setColspan (numeroColonne);
        rigaN3.setHorizontalAlignment (Element.ALIGN_LEFT);
        rigaN3.setPadding (1.0f);
        rigaN3.setBorder(0);
        table.addCell(rigaN3);
        
    	MyLogger.getLogger().finest("Aggiungo la tabella al documento");
        document.add(table);
        
    	document.add(new Paragraph("\n\n"));
        
		MyLogger.getLogger().exiting("PdfUtils", "stampaRefertoRisiko");
	}

	
	private static BaseColor getBackGroundColor(TipoTorneo tipoTorneo){
		BaseColor baseColor = null;
		switch (tipoTorneo) {
		case CAMPIONATO_NAZIONALE:
			baseColor = new BaseColor(51,51,204);
			break;
		case RADUNO_NAZIONALE:
			baseColor = new BaseColor(204,0,0);
			break;
		case MASTER:
			baseColor = new BaseColor(179,60,0);
			break;
		case OPEN:
			baseColor = new BaseColor(255,173,51);
			break;
		case INTERCLUB:
			baseColor = new BaseColor(255,255,0);
			break;
		case CAMPIONATO:
			baseColor = new BaseColor(0,204,0);
			break;
		case TORNEO_A_SQUADRE:
			baseColor = new BaseColor(51,0,255);
			break;
		case TORNEO_2VS2:
			baseColor = new BaseColor(255,179,236);
			break;
		case AMICHEVOLI:
			baseColor = new BaseColor(0,255,255);
			break;
		default:
			baseColor = new BaseColor(128, 200, 128);
			break;
		}
		return baseColor;
	}
	
	private static BaseColor getForeGroundColor(TipoTorneo tipoTorneo){
		BaseColor baseColor = null;
		switch (tipoTorneo) {
		case CAMPIONATO_NAZIONALE:
		case RADUNO_NAZIONALE:
		case MASTER:
		case CAMPIONATO:
		case TORNEO_A_SQUADRE:
		case TORNEO_2VS2:
			baseColor = new BaseColor(255,255,255);
			break;
		default:
			baseColor = new BaseColor(0, 0, 0);
			break;
		}
		return baseColor;
	}
	
	
	public static void main (String[] args){
		Partita partita = new Partita();
		partita.setNumeroGiocatori(5);
		partita.setNumeroTavolo(1);
		GiocatoreDTO giocatore1 = new GiocatoreDTO();
		giocatore1.setNome("pippo");
		giocatore1.setCognome("Baudoooooooooooooooooo");
		giocatore1.setClubProvenienza(ClubLoader.IL_GUFO);
		giocatore1.setId(1);
		partita.addGiocatore(giocatore1, 0f);
		GiocatoreDTO giocatore2 = new GiocatoreDTO();
		giocatore2.setNome("pluto");
		giocatore2.setCognome("Canisssssssssssssssssss");
		giocatore2.setClubProvenienza(ClubLoader.GRIFONE);
		giocatore2.setId(2);
		partita.addGiocatore(giocatore2, 0f);
		GiocatoreDTO giocatore3 = new GiocatoreDTO();
		giocatore3.setNome("paperino");
		giocatore3.setCognome("paolinoooooooooooooo");
		giocatore3.setClubProvenienza(ClubLoader.IL_GUISCARDO);
		giocatore3.setId(3);
		partita.addGiocatore(giocatore3, 0f);
		GiocatoreDTO giocatore4 = new GiocatoreDTO();
		giocatore4.setNome("rockerduck");
		giocatore4.setCognome("anatra");
		giocatore4.setClubProvenienza(ClubLoader.ASINELLI);
		giocatore4.setId(4);
		partita.addGiocatore(giocatore4, 0f);
		GiocatoreDTO giocatore5 = new GiocatoreDTO();
		giocatore5.setNome("commissario");
		giocatore5.setCognome("basettoni");
		giocatore5.setClubProvenienza(ClubLoader.GIMAGIOKE);
		giocatore5.setId(5);
		partita.addGiocatore(giocatore5, 0f);
		
		Document document = new Document(PageSize.A4, 50, 50, 25, 25);
		document.addAuthor("Gestione Raduno"); 
		document.addSubject("Questo è il Subject");
		document.addCreationDate();
		document.addCreator("Questo è il Creator");
		document.addHeader("name", "content");
		document.addTitle("Questo è il Title");
		
		PdfWriter writer = null;
		String fileName = "C:\\Users\\mds\\Desktop\\test.pdf";
		PdfUtils pdfUtils = new PdfUtils();
		pdfUtils.openDocument(fileName);
		try{
			pdfUtils.stampaPartiteRisiko(new Partita[]{partita}, "1");
            pdfUtils.closeDocument();
		} catch (Exception e) {
			MyLogger.getLogger().severe("Eccezione sul file "+fileName+" :"+e.getMessage());
		} 
		System.out.println("End");
	}
}
