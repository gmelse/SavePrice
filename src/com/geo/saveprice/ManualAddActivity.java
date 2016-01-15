package com.geo.saveprice;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

public class ManualAddActivity extends Activity{
	
	private static String url_get_products = "http://213.16.150.49/androidserver/Test/getAllProducts.php";
	
	// JSON Node names
    //private static final String TAG_SUCCESS = "success";
    private static final String TAG_PRODUCTS = "products";
    private static final String TAG_NAME = "name";
	
	JSONParser jsonParser = new JSONParser();
    JSONArray products = null;
	
	ArrayAdapter<String> myAdapter;
	AutoCompleteTextView textView;
	
	//Initial value in the TextField
    String[] item = new String[] {"Type a product..."};
	
	@Override
    public void onCreate(Bundle savedInstanceState) {         

       super.onCreate(savedInstanceState);    
       setContentView(R.layout.manual_add_layout);
       
       StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
       StrictMode.setThreadPolicy(policy);
       
       // autocompletetextview is in activity_main.xml
       //myAutoComplete = (CustomAutoCompleteView) findViewById(R.id.myautocomplete);
       textView = (AutoCompleteTextView) findViewById(R.id.productsList);
        
       // add the listener so it will tries to suggest while the user types
       textView.addTextChangedListener(new CustomAutoCompleteTextChangedListener(this));
        
       // set our adapter
       //myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, COUNTRIES);
       //myAutoComplete.setAdapter(myAdapter);
       myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, item);
       textView.setAdapter(myAdapter);
       
       
   }

   /*private static final String[] COUNTRIES = new String[] {
       "Belgium", "France", "Italy", "Germany", "Spain"
   };*/

// this function is used in CustomAutoCompleteTextChangedListener.java
   public String[] getItemsFromDb(String searchTerm) throws JSONException, UnsupportedEncodingException{
        
	   // Building Parameters
       List<NameValuePair> params = new ArrayList<NameValuePair>();
       byte[] data = searchTerm.getBytes("UTF-8");
		String base64 = Base64.encodeBytes(data);
		
		params.add(new BasicNameValuePair("searchTerm", base64 ));
       
       //params.add(new BasicNameValuePair("searchTerm", searchTerm));

       // getting JSON Object
       // Note that create product url accepts POST method
       JSONObject json = jsonParser.makeHttpRequest(url_get_products, "POST", params);
       products = json.getJSONArray(TAG_PRODUCTS);
       
       Log.v(TAG_NAME, "Products Got it");
       
       String[] item = new String[products.length()];
       
       // looping through All Products
       for (int i = 0; i < products.length(); i++) {
           JSONObject c = products.getJSONObject(i);

           // Storing each json item in variable
           String name = c.getString(TAG_NAME);
           item[i] = name;
           Log.v(TAG_NAME, "Product now is: " + name);
           // creating new HashMap
           //HashMap<String, String> map = new HashMap<String, String>();

           // adding each child node to HashMap key => value
           /*map.put(TAG_PID, id);
           map.put(TAG_NAME, name);

           // adding HashList to ArrayList
           productsList.add(map);*/
       }
	   
       // add items on the array dynamically
       /*List<MyObject> products = databaseH.read(searchTerm);
       int rowCount = products.size();
        
       String[] item = new String[rowCount];
       int x = 0;
        
       for (MyObject record : products) {
            
           item[x] = record.objectName;
           x++;
       }*/
        
       return item;
   }
	
	
	

}
