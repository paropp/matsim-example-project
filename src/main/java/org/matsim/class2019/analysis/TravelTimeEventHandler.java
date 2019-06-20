package org.matsim.class2019.analysis;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.population.Person;

import java.util.HashMap;
import java.util.Map;

public class TravelTimeEventHandler implements ActivityEndEventHandler, ActivityStartEventHandler {

	private final Map<Id<Person>, ActivityEndEvent> openTrips = new HashMap<>();
	private final Map<Id<Person>, Double> travelTimes = new HashMap<>();

	Map<Id<Person>, Double> getTravelTimesByPerson() {
		return travelTimes;
	}

	double calculateOverallTravelTime() {
		return travelTimes.values().stream().mapToDouble(d -> d).sum();
	}

	@Override
	public void handleEvent(ActivityEndEvent event) {

		if (isNotInteraction(event.getActType()))
			openTrips.put(event.getPersonId(), event);
	}

	@Override
	public void handleEvent(ActivityStartEvent event) {

		if (isNotInteraction(event.getActType()) && openTrips.containsKey(event.getPersonId())) {
			ActivityEndEvent actEnd = openTrips.remove(event.getPersonId());
			double timeTravelled = event.getTime() - actEnd.getTime();
			travelTimes.merge(event.getPersonId(), timeTravelled, Double::sum);
		}
	}

	private boolean isNotInteraction(String activityType) {
		return !activityType.contains(" interaction");
	}
}
