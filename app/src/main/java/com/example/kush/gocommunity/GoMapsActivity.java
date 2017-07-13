package com.example.kush.gocommunity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.AddPlaceRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.GeocodingApiRequest;
import com.google.maps.PendingResult;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressType;
import com.google.maps.model.GeocodingResult;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GoMapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private static final String TAG = "new pokestop";

    android.location.Location lastLocation = null;
    private static LatLng userLatLng = null;
    Float currentZoom;
    Float newZoom;
    LatLng target;
    GoogleApiClient mClient;
    private GoogleMap mMap;
    private DatabaseReference mDatabase;
    private FloatingActionsMenu menuMultipleActions;
    private Double lat = 27.0;
    private Double lng = 28.0;
    private LatLng latLng;
    private FloatingActionButton actionB;
    private FloatingActionButton actionA;
    private FloatingActionButton actionL;
    private FloatingActionButton actionDl;
    private TextView tvp;
    private TextView tvg;
    private TextView tvl;
    private TextView tvdl;
    private Location location;
    private ArrayList<Location> pokestopsData;
    private ArrayList<Location> gymsData;
    private String numLike = "0";
    private String numDislike = "0";
    private String key = null;
    private int likeButtonToggle = -1;
    private int dislikeButtonToggle = -1;
    private TextView tvload;
    private ImageView iv;
    private Float zoomLevel;
    AVLoadingIndicatorView avi;
    private FloatingActionButton fabRemove;
    private int like;
    private int flagb = 1;
    private int flaga = 1;
    Boolean requestingLocationUpdates = false;
    Boolean isConnected;
    NetworkInfo activeNetwork;
    Snackbar snackbar;
    ConnectivityManager cm;
    ShowcaseView add,fabPokestop,fabGym,fab,fabRmv;
    ViewTarget plus,fabP,fabG,fabR;
    private static String state,country;
    UserLocationData userLocationData = new UserLocationData();
    static Circle circle;
    CircleOptions circleOptions;
    private static int circleFlag = 0;

//    ArrayList<GCResult> resultsData;
//    ArrayList<AddressComponents> addressComponentsData;
//    private FirebaseAuth mAuth;
//    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_maps);

//        country = "India";
//        state = "Delhi";

        tvload = (TextView) findViewById(R.id.loading_text);
        iv = (ImageView) findViewById(R.id.loading_back);
        avi = (AVLoadingIndicatorView) findViewById(R.id.avi);

        cm =  (ConnectivityManager)GoMapsActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);

        activeNetwork = cm.getActiveNetworkInfo();

        isConnected = (activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting());

        if (!isConnected){
            snackbar = Snackbar
                    .make(iv, "Connect to internet and press retry.", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Retry", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent();
                            intent.setClass(GoMapsActivity.this,GoMapsActivity.class);
                            startActivity(intent);
                            finish();
//                            activeNetwork = cm.getActiveNetworkInfo();
//                            isConnected = activeNetwork != null &&
//                                    activeNetwork.isConnectedOrConnecting();
//                            if (isConnected)
//                                snackbar.dismiss();
//                            else if (!isConnected)
//                                snackbar.show();
                        }
                    });
            snackbar.show();
        }

        if (mClient == null) {
            mClient = new GoogleApiClient.Builder(GoMapsActivity.this).
                    addConnectionCallbacks(GoMapsActivity.this)
                    .addOnConnectionFailedListener(GoMapsActivity.this)
                    .addApi(LocationServices.API)
                    .build();
        }

        fabRemove = (FloatingActionButton) findViewById(R.id.remove);
        zoomLevel = 18.0f;
        avi.smoothToShow();
//        new java.util.Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//
//                // When you need to modify a UI element, do so on the UI thread.
//                // 'getActivity()' is required as this is being ran from a Fragment.
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        // This code will always run on the UI thread, therefore is safe to modify UI elements.
//                        avi.hide();
//                        iv.setVisibility(View.GONE);
//                        tvload.setVisibility(View.GONE);
//                    }
//                });
//            }
//        },6500
//        ); // End of your timer code.

//        TextView tv = (TextView) findViewById(R.id.database);
//
//        tv.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//
//                Intent dbmanager = new Intent(GoMapsActivity.this, AndroidDatabaseManager.class);
//                startActivity(dbmanager);
//            }
//        });

        actionB = (FloatingActionButton) findViewById(R.id.action_b);
        actionA = (FloatingActionButton) findViewById(R.id.action_a);
        actionL = (FloatingActionButton) findViewById(R.id.like);
        actionDl = (FloatingActionButton) findViewById(R.id.dislike);
        tvp = (TextView) findViewById(R.id.pokestop_text);
        tvg = (TextView) findViewById(R.id.gym_text);
        tvl = (TextView) findViewById(R.id.like_text);
        tvdl = (TextView) findViewById(R.id.dislike_text);

        actionA.setVisibility(View.GONE);
        actionB.setVisibility(View.GONE);
        tvg.setVisibility(View.GONE);
        tvp.setVisibility(View.GONE);
        actionL.setVisibility(View.GONE);
        actionDl.setVisibility(View.GONE);
        tvl.setVisibility(View.GONE);
        tvdl.setVisibility(View.GONE);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //fab
        menuMultipleActions = (FloatingActionsMenu) findViewById(R.id.multiple_actions);

        menuMultipleActions.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                tvdl.setVisibility(View.GONE);
                tvl.setVisibility(View.GONE);
                actionL.setVisibility(View.GONE);
                actionDl.setVisibility(View.GONE);
                actionA.setVisibility(View.VISIBLE);
                actionB.setVisibility(View.VISIBLE);
                tvg.setVisibility(View.VISIBLE);
                tvp.setVisibility(View.VISIBLE);
                mMap.getUiSettings().setMapToolbarEnabled(false);
            }

            @Override
            public void onMenuCollapsed() {
                actionA.setVisibility(View.GONE);
                actionB.setVisibility(View.GONE);
                tvg.setVisibility(View.GONE);
                tvp.setVisibility(View.GONE);
                mMap.getUiSettings().setMapToolbarEnabled(true);

//                ViewGroup.LayoutParams params = menuMultipleActions.getLayoutParams();
//                params.width = 5;
//                menuMultipleActions.setLayoutParams(params);
            }
        });

        ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
        drawable.getPaint().setColor(0x7f0b0069);

//        zoom.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                currentZoom = mMap.getCameraPosition().zoom;
//                target = mMap.getCameraPosition().target;
//                if (currentZoom >= zoomLevel){
//                    newZoom = currentZoom + 2;
//                }
//                else{
//                    newZoom = zoomLevel;
//                }
//                if (mMap!= null){
//                    CameraPosition MARKER =
//                            new CameraPosition.Builder().target(target)
//                                    .zoom(newZoom)
//                                    .build();
//                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(MARKER), null);
//                }
//
//            }
//        });
    }
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
///*********** fake value isConnected ***********/////
        isConnected = true;
        /////****************////////////
        if (mMap != null && isConnected) {
            mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
//                    CameraPosition USER =
//                            new CameraPosition.Builder().target(userLatLng)
//                                    .zoom(zoomLevel)
//                                    .build();
                    Log.d("Line 310 - Userlatlng"," = " + userLatLng.latitude + ", " + userLatLng.longitude);

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, zoomLevel), new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {
                            Log.d("Line 315 - Userlatlng"," = " + userLatLng.latitude + ", " + userLatLng.longitude);
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(userLatLng));
//                            Log.d("Line 317 - Userlatlng"," = " + userLatLng.latitude + ", " + userLatLng.longitude);
//                            // Instantiates a new CircleOptions object and defines the center and radius
//                            circleOptions = new CircleOptions()
//                                    .center(userLatLng)
//                                    .radius(600)
//                                    .fillColor(Color.argb(50,87,206,233))
//                                    .strokeColor(Color.argb(50,87,206,233))
//                                    .strokeWidth(0);
//                            // In meters
//                            Log.d("Line 326 - Userlatlng"," = " + userLatLng.latitude + ", " + userLatLng.longitude);
//// Get back the mutable Circle
//                            circle = mMap.addCircle(circleOptions);

                            return;
                        }

                        @Override
                        public void onCancel() {
                            Log.d("Line 335 - Userlatlng"," = " + userLatLng.latitude + ", " + userLatLng.longitude);
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, zoomLevel), null);
                        }
                    });
//                    appIntro();
                    Log.d("Line 340 - Userlatlng"," = " + userLatLng.latitude + ", " + userLatLng.longitude);
                    avi.hide();
                    iv.setVisibility(View.GONE);
                    tvload.setVisibility(View.GONE);

