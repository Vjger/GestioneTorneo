package it.desimone.risiko.torneo.dto;


public class ProvinciaDTO {

	private String codice;
	private Integer idRegione;
	private String descrizione;
	
	public ProvinciaDTO(){}
	
	public ProvinciaDTO(String codice, Integer idRegione, String descrizione){
		this.codice = codice;
		this.idRegione = idRegione;
		this.descrizione = descrizione;
	}

	public ProvinciaDTO(String record){
		String[] fields = record.split(this.getClass().getName());
		codice = fields[0];
		idRegione = Integer.getInteger(fields[1]);
		descrizione = fields[2];
	}
	
	public String getCodice() {
		return codice;
	}

	public void setCodice(String codice) {
		this.codice = codice;
	}

	public Integer getIdRegione() {
		return idRegione;
	}

	public void setIdRegione(Integer idRegione) {
		this.idRegione = idRegione;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public void setDescrizione(String descrizione) {
		this.descrizione = descrizione;
	}
	
	public boolean equals(Object o){
		ProvinciaDTO provinciaDTO = (ProvinciaDTO) o;
		return this.getCodice().equals(provinciaDTO.getCodice());
	}
	
	public int hashCode(){
		return this.getCodice().hashCode();
	}
	
	public String toString(){
		return codice+this.getClass().getName()+idRegione+this.getClass().getName()+descrizione;
	}
	
}
