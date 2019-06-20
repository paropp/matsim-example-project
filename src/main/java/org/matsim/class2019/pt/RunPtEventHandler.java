package org.matsim.class2019.pt;

import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;

import java.nio.file.Paths;

public class RunPtEventHandler {

    public static void main(String[] args) {

        PtEventHandler handler = new PtEventHandler();
        PtLegEventHandler legEventHandler = new PtLegEventHandler();
        EventsManager manager = EventsUtils.createEventsManager();

        manager.addHandler( handler );
        manager.addHandler( legEventHandler );
        new MatsimEventsReader( manager ).readFile(Paths.get( "/home/misax/Documents/Uni/Master/Matsim/erfurt-with-pt/50.events.xml.gz" ).toString());


        System.out.println("Number of tours: " + handler.getNumberOfTours());
        System.out.println("Number of pt-legs: " + legEventHandler.getPtTrips());
    }
}