//                    Toast.makeText(GoMapsActivity.this, "Map Ready", Toast.LENGTH_LONG).show();
                    if (mMap != null && userLatLng != null) {
                        Log.d("Line 345 - Userlatlng"," = " + userLatLng.latitude + ", " + userLatLng.longitude);
//                        while ( mMap.getCameraPosition().zoom == zoomLevel) {
//                            Log.d("Zoom before"," : " + mMap.getCameraPosition().zoom + "and zoomlevel = " + zoomLevel);
//                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, zoomLevel), null);
//                            Log.d("Zoom after"," : " + mMap.getCameraPosition().zoom  + "and zoomlevel = " + zoomLevel);
//                        }

//                        CameraPosition USER =
//                                new CameraPosition.Builder().target(userLatLng)
//                                        .zoom(zoomLevel)
//                                        .build();
//                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(USER), null);
//                        mMap.getCameraPosition();
//                            LatLng userLoc = userLatLng;
                            try {
                                Log.d("Line 362 - Userlatlng"," = " + userLatLng.latitude + ", " + userLatLng.longitude);
                                userLocationFinder(userLatLng);
                                Log.d("Line 364 - Userlatlng"," = " + userLatLng.latitude + ", " + userLatLng.longitude);
//                                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(USER), null);
                            } catch (Exception e) {
                                Log.d("stacktrace"," error");
                                e.printStackTrace();
                            }

//                            state = addressComp.get(0).getLong_name();
//                            country = addressComp.get(1).getLong_name();
                        Log.d("Line 372 - Userlatlng"," = " + userLatLng.latitude + ", " + userLatLng.longitude);
                            Log.d("ready user location " + userLocationData.getState(), " & " + userLocationData.getCountry());
                            while(userLocationData.getState() == null && userLocationData.getCountry() == null){
                                //do nothing
                                int a=1;
                            }
                            state = userLocationData.getState();
                            country = userLocationData.getCountry();
                        Log.d("Address "," Kush2 State = " + state);
                        Log.d("Address "," Kush2 Country = " + country);
                        if(state == "Delhi")
                            Log.d("ans"," yes state is delhi ");
                        else
                            Log.d("ans"," state is null state = "+state);
                        if(country == "India")
                            Log.d("ans"," yes country is India ");
                        else
                            Log.d("ans"," country is null country = " + country);
                        state = userLocationData.getState();
                        country = userLocationData.getCountry();
                        if (state != null && country != null) {
                                Log.d("Line 377 - Userlatlng"," = " + userLatLng.latitude + ", " + userLatLng.longitude);
                                mDatabase.child("pokéstops").child(country).child(state).addChildEventListener(new ChildEventListener() {

                                    @Override
                                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                        Log.d("In A C E L " + state + "", country + "");

                                        Map<String, Double> map = (Map<String, Double>) dataSnapshot.getValue();
                                        lat = map.get("latitude");
                                        lng = map.get("longitude");

//                 A new marker has been added, add it to the map from database
                                        latLng = new LatLng(lat, lng);
                                        mMap.addMarker(new MarkerOptions().position(latLng)
                                                        .title("Pokéstop")
//                .snippet("Population: 4,137,400")
                                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pokestop))
                                        );

                                        Map<String, String> mapInt = (Map<String, String>) dataSnapshot.getValue();
                                        numLike = mapInt.get("numLikes");
                                        numDislike = mapInt.get("numDislikes");
                                        key = dataSnapshot.getKey();
                                        Log.d(TAG, "Pokéstop added" + lat + "  ," + lng + " ," + numLike + " ," + numDislike + ": key: " + key);
                                        location = new Location(lat, lng, numLike, numDislike, key);
                                        pokestopsData.add(location);

                                        GODBOpenHelper helper = new GODBOpenHelper(GoMapsActivity.this);
                                        SQLiteDatabase db = helper.getWritableDatabase();
                                        String[] columns = {GODBOpenHelper.GO_TABLE_ID, GODBOpenHelper.GYM, GODBOpenHelper.POKESTOP,
                                                GODBOpenHelper.LONGITUDE, GODBOpenHelper.LATITUDE, GODBOpenHelper.LIKED, GODBOpenHelper.DISLIKED};

                                        Cursor c = db.query(GODBOpenHelper.GO_TABLE, columns, null, null, null, null, null);
                                        int count = 0;
                                        while (c.moveToNext()) {
                                            if ((lat == c.getDouble(c.getColumnIndex(GODBOpenHelper.LATITUDE)))
                                                    && (lng == c.getDouble(c.getColumnIndex(GODBOpenHelper.LONGITUDE)))) {
                                                count = 1;
                                                break;
                                            }
                                        }
                                        if (location != null && count != 1) {

                                            ContentValues cv = new ContentValues();

                                            cv.put(GODBOpenHelper.GO_TABLE_ID, location.getKey());
                                            cv.put(GODBOpenHelper.POKESTOP, 1);
                                            cv.put(GODBOpenHelper.GYM, 0);
                                            cv.put(GODBOpenHelper.LATITUDE, location.getLatitude());
                                            cv.put(GODBOpenHelper.LONGITUDE, location.getLongitude());
                                            cv.put(GODBOpenHelper.LIKED, -1);
                                            cv.put(GODBOpenHelper.DISLIKED, -1);

                                            db.insert(GODBOpenHelper.GO_TABLE, null, cv);

                                        }
                                    }

                                    @Override
                                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                                        Map<String, Double> map = (Map<String, Double>) dataSnapshot.getValue();

                                        lat = map.get("latitude");
                                        lng = map.get("longitude");

//                 A new marker has been added, add it to the map from database
                                        Map<String, String> mapInt = (Map<String, String>) dataSnapshot.getValue();
                                        numLike = mapInt.get("numLikes");
                                        numDislike = mapInt.get("numDislikes");

                                        key = dataSnapshot.getKey();
                                        for (Location pl : pokestopsData) {
                                            if ((pl.getLatitude() == lat) && (pl.getLongitude() == lng)) {
                                                pl.setKey(key);
                                                pl.setNumLikes(numLike);
                                                pl.setNumDislikes(numDislike);

                                                Log.d(TAG, "Pokestop data updated : " + lat + "  ," + lng + " ," + numLike + " ," + numDislike + ": key: " + key);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                                        Map<String, Double> map = (Map<String, Double>) dataSnapshot.getValue();

                                        lat = map.get("latitude");
                                        lng = map.get("longitude");

                                        GODBOpenHelper helper = new GODBOpenHelper(GoMapsActivity.this);
                                        SQLiteDatabase dbwrite = helper.getWritableDatabase();

                                        key = dataSnapshot.getKey();

                                        //Remove gym
                                        for (Location pl : pokestopsData) {
                                            if ((pl.getLatitude() == lat) && (pl.getLongitude() == lng)) {
                                                key = pl.getKey();
                                                dbwrite.delete(GODBOpenHelper.GO_P_TABLE, GODBOpenHelper.LATITUDE
                                                        + " = " + lat + " and " + GODBOpenHelper.LONGITUDE + " = " + lng, null);
                                                dbwrite.delete(GODBOpenHelper.GO_TABLE, GODBOpenHelper.GO_TABLE_ID
                                                        + " = \"" + key + "\"", null);
                                                pokestopsData.remove(pl);
                                                break;
                                            }
                                        }
                                        mMap.clear();

                                        for (Location pl : pokestopsData) {
                                            lat = pl.getLatitude();
                                            lng = pl.getLongitude();

//                 A new marker has been added, add it to the map from database
                                            latLng = new LatLng(lat, lng);
                                            mMap.addMarker(new MarkerOptions().position(latLng)
                                                    .title("Pokéstop")
                                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pokestop))
                                            );
                                        }
                                        for (Location gl : gymsData) {
                                            lat = gl.getLatitude();
                                            lng = gl.getLongitude();

//                 A new marker has been added, add it to the map from database
                                            latLng = new LatLng(lat, lng);
                                            mMap.addMarker(new MarkerOptions().position(latLng)
                                                    .title("Gym")
                                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.gym))
                                            );
                                        }
                                        Log.d("Line 507 - Userlatlng"," = " + userLatLng.latitude + ", " + userLatLng.longitude);
                                        circleOptions = new CircleOptions()
                                                .center(userLatLng)
                                                .radius(600)
                                                .fillColor(Color.argb(50,87,206,233))
                                                .strokeColor(Color.argb(50,87,206,233))
                                                .strokeWidth(0);
                                        // In meters

// Get back the mutable Circle
                                        if(!circle.isVisible()){
                                            circle = mMap.addCircle(circleOptions);
                                            Log.d("Circle","1");
                                        }
                                    }

                                    @Override
                                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                                mDatabase.child("gyms").child(country).child(state).addChildEventListener(new ChildEventListener() {
                                    @Override
                                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                                        Map<String, Double> map = (Map<String, Double>) dataSnapshot.getValue();

                                        lat = map.get("latitude");
                                        lng = map.get("longitude");

//                 A new marker has been added, add it to the map from database
                                        latLng = new LatLng(lat, lng);
                                        mMap.addMarker(new MarkerOptions().position(latLng)
                                                        .title("Gym")
//                .snippet("Population: 4,137,400")
                                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.gym))
                                        );
                                        Map<String, String> mapInt = (Map<String, String>) dataSnapshot.getValue();
                                        numLike = mapInt.get("numLikes");
                                        numDislike = mapInt.get("numDislikes");

                                        key = dataSnapshot.getKey();
                                        Log.d(TAG, "Gym added " + lat + "  ," + lng + " ," + numLike + " ," + numDislike + ": key: " + key);

                                        location = new Location(lat, lng, numLike, numDislike, key);
                                        gymsData.add(location);
                                        GODBOpenHelper helper = new GODBOpenHelper(GoMapsActivity.this);
                                        SQLiteDatabase db = helper.getWritableDatabase();
                                        String[] columns = {GODBOpenHelper.GO_TABLE_ID, GODBOpenHelper.GYM, GODBOpenHelper.POKESTOP,
                                                GODBOpenHelper.LONGITUDE, GODBOpenHelper.LATITUDE, GODBOpenHelper.LIKED, GODBOpenHelper.DISLIKED};

                                        Cursor c = db.query(GODBOpenHelper.GO_TABLE, columns, null, null, null, null, null);
                                        int count = 0;
                                        while (c.moveToNext()) {
                                            if ((lat == c.getDouble(c.getColumnIndex(GODBOpenHelper.LATITUDE)))
                                                    && (lng == c.getDouble(c.getColumnIndex(GODBOpenHelper.LONGITUDE)))) {
                                                count = 1;
                                                break;
                                            }
                                        }
                                        if (location != null && count != 1) {
                                            ContentValues cv = new ContentValues();

                                            cv.put(GODBOpenHelper.GO_TABLE_ID, location.getKey());
                                            cv.put(GODBOpenHelper.POKESTOP, 0);
                                            cv.put(GODBOpenHelper.GYM, 1);
                                            cv.put(GODBOpenHelper.LATITUDE, location.getLatitude());
                                            cv.put(GODBOpenHelper.LONGITUDE, location.getLongitude());
                                            cv.put(GODBOpenHelper.LIKED, -1);
                                            cv.put(GODBOpenHelper.DISLIKED, -1);

                                            db.insert(GODBOpenHelper.GO_TABLE, null, cv);
                                        }
                                    }

                                    @Override
                                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                                        Map<String, Double> map = (Map<String, Double>) dataSnapshot.getValue();

                                        lat = map.get("latitude");
                                        lng = map.get("longitude");

