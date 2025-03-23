package it.desimone.utils;

import java.util.Random;

import javax.swing.JOptionPane;

public class IDRandomizer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String numeroGiocatori = JOptionPane.showInputDialog("Numero giocatori");
		if (numeroGiocatori != null){
			int numero = Integer.parseInt(numeroGiocatori);
			Random random = new Random();
			int casuale = random.nextInt(numero)+1;
			JOptionPane.showMessageDialog(null, "Il numero estratto è "+casuale);
		}
	}

}
