package org.matsim.project;

import org.matsim.contrib.av.robotaxi.fares.drt.DrtFareModule;
import org.matsim.contrib.av.robotaxi.fares.drt.DrtFaresConfigGroup;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.run.DrtControlerCreator;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.gbl.Gbl;

public class RunDrtScenario {

	public static void main(String[] args) {
		
		Gbl.assertIf( args.length >= 1 && !args[0].equals("") );
		run( ConfigUtils.loadConfig(args[0], new DrtConfigGroup(), new DvrpConfigGroup(), new DrtFaresConfigGroup()) );
		// makes some sense to not modify the config here but in the run method to help  with regression testing.
	}

	static void run( Config config ) {

		Controler controler = DrtControlerCreator.createControlerWithSingleModeDrt( config, false );
		
		DrtControlerCreator.addDrtRouteFactory( controler.getScenario() );
		
		controler.addOverridingModule(new DrtFareModule());
		controler.getConfig().plansCalcRoute().setInsertingAccessEgressWalk( true );
		
		controler.run();
	}
}