//                 A new marker has been added, add it to the map from database
                                        Map<String, String> mapInt = (Map<String, String>) dataSnapshot.getValue();
                                        numLike = mapInt.get("numLikes");
                                        numDislike = mapInt.get("numDislikes");

                                        key = dataSnapshot.getKey();
                                        for (Location gl : gymsData) {
                                            if ((gl.getLatitude() == lat) && (gl.getLongitude() == lng)) {
                                                gl.setKey(key);
                                                gl.setNumLikes(numLike);
                                                gl.setNumDislikes(numDislike);

                                                Log.d(TAG, "Gym data updated : " + lat + "  ," + lng + " ," + numLike + " ," + numDislike + ": key: " + key);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                                        Map<String, Double> map = (Map<String, Double>) dataSnapshot.getValue();

                                        lat = map.get("latitude");
                                        lng = map.get("longitude");

                                        GODBOpenHelper helper = new GODBOpenHelper(GoMapsActivity.this);
                                        SQLiteDatabase dbwrite = helper.getWritableDatabase();

                                        key = dataSnapshot.getKey();

                                        //Remove gym
                                        for (Location gl : gymsData) {
                                            if ((gl.getLatitude() == lat) && (gl.getLongitude() == lng)) {
                                                key = gl.getKey();
                                                dbwrite.delete(GODBOpenHelper.GO_P_TABLE, GODBOpenHelper.LATITUDE
                                                        + " = " + lat + " and " + GODBOpenHelper.LONGITUDE + " = " + lng, null);
                                                dbwrite.delete(GODBOpenHelper.GO_TABLE, GODBOpenHelper.GO_TABLE_ID
                                                        + " = \"" + key + "\"", null);
                                                gymsData.remove(gl);
                                                break;

                                            }
                                        }
                                        mMap.clear();

                                        for (Location pl : pokestopsData) {
                                            lat = pl.getLatitude();
                                            lng = pl.getLongitude();

//                 A new marker has been added, add it to the map from database
                                            latLng = new LatLng(lat, lng);
                                            mMap.addMarker(new MarkerOptions().position(latLng)
                                                    .title("Pokéstop")
                                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pokestop))
                                            );
                                        }
                                        for (Location gl : gymsData) {
                                            lat = gl.getLatitude();
                                            lng = gl.getLongitude();

//                 A new marker has been added, add it to the map from database
                                            latLng = new LatLng(lat, lng);
                                            mMap.addMarker(new MarkerOptions().position(latLng)
                                                    .title("Gym")
                                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.gym))
                                            );
                                        }
                                        Log.d("Line 657 - Userlatlng", " = " + userLatLng.latitude + ", " + userLatLng.longitude);
                                        circleOptions = new CircleOptions()
                                                .center(userLatLng)
                                                .radius(600)
                                                .fillColor(Color.argb(50, 87, 206, 233))
                                                .strokeColor(Color.argb(50, 87, 206, 233))
                                                .strokeWidth(0);
                                        // In meters

