package org.matsim.class2019.ber;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.matsim.api.core.v01.Scenario;
import org.matsim.class2019.network.Link2Add;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ModeParams;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup.ModeRoutingParams;
import org.matsim.core.config.groups.QSimConfigGroup.TrafficDynamics;
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
	
	//constants for qsim
	private static final String START_TIME = "00:00:00" ;
	private static final String END_TIME = "36:00:00" ;
	private static final double CAPACITY_FACTOR = 0.1 ;
	private static final double STUCK_TIME = 30 ;
	//TODO: ask why, when:
	//investigations have shown that the simulations become,
	//in comparison to traffic counts data,
	//less realistic when this parameter is increased
	private static final TrafficDynamics TRAFFIC_DYNAMICS = TrafficDynamics.kinematicWaves ;
	private static final boolean isInsertingWaitingVehiclesBeforeDrivingVehicles = true ;
	
	//constants strategy
	private static final double FRACTION_TO_DISABLE_INNOVATION = 0.8 ;

	


	public static void main( String[] args ) {
		
		String configfile = "./scenarios/equil/config.xml" ;
		Config config = ConfigUtils.loadConfig( configfile ) ;
		
		config.timeAllocationMutator().setMutationRange( 7200.0 );
		
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
		
		config.vehicles().setVehiclesFile( str  ) ;
		
		config.transit().setTransitScheduleFile( filename ) ;
		config.transit().setUseTransit( val ) ;
		config.transit().setVehiclesFile( filename ) ;
		
		config.plansCalcRoute().setNetworkModes( NETWORK_MODES );
		//TODO: finish
		
		config.qsim().setStartTime( START_TIME );
		config.qsim().setEndTime( END_TIME ) ;
		config.qsim().setFlowCapFactor( CAPACITY_FACTOR ) ;
		config.qsim().setStorageCapFactor(CAPACITY_FACTOR) ;
		config.qsim().setMainModes( mainModes ) ;
		//config.qsim().setNumberOfThreads( numberOfThreads );
		config.qsim().setStuckTime( STUCK_TIME ) ;
		config.qsim().setTrafficDynamics( TRAFFIC_DYNAMICS ) ;
		//config.qsim().setVehiclesSource( "modeVehicleTypesFromVehiclesData" );
		config.qsim().setInsertingWaitingVehiclesBeforeDrivingVehicles( isInsertingWaitingVehiclesBeforeDrivingVehicles );
		//TODO: finish
		
		//config.strategy().setMaxAgentPlanMemorySize( maxAgentPlanMemorySize ) ;
		//TODO: finish
		config.strategy().setFractionOfIterationsToDisableInnovation( FRACTION_TO_DISABLE_INNOVATION );
		
		StrategySettings stratSets = new StrategySettings() ;
		
		stratSets.setStrategyName( "ChangeExpBeta" ) ;
		stratSets.setWeight( 0.85 ) ;
		stratSets.setSubpopulation( "person" );
		config.strategy().addStrategySettings( stratSets ) ;
		
		stratSets.setStrategyName( "ReRoute" ) ;
		stratSets.setWeight( 0.05 ) ;
		stratSets.setSubpopulation( "person" );
		config.strategy().addStrategySettings( stratSets ) ;
		
		stratSets.setStrategyName( "SubtourModeChoice" ) ;
		stratSets.setWeight( 0.05 ) ;
		stratSets.setSubpopulation( "person" );
		config.strategy().addStrategySettings( stratSets ) ;
		
		stratSets.setStrategyName( "TimeAllocationMutator" ) ;
		stratSets.setWeight( 0.05 ) ;
		stratSets.setSubpopulation( "person" );
		config.strategy().addStrategySettings( stratSets ) ;
		
		stratSets.setStrategyName( "ChangeExpBeta" ) ;
		stratSets.setWeight( 0.95 ) ;
		stratSets.setSubpopulation( "freight" );
		config.strategy().addStrategySettings( stratSets ) ;
		
		stratSets.setStrategyName( "ReRoute" ) ;
		stratSets.setWeight( 0.05 ) ;
		stratSets.setSubpopulation( "freight" );
		config.strategy().addStrategySettings( stratSets ) ;

		
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
