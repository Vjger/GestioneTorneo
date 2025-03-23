package it.desimone.risiko.torneo.dto;

import it.desimone.utils.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class GiocatoreDTO implements Comparable{
	
	public static final GiocatoreDTO FITTIZIO;
	public static final GiocatoreDTO ANONIMO;

	static{
		Calendar cal = GregorianCalendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		cal.set(Calendar.YEAR, 2019);
		Date birthGhostDate = cal.getTime();
		birthGhostDate = DateUtils.normalizeDate(birthGhostDate);
		
		FITTIZIO = new GiocatoreDTO();
		FITTIZIO.setId(-1);
		FITTIZIO.setNome("The");
		FITTIZIO.setCognome("Ghost");
		FITTIZIO.setDataDiNascita(birthGhostDate);
		
		//FITTIZIO.setEmail("TheGhost@theghost.it");
		
		ANONIMO = new GiocatoreDTO();
		ANONIMO.setNome("Anonimo");
		ANONIMO.setCognome("Anonimo");
		//ANONIMO.setEmail("anonimo@anonimo.it");
	}
	
	private static final long serialVersionUID = 1;
	
	private static final String SEPARATOR="\t";
	
	private Integer id = 0;
	
	private String nome;
	private String cognome;
	private String email;
	private Date dataDiNascita;
	private Integer idNazionale;
	private String nick;
	private RegioneDTO regioneProvenienza;
	private ClubDTO clubProvenienza;
	private Boolean presenteTorneo;
	
	private Boolean fissoAlTipoDiTavolo = Boolean.FALSE;
	
	public GiocatoreDTO(){}
	
	public String toString(){
		StringBuilder buffer = new StringBuilder();
		buffer.append(nome);
		buffer.append(" "+cognome);
		buffer.append(" [");
		if (clubProvenienza != null){
			buffer.append(clubProvenienza);
		}
		buffer.append("]");
//		if (nick != null){
//			buffer.append(" ("+nick+")");
//		}
//		if (clubProvenienza != null){
//			buffer.append(" del club "+clubProvenienza);			
//		}

		return buffer.toString();
	}
	
	public boolean equals(Object o){
		GiocatoreDTO giocatore = (GiocatoreDTO) o;
		return giocatore.getId().equals(this.getId());
	}
	
	public boolean uguale(GiocatoreDTO giocatore){
		boolean stessoNome = (this.nome == null && giocatore.getNome() == null) || (this.nome != null && giocatore.getNome() != null && this.nome.trim().equalsIgnoreCase(giocatore.getNome().trim()));
		boolean stessoCognome = (this.cognome == null && giocatore.getCognome() == null) || (this.cognome != null && giocatore.getCognome() != null && this.cognome.trim().equalsIgnoreCase(giocatore.getCognome().trim()));
		//boolean stessaMail = (this.email == null && giocatore.getEmail() == null) || (this.email != null && giocatore.getEmail() != null && this.email.trim().equalsIgnoreCase(giocatore.getEmail().trim()));
		boolean stessaDataDiNascita = (this.dataDiNascita == null && giocatore.getDataDiNascita() == null) || (this.dataDiNascita != null && giocatore.getDataDiNascita() != null && this.dataDiNascita.equals(giocatore.getDataDiNascita()));
		boolean stessoIdNazionale = (this.idNazionale == null && giocatore.getIdNazionale() == null) || (this.idNazionale != null && giocatore.getIdNazionale() != null && this.idNazionale.equals(giocatore.getIdNazionale()));
		return stessoNome && stessoCognome && stessaDataDiNascita; //stessaMail;
	}

	public boolean isAnonimo(){
		boolean result = nome != null && nome.equalsIgnoreCase(ANONIMO.getNome()) && cognome != null && cognome.equalsIgnoreCase(ANONIMO.getCognome());
		return result;
	}
	
	public int hashCode(){
		return id;
	}
	
	public String toExcel(){
		StringBuilder buffer = new StringBuilder();
		buffer.append(nome+SEPARATOR);
		buffer.append(cognome+SEPARATOR);
		buffer.append(email+SEPARATOR);
		buffer.append(nick!=null?nick:""+SEPARATOR);
		buffer.append(regioneProvenienza+SEPARATOR);
		buffer.append(clubProvenienza!=null?clubProvenienza:""+SEPARATOR);
		buffer.append(presenteTorneo+SEPARATOR);
		return buffer.toString();
	}

	public String getNome() {
		return nome;
	}


	public void setNome(String nome) {
		this.nome = nome;
	}


	public String getCognome() {
		return cognome;
	}


	public void setCognome(String cognome) {
		this.cognome = cognome;
	}

//	public String getEmail() {
//		return email;
//	}
//
//	public void setEmail(String email) {
//		this.email = email;
//	}

	public String getNick() {
		return nick;
	}


	public void setNick(String nick) {
		this.nick = nick;
	}

	public RegioneDTO getRegioneProvenienza() {
		return regioneProvenienza;
	}


	public void setRegioneProvenienza(RegioneDTO regioneProvenienza) {
		this.regioneProvenienza = regioneProvenienza;
	}


	public ClubDTO getClubProvenienza() {
		return clubProvenienza;
	}


	public void setClubProvenienza(ClubDTO club) {
		this.clubProvenienza = club;
	}


	public Boolean getPresenteTorneo() {
		return presenteTorneo;
	}


	public void setPresenteTorneo(Boolean presenteTorneo) {
		this.presenteTorneo = presenteTorneo;
	}


	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	

	public Date getDataDiNascita() {
		return dataDiNascita;
	}

	public void setDataDiNascita(Date dataDiNascita) {
		this.dataDiNascita = dataDiNascita;
	}
	
	public Integer getIdNazionale() {
		return idNazionale;
	}

	public void setIdNazionale(Integer idNazionale) {
		this.idNazionale = idNazionale;
	}

	public Boolean isFissoAlTipoDiTavolo() {
		return fissoAlTipoDiTavolo;
	}

	public void setFissoAlTipoDiTavolo(Boolean fissoAlTipoDiTavolo) {
		this.fissoAlTipoDiTavolo = fissoAlTipoDiTavolo;
	}

	public int compareTo(Object arg0) {
		GiocatoreDTO giocatore = (GiocatoreDTO) arg0;
		return giocatore.getId().compareTo(this.id);
	}
	
	

}
