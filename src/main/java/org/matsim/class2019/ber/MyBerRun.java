package org.matsim.class2019.ber;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ModeParams;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup.ModeRoutingParams;
import org.matsim.core.config.groups.StrategyConfigGroup.StrategySettings;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule.DefaultStrategy;
import org.matsim.core.scenario.ScenarioUtils;

public class MyBerRun {
	
	//constants for controler
	private static final Path BASE_PATH = Paths.get( "/home/misax/Documents/berlin-v5.3-10pct_BER/" ) ;
	private static final Path INPUT_PATH = BASE_PATH.resolve( "input" ) ;
	private static final Path EDITS_PATH = BASE_PATH.resolve( "edits" ) ;
	
	//constants for global
	private static final int NUMBER_OF_ITERATIONS = 500 ;
	private static final int WRITE_INTERVAL = 100 ;
	private static final String COORDINATE_SYSTEM = "GK4" ;
	private static final int NUMBER_OF_THREADS = 2;
	private static final long RANDOM_SEED = 2342 ;
	
	//constants plansCalcRoute
	private static final Collection<String> NETWORK_MODES = Arrays.asList("car", "freight", "ride");
	



	public static void main( String[] args ) {
		String configfile = "./scenarios/equil/config.xml" ;
		Config config = ConfigUtils.loadConfig( configfile ) ;
		
		config.controler().setLastIteration( NUMBER_OF_ITERATIONS );
		config.controler().setOverwriteFileSetting( OverwriteFileSetting.overwriteExistingFiles ) ;
		config.controler().setRunId( "berlin-v5.3-10pct-BER-superTrain" );
		config.controler().setOutputDirectory( "./output" ) ;
		config.controler().setWriteEventsInterval( WRITE_INTERVAL ) ;
		config.controler().setWritePlansInterval( WRITE_INTERVAL ) ;
		
		config.global().setCoordinateSystem( COORDINATE_SYSTEM ) ;
		config.global().setInsistingOnDeprecatedConfigVersion( false ) ;
		config.global().setNumberOfThreads( NUMBER_OF_THREADS );
		config.global().setRandomSeed( RANDOM_SEED );
		
		config.network().setInputFile( EDITS_PATH.resolve( "" ).toString() ) ;
		
		config.plans().setInputPersonAttributeFile( inputPersonAttributeFile ) ;
		config.plans().setInputFile( inputFile ) ;
		config.plans().setRemovingUnneccessaryPlanAttributes( removingUnneccessaryPlanAttributes ) ;
		
		config.vehicles().setVehiclesFile( str ) ;
		
		config.transit().setTransitScheduleFile( filename ) ;
		config.transit().setUseTransit( val ) ;
		config.transit().setVehiclesFile( filename ) ;
		
		config.plansCalcRoute().setNetworkModes( NETWORK_MODES );
		config.plansCalcRoute().

		
//		// new mode
//		StrategySettings stratSets = new StrategySettings() ;
//		stratSets.setStrategyName( DefaultStrategy.ChangeSingleTripMode ) ;
//		stratSets.setWeight( 1 ) ;
//		config.strategy().addStrategySettings( stratSets ) ;
//
//		ModeRoutingParams pars = new ModeRoutingParams( "scooter" ) ;
//		pars.setBeelineDistanceFactor( 1.5 ) ;
//		pars.setTeleportedModeSpeed( 6.2 ) ;
//
//		config.plansCalcRoute().addModeRoutingParams( pars );
//		
//		ModeParams params = new ModeParams( "scooter" ) ;
//		params.setConstant( 4.0 ) ;
//		params.setMarginalUtilityOfTraveling( 0. ) ;
//		params.setMarginalUtilityOfDistance( -0.1 ) ;
//		config.planCalcScore().addModeParams( params ) ;
//		
//		String[] modes = { "car", "scooter" } ;
//		config.changeMode().setModes( modes ) ;
		
		Scenario scenario = ScenarioUtils.loadScenario( config ) ;
		Controler controler = new Controler( scenario ) ;
		controler.run() ;
	}

}
