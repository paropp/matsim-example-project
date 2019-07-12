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
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PopulationUtils;

class CreateBerDemand {

	private static final Logger logger = Logger.getLogger("CreateDemand");

	//private static final String HOME_REGION = "Wohnort";
	//private static final String WORK_REGION = "Arbeitsort";
	//private static final String TOTAL = "Insgesamt";
	//private static final String REGION_KEY = "Schluessel";
	//private static final String HOME_AND_WORK_REGION = "Wohnort gleich Arbeitsort";

	//private  int homeEndTime = 0;
	//private static final int WORK_END_TIME = 17 * 60 * 60;
	private static final double SCALE_FACTOR = 0.1;
	private static final GeometryFactory geometryFactory = new GeometryFactory();

	//private final Map<String, Geometry> regions;
	//private final EnumeratedDistribution<Geometry> landcover;
	//private final Path interRegionCommuterStatistic;
	private final Random random = new Random();
	private Population population ;
	private Coord AirportCoord ;
	private Geometry  geometry ;

	CreateBerDemand() {
		//TODO: pfade übergeben
		
		//this.ArrDepStatistic = .resolve("commuters-inter-regional.csv");

		//this.interRegionCommuterStatistic = sampleFolder.resolve("commuters-inter-regional.csv");

		// read in the shape file and store the geometries according to their region identifier stored as 'RS' in the
		// shape file
		//regions = ShapeFileReader.getAllFeatures( shapeFolder.resolve( "berlin.shp" ).toString() ).stream()
		//		.collect( Collectors.toMap( feature -> (String) feature.getAttribute("RS"), feature -> (Geometry) feature.getDefaultGeometry()) ) ;

		//create polygon representing berlin for additional demand genration
		
		GeometryFactory factory = new GeometryFactory();
		this.geometry = factory.createPolygon(new Coordinate[] {
				new Coordinate(4572280.12883134, 5841054.390968139),
				new Coordinate(4624253.583543593, 5842538.259215522),
				new Coordinate(4622327.17205539, 5796502.222930692),
				new Coordinate(4562886.2292778995, 5793245.98195416),
				new Coordinate(4572280.12883134, 5841054.390968139)
		});
		
		//create Airport Coodinate at SXF from Node pt_000008010109
		this.AirportCoord = new Coord( 4603139.379672928, 5807465.218550463 ) ;
		
		this.population = PopulationUtils.createPopulation( ConfigUtils.createConfig() );
		//TODO: does this work? no
	}

	Population getPopulation() {
		return this.population;
	}

	void create( Path plans_input, Path arrDepSeats) {
		PopulationUtils.readPopulation( population, plans_input.toString() );
		createPersons( arrDepSeats, geometry, AirportCoord ) ;
		logger.info("Done.");
	}

	private void createPersons(Path arrDepSeats, Geometry geometry, Coord AirportCoord) {

		// if the person works or lives outside the state we will not use them
		//if (!regions.containsKey(homeRegionKey) || !regions.containsKey(workRegionKey)) return;

		//logger.info("Home region: " + homeRegionKey + " work region: " + workRegionKey + " number of commuters: " + numberOfPersons);

		//Geometry homeRegion = regions.get(homeRegionKey);
		//Geometry workRegion = regions.get(workRegionKey);

		// create as many persons as there are commuters multiplied by the scale factor
		// how are departing and arriving persons are distributed over the day?
		// found only CAPA: Berlin Schoenefeld Airport
		// https://tinyurl.com/y5gtb6z4
		
		//to work with relative demand
		//int sumArrivals = 0;
		int sumDepartures = 0;
		
		try (CSVParser parser = CSVParser.parse(arrDepSeats, StandardCharsets.UTF_8, CSVFormat.newFormat('\t').withFirstRecordAsHeader())) {
			
			for( CSVRecord record : parser ) {
				//sumArrivals		+=  Integer.parseInt( record.get( "Arr" ) ) ;
				sumDepartures	+=  Integer.parseInt( record.get( "Dep" ) ) ;
			}

			// this will iterate over every line in the commuter statistics except the first one which contains the column headers
			for (CSVRecord record : parser) {
				
				if ( record.get( "timeBin" ) != null ) {
					
					int timeBin					= Integer.parseInt( record.get( "timeBin" ) ) - 1;
					double departuresInPercent	= Integer.parseInt( record.get( "Dep" ) ) / sumDepartures;
					//double arrivalsInPercent	= Integer.parseInt( record.get( "Arr" ) ) / sumArrivals;
					
					int departuresInBin			= (int) ( departuresInPercent * RunBer.NUMBER_OF_TRAVELERS_TOTAL * SCALE_FACTOR ) ;
					//int arrivalsInBin			= (int) ( arrivalsInPercent   * RunBer.NUMBER_OF_TRAVELERS_TOTAL * SCALE_FACTOR ) ;
					
					//because here there are departures
					//they have to take place at 25th/ 24th hour to be reachable
					if( timeBin == 0 ) timeBin += 24 ;
					
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
						population.addPerson( person );
						//timeBin 0 -> objects fly over night, go home by day
						//rest: entsprechend verteilung
						
						//penalty hoch in activity
					}
					
				} else {}
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
		Person person = population.getFactory().createPerson( Id.createPersonId( id ) ) ;
		Plan plan = createPlan( home, AirportCoord, mode, flyDepTime, flyArrTime ) ;
		person.addPlan( plan ) ;
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
		Plan plan = population.getFactory().createPlan() ;

		Activity homeActivity = population.getFactory().createActivityFromCoord( "home", home ) ;
		//homeActivityInTheMorning.setEndTime( HOME_END_TIME );
		//only fly will get start and end time
		plan.addActivity( homeActivity ) ;

		Leg toFly = population.getFactory().createLeg( mode ) ;
		plan.addLeg( toFly ) ;

		Activity flyActivity = population.getFactory().createActivityFromCoord( "fly", AirportCoord ) ;
		flyActivity.setStartTime( flyDepTime ) ;
		flyActivity.setEndTime( flyArrTime ) ;
		plan.addActivity( flyActivity );

		Leg toHome = population.getFactory().createLeg( mode );
		plan.addLeg( toHome );

		Activity homeActivityInTheEvening = population.getFactory().createActivityFromCoord( "home", home );
		plan.addActivity( homeActivityInTheEvening ) ;

		return plan;
	}

	private Coord getCoordInGeometry( Geometry geometry ) {

		double x, y;
		Point point;
		
		//geometry.contains(MGC.coord2Point(coord));

		// if the landcover feature is in the correct region generate a random coordinate within the bounding box of the
		// landcover feature. Repeat until a coordinate is found which is actually within the geometry feature.
		do {
			Envelope envelope = geometry.getEnvelopeInternal();

			x = envelope.getMinX() + envelope.getWidth() * random.nextDouble();
			y = envelope.getMinY() + envelope.getHeight() * random.nextDouble();
			
			point = geometryFactory.createPoint( new Coordinate(x, y) ) ;
		} while ( point == null || !geometry.contains(point) ) ;

		return new Coord(x, y);
	}

}