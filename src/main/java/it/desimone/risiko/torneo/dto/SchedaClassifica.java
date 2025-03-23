package it.desimone.risiko.torneo.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SchedaClassifica {

	public static class RigaClassifica{
		private Integer idGiocatore;
		private Integer posizioneGiocatore;
		private BigDecimal punteggioFinaleGiocatore;
		public Integer getIdGiocatore() {
			return idGiocatore;
		}
		public void setIdGiocatore(Integer idGiocatore) {
			this.idGiocatore = idGiocatore;
		}
		public Integer getPosizioneGiocatore() {
			return posizioneGiocatore;
		}
		public void setPosizioneGiocatore(Integer posizioneGiocatore) {
			this.posizioneGiocatore = posizioneGiocatore;
		}
		public BigDecimal getPunteggioFinaleGiocatore() {
			return punteggioFinaleGiocatore;
		}
		public void setPunteggioFinaleGiocatore(BigDecimal punteggioFinaleGiocatore) {
			this.punteggioFinaleGiocatore = punteggioFinaleGiocatore;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((idGiocatore == null) ? 0 : idGiocatore.hashCode());
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
			RigaClassifica other = (RigaClassifica) obj;
			if (idGiocatore == null) {
				if (other.idGiocatore != null)
					return false;
			} else if (!idGiocatore.equals(other.idGiocatore))
				return false;
			return true;
		}
	}
	
	private List<RigaClassifica> classifica;

	public List<RigaClassifica> getClassifica() {
		return classifica;
	}

	public void setClassifica(List<RigaClassifica> classifica) {
		this.classifica = classifica;
	}

	public void addRigaClassifica(RigaClassifica rigaClassifica){
		if (classifica == null){classifica = new ArrayList<RigaClassifica>();}
		classifica.add(rigaClassifica);
	}
	
	@Override
	public String toString() {
		return "SchedaClassifica [classifica=" + classifica + "]";
	}
	
	
	
}
