package org.matsim.class2019.ber;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.class2019.pt.PtEventHandler;
import org.matsim.class2019.pt.PtLegEventHandler;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.utils.io.IOUtils;

public class CalcOccupancy {
	
	private static final Logger logger = Logger.getLogger("CalcOccupancy");
	
	private Path eventsPath = Paths.get("/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_BER/output_policy_160/berlin-v5.3-1pct.output_events.xml.gz");
	private Path occupancyPath = Paths.get("/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_BER/output_policy_160/vehiclesOccupancy.txt");
	
    public static void main(String[] args) {
    	new CalcOccupancy().run();
    }
	
	void run() {
		
        org.matsim.class2019.ber.PtEventHandler handler = new org.matsim.class2019.ber.PtEventHandler();
        EventsManager manager = EventsUtils.createEventsManager();

        manager.addHandler( handler );
        new MatsimEventsReader( manager ).readFile( eventsPath.toString() );

		BufferedWriter bwOccupancy = IOUtils.getBufferedWriter( occupancyPath.toString() );
		try {
			bwOccupancy.write( "vehicle,time,OccupancyOnFirstTrack,OccupancyOnSecondTrack," );
			bwOccupancy.newLine();
			
			for (Entry<String, VehicleOccupancy> vehicle : handler.getOccupancy().entrySet()){
				
				String key = vehicle.getKey() ;
				VehicleOccupancy value = vehicle.getValue() ;
				
				bwOccupancy.write( key + "," + value.getTime() + "," + value.personsOnFirstTrack.size() + "," + value.personsOnSecondTrack.size() + ",");
				bwOccupancy.newLine();
			}
			bwOccupancy.flush();
			bwOccupancy.close();
		}
		catch (IOException e ){
		}

		logger.info("Done.");
	}
}
