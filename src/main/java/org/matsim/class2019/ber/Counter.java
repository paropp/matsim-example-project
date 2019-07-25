package org.matsim.class2019.ber;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PopulationUtils;

public class Counter {

	private static final Path PLANS_PATH = Paths.get( "/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_BER/output_policy_160/berlin-v5.3-1pct.output_plans-express.xml.gz" ) ;
	
	private Population pop ;
	
	public static void main( String[] args ) {
		
		new Counter().run();
	}
	
	public void run() {
		this.pop = PopulationUtils.createPopulation( ConfigUtils.createConfig() );
		PopulationUtils.readPopulation( pop, PLANS_PATH.toString() );
		
		Set<Id<Person>> uniquePersonsSet = new HashSet<>();
		for( Person person : pop.getPersons().values() ) {
			Id<Person> personId = person.getId() ;
			if( uniquePersonsSet.contains( personId ) ) {
			} else {
				uniquePersonsSet.add( personId );
			}
		}
		System.out.println( "unique Users: " + uniquePersonsSet.size() );
		
		int counterAirportPersons = 0 ;
		for( Id<Person> personId :  uniquePersonsSet ) {
			if( personId.toString().contains( "airport" ) ) {
				counterAirportPersons++ ;
			} else {}
		}
		System.out.println( "unique AirportUsers: " + counterAirportPersons );
		

	}
}
