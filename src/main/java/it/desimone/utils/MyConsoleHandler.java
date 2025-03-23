package it.desimone.utils;

import java.io.OutputStream;
import java.util.logging.ConsoleHandler;

public class MyConsoleHandler extends ConsoleHandler {
	
	public void setOutputStream(OutputStream out) throws SecurityException{
		super.setOutputStream(out);
	}

}
