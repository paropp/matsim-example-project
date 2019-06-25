package org.matsim.class2019.ber;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.class2019.pt.CreatePtManually;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
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

public class MyClass {
	
	private static final String[] nodesTegel = new String[]{
			"637767071",
			"150552644",
			"150552645",
			"150554455",
			"150554467",
			"28205955",
			"28205956",
			"28205961",
			"28205962",
			"28205964",
			"28205966",
			"28205968",
			"28205972",
			"28205975",
			"28205980",
			"28205998",
			"28206002",
			"28206004",
			"660680951",
			"660690616",
			"745108110",
			"745108242",
			"pt_070101000378",
			"pt_070101000726",
			"135530593",
			"28206050",
			"28206056",
			"28206078",
			"28206081",
			"28206082",
			"28206084",
			"28246285",
			"28246286",
			"428699367",
			"428699463",
			"428699464",
			"428699832",
			"95056459",
			"pt_070101000460",
			"pt_070101000514",
			"pt_070101000525",
			"pt_070101000573",
	} ;
	private static final String nodeBer = "pt_160000014391" ;
	
	private static final String inputPath = "/home/misax/Documents/berlin-v5.3-10pct_BER/input/" ;
	private static final String outputPath = "/home/misax/Documents/berlin-v5.3-10pct_BER/edits/" ;
	
	private static final String configFile = inputPath + "berlin-v5.3-10pct.config.xml" ;
	
	private static final Path transitSchedule = Paths.get( inputPath + "berlin-v5-transit-schedule.xml.gz" ) ;
	private static final Path transitVehicles = Paths.get( inputPath + "berlin-v5-transit-vehicles.xml.gz" ) ;
	private static final Path networkPath = Paths.get( inputPath + "berlin-v5-network.xml.gz" );

	private static final Path outputNetwork = Paths.get( outputPath + "networkEdit.xml.gz" );
	private static final Path outputVehicles = Paths.get( outputPath + "transitVehiclesEdit.xml.gz");
	private static final Path outputTransitSchedule = Paths.get( outputPath + "transitScheduleEdit.xml.gz" ) ;

	
	public static void main(String[] args) {
		new MyClass().create() ;
	}

