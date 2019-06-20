package org.matsim.class2019.network;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;

public class ReduceLanes {
	
	private static Path inputNetwork = Paths.get("/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_reducedLanes/input/berlin-v5-network.xml.gz");
	private static Path outputNetwork = Paths.get("/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_reducedLanes/input/berlin-v5-network-edit.xml.gz");

	private String bundesLinks2two[] = new String[]
			{
					"81239",
					"77388",
					"81240",
					"77387",
					"153659",
					"153660",
					"77386",
					"77328",
					"152989",
					"78198",
					"77391",
					"133604",
					"13016",
					"133605",
					"13015",
					"133606",
					"13014",
					"49245",
					"99226",
					"99280",
					"78199",
					"77389",
					"72173",
					"77390",
					"72172",
					"102671",
					"144179",
					"98313",
					"138531",
					"79839",
					"98313",
					"79839",
					"138531",
					"62502",
					"102687",
					"98316",
					"149298",
					"147020",
					"89022",
					"46610",
					"46609",
					"28916",
					"28917",
					"8010"
			};
	
	private String bundesLinks2one[] = new String[]
			{
					"144221",
					"99266",
					"99312",
					"48013",
					"48015",
					"48016",
					"111755",
					"111754",
					"144199",
					"144190",
					"144193",
					"144192",
					"144191",
					"144180",
					"144181",
					"144185",
					"144184",
					"87243",
					"143491",
					"143453",
					"143454",
					"102675",
					"62635",
					"143455",
					"62634",
					"62633",
					"143456",
					"62632",
					"62631",
					"8008",
					"157336",
					"8009",
					"157335",
					"157334",
					"157335"
			};
	
	public static void main(String[] args) {
		new ReduceLanes().run();
	}
	
	public void run() {
		
		Network network = NetworkUtils.createNetwork();
		new MatsimNetworkReader(network).readFile(inputNetwork.toString());
		
		//Policy: Anzahl Fahrstreifen + Kapazität reduzieren
		//Orientierung für Werte an Greifswalder Straße
		//Tunnel bleibt unangetastet, da rückbau als ausgeschlossen gilt
		for( String link: bundesLinks2two) {
			  network.getLinks().get(Id.createLinkId( link )).setNumberOfLanes(2);
			  network.getLinks().get(Id.createLinkId( link )).setCapacity(2000.0);
		}
		
		for( String link: bundesLinks2one ) {
			  network.getLinks().get(Id.createLinkId( link )).setNumberOfLanes(1);
			  network.getLinks().get(Id.createLinkId( link )).setCapacity(1200.0);
		}
		
		new NetworkWriter(network).write(outputNetwork.toString());
	}
	
}
