package org.matsim.class2019.network;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.population.io.PopulationWriter;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.scenario.ScenarioUtils;

public class ReduceModes {
	
	private static Path inputNetwork = Paths.get(	"/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_reducedModes/input/berlin-v5-network.xml.gz") ;
	private static Path outputNetwork =	 Paths.get(	"/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_reducedModes/input/berlin-v5-network-edit.xml.gz") ;
	private static Path outputPlansFile = Paths.get("/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_reducedModes/input/berlin-v5.3-1pct.plans-edit.xml.gz") ;
	private static String configFile =				"/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_reducedModes/input/berlin-v5.3-1pct.config.xml" ;
	
	//	No-Car-Zone: Unter den Linden
	private String[] lindenLinks = new String[] {
			"140592",
			"126811",
			"126810",
			"126809",
			"24397",
			"111285",
			"36964",
			"36963",
			"151904",
			"4234",
			"54782",
			"40676",
			"54785",
			"151896",
			"32495",
			"11111",
			"11110",
			"42529",
			"126803",
			"137906",
			"137905",
			"137904",
			"137903",
			"137902",
			"13166",
			"13167",
			"65531",
			"65530",
			"13067",
			"13068",
			"24398",
			"24399",
			"160065",
			"160066", 
			"107789",
			"107790",
			"142295",
			"142296"
			} ;
	 
	//	No-Car-Zone: 17. Juni
	private String[] juniLinks = new String[] {
			"29009",
			"153365",
			"126782",
			"57952",
			"66328",
			"153347",
			"29023",
			"79614"
			} ;
	
	//select and adjust path project folder
	private String[] links2Remove = juniLinks ;
	
	public static void main(String[] args) {
		new ReduceModes().run();
	}
	
	public void run() {
		
		Set<String> allowedModes = new HashSet<>();

		allowedModes.add(TransportMode.bike);
		allowedModes.add(TransportMode.walk);
		allowedModes.add(TransportMode.access_walk);
		allowedModes.add(TransportMode.egress_walk);
		allowedModes.add(TransportMode.transit_walk);
		
		Network network = NetworkUtils.createNetwork() ;
		new MatsimNetworkReader(network).readFile(inputNetwork.toString()) ;
		
		//Policy: Modes nur bike + Fuß
		//kein REMOVE weil kA welche Modes schon drin sind
		
		for( String link : links2Remove ) {
			network.getLinks().get(Id.createLinkId( link )).setAllowedModes( allowedModes ) ;
		}
		
		//und jetzt alle routen löschen, wenn id in string1 oder string 2
		
        Config config = ConfigUtils.loadConfig( configFile );
        
        Scenario scenario = ScenarioUtils.loadScenario( config ) ;
        final Population pop = scenario.getPopulation();
        
        for ( Person person : pop.getPersons().values() ) {
            Plan plan = person.getSelectedPlan() ;
            
            for ( Leg leg : TripStructureUtils.getLegs( plan ) ) {
                	
                	for(String link: links2Remove) {
                    	boolean includesLink2Remove = leg.getRoute().toString().contains( link );
                    	
                        if ( includesLink2Remove ) {
                        	leg.setRoute(null);
                        	break;
                        } else {}
                    }
            	}
            }
        
        new PopulationWriter( pop ).write( outputPlansFile.toString() ) ;
        new NetworkWriter( network ).write( outputNetwork.toString() );
	}

}
