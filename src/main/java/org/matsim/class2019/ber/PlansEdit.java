package org.matsim.class2019.ber;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.locationtech.jts.geom.Geometry;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.class2019.basics.Rectangle;
import org.matsim.class2019.network.ReduceModes;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.population.io.PopulationWriter;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.core.utils.io.OsmNetworkReader;
import org.matsim.utils.objectattributes.attributable.Attributes;
import org.opengis.feature.simple.SimpleFeature;

public class PlansEdit {
	

	private static final String inputPath = "/home/misax/Documents/berlin-v5.3-10pct_BER/input/" ;
	private static final String outputPath = "/home/misax/Documents/berlin-v5.3-10pct_BER/edits/" ;
	
	private static final String configFile = inputPath + "berlin-v5.3-10pct.config.xml" ;
	private static final String inputNetwork = inputPath + "berlin-v5-network.xml.gz" ;
	
	private static final String outputNetwork = outputPath + "berlin-v5-network-edit.xml.gz" ;
	private static final String outputPlansFile = outputPath + "plans-edit.xml" ;
	
	private static final String filterShapeFile = inputPath + "shape.shp" ;


	private static final String[] nodesTegel = new String[]{
			"637767071",
			"150552644",
			"150552645",
			"150554455",
			"150554467",
			"28205955",
			"28205956",
			"28205961",
			"28205962",
			"28205964",
			"28205966",
			"28205968",
			"28205972",
			"28205975",
			"28205980",
			"28205998",
			"28206002",
			"28206004",
			"660680951",
			"660690616",
			"745108110",
			"745108242",
			"pt_070101000378",
			"pt_070101000726",
			"135530593",
			"28206050",
			"28206056",
			"28206078",
			"28206081",
			"28206082",
			"28206084",
			"28246285",
			"28246286",
			"428699367",
			"428699463",
			"428699464",
			"428699832",
			"95056459",
			"pt_070101000460",
			"pt_070101000514",
			"pt_070101000525",
			"pt_070101000573",
	} ;
	
	public static void main(String[] args) throws IOException {
		new PlansEdit().run() ;
	}	
	
	public void run() throws IOException {	
				
		List<Geometry> geometries = new ArrayList<Geometry>() ;
		
		for (SimpleFeature feature : ShapeFileReader.getAllFeatures(filterShapeFile.toString())) {
				geometries.add((Geometry) feature.getDefaultGeometry());
			}
		
		
		
		Network network = NetworkUtils.createNetwork() ;
		new MatsimNetworkReader(network).readFile( inputNetwork.toString() ) ;
		
        Config config = ConfigUtils.loadConfig( configFile ) ;
        
        Scenario scenario = ScenarioUtils.loadScenario( config ) ;
        
        Population pop = scenario.getPopulation() ;
        
        //TODO: edit
        Set< Id<Person> > persons2Remove = new HashSet<>() ;
		
        for ( Person person : pop.getPersons().values() ) {
        	
        	for ( Plan plan : person.getPlans() ) {
        		
        		for ( PlanElement element : plan.getPlanElements() ) {
        			
        			if ( isActivityElement( element ) ) {
        				
        				Activity act = ( Activity ) element;
    					String typeOfStartActivity = act.getType().toString() ;
						
						if ( containsCoord( act.getCoord(), geometries) ) {
							 //nach BER verlegen
												
						}
        				
        			} else {}
        			
        		}
        	}

        }
        
        new PopulationWriter( pop ).write( outputPlansFile.toString() ) ;
	}

	//because I don't know how to check for instaceOf Leg or Activity
	private boolean isActivityElement(PlanElement el) {
		return el.getClass().toString().contains( "ActivityImpl" ) ;
	}
	
	private boolean isLegElement(PlanElement el) {
		return el.getClass().toString().contains( "LegImpl" ) ;
	}
	
	private boolean includesLinks2Watch(Leg leg, String[] links) {
		
		boolean isIncluded = false;
    	for(String link: links2Watch) {
    		
    		String route = leg.getRoute().toString();
    		if( route.contains( link ) ) {
    			isIncluded = true;
    			break;
    		} else {}
    	}
    	return isIncluded ;

	}
	

	private boolean containsCoord(Coord coord, Collection<Geometry> geometries) {
		return geometries.stream().anyMatch(geom -> geom.contains(MGC.coord2Point(coord)));
	}
	
	private static class NetworkFilter implements OsmNetworkReader.OsmFilter {

		private final Collection<Geometry> geometries = new ArrayList<>();

		NetworkFilter(Path shapeFile) {
			for (SimpleFeature feature : ShapeFileReader.getAllFeatures(shapeFile.toString())) {
				geometries.add((Geometry) feature.getDefaultGeometry());
			}
		}

		@Override
		public boolean coordInFilter(Coord coord, int hierarchyLevel) {
			// hierachy levels 1 - 3 are motorways and primary roads, as well as their trunks
			if (hierarchyLevel <= 4) return true;

			// if coord is within the supplied shape use every street above level of tracks and cycle ways
			return hierarchyLevel <= 8 && containsCoord(coord);
		}

		private boolean containsCoord(Coord coord) {
			return geometries.stream().anyMatch(geom -> geom.contains(MGC.coord2Point(coord)));
		}
	

}
