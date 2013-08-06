package com.gmail.boiledorange73.app.TokyoMapOld;

import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gmail.boiledorange73.ut.map.AlertDialogHelper;
import com.gmail.boiledorange73.ut.map.Location;
import com.gmail.boiledorange73.ut.map.LonLat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

//
// Geocoder Activity
//
//

public class GeocoderActivity extends Activity {

    private ArrayAdapter<Location<Integer>> mAdpPref;
    private ArrayAdapter<Location<Integer>> mAdpMncpl;
    private Spinner mSpnPref;
    private Spinner mSpnMncpl;
    private Button mBtnOk;
    private Button mBtnCancel;

    private ProgressDialog mProgressDialog = null;
    private SimpleWebClient mSimpleWebClient = null;
    private LonLat mCurrentPref = null;
    private LonLat mCurrentMncpl = null;

    private Handler mHandler;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(com.gmail.boiledorange73.ut.map.Messages
                .getString("W_GEOCODER"));
        this.mHandler = new Handler();
        // adapters
        this.mAdpPref = new ArrayAdapter<Location<Integer>>(this,
                android.R.layout.simple_spinner_item);
        this.mAdpMncpl = new ArrayAdapter<Location<Integer>>(this,
                android.R.layout.simple_spinner_item);
        // root
        LinearLayout root = new LinearLayout(this);
        this.setContentView(root, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT));
        root.setOrientation(LinearLayout.VERTICAL);
        // pref title
        TextView txtPref = new TextView(this);
        txtPref.setText(com.gmail.boiledorange73.ut.map.Messages
                .getString("W_PREFECTURE"));
        root.addView(txtPref, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        // pref
        this.mSpnPref = new Spinner(this);
        this.mAdpPref
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.mSpnPref.setAdapter(this.mAdpPref);
        root.addView(this.mSpnPref, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        // pref/event
        this.mSpnPref
                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                            View view, int position, long id) {
                        if (position >= 0 && position < parent.getCount()) {
                            Object item = parent.getItemAtPosition(position);
                            if (item != null) {
                                final Location<Integer> location = (Location<Integer>) item;
                                GeocoderActivity.this.mHandler
                                        .post(new Runnable() {
                                            @Override
                                            public void run() {
                                                GeocoderActivity.this
                                                        .onPrefSelect(location.code > 0 ? location
                                                                : null);
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                        GeocoderActivity.this.mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                GeocoderActivity.this.onPrefSelect(null);
                            }
                        });
                    }
                });
        // mncpl title
        TextView txtMncpl = new TextView(this);
        txtMncpl.setText(com.gmail.boiledorange73.ut.map.Messages
                .getString("W_MUNICIPALITY"));
        root.addView(txtMncpl, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        // mncpl
        this.mSpnMncpl = new Spinner(this);
        this.mAdpMncpl
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.mSpnMncpl.setAdapter(this.mAdpMncpl);
        root.addView(this.mSpnMncpl, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        // mncpl/event
        this.mSpnMncpl
                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                            View view, int position, long id) {
                        if (position >= 0 && position < parent.getCount()) {
                            Object item = parent.getItemAtPosition(position);
                            if (item != null) {
                                final Location<Integer> location = (Location<Integer>) item;
                                GeocoderActivity.this.mHandler
                                        .post(new Runnable() {
                                            @Override
                                            public void run() {
                                                GeocoderActivity.this
                                                        .onMncplSelect(location.code > 0 ? location
                                                                : null);
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                        GeocoderActivity.this.mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                GeocoderActivity.this.onMncplSelect(null);
                            }
                        });
                    }
                });
        // dip
        float sd = this.getResources().getDisplayMetrics().scaledDensity;
        // button holder
        LinearLayout buttons = new LinearLayout(this);
        LinearLayout.LayoutParams lpButtons = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lpButtons.topMargin = (int) (24 * sd);
        root.addView(buttons, lpButtons);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        // ok
        this.mBtnOk = new Button(this);
        this.mBtnOk.setText(android.R.string.ok);
        LinearLayout.LayoutParams lpOk = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lpOk.weight = 1;
        buttons.addView(this.mBtnOk, lpOk);
        // ok / event
        this.mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GeocoderActivity.this.onDecided();
            }
        });
        // cancel
        this.mBtnCancel = new Button(this);
        this.mBtnCancel.setText(android.R.string.cancel);
        LinearLayout.LayoutParams lpCancel = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lpCancel.weight = 1;
        buttons.addView(this.mBtnCancel, lpCancel);
        // cancel / event
        this.mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GeocoderActivity.this.onCanceled();
            }
        });
        // progress
        this.mProgressDialog = new ProgressDialog(this);
        this.mProgressDialog
                .setMessage(com.gmail.boiledorange73.ut.map.Messages
                        .getString("S_FINDING_MUNICIPALITIES"));
        this.mProgressDialog.setIndeterminate(true);
        this.mProgressDialog.setCancelable(false);
        // init: pref
        this.initAdpPref();
        this.onPrefSelect(null);
        this.onMncplSelect(null);
        this.clearMncpls();
    }

    @Override
    protected void onDestroy() {
        // stops web client.
        this.stopWebClient();
        // purges the handler.
        this.mHandler = null;
        // purges currently selected location.
        this.mCurrentPref = null;
        this.mCurrentMncpl = null;
        // purges views.
        this.mSpnPref = null;
        this.mSpnMncpl = null;
        this.mBtnOk = null;
        this.mBtnCancel = null;
        // progress
        if (this.mProgressDialog != null) {
            if (this.mProgressDialog.isShowing()) {
                this.mProgressDialog.dismiss();
            }
            this.mProgressDialog = null;
        }
        // purges adapters for spinner.
        if (this.mAdpPref != null) {
            this.mAdpPref.clear();
            this.mAdpPref = null;
        }
        if (this.mAdpMncpl != null) {
            this.mAdpMncpl.clear();
            this.mAdpMncpl = null;
        }
        // calls destroy of super class
        super.onDestroy();
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
        com.gmail.boiledorange73.ut.map.Messages.refresh();
    }

    private void onCanceled() {
        this.setResult(Activity.RESULT_CANCELED);
        this.finish();
    }

    private void onDecided() {
        LonLat current = this.mCurrentMncpl != null ? this.mCurrentMncpl
                : this.mCurrentPref;
        if (current == null) {
            this.onCanceled();
            return;
        }
        Intent data = new Intent();
        data.putExtra("lat", current.lat);
        data.putExtra("lon", current.lon);
        this.setResult(Activity.RESULT_OK, data);
        this.finish();
    }

    private void initAdpPref() {
        Locale locale = Locale.getDefault();
        String lang = locale.getLanguage();
        if ("ja".equals(lang)) {
            this.initAdpPref_ja();
        } else {
            this.initAdpPref_en();
        }
    }

    private void initAdpPref_ja() {
        this.mAdpPref.add(new Location<Integer>(0, 0, 0, ""));
        this.mAdpPref.add(new Location<Integer>(140.316140397838,
                36.3103575970948, 8, "茨城"));
        this.mAdpPref.add(new Location<Integer>(139.816117795515,
                36.6925903712723, 9, "栃木"));
        this.mAdpPref.add(new Location<Integer>(138.982336962846,
                36.5074705622156, 10, "群馬"));
        this.mAdpPref.add(new Location<Integer>(139.344616963215,
                35.9996966052509, 11, "埼玉"));
        this.mAdpPref.add(new Location<Integer>(140.200851882299,
                35.5167582083953, 12, "千葉"));
        this.mAdpPref.add(new Location<Integer>(139.439348850293,
                35.7135720182827, 13, "東京"));
        this.mAdpPref.add(new Location<Integer>(139.338839580796,
                35.4174673916814, 14, "神奈川"));
    }

    private void initAdpPref_en() {
        this.mAdpPref.add(new Location<Integer>(0, 0, 0, ""));
        this.mAdpPref.add(new Location<Integer>(140.316140397838,
                36.3103575970948, 8, "Ibaraki"));
        this.mAdpPref.add(new Location<Integer>(139.816117795515,
                36.6925903712723, 9, "Tochigi"));
        this.mAdpPref.add(new Location<Integer>(138.982336962846,
                36.5074705622156, 10, "Gnuma"));
        this.mAdpPref.add(new Location<Integer>(139.344616963215,
                35.9996966052509, 11, "Saitama"));
        this.mAdpPref.add(new Location<Integer>(140.200851882299,
                35.5167582083953, 12, "Chiba"));
        this.mAdpPref.add(new Location<Integer>(139.439348850293,
                35.7135720182827, 13, "Tokyo"));
        this.mAdpPref.add(new Location<Integer>(139.338839580796,
                35.4174673916814, 14, "Kanagawa"));
    }

    private void refreshButtons() {
        LonLat current = this.mCurrentMncpl != null ? this.mCurrentMncpl
                : this.mCurrentPref;
        this.mBtnOk.setEnabled(current != null);
    }

    private void onMncplSelect(Location<Integer> mncpl) {
        this.mCurrentMncpl = mncpl != null && mncpl.code > 0 ? mncpl : null;
        this.refreshButtons();
    }

    private void clearMncpls() {
        this.mAdpMncpl.clear();
        this.mSpnMncpl.setEnabled(false);
    }

    private void onPrefSelect(Location<Integer> pref) {
        this.clearMncpls();
        this.mCurrentPref = pref;
        this.refreshButtons();

        if (pref == null) {
            return;
        }

        if (this.mSimpleWebClient != null) {
            // NOW running for another task.
            this.stopWebClient();
        }
        // URL
        String url = "http://www.finds.jp/ws/mlist.php?json&pcode="
                + String.valueOf(pref.code);
        // lc
        Locale locale = Locale.getDefault();
        String lang = locale.getLanguage();
        if ("ja".equals(lang)) {
            url = url + "&lc=ja";
        } else {
            url = url + "&lc=en";
        }

        // web client
        SimpleWebClient.SimpleWebClientListener listener = new SimpleWebClient.SimpleWebClientListener() {
            @Override
            public void onError(final SimpleWebClient client, final String url,
                    final String message) {
                GeocoderActivity.this.mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        GeocoderActivity.this.onError(client, message);
                    }
                });
            }

            @Override
            public void onArrive(final SimpleWebClient client,
                    final String url, final String result) {
                GeocoderActivity.this.mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (GeocoderActivity.this.mProgressDialog != null) {
                            GeocoderActivity.this.mProgressDialog.dismiss();
                        }
                        GeocoderActivity.this.onArrive(client, result);
                    }
                });
            }

            @Override
            public void onPreExecuteOnUiThread(SimpleWebClient client) {
                if (GeocoderActivity.this.mProgressDialog != null) {
                    GeocoderActivity.this.mProgressDialog.show();
                }
            }

            @Override
            public void onCanceledOnUiThread(SimpleWebClient client) {
                if (GeocoderActivity.this.mProgressDialog != null) {
                    GeocoderActivity.this.mProgressDialog.dismiss();
                }
                GeocoderActivity.this.mSimpleWebClient = null;
            }

            @Override
            public void onFinishedOnUiThread(SimpleWebClient client, int result) {
                if (GeocoderActivity.this.mProgressDialog != null) {
                    GeocoderActivity.this.mProgressDialog.dismiss();
                }
                GeocoderActivity.this.mSimpleWebClient = null;
            }
        };
        this.mSimpleWebClient = new SimpleWebClient();
        this.mSimpleWebClient.load(url, listener);
    }

    private void stopWebClient() {
        if (this.mSimpleWebClient != null) {
            this.mSimpleWebClient.cancel();
            this.mSimpleWebClient = null;
        }
    }

    private void onError(SimpleWebClient receiver, String message) {
        AlertDialogHelper.showSimple(this,
                com.gmail.boiledorange73.ut.map.Messages.getString("W_ERROR"),
                message, android.R.drawable.ic_dialog_alert, mSpnMncpl);
    }

    private void onArrive(SimpleWebClient receiver, String result) {
        JSONArray arrMncpl = null;
        try {
            JSONObject root = new JSONObject(result);
            int status = root.getInt("status");
            if (status / 100 != 2) {
                AlertDialogHelper.showSimple(this,
                        com.gmail.boiledorange73.ut.map.Messages
                                .getString("W_ERROR"), root.getString("error"),
                        android.R.drawable.ic_dialog_alert, null);
                return;
            }
            JSONObject res = root.getJSONObject("result");
            arrMncpl = res.getJSONArray("municipality");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayList<Location<Integer>> arr = new ArrayList<Location<Integer>>();
        if (arrMncpl != null && arrMncpl.length() > 0) {
            for (int n = 0; n < arrMncpl.length(); n++) {
                try {
                    JSONObject mncpl = arrMncpl.getJSONObject(n);
                    String mcode = mncpl.getString("mcode");
                    String mname = mncpl.getString("mname");
                    JSONObject c = mncpl.getJSONObject("centroid");
                    String longitude = c.getString("longitude");
                    String latitude = c.getString("latitude");
                    double lon = Double.parseDouble(longitude);
                    double lat = Double.parseDouble(latitude);
                    int mc = Integer.parseInt(mcode);
                    arr.add(new Location<Integer>(lon, lat, mc, mname));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        if (arr.size() > 0) {
            this.mAdpMncpl.add(new Location<Integer>(0, 0, 0, ""));
            for (Location<Integer> location : arr) {
                this.mAdpMncpl.add(location);
            }
            this.mSpnMncpl.setEnabled(true);
        }
    }
};
