package com.gmail.boiledorange73.app.TokyoMapOld;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public final class MapPreferences {
    public static final String KEY_HIGHACCURACY = "HighAccuracy";
    public static final boolean DEFAULT_HIGHACCURACY = false;
    public static final String KEY_MAPIMAGETYPE = "MapImageType";
    public static final String DEFAULT_MAPNAMESIZE = "_12.5px";
    public static final String KEY_MAPNAMESIZE = "MapNameSize";

    public static boolean getHighAccuracy(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(MapPreferences.KEY_HIGHACCURACY,
                MapPreferences.DEFAULT_HIGHACCURACY);
    }

    public static String getMapImageType(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPreferences.getString(MapPreferences.KEY_MAPIMAGETYPE, null);
    }

    public static void setMapImageType(Context context, String value) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sharedPreferences.edit();
        if( value != null ) {
            editor.putString(MapPreferences.KEY_MAPIMAGETYPE, value);
        }
        else {
            editor.remove(MapPreferences.KEY_MAPIMAGETYPE);
        }
        editor.commit();
    }

    public static String getMapNameSize(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        String ret = sharedPreferences.getString(MapPreferences.KEY_MAPNAMESIZE, MapPreferences.DEFAULT_MAPNAMESIZE);
        if( ret != null && ret.charAt(0) == '_' ) {
            ret= ret.substring(1);
        }
        return ret;
    }

}
