package com.siddhu.provider;

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
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class WorkActivity extends AppCompatActivity {


    SharedPreferences sharedpreferences;

    SharedPreferences.Editor editor;

    public static final String providerPrefrences = "ProviderApp";
    public static final String AVAILABLE_DRIVERS = "DriversAvailable";
    public static final Double RADIUS = 10.0;
    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 4000; /* 4 sec */

    private DatabaseReference availableDriverDataBaseRef;
    private GeoFire clientRequestGeoFire;
    private GeoFire availableDriverRequestGeoFire;

    private FusedLocationProviderClient mFusdedLocationClient;
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

        //Checking for the GPS on or not
        mFusdedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);

        checkPermissions();

        mService = findViewById(R.id.serviceSwitch);
        mLogout = findViewById(R.id.logoutButton);

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopDriverServicer();
                clearLocalData();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(WorkActivity.this,MainActivity.class));
                finish();
            }
        });


        mbutton = findViewById(R.id.button);

        mbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WorkActivity.this,MandatoryDetailsActivity.class));
            }
        });



        mbutton2 = findViewById(R.id.button2);

        mbutton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedpreferences;
                SharedPreferences.Editor editor;
                String providerPrefrences = "ProviderApp";
                sharedpreferences = getSharedPreferences(providerPrefrences, Context.MODE_PRIVATE);
                editor = sharedpreferences.edit();
                editor.clear();
                editor.commit();
            }
        });


        button3 = findViewById(R.id.button3);

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WorkActivity.this,Main2Activity.class));

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
                stopLocationUpdates();

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
        mFusdedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void broadCastLocation(Location currentLocation) {

        availableDriverDataBaseRef = FirebaseDatabase.getInstance().getReference().child("driversAvailable");
        availableDriverRequestGeoFire = new GeoFire(availableDriverDataBaseRef);
        availableDriverRequestGeoFire.setLocation(currentDriverId, new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    if (error != null) {
//                    clientRequestDatabaseFlag = false;
                        String msg = "Setting client request flag off \n";
                        showMsg(msg);
                        showMsg(error.toString());
                    } else {

                    }
                }
            });

        showMsg("Firebase service is running");
    }



    //Turning of the location service
    private void stopDriverServicer(){
        if(mFusdedLocationClient != null){
            mFusdedLocationClient.removeLocationUpdates(locationCallback);
        }
        availableDriverRequestGeoFire.removeLocation(currentDriverId);
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

    private void stopLocationUpdates(){
        if(mFusdedLocationClient != null){
            mFusdedLocationClient.removeLocationUpdates(locationCallback);
        }
        availableDriverRequestGeoFire.removeLocation(currentDriverId ,new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if(error != null)
                    showMsg(error.getMessage());
            }
        });

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

}
