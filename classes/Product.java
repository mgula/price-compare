package classes;

import enums.ProductType;
import java.math.BigDecimal;

public class Product {
	private String name;
	private ProductType type;
	private boolean uniform; // same price everywhere
	private BigDecimal basePrice;
	
	public Product(String s, ProductType t, boolean u, BigDecimal p) {
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
	
	public BigDecimal getBasePrice() {
		return this.basePrice;
	}
}
