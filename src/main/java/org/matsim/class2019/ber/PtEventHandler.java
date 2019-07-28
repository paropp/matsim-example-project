package org.matsim.class2019.ber;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.population.Person;
import org.matsim.class2019.basics.Rectangle;
import org.matsim.core.api.experimental.events.VehicleArrivesAtFacilityEvent;
import org.matsim.core.api.experimental.events.VehicleDepartsAtFacilityEvent;
import org.matsim.core.api.experimental.events.handler.VehicleArrivesAtFacilityEventHandler;
import org.matsim.core.api.experimental.events.handler.VehicleDepartsAtFacilityEventHandler;
import org.matsim.core.mobsim.jdeqsim.Vehicle;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PtEventHandler implements VehicleArrivesAtFacilityEventHandler, PersonEntersVehicleEventHandler, VehicleDepartsAtFacilityEventHandler {

    private Map<String, VehicleOccupancy> vehiclesOccupancy = new HashMap<>();

    Map<String, VehicleOccupancy> getOccupancy() {
        return vehiclesOccupancy ;
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
            personsOnRoute.remove( personDepartureEvent.getPersonId() ) ;
            if ( personDepartureEvent.getLegMode().equals( "access_walk" ) ) {
                ptTrips++;
            }
        }
    }

    private boolean isMainActivity(String type) {
        return type.equals("home") || type.equals("work");
    }

	@Override
	public void handleEvent( VehicleArrivesAtFacilityEvent event ) {
		
		Id<org.matsim.vehicles.Vehicle> vehicleId = event.getVehicleId() ;
		Id<TransitStopFacility> facilityId = event.getFacilityId() ;
		
		if( vehicleId.toString().contains( "SXF" ) ) {
			
			if( !facilityId.toString().contains( "airport-express-stop-0" ) ) {
				
				if( vehiclesOccupancy.containsKey( event.getVehicleId().toString() ) ) {
					
					if( vehicleId.toString().contains( "FromSXF" ) ) {
						
					} else if ( vehicleId.toString().contains( "ToSXF" ) ) {
						
					} else {}
					
				} else {
					vehiclesOccupancy.put( vehicleId.toString() , new VehicleOccupancy( event.getTime() )) ;
				}
				
			} else {}
			
		} else {}
	}
    
	@Override
	public void handleEvent(VehicleDepartsAtFacilityEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleEvent(PersonEntersVehicleEvent event) {
		// TODO Auto-generated method stub
		
	}


}
