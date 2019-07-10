package org.matsim.class2019.ber;

/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2017 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

import static org.matsim.core.config.groups.ControlerConfigGroup.RoutingAlgorithmType.FastAStarLandmarks;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.analysis.ScoreStats;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Population;
import org.matsim.codeexamples.population.demandGenerationFromShapefile.CreateDemand;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.QSimConfigGroup.TrafficDynamics;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryLogging;
import org.matsim.core.scenario.ScenarioUtils;

import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorModule;

/**
* @author ikaddoura
*/

public class RunBer {

	private static final Logger log = Logger.getLogger(RunBer.class);

	private final String configFileName;
	private final String overridingConfigFileName;
	private Config config;
	private Scenario scenario;
	private Controler controler;
	
	private boolean hasPreparedConfig = false ;
	private boolean hasPreparedScenario = false ;
	private boolean hasPreparedControler = false ;
	
	//TODO: change all to downloaded output directory

	private static final Path BASE_PATH						=	Paths.get( "/home/misax/Documents/berlin-v5.3-10pct_BER/" ) ;
	private static final Path INPUT_PATH					=	BASE_PATH.resolve( "input" ) ;
	private static final Path OUTPUT_PATH					=	BASE_PATH.resolve( "edits" ) ;
	
	private static final Path CONFIG_FILE_PATH				=	INPUT_PATH.resolve( "berlin-v5.3-10pct.config.xml" ) ;
	
	private static final Path ARR_DEP_SEATS_PATH				=	BASE_PATH.resolve( "seats-data.txt" ) ;
	private static final Path TRANSIT_SCHEDULE_PATH			=	INPUT_PATH.resolve( "berlin-v5-transit-schedule.xml.gz" ) ;
	private static final Path TRANSIT_VEHCILES_PATH			=	INPUT_PATH.resolve( "berlin-v5-transit-vehicles.xml.gz" ) ;
	private static final Path NETWORK_PATH					=	INPUT_PATH.resolve( "berlin-v5-network.xml.gz" ) ;
	private static final Path PLANS_PATH					=	INPUT_PATH.resolve( "berlin-v5.3-10pct.plans.xml.gz" ) ;

	private static final Path OUTPUT_NETWORK_PATH			=	OUTPUT_PATH.resolve( "berlin-v5-network.xml.gz" ) ;
	private static final Path OUTPUT_VEHICLES_PATH			=	OUTPUT_PATH.resolve( "berlin-v5-transit-vehicles.xml.gz") ;
	private static final Path OUTPUT_TRANSIT_SCHEDULE_PATH	=	OUTPUT_PATH.resolve( "berlin-v5-transit-schedule.xml.gz" ) ;
	private static final Path OUTPUT_PLANS_PATH				=	OUTPUT_PATH.resolve( "berlin-v5.3-10pct.plans.xml.gz" ) ;
	
	private final static int NUMBER_OF_TRAVELERS_TOTAL = 1000;
	
	
	
	public static void main(String[] args) {
		
		//TODO: reduce (more)
		String configFileName ;
		String overridingConfigFileName = null;
		
		configFileName = CONFIG_FILE_PATH.toString() ;
		overridingConfigFileName = "overridingConfig.xml";
		
		log.info( "config file: " + configFileName );
		
		//////////
		// HERE
		//////////
		new CreateSuperTrain().run(
				TRANSIT_SCHEDULE_PATH,
				TRANSIT_VEHCILES_PATH,
				NETWORK_PATH,
				OUTPUT_TRANSIT_SCHEDULE_PATH,
				OUTPUT_VEHICLES_PATH,
				OUTPUT_NETWORK_PATH
				);
		
		//TODO: pfade übergeben
		new CreateBerDemand().create(
				PLANS_PATH,
				OUTPUT_PLANS_PATH,
				ARR_DEP_SEATS_PATH
				);
		new RunBer( configFileName, overridingConfigFileName ).run() ;
	}
	
	public RunBer( String configFileName, String overridingConfigFileName) {
		this.configFileName = configFileName;
		this.overridingConfigFileName = overridingConfigFileName;
	}

	public Controler prepareControler( AbstractModule... overridingModules ) {
		if ( !hasPreparedScenario ) {
			prepareScenario() ;
		}
		
		controler = new Controler( scenario );
		
		if (controler.getConfig().transit().isUsingTransitInMobsim()) {
			// use the sbb pt raptor router
			controler.addOverridingModule( new AbstractModule() {
				@Override
				public void install() {
					install( new SwissRailRaptorModule() );
				}
			} );
		} else {
			log.warn("Public transit will be teleported and not simulated in the mobsim! "
					+ "This will have a significant effect on pt-related parameters (travel times, modal split, and so on). "
					+ "Should only be used for testing or car-focused studies with fixed modal split.  ");
		}
		
		// use the (congested) car travel time for the teleported ride mode
		controler.addOverridingModule( new AbstractModule() {
			@Override
			public void install() {
				addTravelTimeBinding( TransportMode.ride ).to( networkTravelTime() );
				addTravelDisutilityFactoryBinding( TransportMode.ride ).to( carTravelDisutilityFactoryKey() );
			}
		} );
		
		for ( AbstractModule overridingModule : overridingModules ) {
			controler.addOverridingModule( overridingModule );
		}
		
		hasPreparedControler = true ;
		return controler;
	}
	
