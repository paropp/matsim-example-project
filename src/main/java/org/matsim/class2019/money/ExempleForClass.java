package org.matsim.class2019.money;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.mobsim.framework.AbstractMobsimModule;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.scoring.SumScoringFunction;
import org.matsim.core.scoring.functions.CharyparNagelActivityScoring;
import org.matsim.core.scoring.functions.CharyparNagelLegScoring;
import org.matsim.core.scoring.functions.ScoringParameters;
import org.matsim.core.scoring.functions.ScoringParametersForPerson;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.examples.ExamplesUtils;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

public final class ExempleForClass{

	private Config config = null ;

	public static void main ( String [] args ) {
		new ExempleForClass().run() ;
	}

	public final Config prepareConfig() {
		config = ConfigUtils.loadConfig( IOUtils.newUrl( ExamplesUtils.getTestScenarioURL( "equil" ), "config.xml" ) ) ;
		return config ;
	}

	void run() {
		if ( config==null ) {
			prepareConfig() ;
		}

		Scenario scenario = ScenarioUtils.loadScenario( config );

		Controler controler = new Controler( scenario );
		
		for( Person person :  )

		controler.run() ;
		
		org.matsim.core.controler.AbstractModule abstractModule = new org.matsim.core.controler.AbstractModule() {
			
			@Override
			public void install() {
				// TODO Auto-generated method stub
				ScoringFunctionFactory instance = new ScoringFunctionFactory() {
					
					@Inject private ScoringParametersForPerson params;
					@Inject private Network network;
					
					@Override
					public ScoringFunction createNewScoringFunction(Person person) {
						// TODO Auto-generated method stub
						final ScoringParameters parameters = params.getScoringParameters(person);
						
						SumScoringFunction sumScoringFuntion = new SumScoringFunction();
						sumScoringFuntion.addScoringFunction(new CharyparNagelActivityScoring(parameters));
						sumScoringFuntion.addScoringFunction(new CharyparNagelLegScoring(params, network));
						
						
						
						
						return null;
					}
				};
			}
		};

	}

}
