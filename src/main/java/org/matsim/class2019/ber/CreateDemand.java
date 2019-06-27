package org.matsim.class2019.ber;

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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This class generates a MATSim population from the sample files in
 * examples/tutorial/population/demandGenerationFromShapefile
 * It parses two commuter statistics and places agent's work and home locations in the corresponding regions.
 * The locations within a region are chosen randomly within the shape of a region extracted from the
 * 'thrueringen-kreise.shp' shape file. To ensure agents living in populated areas it is also ensured that home and work
 * locations lie within a shape extracted from the 'landcover.shp' shapefile. The input data is taken from:
 * <p>
 * commuters-inner-regional.csv: https://statistik.arbeitsagentur.de/nn_31966/SiteGlobals/Forms/Rubrikensuche/Rubrikensuche_Form.html?view=processForm&resourceId=210368&input_=&pageLocale=de&topicId=746732&year_month=201806&year_month.GROUP=1&search=Suchen
 * commuters-inter-regional.csv: https://statistik.arbeitsagentur.de/nn_31966/SiteGlobals/Forms/Rubrikensuche/Rubrikensuche_Form.html?view=processForm&resourceId=210368&input_=&pageLocale=de&topicId=882788&year_month=201806&year_month.GROUP=1&search=Suchen
 * thueringen-kreise.shp: http://www.geodatenzentrum.de/geodaten/gdz_rahmen.gdz_div?gdz_spr=deu&gdz_akt_zeile=5&gdz_anz_zeile=1&gdz_unt_zeile=13&gdz_user_id=0
 * landcover.shp: http://www.geodatenzentrum.de/geodaten/gdz_rahmen.gdz_div?gdz_spr=deu&gdz_akt_zeile=5&gdz_anz_zeile=1&gdz_unt_zeile=22&gdz_user_id=0
 * <p>
 * The leg mode of all legs is 'car', all agents leave home at 9am and finish work at 5pm.
 * The created population is a 1% sample
 */
class CreateDemand {

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
	private final Path interRegionCommuterStatistic;
	private final Path innerRegionCommuterStatistic;
	private final Random random = new Random();

	private Population population;

	Population getPopulation() {
		return this.population;
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
		Person person = population.getFactory().createPerson( Id.createPersonId(id) );
		Plan plan = createPlan( home, work, mode );
		person.addPlan( plan );
		return person;
	}

	private Plan createPlan(Coord home, Coord work, String mode) {

		// create a plan for home and work. Note, that activity -> leg -> activity -> leg -> activity have to be inserted in the right
		// order.
		Plan plan = population.getFactory().createPlan();

		Activity homeActivityInTheMorning = population.getFactory().createActivityFromCoord( "home", home );
		homeActivityInTheMorning.setEndTime( HOME_END_TIME ); //TODO: Verteilung
		plan.addActivity( homeActivityInTheMorning );

		Leg toWork = population.getFactory().createLeg( mode );
		plan.addLeg( toWork );

		Activity workActivity = population.getFactory().createActivityFromCoord("work", work);
		workActivity.setEndTime( WORK_END_TIME ); //TODO: Verteilung
		plan.addActivity( workActivity );

		Leg toHome = population.getFactory().createLeg( mode );
		plan.addLeg( toHome );

		Activity homeActivityInTheEvening = population.getFactory().createActivityFromCoord( "home", home );
		plan.addActivity( homeActivityInTheEvening );

		return plan;
	}

	private Coord getCoordInGeometry(Geometry regrion) {

		double x, y;
		Point point;
		Geometry selectedLandcover;

		// select a landcover feature and test whether it is in the right region. If not select a another one.
		do {
			selectedLandcover = landcover.sample(); //TODO: wie hier anzuwenden?
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
}