// Get back the mutable Circle
                                        if (!circle.isVisible()) {
                                            circle = mMap.addCircle(circleOptions);
                                            Log.d("Circle", "2");
                                        }
                                    }

                                    @Override
                                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                                final FloatingActionButton actionB = (FloatingActionButton) findViewById(R.id.action_b);

                                actionB.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        flagb = 0;
                                        Toast.makeText(GoMapsActivity.this, "Hold your finger on the map to place marker", Toast.LENGTH_LONG).show();

                                        final FloatingActionsMenu menuMultipleActions = (FloatingActionsMenu) findViewById(R.id.multiple_actions);
                                        menuMultipleActions.collapse();

                                        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                                            @Override
                                            public void onMapLongClick(LatLng latLng) {
                                                float[] distance = new float[2];

                                                android.location.Location.distanceBetween(latLng.latitude, latLng.longitude,
                                                        circle.getCenter().latitude, circle.getCenter().longitude, distance);

//                                                if( distance[0] > circle.getRadius()  ){
//                                                    Toast.makeText(getBaseContext(), "Outside", Toast.LENGTH_LONG).show();
//                                                } else {
//                                                    Toast.makeText(getBaseContext(), "Inside", Toast.LENGTH_LONG).show();
//                                                }

                                             if (distance[0] <= circle.getRadius()){
                                                    if (flagb == 0 && mMap.getCameraPosition().zoom >= zoomLevel) {
                                                        flagb = 1;
//                                mMap.addMarker(new MarkerOptions().position(latLng)
//                                        .title("Pokéstop").icon(BitmapDescriptorFactory.fromResource(R.drawable.pokestop)));


                                                        lng = latLng.longitude;
                                                        lat = latLng.latitude;
                                                        location = new Location(lat, lng, "0", "0", null);
                                                        Intent intentVibrate = new Intent(getApplicationContext(), VibrateService.class);
                                                        startService(intentVibrate);
                                                        mDatabase.child("pokéstops").child(country).child(state).push().setValue(location);
//                                mDatabase.child("pokéstops").child(country).push().setValue(location);

                                                        //Remove Button
                                                        GODBOpenHelper helper = new GODBOpenHelper(GoMapsActivity.this);
                                                        SQLiteDatabase db = helper.getWritableDatabase();

                                                        ContentValues cv = new ContentValues();

                                                        cv.put(GODBOpenHelper.PARENT, 1);
                                                        cv.put(GODBOpenHelper.LATITUDE, lat);
                                                        cv.put(GODBOpenHelper.LONGITUDE, lng);

                                                        db.insert(GODBOpenHelper.GO_P_TABLE, null, cv);
                                                    }
                                                 else if (flagb == 0 && mMap.getCameraPosition().zoom < zoomLevel){
                                                        Toast.makeText(GoMapsActivity.this, "Please, Zoom in for accuracy", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                                else if (flagb != 1) {
                                                        Toast.makeText(GoMapsActivity.this, "Location out of reach !!!", Toast.LENGTH_LONG).show();
                                                    }
                                            }
                                        });

                                    }
                                });
                                final FloatingActionButton actionA = (FloatingActionButton) findViewById(R.id.action_a);

                                actionA.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        flaga = 0;
                                        Toast.makeText(GoMapsActivity.this, "Hold your finger on the map to place marker", Toast.LENGTH_LONG).show();
                                        menuMultipleActions = (FloatingActionsMenu) findViewById(R.id.multiple_actions);
                                        menuMultipleActions.collapse();

                                        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                                            @Override
                                            public void onMapLongClick(LatLng latLng) {
                                                float[] distance = new float[2];

                                                android.location.Location.distanceBetween(latLng.latitude, latLng.longitude,
                                                        circle.getCenter().latitude, circle.getCenter().longitude, distance);

//                                                if( distance[0] > circle.getRadius()  ){
//                                                    Toast.makeText(getBaseContext(), "Outside", Toast.LENGTH_LONG).show();
//                                                } else {
//                                                    Toast.makeText(getBaseContext(), "Inside", Toast.LENGTH_LONG).show();
//                                                }
                                                if (distance[0] <= circle.getRadius()){
                                                    if (flaga == 0 &&  mMap.getCameraPosition().zoom >= zoomLevel) {
                                                    flaga = 1;
//                            mMap.addMarker(new MarkerOptions().position(latLng)
//                                    .title("Gym").icon(BitmapDescriptorFactory.fromResource(R.drawable.gym)));
                                                    lng = latLng.longitude;
                                                    lat = latLng.latitude;
                                                    location = new Location(lat, lng, "0", "0", null);
                                                    Intent intentVibrate = new Intent(getApplicationContext(), VibrateService.class);
                                                    startService(intentVibrate);
                                                    mDatabase.child("gyms").child(country).child(state).push().setValue(location);

                                                    //Remove Button
                                                    GODBOpenHelper helper = new GODBOpenHelper(GoMapsActivity.this);
                                                    SQLiteDatabase db = helper.getWritableDatabase();

                                                    ContentValues cv = new ContentValues();

                                                    cv.put(GODBOpenHelper.PARENT, 1);
                                                    cv.put(GODBOpenHelper.LATITUDE, lat);
                                                    cv.put(GODBOpenHelper.LONGITUDE, lng);

                                                    db.insert(GODBOpenHelper.GO_P_TABLE, null, cv);
                                                    }
                                                    else if (flaga == 0 && mMap.getCameraPosition().zoom < zoomLevel){
                                                        Toast.makeText(GoMapsActivity.this, "Please, Zoom in for accuracy", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                                else if (flaga != 1) {
                                                    Toast.makeText(GoMapsActivity.this, "Location out of reach !!!", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    }
                                });

                                LatLng bagh3 = new LatLng(28.675853, 77.197418);

                                mMap.addMarker(new MarkerOptions().position(bagh3)
                                        .title("Pokestop")
                                        .snippet("Roshanara Garden")
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pokestop)));

//                                mMap.moveCamera(CameraUpdateFactory.newLatLng(bagh3));

                                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

                                    @Override
                                    public boolean onMarkerClick(final Marker marker) {
                                        fabRemove.setVisibility(View.GONE);
                                        GODBOpenHelper helper = new GODBOpenHelper(GoMapsActivity.this);

                                        menuMultipleActions.collapse();

                                        menuMultipleActions.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
                                            @Override
                                            public void onMenuExpanded() {
                                                marker.hideInfoWindow();
                                                fabRemove.setVisibility(View.GONE);
                                                tvdl.setVisibility(View.GONE);
                                                tvl.setVisibility(View.GONE);
                                                actionL.setVisibility(View.GONE);
                                                actionDl.setVisibility(View.GONE);
                                                actionA.setVisibility(View.VISIBLE);
                                                actionB.setVisibility(View.VISIBLE);
                                                tvg.setVisibility(View.VISIBLE);
                                                tvp.setVisibility(View.VISIBLE);
                                            }

                                            @Override
                                            public void onMenuCollapsed() {
                                                fabRemove.setVisibility(View.GONE);
                                                actionA.setVisibility(View.GONE);
                                                actionB.setVisibility(View.GONE);
                                                tvg.setVisibility(View.GONE);
                                                tvp.setVisibility(View.GONE);
//                ViewGroup.LayoutParams params = menuMultipleActions.getLayoutParams();
//                params.width = 5;
//                menuMultipleActions.setLayoutParams(params);
                                            }
                                        });

                                        LatLng position = marker.getPosition();
                                        lng = position.longitude;
                                        lat = position.latitude;
                                        currentZoom = mMap.getCameraPosition().zoom;
                                        if (currentZoom >= zoomLevel) {
                                            newZoom = currentZoom;
                                        } else {
                                            newZoom = zoomLevel;
                                        }
                                        new java.util.Timer().schedule(new TimerTask() {
                                                                           @Override
                                                                           public void run() {
                                                                               // Your logic here...

                                                                               // When you need to modify a UI element, do so on the UI thread.
                                                                               // 'getActivity()' is required as this is being ran from a Fragment.
                                                                               runOnUiThread(new Runnable() {
                                                                                   @Override
                                                                                   public void run() {
                                                                                       final CameraPosition MARKER =
                                                                                               new CameraPosition.Builder().target(new LatLng(lat, lng))
                                                                                                       .zoom(newZoom)
                                                                                                       .build();
                                                                                       mMap.animateCamera(CameraUpdateFactory.newCameraPosition(MARKER), null);
                                                                                   }
                                                                               });
                                                                           }
                                                                       }, 1
                                        );

                                        final SQLiteDatabase db = helper.getReadableDatabase();
                                        final ContentValues cv = new ContentValues();
                                        final SQLiteDatabase dbw = helper.getWritableDatabase();
                                        final String[] columns = {GODBOpenHelper.GO_TABLE_ID, GODBOpenHelper.GYM, GODBOpenHelper.POKESTOP,
                                                GODBOpenHelper.LONGITUDE, GODBOpenHelper.LATITUDE, GODBOpenHelper.LIKED, GODBOpenHelper.DISLIKED};


                                        final Cursor c = db.query(GODBOpenHelper.GO_TABLE, columns, null, null, null, null, null);
                                        String likes;
                                        String disLikes;
                                        //database

                                        if (pokestopsData == null)
                                            return false;
                                        else {
                                            for (Location pl : pokestopsData) {
                                                //number of likes and dislikes
                                                likes = pl.getNumLikes();
                                                disLikes = pl.getNumDislikes();
                                                if ((pl.getLatitude() == lat) && (pl.getLongitude() == lng)) {
                                                    Log.d("lat lng", " Lat v = " + lat + " Lng v = " + lng);
                                                    Log.d("getlat getlng", " Lat g = " + pl.getLatitude() + " Lng g = " + pl.getLongitude());
                                                    marker.setSnippet(likes + " Likes & " + disLikes + " Dislikes ");
                                                    //database for likes button enable or disable
                                                    while (c.moveToNext()) {
                                                        if ((pl.getLatitude() == c.getDouble(c.getColumnIndex(GODBOpenHelper.LATITUDE)))
                                                                && (pl.getLongitude() == c.getDouble(c.getColumnIndex(GODBOpenHelper.LONGITUDE)))) {
                                                            Log.d("data base lat lng", " Lat d = " + c.getDouble(c.getColumnIndex(GODBOpenHelper.LATITUDE))
                                                                    + " Lng d = " + c.getDouble(c.getColumnIndex(GODBOpenHelper.LONGITUDE)));
                                                            likeButtonToggle = c.getInt(c.getColumnIndex(GODBOpenHelper.LIKED));
                                                            dislikeButtonToggle = c.getInt(c.getColumnIndex(GODBOpenHelper.DISLIKED));
                                                            actionL.setVisibility(View.VISIBLE);
                                                            actionDl.setVisibility(View.VISIBLE);
                                                            tvl.setVisibility(View.VISIBLE);
                                                            tvdl.setVisibility(View.VISIBLE);

                                                            actionL.setEnabled(true);
                                                            actionDl.setEnabled(true);

                                                            if (likeButtonToggle == -1 &&
                                                                    dislikeButtonToggle == -1) {
                                                                actionL.setEnabled(true);
                                                                actionDl.setEnabled(true);
                                                            } else if (likeButtonToggle == 1) {
                                                                actionL.setEnabled(false);
                                                            } else if (dislikeButtonToggle == 1) {
                                                                actionDl.setEnabled(false);
                                                            }
                                                            break;
                                                        }
                                                    }
                                                }

                                            }
                                            for (Location gl : gymsData) {
                                                //number of likes and dislikes
                                                likes = gl.getNumLikes();
                                                disLikes = gl.getNumDislikes();
                                                if ((gl.getLatitude() == lat) && (gl.getLongitude() == lng)) {

                                                    marker.setSnippet(likes + " Likes & " + disLikes + " Dislikes ");
                                                    //database for likes button enable or disable
                                                    while (c.moveToNext()) {
                                                        if ((gl.getLatitude() == c.getDouble(c.getColumnIndex(GODBOpenHelper.LATITUDE)))
                                                                && (gl.getLongitude() == c.getDouble(c.getColumnIndex(GODBOpenHelper.LONGITUDE)))) {
                                                            likeButtonToggle = c.getInt(c.getColumnIndex(GODBOpenHelper.LIKED));
                                                            dislikeButtonToggle = c.getInt(c.getColumnIndex(GODBOpenHelper.DISLIKED));
                                                            actionL.setVisibility(View.VISIBLE);
                                                            actionDl.setVisibility(View.VISIBLE);
                                                            tvl.setVisibility(View.VISIBLE);
                                                            tvdl.setVisibility(View.VISIBLE);

                                                            actionL.setEnabled(true);
                                                            actionDl.setEnabled(true);

                                                            if (likeButtonToggle == -1 && dislikeButtonToggle == -1) {
                                                                actionL.setEnabled(true);
                                                                actionDl.setEnabled(true);
                                                            } else if (likeButtonToggle == 1) {
                                                                actionL.setEnabled(false);
                                                            } else if (dislikeButtonToggle == 1) {
                                                                actionDl.setEnabled(false);
                                                            }
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
//                    for (Location l : pokestopsData) {
//                        Log.d("pokedata : ", "enter in loop");
//                        if ((l.getLatitude() == lat) && (l.getLongitude() == lng)) {
//                            Log.d("pokedata :likes: ",l.getNumLikes());
//                            numLike = l.getNumLikes();
//                            numDislike = l.getNumDislikes();
//                            actionL.setTitle(numLike);
//                            actionDl.setTitle(numDislike);
//                        }
//                    }

                                            //Remove Marker button
                                            marker.showInfoWindow();
                                            SQLiteDatabase dbr = helper.getReadableDatabase();
                                            final SQLiteDatabase dbwrite = helper.getWritableDatabase();

                                            String[] columnsP = {GODBOpenHelper.PARENT, GODBOpenHelper.LONGITUDE, GODBOpenHelper.LATITUDE};

                                            Cursor cP = dbr.query(GODBOpenHelper.GO_P_TABLE, columnsP, null, null, null, null, null);
                                            while (cP.moveToNext()) {
                                                if ((lat == cP.getDouble(cP.getColumnIndex(GODBOpenHelper.LATITUDE)))
                                                        && (lng == cP.getDouble(cP.getColumnIndex(GODBOpenHelper.LONGITUDE)))) {
                                                    for (Location l : pokestopsData) {
                                                        if ((lat == l.getLatitude()) && (lng == l.getLongitude())) {
                                                            like = Integer.parseInt(l.getNumLikes());
                                                            if ((like < 10) && ((cP.getInt(cP.getColumnIndex(GODBOpenHelper.PARENT))) == 1)) {
                                                                fabRemove.setVisibility(View.VISIBLE);
                                                            } else if ((like > 10) && ((cP.getInt(cP.getColumnIndex(GODBOpenHelper.PARENT))) == 1))
                                                                fabRemove.setVisibility(View.GONE);
                                                            else
                                                                fabRemove.setVisibility(View.GONE);
                                                            break;
                                                        }
                                                    }
                                                    for (Location g : gymsData) {
                                                        if ((lat == g.getLatitude()) && (lng == g.getLongitude())) {
                                                            like = Integer.parseInt(g.getNumLikes());
                                                            if ((like < 10) && ((cP.getInt(cP.getColumnIndex(GODBOpenHelper.PARENT))) == 1)) {
                                                                fabRemove.setVisibility(View.VISIBLE);
                                                            } else if ((like > 10) && ((cP.getInt(cP.getColumnIndex(GODBOpenHelper.PARENT))) == 1))
                                                                fabRemove.setVisibility(View.GONE);
                                                            else
                                                                fabRemove.setVisibility(View.GONE);
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                            fabRemove.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    fabRemove.setVisibility(View.GONE);
                                                    marker.hideInfoWindow();
                                                    //Remove Pokestop
                                                    for (Location pl : pokestopsData) {
                                                        if ((pl.getLatitude() == lat) && (pl.getLongitude() == lng)) {
                                                            key = pl.getKey();
                                                            dbwrite.delete(GODBOpenHelper.GO_P_TABLE, GODBOpenHelper.LATITUDE
                                                                    + " = " + lat + " and " + GODBOpenHelper.LONGITUDE + " = " + lng, null);
                                                            dbw.delete(GODBOpenHelper.GO_TABLE, GODBOpenHelper.GO_TABLE_ID
                                                                    + " = \"" + key + "\"", null);
                                                            mDatabase.child("pokéstops").child(country).child(state).child(key).removeValue();
                                                            pokestopsData.remove(pl);
                                                            break;
                                                        }
                                                    }
                                                    //Remove gym
                                                    for (Location gl : gymsData) {
                                                        if ((gl.getLatitude() == lat) && (gl.getLongitude() == lng)) {
                                                            key = gl.getKey();
                                                            dbwrite.delete(GODBOpenHelper.GO_P_TABLE, GODBOpenHelper.LATITUDE
                                                                    + " = " + lat + " and " + GODBOpenHelper.LONGITUDE + " = " + lng, null);
                                                            dbw.delete(GODBOpenHelper.GO_TABLE, GODBOpenHelper.GO_TABLE_ID
                                                                    + " = \"" + key + "\"", null);
                                                            mDatabase.child("gyms").child(country).child(state).child(key).removeValue();
                                                            gymsData.remove(gl);
                                                            break;
                                                        }
                                                    }
                                                    actionL.setVisibility(View.GONE);
                                                    actionDl.setVisibility(View.GONE);
                                                    tvl.setVisibility(View.GONE);
                                                    tvdl.setVisibility(View.GONE);

                                                    mMap.clear();

                                                    for (Location pl : pokestopsData) {
                                                        lat = pl.getLatitude();
                                                        lng = pl.getLongitude();

//                 A new marker has been added, add it to the map from database
                                                        latLng = new LatLng(lat, lng);
                                                        mMap.addMarker(new MarkerOptions().position(latLng)
                                                                .title("Pokéstop")
                                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pokestop))
                                                        );
                                                    }
                                                    for (Location gl : gymsData) {
                                                        lat = gl.getLatitude();
                                                        lng = gl.getLongitude();

//                 A new marker has been added, add it to the map from database
                                                        latLng = new LatLng(lat, lng);
                                                        mMap.addMarker(new MarkerOptions().position(latLng)
                                                                .title("Gym")
                                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.gym))
                                                        );
                                                    }
                                                    Log.d("Line 1076 - Userlatlng"," = " + userLatLng.latitude + ", " + userLatLng.longitude);
                                                    circleOptions = new CircleOptions()
                                                            .center(userLatLng)
                                                            .radius(600)
                                                            .fillColor(Color.argb(50,87,206,233))
                                                            .strokeColor(Color.argb(50,87,206,233))
                                                            .strokeWidth(0);
                                                    // In meters

// Get back the mutable Circle
                                                    if(circle.isVisible()==false){
                                                        circle = mMap.addCircle(circleOptions);
                                                        Log.d("Circle","3");
                                                    }
//                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.invisible));
//                            if (marker.isVisible())
//                            marker.setVisible(false);
//                            marker.remove();
                                                }
                                            });
                                            actionL.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    Cursor clp = db.query(GODBOpenHelper.GO_TABLE, columns, null, null, null, null, null);
                                                    for (Location pl : pokestopsData) {
                                                        if ((pl.getLatitude() == lat) && (pl.getLongitude() == lng)) {
                                                            key = pl.getKey();
                                                            while (clp.moveToNext()) {
                                                                if ((pl.getLatitude() == c.getDouble(c.getColumnIndex(GODBOpenHelper.LATITUDE)))
                                                                        && (pl.getLongitude() == c.getDouble(c.getColumnIndex(GODBOpenHelper.LONGITUDE)))) {

                                                                    if ((c.getInt(c.getColumnIndex(GODBOpenHelper.LIKED)) == -1) && (c.getInt(c.getColumnIndex(GODBOpenHelper.DISLIKED)) == 1)) {

                                                                        int likeAdd = Integer.parseInt(pl.getNumLikes()) + 1;
                                                                        String likeAddS = String.valueOf(likeAdd);
                                                                        pl.setNumLikes(likeAddS);
                                                                        cv.put(GODBOpenHelper.LIKED, 1);

                                                                        int likeSub = Integer.parseInt(pl.getNumDislikes()) - 1;
                                                                        String likeSubS = String.valueOf(likeSub);
                                                                        pl.setNumDislikes(likeSubS);
                                                                        cv.put(GODBOpenHelper.DISLIKED, -1);

                                                                        int i = (dbw.update(GODBOpenHelper.GO_TABLE, cv, GODBOpenHelper.GO_TABLE_ID + " = \"" + key + "\"", null));
                                                                        Log.d("liked lat lng", " Lat v = " + lat + " Lng v = " + lng);
                                                                        Log.d("liked getlat getlng", " Lat g = " + pl.getLatitude() + " Lng g = " + pl.getLongitude());

                                                                        Log.d("liked Db Update", " = " + i);
                                                                        Location likeLoc = new Location(pl.getLatitude(), pl.getLongitude(), likeAddS, likeSubS, key);

                                                                        Toast.makeText(GoMapsActivity.this, "Liked" + likeAddS, Toast.LENGTH_SHORT).show();
                                                                        mDatabase.child("pokéstops").child(country).child(state).child(key).setValue(likeLoc);
                                                                        actionL.setVisibility(View.GONE);
                                                                        actionDl.setVisibility(View.GONE);
                                                                        tvl.setVisibility(View.GONE);
                                                                        tvdl.setVisibility(View.GONE);
                                                                        marker.hideInfoWindow();
                                                                        break;
                                                                    } else if ((c.getInt(c.getColumnIndex(GODBOpenHelper.LIKED)) == -1) && (c.getInt(c.getColumnIndex(GODBOpenHelper.DISLIKED)) == -1)) {
                                                                        int likeAdd = Integer.parseInt(pl.getNumLikes()) + 1;
                                                                        String likeAddS = String.valueOf(likeAdd);
                                                                        pl.setNumLikes(likeAddS);
                                                                        cv.put(GODBOpenHelper.LIKED, 1);
                                                                        int i = (dbw.update(GODBOpenHelper.GO_TABLE, cv, GODBOpenHelper.GO_TABLE_ID + " = \"" + key + "\"", null));
                                                                        Log.d("liked lat lng", " Lat v = " + lat + " Lng v = " + lng);
                                                                        Log.d("liked getlat getlng", " Lat g = " + pl.getLatitude() + " Lng g = " + pl.getLongitude());

                                                                        Log.d("liked Db Update", " = " + i);
                                                                        Location likeLoc = new Location(pl.getLatitude(), pl.getLongitude(), likeAddS, pl.getNumDislikes(), key);

                                                                        Toast.makeText(GoMapsActivity.this, "Liked" + likeAddS, Toast.LENGTH_SHORT).show();
                                                                        mDatabase.child("pokéstops").child(country).child(state).child(key).setValue(likeLoc);
                                                                        actionL.setVisibility(View.GONE);
                                                                        actionDl.setVisibility(View.GONE);
                                                                        tvl.setVisibility(View.GONE);
                                                                        tvdl.setVisibility(View.GONE);
                                                                        marker.hideInfoWindow();
                                                                        break;
                                                                    }
                                                                    Log.d("Key ", "pokestop db key = " + key);

                                                                    int i = (dbw.update(GODBOpenHelper.GO_TABLE, cv, GODBOpenHelper.GO_TABLE_ID + " = \"" + key + "\"", null));
                                                                    Log.d("Db key Update", " = " + i);
                                                                }
                                                            }
                                                            break;
                                                        }
                                                    }
                                                    Cursor clg = db.query(GODBOpenHelper.GO_TABLE, columns, null, null, null, null, null);
                                                    for (Location gl : gymsData) {
                                                        if ((gl.getLatitude() == lat) && (gl.getLongitude() == lng)) {
                                                            key = gl.getKey();
                                                            while (clg.moveToNext()) {
                                                                if ((gl.getLatitude() == c.getDouble(c.getColumnIndex(GODBOpenHelper.LATITUDE)))
                                                                        && (gl.getLongitude() == c.getDouble(c.getColumnIndex(GODBOpenHelper.LONGITUDE)))) {

                                                                    if ((c.getInt(c.getColumnIndex(GODBOpenHelper.LIKED)) == -1) && (c.getInt(c.getColumnIndex(GODBOpenHelper.DISLIKED)) == 1)) {

                                                                        int likeAdd = Integer.parseInt(gl.getNumLikes()) + 1;
                                                                        String likeAddS = String.valueOf(likeAdd);
                                                                        gl.setNumLikes(likeAddS);
                                                                        cv.put(GODBOpenHelper.LIKED, 1);

                                                                        int likeSub = Integer.parseInt(gl.getNumDislikes()) - 1;
                                                                        String likeSubS = String.valueOf(likeSub);
                                                                        gl.setNumDislikes(likeSubS);
                                                                        cv.put(GODBOpenHelper.DISLIKED, -1);

                                                                        int i = (dbw.update(GODBOpenHelper.GO_TABLE, cv, GODBOpenHelper.GO_TABLE_ID + " = \"" + key + "\"", null));
                                                                        Log.d("liked lat lng", " Lat v = " + lat + " Lng v = " + lng);
                                                                        Log.d("liked getlat getlng", " Lat g = " + gl.getLatitude() + " Lng g = " + gl.getLongitude());

                                                                        Log.d("liked Db Update", " = " + i);
                                                                        Location likeLoc = new Location(gl.getLatitude(), gl.getLongitude(), likeAddS, likeSubS, key);

                                                                        Toast.makeText(GoMapsActivity.this, "Liked" + likeAddS, Toast.LENGTH_SHORT).show();
                                                                        mDatabase.child("gyms").child(country).child(state).child(key).setValue(likeLoc);
                                                                        actionL.setVisibility(View.GONE);
                                                                        actionDl.setVisibility(View.GONE);
                                                                        tvl.setVisibility(View.GONE);
                                                                        tvdl.setVisibility(View.GONE);
                                                                        marker.hideInfoWindow();
                                                                        break;
                                                                    } else if ((c.getInt(c.getColumnIndex(GODBOpenHelper.LIKED)) == -1) && (c.getInt(c.getColumnIndex(GODBOpenHelper.DISLIKED)) == -1)) {
                                                                        int likeAdd = Integer.parseInt(gl.getNumLikes()) + 1;
                                                                        String likeAddS = String.valueOf(likeAdd);
                                                                        gl.setNumLikes(likeAddS);
                                                                        cv.put(GODBOpenHelper.LIKED, 1);
                                                                        int i = (dbw.update(GODBOpenHelper.GO_TABLE, cv, GODBOpenHelper.GO_TABLE_ID + " = \"" + key + "\"", null));
                                                                        Log.d("liked lat lng", " Lat v = " + lat + " Lng v = " + lng);
                                                                        Log.d("liked getlat getlng", " Lat g = " + gl.getLatitude() + " Lng g = " + gl.getLongitude());

                                                                        Log.d("liked Db Update", " = " + i);
                                                                        Location likeLoc = new Location(gl.getLatitude(), gl.getLongitude(), likeAddS, gl.getNumDislikes(), key);

                                                                        Toast.makeText(GoMapsActivity.this, "Liked" + likeAddS, Toast.LENGTH_SHORT).show();
                                                                        mDatabase.child("gyms").child(country).child(state).child(key).setValue(likeLoc);
                                                                        actionL.setVisibility(View.GONE);
                                                                        actionDl.setVisibility(View.GONE);
                                                                        tvl.setVisibility(View.GONE);
                                                                        tvdl.setVisibility(View.GONE);
                                                                        marker.hideInfoWindow();
                                                                        break;
                                                                    }
                                                                    Log.d("Key ", "pokestop db key = " + key);

                                                                    int i = (dbw.update(GODBOpenHelper.GO_TABLE, cv, GODBOpenHelper.GO_TABLE_ID + " = \"" + key + "\"", null));
                                                                    Log.d("Db key Update", " = " + i);
                                                                }
                                                            }
                                                            break;
                                                        }
                                                    }
                                                    fabRemove.setVisibility(View.GONE);
                                                }
                                            });
                                            actionDl.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    Cursor cdp = db.query(GODBOpenHelper.GO_TABLE, columns, null, null, null, null, null);
                                                    for (Location pl : pokestopsData) {
                                                        if ((pl.getLatitude() == lat) && (pl.getLongitude() == lng)) {
                                                            key = pl.getKey();
                                                            while (cdp.moveToNext()) {
                                                                if ((pl.getLatitude() == c.getDouble(c.getColumnIndex(GODBOpenHelper.LATITUDE)))
                                                                        && (pl.getLongitude() == c.getDouble(c.getColumnIndex(GODBOpenHelper.LONGITUDE)))) {

                                                                    if ((c.getInt(c.getColumnIndex(GODBOpenHelper.LIKED)) == 1) && (c.getInt(c.getColumnIndex(GODBOpenHelper.DISLIKED)) == -1)) {

                                                                        int likeAdd = Integer.parseInt(pl.getNumLikes()) - 1;
                                                                        String likeAddS = String.valueOf(likeAdd);
                                                                        pl.setNumLikes(likeAddS);
                                                                        cv.put(GODBOpenHelper.LIKED, -1);

                                                                        int likeSub = Integer.parseInt(pl.getNumDislikes()) + 1;
                                                                        String likeSubS = String.valueOf(likeSub);
                                                                        pl.setNumDislikes(likeSubS);
                                                                        cv.put(GODBOpenHelper.DISLIKED, 1);

                                                                        int i = (dbw.update(GODBOpenHelper.GO_TABLE, cv, GODBOpenHelper.GO_TABLE_ID + " = \"" + key + "\"", null));
                                                                        Log.d("liked lat lng", " Lat v = " + lat + " Lng v = " + lng);
                                                                        Log.d("liked getlat getlng", " Lat g = " + pl.getLatitude() + " Lng g = " + pl.getLongitude());

                                                                        Log.d("liked Db Update", " = " + i);
                                                                        Location likeLoc = new Location(pl.getLatitude(), pl.getLongitude(), likeAddS, likeSubS, key);

                                                                        Toast.makeText(GoMapsActivity.this, "Disliked " + likeAddS, Toast.LENGTH_SHORT).show();
                                                                        mDatabase.child("pokéstops").child(country).child(state).child(key).setValue(likeLoc);
                                                                        actionL.setVisibility(View.GONE);
                                                                        actionDl.setVisibility(View.GONE);
                                                                        tvl.setVisibility(View.GONE);
                                                                        tvdl.setVisibility(View.GONE);
                                                                        marker.hideInfoWindow();
                                                                        break;
                                                                    } else if ((c.getInt(c.getColumnIndex(GODBOpenHelper.LIKED)) == -1) && (c.getInt(c.getColumnIndex(GODBOpenHelper.DISLIKED)) == -1)) {
                                                                        int likeSub = Integer.parseInt(pl.getNumDislikes()) + 1;
                                                                        String likeSubS = String.valueOf(likeSub);
                                                                        pl.setNumDislikes(likeSubS);
                                                                        cv.put(GODBOpenHelper.DISLIKED, 1);
                                                                        int i = (dbw.update(GODBOpenHelper.GO_TABLE, cv, GODBOpenHelper.GO_TABLE_ID + " = \"" + key + "\"", null));
                                                                        Log.d("liked lat lng", " Lat v = " + lat + " Lng v = " + lng);
                                                                        Log.d("liked getlat getlng", " Lat g = " + pl.getLatitude() + " Lng g = " + pl.getLongitude());

                                                                        Log.d("liked Db Update", " = " + i);
                                                                        Location likeLoc = new Location(pl.getLatitude(), pl.getLongitude(), pl.getNumLikes(), likeSubS, key);

                                                                        Toast.makeText(GoMapsActivity.this, "DisLiked" + likeSubS, Toast.LENGTH_SHORT).show();
                                                                        mDatabase.child("pokéstops").child(country).child(state).child(key).setValue(likeLoc);
                                                                        actionL.setVisibility(View.GONE);
                                                                        actionDl.setVisibility(View.GONE);
                                                                        tvl.setVisibility(View.GONE);
                                                                        tvdl.setVisibility(View.GONE);
                                                                        marker.hideInfoWindow();
                                                                        break;
                                                                    }
                                                                    Log.d("Key ", "pokestop db key = " + key);

                                                                    int i = (dbw.update(GODBOpenHelper.GO_TABLE, cv, GODBOpenHelper.GO_TABLE_ID + " = \"" + key + "\"", null));
                                                                    Log.d("Db key Update", " = " + i);
                                                                }
                                                            }
                                                            break;
                                                        }
                                                    }
                                                    Cursor cdg = db.query(GODBOpenHelper.GO_TABLE, columns, null, null, null, null, null);

                                                    for (Location gl : gymsData) {
                                                        if ((gl.getLatitude() == lat) && (gl.getLongitude() == lng)) {
                                                            key = gl.getKey();
                                                            while (cdg.moveToNext()) {
                                                                if ((gl.getLatitude() == c.getDouble(c.getColumnIndex(GODBOpenHelper.LATITUDE)))
                                                                        && (gl.getLongitude() == c.getDouble(c.getColumnIndex(GODBOpenHelper.LONGITUDE)))) {

                                                                    if ((c.getInt(c.getColumnIndex(GODBOpenHelper.LIKED)) == 1) && (c.getInt(c.getColumnIndex(GODBOpenHelper.DISLIKED)) == -1)) {

                                                                        int likeAdd = Integer.parseInt(gl.getNumLikes()) - 1;
                                                                        String likeAddS = String.valueOf(likeAdd);
                                                                        gl.setNumLikes(likeAddS);
                                                                        cv.put(GODBOpenHelper.LIKED, -1);

                                                                        int likeSub = Integer.parseInt(gl.getNumDislikes()) + 1;
                                                                        String likeSubS = String.valueOf(likeSub);
                                                                        gl.setNumDislikes(likeSubS);
                                                                        cv.put(GODBOpenHelper.DISLIKED, 1);

                                                                        int i = (dbw.update(GODBOpenHelper.GO_TABLE, cv, GODBOpenHelper.GO_TABLE_ID + " = \"" + key + "\"", null));
                                                                        Log.d("liked lat lng", " Lat v = " + lat + " Lng v = " + lng);
                                                                        Log.d("liked getlat getlng", " Lat g = " + gl.getLatitude() + " Lng g = " + gl.getLongitude());

                                                                        Log.d("liked Db Update", " = " + i);
                                                                        Location likeLoc = new Location(gl.getLatitude(), gl.getLongitude(), likeAddS, likeSubS, key);

                                                                        Toast.makeText(GoMapsActivity.this, "Disliked " + likeAddS, Toast.LENGTH_SHORT).show();
                                                                        mDatabase.child("gyms").child(country).child(state).child(key).setValue(likeLoc);
                                                                        actionL.setVisibility(View.GONE);
                                                                        actionDl.setVisibility(View.GONE);
                                                                        tvl.setVisibility(View.GONE);
                                                                        tvdl.setVisibility(View.GONE);
                                                                        marker.hideInfoWindow();
                                                                        break;
                                                                    } else if ((c.getInt(c.getColumnIndex(GODBOpenHelper.LIKED)) == -1) && (c.getInt(c.getColumnIndex(GODBOpenHelper.DISLIKED)) == -1)) {
                                                                        int likeSub = Integer.parseInt(gl.getNumDislikes()) + 1;
                                                                        String likeSubS = String.valueOf(likeSub);
                                                                        gl.setNumDislikes(likeSubS);
                                                                        cv.put(GODBOpenHelper.DISLIKED, 1);
                                                                        int i = (dbw.update(GODBOpenHelper.GO_TABLE, cv, GODBOpenHelper.GO_TABLE_ID + " = \"" + key + "\"", null));
                                                                        Log.d("liked lat lng", " Lat v = " + lat + " Lng v = " + lng);
                                                                        Log.d("liked getlat getlng", " Lat g = " + gl.getLatitude() + " Lng g = " + gl.getLongitude());

                                                                        Log.d("liked Db Update", " = " + i);
                                                                        Location likeLoc = new Location(gl.getLatitude(), gl.getLongitude(), gl.getNumLikes(), likeSubS, key);

                                                                        Toast.makeText(GoMapsActivity.this, "DisLiked" + likeSubS, Toast.LENGTH_SHORT).show();
                                                                        mDatabase.child("gyms").child(country).child(state).child(key).setValue(likeLoc);
                                                                        actionL.setVisibility(View.GONE);
                                                                        actionDl.setVisibility(View.GONE);
                                                                        tvl.setVisibility(View.GONE);
                                                                        tvdl.setVisibility(View.GONE);
                                                                        marker.hideInfoWindow();
                                                                        break;
                                                                    }
                                                                    Log.d("Key ", "gym db key = " + key);

                                                                    int i = (dbw.update(GODBOpenHelper.GO_TABLE, cv, GODBOpenHelper.GO_TABLE_ID + " = \"" + key + "\"", null));
                                                                    Log.d("Db key Update", " = " + i);
                                                                }
                                                            }
                                                            break;
                                                        }
                                                    }
                                                    fabRemove.setVisibility(View.GONE);
                                                }
                                            });
                                        }
                                        return false;
                                    }
                                });
                            }

