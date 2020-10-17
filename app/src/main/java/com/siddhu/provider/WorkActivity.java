package com.siddhu.provider;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;

public class WorkActivity extends AppCompatActivity {

    public static final String CLIENT_NAME = "CLIENT_NAME";
    public static final String CLIENT_PHONE_NUMBER = "ClIENT_PHONE_NUMBER";
    public static final String CLIENT_SOURCE = "CLIENT_SOURCE";
    public static final String CLIENT_DESTINATION = "CLIENT_DESTINATION";
    public static final String CLIENT_TRUCKTYPE = "CLIENT_TRUCKTYPE";
    public static final String CLIENT_DATE = "CLIENT_DATE";
    public static final String CLIENT_TIME = "CLIENT_TIME";
    public static final String CLIENT_NOTE = "CLIENT_NOTE";

    public static final String providerPrefrences = "ProviderApp";
    public static final String AVAILABLE_DRIVERS = "DriversAvailable";
    public static final Double RADIUS = 10.0;
    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 7000; /* 4 sec */

    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;
    HashMap map;
    private boolean availableDriverFlag = false;
    private TextView noteTextView;

    private DatabaseReference availableDriverDataBaseRef;

    private DatabaseReference driverRequestDatabase;
    private ValueEventListener driverRequestDatabaseEventLister;

    private GeoFire clientRequestGeoFire;
    private GeoFire availableDriverRequestGeoFire;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location currentLocation;

    Context context;

    private String currentDriverId;
    private Switch mService;
    private Button mLogout;
    private Button mbutton,mbutton2,button3,starService,endService;
    private Location mLastLocation;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work);

        currentDriverId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        sharedpreferences = getSharedPreferences(providerPrefrences, Context.MODE_PRIVATE);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);

        noteTextView = findViewById(R.id.noteTextView);

        mService = findViewById(R.id.serviceSwitch);
        mLogout = findViewById(R.id.logoutButton);

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                releaseResources();
                clearLocalData();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(WorkActivity.this,MainActivity.class));
                finish();
            }
        });

        starService = findViewById(R.id.startService);
        starService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(WorkActivity.this,BackgroundLocationService.class);
//                startService(intent);
                startLocationUpdates();

            }
        });

        endService = findViewById(R.id.stopService);
        endService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(WorkActivity.this,BackgroundLocationService.class);
//                stopService(intent);
                releaseResources();

            }
        });

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {

                } else {
                    for (Location location : locationResult.getLocations()) {
                        if (location != null) {
                            currentLocation = location;
                            showMsg("location updating");
                            broadCastLocation(currentLocation);
                        }
                    }
                }
            }
        };
    }

    public void startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void broadCastLocation(Location currentLocation) {

        availableDriverDataBaseRef = FirebaseDatabase.getInstance().getReference().child("driversAvailable");
        availableDriverRequestGeoFire = new GeoFire(availableDriverDataBaseRef);
        availableDriverRequestGeoFire.setLocation(currentDriverId, new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    if (error != null) {
                        String msg = "driversAvailable request problem can't solve it";
                        showMsg(msg);
                        showMsg(error.toString());
                    } else {
                        if(!availableDriverFlag){
                            availableDriverFlag = true;
                            waitForOrder();
                        }
                    }
                }
            });
        showMsg("Firebase service is running");
    }

    private void waitForOrder(){
        String msg = "Waiting for an order";
        showMsg(msg);
        driverRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Drivers").child(currentDriverId).child("Request");
        driverRequestDatabase.addValueEventListener(driverRequestDatabaseEventLister = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    map = (HashMap <String,Object>) snapshot.getValue();
                    String msg = "Order found";
                    showMsg(msg);
                   displayNewOffer(map);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showMsg(error.getMessage());
            }
        });
    }

    private void displayOrder(Map map) {
        String order = "";
        order += getvalue(map,CLIENT_NAME);
        order += getvalue(map,CLIENT_PHONE_NUMBER);
        order += getvalue(map,CLIENT_SOURCE);
        order += getvalue(map,CLIENT_DESTINATION);
        order += getvalue(map,CLIENT_TRUCKTYPE);
        order += getvalue(map,CLIENT_DATE);
        order += getvalue(map,CLIENT_TIME);
        order += getvalue(map,CLIENT_NOTE);

        noteTextView.setText(order);
    }
    private String getvalue(Map map,String key){
        if(map.containsKey(key)){
            return map.get(key).toString();
        }
        return key+" is null";
    }

    private void clearLocalData(){
        editor = sharedpreferences.edit();
        String phoneNumber = sharedpreferences.getString("DRIVER_PHONE_NUMBER","");
        editor.remove("DRIVER_PHONE_NUMBER");
        editor.remove("DRIVER_NAME");
        editor.remove("DRIVER_LOCALITY");
        editor.remove("TRUCK_TYPE");
        editor.commit();
    }

    private void releaseResources() {
        stopLocationUpdates();
        removeAvailableDriver();
    }

    private void displayNewOffer(HashMap map){
        Intent intent = new Intent(this, OfferActivity.class);
        intent.putExtra("map", map);
        releaseResources();

        startActivity(intent);

    }

    private void stopLocationUpdates() {
        if (mFusedLocationClient != null) {
            String msg = "location service distroyed.";
            showMsg(msg);
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private void removeAvailableDriver(){
        availableDriverDataBaseRef = FirebaseDatabase.getInstance().getReference().child("driversAvailable");
        availableDriverRequestGeoFire = new GeoFire(availableDriverDataBaseRef);
        availableDriverRequestGeoFire.removeLocation(currentDriverId ,new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (error != null)
                    showMsg(error.getMessage());
            }
        });
    }

    //Function to check Permission
    private void checkPermissions(){

        boolean foreground = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (foreground) {
            boolean background = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;

            if (background) {
                //Project permissions granted
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 1);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 1);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {

            boolean foreground = false, background = false;

            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equalsIgnoreCase(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    //foreground permission allowed
                    if (grantResults[i] >= 0) {
                        foreground = true;
                        Toast.makeText(getApplicationContext(), "Foreground location permission allowed", Toast.LENGTH_SHORT).show();
                        continue;
                    } else {
                        Toast.makeText(getApplicationContext(), "Location Permission denied", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }

                if (permissions[i].equalsIgnoreCase(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    if (grantResults[i] >= 0) {
                        foreground = true;
                        background = true;
                        Toast.makeText(getApplicationContext(), "Background location location permission allowed", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Background location location permission denied", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            if (foreground) {
                if (background) {
                    //background updates
                } else {
                    //foreground
                }
            }
        }
    }


    private void showMsg(String msg){
        //Snackbar.make(findViewById(android.R.id.content).getRootView(),msg, BaseTransientBottomBar.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        releaseResources();
        super.onDestroy();
    }
}