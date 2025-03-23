package it.desimone.risiko.torneo.scoreplayer;

import it.desimone.risiko.torneo.dto.ClubDTO;
import it.desimone.risiko.torneo.dto.GiocatoreDTO;
import it.desimone.risiko.torneo.dto.Partita;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface ScoreTeam{

	public ClubDTO getTeam();
	public BigDecimal getPunteggioB(int numeroPartiteValide);
	public Float getPunteggio(int numeroPartiteValide);
	public int getNumeroVittorie();
	public Set<GiocatoreDTO> getGiocatori();
	public List<Partita> getPartite();
	public void addPartitaPerGiocatore(GiocatoreDTO giocatore, Partita partita);
	public List<Partita> getPartitePerGiocatore(GiocatoreDTO giocatore);
	public void setPosition(int position);
	public int getPosition();
}
