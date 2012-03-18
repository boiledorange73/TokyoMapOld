package com.gmail.boiledorange73.app.TokyoMapOld;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class MapPreferencesActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      this.addPreferencesFromResource(R.xml.mapoldpreferences);
    }
}
