package com.felkertech.n.virtualpets.Utils;

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
import java.util.Objects;


/**
 * Version 1.1
 * Created by N on 14/9/2014.
 * Last Edited 13/5/2015
 *   * Support for syncing data to wearables
 */
public class SettingsManager {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String TAG = "PreferenceManager";
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
    public Context getContext() {
        return mContext;
    }
    public String getString(int resId) {
        return getString(mContext.getString(resId));
    }
    public String getString(String key) {
        return getString(key, "-1", "");
    }
    public String getString(int resId, String def) {
        return getString(mContext.getString(resId), def);
    }
    public String getString(String key, String def) {
        return getString(key, "-1", def);
    }
    public String getString(String key, String val, String def) {
//        Log.d(TAG, key + " - " + val + " - " + def);
        String result = sharedPreferences.getString(key, val);
        assert result != null;
        if(result.equals("-1")) {
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

    public int getInt(int resId) {
        return sharedPreferences.getInt(mContext.getString(resId), 0);
    }
    public int setInt(int resId, int val) {
        return setInt(mContext.getString(resId), val);
    }
    public int setInt(String key, int val) {
        editor.putInt(key, val);
        editor.commit();
        return val;
    }
    public long getLong(int resId) {
        return sharedPreferences.getLong(mContext.getString(resId), 0);
    }
    public long setLong(int resId, long val) {
        return setLong(mContext.getString(resId), val);
    }
    public long setLong(String key, long val) {
        editor.putLong(key, val);
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
    private boolean syncEnabled = false;
    private GoogleApiClient syncClient;

    public boolean setSyncableSettingsManager(GoogleApiClient gapi) {
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
        if(syncClient == null) {
            Log.d(TAG, "Can't sync");
            return;
        }
        Log.d(TAG, "Starting to push stuff");
        Iterator<String> keys = sharedPreferences.getAll().keySet().iterator();
        PutDataMapRequest dataMap = PutDataMapRequest.create("/prefs");
        while(keys.hasNext()) {
            String key = keys.next();
            Object v = sharedPreferences.getAll().get(key);
            if(v.getClass().toString().contains("Boolean")) {
                dataMap.getDataMap().putBoolean(key, (Boolean) v);
                Log.d(TAG, "Sending boolean "+key+" = "+v);
            } else if(v.getClass().toString().contains("String")) {
                dataMap.getDataMap().putString(key, (String) v);
                Log.d(TAG, "Sending string "+key+" = "+v);
            } else if (v.getClass().toString().contains("Integer")) {
                dataMap.getDataMap().putInt(key, (int) v);
                Log.d(TAG, "Sending int "+key+" = "+v);
            } else if (v.getClass().toString().contains("Long")) {
                dataMap.getDataMap().putLong(key, (long) v);
                Log.d(TAG, "Sending long "+key+" = "+v);
            }
        }
        PutDataRequest request = dataMap.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(syncClient, request);
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
                    if(v.getClass().toString().contains("Boolean")) {
                        setBoolean(key, (Boolean) v);
                    } else if(v.getClass().toString().contains("String")) {
                        setString(key, (String) v);
                    } else if (v.getClass().toString().contains("Integer")) {
                        setInt(key, (int) v);
                    } else if (v.getClass().toString().contains("Long")) {
                        setLong(key, (long) v);
                    }
                }
            }
        }
    }
}
