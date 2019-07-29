package org.matsim.class2019.ber;

import java.util.HashSet;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;

public class VehicleOccupancy {
	
	private double time ;

	private String status ;
	public Set<Id<Person>> personsOnFirstTrack = new HashSet<>() ;
	public Set<Id<Person>> personsOnSecondTrack = new HashSet<>() ;
	
	public VehicleOccupancy(double time, String s) {
		if( time > 0 ) {
			this.time = time ;
			this.status = s ;
		} else {
			System.out.println("time has to be positive") ;
		}
	}
	
	public String getStatus() {
		return this.status ;
	}
	
	public void setStatus( String s ) {
		this.status = s ;
	}
	
	public double getTime() {
		return time;
	}
	
}
