package org.matsim.class2019.analysis;

import org.jfree.data.io.CSV;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.class2019.basics.Rectangle;
import org.matsim.class2019.network.Link2Add;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.utils.io.IOUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Format;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("ALL")
public class RunBasicAnalysis {
	
	private static String[] pathsA100 = new String[] {
			"/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_A100/output/berlin-v5.3-1pct.output_network.xml.gz" ,
			"/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_A100/output/berlin-v5.3-1pct.output_events.xml.gz" ,
			"/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct/output-berlin-v5.3-1pct/berlin-v5.3-1pct.output_events.xml.gz" ,
			"/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_A100/output/"
	};
	
	private static String[] pathsJuneModes = new String[] {
			"/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_reducedModes_17thJune/output/berlin-v5.3-1pct.output_network.xml.gz" ,
			"/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct/output-berlin-v5.3-1pct/berlin-v5.3-1pct.output_events.xml.gz" ,
			"/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_reducedModes_17thJune/output/berlin-v5.3-1pct.output_events.xml.gz" ,
			"/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_reducedModes_17thJune/output/"
	};
	
	private static String[] pathsJuneCapa = new String[] {
			"/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_reducedCapacity_17thJune/output/berlin-v5.3-1pct.output_network.xml.gz" ,
			"/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct/output-berlin-v5.3-1pct/berlin-v5.3-1pct.output_events.xml.gz" ,
			"/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_reducedCapacity_17thJune/output/berlin-v5.3-1pct.output_events.xml.gz" ,
			"/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_reducedCapacity_17thJune/output/"
	};
	
	private static String[] pathsBundes = new String[] {
			"/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_reducedLanes_Bundes/output/berlin-v5.3-1pct.output_network.xml.gz" ,
			"/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct/output-berlin-v5.3-1pct/berlin-v5.3-1pct.output_events.xml.gz" ,
			"/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_reducedLanes_Bundes/output/berlin-v5.3-1pct.output_events.xml.gz" ,
			"/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_reducedLanes_Bundes/output/"
	};
	
	private static String[] pathsBer = new String[] {
			"/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_BER/output_policy_160/berlin-v5.3-1pct.output_network.xml.gz" ,
			"/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_BER/output_base2/berlin-v5.3-1pct.output_events.xml.gz" ,
			"/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_BER/output_policy_160/berlin-v5.3-1pct.output_events.xml.gz" ,
			"/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_BER/output/"
	};

	
	private static String[] linksBer = new String[] {
			"kreuz-sxf",
			"sxf-kreuz",
			"hbf-kreuz",
			"kreuz-hbf"
	} ;
	
	private static String[] linksA100 = new String[] {
			"9999900",
			"9999901",
			"9999902",
			"9999903",
			"9999904",
			"9999905",
			"9999906",
			"9999907",
			"9999908",
			"9999909",
			"9999910",
			"9999911",
			"9999912",
			"9999913",
			"9999914",
			"9999915",
			"9999916",
			"9999917"
	};
	
	private static String[] linksJune = new String[] { "29009", "153365", "126782", "57952",
			  "66328", "153347", "29023", "79614" } ;
	
	private static String[] linksBundes = new String[] {
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
			"62631"
	};
			
	private static String[] paths = pathsBer;
	private static String[] links2Watch = linksBer;
	
	public static void main(String[] args) {
		RunBasicAnalysis.run( paths, links2Watch ) ;
	}
	