//                        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
//                            @Override
//                            public void onCameraChange(CameraPosition cameraPosition) {
//
//                            }
//                            hdfhdh
//                        });
                    }
                }
            });
            mMap.getUiSettings().setZoomControlsEnabled(true);
//            mMap.getUiSettings().set
            mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    if (mMap != null && userLatLng != null) {
                        Log.d("Line 1387 MyLoc ull"," = " + userLatLng.latitude + ", " + userLatLng.longitude);
//                        CameraPosition MARKER =
//                                new CameraPosition.Builder().target(userLatLng)
//                                        .zoom(zoomLevel)
//                                        .build();
//                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(MARKER), null);
                    } else if (mMap != null) {
                        startLocationUpdate();
                        Log.d("Line 1395 - Userlatlng"," = " + userLatLng.latitude + ", " + userLatLng.longitude);
                    }
                    return false;
                }
            });

            if (checkPermission()) {
                mMap.setMyLocationEnabled(true);
            }

//            userLocationFinder();

            mDatabase = FirebaseDatabase.getInstance().getReference();
            pokestopsData = new ArrayList<>();
            gymsData = new ArrayList<>();
//country state
            Log.d("A C E L " + state + "", country + "");

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                menuMultipleActions.collapse();
                actionL.setVisibility(View.GONE);
                actionDl.setVisibility(View.GONE);
                tvl.setVisibility(View.GONE);
                tvdl.setVisibility(View.GONE);
                fabRemove.setVisibility(View.GONE);
            }
        });
    }
    }

