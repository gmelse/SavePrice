package com.geo.saveprice;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.util.Log;

public class RecognizeProducts {

	private static final String TAG = "RecognizeProducts";
	
	private Context myContext;
	private String recognizedText = "", finalText = "";
	private String[] lines;
	
	public RecognizeProducts(String recognizedText, Context myContext){
		this.recognizedText = recognizedText;
		this.myContext = myContext;
	}
	
	public String recognize(){
		lines = recognizedText.split(System.getProperty("line.separator"));
		
		for(int i=0; i<lines.length; i++){
			if(lines[i].equals("")){
				Log.v(TAG, "Line" + i + ":is empty" + lines[i]);
			}else{
				Log.v(TAG, "MyLine" + i + ":" + lines[i]);
			}
		}
		
		//We assume that the name of the store will be in the first 3 lines
		String[] firstLines = new String[2];
		int index = 0;
		int counter = 0;
		for(int i=0; i<lines.length; i++){
			
			if(lines[i].equals("")){
				//Do-Nothing!
			}else{
				counter++;
				if(counter>1){ //To avoid first Line, The name will be in the 2nd or in the 3rd Line.
					firstLines[index] = lines[i];
					index++;
					if(index == 2){
						break;
					}
				}
			}
		}
		
		//Print Lines
		for(int i=0; i<firstLines.length; i++){
			Log.v(TAG, "TheLine" + i + " is " + firstLines[i]);
		}
		
		//For example: ΔΙΑΜΑΝΤΗΣ ΜΑΣΟΥΤΗΣ Α.Ε --> We split to 3 words: ΔΙΑΜΑΝΤΗΣ, ΜΑΣΟΥΤΗΣ, Α.Ε
		String[] words0 = firstLines[0].split(" ");
		String[] words1 = firstLines[1].split(" ");
		
		int plithos = words0.length + words1.length;
		Log.v(TAG, "Plithos:" + plithos);
		
		if (plithos<5){ //This a marinopoulos receipt
			Log.v(TAG, "MARINOPOULOS RECEIPT");
			finalText = analyzeMarinopoulosReceipt();
		}else if(plithos>=5 && plithos<8){ //This is a masoutis receipt 
			Log.v(TAG, "MASOUTIS RECEIPT");
			finalText = analyzeMasoutisReceipt();
		}else if(plithos>=8){ //This is a lidl receipt
			Log.v(TAG, "LIDL RECEIPT");
			finalText = analyzeLidlReceipt();
		}
		
		for(int i=0; i<words0.length; i++){
			Log.v(TAG, "First Pattern:" +  words0[i]);
		}

		for(int i=0; i<words1.length; i++){
			Log.v(TAG, "First Pattern:" +  words1[i]);
		}
		
		return finalText;
	}

