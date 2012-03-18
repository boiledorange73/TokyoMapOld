package com.gmail.boiledorange73.ut.map;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * The activity of this software.
 * 
 * @author yellow
 * 
 */
public abstract class MapActivityBase extends Activity implements
        JSBridgeListener {
    private static final int ID_MYLOCATION = 1;
    private static final int ID_MORE_EXIT = 2;
    private static final int ID_MORE_PREFERENCES = 3;
    private static final int ID_MORE_ABOUT = 4;
    private static final int ID_MAPTYPE = 5;
    private static final int ID_GEOCODER = 6;

    private static final int RC_GEOCODER = 1;

    private static final String JSPREFIX = "mapviewer";

    /** Latest status of the map */
    private MapState mMapState;
    /** Bridge JS to Dalvik */
    private JSBridge mJSBridge;
    /** Web view */
    private WebView mWebView;

    /** True if JS has been loaded. */
    private boolean mLoaded = false;
    /** Statements queue for JS */
    private LinkedList<String> mStatements = new LinkedList<String>();

    /** List of maptype whose element contains "id" and "name". */
    private ArrayList<ValueText<String>> mMaptypeList;

    /**
     * Gets URL for the application. Usually, "file:///android_asset/index.html"
     */
    protected abstract String getWebUrl();

    /**
     * Gets URL for the about dialog. Usually,
     * "file:///android_asset/about_(lang).html"
     */
    protected abstract String getAboutUrl();

    /** Gets CSS size of map name text. i.e. "32px", "0.8em". */
    protected abstract String getMapNameSize();

    /**
     * Gets whether the application currently requires high accuracy location
     * provider.
     */
    protected abstract boolean isHighAccuracy();

    /** Gets the activity for preferences. */
    protected abstract Class<? extends Activity> getPreferenceActivityClass();

    /** Gets whether the activity has geocoder. */
    protected abstract Class<? extends Activity> getGeocoderActivityClass();

    private void initActionBar() {
        Class<?> windowClass = Window.class;
        try {
            Field field = windowClass.getField("FEATURE_ACTION_BAR");
            int fieldValue = field.getInt(null);
            this.getWindow().requestFeature(fieldValue);
        }
        catch(NoSuchFieldException e) {
            // DOES NOTHING
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    // -------- Activity
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.initActionBar();
        // init flag
        this.mLoaded = false;
        this.mStatements.clear();
        // appends views
        RelativeLayout layoutRoot = new RelativeLayout(this);
        this.addContentView(layoutRoot, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT));
        this.mWebView = new WebView(this);
        this.mWebView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
        this.mWebView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT));
        layoutRoot.addView(this.mWebView);
        this.mWebView.getSettings().setJavaScriptEnabled(true);
        // load htmls
        Uri.Builder uriBuilder = (Uri.parse(this.getWebUrl())).buildUpon();
        String url = uriBuilder.toString();
        this.mWebView.loadUrl(url);
        // registers bridge in JS.
        this.mJSBridge = new JSBridge(this);
        this.mWebView.addJavascriptInterface(this.mJSBridge, "jsBridge");
    }

    /** Called when the activity is destroyed */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.mJSBridge != null) {
            this.mJSBridge.expire();
            this.mStatements.clear();
            this.mJSBridge = null;
        }
    }

    /** Shows the about dialog. */
    public void showAbout() {
        AlertDialogHelper.showAbout(this, this.getAppName(),
                this.getVersionName(), this.getAboutUrl(),
                this.getApplicationInfo().icon);
    }

    /**
     * Shows the dialog to confirm to exit. If user pushes "ok", this will exit.
     */
    public void showExitConfirmation() {
        AlertDialogHelper.showExitConfirmationDialog(this,
                Messages.getString("W_CONFIRMATION"),
                Messages.getString("P_CONFIRM_EXIT"));

    }

    /** Called whe the menu is created. */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // mylocation
        menu.add(Menu.NONE, ID_MYLOCATION, Menu.NONE,
                Messages.getString("W_MYLOCATION")).setIcon(
                android.R.drawable.ic_menu_mylocation);
        // maptypes (invisible)
        menu.add(Menu.NONE, ID_MAPTYPE, Menu.NONE,
                Messages.getString("W_CHANGE_MAP")).setIcon(
                android.R.drawable.ic_menu_mapmode).setVisible(false);
        // geocoder
        if( this.getGeocoderActivityClass() != null ) {
            menu.add(Menu.NONE, ID_GEOCODER, Menu.NONE, Messages.getString("W_GEOCODER")).setIcon(android.R.drawable.ic_menu_search);
        }
        // more
        SubMenu smMore = menu.addSubMenu(Messages.getString("W_MORE")).setIcon(
                android.R.drawable.ic_menu_more);
        if (this.getPreferenceActivityClass() != null) {
            smMore.add(Menu.NONE, ID_MORE_PREFERENCES, Menu.NONE,
                    Messages.getString("W_PREFERENCES"));
        }
        smMore.add(Menu.NONE, ID_MORE_ABOUT, Menu.NONE,
                Messages.getString("W_ABOUT"));
        smMore.add(Menu.NONE, ID_MORE_EXIT, Menu.NONE,
                Messages.getString("W_EXIT"));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch( requestCode ) {
        case MapActivityBase.RC_GEOCODER:
            if( resultCode == Activity.RESULT_OK ) {
                double lat = data.getDoubleExtra("lat", Double.NaN);
                double lon = data.getDoubleExtra("lon", Double.NaN);
                if( !Double.isNaN(lat) && !Double.isNaN(lon) ) {
                    this.restoreMapState(null, lon, lat, null);
                }
            }
            break;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // switch maptypes
        MenuItem miMapType = menu.findItem(ID_MAPTYPE);
        if( miMapType != null ) {
            if( this.mMaptypeList != null && this.mMaptypeList.size() > 0 ) {
                miMapType.setVisible(true);
            }
            else {
                miMapType.setVisible(false);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }
    /** Called when one of the menu item is selected. */
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
        case ID_MAPTYPE:
            if (this.mMaptypeList != null && this.mMaptypeList.size() > 0) {
                AlertDialogHelper
                        .<ValueText<String>> showChoice(
                                this,
                                item.getTitle().toString(),
                                this.mMaptypeList,
                                new AlertDialogHelper.OnChoiceListener<ValueText<String>>() {
                                    @Override
                                    public void onChoice(
                                            DialogInterface dialog,
                                            ValueText<String> item) {
                                        if (item != null
                                                && item.getValue() != null) {
                                            MapActivityBase.this
                                                    .restoreMapState(
                                                            item.getValue(),
                                                            null, null, null);
                                        }
                                    }
                                });
            }
            return true;
        case ID_MYLOCATION:
            this.startLocationService(this.isHighAccuracy());
            return true;
        case ID_MORE_EXIT:
            this.showExitConfirmation();
            return true;
        case ID_MORE_PREFERENCES:
            Class<? extends Activity> preferenceActivityClass = this
                    .getPreferenceActivityClass();
            if (preferenceActivityClass != null) {
                this.startActivity(new Intent(this, preferenceActivityClass));
            }
            return true;
        case ID_MORE_ABOUT:
            this.showAbout();
            return true;
        case ID_GEOCODER:
            Class<? extends Activity> geocoderClass = this.getGeocoderActivityClass();
            if( geocoderClass != null ) {
                Intent intent = new Intent(this, geocoderClass);
                this.startActivityForResult(intent, RC_GEOCODER);
            }
        }
        return false;
    }

    /** Called when the current status is saved. */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            MapState mapState = this.getCurrentMapState();
            if (mapState != null) {
                outState.putParcelable("MapState", mapState);
            }
        }
    }

    /** Called when the latest status is restored. */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.restoreSavedInstanceState(savedInstanceState);
    }

    /**
     * Restores the latest status of this activity.
     * 
     * @param savedInstanceState
     *            The instance which has the latest status.
     */
    private void restoreSavedInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        if (savedInstanceState.containsKey("MapState")) {
            this.mMapState = (MapState) savedInstanceState
                    .getParcelable("MapState");
            if (this.mMapState != null) {
                this.restoreMapState(this.mMapState.id, this.mMapState.lon,
                        this.mMapState.lat, this.mMapState.z);
            }
        }
    }

    private void restoreMapState(String id, Double lon, Double lat, Integer z) {
        String sid = id != null ? "\"" + id + "\"" : "null";
        String slon = lon != null ? String.valueOf(lon) : "null";
        String slat = lat != null ? String.valueOf(lat) : "null";
        String sz = z != null ? String.valueOf(z) : "null";
        String cmd = "javascript:" + JSPREFIX + ".map.restoreMapState(" + sid
                + "," + slon + "," + slat + "," + sz + ")";
        this.execute(cmd);
    }

    // -------- JS Listener
    /** True if this is waiting for JS:getCurrentMapState() returns. */
    private boolean mWaitingForgetCurrentMapState;

    /**
     * Called when JS sends the message.
     * 
     * @param bridge
     *            Receiver instance.
     * @param code
     *            The code name. This looks like the name of function.
     * @param message
     *            The message. This looks like the argument of function. This is
     *            usually expressed as JSON.
     */
    @Override
    public void onArriveMessage(JSBridge bridge, String code, String message) {
        if (code == null) {
        } else if (code.equals("onLoad")) {
            this.mMaptypeList = new ArrayList<ValueText<String>>();
            this.mLoaded = true;
            this.execute(null);
            JSONArray json = null;
            try {
                json = new JSONArray(message);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (json != null) {
                for (int n = 0; n < json.length(); n++) {
                    JSONObject one = null;
                    try {
                        one = json.getJSONObject(n);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (one != null) {
                        String id = null, name = null;
                        try {
                            id = one.getString("id");
                            name = one.getString("name");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (id != null && name != null) {
                            this.mMaptypeList.add(new ValueText<String>(id,
                                    name));
                        }
                    }
                }
            }
        } else if (code.equals("getCurrentMapState")) {
            this.mWaitingForgetCurrentMapState = false;
            if (message == null || !(message.length() > 0)) {
                // DOES NOTHING
            } else {
                try {
                    JSONObject json = new JSONObject(message);
                    this.mMapState = new MapState();
                    this.mMapState.id = json.getString("id");
                    this.mMapState.lat = json.getDouble("lat");
                    this.mMapState.lon = json.getDouble("lon");
                    this.mMapState.z = json.getInt("z");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Called when JS calls the function.
     * 
     * @param bridge
     *            Receiver instance.
     * @param code
     *            The code name. This looks like the name of function.
     * @param message
     *            The message. This looks like the argument of function. This is
     *            usually expressed as JSON.
     * @return Returned message. This is usally expressed as JSON.
     */
    @Override
    public String onQuery(JSBridge bridge, String code, String message) {
        if ("getLC".equals(code)) {
            /* "en", "ja" ... */
            return Locale.getDefault().getLanguage();
        }
        else if("getMapNameSize".equals(code)) {
            return this.getMapNameSize();
        }
        /*
         * if (code.equals("tileUrl")) { if (message == null ||
         * !(message.length() > 0)) { return null; } String ax = null, ay =
         * null, az = null, map = null; try { JSONObject json = new
         * JSONObject(message); if( json.has("map") ) { map =
         * json.getString("map"); } ax = json.getString("x"); ay =
         * json.getString("y"); az = json.getString("z"); } catch (JSONException
         * e) { e.printStackTrace(); return null; } if (ax == null || ay == null
         * || az == null || !ax.matches("-?[0-9]+") || !ay.matches("-?[0-9]+")
         * || !az.matches("-?[0-9]+")) { return null; } int x =
         * Integer.parseInt(ax); int y = Integer.parseInt(ay); int z =
         * Integer.parseInt(az); return this.calculateTileUrl(map, z, x, y); }
         */
        return null;
    }

    // -------- local methods
    /**
     * (LOCAL) Gets the version name.
     */
    private String getVersionName() {
        PackageInfo info = null;
        try {
            info = this.getPackageManager().getPackageInfo(
                    this.getPackageName(), PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return info != null ? info.versionName : null;
    }

    /**
     * (LOCAL) Gets the application name. 1. Returns label attribute of
     * application element if exists. 2. Returns the title of the activity. 3.
     * Returns empty string (not null).
     */
    private String getAppName() {
        CharSequence appinfoLabel = this.getPackageManager()
                .getApplicationLabel(this.getApplicationInfo());
        if (appinfoLabel != null) {
            return appinfoLabel.toString();
        }
        CharSequence title = this.getTitle();
        if (title != null) {
            return title.toString();
        }
        return "";
    }

    /**
     * (LOCAL) Gets the current status of the map. This method request JS to
     * send the status, waiting for JS and deserializes JSON text.
     * 
     * @return Current status of the map.
     */
    private synchronized MapState getCurrentMapState() {
        long timedout;

        timedout = System.currentTimeMillis() + 1000;
        while (this.mWaitingForgetCurrentMapState) {
            try {
                Thread.sleep(100);
                if (System.currentTimeMillis() > timedout) {
                    return null; // TIMEDOUT
                }
            } catch (InterruptedException e) {
                // interrupted
                return null;
            }
        }
        // Turns on the flag
        this.mWaitingForgetCurrentMapState = true;
        // Calls JS
        this.execute("javascript:" + JSPREFIX + ".map.getCurrentMapState()");
        // Waits
        timedout = System.currentTimeMillis() + 1000;
        while (this.mWaitingForgetCurrentMapState) {
            try {
                Thread.sleep(100);
                if (System.currentTimeMillis() > timedout) {
                    return null; // TIMEDOUT
                }
            } catch (InterruptedException e) {
                // interrupted
                return null;
            }
        }
        return this.mMapState;
    }

    /**
     * Executes the statement. If the JS is not loaded completely, the statement
     * is queued.
     * 
     * @param statement
     *            The statement (URL). This starts with "javascript:".
     */
    private void execute(String statement) {
        if (statement != null) {
            this.mStatements.add(statement);
        }
        if (this.mLoaded) {
            while (!this.mStatements.isEmpty()) {
                String s = this.mStatements.poll();
                if (s != null) {
                    this.mWebView.loadUrl(s);
                }
            }
        }
    }

    // --------
    // JS bridge
    // --------
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Timer mLocationTimer;
    protected long mTimedOut;
    private Toast mToast;

    /**
     * Starts location services and timer to stop after specfied time.
     * 
     * @see http://d.hatena.ne.jp/orangesignal/20101223/1293079002
     */
    private void startLocationService(boolean highaccuracy) {
        //
        this.stopLocationService();
        this.mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (this.mLocationManager == null) {
            // this device has no location service. DOES NOTHING.
            return;
        }
        // searches for high accuracy provider.
        String bestProvider = null;
        if (highaccuracy) {
            Criteria criteria = new Criteria();
            criteria.setBearingRequired(false);
            criteria.setSpeedRequired(false);
            criteria.setAltitudeRequired(false);
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            bestProvider = this.mLocationManager
                    .getBestProvider(criteria, true);
        }
        // generates list of providers.
        List<String> providerList = null;
        if (bestProvider != null) {
            // high accuracy found.
            providerList = new ArrayList<String>();
            providerList.add(bestProvider);
        } else {
            // all providers
            providerList = this.mLocationManager.getProviders(true);
        }

        if (providerList == null) {
            // Shows the dialog which let user to open system preference.
            AlertDialogHelper.showConfirmationDialog(this, Messages
                    .getString("P_LOCATION_PROVIDER_ERROR"), Messages
                    .getString("P_CONFIRM_OPEN_LOACTIONPROVIDER_SETTINGS"),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog,
                                final int which) {
                            try {
                                startActivity(new Intent(
                                        "android.settings.LOCATION_SOURCE_SETTINGS"));
                            } catch (final ActivityNotFoundException e) {
                                // DOES NOTHING
                                e.printStackTrace();
                            }
                        }
                    });
            this.stopLocationService();
            return;
        }
        // Starts timer. Checks whether timed out frequently,
        // and stops location services when specified time comes.
        this.mLocationTimer = new Timer(true);
        this.mTimedOut = System.currentTimeMillis() + 30000L;
        final Handler handler = new Handler();
        final MapActivityBase finalThis = this;
        this.mToast = Toast.makeText(this,
                Messages.getString("P_MYLOCATION_FINDING"), Toast.LENGTH_SHORT);
        this.mToast.show();
        this.mLocationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Sends the procedure checks whether timed out.
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        long current = System.currentTimeMillis();
                        if (current >= finalThis.mTimedOut) {
                            // timed out.
                            Toast.makeText(
                                    finalThis,
                                    Messages.getString("P_MYLOCATION_NOTFOUND"),
                                    Toast.LENGTH_LONG).show();
                            finalThis.stopLocationService();
                        } else {
                            // NOT timed out. Shows the TOAST.
                            finalThis.mToast.show();
                        }
                    }
                });
            }
        }, 0L, 1000L);
        // starts
        this.mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                // New location got. Sends it to JS.
                setLocation(location, true);
            }

            @Override
            public void onProviderDisabled(final String provider) {
                // DOES NOTHING.
            }

            @Override
            public void onProviderEnabled(final String provider) {
                // DOES NOTHING.
            }

            @Override
            public void onStatusChanged(final String provider,
                    final int status, final Bundle extras) {
                // DOES NOTHING.
            }
        };
        for (String provider : providerList) {
            this.mLocationManager.requestLocationUpdates(provider, 0, 0,
                    this.mLocationListener);
        }
    }

    /**
     * Stops location services and TOAST.
     */
    private void stopLocationService() {
        // Stops TOAST and purges it.
        if (this.mToast != null) {
            this.mToast.cancel();
            this.mToast = null;
        }
        // Stops timer and purges it.
        if (this.mLocationTimer != null) {
            this.mLocationTimer.cancel();
            this.mLocationTimer.purge();
            this.mLocationTimer = null;
        }
        // Stops location manager and purges.
        if (this.mLocationManager != null) {
            if (this.mLocationListener != null) {
                this.mLocationManager.removeUpdates(this.mLocationListener);
                this.mLocationListener = null;
            }
            this.mLocationManager = null;
        }
    }

    /**
     * Called when configuration of this device changes. Currently, this method
     * refreshes {@link com.gmail.boiledorange73.ut.map.Messages}.
     * 
     * @param newConfig
     *            New configuration.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Messages.refresh();
    }

    /**
     * Sets the location for center of the map.
     * 
     * @param location
     *            New location.
     * @param toast
     *            Toast instance. If this is not null, shows "location found"
     *            message.
     */
    private void setLocation(final Location location, final boolean toast) {
        stopLocationService();

        String url = String.format("javascript:" + JSPREFIX
                + ".map.setMyLocation(%f,%f,%f,%d,%s,%d)",
                location.getLongitude(), location.getLatitude(),
                location.getAccuracy(), 10, "true", 5000);
        this.execute(url);
        if (toast) {
            Toast.makeText(this, Messages.getString("P_MYLOCATION_FOUND"),
                    Toast.LENGTH_LONG).show();
        }
    }

}