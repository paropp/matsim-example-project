package org.matsim.class2019.ber;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.io.PopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;

public class BerTripAnalysis {
	

	
	private static Path inputNetwork = Paths.get(	"/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_BER/output_policy_160/berlin-v5.3-1pct.output_network.xml.gz" ) ;
	private static Path plans_output = Paths.get("/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_BER/output_policy_160/berlin-v5.3-1pct.output_plans-express.xml.gz") ;
	private static String plans_input =				"/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_BER/output_policy_160/berlin-v5.3-1pct.output_plans.xml.gz" ;
	private static String tripsFile =				"/home/misax/Documents/Uni/Master/Matsim/openBerlin-v5.3-1pct_BER/output_policy_160/berlin-v5.3-1pct.tripsBer.txt" ;

	
	private static String[] links2Watch = new String[] {
			"kreuz-sxf",
			"sxf-kreuz",
			"hbf-kreuz",
			"kreuz-hbf"
	} ;
	
	private static String[] origActivities = new String[] {
			"home",
			"work",
			"leisure",
			"education",
			"shopping",
			"other",
			"fly"
	} ;
	//TODO: extend for fly activity
	
	private Population pop ;
	
	public static void main(String[] args) throws IOException {
		new BerTripAnalysis().run() ;
	}	
	
