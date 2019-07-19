package org.matsim.class2019.ber;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Random;
import java.util.logging.Logger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.mobsim.qsim.agents.PopulationAgentSource;
import org.matsim.core.population.PopulationUtils;
import org.matsim.utils.objectattributes.attributable.Attributes;
import org.matsim.utils.objectattributes.attributable.AttributesUtils;

class CreateBerDemand {

	private static final Logger logger = Logger.getLogger("CreateDemand");

	private static final double SCALE_FACTOR = 0.01;
	private static final GeometryFactory geometryFactory = new GeometryFactory();

	private final Random random = new Random();
	private Population population ;
	private Coord AirportCoord ;
	private Geometry  geometry ;

	CreateBerDemand() {
		
		GeometryFactory factory = new GeometryFactory();
		this.geometry = factory.createPolygon(new Coordinate[] {
				new Coordinate(4579118.803517, 5832470.087352),
				new Coordinate(4579118.803517, 5809355.682157),
				new Coordinate(4610687.504775, 5809355.682157),
				new Coordinate(4610687.504775, 5832470.087352),
				new Coordinate(4579118.803517, 5832470.087352)
		});
		
		//create Airport Coodinate at SXF from Node pt_000008010109
		this.AirportCoord = new Coord( 4603430.97313, 5807236.681163 ) ;
		
		this.population = PopulationUtils.createPopulation( ConfigUtils.createConfig() );

		
	}

	Population getPopulation() {
		return this.population;
	}

	void create( Path plans_input, Path arrDepSeats) {
		PopulationUtils.readPopulation( this.population, plans_input.toString() );
		createPersons( arrDepSeats, geometry, AirportCoord ) ;
		logger.info("Done.");
	}

