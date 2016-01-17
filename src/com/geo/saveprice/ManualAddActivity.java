package com.geo.saveprice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class ManualAddActivity extends Activity{
	
	private static String url_get_products = "http://46.12.52.5/androidserver/getAllProducts.php";
	
	// Progress Dialog
    private ProgressDialog pDialog;
    
	// Creating JSON Parser object
    JSONParser jParser = new JSONParser();
	
    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PRODUCTS = "products";
    private static final String TAG_PID = "pid";
    private static final String TAG_NAME = "name";

    JSONArray products = null;
	
	ArrayAdapter<String> myAdapter;
	CustomAutoCompleteView myAutoComplete;
	
	//Initial value in the TextField
    String[] item = new String[] {"Type a product..."};
	
	@Override
    public void onCreate(Bundle savedInstanceState) {         

       super.onCreate(savedInstanceState);    
       setContentView(R.layout.manual_add_layout);
       
      // StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
      // StrictMode.setThreadPolicy(policy);
       try{
	       // autocompletetextview is in activity_main.xml
	       myAutoComplete = (CustomAutoCompleteView) findViewById(R.id.productsList);
	        
	       // add the listener so it will tries to suggest while the user types
	       myAutoComplete.addTextChangedListener(new CustomAutoCompleteTextChangedListener(this));
	        
	       // set our adapter
	       myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, item);
	       myAutoComplete.setAdapter(myAdapter);
       } catch (NullPointerException e) {
           e.printStackTrace();
       } catch (Exception e) {
           e.printStackTrace();
       }
       
       
   }
	
	// this function is used in CustomAutoCompleteTextChangedListener.java
    public String[] getItemsFromDb(String searchTerm){
         
        // add items on the array dynamically
        /*List<MyObject> products = databaseH.read(searchTerm);
        int rowCount = products.size();
         
        String[] item = new String[rowCount];
        int x = 0;
         
        for (MyObject record : products) {
             
            item[x] = record.objectName;
            x++;
        }*/
    	
    	new LoadAllProducts().execute(searchTerm);
        

    	String[] item = new String[] {"Belgium", "France", "Italy", "Germany", "Spain"};
    	
        return item;
    }
    /**
     * Background Async Task to Load all product by making HTTP Request
     * */
    class LoadAllProducts extends AsyncTask<String, String, String> {
    	
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ManualAddActivity.this);
            pDialog.setMessage("Loading products. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }
 
        /**
         * getting All products from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_get_products, "GET", params);
 
            // Check your log cat for JSON reponse
            Log.d("All Products: ", json.toString());
 
            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);
 
                if (success == 1) {
                    // products found
                    // Getting Array of Products
                    products = json.getJSONArray(TAG_PRODUCTS);
 
                    // looping through All Products
                    for (int i = 0; i < products.length(); i++) {
                        JSONObject c = products.getJSONObject(i);
 
                        // Storing each json item in variable
                        String name = c.getString(TAG_NAME);
                        
                        Log.v(TAG_NAME, "Product" + i + ":" + name);
 
                    }
                } else {
                    // no products found
                	Log.v(TAG_NAME, "No products!");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
 
            return null;
        }
        
        @Override
        protected void onProgressUpdate(String... values) {
        	
        }
 
        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
           
 
        }
 
    }
}
