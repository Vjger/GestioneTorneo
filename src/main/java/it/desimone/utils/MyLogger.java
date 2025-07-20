package it.desimone.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

public class MyLogger {
	
	private static java.util.logging.Logger _log = java.util.logging.Logger.getAnonymousLogger();
	private static ConsoleHandler consoleHandler = null;
	private static ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	private static StreamHandler streamHandler = null;
	private static FileHandler fileHandler = null;
	private static DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
	static{
		
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		InputStream propertiesStream = classLoader.getResourceAsStream("loggerConfiguration.properties");
		String logConfiguratorFileName = null;
		try {
			LogManager logManager = LogManager.getLogManager();
			logManager.readConfiguration(propertiesStream);
		} catch (SecurityException e) {
			MyLogger.getLogger().severe("SecurityException nell'accesso al file "+logConfiguratorFileName+": "+e.getMessage());
			throw new RuntimeException(e);
		} catch (IOException e) {
			MyLogger.getLogger().severe("IOException nell'accesso al file "+logConfiguratorFileName+": "+e.getMessage());
			throw new RuntimeException(e);
		}
		
		_log.setLevel(Level.FINEST);
		_log.setUseParentHandlers(false);
		consoleHandler = new MyConsoleHandler();
		consoleHandler.setLevel(Level.OFF);
		consoleHandler.setFormatter(new MySimpleFormatter());
		streamHandler = new StreamHandler(byteArrayOutputStream, new SimpleFormatter());
		streamHandler.setLevel(Level.INFO);
		try{
			File dirLog = new File(ResourceLoader.ROOT+File.separator+"log");
			if (!dirLog.exists()){dirLog.mkdir();}
			String logFileName = "GestioneRaduno_"+dateFormat.format(Calendar.getInstance().getTime())+".log";
			fileHandler = new FileHandler(dirLog+File.separator+logFileName);
			fileHandler.setFormatter(new SimpleFormatter());
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
		fileHandler.setLevel(Level.FINEST);
		_log.addHandler(consoleHandler);
		_log.addHandler(streamHandler);
		_log.addHandler(fileHandler);
	}
	
	
	public static void closeFileHandler(){
		fileHandler.close();
	}
	
	public static void setConsoleLogLevel(Level level){
		if (consoleHandler != null) consoleHandler.setLevel(level);
	}
	
	public static void setConsoleOutputStream(OutputStream ps){
		if (consoleHandler != null) ((MyConsoleHandler)consoleHandler).setOutputStream(ps);
	}
	
	public static void setStreamLogLevel(Level level){
		if (streamHandler != null)  streamHandler.setLevel(level);
	}
	
	public static void setFileLogLevel(Level level){
		if (fileHandler != null) fileHandler.setLevel(level);
	}
	
	public static void setLogLevel(Level level){
		_log.setLevel(level);
		if (consoleHandler != null) consoleHandler.setLevel(level);
		if (streamHandler != null)  streamHandler.setLevel(level);
		if (fileHandler != null) fileHandler.setLevel(level);
	}
	
	public static String getLogStream(){
		streamHandler.flush();
		return byteArrayOutputStream.toString();
	}
	
	public static List<String> getListLogStream(){
		streamHandler.flush();
		String logString = byteArrayOutputStream.toString();
		String[] stringRows = logString.split("\n");
		List result = Arrays.asList(stringRows);
		//TEST TEST TEST per resettare lo stream excel
		byteArrayOutputStream.reset();
		return result;
	}
	
	public static java.util.logging.Logger getLogger(){
		return _log;
	}
	
	public static void main (String[] args){
		DateFormat df =  new SimpleDateFormat("dd_MM_yyyy_HH:mm:ss");
		System.out.println(df.format(Calendar.getInstance().getTime()));
	}
	
}
