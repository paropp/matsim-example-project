package org.matsim.class2019.ber;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.PersonLeavesVehicleEvent;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.PersonLeavesVehicleEventHandler;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.VehicleArrivesAtFacilityEvent;
import org.matsim.core.api.experimental.events.VehicleDepartsAtFacilityEvent;
import org.matsim.core.api.experimental.events.handler.VehicleArrivesAtFacilityEventHandler;
import org.matsim.core.api.experimental.events.handler.VehicleDepartsAtFacilityEventHandler;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

import java.util.HashMap;
import java.util.Map;

public class PtEventHandler implements VehicleArrivesAtFacilityEventHandler, VehicleDepartsAtFacilityEventHandler, PersonEntersVehicleEventHandler, PersonLeavesVehicleEventHandler {

    private Map<String, VehicleOccupancy> vehiclesOccupancy = new HashMap<>();

    Map<String, VehicleOccupancy> getOccupancy() {
        return vehiclesOccupancy ;
    }

	@Override
	public void handleEvent( VehicleArrivesAtFacilityEvent event ) {
		
		Id<org.matsim.vehicles.Vehicle> vehicleId = event.getVehicleId() ;
		Id<TransitStopFacility> facilityId = event.getFacilityId() ;
		
		if( vehicleId.toString().contains( "SXF" ) ) {
			
			if( vehiclesOccupancy.containsKey( vehicleId.toString() ) ) {
				
				if( vehiclesOccupancy.get( vehicleId.toString() ).getStatus().contains( "enRoute" )) {
					vehiclesOccupancy.get( vehicleId.toString() ).setStatus( facilityId.toString() );
				} else {
					System.out.println( "should be enRoute. something went wrong. :_|" ) ;
				}
				
			} else {
				vehiclesOccupancy.put( vehicleId.toString() , new VehicleOccupancy( event.getTime(), facilityId.toString() )) ;
			}
			
		} else {}
	}
    
	@Override
	public void handleEvent( VehicleDepartsAtFacilityEvent event ) {
		
		Id<org.matsim.vehicles.Vehicle> vehicleId = event.getVehicleId() ;
		Id<TransitStopFacility> facilityId = event.getFacilityId() ;
		
		if( vehicleId.toString().contains( "SXF" ) ) {
			
			if( vehiclesOccupancy.get( vehicleId.toString() ).getStatus().contains( facilityId.toString() ) ) {
				
				vehiclesOccupancy.get( vehicleId.toString() ).setStatus( "enRoute" );
				
			} else {
				System.out.println( "Should depart from the same facility it entered. Something went wrong. :_|" );
			}
			
		} else {}
		
	}

	@Override
	public void handleEvent( PersonEntersVehicleEvent event ) {
		Id<org.matsim.vehicles.Vehicle> vehicleId = event.getVehicleId() ;
		Id<Person> personId = event.getPersonId() ;
		
		if( vehicleId.toString().contains( "SXF" ) ) {
			
			if( personId.toString().contains( "pt_vehicle" ) ) {
				// that is the driver
			} else {

				String vehicleStatus = vehiclesOccupancy.get( vehicleId.toString() ).getStatus() ;
				
				if( vehicleId.toString().contains( "ToSXF" ) ) {

					if( vehicleStatus.contains( "airport-express-stop-0" ) ) {
						
						vehiclesOccupancy.get( vehicleId.toString() ).personsOnFirstTrack.add( personId ) ;
						vehiclesOccupancy.get( vehicleId.toString() ).personsOnSecondTrack.add( personId ) ;
						
					} else if ( vehicleStatus.contains( "airport-express-stop-1" ) ) {
						
						vehiclesOccupancy.get( vehicleId.toString() ).personsOnFirstTrack.add( personId ) ;
						vehiclesOccupancy.get( vehicleId.toString() ).personsOnSecondTrack.add( personId ) ;
						
					} else if ( vehicleStatus.contains( "airport-express-stop-2" ) ) {
						
						vehiclesOccupancy.get( vehicleId.toString() ).personsOnSecondTrack.add( personId ) ;
						
					} else {}
					
				} else if ( vehicleId.toString().contains( "FromSXF" ) ) {
					
					
					if( vehicleStatus.contains( "airport-express-stop-00" ) ) {
						
						vehiclesOccupancy.get( vehicleId.toString() ).personsOnFirstTrack.add( personId ) ;
						vehiclesOccupancy.get( vehicleId.toString() ).personsOnSecondTrack.add( personId ) ;
						
					} else if ( vehicleStatus.contains( "airport-express-stop-4" ) ) {
						
						vehiclesOccupancy.get( vehicleId.toString() ).personsOnFirstTrack.add( personId ) ;
						vehiclesOccupancy.get( vehicleId.toString() ).personsOnSecondTrack.add( personId ) ;
						
					} else if ( vehicleStatus.contains( "airport-express-stop-5" ) ) {
						
						vehiclesOccupancy.get( vehicleId.toString() ).personsOnSecondTrack.add( personId ) ;
						
					} else {}

				} else {}
			}
			
		} else {}
		
		
	}

	@Override
	public void handleEvent( PersonLeavesVehicleEvent event ) {
		
		Id<org.matsim.vehicles.Vehicle> vehicleId = event.getVehicleId() ;
		Id<Person> personId = event.getPersonId() ;
		
		if( vehicleId.toString().contains( "SXF" ) ) {
			
			String vehicleStatus = vehiclesOccupancy.get( vehicleId.toString() ).getStatus() ;
			
			if( personId.toString().contains( "pt_vehicle" ) ) {
				// that is the driver
			} else {

				if( vehicleId.toString().contains( "ToSXF" ) ) {
					
					if( vehicleStatus.contains( "airport-express-stop-1" ) ) {
						
						vehiclesOccupancy.get( vehicleId.toString() ).personsOnFirstTrack.remove( personId ) ;
						vehiclesOccupancy.get( vehicleId.toString() ).personsOnSecondTrack.remove( personId ) ;
						
					} else if ( vehicleStatus.contains( "airport-express-stop-2" ) ) {
						
						vehiclesOccupancy.get( vehicleId.toString() ).personsOnSecondTrack.remove( personId ) ;
						
					} else {}
					
				} else if ( vehicleId.toString().contains( "FromSXF" ) ) {
					
					if( vehicleStatus.contains( "airport-express-stop-4" ) ) {
						
						vehiclesOccupancy.get( vehicleId.toString() ).personsOnFirstTrack.remove( personId ) ;
						vehiclesOccupancy.get( vehicleId.toString() ).personsOnSecondTrack.remove( personId ) ;
						
					} else if  ( vehicleStatus.contains( "airport-express-stop-5" ) ) {
						
						vehiclesOccupancy.get( vehicleId.toString() ).personsOnSecondTrack.remove( personId ) ;
						
					} else {}

				} else {}
			}
			
		} else {}
		
	}


}
