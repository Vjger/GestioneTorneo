package it.desimone.risiko.torneo.scoreplayer;


public abstract class AbstractScorePlayer implements ScorePlayer {

	private int position;

	@Override
	public void setPosition(int position) {
		this.position = position;
	}

	@Override
	public int getPosition() {
		return position;
	}

}
