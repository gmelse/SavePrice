package com.geo.saveprice;

import java.io.Serializable;

/**
 * 
 * This class creates a new product.
 *
 */

@SuppressWarnings("serial")
public class Product implements Serializable{
	
	private String name;
	private String price;
	
	public Product(String name, String price){
		this.name = name;
		this.price = price;
	}

	public String getName() {
		return name;
	}
	
	public String getPrice() {
		return price;
	}

	
}
