/*
 * Copyright (C) 2008 ZXing authors
 * Copyright 2014 George Melidis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.geo.saveprice;

import com.geo.saveprice.R;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Class to handle preferences that are saved across sessions of the app. Shows
 * a hierarchy of preferences to the user, organized into sections. These
 * preferences are displayed in the options menu that is shown when the user
 * presses the MENU button and then Settings.
 * 
 * The code for this class was adapted from the ZXing project: http://code.google.com/p/zxing
 */
public class PreferencesActivity extends PreferenceActivity implements
  OnSharedPreferenceChangeListener {
  
  // Preference keys not carried over from ZXing project
  public static final String KEY_SOURCE_LANGUAGE_PREFERENCE = "langSource";
  //public static final String KEY_CHARACTER_WHITELIST = "preference_character_whitelist";
  public static final String KEY_DOWNLOAD_LANGUAGE_PREFERENCE = "langDownload";

  private static ListPreference listPreferenceSourceLanguage;
  private static Preference downloadLanguage;
  
  private static SharedPreferences sharedPreferences;
  
  private static final String TAG = "PreferencesActivity";
  
  private static String LN;
  
  private ProgressDialog dialog;
  
  private String languagePath = Environment.getExternalStorageDirectory() + "/MyApp/";

  /**
   * Set the default preference values.
   * 
   * @param Bundle
   *            savedInstanceState the current Activity's state, as passed by
   *            Android
   */
  @SuppressWarnings("deprecation")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);
    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    
    listPreferenceSourceLanguage = (ListPreference) getPreferenceScreen().findPreference(KEY_SOURCE_LANGUAGE_PREFERENCE);
    downloadLanguage = (Preference) getPreferenceScreen().findPreference(KEY_DOWNLOAD_LANGUAGE_PREFERENCE);
    
    dialog = new ProgressDialog(this);
   
    downloadLanguage.setOnPreferenceClickListener(new OnPreferenceClickListener() {
    	public boolean onPreferenceClick(Preference preference) {
	            //open browser or intent here
	       	 new LanguageAsyncTask(dialog, "ell", "Greek", PreferencesActivity.this).execute(languagePath);
	       	 
	       	 Handler handler = new Handler(); 
	            handler.postDelayed(new Runnable() { 
	                 public void run() { 
	                	 new LanguageAsyncTask(dialog, "eng", "English", PreferencesActivity.this).execute(languagePath);
	                 } 
	            }, 3000); 
            return false;
        }
      });       
   
  }
  
  /**
   * Interface definition for a callback to be invoked when a shared
   * preference is changed. Sets summary text for the app's preferences. Summary text values show the
   * current settings for the values.
   * 
   * @param sharedPreferences
   *            the Android.content.SharedPreferences that received the change
   * @param key
   *            the key of the preference that was changed, added, or removed
   */
  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {    
    // Update preference summary values to show current preferences
    if(key.equals(KEY_SOURCE_LANGUAGE_PREFERENCE)) {
    	LN = getOcrLanguageName(getBaseContext(), sharedPreferences.getString(key, MainActivity.DEFAULT_SOURCE_LANGUAGE_CODE));
    	//LC = sharedPreferences.getString(key, MainActivity.DEFAULT_SOURCE_LANGUAGE_CODE);
    	
      // Set the summary text for the source language name
      listPreferenceSourceLanguage.setSummary(LN);
      
    }
    
  }

  /**
   * Sets up initial preference summary text
   * values and registers the OnSharedPreferenceChangeListener.
   */
  @SuppressWarnings("deprecation")
  @Override
  protected void onResume() {
    super.onResume();
    // Set up the initial summary values
    listPreferenceSourceLanguage.setSummary(getOcrLanguageName(getBaseContext(), sharedPreferences.getString(KEY_SOURCE_LANGUAGE_PREFERENCE, MainActivity.DEFAULT_SOURCE_LANGUAGE_CODE)));
  
    //For the downloadLanguage Preference we don't need to setSummary because the Summary is always the same 
    
    // Set up a listener whenever a key changes
    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
  }

  /**
   * Called when Activity is about to lose focus. Unregisters the
   * OnSharedPreferenceChangeListener.
   */
  @SuppressWarnings("deprecation")
  @Override
  protected void onPause() {
    super.onPause();
    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
  }
  
  /**
	 * Map the given ISO 639-3 language code to a name of a language, for example,
	 * "Spanish"
	 * 
	 * @param context
	 *            interface to calling application environment. Needed to access
	 *            values from strings.xml.
	 * @param languageCode
	 *            ISO 639-3 language code
	 * @return language name
	 */
	public static String getOcrLanguageName(Context context, String languageCode) {
		Resources res = context.getResources();
		String[] language6393 = res.getStringArray(R.array.Language_Data_File_Name);
		String[] languageNames = res.getStringArray(R.array.LanguageNames);
		int len;

		// Finds the given language code in the iso6393 array, and takes the name with the same index
		// from the languagenames array.
		for (len = 0; len < language6393.length; len++) {
			if (language6393[len].equals(languageCode)) {
				Log.d(TAG, "getOcrLanguageName: " + languageCode + "->"
						+ languageNames[len]);
				return languageNames[len];
			}
		}
		
		Log.d(TAG, "languageCode: Could not find language name for ISO 693-3: "
				+ languageCode);
		return languageCode;
	}
}