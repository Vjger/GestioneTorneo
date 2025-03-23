package it.desimone.utils;


public class MyException extends RuntimeException {

	private String code;

	/**
	 * Costruttore con codice e messaggio
	 *
	 * @param code il codice univoco da eventualmente trascodificare (Es. codice per doppia insert)
	 * @param message il messaggio
	 */
	
	public MyException(Exception e){
		super(e.getMessage());
		setStackTrace(e.getStackTrace());
	}
	
	public MyException(String message){
		super(message);
	}
	
	public MyException(Exception e, String message) {
		this(e.getMessage()+"\n"+ message);
		setStackTrace(e.getStackTrace());
	}
	
	public MyException(Exception e, String message, String code) {
		this(e,message);
		this.code = code;
	}
	
	public MyException(String message, String code) {
		super(message);
		this.code = code;
	}


	/**
	 * Restituisce il codice univoco
	 * @return il codice univoco
	 */
	public String getCode() {
		return code;
	}

}