//    private void appIntro() {
//
//        plus = new ViewTarget(R.id.multiple_actions,this);
//        fabP = new ViewTarget(R.id.action_b,this);
//        fabG = new ViewTarget(R.id.action_a,this);
//        fabR = new ViewTarget(R.id.remove,this);
//        add = new ShowcaseView.Builder(this)
//                .withMaterialShowcase()
//                .setTarget(plus).setStyle(R.style.CustomShowcaseTheme2)
//                .setContentTitle("Add Button").blockAllTouches()
//                .setContentText("Press this button to choose from markers to place.")
//                .setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        add.hide();
//                        menuMultipleActions.expand();
//                        fabPokestop = new ShowcaseView.Builder(GoMapsActivity.this)
//                                .withMaterialShowcase()
//                                .setTarget(fabP).setStyle(R.style.CustomShowcaseTheme2)
//                                .setContentTitle("Pokestop Marker Button").blockAllTouches()
//                                .setContentText("Press this button to select Pokestop Marker.")
//                                .setOnClickListener(new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View view) {
//                                        fabPokestop.hide();
//                                        fabGym = new ShowcaseView.Builder(GoMapsActivity.this)
//                                                .withMaterialShowcase()
//                                                .setTarget(fabG).setStyle(R.style.CustomShowcaseTheme2)
//                                                .setContentTitle("Gym Marker Button").blockAllTouches()
//                                                .setContentText("Press this button to select Pokemon Gym Marker.")
//                                                .setOnClickListener(new View.OnClickListener() {
//                                                    @Override
//                                                    public void onClick(View view) {
//                                                        fabGym.hide();
//                                                        menuMultipleActions.collapse();
//                                                        fab = new ShowcaseView.Builder(GoMapsActivity.this)
//                                                                .withMaterialShowcase()
//                                                                .setStyle(R.style.CustomShowcaseTheme2)
//                                                                .setContentTitle("Placing a Marker").blockAllTouches()
//                                                                .setContentText("After selcting one of the Markers you need to hold your finger at the location on Map where you want to mark a Pokestop or a Gym.")
//                                                                .setOnClickListener(new View.OnClickListener() {
//                                                                    @Override
//                                                                    public void onClick(View view) {
//                                                                        fab.hide();
//                                                                        fabRemove.setVisibility(View.VISIBLE);
//                                                                        fabRmv = new ShowcaseView.Builder(GoMapsActivity.this)
//                                                                                .withMaterialShowcase()
//                                                                                .setTarget(fabR)
//                                                                                .setStyle(R.style.CustomShowcaseTheme2)
//                                                                                .setContentTitle("Removing a Marker").blockAllTouches()
//                                                                                .setContentText("Tap a marker you placed and then press this Remove Button to remove that marker.")
//                                                                                .setOnClickListener(new View.OnClickListener() {
//                                                                                    @Override
//                                                                                    public void onClick(View view) {
//                                                                                        fabRmv.hide();
//                                                                                        fabRemove.setVisibility(View.GONE);
//                                                                                        fabRmv = new ShowcaseView.Builder(GoMapsActivity.this)
//                                                                                                .withMaterialShowcase()
//                                                                                                .setStyle(R.style.CustomShowcaseTheme2)
//                                                                                                .setContentTitle("Removing a Marker").blockAllTouches()
//                                                                                                .setContentText("Note : " +
//                                                                                                        "You cannot remove a Marker you placed if it has acquired more than 10 likes from other Users")
//                                                                                                .build();
//                                                                                    }
//                                                                                })
//                                                                                .build();
//                                                                    }
//                                                                })
//                                                                .build();
//                                                    }
//                                                })
//                                                .build();
//                                    }
//                                })
//                                .build();
//                    }
//                })
//                .build();
////        sv.setButtonPosition(lps);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()== R.id.menu_tutorial){
//            appIntro();
        }
        if(item.getItemId()== R.id.menu_faq){
            Intent i = new Intent();
            i.setClass(GoMapsActivity.this, Faq.class);
            startActivity(i);
        }
        if(item.getItemId()== R.id.menu_about){
            Intent i = new Intent();
            i.setClass(GoMapsActivity.this, About.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }
    private boolean checkPermission() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return false;
        }
        return true;
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (!checkPermission())
            return;
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(mClient);
        Log.d("Line 1553 - Lastloc ll"," = " + lastLocation.getLatitude() + ", " + lastLocation.getLongitude());
        if (lastLocation == null) {
            Toast.makeText(this, "No last Location found", Toast.LENGTH_LONG).show();
            return;
        }
        double la = lastLocation.getLatitude();
        double lo = lastLocation.getLongitude();

