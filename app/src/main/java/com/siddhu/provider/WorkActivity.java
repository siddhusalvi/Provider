package com.siddhu.provider;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

public class WorkActivity extends AppCompatActivity {

    private FusedLocationProviderClient mFusdedLocationClient;

    private  LocationRequest mLocationRequest;

    private Switch mService;
    private Button mLogout;

    private Location mLastLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work);
        mFusdedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mService = findViewById(R.id.serviceSwitch);
        mLogout = findViewById(R.id.logoutButton);

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopDriverServicer();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(WorkActivity.this,MainActivity.class));
                finish();
            }
        });

        mService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //Start service
                if(isChecked){
                    mLocationRequest = new LocationRequest();
                    mLocationRequest.setInterval(1000);
                    mLocationRequest.setFastestInterval(1000);
                    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                    mFusdedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallback, Looper.myLooper());

                //Stopservice
                }else{
                    stopDriverServicer();
                }
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


    private void showMsg(String msg){
        //Snackbar.make(findViewById(android.R.id.content).getRootView(),msg, BaseTransientBottomBar.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }

}
