package org.matsim.class2019.ber;

import java.io.BufferedWriter;
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
import org.matsim.core.utils.io.IOUtils;

public class CounterAndWriter {

	private static final Path PLANS_PATH = Paths.get( "/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_BER/output_policy_160/berlin-v5.3-1pct.output_plans-express.xml.gz" ) ;
	private static final Path UNIQUE_AGENTS_PATH = Paths.get( "/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_BER/output_policy_160/uniqueAirportAgents.txt" ) ;
	private static final Path UNIQUE_AGENTS_NON_AIRPORT_PATH = Paths.get( "/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_BER/output_policy_160/uniqueNonAirportAgents.txt" ) ;

	
	private Population pop ;
	
	public static void main( String[] args ) {
		
		new CounterAndWriter().run();
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
        BufferedWriter bwUnique 		= IOUtils.getBufferedWriter( UNIQUE_AGENTS_PATH.toString() );
        BufferedWriter bwUniqueNonAir	= IOUtils.getBufferedWriter( UNIQUE_AGENTS_NON_AIRPORT_PATH.toString() );
        for( Id<Person> personId :  uniquePersonsSet ) {
			
			try {
				bwUnique.write( personId.toString() );
				bwUnique.newLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if( personId.toString().contains( "airport" ) ) {
				counterAirportPersons++ ;
				
			} else {
				
				try {
					bwUniqueNonAir.write( personId.toString() );
					bwUniqueNonAir.newLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		try {
			bwUnique.flush();
			bwUnique.close();
			bwUniqueNonAir.flush();
			bwUniqueNonAir.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		System.out.println( "unique AirportUsers: " + counterAirportPersons );
		

	}
}
