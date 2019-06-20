package org.matsim.class2019.demand;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationWriter;

import java.nio.file.Paths;
import java.util.logging.Logger;

public class RunCreateDemand {

	private static final Logger logger = Logger.getLogger("RunCreateDemand");

	public static void main(String[] args) {

		InputArguments input = new InputArguments();
		JCommander.newBuilder().addObject(input).build().parse(args);

		CreateDemand creator = new CreateDemand(Paths.get(input.commuters), Paths.get(input.workforce),
				Paths.get(input.regionsShape), Paths.get(input.landcoverShape));
		creator.create();
		Population result = creator.getPopulation();

		logger.info("Writing " + result.getPersons().size() + " persons to: " + input.output);
		new PopulationWriter(result).write(Paths.get(input.output).toString());
	}

	private static class InputArguments {

		@Parameter(names = "-commuters", required = true)
		private String commuters;

		@Parameter(names = "-workforce", required = true)
		private String workforce;

		@Parameter(names = "-regionShape", required = true)
		private String regionsShape;

		@Parameter(names = "-landcoverShape", required = true)
		private String landcoverShape;

		@Parameter(names = "-output", required = true)
		private String output;
	}
}
