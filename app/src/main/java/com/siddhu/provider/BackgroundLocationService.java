package com.siddhu.provider;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
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

public class BackgroundLocationService extends Service {

    private FusedLocationProviderClient mLocationClient;
    private LocationCallback mLocationCallback;

    private String userId;

    public BackgroundLocationService() {

    }


    @Override
    public void onCreate() {
        super.onCreate();
        mLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String msg = "Provider Location Service Started";
        showMsg(msg);


        getLocationUpdates();

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
                    GeoFire geoFireAvailable = new GeoFire(refAvailable);
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


    @Override
    public void onDestroy() {
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
