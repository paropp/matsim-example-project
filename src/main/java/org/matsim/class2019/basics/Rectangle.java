package org.matsim.class2019.basics;


public class Rectangle {
	
	private double width;
	private double height;
	
	public Rectangle(double width, double height) {
		if(width > 0 & height > 0) {
			this.width = width;
			this.height = height;
		} else {
			System.out.println("Hoehe und Breitemüssen positiv sein");
		}
	}
	
	public double calculateArea() {
		return width * height;
	}
}
