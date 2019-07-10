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

		Path shapeFolder = Paths.get("/home/misax/Documents/berlin-v5.3-10pct_BER/input/berlin-shp") ;
		
		Path landcoverFolder = Paths.get("/home/misax/Documents/Uni/Master/Matsim/berlin-v5.3-10pct_BER/landcover/clc10.utm32s.shape/clc10") ;

		//this.interRegionCommuterStatistic = sampleFolder.resolve("commuters-inter-regional.csv");

		// read in the shape file and store the geometries according to their region identifier stored as 'RS' in the
		// shape file
		regions = ShapeFileReader.getAllFeatures( shapeFolder.resolve( "berlin.shp" ).toString() ).stream()
				.collect( Collectors.toMap( feature -> (String) feature.getAttribute("RS"), feature -> (Geometry) feature.getDefaultGeometry()) ) ;

		//create polygon representing berlin for additional demand genration
		GeometryFactory factory = new GeometryFactory();
		Geometry  geometry = factory.createPolygon(new Coordinate[] {
				new Coordinate(4572280.12883134, 5841054.390968139),
				new Coordinate(4624253.583543593, 5842538.259215522),
				new Coordinate(4622327.17205539, 5796502.222930692),
				new Coordinate(4562886.2292778995, 5793245.98195416),
				new Coordinate(4572280.12883134, 5841054.390968139)
		});

		this.population = PopulationUtils.createPopulation( ConfigUtils.createConfig() ) ;
	}

	Population getPopulation() {
		return this.population;
	}

	void create( Path input, Path output ) {
		//population = PopulationUtils.readPopulation(population, filename );
		population = PopulationUtils.createPopulation( ConfigUtils.createConfig() ) ;
		createAirportCommuters();
		logger.info("Done.");
	}

	private void createAirportCommuters() {

		logger.info( "Create travelers" );
		//TODO: beenden

		for (XXX) {
			
			createPersons(currentHomeRegion, workRegion, numberOfCommuters);
		}
	}

	private void createPersons(String homeRegionKey, String workRegionKey, int numberOfPersons) {

		// if the person works or lives outside the state we will not use them
		if (!regions.containsKey(homeRegionKey) || !regions.containsKey(workRegionKey)) return;

		logger.info("Home region: " + homeRegionKey + " work region: " + workRegionKey + " number of commuters: " + numberOfPersons);

		Geometry homeRegion = regions.get(homeRegionKey);
		Geometry workRegion = regions.get(workRegionKey);

		// create as many persons as there are commuters multiplied by the scale factor
		for (int i = 0; i < numberOfPersons * SCALE_FACTOR; i++) {

			Coord home = getCoordInGeometry(homeRegion);
			Coord work = getCoordInGeometry(workRegion);
			String id = homeRegionKey + "_" + workRegionKey + "_" + i;

			Person person = createPerson(home, work, TransportMode.car, id);
			population.addPerson(person);
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

	private Coord getCoordInGeometry(Geometry regrion) {

		double x, y;
		Point point;

		
		geometry.getEnvelopeInternal().getMinX()
		
		Coord coord = new Coord(1,2);
		geometry.contains(MGC.coord2Point(coord));

		// select a landcover feature and test whether it is in the right region. If not select a another one.
		do {
			selectedLandcover = landcover.sample();
		} while (!regrion.contains(selectedLandcover));

		// if the landcover feature is in the correct region generate a random coordinate within the bounding box of the
		// landcover feature. Repeat until a coordinate is found which is actually within the landcover feature.
		do {
			Envelope envelope = selectedLandcover.getEnvelopeInternal();

			x = envelope.getMinX() + envelope.getWidth() * random.nextDouble();
			y = envelope.getMinY() + envelope.getHeight() * random.nextDouble();
			
			point = geometryFactory.createPoint(new Coordinate(x, y));
		} while (point == null || !selectedLandcover.contains(point));

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