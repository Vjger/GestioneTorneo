package it.desimone.utils;

import it.desimone.risiko.torneo.dto.Partita;

public class ArrayUtils {

	public static String fromPartiteToString(Partita[] partite){
		StringBuilder buffer = new StringBuilder(); 
		if (partite != null){
			for (Partita partita: partite){
				if (partita != null){
					buffer.append(partita.toString()+"\n");
				}
			}
		}
		
		return buffer.toString();
	}
	
	
	
	public static Partita[] clonaPartite(Partita[] partite){
		Partita[] clone = null;
		if (partite != null){
			clone = new Partita[partite.length];
			for (int index = 0; index < partite.length; index++){
				clone[index] = (Partita) partite[index].clone();
			}
		}
		return clone;
	}
	
    public static Object[] concatena(Object[] a, Object[] b){
    	if (a != null && b == null) return a;
    	if (b != null && a == null) return b;
    	if (a == null && b == null) return null;
    	
        int length = a.length + b.length;
        Object[] result = new Object[length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
    
    public static Partita[] concatenaPartite(Partita[] a, Partita[] b){
    	Partita[] result = null;
    	Object[] arrayO = concatena(a, b);
    	if (arrayO != null){
    		result = new Partita[arrayO.length];
    		for (int index = 0; index < arrayO.length; index++){
    			result[index] = (Partita)arrayO[index];
    		}
    	}
        return result;
    }


}
