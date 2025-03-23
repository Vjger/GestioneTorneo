package it.desimone.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtils {

	public static final DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
	
	public static Date normalizeDate(Date date){
		if (date != null){
			Calendar cal = GregorianCalendar.getInstance();
			cal.setTime(date);
			cal.set(Calendar.HOUR, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			date = cal.getTime();
		}
		return date;
	}
	
	public static void formatDate(Date date){
		if (date != null){
			df.format(date);
		}
	}
	
	public static Date parseItalianDate(String data) {
		Date result = null;
		try {
			if (!StringUtils.isNullOrEmpty(data)) {
				result = df.parse(data);
			}
		}catch(Exception e) {
			MyLogger.getLogger().severe(e.getMessage());
		}
		return result;
	}
}