//        Toast.makeText(this, la + " " + lo, Toast.LENGTH_SHORT).show();
        userLatLng = new LatLng(la,lo);
        Log.d("Line 1563 - Userlatlng"," = " + userLatLng.latitude + ", " + userLatLng.longitude);
        // Instantiates a new CircleOptions object and defines the center and radius

        circleOptions = new CircleOptions()
                .center(userLatLng)
                .radius(600)
                .fillColor(Color.argb(50,87,206,233))
                .strokeColor(Color.argb(50,87,206,233))
                .strokeWidth(0);
        // In meters
        Log.d("Line 1573 - Userlatlng"," = " + userLatLng.latitude + ", " + userLatLng.longitude);
// Get back the mutable Circle
        if(circleFlag++ == 0) {
            circle = mMap.addCircle(circleOptions);
            Log.d("Circle", "4 Flag = " + circleFlag);
        }
        else{
            circle.remove();
            circle = mMap.addCircle(circleOptions);
            Log.d("Circle","5");
        }

        Log.d("Line 1582 - Userlatlng"," = " + userLatLng.latitude + ", " + userLatLng.longitude);
//        if (userLatLng != null && mMap != null) {
//            CameraPosition MARKER =
//                    new CameraPosition.Builder().target(userLatLng)
//                            .zoom(zoomLevel)
//                            .build();
//            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(MARKER), null);
//        }
        startLocationUpdate();
    }

