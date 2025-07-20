package it.desimone.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class ResourceLoader {
	
	
	public static final String ROOT;
	private static final String ZIP_FILE_NAME = "resources.zip";
	private static final File ZIPFILE;
	
	static{
		ROOT = new File("").getAbsolutePath();
		MyLogger.getLogger().finer("ROOT: "+ROOT);	
		ZIPFILE = new File(ROOT+File.separator+"resources"+File.separator+ZIP_FILE_NAME);
		if (!ZIPFILE.exists()){
			MyLogger.getLogger().severe("Non è stato trovato il file "+ZIPFILE);
		}
	}
	
	private static final String HELPFILE = "ManualeGestioneTorneo.pdf";
	private static final String NOME_FILE_EXCEL = "ModuloTorneo.xls";
	private static final String NOME_FILE_IMMAGINE_RISIKO = "image004_rid.png";
	
	private static final String googleClientSecret = "client_secret.json";
	
	
	public static String getLoadingIconPath(){
		return ROOT+File.separator+"resources"+File.separator+"loadingconf.gif";
	}
	
	public static String googleClientSecretPath(){
		return ROOT+File.separator+"resources"+File.separator+"google"+File.separator+googleClientSecret;
	}
	
	public static File googleCredentials(){
		return new java.io.File(ROOT+File.separator+"resources"+File.separator+"google", ".credentials/RisiKo Data");
	}
	
	public static File tempRisikoDataCredentials(){
		return new java.io.File(System.getProperty("java.io.tmpdir")+File.separator+"googleapiprojects", ".credentials/RisiKo Data");
	}
	
	public static InputStream googleAPIAccess(){
		return FileUtils.estraiInputStreamDaZip(ZIPFILE, googleClientSecret);
	}
	
	public File estraiManuale(){
		File manuale = FileUtils.estraiDaZip(ZIPFILE, HELPFILE);
		if (manuale != null && manuale.exists()){
			manuale.deleteOnExit();
		}
		
		MyLogger.getLogger().finer("Manuale: "+manuale.getAbsolutePath());
		return manuale;
	}
	
	public File estraiManualeByFS(){
		File manuale = new File("./resources/"+HELPFILE);
		if (!manuale.exists()){
			MyLogger.getLogger().severe("Non è stato trovato il file "+manuale);
			return null;
		}
		MyLogger.getLogger().finer("Manuale: "+manuale.getAbsolutePath());
		return manuale;
	}
	
	public File estraiManualeByCL() throws IOException{
		
		URL manualeURL = this.getClass().getResource("/resources/" +HELPFILE);
		MyLogger.getLogger().finer("URL: "+manualeURL);
				
		File fileHelp = new File(HELPFILE);
		fileHelp.deleteOnExit();

		MyLogger.getLogger().finer("fileHelp: "+fileHelp);
		
		InputStream is = (InputStream) manualeURL.openStream();
		//ClassLoader cl = this.getClass().getClassLoader();
		//InputStream is = cl.getResourceAsStream("/resources/" +HELPFILE);
		byte [] buff = new byte [1024];
		OutputStream out = new FileOutputStream(fileHelp);
		int n;
		while( (n = is.read(buff, 0, buff.length))!= -1){
			out.write(buff, 0 , n);
		}
		is.close();
		out.close();
		
		return fileHelp;
	}
	
	public File estraiManuale_old() throws IOException{
		File fileHelp = new File(HELPFILE);
		
		fileHelp.deleteOnExit();
		InputStream is = this.getClass().getResourceAsStream("/resources/" +HELPFILE);
		byte [] buff = new byte [1024];
		OutputStream out = new FileOutputStream(fileHelp);
		int n;
		while( (n = is.read(buff, 0, buff.length))!= -1){
			out.write(buff, 0 , n);
		}
		is.close();
		out.close();
		
		return fileHelp;
	}
	
	public void estraiXLSVergine(File xls) throws IOException{
		
		File xlsOriginario = FileUtils.estraiDaZip(ZIPFILE, NOME_FILE_EXCEL);
		
		if (xlsOriginario != null && xlsOriginario.exists()){
			xlsOriginario.deleteOnExit();
			InputStream is = new FileInputStream(xlsOriginario);
			byte [] buff = new byte [1024];
			OutputStream out = new FileOutputStream(xls);
			int n;
			while( (n = is.read(buff, 0, buff.length))!= -1){
				out.write(buff, 0 , n);
			}
			is.close();
			out.close();
		}else{
			MyLogger.getLogger().severe("Non ï¿½ stato trovato il file "+xlsOriginario);
		}
	}
	
	public void estraiXLSVergineByFS(File xls) throws IOException{
		
		File xlsOriginario = new File("./resources/"+NOME_FILE_EXCEL);
		
		if (xlsOriginario.exists()){
			InputStream is = new FileInputStream(xlsOriginario);
			byte [] buff = new byte [1024];
			OutputStream out = new FileOutputStream(xls);
			int n;
			while( (n = is.read(buff, 0, buff.length))!= -1){
				out.write(buff, 0 , n);
			}
			is.close();
			out.close();
		}else{
			MyLogger.getLogger().severe("Non ï¿½ stato trovato il file "+xlsOriginario);
		}
	}
	
	public void estraiXLSVergineByCL(File xls) throws IOException{
		
		URL xlsURL = this.getClass().getResource("/resources/" +NOME_FILE_EXCEL);
		MyLogger.getLogger().finer("URL: "+xlsURL);
		
		InputStream is = (InputStream) xlsURL.openStream();
		byte [] buff = new byte [1024];
		OutputStream out = new FileOutputStream(xls);
		int n;
		while( (n = is.read(buff, 0, buff.length))!= -1){
			out.write(buff, 0 , n);
		}
		is.close();
		out.close();
	}
	
	public void estraiXLSVergine_old(File xls) throws IOException{
		
		InputStream is = this.getClass().getResourceAsStream("/resources/" +NOME_FILE_EXCEL);
		byte [] buff = new byte [1024];
		OutputStream out = new FileOutputStream(xls);
		int n;
		while( (n = is.read(buff, 0, buff.length))!= -1){
			out.write(buff, 0 , n);
		}
		is.close();
		out.close();
	}
	
	public byte[] getImmagineRisiko(){
		byte[] result = null;
		File immagineRisiko = FileUtils.estraiDaZip(ZIPFILE, NOME_FILE_IMMAGINE_RISIKO);
		if (immagineRisiko != null && immagineRisiko.exists()){
			immagineRisiko.deleteOnExit();
			InputStream is;
			try {
				is = new FileInputStream(immagineRisiko);
				byte [] buff = new byte [1024];
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				int n;
				while( (n = is.read(buff, 0, buff.length))!= -1){
					out.write(buff, 0 , n);
				}
				is.close();
				out.close();
				result = out.toByteArray();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			MyLogger.getLogger().finer("file immagine: "+(result==null?"0":result.length+" byte"));
		}else{
			MyLogger.getLogger().severe("Non è stato trovato il file "+immagineRisiko);
		}
		return result;
	}
	
	
	public byte[] getImmagineLogo(String clubName){
		byte[] result = null;
		String pathImage = "loghi50px_vert/"+clubName+".png";
		File immagineRisiko = FileUtils.estraiDaZip(ZIPFILE, pathImage);
		if (immagineRisiko != null && immagineRisiko.exists()){
			immagineRisiko.deleteOnExit();
			InputStream is;
			try {
				is = new FileInputStream(immagineRisiko);
				byte [] buff = new byte [1024];
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				int n;
				while( (n = is.read(buff, 0, buff.length))!= -1){
					out.write(buff, 0 , n);
				}
				is.close();
				out.close();
				result = out.toByteArray();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			MyLogger.getLogger().finer("file immagine: "+(result==null?"0":result.length+" byte"));
		}else{
			MyLogger.getLogger().severe("Non è stato trovato il file "+pathImage);
		}
		return result;
	}
	
	public byte[] getImmagineRisikoByFS(){
		byte[] result = null;
		File immagineRisiko = new File("./resources/"+NOME_FILE_IMMAGINE_RISIKO);
		immagineRisiko = new File(ROOT+File.separator+"resources"+File.separator+NOME_FILE_IMMAGINE_RISIKO);
		//immagineRisiko = new File("C:\\Documents and Settings\\Vjger\\Desktop\\Risiko!\\Sorteggio Tavoli New\\resources\\"+NOME_FILE_IMMAGINE_RISIKO);
		if (immagineRisiko.exists()){
			InputStream is;
			try {
				is = new FileInputStream(immagineRisiko);
				byte [] buff = new byte [1024];
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				int n;
				while( (n = is.read(buff, 0, buff.length))!= -1){
					out.write(buff, 0 , n);
				}
				is.close();
				out.close();
				result = out.toByteArray();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			MyLogger.getLogger().finer("file immagine: "+(result==null?"0":result.length+" byte"));
		}else{
			MyLogger.getLogger().severe("Non ï¿½ stato trovato il file "+immagineRisiko);
		}
		return result;
	}
	
	public String getImmagineRisikoByFileName(){
		String result = null;
		File immagineRisiko = new File("./resources/"+NOME_FILE_IMMAGINE_RISIKO);
		if (immagineRisiko.exists()){
			result = immagineRisiko.getPath();
			MyLogger.getLogger().finer("file immagine: "+result);
		}else{
			MyLogger.getLogger().severe("Non ï¿½ stato trovato il file "+immagineRisiko);
		}
		return result;
	}
	
	public URL getImmagineRisikoUrl(){
		URL result = null;
		File immagineRisiko = new File("./resources/"+NOME_FILE_IMMAGINE_RISIKO);
		if (immagineRisiko.exists()){
			URI uri = immagineRisiko.toURI();
			try {
				result = uri.toURL();
				MyLogger.getLogger().finer("URL immagine: "+result);
			} catch (MalformedURLException e) {
				MyLogger.getLogger().severe("Eccezione nel trasformare in url la URI "+uri+" del file "+immagineRisiko);
			}
		}else{
			MyLogger.getLogger().severe("Non ï¿½ stato trovato il file "+immagineRisiko);
		}
		return result;
	}
	
	public URL getImmagineRisikoByCL(){
		return this.getClass().getResource("/resources/"+NOME_FILE_IMMAGINE_RISIKO);
	}
	
}
