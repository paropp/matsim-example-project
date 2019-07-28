package org.matsim.class2019.ber;

import java.util.HashSet;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;

public class VehicleOccupancy {
	
	private double time;
	private Set<Id<Person>> personsOnFirstTrack = new HashSet<>();
	private Set<Id<Person>> personsOnSecondTrack = new HashSet<>();
	
	public VehicleOccupancy(double time) {
		if(time > 0) {
			this.time = time;
		} else {
			System.out.println("time has to be positive");
		}
	}
	
}
