package org.matsim.class2019.network;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;

public class Link2Add {
	
	private Id<Link> id;
	private Id<Node> startNodeId;
	private Id<Node> endNodeId;
	private double length;
	private double numLanes;
	private double capacity;
	private double freespeed;
	
	public Link2Add(	String id,
						String startNodeId, 
						String endNodeId,
						double length,
						double numLanes,
						double capacity,
						double freespeed ) {
		
		if(		id != null &
				startNodeId != null &
				endNodeId != null &
				length > 0 &
				numLanes > 0 &
				capacity > 0 &
				freespeed > 0) {

			this.id = Id.createLinkId(id);
			this.startNodeId = Id.createNodeId( startNodeId );
			this.endNodeId = Id.createNodeId( endNodeId );
			this.length = length;
			this.numLanes = numLanes;
			this.capacity = capacity;
			this.freespeed = freespeed;
			
			} else {
				System.out.println("Fehlerhafter input");
				}
	}
	
	Id<Link> getId() {
		return this.id;
	}
	
	Id<Node> getStartNodeId() {
		return this.startNodeId; 
	}
	
	Id<Node> getEndNodeId() {
		return this.endNodeId;
	}
	
	double getLength() {
		return this.length;
	}
	
	double getNumLanes() {
		return this.numLanes;
	}
	
	double getCapacity() {
		return this.capacity;
	}
	
	double getFreeSpeed() {
		return this.freespeed;
	}

}
