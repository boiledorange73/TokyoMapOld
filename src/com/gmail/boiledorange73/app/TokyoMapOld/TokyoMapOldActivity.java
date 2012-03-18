package com.gmail.boiledorange73.app.TokyoMapOld;

import com.gmail.boiledorange73.ut.map.JSBridge;
import com.gmail.boiledorange73.ut.map.MapActivityBase;

import android.app.Activity;

public class TokyoMapOldActivity extends MapActivityBase {

    @Override
    protected String getWebUrl() {
        return this.getString(R.string.weburl);
    }

    @Override
    protected String getAboutUrl() {
        return this.getString(R.string.abouturl);
    }

    @Override
    protected Class<? extends Activity> getPreferenceActivityClass() {
        return MapPreferencesActivity.class;
    }

    @Override
    protected boolean isHighAccuracy() {
        return MapPreferences.getHighAccuracy(this);
    }

    @Override
    public String onQuery(JSBridge bridge, String code, String message) {
        if( "getExtension".equals(code) ) {
            MapPreferences.getMapImageType(this);
        }
        return super.onQuery(bridge, code, message);
    }

    @Override
    protected String getMapNameSize() {
        return MapPreferences.getMapNameSize(this);
    }

    @Override
    protected Class<? extends Activity> getGeocoderActivityClass() {
        return GeocoderActivity.class;
    }

}