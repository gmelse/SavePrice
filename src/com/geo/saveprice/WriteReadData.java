package com.geo.saveprice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

@SuppressLint("ShowToast")
public class WriteReadData {
	
	private static final String TAG = "WriteReadData";
	
	private String hashMap_Path = Environment.getExternalStorageDirectory() + "/MyApp/HashMap/";
	private String dataNotSend_Path = Environment.getExternalStorageDirectory() + "/MyApp/DataNotSend/";
	
	private Context myContext;
	
	public WriteReadData(Context myContext){
		this.myContext = myContext;
	}
	
	//Check if there is a file which contains data that haven't sent and load them to oldProducts ArrayList
	@SuppressWarnings("unchecked")
	public void loadProducts(){
		//Create a reference to the folder that contains the file Data_Not_Sended.ser
		File dataNotSendDirectory = new File(dataNotSend_Path);
		if(dataNotSendDirectory.exists()){
			File file = new File(dataNotSend_Path, "Data_Not_Send.ser");
			if(file.exists()){
				//read and add file contents to products
				try {
					FileInputStream f = new FileInputStream(file);
					ObjectInputStream s = new ObjectInputStream(f);
					MyProducts.addToOldProducts((ArrayList<Product>) s.readObject());
					s.close();
					f.close();
				} catch (FileNotFoundException e) {
					Log.e(TAG, e.getMessage(), e);
				} catch (StreamCorruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				
				//delete file
				file.delete();
			}
		}
		
	}
	
	public void saveProducts(boolean load){
		if(load){
			loadAndShowProductsFromFile();
		}
		
		//Write To file
		File dataNotSendDirectory = new File(dataNotSend_Path);
		dataNotSendDirectory.mkdirs();
		    
		    File file = new File(dataNotSend_Path, "Data_Not_Send.ser");
		    
		    try {
				FileOutputStream f = new FileOutputStream(file);
				ObjectOutputStream s = new ObjectOutputStream(f);
				s.writeObject(MyProducts.getOldProducts());
				MyProducts.getOldProducts().clear();
				s.close();
				f.close();
		    } catch (IOException e) {
		    	Log.e(TAG, e.getMessage(), e);
			}
		 Toast toast = Toast.makeText(myContext, "Try to send data again next time", 2000);
    	 toast.show();
	}
	
	@SuppressWarnings("unchecked")
	public void loadHashMaps(){
		File file1 = new File(hashMap_Path, "HashMap_Values.ser");
		
		if(file1.exists()){
			try {
				FileInputStream f = new FileInputStream(file1);
				ObjectInputStream s = new ObjectInputStream(f);
				((MainActivity)myContext).setMap((HashMap<String, String>) s.readObject());
				s.close();
				f.close();
			} catch (FileNotFoundException e) {
				Log.e(TAG, e.getMessage(), e);
			} catch (StreamCorruptedException e) {
				Log.e(TAG, "StreamCorruptedExcpetion", e);
			} catch (IOException e) {
				Log.e(TAG, "IOException", e);
			} catch (ClassNotFoundException e) {
				Log.e(TAG, "classNotFoundException", e);
			}
		}
		
		File file2 = new File(hashMap_Path, "parallelHashMap_Values.ser");
		
		if(file2.exists()){
			try {
				FileInputStream f = new FileInputStream(file2);
				ObjectInputStream s = new ObjectInputStream(f);
				((MainActivity)myContext).setParallelMap((HashMap<String, String>) s.readObject());
				s.close();
				f.close();
			} catch (FileNotFoundException e) {
				Log.e(TAG, e.getMessage(), e);
			} catch (StreamCorruptedException e) {
				Log.e(TAG, "StreamCorruptedExcpetion", e);
			} catch (IOException e) {
				Log.e(TAG, "IOException", e);
			} catch (ClassNotFoundException e) {
				Log.e(TAG, "classNotFoundException", e);
			}
		}
	}
	
	public void saveHashMap(Map<String, String> map, String fileName){
	    File hashDirectory = new File(hashMap_Path);
	    hashDirectory.mkdirs();
	   
	    File file = new File(hashMap_Path, fileName);
	    
	    try {
			FileOutputStream f = new FileOutputStream(file);
			ObjectOutputStream s = new ObjectOutputStream(f);
			s.writeObject(map);
			//s.writeObject(((MainActivity) myContext).getMap());
			s.close();
			f.close();
	    } catch (IOException e) {
	    	Log.e(TAG, e.getMessage(), e);
		}
	}	
	
	public void loadAndShowProductsFromFile(){
		loadProducts(); //In order to be able to see the products that haven't sent and stored to a file
		
		if(!MyProducts.getOldProducts().isEmpty()){
			String productsNotSend ="Old Products:\n\n";
			for(int i=0; i<MyProducts.getOldProducts().size(); i++){
				productsNotSend = productsNotSend + MyProducts.getOldProducts().get(i).getName() + "  "
												  + MyProducts.getOldProducts().get(i).getPrice() + "\n";
			}
			productsNotSend = productsNotSend + "\n";
			((MainActivity) myContext).setOcrResult(productsNotSend);
		}
		
		
	}
	
}
