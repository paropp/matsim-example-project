package org.matsim.class2019.pt;

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
import org.matsim.pt.transitSchedule.api.*;
import org.matsim.vehicles.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class CreatePtManually {

	private static final Path transitSchedule = Paths.get("/home/misax/Documents/Uni/Master/Matsim/erfurt-with-pt/output/transitSchedule.xml.gz");
	private static final Path transitVehicles = Paths.get("/home/misax/Documents/Uni/Master/Matsim/erfurt-with-pt/output/transitVehicles.xml.gz");
	private static final Path networkPath = Paths.get("/home/misax/Documents/Uni/Master/Matsim/erfurt-with-pt/output/network.xml.gz");

	private static final Path outputNetwork = Paths.get("/home/misax/Documents/Uni/Master/Matsim/erfurt-with-pt/output/super-train-network.xml.gz");
	private static final Path outputVehicles = Paths.get("/home/misax/Documents/Uni/Master/Matsim/erfurt-with-pt/output/super-train-transitVehicles.xml.gz");
	private static final Path outputTransitSchedule = Paths.get("/home/misax/Documents/Uni/Master/Matsim/erfurt-with-pt/output/super-train-transitSchedule.xml.gz");


	public static void main(String[] args) {
		new CreatePtManually().create();
	}

	private void create() {

		Config config = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(config);

		// read in existing files
		new TransitScheduleReader(scenario).readFile(transitSchedule.toString());
		new VehicleReaderV1(scenario.getTransitVehicles()).readFile(transitVehicles.toString());
		new MatsimNetworkReader(scenario.getNetwork()).readFile(networkPath.toString());

		// create some transit vehicle type
		VehicleType type = scenario.getTransitVehicles().getFactory().createVehicleType(Id.create("super-train", VehicleType.class));
		type.setLength(400);
		VehicleCapacity capacity = scenario.getTransitVehicles().getFactory().createVehicleCapacity();
		capacity.setSeats(800);
		type.setCapacity(capacity);
		type.setPcuEquivalents(0);
		scenario.getTransitVehicles().addVehicleType(type);

		// create two vehicles
		Vehicle vehicle1 = scenario.getTransitVehicles().getFactory().createVehicle(Id.createVehicleId("vehicle-1"), type);
		Vehicle vehicle2 = scenario.getTransitVehicles().getFactory().createVehicle(Id.createVehicleId("vehicle-2"), type);
		scenario.getTransitVehicles().addVehicle(vehicle1);
		scenario.getTransitVehicles().addVehicle(vehicle2);

		// get the existing nodes we want to connect
		Node stop1Node = scenario.getNetwork().getNodes().get(Id.createNodeId("pt_000008010366"));
		Node stop2Node = scenario.getNetwork().getNodes().get(Id.createNodeId("pt_000008010101"));
		Node stop3Node = scenario.getNetwork().getNodes().get(Id.createNodeId("pt_000000167205"));

		// connect nodes with links
		Link link1 = createLink("stop1-stop2", stop1Node, stop2Node, scenario.getNetwork().getFactory());
		Link link2 = createLink("stop2-stop3", stop2Node, stop3Node, scenario.getNetwork().getFactory());
		scenario.getNetwork().addLink(link1);
		scenario.getNetwork().addLink(link2);

		// create stops and add them to the scenario
		TransitStopFacility stop1 = scenario.getTransitSchedule().getFactory().createTransitStopFacility(
				Id.create("super-train-stop-1", TransitStopFacility.class), stop1Node.getCoord(), false);
		stop1.setName("weimar-super-train");
		stop1.setLinkId(link1.getId());

		TransitStopFacility stop2 = scenario.getTransitSchedule().getFactory().createTransitStopFacility(
				Id.create("super-train-stop-2", TransitStopFacility.class), stop2Node.getCoord(), false);
		stop2.setName("erfurt-super-train");
		stop2.setLinkId(link1.getId());

		TransitStopFacility stop3 = scenario.getTransitSchedule().getFactory().createTransitStopFacility(
				Id.create("super-train-stop-3", TransitStopFacility.class), stop3Node.getCoord(), false);
		stop2.setName("gotha-super-train");
		stop3.setLinkId(link2.getId());

		scenario.getTransitSchedule().addStopFacility(stop1);
		scenario.getTransitSchedule().addStopFacility(stop2);
		scenario.getTransitSchedule().addStopFacility(stop3);

		// create a line
		TransitLine line = scenario.getTransitSchedule().getFactory().createTransitLine(Id.create("line1", TransitLine.class));
		line.setName("super-train-line-1");

		//create transit route stops
		List<TransitRouteStop> transitStops = Arrays.asList(
				scenario.getTransitSchedule().getFactory().createTransitRouteStop(stop1, 0, 0),
				scenario.getTransitSchedule().getFactory().createTransitRouteStop(stop2, 300, 300),
				scenario.getTransitSchedule().getFactory().createTransitRouteStop(stop3, 600, 600)
		);

		// create a transit route with the previously created transit stops.
		// also give it a network route with the two links our transit stops are located at
		TransitRoute transitRoute = scenario.getTransitSchedule().getFactory().createTransitRoute(
				Id.create("super-train-line-1-route-0", TransitRoute.class),
				RouteUtils.createNetworkRoute(Arrays.asList(link1.getId(), link2.getId()), scenario.getNetwork()),
				transitStops,
				TransportMode.train
		);

		// create and add two departures for the transit route
		Departure departure1 = scenario.getTransitSchedule().getFactory().createDeparture(
				Id.create("departure-1", Departure.class), 0
		);
		departure1.setVehicleId(vehicle1.getId());
		Departure departure2 = scenario.getTransitSchedule().getFactory().createDeparture(
				Id.create("departure-2", Departure.class), 1800
		);
		departure2.setVehicleId(vehicle2.getId());
		transitRoute.addDeparture(departure1);
		transitRoute.addDeparture(departure2);

		//add the transit route to our line
		line.addRoute(transitRoute);

		// add the line to the scenario's schedule
		scenario.getTransitSchedule().addTransitLine(line);

		// write out the files
		new NetworkWriter(scenario.getNetwork()).write(outputNetwork.toString());
		new VehicleWriterV1(scenario.getTransitVehicles()).writeFile(outputVehicles.toString());
		new TransitScheduleWriter(scenario.getTransitSchedule()).writeFile(outputTransitSchedule.toString());
	}

	private Link createLink(String id, Node from, Node to, NetworkFactory factory) {
		Link link = factory.createLink(Id.createLinkId(id), from, to);
		link.setAllowedModes(new HashSet<>(Collections.singletonList(TransportMode.pt)));
		link.setCapacity(999999);
		link.setLength(NetworkUtils.getEuclideanDistance(link.getFromNode().getCoord(), link.getToNode().getCoord()));
		link.setFreespeed(83.3);
		return link;
	}
}
