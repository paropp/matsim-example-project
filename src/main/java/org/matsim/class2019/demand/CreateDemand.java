package org.matsim.class2019.demand;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.locationtech.jts.geom.*;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;

class CreateDemand {

	private static final Logger logger = Logger.getLogger("CreateDemand");

	private static final String HOME_REGION = "Wohnort";
	private static final String WORK_REGION = "Arbeitsort";
	private static final String TOTAL = "Insgesamt";
	private static final String REGION_KEY = "Schluessel";
	private static final String HOME_AND_WORK_REGION = "Wohnort gleich Arbeitsort";

	private static final int HOME_END_TIME = 9 * 60 * 60;
	private static final int WORK_END_TIME = 17 * 60 * 60;
	private static final double SCALE_FACTOR = 0.01;
	private static final GeometryFactory geometryFactory = new GeometryFactory();

	private final Map<String, Geometry> regions;
	private final EnumeratedDistribution<Geometry> landcover;
	private final Path interRegionCommuterStatistic;
	private final Path innerRegionCommuterStatistic;
	private final Random random = new Random();

	private Population population;

	CreateDemand(Path interRegionCommuterStatistic, Path innerRegionCommuterStatistic, Path regionsShapeFile, Path landcoverShapeFile) {

		// test if input files exist
		if (!Files.exists(interRegionCommuterStatistic))
			throw new RuntimeException("File: " + interRegionCommuterStatistic + " doesn't exist");
		if (!Files.exists(regionsShapeFile)) throw new RuntimeException("File: " + regionsShapeFile + " doesn't exist");
		if (!Files.exists(innerRegionCommuterStatistic))
			throw new RuntimeException(("File " + innerRegionCommuterStatistic + " doesn't exist"));

		this.interRegionCommuterStatistic = interRegionCommuterStatistic;
		this.innerRegionCommuterStatistic = innerRegionCommuterStatistic;

		// read in the shape file and store the geometries according to their region identifier stored as 'RS' in the
		// shape file
		regions = ShapeFileReader.getAllFeatures(regionsShapeFile.toString()).stream()
				.collect(Collectors.toMap(feature -> (String) feature.getAttribute("RS"), feature -> (Geometry) feature.getDefaultGeometry()));

		// Bonus: Read in landcover data to make people stay in populated areas
		// we are using a weighted distribution by area-size, so that small areas receive less inhabitants than more
		// populated ones.
		List<Pair<Geometry, Double>> weightedGeometries = new ArrayList<>();
		for (SimpleFeature feature : ShapeFileReader.getAllFeatures(landcoverShapeFile.toString())) {
			Geometry geometry = (Geometry) feature.getDefaultGeometry();
			weightedGeometries.add(new Pair<>(geometry, geometry.getArea()));
		}
		landcover = new EnumeratedDistribution<>(weightedGeometries);

		this.population = PopulationUtils.createPopulation(ConfigUtils.createConfig());
	}

	Population getPopulation() {
		return this.population;
	}

	void create() {
		population = PopulationUtils.createPopulation(ConfigUtils.createConfig());
		createInterRegionCommuters();
		createInnerRegionCommuters();
		logger.info("Done.");
	}

	private void createInterRegionCommuters() {

		logger.info("Create commuters from inter regional statistic");

		// read the commuter csv file
		try (CSVParser parser = CSVParser.parse(interRegionCommuterStatistic, StandardCharsets.UTF_8, CSVFormat.newFormat(';').withFirstRecordAsHeader())) {

			String currentHomeRegion = "";

			// this will iterate over every line in the commuter statistics except the first one which contains the column headers
			for (CSVRecord record : parser) {
				if (record.get(HOME_REGION) != null && !record.get(HOME_REGION).equals("")) {
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
	}

	private void createInnerRegionCommuters() {

		logger.info("Creating regional commuters.");
		try (CSVParser parser = CSVParser.parse(innerRegionCommuterStatistic, StandardCharsets.UTF_8, CSVFormat.newFormat(';').withFirstRecordAsHeader())) {

			for (CSVRecord record : parser) {

				String region = record.get(REGION_KEY);
				if (region.endsWith("000")) {
					region = region.substring(0, region.length() - 3);
				}

				if (regions.containsKey(region)) {
					int numberOfCommuters = tryParseValue(record.get(HOME_AND_WORK_REGION));
					createPersons(region, region, numberOfCommuters);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void createPersons(String homeRegionKey, String workRegionKey, int numberOfPersons) {

		// if the person works or lives outside the state we will not use them
		if (!regions.containsKey(homeRegionKey) || !regions.containsKey(workRegionKey)) return;

		logger.info("Home region: " + homeRegionKey + " work region: " + workRegionKey + " number of commuters: " + numberOfPersons);

		Geometry homeRegion = regions.get(homeRegionKey);
		Geometry workRegion = regions.get(workRegionKey);

		for (int i = 0; i < numberOfPersons * SCALE_FACTOR; i++) {

			Coord home = getCoordInGeometry(homeRegion);
			Coord work = getCoordInGeometry(workRegion);
			String id = homeRegionKey + "_" + workRegionKey + "_" + i;

			Person person = createPerson(home, work, TransportMode.car, id);
			population.addPerson(person);
		}
	}

	private Person createPerson(Coord home, Coord work, String mode, String id) {

		Person person = population.getFactory().createPerson(Id.createPersonId(id));
		Plan plan = createPlan(home, work, mode);
		person.addPlan(plan);
		return person;
	}

	private Plan createPlan(Coord home, Coord work, String mode) {

		// create a plan for home and work. Note, that activity -> leg -> activity -> leg have to be inserted in the right
		// order.
		Plan plan = population.getFactory().createPlan();

		Activity homeActivity = population.getFactory().createActivityFromCoord("home", home);
		homeActivity.setEndTime(HOME_END_TIME);
		plan.addActivity(homeActivity);

		Leg toWork = population.getFactory().createLeg(mode);
		plan.addLeg(toWork);

		Activity workActivity = population.getFactory().createActivityFromCoord("work", work);
		workActivity.setEndTime(WORK_END_TIME);
		plan.addActivity(workActivity);

		Leg toHome = population.getFactory().createLeg(mode);
		plan.addLeg(toHome);

		Activity homeActivity2 = population.getFactory().createActivityFromCoord("home", home);
		plan.addActivity(homeActivity2);

		return plan;
	}

	private Coord getCoordInGeometry(Geometry regrion) {

		double x, y;
		Point point;
		Geometry selectedLandcover;

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