package com.geo.saveprice;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONException;

import android.content.Context;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;

/**
  *
  * @author gmel
  * This class is where the program requeries the database each 
  * time a user types a character on the AutocompleteTextView.
  * 
  */
 
public class CustomAutoCompleteTextChangedListener implements TextWatcher{
 
    public static final String TAG = "CustomAutoCompleteTextChangedListener";
    Context context;
     
    public CustomAutoCompleteTextChangedListener(Context context){
        this.context = context;
    }
     
    @Override
    public void afterTextChanged(Editable s) {
        // TODO Auto-generated method stub
         
    }
 
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
            int after) {
        // TODO Auto-generated method stub
         
    }
    
    @Override
    public void onTextChanged(CharSequence userInput, int start, int before, int count) {
 
        // if you want to see in the logcat what the user types
        Log.e(TAG, "User input: " + userInput);
 
        //MainActivity mainActivity = ((MainActivity) context);
        ManualAddActivity amaActivity = ((ManualAddActivity) context);
         
        // query the database based on the user input
        amaActivity.item = amaActivity.getItemsFromDb(userInput.toString());
         
        // update the adapater
        amaActivity.myAdapter.notifyDataSetChanged();
        amaActivity.myAdapter = new ArrayAdapter<String>(amaActivity, android.R.layout.simple_dropdown_item_1line, amaActivity.item);
        amaActivity.myAutoComplete.setAdapter(amaActivity.myAdapter);
         
    }
    
}
