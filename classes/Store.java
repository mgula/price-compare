package classes;

import java.util.ArrayList;

public class Store {
	private String name;
	private ArrayList<Product> inventory;
	private int distanceTo;
	private int xLoc;
	private int yLoc;
	
	public Store(String n, ArrayList<Product> i, int x, int y) {
		this.name = n;
		this.inventory = i;
		this.xLoc = x;
		this.yLoc = y;
	}
	
	public String getName() {
		return this.name;
	}
	
	public ArrayList<Product> getInventory() {
		return this.inventory;
	}
	
	public int getDistanceTo() {
		return this.distanceTo;
	}
	
	/*Returns different modifiers based on the product and store - for the
	 *scope of this project, this is just to create the illusion of differing
	 *prices at stores*/
	public double getPriceModifier(Product p) {
		if (p.getUniform()) {
			return 1.00;
		}
		
		double priceModifier = 0;
		
		if (this.name == "Store A") {
			switch (p.getType()) {
				case BEVERAGE:
					priceModifier = .92;
					break;
				case FRUIT:
					priceModifier = .94;
					break;
				case MEAT:
					priceModifier = .93;
					break;
				default:
					priceModifier = 1.05;
					break;
			}
		} else if (this.name == "Store B") {
			switch (p.getType()) {
				case SNACK:
					priceModifier = .91;
					break;
				case UTIL:
					priceModifier = .92;
					break;
				case VEGGIE:
					priceModifier = .91;
					break;
				default:
					priceModifier = 1.06;
					break;
				}
		} else if (this.name == "Store C") {
			switch (p.getType()) {
				case WHEAT:
					priceModifier = .95;
					break;
				case DAIRY:
					priceModifier = .93;
					break;
				case MEAT:
					priceModifier = .91;
					break;
				default:
					priceModifier = 1.03;
					break;
			}
		} else if (this.name == "Store D") {
			priceModifier = .98;
		}
		
		return priceModifier;
	}
	
	public int getXLoc() {
		return this.xLoc;
	}
	
	public int getYLoc() {
		return this.yLoc;
	}
	
	public void setDistanceTo(int d) {
		this.distanceTo = d;
	}
}