//    private LatLng userLocation(Double latitude, Double longitude){
//        LatLng userLocation = new LatLng(latitude,longitude);
//        return  userLocation;
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d("Line 1582 - Userlatlng"," = " + userLatLng.latitude + ", " + userLatLng.longitude);
                startLocationUpdate();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private void startLocationUpdate() {
        Log.d("Line 1620 - Userlatlng"," = " + userLatLng.latitude + ", " + userLatLng.longitude);
        requestingLocationUpdates = true;
        final LocationRequest lr = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000)
                .setFastestInterval(2000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(lr);
        LocationServices.SettingsApi.checkLocationSettings(mClient, builder.build())
                .setResultCallback(new ResultCallback<LocationSettingsResult>() {
                    @Override
                    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                        int statusCode = locationSettingsResult.getStatus().getStatusCode();
                        if (statusCode == LocationSettingsStatusCodes.SUCCESS) {
                            if(!checkPermission())
                                return;
                            LocationServices.FusedLocationApi.requestLocationUpdates(mClient, lr, GoMapsActivity.this);
                        } else if (statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                            try {
                                locationSettingsResult.getStatus()
                                        .startResolutionForResult(GoMapsActivity.this, 1);
                            } catch (IntentSender.SendIntentException e) {
                                // ignore
                            }
                        } else {
                            Toast.makeText(GoMapsActivity.this, "Could not fetch Location", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "connection to location services failed " + i, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "failed " + connectionResult.getErrorMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        mClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mClient.disconnect();
        super.onStop();
    }

    android.location.Location bestLocation = null;
    int i = 0;
    @Override
    public void onLocationChanged(android.location.Location location) {
        Log.d("Line 1645 - Location"," = " + location.getLatitude() + ", " + location.getLongitude());
        if (bestLocation == null || location.getAccuracy() < bestLocation.getAccuracy())
            bestLocation = location;
        i++;
        if (bestLocation.getAccuracy() < 100 || i > 10) {
            i = 0;
            LocationServices.FusedLocationApi.removeLocationUpdates(mClient, this);
            useLocation();
        }
    }
    private void useLocation() {
        Toast.makeText(this, bestLocation.getLatitude() + " " + bestLocation.getLongitude() + " "
                + bestLocation.getAccuracy(), Toast.LENGTH_LONG).show();
        double la = bestLocation.getLatitude();
        double lo = bestLocation.getLongitude();
        userLatLng = new LatLng(la,lo);
        Log.d("Line 1659 - Userlatlng"," = " + userLatLng.latitude + ", " + userLatLng.longitude);
        // Instantiates a new CircleOptions object and defines the center and radius

        circleOptions = new CircleOptions()
                .center(userLatLng)
                .radius(600)
                .fillColor(Color.argb(50,87,206,233))
                .strokeColor(Color.argb(50,87,206,233))
                .strokeWidth(0);
        // In meters

// Get back the mutable Circle
        if(circle.isVisible()){
            circle.remove();
            circle = mMap.addCircle(circleOptions);
            Log.d("Circle","6");
        }
        else {
            circle = mMap.addCircle(circleOptions);
            Log.d("Circle","7");
        }
    }
    protected void onPause(){
        super.onPause();
        stopLocationUpdates();
    }
    @Override
    public void onResume() {
        super.onResume();
        if (mClient.isConnected() && !requestingLocationUpdates) {
            startLocationUpdate();
        }
    }
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mClient, this);
    }
//    userLocationData = new UserLocationData();
    public void userLocationFinder(LatLng userLoc) throws Exception {
        Log.d("Line 1690 - Userlatlng"," = " + userLatLng.latitude + ", " + userLatLng.longitude);
        Double uLat = userLoc.latitude;
        Double uLng = userLoc.longitude;
        com.google.maps.model.LatLng uLoc = new com.google.maps.model.LatLng(uLat,uLng);
        GeoApiContext geoContext = new GeoApiContext().setApiKey("AIzaSyD0C_D96znoq5-zd5JMXDnjCNcHPFmqvRw");

        GeocodingApiRequest req = GeocodingApi.newRequest(geoContext).latlng(uLoc);
//        GeocodingResult[] results = GeocodingApi.newRequest(geoContext).latlng(uLoc)
//                .awaitIgnoreError();
        req.setCallback(new PendingResult.Callback<GeocodingResult[]>() {
            @Override
            public void onResult(GeocodingResult[] result) {
                // Handle successful request.
                int i =0;
                for (GeocodingResult r : result){
                    AddressType[] addressTypes = r.types;
                    for (AddressType a : addressTypes){
                        if (a.toString() == "administrative_area_level_1"){
                            AddressComponent[] addressComponents = r.addressComponents;
                            for (AddressComponent add : addressComponents){
                                if (i == 0){
                                    userLocationData.setState(add.longName);
                                    Log.d("State name", " kush "+add.longName);
                                    i++;
                                }
                                else if (i == 1){
                                    userLocationData.setCountry(add.longName);
                                    Log.d("Country name", " kush "+add.longName);
                                }
                                else
                                    return;
                            }
                        }
                    }
                }
                Log.d(""+result,"result");
                if (result.length > 0)
                    Toast.makeText(GoMapsActivity.this,"Got Your Location Result",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(GoMapsActivity.this,"Havent Got Your Location Result",Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(Throwable e) {
                // Handle error.
            }
        });
//        GeocodingResult[] results =  GeocodingApi.geocode(geoContext,
//                "1600 Amphitheatre Parkway Mountain View, CA 94043").await();
//        System.out.println(results[0].formattedAddress);
//        GeocodingResult[] results = GeocodingApi.newRequest(geoContext)
//                .latlng(new com.google.maps.model.LatLng(-33.8674869,151.2069902)).awaitIgnoreError();
//
//        Log.d("log google : ",results[0].formattedAddress);
//        GeocodingResult[] result = GeocodingApi.reverseGeocode(geoContext,uLoc).await();
//        int i =0;
//        for (GeocodingResult r : result){
//            AddressType[] addressTypes = r.types;
//            for (AddressType a : addressTypes){
//                if (a.toString() == "administrative_area_level_1"){
//                    AddressComponent[] addressComponents = r.addressComponents;
//                    for (AddressComponent add : addressComponents){
//                        if (i == 0){
//                            userLocationData.setState(add.longName);
//                            Log.d("State name", " kush "+add.longName);
//                            i++;
//                        }
//                        else if (i == 1){
//                            userLocationData.setCountry(add.longName);
//                            Log.d("Country name", " kush "+add.longName);
//                        }
//                        else
//                            return;
//                    }
//                }
//            }
//        }
//        Log.d(""+result,"result");
//        if (result.length > 0)
//            Toast.makeText(GoMapsActivity.this,"Got Your Location Result",Toast.LENGTH_SHORT).show();
//        else
//            Toast.makeText(GoMapsActivity.this,"Havent Got Your Location Result",Toast.LENGTH_SHORT).show();
//        Geocoder gcd = new Geocoder(GoMapsActivity.this, Locale.getDefault());
//        List<Address> addresses = gcd.getFromLocation(userLoc.latitude, userLoc.longitude, 1);
//        if (addresses.size() > 0)
//            System.out.println(addresses.get(0).getLocality());

//        Double uLat = userLoc.latitude;
//        Double uLng = userLoc.longitude;
//        final GCAPIInterface apiService = GoClient.getService();
//        Call<GCResponse> call = apiService.getAddress(uLat + "," + uLng);
//        call.enqueue(new Callback<GCResponse>() {
//            @Override
//            public void onResponse(Call<GCResponse> call, Response<GCResponse> response) {
//                if (response.isSuccessful()) {
//                    ArrayList<GCResult> results = response.body().getResults();
//                    if (response == null)
//                        return;
//
//                    for (GCResult r : results) {
////                            resultsData.add(r);
//                        addressComponent = r.getAddress_components();
//                        state = addressComponent.get(0).getLong_name();
//                        country = addressComponent.get(1).getLong_name();
////                                        country = "India";
////                                        state = "Delhi";
//                        Log.d("userLocationFinder", state + " " + country);
//                    }
////                                    mapFunction();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<GCResponse> call, Throwable t) {
//                Toast.makeText(GoMapsActivity.this, "response failure", Toast.LENGTH_LONG).show();
//                Log.d("Failure","");
//            }
//        });
//        return addressComponent;
        return;
    }
}