	public Scenario prepareScenario() {
		if ( !hasPreparedConfig ) {
			prepareConfig( ) ;
		}
		
		// so that config settings in code, which come after the settings from the initial config file, can
		// be overridden without having to change the jar file.  Normally empty.
		if (this.overridingConfigFileName==null || this.overridingConfigFileName=="null" || this.overridingConfigFileName=="") {
			// do not load overriding config
		} else {
			ConfigUtils.loadConfig( config, this.overridingConfigFileName );	
		}
		// note that the path for this is different when run from GUI (path of original config) vs.
		// when run from command line/IDE (java root).  :-(    See comment in method.  kai, jul'18
		
		scenario = ScenarioUtils.loadScenario( config );

		hasPreparedScenario = true ;
		return scenario;
	}
	
	public Config prepareConfig(ConfigGroup... customModules) {
		OutputDirectoryLogging.catchLogEntries();
		
		config = ConfigUtils.loadConfig( configFileName, customModules ) ; // I need this to set the context
		//////
		//HERE
		//////
		config.controler().setRoutingAlgorithmType( FastAStarLandmarks );
		
		config.subtourModeChoice().setProbaForRandomSingleTripMode( 0.5 );
		
		config.plansCalcRoute().setRoutingRandomness( 3. );
		
		config.qsim().setInsertingWaitingVehiclesBeforeDrivingVehicles( true );
		
		// vsp defaults
		config.plansCalcRoute().setInsertingAccessEgressWalk( true );
		config.qsim().setUsingTravelTimeCheckInTeleportation( true );
		config.qsim().setTrafficDynamics( TrafficDynamics.kinematicWaves );
		
		//////////
		// EDITS
		//////////
		//cause files referenced in config were changed
		config.network().setInputFile( OUTPUT_NETWORK_PATH.toString() );
		config.transit().setTransitScheduleFile( OUTPUT_TRANSIT_SCHEDULE_PATH.toString() );
		config.transit().setVehiclesFile( OUTPUT_VEHICLES_PATH.toString() );
		
		// activities:
		for ( long ii = 600 ; ii <= 97200; ii+=600 ) {
			final ActivityParams params = new ActivityParams( "home_" + ii + ".0" ) ;
			params.setTypicalDuration( ii );
			config.planCalcScore().addActivityParams( params );
		}
		for ( long ii = 600 ; ii <= 97200; ii+=600 ) {
			final ActivityParams params = new ActivityParams( "work_" + ii + ".0" ) ;
			params.setTypicalDuration( ii );
			params.setOpeningTime(6. * 3600.);
			params.setClosingTime(20. * 3600.);
			config.planCalcScore().addActivityParams( params );
		}
		for ( long ii = 600 ; ii <= 97200; ii+=600 ) {
			final ActivityParams params = new ActivityParams( "leisure_" + ii + ".0" ) ;
			params.setTypicalDuration( ii );
			params.setOpeningTime(9. * 3600.);
			params.setClosingTime(27. * 3600.);
			config.planCalcScore().addActivityParams( params );
		}
		for ( long ii = 600 ; ii <= 97200; ii+=600 ) {
			final ActivityParams params = new ActivityParams( "shopping_" + ii + ".0" ) ;
			params.setTypicalDuration( ii );
			params.setOpeningTime(8. * 3600.);
			params.setClosingTime(20. * 3600.);
			config.planCalcScore().addActivityParams( params );
		}
		for ( long ii = 600 ; ii <= 97200; ii+=600 ) {
			final ActivityParams params = new ActivityParams( "other_" + ii + ".0" ) ;
			params.setTypicalDuration( ii );
			config.planCalcScore().addActivityParams( params );
		}
		{
			final ActivityParams params = new ActivityParams( "freight" ) ;
			params.setTypicalDuration( 12.*3600. );
			config.planCalcScore().addActivityParams( params );
		}
		
		hasPreparedConfig = true ;
		return config ;
	}
	
	 public void run() {
		if ( !hasPreparedControler ) {
			prepareControler() ;
		}
		controler.run();
		log.info("Done.");
	}
	
	final ScoreStats getScoreStats() {
		return controler.getScoreStats() ;
	}
	
	final Population getPopulation() {
		return controler.getScenario().getPopulation();
	}

}


