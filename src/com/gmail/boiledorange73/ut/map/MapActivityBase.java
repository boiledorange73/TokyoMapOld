package com.gmail.boiledorange73.ut.map;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.location.LocationProvider;
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
@SuppressLint("SetJavaScriptEnabled")
public abstract class MapActivityBase extends Activity implements
        JSBridgeListener {
    private static final int ID_ZOOM = 1;
    private static final int ID_MYLOCATION = 2;
    private static final int ID_MORE_EXIT = 3;
    private static final int ID_MORE_PREFERENCES = 4;
    private static final int ID_MORE_EXTERNALMAP = 5;
    private static final int ID_MORE_REMOVEDOWNLOADEDFILES = 6;
    private static final int ID_MORE_CLEARCACHE = 7;
    private static final int ID_MORE_ABOUT = 8;
    private static final int ID_MAPTYPE = 9;
    private static final int ID_GEOCODER = 10;

    private static final int RC_PREFERENCES = 1;
    private static final int RC_GEOCODER = 2;

    private static final String JSPREFIX = "mapviewer";

    /** Handler */
    private Handler mHandler;

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
     * Set before preference activity starts and used after preference activity
     * finishes (and clears at that time).
     */
    private MapState mLatestMapState = null;

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

    /** Gets User-Agent */
    protected abstract String getUserAgentCore();

    /**
     * Gets array of path to downloaded file. If not null, menuitem to remove
     * downloaded files is shown.
     */
    protected abstract boolean hasDownloadedFiles();

    /**
     * Removes downloaded files. If {@link #hasDownloadedFiles} returns false,
     * this is never called.
     */
    protected abstract void removeDownloadedFiles();

    /**
     * Gets whether the application currently requires high accuracy location
     * provider.
     */
    protected abstract boolean isHighAccuracy();

    /**
     * Checks whether current license is accepted.
     * 
     * @return Whether current license is accepted. If returns false, shows the
     *         license dialog.
     */
    protected abstract boolean checkLicenseCode();

    /**
     * Updates accepted license code.
     */
    protected abstract void updateLicenseCode();

    /**
     * Gets the URL directing the license HTML page.
     * 
     * @return the URL string.
     */
    protected abstract String getLicenseUrl();

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
        } catch (NoSuchFieldException e) {
            // DOES NOTHING
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clears all memory and file caches.
     */
    public void clearCache() {
        if (this.mWebView != null) {
            this.mWebView.clearCache(true);
        }
    }

    // -------- Activity
    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // creates action bar if can add it.
        this.initActionBar();

        this.mHandler = new Handler();
        if (this.checkLicenseCode()) {
            this.onLicensePassed();
        } else {
            DialogInterface.OnClickListener onAccept = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MapActivityBase.this.mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            MapActivityBase.this.onLicensePassed();
                        }
                    });
                }
            };
            DialogInterface.OnClickListener onDecline = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MapActivityBase.this.mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            System.exit(2);
                        }
                    });
                }
            };
            AlertDialogHelper.showLicenseDialog(this, this.getLicenseUrl(),
                    onAccept, onDecline);
        }
    }

    /**
     * Checks whether this can initialize. If returns false, this does nothing
     * after that.
     * 
     * @return Whether this can initialize. Actually, always returns true.
     */
    protected boolean checkReadyToInitialize() {
        return true;
    }

    private void onLicensePassed() {
        this.updateLicenseCode();
        if (this.checkReadyToInitialize()) {
            this.initialize();
        }
    }

    protected void initialize() {
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
        this.applyUserAgent(this.mWebView);
        layoutRoot.addView(this.mWebView);
        this.mWebView.getSettings().setJavaScriptEnabled(true);
        // starts the map application
        this.restart();
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
        if (this.mWebView != null) {
            this.mWebView.destroy();
            this.mWebView = null;
        }
        this.mHandler = null;
    }

    private void applyUserAgent(WebView webView) {
        String ua = this.getUserAgentCore();
        if (ua != null && ua.length() > 0) {
            ua = ua + "/" + this.getVersionName();
            this.mWebView.getSettings().setUserAgentString(ua);
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
                Messages.getString("P_CONFIRM_EXIT"), 0);

    }

    /** Called whe the menu is created. */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // zoom (invisible)
        menu.add(Menu.NONE, ID_ZOOM, Menu.NONE, Messages.getString("W_ZOOM"))
                .setIcon(android.R.drawable.ic_menu_zoom).setVisible(false);
        // mylocation (invisible)
        menu.add(Menu.NONE, ID_MYLOCATION, Menu.NONE,
                Messages.getString("W_MYLOCATION"))
                .setIcon(android.R.drawable.ic_menu_mylocation)
                .setVisible(false);
        // maptypes (invisible)
        menu.add(Menu.NONE, ID_MAPTYPE, Menu.NONE,
                Messages.getString("W_CHANGE_MAP"))
                .setIcon(android.R.drawable.ic_menu_mapmode).setVisible(false);
        // geocoder (invisible)
        if (this.getGeocoderActivityClass() != null) {
            menu.add(Menu.NONE, ID_GEOCODER, Menu.NONE,
                    Messages.getString("W_GEOCODER"))
                    .setIcon(android.R.drawable.ic_menu_search)
                    .setVisible(false);
        }
        // more
        SubMenu smMore = menu.addSubMenu(Messages.getString("W_MORE")).setIcon(
                android.R.drawable.ic_menu_more);
        if (this.getPreferenceActivityClass() != null) {
            smMore.add(Menu.NONE, ID_MORE_PREFERENCES, Menu.NONE,
                    Messages.getString("W_PREFERENCES"));
        }
        // more/externalmap (invisible)
        smMore.add(Menu.NONE, ID_MORE_EXTERNALMAP, Menu.NONE,
                Messages.getString("W_EXTERNALMAP")).setVisible(false);
        // more/removedownloadedfiles (shown if needed)
        if (this.hasDownloadedFiles()) {
            smMore.add(Menu.NONE, ID_MORE_REMOVEDOWNLOADEDFILES, Menu.NONE,
                    Messages.getString("W_REMOVEDOWNLOADEDFILES")).setIcon(
                    android.R.drawable.ic_menu_delete);
        }
        // more/clearcache (2013/08/07)
        smMore.add(Menu.NONE, ID_MORE_CLEARCACHE, Menu.NONE,
                Messages.getString("W_CLEARCACHE"));

        // more/about
        smMore.add(Menu.NONE, ID_MORE_ABOUT, Menu.NONE,
                Messages.getString("W_ABOUT"));
        // more/exit
        smMore.add(Menu.NONE, ID_MORE_EXIT, Menu.NONE,
                Messages.getString("W_EXIT"));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case MapActivityBase.RC_GEOCODER:
            if (resultCode == Activity.RESULT_OK) {
                double lat = data.getDoubleExtra("lat", Double.NaN);
                double lon = data.getDoubleExtra("lon", Double.NaN);
                if (!Double.isNaN(lat) && !Double.isNaN(lon)) {
                    this.restoreMapState(null, lon, lat, null);
                }
            }
            break;
        case MapActivityBase.RC_PREFERENCES:
            this.restart();
            break;
        }
    }

    private void activateMenuItemIfExists(Menu menu, int id) {
        MenuItem item = menu.findItem(id);
        if (item != null) {
            item.setVisible(true);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (this.mLoaded) {
            this.activateMenuItemIfExists(menu, ID_ZOOM);
            this.activateMenuItemIfExists(menu, ID_MYLOCATION);
            if (this.mMaptypeList != null && this.mMaptypeList.size() > 0) {
                this.activateMenuItemIfExists(menu, ID_MAPTYPE);
            }
            this.activateMenuItemIfExists(menu, ID_GEOCODER);
            this.activateMenuItemIfExists(menu, ID_MORE_EXTERNALMAP);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    /** Called when one of the menu item is selected. */
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
        case ID_ZOOM:
            ZoomState zoomState = this.getCurrentZoomState();
            if (zoomState != null && zoomState.minzoom >= 0
                    && zoomState.maxzoom >= zoomState.minzoom) {
                AlertDialogHelper.createSequentialDialog(this,
                        Messages.getString("W_ZOOM"), zoomState.minzoom,
                        zoomState.maxzoom, 1, zoomState.currentzoom, 0,
                        new AlertDialogHelper.OnChoiceListener<Integer>() {
                            @Override
                            public void onChoice(DialogInterface dialog,
                                    Integer item) {
                                MapActivityBase.this.restoreMapState(null,
                                        null, null, item);
                            }
                        }).show();
            }
            return true;
        case ID_MAPTYPE:
            if (this.mMaptypeList != null && this.mMaptypeList.size() > 0) {
                AlertDialogHelper
                        .<ValueText<String>> showChoice(
                                this,
                                item.getTitle().toString(),
                                this.mMaptypeList,
                                0,
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
        case ID_MORE_CLEARCACHE:
            AlertDialogHelper.showConfirmationDialog(this,
                    Messages.getString("W_CLEARCACHE"),
                    Messages.getString("P_CONFIRM_CLEARCACHE"),
                    android.R.drawable.ic_dialog_alert,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (MapActivityBase.this.mHandler != null) {
                                MapActivityBase.this.mHandler
                                        .post(new Runnable() {
                                            @Override
                                            public void run() {
                                                MapActivityBase.this
                                                        .clearCache();
                                            }
                                        });
                            } else {
                                MapActivityBase.this.clearCache();
                            }
                        }
                    });
            return true;
        case ID_MORE_EXIT:
            this.showExitConfirmation();
            return true;
        case ID_MORE_PREFERENCES:
            Class<? extends Activity> preferenceActivityClass = this
                    .getPreferenceActivityClass();
            if (preferenceActivityClass != null) {
                // saves current map state into this.mMapState
                this.mLatestMapState = this.getCurrentMapState();
                this.startActivityForResult(new Intent(this,
                        preferenceActivityClass), RC_PREFERENCES);
            }
            return true;
        case ID_MORE_EXTERNALMAP:
            this.showExternalMap();
            return true;
        case ID_MORE_REMOVEDOWNLOADEDFILES:
            AlertDialogHelper.showConfirmationDialog(this,
                    Messages.getString("W_REMOVEDOWNLOADEDFILES"),
                    Messages.getString("P_CONFIRM_REMOVEDOWNLOADEDFILES"),
                    android.R.drawable.ic_dialog_alert,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MapActivityBase.this.mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    MapActivityBase.this
                                            .removeDownloadedFiles();
                                    System.exit(0);
                                }
                            });
                        }
                    });
            return true;
        case ID_MORE_ABOUT:
            this.showAbout();
            return true;
        case ID_GEOCODER:
            Class<? extends Activity> geocoderClass = this
                    .getGeocoderActivityClass();
            if (geocoderClass != null) {
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
            MapState mapState = (MapState) savedInstanceState
                    .getParcelable("MapState");
            if (mapState != null) {
                this.restoreMapState(mapState.id, mapState.lon, mapState.lat,
                        mapState.z);
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

    /**
     * Restarts.
     */
    private void restart() {
        // init flag
        this.mLoaded = false;
        this.mStatements.clear();
        // load htmls
        Uri.Builder uriBuilder = (Uri.parse(this.getWebUrl())).buildUpon();
        String url = uriBuilder.toString();
        // loads html (and js)
        this.mWebView.loadUrl(url);
        // registers bridge in JS.
        this.mJSBridge = new JSBridge(this);
        this.mWebView.addJavascriptInterface(this.mJSBridge, "jsBridge");
    }

    /**
     * Calls invalidateOptionsMenu() if it is possible (in other words, Android
     * 3.0 or higher).
     */
    private void invalidateOptionsMenuIfPossible() {
        try {
            Class<? extends MapActivityBase> clazz = this.getClass();
            if (clazz != null) {
                Method method = clazz.getMethod("invalidateOptionsMenu",
                        (Class[]) null);
                if (method != null) {
                    method.invoke(this);
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    // -------- JS Listener
    /** Map status when last {@link #getCurrentMapState()} is called. */
    private MapState mInternalMapState;
    /** True if this is waiting for JS:getCurrentMapState() returns. */
    private boolean mWaitingForgetCurrentMapState;
    /** Zoom status including minimum, maximum and current when last . */
    private ZoomState mZoomState;
    /** True if this is waiting for JS:getCurrentZoomState() returns. */
    private boolean mWaitingForgetCurrentZoomState;

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
            // executes queued commands.
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
            // restores map state (2013/08/07)
            if (this.mInternalMapState != null) {
                this.restoreMapState(this.mInternalMapState.id,
                        this.mInternalMapState.lon, this.mInternalMapState.lat,
                        this.mInternalMapState.z);
                this.mInternalMapState = null;
            }
            // request to create menu items.
            this.invalidateOptionsMenuIfPossible();
        } else if (code.equals("getCurrentMapState")) {
            if (message == null || !(message.length() > 0)) {
                // DOES NOTHING
            } else {
                try {
                    JSONObject json = new JSONObject(message);
                    this.mInternalMapState = new MapState();
                    this.mInternalMapState.id = json.getString("id");
                    this.mInternalMapState.lat = json.getDouble("lat");
                    this.mInternalMapState.lon = json.getDouble("lon");
                    this.mInternalMapState.z = json.getInt("z");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            this.mWaitingForgetCurrentMapState = false;
        } else if (code.equals("getCurrentZoomState")) {
            if (message == null) {
                // DOES NOTHING
            } else {
                try {
                    JSONObject json = new JSONObject(message);
                    this.mZoomState = new ZoomState();
                    this.mZoomState.minzoom = json.getInt("minzoom");
                    this.mZoomState.maxzoom = json.getInt("maxzoom");
                    this.mZoomState.currentzoom = json.getInt("currentzoom");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                this.mWaitingForgetCurrentZoomState = false;
            }
        } else if (code.equals("alert")) {
            // shows alert text.
            (new AlertDialog.Builder(this)).setTitle(this.getTitle())
                    .setMessage(message != null ? message : "")
                    .setCancelable(true)
                    .setPositiveButton(android.R.string.ok, null).show();
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
        } else if ("getMapNameSize".equals(code)) {
            return this.getMapNameSize();
        } else if ("getInitialMapState".equals(code)) {
            if (this.mLatestMapState != null) {
                String ret = "{\"id\":\"" + this.mLatestMapState.id
                        + "\",\"lon\":" + this.mLatestMapState.lon
                        + ",\"lat\":" + this.mLatestMapState.lat + ",\"z\":"
                        + this.mLatestMapState.z + "}";
                this.mLatestMapState = null;
                return ret;
            } else {
                return null;
            }
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
        // clears the member
        this.mInternalMapState = null;
        // waiting the method before this.
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
        return this.mInternalMapState;
    }

    /**
     * (LOCAL) Gets the current status of the map. This method request JS to
     * send the status, waiting for JS and deserializes JSON text.
     * 
     * @return Current status of the map.
     */
    private synchronized ZoomState getCurrentZoomState() {
        long timedout;

        // waiting the method before this.
        timedout = System.currentTimeMillis() + 1000;
        while (this.mWaitingForgetCurrentZoomState) {
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
        this.mWaitingForgetCurrentZoomState = true;
        // Calls JS
        this.execute("javascript:" + JSPREFIX + ".map.getCurrentZoomState()");
        // Waits
        timedout = System.currentTimeMillis() + 1000;
        while (this.mWaitingForgetCurrentZoomState) {
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
        return this.mZoomState;
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

        // gets active location providers.
        // if requires high accuracy, creates filtered provider list.
        List<String> providerList = this.mLocationManager.getProviders(true);
        if (highaccuracy) {
            ArrayList<String> candidateList = new ArrayList<String>();
            if (providerList != null) {
                for (String providerName : providerList) {
                    if (providerName != null) {
                        LocationProvider provider = this.mLocationManager
                                .getProvider(providerName);
                        if (provider != null
                                && provider.getAccuracy() == Criteria.ACCURACY_FINE) {
                            candidateList.add(providerName);
                        }
                    }
                }
            }
            providerList = candidateList;
        }
        // checks whether at least one location provider is available.
        if (providerList == null || !(providerList.size() > 0)) {
            // Shows the dialog which let user to open system preference.
            AlertDialogHelper.showConfirmationDialog(this, Messages
                    .getString("P_LOCATION_PROVIDER_ERROR"), Messages
                    .getString("P_CONFIRM_OPEN_LOACTIONPROVIDER_SETTINGS"),
                    android.R.drawable.ic_dialog_info,
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
        final boolean onlyPassive = (providerList.size() == 1 && "passive"
                .equals(providerList.get(0)));
        this.mLocationTimer = new Timer(true);
        this.mTimedOut = System.currentTimeMillis() + 30000L;
        final Handler handler = new Handler();
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
                        if (current >= MapActivityBase.this.mTimedOut) {
                            // timed out.
                            MapActivityBase.this
                                    .onLocationServiceTimedout(onlyPassive);
                        } else {
                            // NOT timed out. Shows the TOAST.
                            MapActivityBase.this.mToast.show();
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

    private void onLocationServiceTimedout(boolean onlyPassive) {
        if (onlyPassive) {
            AlertDialogHelper
                    .showConfirmationDialog(
                            this,
                            Messages.getString("P_LOCATION_PROVIDER_ERROR"),
                            Messages.getString("P_CONFIRM_OPEN_LOACTIONPROVIDER_SETTINGS_ONLYPASSIVE"),
                            android.R.drawable.ic_dialog_info,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(
                                        final DialogInterface dialog,
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
        } else {
            Toast.makeText(this, Messages.getString("P_MYLOCATION_NOTFOUND"),
                    Toast.LENGTH_LONG).show();
        }
        MapActivityBase.this.stopLocationService();
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
        // sets latest mapstate
        this.mLatestMapState = this.getCurrentMapState();
        // change resources
        Messages.refresh();
        // restarts
        this.restart();
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

        String url = "javascript:" + JSPREFIX + ".map.setMyLocation("
                + String.valueOf(location.getLongitude()) + ","
                + String.valueOf(location.getLatitude()) + ","
                + String.valueOf(location.getAccuracy()) + "," + "10,"
                + "true," + "5000)";

        this.execute(url);
        if (toast) {
            Toast.makeText(this, Messages.getString("P_MYLOCATION_FOUND"),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void showExternalMap() {
        MapState mapState = this.getCurrentMapState();
        String uri = "geo:";
        if (mapState == null) {
            return;
        }
        uri = uri + mapState.lat + "," + mapState.lon + "?z=" + mapState.z;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        try {
            this.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, Messages.getString("P_EXTERNALMAP_NOTFOUND"),
                    Toast.LENGTH_SHORT).show();
        }
    }
}