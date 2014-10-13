package com.mabook.android.cyclo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mabook.android.cyclo.core.CycloDatabase;
import com.mabook.android.cyclo.core.CycloManager;
import com.mabook.android.cyclo.core.data.CycloSession;

import java.util.ArrayList;
import java.util.Date;

public class MapsActivity extends FragmentActivity {


    private static final String TAG = "MapsActivity";
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private CycloSession mSession;
    private AsyncTask<Void, Void, ArrayList<LatLng>> mTask;
    private BroadcastReceiver mReceiver;
    private TextView mTxtStatistics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mTxtStatistics = (TextView) findViewById(R.id.statistics);
        mTxtStatistics.setVisibility(View.GONE);

        Intent startIntent = getIntent();
        Bundle startBundle = startIntent.getExtras();
        if (startBundle != null) {
            mSession = startBundle.getParcelable("session");
        }

        setUpMapIfNeeded();


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        if (mSession.getEndTIme() == null) {
            mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Bundle extras = intent.getExtras();
                    String type = extras.getString(CycloManager.KEY_BROADCAST_TYPE);
                    long sessionId = extras.getLong(CycloManager.KEY_BROADCAST_SESSION);
                    long trackId = extras.getLong(CycloManager.KEY_BROADCAST_TRACK);
                    Location location = extras.getParcelable(CycloManager.KEY_BROADCAST_LOCATION);
                    if (sessionId == mSession.getId()) {
                        if (CycloManager.BROADCAST_TYPE_UPDATE.equals(type)) {
                            updateMap();
                        }
                    }
                }
            };
            registerReceiver(mReceiver, new IntentFilter(CycloManager.ACTION_BROADCAST));
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    void updateMap() {
        mTask = new AsyncTask<Void, Void, ArrayList<LatLng>>() {
            double elapsed = 0;
            double totalDistance = 0;
            double totalSpeed = 0;
            double maxSpeed = 0;
            double speedCount = 0;
            double avrSpeed = 0;
            float[] dist = new float[1];
            Date beforeTime = null;
            double totalTime = 0;

            @Override
            protected ArrayList<LatLng> doInBackground(Void... voids) {
                Uri contentUri = Uri.parse("content://" + CycloManager.AUTHORITY + "/session/" + mSession.getId() + "/track");
                Cursor cursor = getContentResolver().query(contentUri, CycloManager.TRACK_FIELD_ALL, null, null, null);
//                ArrayList<CycloTrack> tracks = new ArrayList<CycloTrack>();
                ArrayList<LatLng> points = new ArrayList<LatLng>();

                LatLng beforeLatLng = null;
                LatLng latlng = null;


                while (cursor.moveToNext()) {
//                    tracks.add(new CycloTrack(
//                            cursor.getLong(cursor.getColumnIndex(CycloManager.TRACK_FIELD_ID)),
//                            cursor.getLong(cursor.getColumnIndex(CycloManager.TRACK_FIELD_SESSION_ID)),
//                            cursor.getString(cursor.getColumnIndex(CycloManager.TRACK_FIELD_REGTIME)),
//                            cursor.getFloat(cursor.getColumnIndex(CycloManager.TRACK_FIELD_ACCURACY)),
//                            cursor.getDouble(cursor.getColumnIndex(CycloManager.TRACK_FIELD_LATITUDE)),
//                            cursor.getDouble(cursor.getColumnIndex(CycloManager.TRACK_FIELD_LONGITUDE)),
//                            cursor.getDouble(cursor.getColumnIndex(CycloManager.TRACK_FIELD_ALTITUDE)),
//                            cursor.getDouble(cursor.getColumnIndex(CycloManager.TRACK_FIELD_SPEED))
//                    ));

                    double speed = cursor.getDouble(cursor.getColumnIndex(CycloManager.TRACK_FIELD_SPEED));
                    if (speed > 1) {
                        totalSpeed += speed;
                        speedCount++;
                        if (speed > maxSpeed) {
                            maxSpeed = speed;
                        }
                        Date d = CycloDatabase.getDateTimeDate(cursor.getString(cursor.getColumnIndex(CycloManager.TRACK_FIELD_REGTIME)));
                        if (beforeTime != null) {
                            long diff = d.getTime() - beforeTime.getTime();
                            totalTime += diff;
                        }
                        beforeTime = d;
                    }

                    latlng = new LatLng(
                            cursor.getDouble(cursor.getColumnIndex(CycloManager.TRACK_FIELD_LATITUDE))
                            , cursor.getDouble(cursor.getColumnIndex(CycloManager.TRACK_FIELD_LONGITUDE)));

                    if (beforeLatLng != null) {
                        Location.distanceBetween(beforeLatLng.latitude, beforeLatLng.longitude,
                                latlng.latitude, latlng.longitude, dist);
                        totalDistance += dist[0];
                    }

                    points.add(latlng);
                    beforeLatLng = latlng;
                }
                // m/s -> km/h
                avrSpeed = totalSpeed * 3600.0 / 1000.0 / speedCount;
                maxSpeed = maxSpeed * 3600.0 / 1000.0;
                totalDistance = totalDistance / 1000.0;
                totalTime = totalTime / 1000.0;
                return points;
            }

            @Override
            protected void onPostExecute(ArrayList<LatLng> points) {
                mMap.clear();
                if (points.size() > 1) {

                    StringBuilder sb = new StringBuilder();
                    sb.append("Elapsed:").append(String.format("%d", (long) Math.round(totalTime / 60.0))).append("min\n")
                            .append("Distance:").append(String.format("%.3f", totalDistance)).append("km\n")
                            .append("MAX Speed:").append(String.format("%.3f", maxSpeed)).append("km/h\n")
                            .append("AVR Speed:").append(String.format("%.3f", avrSpeed)).append("km/h");
                    mTxtStatistics.setText(sb.toString());
                    mTxtStatistics.setVisibility(View.VISIBLE);

                    mMap.addMarker(new MarkerOptions().title("Start").position(points.get(0)));
                    if (mSession.getEndTIme() == null) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(points.get(points.size() - 1), 18));
                        mMap.addMarker(new MarkerOptions().title("Now").position(points.get(points.size() - 1)));
                    } else {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(points.get(0), 18));
                        mMap.addMarker(new MarkerOptions().title("End").position(points.get(points.size() - 1)));
                    }

                    PolylineOptions opt1 = new PolylineOptions()
                            .color(0xFFFFFFFF)
                            .width(12)
                            .addAll(points);
                    mMap.addPolyline(opt1);
                    PolylineOptions opt2 = new PolylineOptions()
                            .color(0xFFFF00FF)
                            .width(5)
                            .addAll(points);
                    mMap.addPolyline(opt2);


                }
                mTask = null;
            }
        };
        mTask.execute();
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        updateMap();
    }
}