	private String analyzeMasoutisReceipt() {
		
		//Γενικό pattern προιόντος-τιμής για όλες τις αποδείξεις
		  //ΠΑΡΑΔΕΙΓΜΑ: ΧΥΜΟΣ ΠΟΡΤ ΑΜΙΤΑ 1          1,46 23,00%  (ΜΑΡΙΝΟΠΟΥΛΟΣ)
		  //			ΑΜΙΤΑ ΧΥΜ.ΠΟΡΤΟΚ.1L         1,25 23,00%  (ΛΙΝΤΛ)
		  //			ΑΜΙΤΑ ΠΟΡΤΟΚΑΛΙ 100% 1LIT   1,40 23,00%  (ΜΑΣΟΥΤΗΣ)
		  Pattern productName = Pattern.compile("([[A-Z]|[a-z]|[Α-Ω]|[α-ω]|[\\s]|[0-9]|[\\S]]+)([\\s])+([0-99][\\,.][0-9][0-9])([\\s])");
		  Matcher pn;
		  
		  //Ειδικές περιπτώσεις στις αποδείξεις ΜΑΣΟΥΤΗΣ
		  
			  //ΕΚΠΤΩΣΗ 15,00 %
			  Pattern discount = Pattern.compile("([[A-Z]|[a-z]|[Α-Ω]|[α-ω]|[\\S]]+)([\\s])+([0-99][\\,.][0-99])([\\s])+([\\%]|[\\S])");
			  Matcher d;
			  
			  //0,809 x 6,29 || 2 x 1,60
			  Pattern pricePerKiloOrItem = Pattern.compile("[\\s]*([0-9]|[0-9][\\,.][0-999])[\\s]+[XxΧχ][\\s]+([0-99][\\,.][0-99])"); 
			  Matcher pki;
		  
		  Log.v(TAG, "LINES LENGTH:" + lines.length);
		  
		  String price = "", 
				 name = "",
				 dis = "",
			     rightName = "";
		  Boolean found = false;
		  int i=0;
		  while(i<lines.length){
			  Log.v(TAG, lines[i]);
			  
			  pn = productName.matcher(lines[i]);
			  pki = pricePerKiloOrItem.matcher(lines[i]);
			  
			  if(pki.find()){
				  price = pki.group(2);
				  Log.v(TAG, "price:" + price);
				  pn = productName.matcher(lines[i+1]);
				  
				  if(pn.find()){
					  name = pn.group(1);
					  Log.v(TAG, "name:" + name);
					  
					  d = discount.matcher(lines[i+2]);
					  if(d.find()){
						  dis = d.group(3);
						  Log.v(TAG, "discount:" + dis);
						  price = Double.toString((Double.parseDouble(price)*(100-Double.parseDouble(dis)))/100);
						  Log.v(TAG, "newPrice:" + price);
						  i = i+4;
					  }else{
						  i = i+2;
					  }
					  
					  found = true;
				  }
				
			  }else if(pn.find()){
				  price = pn.group(3);
				  name = pn.group(1);
				  Log.v(TAG, "name:"+ name + " price:" + price);
				  
				  d = discount.matcher(lines[i+1]);
				  if(d.find()){
					  dis = d.group(3);
					  Log.v(TAG, "discount:" + dis);
					  price = Double.toString((Double.parseDouble(price)*(100-Double.parseDouble(dis)))/100);
					  Log.v(TAG, "newPrice:" + price);
					  i = i+3;
				  }else{
					  i = i+1;
				  }
				  found = true;
			  }
			  
			  if (found){
				  //Before you start matching with Dictionaty lookup if there is already a key in the HashMap
				  rightName = ((MainActivity) myContext).getMap().get(name);
				  Log.v(TAG, "rightName: " + rightName);
				  
				  if(rightName == null){
					   //Compute Levenshtein Distance for the name with the Strings in Dictionary 
					   //and take the string that has the smallest score and store this and it's 
					  //price-->m.group(1) in array of Products and in the HashMap so next time if
					  //we have the same name don't waste time to compute Levenshtein Distance.
					  //AbstractStringMetric metric = new Levenshtein();
					  int result0 = LevenshteinDistance.computeDistance(name, Dictionary.masoutisDict[0]);
					  Log.v(TAG, name + " distance with:" + Dictionary.masoutisDict[0] + " is " + result0);
					  int index = 0;
					  for(int j=1; j<Dictionary.masoutisDict.length; j++){
						  int result1 = LevenshteinDistance.computeDistance(name, Dictionary.masoutisDict[j]);
						  Log.v(TAG, name + " distance with:" + Dictionary.masoutisDict[j] + " is " + result1);
						  if(result1<result0){
							  result0 = result1;
							  index = j;
						  }
					  }
					  rightName = Dictionary.masoutisDict[index];
					  Log.v(TAG, "rightNameAfterLev: " + rightName);
					  ((MainActivity) myContext).getMap().put(name, rightName);
				  }
				  
				  //Search products ArrayList to be sure that no Duplicates will be shown to the final result
				  Boolean productAlreadyAdded = false;
				  if (MyProducts.getProducts().size()>0){
					  for(int k=0; k<MyProducts.getProducts().size(); k++){
						  if (MyProducts.getProducts().get(k).getName().equals(rightName)){
							  productAlreadyAdded = true;
							  break;
						  }
					  }
				  }
				  
				  //Convert Price from 1,92 to 1.92
				  String rightPrice = price.replace(",", ".");
				  Log.v(TAG, "priceAfterreplace:" + rightPrice);
				  
				  if ((!productAlreadyAdded) && (!rightName.equals("ΣΥΝΟΛΟ")) && (!rightName.equals("ΜΕΤΡΗΤΑ")) && (!rightName.equals("ΡΕΣΤΑ")) ){
					  Product aProduct = new Product(rightName, rightPrice);
					  MyProducts.getProducts().add(aProduct);
					  finalText = finalText + rightName + "  "+ rightPrice + "\n";
				  }
				  
				  found = false;
				  
			  }else{
				  i = i+1;
				  found = false;
			  }
			  
		  }
		return finalText;
	}

