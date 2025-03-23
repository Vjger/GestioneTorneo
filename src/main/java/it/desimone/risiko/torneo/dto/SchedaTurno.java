package it.desimone.risiko.torneo.dto;


public class SchedaTurno {

	private Integer numeroTurno;
	
	private Partita[] partite;

	public Integer getNumeroTurno() {
		return numeroTurno;
	}

	public void setNumeroTurno(Integer numeroTurno) {
		this.numeroTurno = numeroTurno;
	}

	public Partita[] getPartite() {
		return partite;
	}

	public void setPartite(Partita[] partite) {
		this.partite = partite;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((numeroTurno == null) ? 0 : numeroTurno.hashCode());
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
		SchedaTurno other = (SchedaTurno) obj;
		if (numeroTurno == null) {
			if (other.numeroTurno != null)
				return false;
		} else if (!numeroTurno.equals(other.numeroTurno))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SchedaTurno [numeroTurno=" + numeroTurno + ", partite="
				+ partite + "]";
	}
	
}
