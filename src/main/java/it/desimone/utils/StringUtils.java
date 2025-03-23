package it.desimone.utils;

public class StringUtils {

	public static boolean isNullOrEmpty(String s){
		return s == null || s.trim().length() == 0;
	}
	
}
