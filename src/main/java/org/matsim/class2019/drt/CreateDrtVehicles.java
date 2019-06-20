package org.matsim.class2019.drt;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.fleet.DvrpVehicleSpecification;
import org.matsim.contrib.dvrp.fleet.FleetWriter;
import org.matsim.contrib.dvrp.fleet.ImmutableDvrpVehicleSpecification;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

public class CreateDrtVehicles {

	private static final int numberOfVehicles = 1500;
	private static final int seatsPerVehicle = 6;
	private static final double operationStartTime = 0;
	private static final double operationEndTime = 24 * 60 * 60; //24h

	private static final Random random = new Random(0);

	private final Path networkFile;
	private final Path outputFile;

	CreateDrtVehicles(Path networkFile, Path outputFile) {
		this.networkFile = networkFile;
		this.outputFile = outputFile;
	}

	public static void main(String[] args) {

		if (args.length != 2) {
			throw new IllegalArgumentException("you have to supply 2 args: path/to/your/network path/to/your/output/file");
		}
		Path networkFile = Paths.get(args[0]);
		Path outputFile = Paths.get(args[1]);

		new CreateDrtVehicles(networkFile, outputFile).run();
	}

	void run() {

		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(scenario.getNetwork()).readFile(networkFile.toString());
		Stream<DvrpVehicleSpecification> vehicleSpecificationStream = scenario.getNetwork().getLinks().entrySet().stream()
				.filter(entry -> entry.getValue().getAllowedModes().contains(TransportMode.car)) // drt can only start on links with Transport mode 'car'
				.sorted((e1, e2) -> (random.nextInt(2) - 1)) // shuffle links
				.limit(numberOfVehicles) // select the first *numberOfVehicles* links
				.map(entry -> ImmutableDvrpVehicleSpecification.newBuilder()
						.id(Id.create("drt_" + UUID.randomUUID().toString(), DvrpVehicle.class))
						.startLinkId(entry.getKey())
						.capacity(seatsPerVehicle)
						.serviceBeginTime(operationStartTime)
						.serviceEndTime(operationEndTime)
						.build());

		new FleetWriter(vehicleSpecificationStream).write(outputFile.toString());
	}
}
