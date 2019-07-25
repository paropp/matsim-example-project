package org.matsim.class2019.ber;

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
	
	//TODO: doesnt work
	
	private static String[] pathsBer = new String[] {
			"/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_BER/output_policy_160/berlin-v5.3-1pct.output_network.xml.gz" ,
			"/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_BER/output_base/berlin-v5.3-1pct.output_events.xml.gz" ,
			"/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_BER/output_policy_160/berlin-v5.3-1pct.output_events.xml.gz" ,
			"/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_BER/output/"
	};

	
	private static String[] linksBer = new String[] {
			"kreuz-sxf",
			"sxf-kreuz",
			"hbf-kreuz",
			"kreuz-hbf"
	} ;
			
	private static String[] paths = pathsBer;
	private static String[] links2Watch = linksBer;
	
	public static void main(String[] args) {
		RunBasicAnalysis.run( paths, links2Watch ) ;
	}
	
	static void run(String[] paths, String[] links2Watch) {

		// get the paths for the network and the events
		Path networkpath = Paths.get( paths[0] ) ;
		Path baseCaseEventsPath = Paths.get( paths[1] ) ;
		Path policyCaseEventsPath = Paths.get( paths[2] ) ;

		// read in the simulation network
		Network network = NetworkUtils.createNetwork();
		new MatsimNetworkReader( network ).readFile( networkpath.toString() );

		Set<Id<Link>> linksToWatch = new HashSet<>();
		for( String linkId : links2Watch ) {
			linksToWatch.add( Id.createLinkId( linkId ) );
		}

		// start preparing the events manager
		AgentTravelledOnLinkEventHandler agentTravelledOnLinkEventHandler = new AgentTravelledOnLinkEventHandler( linksToWatch ) ;
		TravelDistanceEventHandler travelDistanceEventHandler = new TravelDistanceEventHandler( network ) ;
		TravelTimeEventHandler travelTimeEventHandler = new TravelTimeEventHandler() ;

		EventsManager baseCaseManager = EventsUtils.createEventsManager();
		baseCaseManager.addHandler( travelDistanceEventHandler ) ;
		baseCaseManager.addHandler( travelTimeEventHandler ) ;

		// read the actual events file
		new MatsimEventsReader( baseCaseManager ).readFile( baseCaseEventsPath.toString() ) ;

		// start preparing the events manager for the policy case
		TravelDistanceEventHandler travelDistanceEventHandlerPolicy = new TravelDistanceEventHandler( network ) ;
		TravelTimeEventHandler travelTimeEventHandlerPolicy = new TravelTimeEventHandler() ;

		EventsManager policyCaseManager = EventsUtils.createEventsManager() ;
		policyCaseManager.addHandler( agentTravelledOnLinkEventHandler ) ;
		policyCaseManager.addHandler( travelDistanceEventHandlerPolicy ) ;
		policyCaseManager.addHandler( travelTimeEventHandlerPolicy ) ;

		new MatsimEventsReader( policyCaseManager ).readFile( policyCaseEventsPath.toString() ) ;
		
		double travelTimeBaseTotal			= travelTimeEventHandler.calculateOverallTravelTime() ;
		double travelTimePolicyTotal		= travelTimeEventHandlerPolicy.calculateOverallTravelTime() ;
		double travelTimeTotalDiff			= travelTimePolicyTotal - travelTimeBaseTotal ;
		
		double travelDistanceBaseTotal		= travelDistanceEventHandler.getTotalTravelDistance() ;
		double travelDistancePolicyTotal	= travelDistanceEventHandlerPolicy.getTotalTravelDistance() ;
		double travelDistanceTotalDiff		= travelDistancePolicyTotal - travelDistanceBaseTotal ;
		
		System.out.println( "Total travel time base case: "			+ travelTimeBaseTotal		/ 60 / 60 + " hours" ) ;
		System.out.println( "Total travel time policy case: "		+ travelTimePolicyTotal		/ 60 / 60 + " hours" ) ;
		
		System.out.println( "Total travel distance base case: "		+ travelDistanceBaseTotal	/ 1000 + " km" ) ;
		System.out.println( "Total travel distance policy case: "	+ travelDistancePolicyTotal	/ 1000 + " km" ) ;

		//TODO: I think has to be rerun with ultra slow train
		// calculate travel time for people who used the street
		double baseCaseTravelTime = travelTimeEventHandler.getTravelTimesByPerson().entrySet().stream()
				.filter(entry -> agentTravelledOnLinkEventHandler.getPersonOnWatchedLinks().contains( entry.getKey() ))
				.mapToDouble( entry -> entry.getValue() )
				.sum();

		double policyCaseTravelTime = travelTimeEventHandlerPolicy.getTravelTimesByPerson().entrySet().stream()
				.filter(entry -> agentTravelledOnLinkEventHandler.getPersonOnWatchedLinks().contains( entry.getKey() ))
				.mapToDouble( entry -> entry.getValue() )
				.sum();

		System.out.println("Difference in travel time for people who travelled on link: "
				+ ( policyCaseTravelTime - baseCaseTravelTime ) / 60 / 60 + " hours");

		// calculate travel distances for people who used the street
		double baseCaseDistance = travelDistanceEventHandler.getTravelDistancesByPerson().entrySet().stream()
				.filter( entry -> agentTravelledOnLinkEventHandler.getPersonOnWatchedLinks().contains( entry.getKey() ) )
				.mapToDouble( entry -> entry.getValue() )
				.sum();
		
		double policyCaseDistance = travelDistanceEventHandlerPolicy.getTravelDistancesByPerson().entrySet().stream()
				.filter( entry -> agentTravelledOnLinkEventHandler.getPersonOnWatchedLinks().contains(entry.getKey()) )
				.mapToDouble( entry -> entry.getValue() )
				.sum();
		
		System.out.println("travel distances for people who travelled on link (base): "
				+ (baseCaseDistance) / 1000 + " km");
		
		System.out.println("travel distances for people who travelled on link (policy): "
				+ (policyCaseDistance) / 1000 + " km");
		
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
