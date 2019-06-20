package org.matsim.class2019.pt;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.population.Person;

import java.util.HashSet;
import java.util.Set;

public class PtLegEventHandler implements ActivityEndEventHandler, PersonDepartureEventHandler {

    private Set<Id<Person>> personsOnRoute = new HashSet<>();
    private int ptTrips = 0;

    int getPtTrips() {
        return ptTrips;
    }

    @Override
    public void handleEvent( ActivityEndEvent activityEndEvent ) {

        if ( isMainActivity( activityEndEvent.getActType() ) ) {
            personsOnRoute.add( activityEndEvent.getPersonId() );
        }
    }

    @Override
    public void handleEvent( PersonDepartureEvent personDepartureEvent ) {

        if ( personsOnRoute.contains( personDepartureEvent.getPersonId() ) ) {
            personsOnRoute.remove( personDepartureEvent.getPersonId() );
            if ( personDepartureEvent.getLegMode().equals( "access_walk" ) ) {
                ptTrips++;
            }
        }
    }

    private boolean isMainActivity(String type) {
        return type.equals("home") || type.equals("work");
    }
}
