package org.matsim.class2019.pt;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.PersonLeavesVehicleEvent;
import org.matsim.api.core.v01.events.TransitDriverStartsEvent;
import org.matsim.api.core.v01.events.handler.PersonLeavesVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.TransitDriverStartsEventHandler;
import org.matsim.api.core.v01.population.Person;

import java.util.HashSet;
import java.util.Set;

public class PtEventHandler implements TransitDriverStartsEventHandler, PersonLeavesVehicleEventHandler {

    private Set<Id<Person>> transitDrivers = new HashSet<>();
    private int numberOfTours = 0;

    int getNumberOfTours() {
        return numberOfTours;
    }

    @Override
    public void handleEvent(TransitDriverStartsEvent transitDriverStartsEvent) {
        transitDrivers.add(transitDriverStartsEvent.getDriverId());
    }

    @Override
    public void handleEvent(PersonLeavesVehicleEvent personLeavesVehicleEvent) {

        if (transitDrivers.contains(personLeavesVehicleEvent.getPersonId())) {
            numberOfTours++;
        }
    }
}
