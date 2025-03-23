package it.desimone.risiko.torneo.utils;


import it.desimone.risiko.torneo.dto.RegioneDTO;

import java.lang.reflect.Field;
import java.util.List;

public class RegioniLoader {

	private final static java.util.List<RegioneDTO> regioni = new java.util.ArrayList<RegioneDTO>();
	
	public static final RegioneDTO PIEMONTE  = new RegioneDTO(1,"PIEMONTE");
	public static final RegioneDTO VALDAOSTA = new RegioneDTO(2,"VAL D'AOSTA");
	public static final RegioneDTO LOMBARDIA = new RegioneDTO(3,"LOMBARDIA");
	public static final RegioneDTO TRENTINO  = new RegioneDTO(4,"TRENTINO ALTO-ADIGE");
	public static final RegioneDTO VENETO    = new RegioneDTO(5,"VENETO");
	public static final RegioneDTO FRIULI    = new RegioneDTO(6,"FRIULI-VENEZIA GIULIA");
	public static final RegioneDTO LIGURIA   = new RegioneDTO(7,"LIGURIA");
	public static final RegioneDTO EMILIA    = new RegioneDTO(8,"EMILIA-ROMAGNA");
	public static final RegioneDTO TOSCANA   = new RegioneDTO(9,"TOSCANA");
	public static final RegioneDTO UMBRIA    = new RegioneDTO(10,"UMBRIA");
	public static final RegioneDTO MARCHE    = new RegioneDTO(11,"MARCHE");
	public static final RegioneDTO LAZIO     = new RegioneDTO(12,"LAZIO");
	public static final RegioneDTO ABRUZZO   = new RegioneDTO(13,"ABRUZZO");
	public static final RegioneDTO MOLISE    = new RegioneDTO(14,"MOLISE");
	public static final RegioneDTO CAMPANIA  = new RegioneDTO(15,"CAMPANIA");
	public static final RegioneDTO PUGLIA    = new RegioneDTO(16,"PUGLIA");
	public static final RegioneDTO BASILICATA = new RegioneDTO(17,"BASILICATA");
	public static final RegioneDTO CALABRIA  = new RegioneDTO(18,"CALABRIA");
	public static final RegioneDTO SICILIA   = new RegioneDTO(19,"SICILIA");
	public static final RegioneDTO SARDEGNA  = new RegioneDTO(20,"SARDEGNA");
	
	public static final RegioneDTO FASCIA1  = new RegioneDTO(21,"FASCIA 1");
	public static final RegioneDTO FASCIA2  = new RegioneDTO(22,"FASCIA 2");
	public static final RegioneDTO FASCIA3  = new RegioneDTO(23,"FASCIA 3");
	public static final RegioneDTO FASCIA4  = new RegioneDTO(24,"FASCIA 4");
	
	static{
		regioni.add(PIEMONTE);	
		regioni.add(VALDAOSTA);	
		regioni.add(LOMBARDIA);	
		regioni.add(TRENTINO);	
		regioni.add(VENETO);	
		regioni.add(FRIULI);	
		regioni.add(LIGURIA);	
		regioni.add(EMILIA);	
		regioni.add(TOSCANA);	
		regioni.add(UMBRIA);	
		regioni.add(MARCHE);	
		regioni.add(LAZIO);	
		regioni.add(ABRUZZO);	
		regioni.add(MOLISE);	
		regioni.add(CAMPANIA);	
		regioni.add(PUGLIA);	
		regioni.add(BASILICATA);	
		regioni.add(CALABRIA);	
		regioni.add(SICILIA);	
		regioni.add(SARDEGNA);	
	}
	
	public static List<RegioneDTO> getRegioni(){
		return regioni;
	}
	
	public static void main (String[] args) throws IllegalArgumentException, IllegalAccessException{
		RegioniLoader obj = new RegioniLoader();
		Field[] fields = obj.getClass().getFields();
		for (Field field: fields){
			System.out.println(field.get(obj));
		}
	}
}
