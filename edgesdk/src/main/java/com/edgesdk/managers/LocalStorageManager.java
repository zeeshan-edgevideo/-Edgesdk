package com.edgesdk.managers;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class LocalStorageManager {
    private SharedPreferences prefs;
    static SharedPreferences.Editor editor;
    public LocalStorageManager(Context context) {
        editor = context.getSharedPreferences("edge_local_storage", MODE_PRIVATE).edit();
        prefs = context.getSharedPreferences("edge_local_storage", MODE_PRIVATE);
    }

    public void storeStringValue(String value, String key){
        editor.putString(key,value);
        editor.apply();
    }
    public void storeFloatValue(float value,String key){
        editor.putFloat(key,value);
        editor.apply();
    }

    public void storeBooleanValue(boolean value,String key){
        editor.putBoolean(key,value);
        editor.apply();
    }
    public void storeIntValue(int value,String key){
        editor.putInt(key,value);
        editor.apply();
    }

    public String getStringValue(String key){
        return prefs.getString(key,null);
    }
    public boolean getBooleanValue(String key){
        return prefs.getBoolean(key,false);
    }
    public float getFloatValue(String key){
        return prefs.getFloat(key,0);
    }
    public int getIntValue(String key){
        return prefs.getInt(key,-1);
    }
}
