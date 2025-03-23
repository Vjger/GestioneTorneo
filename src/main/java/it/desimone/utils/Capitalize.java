package it.desimone.utils;

public class Capitalize {

    public static String capitalizeSingleString(String s) {
        if (s.length() == 0) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    public static String capitalize(String s) {
    	if (s == null) return null;
    	String result = "";
        String[] words = s.trim().split("\\s");
        for (String w : words) {
            result += capitalizeSingleString(w) + " ";
        }
        return result.trim();
    }
    
    
    public static void main (String[] args){
    	System.out.println(capitalize("marco"));
    }

}