	private String analyzeLidlReceipt() {
		
		//Γενικό pattern προιόντος-τιμής για όλες τις αποδείξεις
		  //ΠΑΡΑΔΕΙΓΜΑ: ΧΥΜΟΣ ΠΟΡΤ ΑΜΙΤΑ 1          1,46 23,00%  (ΜΑΡΙΝΟΠΟΥΛΟΣ)
		  //			ΑΜΙΤΑ ΧΥΜ.ΠΟΡΤΟΚ.1L         1,25 23,00%  (ΛΙΝΤΛ)
		  //			ΑΜΙΤΑ ΠΟΡΤΟΚΑΛΙ 100% 1LIT   1,40 23,00%  (ΜΑΣΟΥΤΗΣ)
		  Pattern productName = Pattern.compile("([[A-Z]|[a-z]|[Α-Ω]|[α-ω]|[\\s]|[0-9]|[\\S]]+)([\\s])+([0-99][\\,.][0-9][0-9])([\\s])");
		  Matcher pn;
		  
		  //Ειδικές περιπτώσεις στις αποδείξεις ΛΙΝΤΛ
		  
			  //ΕΚΠΤΩΣΗ 15,00 %
			  //Pattern discount = Pattern.compile("([[A-Z]|[a-z]|[Α-Ω]|[α-ω]|[\\S]]+)([\\s])+([0-99][\\,.][0-99])([\\s])+([\\%]|[\\S])");
			 // Matcher d;
			  
			  //1,154 X 1,49 || 3,000 X 1,49
			  Pattern pricePerKiloOrItem = Pattern.compile("[\\s]*([0-99][\\,.][0-999])[\\s]+[XxΧχ][\\s]+([0-99][\\,.][0-999])"); 
			  Matcher pki;
		  
		  Log.v(TAG, "LINES LENGTH:" + lines.length);
		  
		  String price = "", 
				 name = "",
				 rightName = "";
				 //dis = "",
		  Boolean found = false;
		  int i=0;
		  while(i<lines.length){
			  Log.v(TAG, lines[i]);
			  
			  pn = productName.matcher(lines[i]);
			  pki = pricePerKiloOrItem.matcher(lines[i]);
			  
			  if(pki.find()){
				  price = pki.group(2);
				  Log.v(TAG, "price:" + price);
				  pn = productName.matcher(lines[i+1]);
				  
				  if(pn.find()){
					  name = pn.group(1);
					  Log.v(TAG, "name:" + name);
					  
					 /* d = discount.matcher(lines[i+2]);
					  if(d.find()){
						  dis = d.group(3);
						  Log.v(TAG, "discount:" + dis);
						  price = Double.toString((Double.parseDouble(price)*(100-Double.parseDouble(dis)))/100);
						  Log.v(TAG, "newPrice:" + price);
						  i = i+4;
					  }else{
						  i = i+2;
					  }*/
					  i = i+2;
					  found = true;
				  }
				
			  }else if(pn.find()){
				  price = pn.group(3);
				  name = pn.group(1);
				  Log.v(TAG, "name:"+ name + " price:" + price);
				  
				 /* d = discount.matcher(lines[i+1]);
				  if(d.find()){
					  dis = d.group(3);
					  Log.v(TAG, "discount:" + dis);
					  price = Double.toString((Double.parseDouble(price)*(100-Double.parseDouble(dis)))/100);
					  Log.v(TAG, "newPrice:" + price);
					  i = i+3;
				  }else{
					  i = i+1;
				  }*/
				  i = i+1;
				  found = true;
			  }
			  
			  if (found){
				  //Before you start matching with Dictionaty lookup if there is already a key in the HashMap
				  rightName = ((MainActivity) myContext).getMap().get(name);
				  Log.v(TAG, "rightName: " + rightName);
				  
				  if(rightName == null){
					   //Compute Levenshtein Distance for the name with the Strings in Dictionary 
					   //and take the string that has the smallest score and store this and it's 
					  //price-->m.group(1) in array of Products and in the HashMap so next time if
					  //we have the same name don't waste time to compute Levenshtein Distance.
					  //AbstractStringMetric metric = new Levenshtein();
					  int result0 = LevenshteinDistance.computeDistance(name, Dictionary.lidlDict[0]);
					  Log.v(TAG, name + " distance with:" + Dictionary.lidlDict[0] + " is " + result0);
					  int index = 0;
					  for(int j=1; j<Dictionary.lidlDict.length; j++){
						  int result1 = LevenshteinDistance.computeDistance(name, Dictionary.lidlDict[j]);
						  Log.v(TAG, name + " distance with:" + Dictionary.lidlDict[j] + " is " + result1);
						  if(result1<result0){
							  result0 = result1;
							  index = j;
						  }
					  }
					  rightName = Dictionary.lidlDict[index];
					  Log.v(TAG, "rightNameAfterLev: " + rightName);
					  ((MainActivity) myContext).getMap().put(name, rightName);
				  }
				  
				  //Search products ArrayList to be sure that no Duplicates will be shown to the final result
				  Boolean productAlreadyAdded = false;
				  if (MyProducts.getProducts().size()>0){
					  for(int k=0; k<MyProducts.getProducts().size(); k++){
						  if (MyProducts.getProducts().get(k).getName().equals(rightName)){
							  productAlreadyAdded = true;
							  break;
						  }
					  }
				  }
				  
				  //Convert Price from 1,92 to 1.92
				  String rightPrice = price.replace(",", ".");
				  Log.v(TAG, "priceAfterreplace:" + rightPrice);
				  
				  if ((!productAlreadyAdded) && (!rightName.equals("ΜΕΡΙΚΟ ΣΥΝΟΛΟ")) && (!rightName.equals("ΜΕΤΡΗΤΟΙΣ")) && (!rightName.equals("ΡΕΣΤΑ")) && (!rightName.equals("ΣΥΝΟΛΟ ΜΕΤΑ ΦΟΡΟΥ")) && (!rightName.equals("Check ΔΩΡΟΥ"))){
					  Product aProduct = new Product(rightName, rightPrice);
					  MyProducts.getProducts().add(aProduct);
					  finalText = finalText + rightName + "  "+ rightPrice + "\n";
				  }
				  
				  found = false;
				  
			  }else{
				  i = i+1;
				  found = false;
			  }
			  
		  }
		return finalText;
	}

