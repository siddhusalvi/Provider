package com.siddhu.provider;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class OrderActivity extends AppCompatActivity {

    public static final String CLIENT_NAME = "CLIENT_NAME";
    public static final String CLIENT_PHONE_NUMBER = "PHONE_NUMBER";
    public static final String providerPrefrences = "ProviderApp";
    public static final String AVAILABLE_DRIVERS = "DriversAvailable";
    public static final Double RADIUS = 10d;
    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;

    DatabaseReference clientDataBaseRef;
    DatabaseReference availableDriverDataBaseRef;
    GeoFire clientRequestGeoFire;
    GeoFire availableDriverRequestGeoFire;
    GeoQueryEventListener availableDriverQueryEventListener;

    private DatePickerDialog datePicker;
    private TimePickerDialog timePicker;

    private TextView dateTextView;
    private TextView timeTextView;

    private EditText sourecEditText;
    private EditText destinationEditText;
    private EditText noteEditText;
    private static final String TAG = "OrderActivity";
    private RadioGroup truckRadioGroup;

    private Button requestTruckButton;
    private Button logoutButton;

    private String name;
    private String source;
    private String destination;
    private String note;
    private String date;
    private String time;
    private String phoneNumber;
    private String currentUserId;
    private String resultDriverId;

    private int truckType;

    private boolean dateFlag = false;
    private boolean timeFlag = false;
    private boolean truckFlag = false;
    private boolean clientRequestDatabaseFlag = false;

    private Map trucksId;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location currentLocation;
    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 4000; /* 4 sec */
    private GeoQuery availableDriverGeoQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        sharedpreferences = getSharedPreferences(providerPrefrences, Context.MODE_PRIVATE);
        phoneNumber = sharedpreferences.getString(CLIENT_PHONE_NUMBER, "");
        name = sharedpreferences.getString(CLIENT_NAME, "");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);


        trucksId = new HashMap<String, Integer>();
        addTruckId();

        dateTextView = findViewById(R.id.dateTextView);
        timeTextView = findViewById(R.id.timeTextView);

        sourecEditText = findViewById(R.id.sourceEditText);
        destinationEditText = findViewById(R.id.destinationEditText);

        truckRadioGroup = findViewById(R.id.truckTypeRadioGroup);

        noteEditText = findViewById(R.id.noteEditText);

        requestTruckButton = findViewById(R.id.requestTruckButton);
        logoutButton = findViewById(R.id.logoutButton);


        dateTextView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.DAY_OF_MONTH);
                int year = calendar.get(Calendar.YEAR);

                datePicker = new DatePickerDialog(OrderActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        dateTextView.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                        date = dayOfMonth + " " + (month + 1) + " " + year;
                        dateFlag = true;
                    }
                }, year, month, day);
                datePicker.show();
            }
        });

        timeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(OrderActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        timeTextView.setText(selectedHour + ":" + selectedMinute);
                        time = selectedHour + " " + selectedHour;
                        timeFlag = true;
                    }
                }, hour, minute, false);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

        truckRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = group.findViewById(checkedId);
                truckType = (Integer) trucksId.get(radioButton.getText().toString());
                truckFlag = true;