	static void run(String[] paths, String[] links2Watch) {

		// get the paths for the network and the events
		Path networkpath = Paths.get(paths[0]);
		Path baseCaseEventsPath = Paths.get(paths[1]);
		Path policyCaseEventsPath = Paths.get(paths[2]);

		// read in the simulation network
		Network network = NetworkUtils.createNetwork();
		new MatsimNetworkReader(network).readFile(networkpath.toString());

		Set<Id<Link>> linksToWatch = new HashSet<>();
		for( String linkId : links2Watch ) {
			linksToWatch.add(Id.createLinkId( linkId ));
		}

		// start preparing the events manager
		AgentTravelledOnLinkEventHandler agentTravelledOnLinkEventHandler = new AgentTravelledOnLinkEventHandler(linksToWatch);
		TravelDistanceEventHandler travelDistanceEventHandler = new TravelDistanceEventHandler(network);
		TravelTimeEventHandler travelTimeEventHandler = new TravelTimeEventHandler();

		EventsManager baseCaseManager = EventsUtils.createEventsManager();
		baseCaseManager.addHandler(agentTravelledOnLinkEventHandler);
		baseCaseManager.addHandler(travelDistanceEventHandler);
		baseCaseManager.addHandler(travelTimeEventHandler);

		// read the actual events file
		new MatsimEventsReader(baseCaseManager).readFile(baseCaseEventsPath.toString()) ;

		// start preparing the events manager for the policy case
		TravelDistanceEventHandler travelDistanceEventHandlerPolicy = new TravelDistanceEventHandler(network) ;
		TravelTimeEventHandler travelTimeEventHandlerPolicy = new TravelTimeEventHandler() ;

		EventsManager policyCaseManager = EventsUtils.createEventsManager() ;
		policyCaseManager.addHandler(travelDistanceEventHandlerPolicy) ;
		policyCaseManager.addHandler(travelTimeEventHandlerPolicy) ;

		new MatsimEventsReader(policyCaseManager).readFile(policyCaseEventsPath.toString()) ;
		
		double travelTimeBaseTotal			= travelTimeEventHandler.calculateOverallTravelTime();
		double travelTimePolicyTotal		= travelTimeEventHandlerPolicy.calculateOverallTravelTime();
		double travelTimeTotalDiff			= travelTimePolicyTotal - travelTimeBaseTotal;
		
		double travelDistanceBaseTotal		= travelDistanceEventHandler.getTotalTravelDistance();
		double travelDistancePolicyTotal	= travelDistanceEventHandlerPolicy.getTotalTravelDistance();
		double travelDistanceTotalDiff		= travelDistancePolicyTotal - travelDistanceBaseTotal;
		
		System.out.println("Total travel time base case: " + travelTimeBaseTotal / 60 / 60 + " hours") ;
		System.out.println("Total travel time policy case: " + travelTimePolicyTotal / 60 / 60 + " hours") ;
		
		System.out.println("Total travel distance base case: " + travelDistanceBaseTotal / 1000 + " km") ;
		System.out.println("Total travel distance policy case: " + travelDistancePolicyTotal / 1000 + " km") ;

		// calculate travel time for people who used the street
		double baseCaseTravelTime = travelTimeEventHandler.getTravelTimesByPerson().entrySet().stream()
				.filter(entry -> agentTravelledOnLinkEventHandler.getPersonOnWatchedLinks().contains(entry.getKey()))
				.mapToDouble(entry -> entry.getValue())
				.sum();

		double policyCaseTravelTime = travelTimeEventHandlerPolicy.getTravelTimesByPerson().entrySet().stream()
				.filter(entry -> agentTravelledOnLinkEventHandler.getPersonOnWatchedLinks().contains(entry.getKey()))
				.mapToDouble(entry -> entry.getValue())
				.sum();

		System.out.println("Difference in travel time for people who travelled on link: "
				+ (policyCaseTravelTime - baseCaseTravelTime) / 60 / 60 + " hours");

		// calculate travel distances for people who used the street
		double baseCaseDistance = travelDistanceEventHandler.getTravelDistancesByPerson().entrySet().stream()
				.filter(entry -> agentTravelledOnLinkEventHandler.getPersonOnWatchedLinks().contains(entry.getKey()))
				.mapToDouble(entry -> entry.getValue())
				.sum();
		
		double policyCaseDistance = travelDistanceEventHandlerPolicy.getTravelDistancesByPerson().entrySet().stream()
				.filter(entry -> agentTravelledOnLinkEventHandler.getPersonOnWatchedLinks().contains(entry.getKey()))
				.mapToDouble(entry -> entry.getValue())
				.sum();
		
		System.out.println("Difference in travel distances for people who travelled on link: "
				+ (policyCaseDistance - baseCaseDistance) / 1000 + " km");
		
		//agentTravelledOnLinkEventHandler.getPersonOnWatchedLinks().forEach(a -> System.out.println(a));;

		BufferedWriter bwAna = IOUtils.getBufferedWriter(paths[3] + "TimesAndDistances2.txt");
		try {	
			bwAna.write( "Total travel time" );
			bwAna.newLine();
			bwAna.write( "### base case: " + travelTimeBaseTotal / 60 / 60 + " hours" );
			bwAna.newLine();
			bwAna.write( "### policy case: " + travelTimePolicyTotal / 60 / 60 + " hours" );
			bwAna.newLine();
			bwAna.write( "### delta: " +  travelTimeTotalDiff / 60 / 60 + " hours" );
			bwAna.newLine(); bwAna.newLine();
			bwAna.write( "Total travel distance" );
			bwAna.newLine();
			bwAna.write( "### base case: " + travelDistanceBaseTotal / 1000 + " km" );
			bwAna.newLine();
			bwAna.write( "### policy case: " + travelDistancePolicyTotal / 1000 + " km" );
			bwAna.newLine();
			bwAna.write( "### delta: " +  travelDistanceTotalDiff / 1000 + " km" );
			bwAna.newLine(); bwAna.newLine();
			bwAna.write( "Difference for people who travelled on link");
			bwAna.newLine();
			bwAna.write( "### in travel time: " + (policyCaseTravelTime - baseCaseTravelTime) / 60 / 60 + " hours");
			bwAna.newLine();
			bwAna.write( "### in travel distances: " + (policyCaseDistance - baseCaseDistance) / 1000 + " km");
			
			bwAna.flush();
			bwAna.close();
		}
		catch (IOException e ){
		}
		
		BufferedWriter bwPersons = IOUtils.getBufferedWriter(paths[3] + "PersonsOnWatchedLinks.txt");
		try {
			for (Id<Person> personId : agentTravelledOnLinkEventHandler.getPersonOnWatchedLinks()){
				bwPersons.write( personId.toString() );
				bwPersons.newLine();
			}
			bwPersons.flush();
			bwPersons.close();
		}
		catch (IOException e ){
		}
	}
}
