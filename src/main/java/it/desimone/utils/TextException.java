/*
 * Created on 8-ott-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package it.desimone.utils;


import javax.swing.JTextArea;

/**
 * @author marco
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TextException extends JTextArea {
	
	/**
	 * 
	 */
	public TextException(Throwable e) {
		super();
		String text;
		text = ExceptionUtils.parseException(e);
		setText(text);
		setEditable(false);
	}
	
	public TextException(String message) {
		super();
		setText(message);
		setEditable(false);
	}
	
}