	private void createPersons(Path arrDepSeats, Geometry geometry, Coord AirportCoord) {

		// create as many persons as there are commuters multiplied by the scale factor
		// how are departing and arriving persons are distributed over the day?
		// found only CAPA: Berlin Schoenefeld Airport
		// https://tinyurl.com/y5gtb6z4
		
		//to work with relative demand
		//int sumArrivals = 0;
		int sumDepartures = 0;
		
		try (CSVParser parser = CSVParser.parse(arrDepSeats, StandardCharsets.UTF_8, CSVFormat.newFormat(';').withFirstRecordAsHeader())) {
			
			for( CSVRecord record : parser ) {
				
				//sumArrivals		+=  Integer.parseInt( record.get( "Arr" ) ) ;
				sumDepartures	+=  Integer.parseInt( record.get( "Dep" ) ) ;
				System.out.println( sumDepartures );
			}

		} catch ( IOException e ) {
			e.printStackTrace();
		}
		
		int numberOfAddedPersons = 0;
		try (CSVParser parser = CSVParser.parse(arrDepSeats, StandardCharsets.UTF_8, CSVFormat.newFormat(';').withFirstRecordAsHeader())) {
			
			// this will iterate over every line in the commuter statistics except the first one which contains the column headers
			for (CSVRecord record : parser) {
				
				int timeBin					= Integer.parseInt( record.get( "timeBin" ) ) - 1;
				double departuresInFile		= Integer.parseInt( record.get( "Dep" ) );
				double departuresInPercent	= departuresInFile / sumDepartures;
				//double arrivalsInPercent	= Integer.parseInt( record.get( "Arr" ) ) / sumArrivals;
				//0.5 - because NUMBER_OF_TRAVELERS_TOTAL
				double departuresInBin		=  departuresInPercent * RunBer.NUMBER_OF_TRAVELERS_TOTAL * SCALE_FACTOR * 0.5 ;
				//int arrivalsInBin			= (int) ( arrivalsInPercent   * RunBer.NUMBER_OF_TRAVELERS_TOTAL * SCALE_FACTOR ) ;
				
				//because here there are departures
				//they have to take place at 25th/ 24th hour to be reachable
				if( timeBin == 0 ) timeBin += 24 ;
				
//				System.out.println( "timeBin: " + timeBin);
//				System.out.println( "departuresFromFile: " + Integer.parseInt( record.get( "Dep" )));
//				System.out.println( "sumDepartures: " + sumDepartures);
//				System.out.println( "departuresInPercent: " + departuresInPercent);
//				System.out.println( "departuresInBin: " + departuresInBin);
//				System.out.println( "##########################################");
				
				for ( int i = 0; i < departuresInBin; i++ ) {
					
					//to smear the travelers over the hour
					//would be better to know the capacity of an average plane,
					//to know how many are expected at a specific point in time (possible density of plane take offs)
					//all departures one hour earlier for check-in
					//all arrivals take place after one hour, so travelers spend one hour at airport
					//distribution of arrivals doesnt match real one
					//could be done by list thats filled with flyArrTime according to arrivalsInBin
					double flyDepTime = ( timeBin - 1 ) * 60 * 60  + ( ( ( 60 * 60 ) / departuresInBin ) * i ) ;
					double flyArrTime = flyDepTime + ( 60 * 60 ) ;

					Coord home = getCoordInGeometry( geometry ) ;
					String id = "airport_" + i + "_dep_" + flyDepTime ;

					Person person = createPerson( home, AirportCoord, TransportMode.car, id, flyDepTime, flyArrTime ) ;
					
					this.population.addPerson( person ) ;
					numberOfAddedPersons++ ;
					
					System.out.println( "##################################") ;
					System.out.println( "The following print-out has to be added to the person-attributes-xml-file. Dont know another way. Shame on me.") ;
					System.out.println( "##################################") ;
					System.out.println( "\t<object id=\"" + id + "\">") ;
					System.out.println( "\t\t<attribute name=\"subpopulation\" class=\"java.lang.String\">person</attribute>") ;
					System.out.println( "\t</object>") ;

					//System.out.println( "added Person " + numberOfAddedPersons + ": " + id) ;
					
					//timeBin 0 -> objects fly over night, go home by day
					//rest: entsprechend verteilung
					
					//penalty hoch in activity
				}
				
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}		
	}

	private Person createPerson(
			Coord home,
			Coord AirportCoord,
			String mode,
			String id,
			double flyDepTime,
			double flyArrTime ) {

		// create a person by using the population's factory
		// The only required argument is an id
		Person person = this.population.getFactory().createPerson( Id.createPersonId( id ) ) ;
		Plan plan = createPlan( home, AirportCoord, mode, flyDepTime, flyArrTime ) ;
		person.addPlan( plan ) ;
		
		//doesnt work
		person.getAttributes().putAttribute( "subpopulation", "person" ) ;
		
		return person;
	}

	private Plan createPlan(
			Coord home,
			Coord AirportCoord,
			String mode,
			double flyDepTime,
			double flyArrTime ) {

		// create a plan for home and work. Note, that activity -> leg -> activity -> leg -> activity have to be inserted in the right
		// order.
		Plan plan = this.population.getFactory().createPlan() ;

		Activity homeActivity = this.population.getFactory().createActivityFromCoord( "home", home ) ;
		// let the agents start from home 30 minutes prior to their flight
		homeActivity.setEndTime( flyDepTime - (30 * 60) ) ;
		homeActivity.getAttributes().putAttribute( "subpopulation", "person" ) ;
		plan.addActivity( homeActivity ) ;

		Leg toFly = this.population.getFactory().createLeg( mode ) ;
		toFly.getAttributes().putAttribute( "subpopulation", "person" ) ;
		plan.addLeg( toFly ) ;

		Activity flyActivity = this.population.getFactory().createActivityFromCoord( "fly", AirportCoord ) ;
		flyActivity.setStartTime( flyDepTime ) ;
		flyActivity.setEndTime( flyArrTime ) ;
		flyActivity.getAttributes().putAttribute( "subpopulation", "person" ) ;
		plan.addActivity( flyActivity ) ;

		Leg toHome = this.population.getFactory().createLeg( mode );
		toHome.getAttributes().putAttribute( "subpopulation", "person" ) ;
		plan.addLeg( toHome );

		Activity homeActivityInTheEvening = this.population.getFactory().createActivityFromCoord( "home", home );
		homeActivityInTheEvening.getAttributes().putAttribute( "subpopulation", "person" ) ;
		plan.addActivity( homeActivityInTheEvening ) ;
		
		//doesnt work
		plan.getAttributes().putAttribute( "subpopulation", "person" ) ;
		
		return plan;
	}

	private Coord getCoordInGeometry( Geometry geometry ) {

		double x, y;
		Point point;
		
		do {
			Envelope envelope = geometry.getEnvelopeInternal();

			x = envelope.getMinX() + envelope.getWidth() * random.nextDouble();
			y = envelope.getMinY() + envelope.getHeight() * random.nextDouble();
			
			point = geometryFactory.createPoint( new Coordinate(x, y) ) ;
		} while ( point == null || !geometry.contains(point) ) ;

		return new Coord(x, y);
	}

}