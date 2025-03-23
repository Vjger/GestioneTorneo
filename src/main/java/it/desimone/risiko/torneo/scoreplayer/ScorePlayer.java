package it.desimone.risiko.torneo.scoreplayer;

import it.desimone.risiko.torneo.dto.GiocatoreDTO;
import it.desimone.risiko.torneo.dto.Partita;

import java.math.BigDecimal;

public interface ScorePlayer{

	public BigDecimal getPunteggioB(boolean conScarto);
	public Float getPunteggio(boolean conScarto);
	public int getNumeroVittorie();
	public Float getPunteggioMassimo();
	public GiocatoreDTO getGiocatore();
	public void setGiocatore(GiocatoreDTO giocatore);
	public Partita[] getPartite();
	public void setPosition(int position);
	public int getPosition();
}
