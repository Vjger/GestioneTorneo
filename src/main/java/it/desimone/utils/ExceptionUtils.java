package it.desimone.utils;

public class ExceptionUtils {
	
	public static String parseException(Throwable e){
		String response;
		if (e.getMessage() != null){
			response = e.getMessage();
			if (e instanceof MyException && ((MyException)e).getCode() != null){
				//response = response + "\n" + ((MyException)e).getCode(); 
				response = response + "\n" + ((MyException)e).getCode() + "\n" +getStackTrace(e); //per ora
			}else if (!(e instanceof MyException)){
				response += "\n"+e.getClass().getName() + "\n" +getStackTrace(e);
			}
		}else{
			response = e.getClass().getName() + "\n" +getStackTrace(e);
		}
		return response;
	}

	public static String getStackTrace(Throwable e){
		StringBuffer sb = new StringBuffer();
		StackTraceElement []ste = e.getStackTrace();
		for (int i = 0; i < ste.length; i++){
			sb.append(ste[i].toString());
			sb.append("\n");
		}
		return sb.toString();
	}
}
