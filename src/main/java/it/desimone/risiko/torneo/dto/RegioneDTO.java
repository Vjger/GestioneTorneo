package it.desimone.risiko.torneo.dto;


public class RegioneDTO {

	private Integer id;
	private String descrizione;
	private Integer popolazione;
	public RegioneDTO(){}
	
	public RegioneDTO(Integer id, String descrizione){
		this.id=id;
		this.descrizione=descrizione;
	}
	
	public RegioneDTO(String record){
		if (record != null){
			id = record.hashCode();
			descrizione = record;
		}
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getDescrizione() {
		return descrizione;
	}
	public void setDescrizione(String descrizione) {
		this.descrizione = descrizione;
	}
	
	public String toString(){
		//return id+this.getClass().getName()+descrizione;
		return descrizione;
	}
	
	public boolean equals(Object o){
		boolean result = false;
		if (o != null){
			RegioneDTO regione = (RegioneDTO) o;
			result = regione.getId().equals(this.getId());
		}
		return result;
	}

	public Integer getPopolazione() {
		return popolazione;
	}

	public void setPopolazione(Integer popolazione) {
		this.popolazione = popolazione;
	}
	
}
