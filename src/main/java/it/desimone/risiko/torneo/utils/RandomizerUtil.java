package it.desimone.risiko.torneo.utils;

import java.util.Random;

public class RandomizerUtil {

	public static int getRandomLessOneOrPlusOne() {
		Random random = new Random();
		int ran = random.nextInt(2);
		
		if (ran == 0) {
			return -1;
		}else {
			return  1;
		}
	}
	
	
	public static void main (String[] s){
		int countp = 0;
		int countl = 0;
		for (int i = 1; i <=10000; i++) {
			int x = getRandomLessOneOrPlusOne();
			if (x == 1) countp++;
			if (x == -1) countl++;
		}
		System.out.println(("+1: "+countp+" -: "+countl));
	}
}
