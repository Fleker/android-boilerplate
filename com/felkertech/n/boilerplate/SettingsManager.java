package com.felkertech.n.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.Iterator;


/**
 * Version 1.1
 * Created by N on 14/9/2014.
 * Last Edited 25/3/2015
 *   * Support for floats
 * Last Edited 13/5/2015
 *   * Support for syncing data to wearables
 */
public class SettingsManager {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String TAG = "weather::PreferenceManager";
    private Context mContext;
    public SettingsManager(Activity activity) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        mContext = activity;
//        sharedPreferences = getDefaultSharedPreferences(activity);
//        sharedPreferences = activity.getPreferences(Context.MODE_PRIVATE);
//        sharedPreferences = activity.getSharedPreferences(activity.getString(R.string.PREFERENCES), Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
//        Log.d(TAG, sharedPreferences.getAll().keySet().iterator().next());
    }
    public SettingsManager(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mContext = context;
        editor = sharedPreferences.edit();
    }
    public String getString(int resId) {
        return getString(mContext.getString(resId));
    }
    public String getString(String key) {
        return getString(key, "NULL", "");
    }
    public String getString(int resId, String def) {
        return getString(mContext.getString(resId), def);
    }
    public String getString(String key, String def) {
        return getString(key, "NULL", def);
    }
    public String getString(String key, String val, String def) {
//        Log.d(TAG, key + " - " + val + " - " + def);
        String result = sharedPreferences.getString(key, val);
        if(result == "NULL") {
            editor.putString(key, def);
            Log.d(TAG, key + ", " + def);
            editor.commit();
            result = def;
        }
        return result;
    }
    public String setString(int resId, String val) {
        return setString(mContext.getString(resId), val);
    }
    public String setString(String key, String val) {
        editor.putString(key, val);
        editor.commit();
        return val;
    }
    public boolean getBoolean(int resId) {
        return getBoolean(mContext.getString(resId));
    }
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }
    public boolean getBoolean(String key, boolean def) {
        boolean result = sharedPreferences.getBoolean(key, def);
        editor.putBoolean(key, result);
        editor.commit();
        return result;
    }
    public boolean setBoolean(int resId, boolean val) {
        return setBoolean(mContext.getString(resId), val);
    }
    public boolean setBoolean(String key, boolean val) {
        editor.putBoolean(key, val);
        editor.commit();
        return val;
    }

    public float getFloat(int resId, float def) {
        return sharedPreferences.getFloat(mContext.getString(resId), def);
    }
    public float getFloat(String key, float def) {
        return sharedPreferences.getFloat(key, def);
    }
    public int getInt(int resId) {
        return sharedPreferences.getInt(mContext.getString(resId), 0);
    }
    public int setInt(int resId, int val) {
        editor.putInt(mContext.getString(resId), val);
        editor.commit();
        return val;
    }
    public long getLong(int resId) {
        return getLong(resId, 0);
    }
    public long getLong(int resId, long def) {
/*
        Log.d(TAG, "I want to get a long");
        Log.d(TAG, "Want to get "+resId+", "+mContext.getString(resId)+"; ");
        Log.d(TAG, "Gets value "+sharedPreferences.getString(mContext.getString(resId), "NULL"));
*/
        return sharedPreferences.getLong(mContext.getString(resId), def);
    }
    public long setLong(int resId, long val) {
        editor.putLong(mContext.getString(resId), val);
        editor.commit();
        return val;
    }

    //Default Stuff
    public static SharedPreferences getDefaultSharedPreferences(Context context) {
        return context.getSharedPreferences(getDefaultSharedPreferencesName(context),
                getDefaultSharedPreferencesMode());
    }

    private static String getDefaultSharedPreferencesName(Context context) {
        return context.getPackageName() + "_preferences";
    }

    private static int getDefaultSharedPreferencesMode() {
        return Context.MODE_PRIVATE;
    }

    /* SYNCABLE SETTINGS MANAGER */
    private Syncable syncable;
    private boolean syncEnabled = false;
    private GoogleApiClient syncClient;

    public boolean setSyncableSettingsManager(GoogleApiClient gapi, Syncable s) {
        syncable = s;
        syncClient = gapi;
        ConnectionResult wearable = gapi.getConnectionResult(Wearable.API);
        if(wearable.isSuccess()) {
            //Sync enabled
            syncEnabled = true;
            return true;
        }

        return false;
    }

    /**
     * Pushes all settings to other devices
     */
    public void pushData() {
        Log.d(TAG, "Starting to push stuff");
        Iterator<String> keys = sharedPreferences.getAll().keySet().iterator();
        PutDataMapRequest dataMap = PutDataMapRequest.create("/prefs");
        while(keys.hasNext()) {
            String key = keys.next();
            Object v = sharedPreferences.getAll().get(key);
            Log.d(TAG, key + " = " + v+", "+v.getClass().toString());
            if(v.getClass().toString().contains("Boolean")) {
                Log.d(TAG, "Sending boolean "+key + " = " + v);
                dataMap.getDataMap().putBoolean(key, (Boolean) v);
            } else if(v.getClass().toString().contains("String")) {
                Log.d(TAG, "Sending string "+key + " = " + v);
                dataMap.getDataMap().putString(key, (String) v);
            }
        }
        PutDataRequest request = dataMap.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(syncClient, request);
    }
    public PutDataRequest getSettingsManagerDataMap() {
        Log.d(TAG, "Starting to grab and put stuff");
        Iterator<String> keys = sharedPreferences.getAll().keySet().iterator();
        PutDataMapRequest dataMap = PutDataMapRequest.create("/prefs");
        DataMap dm = dataMap.getDataMap();
        while(keys.hasNext()) {
            String key = keys.next();
            Object v = sharedPreferences.getAll().get(key);
//            Log.d(TAG, key + " = " + v+", "+v.getClass().toString());
            if(v.getClass().toString().contains("Boolean")) {
//                Log.d(TAG, "Is boolean");
                Log.d(TAG, "Boolean "+key + " = " + v);
                dm.putBoolean(key, (Boolean) v);
            } else if(v.getClass().toString().contains("String")) {
                Log.d(TAG, "String "+key + " = " + v);
                dm.putString(key, (String) v);
            }
        }
        return dataMap.asPutDataRequest();
    }
    public void pushData(int resId, Object value) {
        pushData(mContext.getString(resId), value);
    }
    public void pushData(String key, Object value) {
        PutDataMapRequest dataMap = PutDataMapRequest.create("/prefs");

/*
        dataMap.getDataMap().putString(
                mContext.getString(R.string.WEAR_SHAPE), getString(R.string.WEAR_SHAPE, "Round")
        );
        dataMap.getDataMap().putBoolean(
                mContext.getString(R.string.WEAR_VIBRATE), getBoolean(R.string.WEAR_VIBRATE)
        );
*/

        PutDataRequest request = dataMap.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(syncClient, request);
    }

    public void pullData(String key, Object value) {

    }

    /**
     * Retrieves any changes in settings and applies them to the correct key
     * @param dataEvents DataEventBuffer from the service callback
     */
    public void pullData(DataEventBuffer dataEvents) {
        Iterator<DataEvent> eventIterator = dataEvents.iterator();
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                Log.d("TAG", "DataItem changed: " + event.getDataItem().getUri());
                DataMap dataMap = DataMap.fromByteArray(event.getDataItem().getData());
                Iterator<String> stringIterator = dataMap.keySet().iterator();
                while(stringIterator.hasNext()) {
                    String key = stringIterator.next();
                    Object v = dataMap.get(key);
                    if(v.getClass().toString().equals("java.lang.Boolean")) {
                        Log.d(TAG, "Retrieved boolean "+key+" = "+v);
                        setBoolean(key, (Boolean) v);
                    } else if(v.getClass().toString().equals("java.lang.String")) {
                        Log.d(TAG, "Retrieved string "+key+" = "+v);
                        setString(key, (String) v);
                    }
                }
            }
        }
    }

    public interface Syncable {
        void onItemEdited(String key, Object value);
        void onItemReceived(String key, Object value);
    }
}