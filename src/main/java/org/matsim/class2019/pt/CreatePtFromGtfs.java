package org.matsim.class2019.pt;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.contrib.gtfs.RunGTFS2MATSim;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.pt.transitSchedule.api.TransitScheduleReader;
import org.matsim.pt.transitSchedule.api.TransitScheduleWriter;
import org.matsim.pt.utils.CreatePseudoNetwork;
import org.matsim.pt.utils.CreateVehiclesForSchedule;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleWriterV1;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

public class CreatePtFromGtfs {


	private static Path gtfsFeed = Paths.get("/home/misax/Documents/Uni/Master/Matsim/erfurt-with-pt/VMT_GTFS.zip");
	private static Path network = Paths.get("/home/misax/Documents/Uni/Master/Matsim/erfurt-with-pt/network-without-pt.xml.gz");
	private static Path outputFolder = Paths.get("/home/misax/Documents/Uni/Master/Matsim/erfurt-with-pt/output/");


	public static void main(String[] args) {
		new CreatePtFromGtfs().createPt();
	}

	private void createPt() {

		CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, "EPSG:25832");
		LocalDate date = LocalDate.parse("2019-06-05");

		//output files
		String scheduleFile = outputFolder.resolve("transitSchedule.xml.gz").toString();
		String networkFile = outputFolder.resolve("network.xml.gz").toString();
		String transitVehiclesFile = outputFolder.resolve("transitVehicles.xml.gz").toString();

		//Convert GTFS
		RunGTFS2MATSim.convertGtfs(gtfsFeed.toString(), scheduleFile, date, ct, false);

		//Parse the schedule again
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new TransitScheduleReader(scenario).readFile(scheduleFile);

		//if neccessary, parse in an existing network file here:
		new MatsimNetworkReader(scenario.getNetwork()).readFile(network.toString());

		//Create a network around the schedule
		new CreatePseudoNetwork(scenario.getTransitSchedule(), scenario.getNetwork(), "pt_").createNetwork();

		//Create simple transit vehicles
		new CreateVehiclesForSchedule(scenario.getTransitSchedule(), scenario.getTransitVehicles()).run();
		
		for( VehicleType type : scenario.getTransitVehicles().getVehicleTypes().values() ) {
			type.setPcuEquivalents( 0 );
		}
	
		
		//Write out network, vehicles and schedule
		new NetworkWriter(scenario.getNetwork()).write(networkFile);
		new TransitScheduleWriter(scenario.getTransitSchedule()).writeFile(scheduleFile);
		new VehicleWriterV1(scenario.getTransitVehicles()).writeFile(transitVehiclesFile);
	}
}