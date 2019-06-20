package org.matsim.class2019.network;



import java.util.HashSet;
import java.util.Set;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;

public class CreateRectangularNetwork {
	
	private Set<String> allowedModes = new HashSet<>();
	
	public CreateRectangularNetwork()
	{
		allowedModes.add(TransportMode.car);
		allowedModes.add(TransportMode.ship);
	}
	
	public Network createNetwork(double left, double right, double top, double bottom)
	{
		//create empty network
		Network network = NetworkUtils.createNetwork();
		NetworkFactory factory = network.getFactory();
		
		//add nodes
		Node n0 = factory.createNode(Id.createNodeId(0), new Coord(left, top));
		Node n1 = factory.createNode(Id.createNodeId(1), new Coord(right, top));
		Node n2 = factory.createNode(Id.createNodeId(2), new Coord(left, bottom));
		Node n3 = factory.createNode(Id.createNodeId(3), new Coord(right, bottom));
		
		network.addNode(n0);
		network.addNode(n1);
		network.addNode(n2);
		network.addNode(n3);
		
		//add links
		Link link1 = factory.createLink(Id.createLinkId(0), n0, n1);
		Link link2 = factory.createLink(Id.createLinkId(1), n1, n2);
		Link link3 = factory.createLink(Id.createLinkId(2), n2, n3);
		Link link4 = factory.createLink(Id.createLinkId(3), n3, n0);
		
		addAttributes(link1);
		addAttributes(link2);
		addAttributes(link3);
		addAttributes(link4);
		
		network.addLink(link1);
		network.addLink(link2);
		network.addLink(link3);
		network.addLink(link4);
		
		return network;
	}
	
	private void addAttributes(Link link) {
		link.setCapacity(600);
		link.setFreespeed(4.5);
		link.setAllowedModes(allowedModes);
	}
}
