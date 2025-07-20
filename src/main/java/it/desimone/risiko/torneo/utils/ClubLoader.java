package it.desimone.risiko.torneo.utils;

import it.desimone.risiko.torneo.dto.ClubDTO;

import java.util.List;

public class ClubLoader {

	private final static java.util.List<ClubDTO> clubs = new java.util.ArrayList<ClubDTO>();
	
	public final static ClubDTO I_FEDERICIANI = new ClubDTO("BA","BARI [I Federiciani]");
	public final static ClubDTO TORRE_MAGICA = new ClubDTO("BT","BARLETTA [La Torre Magica]");
	public final static ClubDTO LE_MURA = new ClubDTO("BG","BERGAMO [Le Mura]");	
	public final static ClubDTO ASINELLI = new ClubDTO("BO","BOLOGNA [Club degli Asinelli]");
	public final static ClubDTO PHOENIX = new ClubDTO("BZ","BOLZANO [Phoenix]");	
	public final static ClubDTO KARALIS = new ClubDTO("CA","CAGLIARI [Karalis]");
	public final static ClubDTO I_MASNADIERI = new ClubDTO("TV","CASTELFRANCO VENETO [I Masnadieri]");	
	public final static ClubDTO LARIO = new ClubDTO("CO","COMO [Club Lario]");
	public final static ClubDTO IL_CANNONE = new ClubDTO("RA","FAENZA [Il Cannone]");
	public final static ClubDTO MALEDETTI_TOSCANI = new ClubDTO("FI","FIRENZE [Maledetti Toscani]");
	public final static ClubDTO HORUS_CLUB = new ClubDTO("LT","GAETA [Horus Club]");
	public final static ClubDTO LABYRINTH = new ClubDTO("GE","GENOVA [Labyrinth]");
	public final static ClubDTO LATINA_GIOCA = new ClubDTO("LT","LATINA [LatinaGioca]");
	public final static ClubDTO LI_DRAGHI = new ClubDTO("TR","TERNI [Li Draghi]");
	public final static ClubDTO EGADI = new ClubDTO("TP","MARSALA [Egadi]");
	public final static ClubDTO MESSINA = new ClubDTO("ME","MESSINA [RisiKo! dello Stretto]");
	public final static ClubDTO MILANO = new ClubDTO("MI","MILANO [RCU]");
	public final static ClubDTO IL_MAIALINO = new ClubDTO("MO","MODENA [Il Maialino]");
	public final static ClubDTO MONZAMICI = new ClubDTO("MI","MONZA [Monzamici]");	
	public final static ClubDTO RISIKOMANI = new ClubDTO("NA","NAPOLI [I RisiKo!..mani]");
	public final static ClubDTO EAGLES = new ClubDTO("PA","PALERMO [Eagles]");	
	public final static ClubDTO GIMAGIOKE = new ClubDTO("PU","PESARO URBINO [Gimagiokè]");
	public final static ClubDTO RAHAL_MAUT = new ClubDTO("AG","RACALMUTO [Rahal Maut]");
	public final static ClubDTO PANDA_ASSASSINO = new ClubDTO("RA","RAVENNA [Panda Assassino]");	
	public final static ClubDTO IL_GUFO = new ClubDTO("RM","ROMA [Il Gufo]");
	public final static ClubDTO SABAUDIA = new ClubDTO("LT","SABAUDIA [RCU]");
	public final static ClubDTO IL_GUISCARDO = new ClubDTO("SA","SALERNO [Il Guiscardo]");	
	public final static ClubDTO IL_PASSATEMPO = new ClubDTO("SR","SIRACUSA [Il Passatempo]");	
	public final static ClubDTO LUDICHE_MENTI = new ClubDTO("TR","TERNI [Ludiche Menti]");	
	public final static ClubDTO VAL_DI_FIEMME = new ClubDTO("TN","TRENTO [A.R.V.F.]");	
	public final static ClubDTO TORISIKO = new ClubDTO("TO","TORINO [ToRisiKo!]");
	public final static ClubDTO GRIFONE = new ClubDTO("VE","VENEZIA [Grifone]");
	public final static ClubDTO ARMATE_SCALIGERE = new ClubDTO("VR","VERONA [Le Armate Scaligere]");
	public final static ClubDTO FAVL = new ClubDTO("VT","VITERBO [FAVL]");
	public final static ClubDTO TITANI = new ClubDTO("AP", "SAN BENEDETTO DEL TRONTO [I Titani]");
	public final static ClubDTO WORLD_DOMINATION = new ClubDTO("CT", "ACIREALE [World Domination]");
	public final static ClubDTO CAPALBIO = new ClubDTO("GR", "CAPALBIO [RCU]");
	public final static ClubDTO AIRONI = new ClubDTO("VA", "GERENZANO [Aironi]");
	public final static ClubDTO BORGO_PILA = new ClubDTO("GE", "GENOVA [Borgo Pila]");
	public final static ClubDTO PANZER8 = new ClubDTO("BA", "BITONTO [Panzer8]");
	public final static ClubDTO I_SATANELLI = new ClubDTO("FG", "FOGGIA [I Satanelli]");
	public final static ClubDTO LE_ARMATE = new ClubDTO("AG", "LICATA [Le Armate]");
	public final static ClubDTO I_BALUARDI = new ClubDTO("LU", "LUCCA [I Baluardi]");
	public final static ClubDTO CESENATICO = new ClubDTO("FC", "CESENATICO [I Corsari]");
	public final static ClubDTO CALTANISSETTA = new ClubDTO("CL", "CALTANISSETTA [Eclettica]");
	public final static ClubDTO RISIKARE = new ClubDTO("BT","BARLETTA [RisikAré]");
	public final static ClubDTO I_PALLADIANI = new ClubDTO("VI","Vicenza [I Palladiani]");
	public final static ClubDTO QUARTU_SANT_ELENA = new ClubDTO("CA","Quartu Sant'Elena [Sardegna]");
	public final static ClubDTO SESSA_AURUNCA = new ClubDTO("CS","Sessa Aurunca [Risiko Aurunco]");
	public final static ClubDTO SIGNA = new ClubDTO("FI","Signa [Gli spettri dei colli]");
	public final static ClubDTO SAN_CATALDO = new ClubDTO("CL","San Cataldo [Il Pifferaio]");
	public final static ClubDTO TRADATE = new ClubDTO("VA","Tradate [I Galli]");

