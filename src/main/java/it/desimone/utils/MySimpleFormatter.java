package it.desimone.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class MySimpleFormatter extends Formatter {

	private static final String format = "%5$s%n";
    private final Date dat = new Date();
	
	@Override
	public String format(LogRecord record) {
	       dat.setTime(record.getMillis());
	        String source;
	        if (record.getSourceClassName() != null) {
	            source = record.getSourceClassName();
	            if (record.getSourceMethodName() != null) {
	               source += " " + record.getSourceMethodName();
	            }
	        } else {
	            source = record.getLoggerName();
	        }
	        String message = formatMessage(record);
	        String throwable = "";
	        if (record.getThrown() != null) {
	            StringWriter sw = new StringWriter();
	            PrintWriter pw = new PrintWriter(sw);
	            pw.println();
	            record.getThrown().printStackTrace(pw);
	            pw.close();
	            throwable = sw.toString();
	        }
	        return String.format(format,
	                             dat,
	                             source,
	                             record.getLoggerName(),
	                             record.getLevel().getLocalizedName(),
	                             message,
	                             throwable);
	}

}
