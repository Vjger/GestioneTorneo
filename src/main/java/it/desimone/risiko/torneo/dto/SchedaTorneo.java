package it.desimone.risiko.torneo.dto;

import java.util.Date;
import java.util.List;

public class SchedaTorneo {

	private String sedeTorneo;
	private String organizzatore;
	private String nomeTorneo;
	private TipoTorneo tipoTorneo;
	private int numeroTurni;
	private String note;
	
	private List<Date> dataTurni;
	
	public enum TipoTorneo{
		  CAMPIONATO_NAZIONALE("Campionato Nazionale", "CNI")
		, RADUNO_NAZIONALE("Raduno Nazionale", "RDN")
		, MASTER("Torneo Master", "MST")
		, OPEN("Torneo Open", "OPN")
		, INTERCLUB("Torneo Interclub", "ITC")		
		, CAMPIONATO("Campionato Periodico", "CPP")
		, TORNEO_A_SQUADRE("Torneo a Squadre", "SQD")
		, TORNEO_2VS2("Torneo 2VS2", "2V2")
		, TORNEO_A_INVITI("Torneo a Inviti", "IVT")
		, AMICHEVOLI("Amichevoli", "AMC");
		TipoTorneo(String tipoTorneo, String acronimo){
			this.tipoTorneo = tipoTorneo;
			this.acronimo = acronimo;
		}
		String tipoTorneo;
		String acronimo;
		public String getTipoTorneo() {
			return tipoTorneo;
		}
		public String getAcronimo() {
			return acronimo;
		}

		public static TipoTorneo parseTipoTorneo(String tipologiaTorneo){
			TipoTorneo result = null;
			if (tipologiaTorneo != null){
				TipoTorneo[] tipiTornei = TipoTorneo.values();
				for (TipoTorneo torneo: tipiTornei){
					if (torneo.getTipoTorneo().equalsIgnoreCase(tipologiaTorneo.trim())){
						result = torneo;
						break;
					}
				}
			}
			return result;
		}
		
		public static boolean prevedeClassifica(TipoTorneo tipoTorneo){
			return tipoTorneo != TORNEO_A_SQUADRE && tipoTorneo != TORNEO_2VS2 && tipoTorneo != AMICHEVOLI;
		}
	}

	public String getSedeTorneo() {
		return sedeTorneo;
	}

	public void setSedeTorneo(String sedeTorneo) {
		this.sedeTorneo = sedeTorneo;
	}

	public String getOrganizzatore() {
		return organizzatore;
	}

	public void setOrganizzatore(String organizzatore) {
		this.organizzatore = organizzatore;
	}

	public String getNomeTorneo() {
		return nomeTorneo;
	}

	public void setNomeTorneo(String nomeTorneo) {
		this.nomeTorneo = nomeTorneo;
	}

	public TipoTorneo getTipoTorneo() {
		return tipoTorneo;
	}

	public void setTipoTorneo(TipoTorneo tipoTorneo) {
		this.tipoTorneo = tipoTorneo;
	}

	public int getNumeroTurni() {
		return numeroTurni;
	}

	public void setNumeroTurni(int numeroTurni) {
		this.numeroTurni = numeroTurni;
	}

	public List<Date> getDataTurni() {
		return dataTurni;
	}

	public void setDataTurni(List<Date> dataTurni) {
		this.dataTurni = dataTurni;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	@Override
	public String toString() {
		return "SchedaTorneo [sedeTorneo=" + sedeTorneo + ", organizzatore="
				+ organizzatore + ", nomeTorneo=" + nomeTorneo
				+ ", tipoTorneo=" + tipoTorneo + ", numeroTurni=" + numeroTurni
				+ ", dataTurni=" + dataTurni + "]";
	}
	
	
}