	static{
		clubs.add(I_FEDERICIANI);
		clubs.add(TORRE_MAGICA);
		clubs.add(ASINELLI);
		clubs.add(KARALIS);
		clubs.add(LARIO);
		clubs.add(IL_CANNONE);
		clubs.add(MALEDETTI_TOSCANI);
		clubs.add(HORUS_CLUB);
		clubs.add(LABYRINTH);
		clubs.add(LATINA_GIOCA);
		clubs.add(EGADI);
		clubs.add(MILANO);
		clubs.add(IL_MAIALINO);
		clubs.add(RISIKOMANI);
		clubs.add(GIMAGIOKE);
		clubs.add(IL_GUFO);
		clubs.add(SABAUDIA);
		clubs.add(IL_GUISCARDO);
		clubs.add(IL_PASSATEMPO);
		clubs.add(LUDICHE_MENTI);
		clubs.add(TORISIKO);
		clubs.add(GRIFONE);
		clubs.add(ARMATE_SCALIGERE);
		clubs.add(VAL_DI_FIEMME);
		clubs.add(PANDA_ASSASSINO);
		clubs.add(EAGLES);
		clubs.add(FAVL);
		clubs.add(MONZAMICI);
		clubs.add(PHOENIX);
		clubs.add(LE_MURA);
		clubs.add(RAHAL_MAUT);
		clubs.add(I_MASNADIERI);
		clubs.add(MESSINA);
		clubs.add(TITANI);
		clubs.add(WORLD_DOMINATION);
		clubs.add(CAPALBIO);
		clubs.add(AIRONI);
		clubs.add(BORGO_PILA);
		clubs.add(PANZER8);
		clubs.add(I_SATANELLI);
		clubs.add(I_BALUARDI);
		clubs.add(LE_ARMATE);
		clubs.add(CESENATICO);
		clubs.add(CALTANISSETTA);
		clubs.add(RISIKARE);
		clubs.add(I_PALLADIANI);
		clubs.add(QUARTU_SANT_ELENA);
		clubs.add(SESSA_AURUNCA);
		clubs.add(SIGNA);
		clubs.add(SAN_CATALDO);
		clubs.add(TRADATE);
	}
	
	public static List<ClubDTO> getClubs(){
		return clubs;
	}
	
	public static void main (String[] args){
		for (ClubDTO club: clubs){
			System.out.println(club);
		}
	}
}
