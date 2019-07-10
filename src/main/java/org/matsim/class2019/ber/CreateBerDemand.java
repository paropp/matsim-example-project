package org.matsim.class2019.ber;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
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
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

class CreateBerDemand {

	private static final Logger logger = Logger.getLogger("CreateDemand");

	private static final String HOME_REGION = "Wohnort";
	private static final String WORK_REGION = "Arbeitsort";
	private static final String TOTAL = "Insgesamt";
	private static final String REGION_KEY = "Schluessel";
	private static final String HOME_AND_WORK_REGION = "Wohnort gleich Arbeitsort";

	private static final int HOME_END_TIME = 9 * 60 * 60;
	private static final int WORK_END_TIME = 17 * 60 * 60;
	private static final double SCALE_FACTOR = 0.1;
	private static final GeometryFactory geometryFactory = new GeometryFactory();

	private final Map<String, Geometry> regions;
	private final EnumeratedDistribution<Geometry> landcover;
	//private final Path interRegionCommuterStatistic;
	private final Random random = new Random();

	private Population population;

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
		Geometry  geometry = factory.createPolygon(new Coordinate[] {
				new Coordinate(4572280.12883134, 5841054.390968139),
				new Coordinate(4624253.583543593, 5842538.259215522),
				new Coordinate(4622327.17205539, 5796502.222930692),
				new Coordinate(4562886.2292778995, 5793245.98195416),
				new Coordinate(4572280.12883134, 5841054.390968139)
		});
		
		//create Airport Coodinate at SXF from Node pt_000008010109
		Coordinate AirportCoord = new Coordinate( 4603139.379672928, 5807465.218550463 ) ;
		
		this.population = PopulationUtils.createPopulation( null ) ;
		//TODO: does this work?
	}

	Population getPopulation() {
		return this.population;
	}

	void create( Path plans_input, Path plans_output, Path arrDepSeats) {
		PopulationUtils.readPopulation( population, plans_input.toString() );
		createAirportCommuters( arrDepSeats );
		logger.info("Done.");
	}

	private void createAirportCommuters( Path arrDepSeats ) {

		logger.info( "Create travelers" );
		//TODO: beenden

		for (XXX) {
			
			createPersons( arrDepSeats, geometry, AirportCoord, numberOfCommuters );
		}
	}

	private void createPersons(Path arrDepSeats, Geometry geometry, Coordinate AirportCoord, int numberOfPersons) {

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
		int sumArrivals = 0;
		int sumDepartures = 0;
		
		try (CSVParser parser = CSVParser.parse(arrDepSeats, StandardCharsets.UTF_8, CSVFormat.newFormat('\t').withFirstRecordAsHeader())) {
			
			for( CSVRecord record : parser ) {
				sumArrivals +=  Integer.parseInt( record.get( "Arr" ) ) ;
				sumArrivals +=  Integer.parseInt( record.get( "Dep" ) ) ;
			}

			// this will iterate over every line in the commuter statistics except the first one which contains the column headers
			for (CSVRecord record : parser) {
				if ( record.get( "timeBin" ) != null ) {
					currentHomeRegion = record.get(HOME_REGION);
				} else {
					String workRegion = record.get(WORK_REGION);
					// we have to use the try parse value method here, because there are some weird values in the 'total'
					// column which we have to filter out
					int numberOfCommuters = tryParseValue(record.get(TOTAL));
					createPersons(currentHomeRegion, workRegion, numberOfCommuters);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for ( int i = 0; i < numberOfPersons * SCALE_FACTOR * 0.5; i++ ) {

			Coord home = getCoordInGeometry( geometry ) ;
			Coord work = getCoordInGeometry( workRegion ) ;
			String id = homeRegionKey + "_" + workRegionKey + "_" + i ;

			Person person = createPerson( home, work, TransportMode.car, id ) ;
			population.addPerson( person );
		}
	}

	private Person createPerson(Coord home, Coord work, String mode, String id) {

		// create a person by using the population's factory
		// The only required argument is an id
		Person person = population.getFactory().createPerson(Id.createPersonId(id));
		Plan plan = createPlan(home, work, mode);
		person.addPlan(plan);
		return person;
	}

	private Plan createPlan(Coord home, Coord work, String mode) {

		// create a plan for home and work. Note, that activity -> leg -> activity -> leg -> activity have to be inserted in the right
		// order.
		Plan plan = population.getFactory().createPlan();

		Activity homeActivityInTheMorning = population.getFactory().createActivityFromCoord("home", home);
		homeActivityInTheMorning.setEndTime(HOME_END_TIME);
		plan.addActivity(homeActivityInTheMorning);

		Leg toWork = population.getFactory().createLeg(mode);
		plan.addLeg(toWork);

		Activity workActivity = population.getFactory().createActivityFromCoord("work", work);
		workActivity.setEndTime(WORK_END_TIME);
		plan.addActivity(workActivity);

		Leg toHome = population.getFactory().createLeg(mode);
		plan.addLeg(toHome);

		Activity homeActivityInTheEvening = population.getFactory().createActivityFromCoord("home", home);
		plan.addActivity(homeActivityInTheEvening);

		return plan;
	}

	private Coord getCoordInGeometry(Geometry geometry) {

		double x, y;
		Point point;
		
		//geometry.contains(MGC.coord2Point(coord));

		// if the landcover feature is in the correct region generate a random coordinate within the bounding box of the
		// landcover feature. Repeat until a coordinate is found which is actually within the geometry feature.
		do {
			Envelope envelope = geometry.getEnvelopeInternal();

			x = envelope.getMinX() + envelope.getWidth() * random.nextDouble();
			y = envelope.getMinY() + envelope.getHeight() * random.nextDouble();
			
			point = geometryFactory.createPoint(new Coordinate(x, y));
		} while (point == null || !geometry.contains(point));

		return new Coord(x, y);
	}

	private int tryParseValue(String value) {

		// first remove things excel may have put into the value
		value = value.replace(",", "");

		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}