	private String analyzeMarinopoulosReceipt() {
		
		//Γενικό pattern προιόντος-τιμής για όλες τις αποδείξεις
		  //ΠΑΡΑΔΕΙΓΜΑ: ΧΥΜΟΣ ΠΟΡΤ ΑΜΙΤΑ 1          1,46 23,00%  (ΜΑΡΙΝΟΠΟΥΛΟΣ)
		  //			ΑΜΙΤΑ ΧΥΜ.ΠΟΡΤΟΚ.1L         1,25 23,00%  (ΛΙΝΤΛ)
		  //			ΑΜΙΤΑ ΠΟΡΤΟΚΑΛΙ 100% 1LIT   1,40 23,00%  (ΜΑΣΟΥΤΗΣ)
		  Pattern productName = Pattern.compile("([[A-Z]|[a-z]|[Α-Ω]|[α-ω]|[\\s]|[0-9]|[\\S]]+)([\\s])+([0-99][\\,.][0-9][0-9])([\\s])");
		  Matcher pn;
		  
		  //Ειδικές περιπτώσεις στις αποδείξεις ΜΑΡΙΝΟΠΟΥΛΟΣ
		  
		  	  //ΧΥΜΟΣ ΑΜΙΤΑ 1L/Δ     2,92-23,00%
			  Pattern discount = Pattern.compile("([[A-Z]|[a-z]|[Α-Ω]|[α-ω]|[\\s]|[0-9]|[\\S]]+)([\\s])+([0-99][\\,.][0-9][0-9])([\\-]|[\\S])");
			  Matcher d;
			  
			  //5201005074248  10,000 x 1,46
			  Pattern pricePerKiloOrItem = Pattern.compile("[\\s]*[0-9]{13}[\\s]+([0-99][\\,.][0-999])[\\s]+[XxΧχ][\\s]+([0-99][\\,.][0-99])"); 
			  Matcher pki;
			  
		  Log.v(TAG, "LINES LENGTH:" + lines.length);
		  
		  String price = "",
				 factor = "",
				 name = "",
				 dis = "",
			     rightName = "";
		  Boolean found = false;
		  int i=0;
		  while(i<lines.length){
			  Log.v(TAG, lines[i]);
			  
			  pn = productName.matcher(lines[i]);
			  pki = pricePerKiloOrItem.matcher(lines[i]);
			  
			  if(pki.find()){
				  factor = pki.group(1);
				  price = pki.group(2);
				  Log.v(TAG, "price:" + price);
				  pn = productName.matcher(lines[i+1]);
				  
				  if(pn.find()){
					  name = pn.group(1);
					  Log.v(TAG, "name:" + name);
					
					  found = true;
				  }
				
			  }else if(pn.find()){
				  price = pn.group(3);
				  name = pn.group(1);
				  Log.v(TAG, "name:"+ name + " price:" + price);
				  
				  found = true;
			  }
			  
			  if (found){
				  //Έλεγξε αν υπάρχει έκπτωση για το προιόν που βρέθηκε
				  for(int j=i; j<lines.length; j++){
					  if(lines[j].contains("ΕΚΠΤΩΣΗ")){
						  d = discount.matcher(lines[j+1]);
						  if(d.find()){
							  if(d.group(1).equals(name)){
								  dis = d.group(3);
								  Log.v(TAG, "discount:" + dis);
								  price = Double.toString(Double.parseDouble(price) - Double.parseDouble(dis)/Double.parseDouble(factor));
								  Log.v(TAG, "newPrice:" + price);
								  lines[j] = "";
								  lines[j+1] = "";
								  break;
							  }
							  
						  }
					  }
				  }
				  
				  //Before you start matching with Dictionaty lookup if there is already a key in the HashMap
				  rightName = ((MainActivity) myContext).getMap().get(name);
				  Log.v(TAG, "rightName: " + rightName);
				  
				  if(rightName == null){
					   //Compute Levenshtein Distance for the name with the Strings in Dictionary 
					   //and take the string that has the smallest score and store this and it's 
					  //price-->m.group(1) in array of Products and in the HashMap so next time if
					  //we have the same name don't waste time to compute Levenshtein Distance.
					  //AbstractStringMetric metric = new Levenshtein();
					  int result0 = LevenshteinDistance.computeDistance(name, Dictionary.marinopoulosDict[0]);
					  Log.v(TAG, name + " distance with:" + Dictionary.marinopoulosDict[0] + " is " + result0);
					  int index = 0;
					  for(int j=1; j<Dictionary.marinopoulosDict.length; j++){
						  int result1 = LevenshteinDistance.computeDistance(name, Dictionary.marinopoulosDict[j]);
						  Log.v(TAG, name + " distance with:" + Dictionary.marinopoulosDict[j] + " is " + result1);
						  if(result1<result0){
							  result0 = result1;
							  index = j;
						  }
					  }
					  rightName = Dictionary.marinopoulosDict[index];
					  Log.v(TAG, "rightNameAfterLev: " + rightName);
					  ((MainActivity) myContext).getMap().put(name, rightName);
				  }
				  
				  //Search products ArrayList to be sure that no Duplicates will be shown to the final result
				  Boolean productAlreadyAdded = false;
				  if (MyProducts.getProducts().size()>0){
					  for(int k=0; k<MyProducts.getProducts().size(); k++){
						  if (MyProducts.getProducts().get(k).getName().equals(rightName)){
							  productAlreadyAdded = true;
							  break;
						  }
					  }
				  }
				  
				  //Convert Price from 1,92 to 1.92
				  String rightPrice = price.replace(",", ".");
				  Log.v(TAG, "priceAfterreplace:" + rightPrice);
				  
				  if ((!productAlreadyAdded) && (!rightName.equals("ΜΕΡΙΚΟ ΣΥΝΟΛΟ")) && (!rightName.equals("ΣΥΝΟΛΟ ΜΕΤΑ ΦΟΡΟΥ")) && (!rightName.equals("Check ΔΩΡΟΥ")) && (!rightName.equals("ΜΕΤΡΗΤΑ")) && (!rightName.equals("ΡΕΣΤΑ")) ){
					  Product aProduct = new Product(rightName, rightPrice);
					  MyProducts.getProducts().add(aProduct);
					  finalText = finalText + rightName + "  "+ rightPrice + "\n";
				  }
				  
				  
			  }
			  if(rightName.equals("ΜΕΡΙΚΟ ΣΥΝΟΛΟ")){
				  i = lines.length;
			  }else{
				  i = i+1;
			  }
			  found = false;
			  
		  }
		  
		return finalText;
	}
}
