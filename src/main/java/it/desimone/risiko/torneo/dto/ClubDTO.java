package it.desimone.risiko.torneo.dto;

public class ClubDTO implements Comparable<ClubDTO>{

	private String codProvincia;
	private String denominazione;
	
	public ClubDTO(){}
	
	public ClubDTO(String denominazione){
		this.denominazione = denominazione;
	}
	
	public ClubDTO(String codProvincia, String denominazione){
		this.codProvincia = codProvincia;
		this.denominazione = denominazione;
	}
	
	public String getDenominazione() {
		return denominazione;
	}

	public void setDenominazione(String denominazione) {
		this.denominazione = denominazione;
	}

	public String getCodProvincia() {
		return codProvincia;
	}

	public void setCodProvincia(String codProvincia) {
		this.codProvincia = codProvincia;
	}
	
	public String toString(){
		return denominazione;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((denominazione == null) ? 0 : denominazione.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClubDTO other = (ClubDTO) obj;
		if (denominazione == null) {
			if (other.denominazione != null)
				return false;
		} else if (!denominazione.equalsIgnoreCase(other.denominazione))
			return false;
		return true;
	}

	@Override
	public int compareTo(ClubDTO o) {
		// TODO Auto-generated method stub
		if (o != null && denominazione != null){
			return denominazione.compareTo(o.getDenominazione());
		}else if (o != null){
			return -1;
		}else{
			return 1;
		}
	}
	

}
