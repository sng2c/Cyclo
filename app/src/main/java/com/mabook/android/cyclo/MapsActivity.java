package com.mabook.android.cyclo;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mabook.android.cyclo.core.CycloManager;
import com.mabook.android.cyclo.core.data.CycloSession;
import com.mabook.android.cyclo.core.data.CycloTrack;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity {

    private static final String TAG = "MapsActivity";
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private CycloSession mSession;
    private AsyncTask<Void, Void, ArrayList<CycloTrack>> mTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent startIntent = getIntent();
        Bundle startBundle = startIntent.getExtras();
        if (startBundle != null) {
            mSession = startBundle.getParcelable("session");
        }

        setUpMapIfNeeded();


    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
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

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

        mTask = new AsyncTask<Void, Void, ArrayList<CycloTrack>>() {

            @Override
            protected ArrayList<CycloTrack> doInBackground(Void... voids) {
                Uri contentUri = Uri.parse("content://" + CycloManager.AUTHORITY + "/session/" + mSession.getId() + "/track");
                Cursor cursor = getContentResolver().query(contentUri, CycloManager.TRACK_FIELD_ALL, null, null, null);
                ArrayList<CycloTrack> tracks = new ArrayList<CycloTrack>();
                while (cursor.moveToNext()) {
                    tracks.add(new CycloTrack(
                            cursor.getLong(cursor.getColumnIndex(CycloManager.TRACK_FIELD_ID)),
                            cursor.getLong(cursor.getColumnIndex(CycloManager.TRACK_FIELD_SESSION_ID)),
                            cursor.getString(cursor.getColumnIndex(CycloManager.TRACK_FIELD_REGTIME)),
                            cursor.getFloat(cursor.getColumnIndex(CycloManager.TRACK_FIELD_ACCURACY)),
                            cursor.getDouble(cursor.getColumnIndex(CycloManager.TRACK_FIELD_LATITUDE)),
                            cursor.getDouble(cursor.getColumnIndex(CycloManager.TRACK_FIELD_LONGITUDE)),
                            cursor.getDouble(cursor.getColumnIndex(CycloManager.TRACK_FIELD_ALTITUDE)),
                            cursor.getDouble(cursor.getColumnIndex(CycloManager.TRACK_FIELD_SPEED))
                    ));
                }
                return tracks;
            }

            @Override
            protected void onPostExecute(ArrayList<CycloTrack> tracks) {
                mMap.clear();
                if (tracks.size() > 1) {
                    LatLngBounds.Builder builder = LatLngBounds.builder();
                    LatLng latlng = null;
//                    for(int i=0; i<tracks.size(); i+=10) {
//                        CycloTrack track = tracks.get(i);
//                        latlng = new LatLng(track.getLatitude(), track.getLongitude());
//
//
//                        CircleOptions circleOpt = new CircleOptions()
//                                .center(latlng)
//                                .radius(1)
//                                .fillColor(0xFFFF00FF)
//                                .strokeWidth(0)
//                                ;
//                        Circle c = mMap.addCircle(circleOpt);
//                        builder = builder.include(latlng);
//
//                    }
//                    LatLngBounds bound = builder.build();
                    ArrayList<LatLng> points = new ArrayList<LatLng>();
                    for (int i = 0; i < tracks.size(); i++) {
                        CycloTrack track = tracks.get(i);
                        latlng = new LatLng(track.getLatitude(), track.getLongitude());
                        points.add(latlng);
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

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(points.get(0), 18));
                }
                mTask = null;
            }
        };
        mTask.execute();
    }
}
