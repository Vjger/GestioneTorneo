package it.desimone.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Logger {
	
//	private static StringBuilder _log = new StringBuilder();
//	private static DateFormat format = new SimpleDateFormat("HH:mm:ss");
//	public static void debug(String log){
//		_log.append(format.format(Calendar.getInstance().getTime())+" "+log+"\n");
//	}
//	
//	public static String getLog(){
//		return _log.toString();
//	}
	
	private static List<String> _log = new ArrayList<String>();
	private static DateFormat format = new SimpleDateFormat("HH:mm:ss");
	public static void debug(String log){
		_log.add(format.format(Calendar.getInstance().getTime())+" "+log);
	}
	
	public static List<String> getLog(){
		return _log;
	}
}