//                Toast.makeText(getApplicationContext(),String.valueOf(truckType),Toast.LENGTH_SHORT).show();
            }
        });

        requestTruckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getAllRequestInfo()) {
                    String msg = name + " " + phoneNumber + source + " " + destination + " " + truckType + " " + date + " " + time + note + " " + "will be sent to the drivier";
                    showMsg(msg);
                    startLocationUpdates();
                }
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                releaseResources();
                startActivity(new Intent(OrderActivity.this,MainActivity.class));
                finish();
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
                            fireClientRequest(currentLocation);
                            if(resultDriverId!=null){
                                stopLocationUpdates();
                                String msg = "Driver Found " + resultDriverId + "sending request to the driver";
                                showMsg(msg);
                                noteEditText.setText(msg);
                            }
                        }
                    }
                }
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
            }
        };
    }

    public void startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void fireClientRequest(Location currentLocation) {

        if (!clientRequestDatabaseFlag) {
            clientRequestDatabaseFlag = true;
            getAvailableDriver();
            clientDataBaseRef = FirebaseDatabase.getInstance().getReference().child("ClientRequest");
            clientRequestGeoFire = new GeoFire(clientDataBaseRef);
            clientRequestGeoFire.setLocation(currentUserId, new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()), new GeoFire.CompletionListener() {
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
        }
        showMsg("Firebase service is running");
    }

    private void getAvailableDriver() {
        showMsg("searching nearest driver");
        availableDriverDataBaseRef = FirebaseDatabase.getInstance().getReference().child("driversAvailable");
        availableDriverRequestGeoFire = new GeoFire(availableDriverDataBaseRef);

        availableDriverGeoQuery = availableDriverRequestGeoFire.queryAtLocation(new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()), RADIUS);
        Log.d("cc","on getAvailableDriver");
        availableDriverGeoQuery.addGeoQueryEventListener(availableDriverQueryEventListener = new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                resultDriverId = key;
            }

            @Override
            public void onKeyExited(String key) {
                Log.d(TAG, "onKeyExited: ");
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Log.d(TAG, "onKeyMoved: ");
            }

            @Override
            public void onGeoQueryReady() {
                Log.d(TAG, "onGeoQueryReady: ");
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                showMsg(error.getMessage());
                Log.d(TAG, "onGeoQueryError: ");
            }
        });
    }


    private boolean getAllRequestInfo() {
        if (sourecEditText.getText() == null || sourecEditText.getText().toString().trim().length() == 0) {
            String msg = "Enter Source";
            showMsg(msg);
            return false;
        }
        source = sourecEditText.getText().toString();

        if (destinationEditText.getText() == null || destinationEditText.getText().toString().trim().length() == 0) {
            String msg = "Enter Destination";
            showMsg(msg);
            return false;
        }
        destination = destinationEditText.getText().toString();

        if (!truckFlag) {
            String msg = "select truck type";
            showMsg(msg);
            return false;
        }

        if (!dateFlag) {
            String msg = "Enter date";
            showMsg(msg);
            return false;
        }

        if (!timeFlag) {
            String msg = "Enter time";
            showMsg(msg);
            return false;
        }

        if (noteEditText.getText() == null || noteEditText.getText().toString().trim().length() == 0) {
            String msg = "Enter note";
            showMsg(msg);
            return false;
        }
        note = noteEditText.getText().toString();
        return true;
    }

    private void addTruckId() {
        trucksId.put("3 Wheeler Tempo", new Integer(1));
        trucksId.put("Tata Ace / Chota Hathi", new Integer(2));
        trucksId.put("Bolero Pickup 5 seater", new Integer(3));
        trucksId.put("Bolero Pickup 3 seater", new Integer(4));
        trucksId.put("Tata Tempo", new Integer(5));
        trucksId.put("Truck", new Integer(6));
    }

    private void showMsg(String msg) {
        //Snackbar.make(findViewById(android.R.id.content).getRootView(),msg, BaseTransientBottomBar.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void releaseResources(){
        stopLocationUpdates();
        removeDriverFromAvailableList();
        deleteClientRequest();
    }
    private void stopLocationUpdates(){
        if (mFusedLocationClient != null) {
            String msg = "location service distroyed.";
            showMsg(msg);
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private void removeDriverFromAvailableList(){
        availableDriverGeoQuery.removeGeoQueryEventListener(availableDriverQueryEventListener);
    }
    private void deleteClientRequest(){
        clientDataBaseRef = FirebaseDatabase.getInstance().getReference().child("ClientRequest");
        clientRequestGeoFire = new GeoFire(clientDataBaseRef);
        clientRequestGeoFire.removeLocation(currentUserId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if(error != null)
                    showMsg(error.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        releaseResources();
        super.onDestroy();
    }
}
