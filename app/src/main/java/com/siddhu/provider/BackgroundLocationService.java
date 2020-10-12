package com.siddhu.provider;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;

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

public class BackgroundLocationService extends Service {

    private FusedLocationProviderClient mLocationClient;
    private LocationCallback mLocationCallback;
    private ValueEventListener customerListener;
    private String userId;
    private GeoFire geoFireAvailable;
    private DatabaseReference customerRef;


    public BackgroundLocationService() {

    }


    @Override
    public void onCreate() {
        super.onCreate();
        mLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        customerListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    showMsg("Ride found for "+snapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                  showMsg(error.getMessage());
            }
        };

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String msg = "Provider Location Service Started";
        showMsg(msg);


        getLocationUpdates();
        waitForCustomer();
        return START_STICKY;


    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void getLocationUpdates() {
        final LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);

        locationRequest.setMaxWaitTime(5 * 1000);

        mLocationClient.requestLocationUpdates(locationRequest, mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                if (locationResult == null) {
                    String msg = "Location error in background";
                    showMsg(msg);
                    return;
                } else {
                    DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("driversAvailable");
                    geoFireAvailable = new GeoFire(refAvailable);
                    Double lat, lng;
                    lat = locationResult.getLastLocation().getLatitude();
                    lng = locationResult.getLastLocation().getLongitude();
                    if (lat != null && lng != null && userId != null) {
                        geoFireAvailable.setLocation(userId, new GeoLocation(lat, lng), new GeoFire.CompletionListener() {
                            @Override
                            public void onComplete(String key, DatabaseError error) {
                                if (error != null) {
                                    showMsg(error.toString());
                                } else {
                                    System.out.println("Location saved on server successfully!");
                                }
                            }
                        });
                        String msg = lat.toString() + "" + lng.toString();
                        showMsg(msg);
                    }
                }
            }
        }, Looper.myLooper());

    }

    private void waitForCustomer(){
        customerRef = FirebaseDatabase.getInstance().getReference().child("Drivers").child(userId).child("CustomerRequest").child("ride");
        customerRef.addValueEventListener(customerListener);

    }

    private void removeDateBaseRef(){

    }


    @Override
    public void onDestroy() {

        if(geoFireAvailable != null && userId!=null ){
        geoFireAvailable.removeLocation(userId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (error != null) {
                    showMsg(error.toString());
                } else {
                    System.out.println("Location saved on server successfully!");
                }
            }
        });}

        customerRef.removeEventListener(customerListener);
        mLocationClient.removeLocationUpdates(mLocationCallback);
        String msg = "Provider Location Service Distroyed";
        showMsg(msg);
        super.onDestroy();
    }

    private void showMsg(String msg) {
        //Snackbar.make(findViewById(android.R.id.content).getRootView(),msg, BaseTransientBottomBar.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