	private void create() {

		Config config = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario( config ) ;

		// read in existing files
		new TransitScheduleReader( scenario ).readFile( transitSchedule.toString() ) ;
		new VehicleReaderV1( scenario.getTransitVehicles()) .readFile( transitVehicles.toString() );
		
		new MatsimNetworkReader( scenario.getNetwork() ).readFile( networkPath.toString() ) ;
		
		//Controler controler = new Controler( scenario );

		// create some transit vehicle type
//		VehicleType type = scenario.getTransitVehicles().getFactory().createVehicleType(Id.create("super-train", VehicleType.class));
//		type.setLength(400);
//		VehicleCapacity capacity = scenario.getTransitVehicles().getFactory().createVehicleCapacity();
//		capacity.setSeats(800);
//		type.setCapacity(capacity);
//		type.setPcuEquivalents(0);
//		scenario.getTransitVehicles().addVehicleType(type);

		// create two vehicles
//		Vehicle vehicle1 = scenario.getTransitVehicles().getFactory().createVehicle(Id.createVehicleId("vehicle-1"), type);
//		Vehicle vehicle2 = scenario.getTransitVehicles().getFactory().createVehicle(Id.createVehicleId("vehicle-2"), type);
//		scenario.getTransitVehicles().addVehicle(vehicle1);
//		scenario.getTransitVehicles().addVehicle(vehicle2);
		
		// create node for wassmannsdorf	
		Node wdorf  = scenario.getNetwork().getFactory().createNode( Id.createNodeId( "pt_99999999" ),
				new Coord( 4599779.119325, 5804882.542085 ) ) ;
		
		scenario.getNetwork().addNode( wdorf );
		

		// get the existing nodes we want to connect
		Node sxf = scenario.getNetwork().getNodes().get( Id.createNodeId( "pt_060260005671" ) ) ;
		Node ber = scenario.getNetwork().getNodes().get( Id.createNodeId( nodeBer ) ) ;
		//Node sxf = scenario.getNetwork().getNodes().get( Id.createNodeId( "3386901041" ) ) ;
		//Node ber = scenario.getNetwork().getNodes().get( Id.createNodeId( "29786490" ) ) ;
		
		System.out.println( sxf.getId().toString()) ;		
		// connect nodes with links
		Link link1 = createLink( "sxf-wdorf", sxf, wdorf, scenario.getNetwork().getFactory(), 4150 ) ;
		Link link2 = createLink( "wdorf-sxf", wdorf, sxf, scenario.getNetwork().getFactory(), 4150 ) ;
		Link link3 = createLink( "ber-wdorf", ber, wdorf, scenario.getNetwork().getFactory(), 3500 ) ;
		Link link4 = createLink( "wdorf-ber", wdorf, ber, scenario.getNetwork().getFactory(), 3500 ) ;
		
		scenario.getNetwork().addLink( link1 ) ;
		scenario.getNetwork().addLink( link2 ) ;
		scenario.getNetwork().addLink( link3 ) ;
		scenario.getNetwork().addLink( link4 ) ;

		// create stops and add them to the scenario
		TransitStopFacility stop1 = scenario.getTransitSchedule().getFactory().createTransitStopFacility(
				Id.create( "sbahn-plus-stop-toBer-1", TransitStopFacility.class), wdorf.getCoord(), false ) ;
		stop1.setName( "wdorf-sbahn-toBer" );
		stop1.setLinkId( link1.getId() );

		TransitStopFacility stop2 = scenario.getTransitSchedule().getFactory().createTransitStopFacility(
				Id.create("sbahn-plus-stop-toBer-2", TransitStopFacility.class), ber.getCoord(), false);
		stop2.setName( "ber-sbahn-toBer" );
		stop2.setLinkId( link4.getId() );

		TransitStopFacility stop3 = scenario.getTransitSchedule().getFactory().createTransitStopFacility(
				Id.create( "sbahn-plus-stop-fromBer-1" , TransitStopFacility.class), ber.getCoord(), false);
		stop3.setName( "ber-sbahn-fromBer" );
		stop3.setLinkId( link3.getId() );
		
		TransitStopFacility stop4 = scenario.getTransitSchedule().getFactory().createTransitStopFacility(
				Id.create( "sbahn-plus-stop-fromBer-2" , TransitStopFacility.class), wdorf.getCoord(), false);
		stop4.setName( "wdorf-sbahn-fromBer" );
		stop4.setLinkId( link3.getId() );

		scenario.getTransitSchedule().addStopFacility( stop1 ) ;
		scenario.getTransitSchedule().addStopFacility( stop2 ) ;
		scenario.getTransitSchedule().addStopFacility( stop3 ) ;
		scenario.getTransitSchedule().addStopFacility( stop4 ) ;

		// create a line
		//TransitLine line = scenario.getTransitSchedule().getFactory().createTransitLine(Id.create("line1", TransitLine.class));
		//line.setName("super-train-line-1");

//		//create transit route stops
//		List<TransitRouteStop> transitStops = Arrays.asList(
//				scenario.getTransitSchedule().getFactory().createTransitRouteStop(stop1, 0, 0),
//				scenario.getTransitSchedule().getFactory().createTransitRouteStop(stop2, 300, 300),
//				scenario.getTransitSchedule().getFactory().createTransitRouteStop(stop3, 600, 600)
//		);
//
//		// create a transit route with the previously created transit stops.
//		// also give it a network route with the two links our transit stops are located at
//		TransitRoute transitRoute = scenario.getTransitSchedule().getFactory().createTransitRoute(
//				Id.create("super-train-line-1-route-0", TransitRoute.class),
//				RouteUtils.createNetworkRoute(Arrays.asList(link1.getId(), link2.getId()), scenario.getNetwork()),
//				transitStops,
//				TransportMode.train
//		);
//
//		// create and add two departures for the transit route
//		Departure departure1 = scenario.getTransitSchedule().getFactory().createDeparture(
//				Id.create("departure-1", Departure.class), 0
//		);
//		departure1.setVehicleId(vehicle1.getId());
//		Departure departure2 = scenario.getTransitSchedule().getFactory().createDeparture(
//				Id.create("departure-2", Departure.class), 1800
//		);
//		departure2.setVehicleId(vehicle2.getId());
//		transitRoute.addDeparture(departure1);
//		transitRoute.addDeparture(departure2);
//
//		//add the transit route to our line
//		line.addRoute(transitRoute);
//
//		// add the line to the scenario's schedule
//		scenario.getTransitSchedule().addTransitLine(line);

		// write out the files
		new NetworkWriter( scenario.getNetwork()) .write( outputNetwork.toString() );
		new VehicleWriterV1( scenario.getTransitVehicles() ).writeFile( outputVehicles.toString() );
		new TransitScheduleWriter( scenario.getTransitSchedule()).writeFile( outputTransitSchedule.toString() );
	}
	
	private Link createLink( String id, Node from,Node to, NetworkFactory factory, double length ) {
		
		Link link = factory.createLink( Id.createLinkId(id), from, to ) ;
		link.setAllowedModes( new HashSet<>( Collections.singletonList( TransportMode.pt) ) ) ;
		link.setCapacity( 500 ) ;
		//link.setLength( NetworkUtils.getEuclideanDistance( link.getFromNode().getCoord(), link.getToNode().getCoord() ) ) ;
		link.setLength( length );
		link.setFreespeed( 8.333334 );
		return link;
	}

}
