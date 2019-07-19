package org.matsim.class2019.berBeta;

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
	
	
}