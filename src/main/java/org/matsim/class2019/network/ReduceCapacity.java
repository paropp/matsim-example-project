package org.matsim.class2019.network;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;


public class ReduceCapacity {
	
	private static Path inputNetwork = Paths.get("/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_reducedCapacity/input/berlin-v5-network.xml.gz");
	private static Path outputNetwork = Paths.get("/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_reducedCapacity/input/berlin-v5-network-edit.xml.gz");
	
	//	No-Car-Zone: Unter den Linden
//	private String[] lindenLinks = new String[] { "140592", "126811", "126810",
//	  "126809", "24397", "111285", "36964", "36963", "151904", "4234", "54782",
//	  "40676", "54785", "151896", "32495", "11111", "11110", "42529", "126803",
//	  "137906", "137905", "137904", "137903", "137902", "13166", "13167", "65531",
//	  "65530", "13067", "13068", "24398", "24399", "160065", "160066", "107789",
//	  "107790", "142295", "142296" } ;
	 
	//	No-Car-Zone: 17. Juni
	private String[] juniLinks = new String[] { "29009", "153365", "126782", "57952",
	  "66328", "153347", "29023", "79614" } ;
	
	//select and adjust path project folder
	private String[] links2Remove = juniLinks ;

	public static void main(String[] args) {
		new ReduceCapacity().run();
	}
	
	public void run() {
		
		Network network = NetworkUtils.createNetwork();
		new MatsimNetworkReader( network ).readFile( inputNetwork.toString() );
		
		//Policy: Straße schließen
		for( String link : links2Remove ) {
			network.getLinks().get(Id.createLinkId( link )).setCapacity(1);
		}
		
		new NetworkWriter(network).write(outputNetwork.toString());
	}

}