	public void run() throws IOException {	
				
		Network network = NetworkUtils.createNetwork() ;
		new MatsimNetworkReader(network).readFile(inputNetwork.toString()) ;
		
//        Config config = ConfigUtils.loadConfig( configFile ) ;
//        Scenario scenario = ScenarioUtils.loadScenario( config ) ;
//        Population pop = scenario.getPopulation() ;

		this.pop = PopulationUtils.createPopulation( ConfigUtils.createConfig() );
		PopulationUtils.readPopulation( this.pop, plans_input );
        
        Set< Id<Person> > persons2Remove = new HashSet<>() ;
        
        BufferedWriter bwTrips = IOUtils.getBufferedWriter( tripsFile );
        
		bwTrips.write(
				
				"personId\t" +
						"startCoordX" + "\t" +
						"startCoordY" + "\t" +
						"endCoordX" + "\t" +
						"endCoordY" + "\t" +
						"startTime" + "\t" +
						"endTime" + "\t" +
						"startActType" + "\t" +
						"endActType" + "\t" +
						"tripCombi"
						);
		
		bwTrips.newLine();
		
        for ( Person person : pop.getPersons().values() ) {
        	
            Plan plan = person.getSelectedPlan() ;
        	List<PlanElement> planElements = plan.getPlanElements() ;
        	int planSize = planElements.size() ;

            boolean usesExpress = false ;  
            
        	int positionLinkElement = 0 ;
        	for( int i = 0; i < planSize; i++ ) {
        		
        		PlanElement element = planElements.get( i ) ;
        		
				String startActivityType = "" ;
				double startActivityCoordX = 0 ;
				double startActivityCoordY = 0 ;
				double startActivityEnd = 0.0 ;
        		
				String endActivityType = "" ;
				double endActivityCoordX = 0 ;
				double endActivityCoordY = 0 ;
				double endActivityStart = 0.0 ;
				
        		if( isLegElement( element ) ) {
        			
            		Leg elementLeg = ( Leg ) element ;
            		
            		if( includesAirportExpress( elementLeg ) ) {
            			
            			usesExpress = true ;
            			
            			//StartAktivität
            			{
                			outerloop:
                    			for( int index = i-1; index >= 0; index-- ) {
                    				
                    				PlanElement startElement = planElements.get( index );
                    				
                    				if( isActivityElement( startElement ) ) {
                    					
                    					Activity startActivity = ( Activity ) startElement;
                    					String typeOfStartActivity = startActivity.getType().toString() ;
                    					
                    					for( String origActivity : origActivities ) {
                    						
                    						if( typeOfStartActivity.contains( origActivity ) ) {
                    							
                    							startActivityType = startActivity.getType() ;
                    							startActivityCoordX = startActivity.getCoord().getX() ;
                    							startActivityCoordY = startActivity.getCoord().getY() ;

                    							//startActivityEnd = startActivity.getEndTime() ;
                    							
                    							Leg nextLeg = ( Leg ) planElements.get( index+1 ) ;
                    							startActivityEnd = nextLeg.getDepartureTime() ;
                    							
                    							break outerloop;
                    						} else {}

                    					}
                    				} else {}
                    			}
            			}
            			
            			//EndAktivität
            			{
                			outerloop:
                    			for( int index2 = i+1; index2 < planSize; index2++ ) {
                    				
                    				PlanElement endElement = planElements.get( index2 ) ;
                    				
                    				if( isActivityElement( endElement ) ) {
                    					
                    					Activity endActivity = ( Activity ) endElement ;
                    					String typeOfEndActivity = endActivity.getType().toString() ;
                    					
                    					for( String origActivity : origActivities ) {
                    						
                    						if( typeOfEndActivity.contains( origActivity ) ) {
                    							
                        						endActivityCoordX = endActivity.getCoord().getX() ;
                        						endActivityCoordY = endActivity.getCoord().getY() ;
                            					endActivityType = endActivity.getType() ;
                            					
                            					Leg lastLeg = ( Leg ) planElements.get( index2-1 ) ;
                            					
                            					endActivityStart = lastLeg.getDepartureTime() ;
                            					//TODO: should be "+ lastLeg.getTravelTime()" but "Deprecated"
                            					//so every egress_walk duration is lost
                            					lastLeg.getTravelTime();
                            					
                    							break outerloop ;
                    						} else {}
                    					}
                    				} else {}
                    			}
            			}
            			
            			//trip combination
        				String tripCombi = "";
            			{
            				if ( startActivityType.contains( "home" ) ) {
            					
            					if ( endActivityType.contains( "home" ) ) {
            						tripCombi = "home-home";
            						
            					} else if ( endActivityType.contains( "leisure" ) ) {
            						tripCombi = "home-leisure";

            					} else if ( endActivityType.contains( "work" ) ) {
            						tripCombi = "home-work";

            					} else if ( endActivityType.contains( "shop" ) ) {
            						tripCombi = "home-shop";

            					} else if ( endActivityType.contains( "other" ) ) {
            						tripCombi = "home-other";

            					} else if ( endActivityType.contains( "education" ) ) {
            						tripCombi = "home-education";
		
            					} else if ( endActivityType.contains( "fly" ) ) {
            						tripCombi = "home-fly";
		
            					} else {}
            					
            				} else if ( startActivityType.contains( "leisure" ) ) {
            					
            					if ( endActivityType.contains( "home" ) ) {
            						tripCombi = "leisure-home";

            					} else if ( endActivityType.contains( "leisure" ) ) {
            						tripCombi = "leisure-leisure";

            					} else if ( endActivityType.contains( "work" ) ) {
            						tripCombi = "leisure-work";

            					} else if ( endActivityType.contains( "shop" ) ) {
            						tripCombi = "leisure-shop";

            					} else if ( endActivityType.contains( "other" ) ) {
            						tripCombi = "leisure-other";

            					} else if ( endActivityType.contains( "education" ) ) {
            						tripCombi = "leisure-education";

            					} else {}
            					
            				} else if ( startActivityType.contains( "work" ) ) {
            					
            					if ( endActivityType.contains( "home" ) ) {
            						tripCombi = "work-home";

            					} else if ( endActivityType.contains( "leisure" ) ) {
            						tripCombi = "work-leisure";

            					} else if ( endActivityType.contains( "work" ) ) {
            						tripCombi = "work-work";

            					} else if ( endActivityType.contains( "shop" ) ) {
            						tripCombi = "work-shop";

            					} else if ( endActivityType.contains( "other" ) ) {
            						tripCombi = "work-other";

            					} else if ( endActivityType.contains( "education" ) ) {
            						tripCombi = "work-education";

            					} else {}
            					
            				} else if ( startActivityType.contains( "shop" ) ) {
            					
            					if ( endActivityType.contains( "home" ) ) {
            						tripCombi = "shop-home";

            					} else if ( endActivityType.contains( "leisure" ) ) {
            						tripCombi = "shop-leisure";

            					} else if ( endActivityType.contains( "work" ) ) {
            						tripCombi = "shop-work";

            					} else if ( endActivityType.contains( "shop" ) ) {
            						tripCombi = "shop-shop";

            					} else if ( endActivityType.contains( "other" ) ) {
            						tripCombi = "shop-other";

            					} else if ( endActivityType.contains( "education" ) ) {
            						tripCombi = "shop-education";

            					} else {}
            					
            				} else if ( startActivityType.contains( "other" ) ) {
            					
            					if ( endActivityType.contains( "home" ) ) {
            						tripCombi = "other-home";

            					} else if ( endActivityType.contains( "leisure" ) ) {
            						tripCombi = "other-leisure";

            					} else if ( endActivityType.contains( "work" ) ) {
            						tripCombi = "other-work";

            					} else if ( endActivityType.contains( "shop" ) ) {
            						tripCombi = "other-shop";
   						
            					} else if ( endActivityType.contains( "other" ) ) {
            						tripCombi = "other-other";

            					} else if ( endActivityType.contains( "education" ) ) {
            						tripCombi = "other-education";

            					} else {}
            					
            				} else if ( startActivityType.contains( "education" ) ) {
            					
            					if ( endActivityType.contains( "home" ) ) {
            						tripCombi = "education-home";

            					} else if ( endActivityType.contains( "leisure" ) ) {
            						tripCombi = "education-leisure";

            					} else if ( endActivityType.contains( "work" ) ) {
            						tripCombi = "education-work";
		
            					} else if ( endActivityType.contains( "shop" ) ) {
            						tripCombi = "education-shop";
	
            					} else if ( endActivityType.contains( "other" ) ) {
            						tripCombi = "education-other";
            						
            					} else if ( endActivityType.contains( "education" ) ) {
            						tripCombi = "education-education";

            					} else {}
            					
            				} else if ( startActivityType.contains( "fly" ) ) {
            					
            					if ( endActivityType.contains( "home" ) ) {
            						tripCombi = "fly-home";

            					} else {}
            					
            				} else {}
            			} 
            			
            			System.out.println("Person: " +	person.getId()+
            					", startTime: " +		Double.toString( startActivityEnd ) +
            					", endTime: " +			Double.toString( endActivityStart ) +
            					", startActType: " +	startActivityType +
            					", endActType: " +		endActivityType +
            					", startCoordX: " +		startActivityCoordX +
            					", startCoordY: " +		startActivityCoordY +
            					", endCoordX: " +		endActivityCoordX +
            					", endCoordY: " +		endActivityCoordY +
            					", tripCombi: " +		tripCombi
            					);
            			
            			
            			
            			try {	            				
            				bwTrips.write(
            						person.getId() + "\t" +
            						startActivityCoordX + "\t" +
            						startActivityCoordY + "\t" +
            						endActivityCoordX + "\t" +
            						endActivityCoordY + "\t" +
            						startActivityEnd + "\t" +
            						endActivityStart + "\t" +
            						startActivityType + "\t" +
            						endActivityType + "\t" +
            						tripCombi
            						);
            				
            				bwTrips.newLine();
            				
 
            			}
            			catch (IOException e ){
            			}
            			
            			
            		} else {}
        		}
        	}

        	if( !usesExpress ) {
        		persons2Remove.add( person.getId() ) ;
        	}
        	
        }
        
		bwTrips.flush();
		bwTrips.close();
        
        for( Id< Person > id : persons2Remove) {
        	pop.removePerson( id ) ;
        }
        
        for ( Person person : pop.getPersons().values() ) {
        	
        	for( Plan plan : person.getPlans() ) {
        		
        		if( plan.equals( person.getSelectedPlan() ) ) {
        		} else {
        			
        		}
        	}
        }
        
        new PopulationWriter( pop ).write( plans_output.toString() ) ;
	}

	//because I don't know how to check for instaceOf Leg or Activity
	private boolean isActivityElement(PlanElement el) {
		return el.getClass().toString().contains( "ActivityImpl" ) ;
	}
	
	private boolean isLegElement(PlanElement el) {
		return el.getClass().toString().contains( "LegImpl" ) ;
	}
	
	private boolean includesAirportExpress(Leg leg) {
		
		boolean isIncluded = false;
		String route = leg.getRoute().toString();
		if( route.contains( "airport-express" ) ) {
			isIncluded = true;
		} else {}
    	return isIncluded ;

	}

}
