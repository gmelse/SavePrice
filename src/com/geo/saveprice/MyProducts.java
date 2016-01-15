package com.geo.saveprice;

import java.util.ArrayList;

/**
 * 
 * This class holds a list of products.
 *
 */

public class MyProducts {

	private static ArrayList<Product> products = new ArrayList<Product>();
	private static ArrayList<Product> oldProducts = new ArrayList<Product>();
	
	//Procedures for products
	public static ArrayList<Product> getProducts() {
		return products;
	}

	public void setProducts(ArrayList<Product> products) {
		MyProducts.products = products;
	}
	//It is used when we want to load data from file to the ArrayList
	public static void addToProducts(ArrayList<Product> products) {
		for(int i=0; i<products.size(); i++){
			MyProducts.products.add(products.get(i));
		}
	}
	
	//Procedures for Old Products. (Old Products are all the products that haven't sent
	//to database and a new scan has started)
	public static ArrayList<Product> getOldProducts() {
		return oldProducts;
	}

	public static void setOldProducts(ArrayList<Product> oldProducts) {
		MyProducts.oldProducts = oldProducts;
	}
	//It is used when we want to load data from file to the ArrayList
	public static void addToOldProducts(ArrayList<Product> products) {
		for(int i=0; i<products.size(); i++){
			MyProducts.oldProducts.add(products.get(i));
		}
	}
	
}
