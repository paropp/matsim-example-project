package org.matsim.class2019.ber;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.class2019.pt.CreatePtManually;
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
	
	private static final Path BASE_PATH						=	Paths.get( "/home/misax/Documents/berlin-v5.3-10pct_BER/" ) ;
	private static final Path INPUT_PATH					=	BASE_PATH.resolve( "input" ) ;
	private static final Path OUTPUT_PATH					=	BASE_PATH.resolve( "edits" ) ;
	
	private static final Path CONFIG_FILE_PATH				=	INPUT_PATH.resolve( "berlin-v5.3-10pct.config.xml" ) ;
	
	private static final Path TRANSIT_SCHEDULE_PATH			=	INPUT_PATH.resolve( "berlin-v5-transit-schedule.xml.gz" ) ;
	private static final Path TRANSIT_VEHCILES_PATH			=	INPUT_PATH.resolve( "berlin-v5-transit-vehicles.xml.gz" ) ;
	private static final Path NETWORK_PATH					=	INPUT_PATH.resolve( "berlin-v5-network.xml.gz" ) ;

	private static final Path OUTPUT_NETWORK_PATH			=	OUTPUT_PATH.resolve( "berlin-v5-network.xml.gz" ) ;
	private static final Path OUTPUT_VEHICLES_PATH			=	OUTPUT_PATH.resolve( "berlin-v5-transit-vehicles.xml.gz") ;
	private static final Path OUTPUT_TRANSIT_SCHEDULE_PATH	=	OUTPUT_PATH.resolve( "berlin-v5-transit-schedule.xml.gz" ) ;


	public static void main(String[] args) {
		new CreateSuperTrain().run();
	}

	private void run() {

		Config config = ConfigUtils.createConfig() ;
		Scenario scenario = ScenarioUtils.createScenario( config ) ;

		// read in existing files
		new TransitScheduleReader( scenario ).readFile( TRANSIT_SCHEDULE_PATH.toString() ) ;
		new VehicleReaderV1( scenario.getTransitVehicles()).readFile( TRANSIT_VEHCILES_PATH.toString() ) ;
		new MatsimNetworkReader( scenario.getNetwork() ).readFile( NETWORK_PATH.toString() );

		// create some transit vehicle type
		VehicleType type = scenario.getTransitVehicles().getFactory().createVehicleType( Id.create( "airport-express",
				VehicleType.class ) ) ;
		type.setLength( 150 ) ;
		VehicleCapacity capacity = scenario.getTransitVehicles().getFactory().createVehicleCapacity() ;
		capacity.setSeats( 500 ) ;
		type.setCapacity( capacity ) ;
		type.setPcuEquivalents( 0 ) ;
		scenario.getTransitVehicles().addVehicleType( type ) ;

		// create vehicles for '10 service
		List<Vehicle> vehiclesToSxfList = new ArrayList<>();
		List<Vehicle> vehiclesFromSxfList = new ArrayList<>();
		for ( int i = 0; i < 132; i++ ) {
			
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
		Node stop1NodeHfb = scenario.getNetwork().getNodes().get( Id.createNodeId( "pt_000008011160" ) ) ; // Hbf
		Node stop2NodeSKreuz = scenario.getNetwork().getNodes().get( Id.createNodeId( "pt_000008011113" ) ) ; // Südkreuz
		Node stop3NodeSXF = scenario.getNetwork().getNodes().get( Id.createNodeId( "pt_000008010109" ) ) ; // SXF

		// connect nodes with links
		Link link1 = createLink( "hbf-skreuz", stop1NodeHfb, stop2NodeSKreuz, scenario.getNetwork().getFactory() ) ;
		Link link2 = createLink( "skreuz-sxf", stop2NodeSKreuz, stop3NodeSXF, scenario.getNetwork().getFactory() ) ;
		Link link3 = createLink( "skreuz-hbf", stop2NodeSKreuz, stop1NodeHfb, scenario.getNetwork().getFactory() ) ;
		Link link4 = createLink( "sxf-skreuz", stop3NodeSXF, stop2NodeSKreuz, scenario.getNetwork().getFactory() ) ;
		scenario.getNetwork().addLink( link1 ) ;
		scenario.getNetwork().addLink( link2 ) ;
		scenario.getNetwork().addLink( link3 ) ;
		scenario.getNetwork().addLink( link4 ) ;

		// create stops and add them to the scenario
		TransitStopFacility stop1 = scenario.getTransitSchedule().getFactory().createTransitStopFacility(
				Id.create( "airport-express-stop-1", TransitStopFacility.class), stop1NodeHfb.getCoord(), false ) ;
		stop1.setName( "Hbf-airport-express-toSXF" ) ;
		stop1.setLinkId( link1.getId() ) ;

		TransitStopFacility stop2 = scenario.getTransitSchedule().getFactory().createTransitStopFacility(
				Id.create( "airport-express-stop-2", TransitStopFacility.class), stop2NodeSKreuz.getCoord(), false ) ;
		stop2.setName( "SKreuz-super-train-toSXF" ) ;
		stop2.setLinkId( link1.getId() ) ;

		TransitStopFacility stop3 = scenario.getTransitSchedule().getFactory().createTransitStopFacility(
				Id.create( "airport-express-stop-3", TransitStopFacility.class), stop3NodeSXF.getCoord(), false );
		stop2.setName( "SXF-super-train-toSXF" ) ;
		stop3.setLinkId( link2.getId() ) ;
		
		TransitStopFacility stop4 = scenario.getTransitSchedule().getFactory().createTransitStopFacility(
				Id.create( "airport-express-stop-4", TransitStopFacility.class), stop1NodeHfb.getCoord(), false );
		stop1.setName( "SXF-airport-express-fromSXF" ) ;
		stop1.setLinkId( link4.getId() );

		TransitStopFacility stop5 = scenario.getTransitSchedule().getFactory().createTransitStopFacility(
				Id.create( "airport-express-stop-5", TransitStopFacility.class), stop2NodeSKreuz.getCoord(), false );
		stop2.setName( "SKreuz-super-train-fromSXF" ) ;
		stop2.setLinkId( link4.getId() ) ;

		TransitStopFacility stop6 = scenario.getTransitSchedule().getFactory().createTransitStopFacility(
				Id.create( "airport-express-stop-6", TransitStopFacility.class), stop3NodeSXF.getCoord(), false );
		stop2.setName( "Hbf-super-train-fromSXF" ) ;
		stop3.setLinkId( link3.getId() ) ;

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
				scenario.getTransitSchedule().getFactory().createTransitRouteStop( stop1, 0, 0 ),
				scenario.getTransitSchedule().getFactory().createTransitRouteStop( stop2, 120, 120 ),
				scenario.getTransitSchedule().getFactory().createTransitRouteStop( stop3, 420, 420 )
		) ;
		
		List<TransitRouteStop> transitStops2 = Arrays.asList(
				scenario.getTransitSchedule().getFactory().createTransitRouteStop( stop4, 0, 0 ),
				scenario.getTransitSchedule().getFactory().createTransitRouteStop( stop5, 300, 300 ),
				scenario.getTransitSchedule().getFactory().createTransitRouteStop( stop6, 420, 420 )
		) ;

		// create a transit route with the previously created transit stops.
		// also give it a network route with the two links our transit stops are located at
		TransitRoute transitRouteToSXF = scenario.getTransitSchedule().getFactory().createTransitRoute(
				Id.create( "super-train-line-1-route-0", TransitRoute.class ),
				RouteUtils.createNetworkRoute( Arrays.asList( link1.getId(), link2.getId()), scenario.getNetwork()),
				transitStops1,
				TransportMode.train
		);
		
		TransitRoute transitRouteFromSXF = scenario.getTransitSchedule().getFactory().createTransitRoute(
				Id.create( "super-train-line-1-route-1", TransitRoute.class ),
				RouteUtils.createNetworkRoute( Arrays.asList( link4.getId(), link3.getId()), scenario.getNetwork()),
				transitStops2,
				TransportMode.train
		);

		// departure every 10 minutes, because of peak fly statistics
		for ( int i = 0; i < 132; i++ ) {
			
			Departure departureTo = scenario.getTransitSchedule().getFactory().createDeparture(
					Id.create( "departureTo-"+i, Departure.class ), 10800 + i * 600
			);
			departureTo.setVehicleId( vehiclesToSxfList.get( i ).getId() );
			
			Departure departureFrom = scenario.getTransitSchedule().getFactory().createDeparture(
					Id.create( "departureFrom-"+i, Departure.class ), 10800 + i * 600
			);
			departureFrom.setVehicleId( vehiclesFromSxfList.get( i ).getId() );
			
			transitRouteToSXF.addDeparture( departureTo );
			transitRouteFromSXF.addDeparture( departureFrom );
		}
		
		//add the transit route to our line
		line.addRoute( transitRouteFromSXF );
		line.addRoute( transitRouteToSXF );

		// add the line to the scenario's schedule
		scenario.getTransitSchedule().addTransitLine( line );

		// write out the files
		new NetworkWriter( scenario.getNetwork()).write( OUTPUT_NETWORK_PATH.toString() );
		new VehicleWriterV1( scenario.getTransitVehicles()).writeFile( OUTPUT_VEHICLES_PATH.toString() );
		new TransitScheduleWriter( scenario.getTransitSchedule()).writeFile( OUTPUT_TRANSIT_SCHEDULE_PATH.toString() );
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
