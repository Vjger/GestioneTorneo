package it.desimone.risiko.torneo.utils;

import java.util.Vector;

public enum TipoTorneo {
	 NazionaleRisiKo
	,RadunoNazionale
	,RadunoNazionale_con_quarti
	,SantEufemia
	,MasterRisiko
	,MasterRisiko2015
	,OpenMaster
	,Open
	,TorneoGufo
	,CampionatoGufo
	,ColoniDiCatan
	,Dominion
	,StoneAge
	,BGL
	,BGL_SVIZZERA
	,_1vs1_
	,_1vs1_SVIZZERA
	,TorneoASquadre;
	
	public static Vector<TipoTorneo> getTipiAbilitati(){
		Vector<TipoTorneo> result = new Vector<TipoTorneo>();
		result.add(NazionaleRisiKo);
		result.add(RadunoNazionale);
		result.add(SantEufemia);
		//result.add(RadunoNazionale_con_quarti);
		result.add(MasterRisiko);
		//result.add(MasterRisiko2015);
		result.add(OpenMaster);
		result.add(Open);
		result.add(TorneoGufo);
		result.add(CampionatoGufo);
		result.add(BGL);
		result.add(BGL_SVIZZERA);
		result.add(_1vs1_);
		result.add(_1vs1_SVIZZERA);
		result.add(TorneoASquadre);
		return result;
	}
	
	public static String getDescrizione(TipoTorneo tipoTorneo){
		String descrizione = null;
		switch (tipoTorneo) {
//		case RadunoNazionale:
//			descrizione = "<HTML>TIPO DI TAVOLI: priorità a quelli da 4 e altrimenti da 5.<BR/>"
//					+ "CRITERI SORTEGGIO: Per il 1° turno "+PrioritaSorteggio.IMPEDITO_STESSO_CLUB+" "+PrioritaSorteggio.IMPEDITA_STESSA_REGIONE+ " Per il 2° Turno: "+PrioritaSorteggio.VINCITORI_SEPARATI+" "+PrioritaSorteggio.IMPEDITO_STESSO_CLUB+" "+PrioritaSorteggio.MINIMIZZAZIONE_SCONTRI_DIRETTI+"<BR/>"
//					+ "CLASSIFICA: PUNTI TAVOLO + BONUS DI 50 PUNTI SE VINCITORE DA SOLO ALTRIMENTI DI 25 SE VITTORIA CONDIVISA</HTML>";
//			break;

		default:
			break;
		}
		
		
		return descrizione;
	}
}
