package org.matsim.class2019.ber;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.TransitDriverStartsEvent;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.vehicles.Vehicle;

import java.util.HashSet;
import java.util.Set;

public class AgentTravelledOnLinkEventHandler implements PersonEntersVehicleEventHandler {

	private final Set<Id<Link>> linksToWatch ;
	private final Set<Id<Person>> personOnWatchedLinks = new HashSet<>() ;

	AgentTravelledOnLinkEventHandler(Set<Id<Link>> linksToWatch) {
		this.linksToWatch = linksToWatch ;
	}

	Set<Id<Person>> getPersonOnWatchedLinks() {
		return personOnWatchedLinks ;
	}
	
    public void handleEvent(PersonEntersVehicleEvent personEntersVehicleEvent) {
    	
    	Id<Vehicle> vehicleId = personEntersVehicleEvent.getVehicleId() ;
    	Id<Person> personId = Id.createPersonId( personEntersVehicleEvent.getPersonId() ) ;
    	
    	//for persons
    	if( vehicleId.toString().contains( "vehicle" ) ) {
    		if( !personId.toString().contains( "pt_" ) ) {
    			if( personOnWatchedLinks.contains( personId ) ) {
    			} else {
    				personOnWatchedLinks.add( personId ) ;
    			}
    		}
    	}
    }
}
