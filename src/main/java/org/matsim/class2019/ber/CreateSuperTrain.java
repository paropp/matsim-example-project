package org.matsim.class2019.ber;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.Departure;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.pt.transitSchedule.api.TransitScheduleReader;
import org.matsim.pt.transitSchedule.api.TransitScheduleWriter;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleCapacity;
import org.matsim.vehicles.VehicleReaderV1;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleWriterV1;

public class CreateSuperTrain {

	protected void run(
			Path transitShedule,
			Path transitVehicles,
			Path network,
			Path transitSheduleOutput,
			Path transitVehiclesOutput,
			Path networkOutput,
			Path trainTiming ) {

		Config config = ConfigUtils.createConfig() ;
		Scenario scenario = ScenarioUtils.createScenario( config ) ;

		// read in existing files
		new TransitScheduleReader( scenario ).readFile( transitShedule.toString() ) ;
		new VehicleReaderV1( scenario.getTransitVehicles()).readFile( transitVehicles.toString() ) ;
		new MatsimNetworkReader( scenario.getNetwork() ).readFile( network.toString() );

		// create some transit vehicle type
		VehicleType type = scenario.getTransitVehicles().getFactory().createVehicleType( Id.create( "airport-express",	VehicleType.class ) ) ;
		type.setLength( 150 ) ;
		VehicleCapacity capacity = scenario.getTransitVehicles().getFactory().createVehicleCapacity() ;
		capacity.setSeats( 5 ) ; // so 500 at 100%
		type.setCapacity( capacity ) ;
		type.setPcuEquivalents( 0 ) ;
		scenario.getTransitVehicles().addVehicleType( type ) ;

		// create vehicles corresponding to needs, see .csv
		List<Vehicle> vehiclesToSxfList = new ArrayList<>();
		List<Vehicle> vehiclesFromSxfList = new ArrayList<>();
		for ( int i = 0; i < 143; i++ ) {
			
			Vehicle vehicleTo = scenario.getTransitVehicles().getFactory().createVehicle(
					Id.createVehicleId( "vehicleToSXF-"+i ), type ) ;
			Vehicle vehicleFrom = scenario.getTransitVehicles().getFactory().createVehicle(
					Id.createVehicleId( "vehicleFromSXF-"+i ), type ) ;
			
			scenario.getTransitVehicles().addVehicle( vehicleTo ) ;
			scenario.getTransitVehicles().addVehicle( vehicleFrom ) ;
			vehiclesToSxfList.add( vehicleTo ) ;
			vehiclesFromSxfList.add( vehicleFrom ) ;
		}		

		// get the existing nodes we want to connect
		Node stop0Node	= scenario.getNetwork().getNodes().get( Id.createNodeId( "1267954718" ) ) ; // dummy
		Node stop00Node	= scenario.getNetwork().getNodes().get( Id.createNodeId( "pt_070101007092" ) ) ; // dummy
		Node nodeHbf	= scenario.getNetwork().getNodes().get( Id.createNodeId( "pt_000008011160" ) ) ; // Hbf
		Node nodeKreuz	= scenario.getNetwork().getNodes().get( Id.createNodeId( "pt_000008011113" ) ) ; // Südkreuz
		Node nodeSXF	= scenario.getNetwork().getNodes().get( Id.createNodeId( "pt_000008010109" ) ) ; // SXF

		// connect nodes with links
		Link link_dummy_to	= createLink( "dummyTo",   stop0Node,	nodeHbf,	scenario.getNetwork().getFactory() ) ;
		Link link_Hbf_Kreuz = createLink( "hbf-kreuz", nodeHbf,		nodeKreuz,	scenario.getNetwork().getFactory() ) ;
		Link link_Kreuz_SXF = createLink( "kreuz-sxf", nodeKreuz,	nodeSXF,	scenario.getNetwork().getFactory() ) ;
		
		Link link_dummy_from = createLink( "dummyFrom", stop00Node,	nodeSXF,	scenario.getNetwork().getFactory() ) ;
		Link link_SXF_Kreuz  = createLink( "sxf-kreuz", nodeSXF,	nodeKreuz,	scenario.getNetwork().getFactory() ) ;
		Link link_Kreuz_Hbf  = createLink( "kreuz-hbf", nodeKreuz,	nodeHbf,	scenario.getNetwork().getFactory() ) ;
		
		scenario.getNetwork().addLink( link_dummy_to ) ;
		scenario.getNetwork().addLink( link_dummy_from ) ;
		
		scenario.getNetwork().addLink( link_Hbf_Kreuz ) ;
		scenario.getNetwork().addLink( link_Kreuz_SXF ) ;

		scenario.getNetwork().addLink( link_SXF_Kreuz ) ;
		scenario.getNetwork().addLink( link_Kreuz_Hbf ) ;

		// create stops and add them to the scenario
		
		TransitStopFacility stop0 = scenario.getTransitSchedule().getFactory().createTransitStopFacility(
				Id.create( "airport-express-stop-0",TransitStopFacility.class), stop0Node.getCoord(), false ) ;
		stop0.setName( "dummy-airport-express-toSXF" ) ;
		stop0.setLinkId( link_dummy_to.getId() ) ;
		
		TransitStopFacility stop00 = scenario.getTransitSchedule().getFactory().createTransitStopFacility(
				Id.create( "airport-express-stop-00",TransitStopFacility.class), stop00Node.getCoord(), false ) ;
		stop00.setName( "dummy-airport-express-fromSXF" ) ;
		stop00.setLinkId( link_dummy_from.getId() ) ;
		
		TransitStopFacility stop1 = scenario.getTransitSchedule().getFactory().createTransitStopFacility(
				Id.create( "airport-express-stop-1",TransitStopFacility.class), nodeHbf.getCoord(), false ) ;
		stop1.setName( "Hbf-airport-express-toSXF" ) ;
		stop1.setLinkId( link_Hbf_Kreuz.getId() ) ;

		TransitStopFacility stop2 = scenario.getTransitSchedule().getFactory().createTransitStopFacility(
				Id.create( "airport-express-stop-2", TransitStopFacility.class), nodeKreuz.getCoord(), false ) ;
		stop2.setName( "Kreuz-super-train-toSXF" ) ;
		stop2.setLinkId( link_Hbf_Kreuz.getId() ) ;

		TransitStopFacility stop3 = scenario.getTransitSchedule().getFactory().createTransitStopFacility(
				Id.create( "airport-express-stop-3", TransitStopFacility.class), nodeSXF.getCoord(), false );
		stop3.setName( "SXF-super-train-toSXF" ) ;
		stop3.setLinkId( link_Kreuz_SXF.getId() ) ;
		
		TransitStopFacility stop4 = scenario.getTransitSchedule().getFactory().createTransitStopFacility(
				Id.create( "airport-express-stop-4", TransitStopFacility.class), nodeSXF.getCoord(), false );
		stop4.setName( "SXF-super-train-fromSXF" ) ;
		stop4.setLinkId( link_SXF_Kreuz.getId() ) ;
		
		TransitStopFacility stop5 = scenario.getTransitSchedule().getFactory().createTransitStopFacility(
				Id.create( "airport-express-stop-5", TransitStopFacility.class), nodeKreuz.getCoord(), false );
		stop5.setName( "Kreuz-super-train-fromSXF" ) ;
		stop5.setLinkId( link_Kreuz_Hbf.getId() ) ;
		
		TransitStopFacility stop6 = scenario.getTransitSchedule().getFactory().createTransitStopFacility(
				Id.create( "airport-express-stop-6", TransitStopFacility.class), nodeHbf.getCoord(), false );
		stop6.setName( "Hbf-airport-express-fromSXF" ) ;
		stop6.setLinkId( link_Kreuz_Hbf.getId() );

		scenario.getTransitSchedule().addStopFacility( stop0 ) ;
		scenario.getTransitSchedule().addStopFacility( stop00 ) ;
		scenario.getTransitSchedule().addStopFacility( stop1 ) ;
		scenario.getTransitSchedule().addStopFacility( stop2 ) ;
		scenario.getTransitSchedule().addStopFacility( stop3 ) ;
		scenario.getTransitSchedule().addStopFacility( stop4 ) ;
		scenario.getTransitSchedule().addStopFacility( stop5 ) ;
		scenario.getTransitSchedule().addStopFacility( stop6 ) ;

		// create a line
		TransitLine line = scenario.getTransitSchedule().getFactory().createTransitLine( Id.create( "line1", TransitLine.class ) ) ;
		line.setName( "super-train-line-1" ) ;

		//create transit route stops
		List<TransitRouteStop> transitStops1 = Arrays.asList(
				scenario.getTransitSchedule().getFactory().createTransitRouteStop( stop0, 0, 0 ),
				scenario.getTransitSchedule().getFactory().createTransitRouteStop( stop1, 1, 1 ),
				scenario.getTransitSchedule().getFactory().createTransitRouteStop( stop2, 70, 70 ), // ca. 300 km/h
				scenario.getTransitSchedule().getFactory().createTransitRouteStop( stop3, 250, 250 ) // ca. 300 km/h
				//scenario.getTransitSchedule().getFactory().createTransitRouteStop( stop2, 120, 120 ), // ca. 160 kmh/
				//scenario.getTransitSchedule().getFactory().createTransitRouteStop( stop3, 420, 420 ) // ca. 160 kmh/
		) ;
		
		List<TransitRouteStop> transitStops2 = Arrays.asList(
				scenario.getTransitSchedule().getFactory().createTransitRouteStop( stop00, 0, 0 ),
				scenario.getTransitSchedule().getFactory().createTransitRouteStop( stop4, 1, 1 ),
				scenario.getTransitSchedule().getFactory().createTransitRouteStop( stop5, 180, 180 ), // ca. 300 km/h
				scenario.getTransitSchedule().getFactory().createTransitRouteStop( stop6, 250, 250 ) // ca. 300 km/h
				//scenario.getTransitSchedule().getFactory().createTransitRouteStop( stop5, 300, 300 ), // ca. 160 kmh/
				//scenario.getTransitSchedule().getFactory().createTransitRouteStop( stop6, 420, 420 ) // ca. 160 km/h
		) ;

		// create a transit route with the previously created transit stops.
		// also give it a network route with the two links our transit stops are located at
		TransitRoute transitRouteToSXF = scenario.getTransitSchedule().getFactory().createTransitRoute(
				Id.create( "super-train-line-1-route-0", TransitRoute.class ),
				RouteUtils.createNetworkRoute( Arrays.asList( link_dummy_to.getId(), link_Hbf_Kreuz.getId(), link_Kreuz_SXF.getId()), scenario.getNetwork()),
				transitStops1,
				TransportMode.train
		);
		
		TransitRoute transitRouteFromSXF = scenario.getTransitSchedule().getFactory().createTransitRoute(
				Id.create( "super-train-line-1-route-1", TransitRoute.class ),
				RouteUtils.createNetworkRoute( Arrays.asList( link_dummy_from.getId(), link_SXF_Kreuz.getId(), link_Kreuz_Hbf.getId()), scenario.getNetwork()),
				transitStops2,
				TransportMode.train
		);

		// create departures every XX minutes
		//TODO: RETHINK
		
		try (CSVParser parser = CSVParser.parse(trainTiming, StandardCharsets.UTF_8, CSVFormat.newFormat(';').withFirstRecordAsHeader())) {
			
			//to get the same iterator of vehicles
			int trainsCreatedForDep = 0 ;
			int trainsCreatedForArr = 0 ;
			
			for( CSVRecord record : parser ) {
				
				int timeBin		= Integer.parseInt( record.get( "timeBin" ) ) - 1;
				int timingDep	= Integer.parseInt( record.get( "timingDep" ) ) ;
				int timingArr	= Integer.parseInt( record.get( "timingArr" ) ) ;
				
				//so they can reach Airport and return home at the end of the day
				if( ( timeBin == 0 ) || ( timeBin == 1 ) ) timeBin += 24 ;
				
				boolean hasDep = ( timingDep != 0 ) ;
				boolean hasArr = ( timingArr != 0 ) ;
				
				if( hasDep ) {
					
					int numOfTrainsForDep = 60 / timingDep ;
					
					if( ( 60 % timingDep != 0 ) ) {
						System.out.println("Timing results in non usable number of Trains. Mudolo 60 has to be 0.") ;
						break ;
					}
					
					for( int ii = 0; ii < numOfTrainsForDep; ii++ ) {
						
						Departure departureTo = scenario.getTransitSchedule().getFactory().createDeparture(
								Id.create( "depToSXF_" + timeBin + "_" + trainsCreatedForDep, Departure.class ),
								( ( 60 * 60 ) * timeBin ) + ii * ( timingDep * 60 )
						);
						departureTo.setVehicleId( vehiclesToSxfList.get( trainsCreatedForDep ).getId() ) ;
						transitRouteToSXF.addDeparture( departureTo ) ;
						System.out.println(  "Created Train depToSXF_" + timeBin + "_" + trainsCreatedForDep ) ;
						trainsCreatedForDep++ ;
					}

				}
				
				if( hasArr ) {
					
					int numOfTrainsForArr = 60 / timingArr ;
					
					if( ( 60 % timingArr != 0 ) ) {
						System.out.println("Timing results in non usable number of Trains. Mudolo 60 has to be 0.") ;
						break ;
					}
					
					for( int ii = 0; ii < numOfTrainsForArr; ii++ ) {
						
						Departure departureFrom = scenario.getTransitSchedule().getFactory().createDeparture(
								Id.create( "depFromSXF_" + timeBin + "_" + trainsCreatedForArr, Departure.class ),
								( ( 60 * 60 ) * timeBin ) + ii * ( timingArr * 60 )
						);
						departureFrom.setVehicleId( vehiclesFromSxfList.get( trainsCreatedForArr ).getId() ) ;
						transitRouteFromSXF.addDeparture( departureFrom ) ;
						System.out.println(  "Created Train depFromSXF_" + timeBin + "_" + trainsCreatedForArr ) ;
						trainsCreatedForArr++ ;
					}

				}

			}

		} catch ( IOException e ) {
			e.printStackTrace();
		}
		
		//add the transit route to our line
		line.addRoute( transitRouteFromSXF );
		line.addRoute( transitRouteToSXF ) ;

		// add the line to the scenario's schedule
		scenario.getTransitSchedule().addTransitLine( line );

		// write out the files
		new NetworkWriter( scenario.getNetwork()).write( networkOutput.toString() );
		new VehicleWriterV1( scenario.getTransitVehicles()).writeFile( transitVehiclesOutput.toString() );
		new TransitScheduleWriter( scenario.getTransitSchedule()).writeFile( transitSheduleOutput.toString() );
	}

	private Link createLink( String id, Node from, Node to, NetworkFactory factory ) {
		Link link = factory.createLink( Id.createLinkId(id), from, to ) ;
		link.setAllowedModes( new HashSet<>( Collections.singletonList( TransportMode.pt ) ) ) ;
		link.setCapacity( 999999 );
		link.setLength(NetworkUtils.getEuclideanDistance( link.getFromNode().getCoord(), link.getToNode().getCoord()) ) ;
		link.setFreespeed( 83.3333333333 ) ; // 44.4444444444
		return link;
	}
}
