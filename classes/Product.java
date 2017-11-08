package classes;

import enums.ProductType;

public class Product {
	private String name;
	private ProductType type;
	private boolean uniform; // same price everywhere
	private double basePrice;
	
	public Product(String s, ProductType t, boolean u, double p) {
		this.name = s;
		this.type = t;
		this.uniform = u;
		this.basePrice = p;
	}
	
	public String getName() {
		return this.name;
	}
	
	public ProductType getType() {
		return this.type;
	}
	
	public boolean getUniform() {
		return this.uniform;
	}
	
	public double getBasePrice() {
		return this.basePrice;
	}
}
