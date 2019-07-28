package org.matsim.class2019.ber;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import org.matsim.api.core.v01.population.Population;
import org.matsim.class2019.pt.PtEventHandler;
import org.matsim.class2019.pt.PtLegEventHandler;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.population.PopulationUtils;

public class CalcOccupancy {
	
	private static final Logger logger = Logger.getLogger("CalcOccupancy");
	
	private Path eventsPath = Paths.get("/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_BER/output_policy_160/berlin-v5.3-1pct.output_events.xml.gz");
	
    public static void main(String[] args) {
    	new CalcOccupancy().run();
    }
	
	void run() {
		
        PtEventHandler handler = new PtEventHandler();
        EventsManager manager = EventsUtils.createEventsManager();

        manager.addHandler( handler );
        new MatsimEventsReader( manager ).readFile( eventsPath.toString() );


		logger.info("Done.");
	}
}
