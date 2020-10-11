package com.siddhu.provider;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class WorkActivity extends AppCompatActivity {


    SharedPreferences sharedpreferences;

    SharedPreferences.Editor editor;

    public static final String providerPrefrences = "ProviderApp";


    private FusedLocationProviderClient mFusdedLocationClient;

    private  LocationRequest mLocationRequest;
    private LocationManager locationManager;

    Context context;

    private Switch mService;
    private Button mLogout;
    private Button mbutton,mbutton2,button3,starService,endService;

    private Location mLastLocation;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work);

        sharedpreferences = getSharedPreferences(providerPrefrences, Context.MODE_PRIVATE);

        //Checking for the GPS on or not
        mFusdedLocationClient = LocationServices.getFusedLocationProviderClient(this);


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


//        mService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                //Start service
//                if(isChecked){
//                    mLocationRequest = new LocationRequest();
//                    mLocationRequest.setInterval(1000);
//                    mLocationRequest.setFastestInterval(1000);
//                    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//
//                    mFusdedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallback, Looper.myLooper());
//
//                //Stopservice
//                }else{
//                    stopDriverServicer();
//                }
//            }
//        });


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
                Intent intent = new Intent(WorkActivity.this,BackgroundLocationService.class);
                startService(intent);

            }
        });

        endService = findViewById(R.id.stopService);
        endService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WorkActivity.this,BackgroundLocationService.class);
                stopService(intent);
            }
        });

    }




    LocationCallback mLocationCallback = new LocationCallback(){

        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            for(Location location:locationResult.getLocations()){
                if (getApplicationContext() != null) {

                    String msg = "Location " + location.toString();
                    showMsg(msg);
                }
            }
        }


        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);

            if(!locationAvailability.isLocationAvailable()){
                String msg = "No Location Updates";
                showMsg(msg);
            }
        }


    };

    //Turning of the location service
    private void stopDriverServicer(){
        if(mFusdedLocationClient != null){
            mFusdedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    private void clearLocalData(){
        editor = sharedpreferences.edit();
        editor.clear();
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
