package com.siddhu.provider;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class OfferActivity extends AppCompatActivity {

    public static final String CLIENT_NAME = "CLIENT_NAME";
    public static final String CLIENT_PHONE_NUMBER = "ClIENT_PHONE_NUMBER";
    public static final String CLIENT_SOURCE = "CLIENT_SOURCE";
    public static final String CLIENT_DESTINATION = "CLIENT_DESTINATION";
    public static final String CLIENT_TRUCKTYPE = "CLIENT_TRUCKTYPE";
    public static final String CLIENT_DATE = "CLIENT_DATE";
    public static final String CLIENT_TIME = "CLIENT_TIME";
    public static final String CLIENT_NOTE = "CLIENT_NOTE";
    public static final String CLIENT_FIREBASE_ID = "CLIENT_FIREBASE_ID";
    public static final String OFFER_RESPONSE_DATABASE_REF = "OfferResponse";


    private RadioGroup truckTypeRadioGroup;

    private TextView nameTextView;
    private TextView phoneTextView;
    private TextView sourceTextView;
    private TextView destinationTextView;
    private TextView dateTextView;
    private TextView timeTextView;
    private TextView noteTextView;

    private Button acceptButton;
    private Button rejectButton;

    private DatabaseReference offerResponseDatabaseRefference;

    private String clientName;
    private String clientPhoneNumber;
    private String clientSource;
    private String clientDestination;
    private int clientTruckType;
    private String clientDate;
    private String clientTime;
    private String clientNote;
    private String clientFirebaseId;
    private String currentDriverId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer);

        Intent intent = getIntent();
        HashMap<String, Object> orderHashMap = (HashMap<String, Object>)intent.getSerializableExtra("map");

        currentDriverId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        nameTextView = findViewById(R.id.nameTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
        sourceTextView = findViewById(R.id.sourceTextView);
        destinationTextView = findViewById(R.id.destinationTextView);
        truckTypeRadioGroup = findViewById(R.id.truckRadioGroup);
        dateTextView = findViewById(R.id.dateTextView);
        timeTextView= findViewById(R.id.timeTextView);
        noteTextView= findViewById(R.id.noteTextView);

        acceptButton = findViewById(R.id.acceptButton);
        rejectButton = findViewById(R.id.rejectButton);

        fetchOrder(orderHashMap);
        displayOrder();

        offerResponseDatabaseRefference = FirebaseDatabase.getInstance().getReference().child("Drivers").child(currentDriverId).child(clientFirebaseId).child("Response");

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                offerResponseDatabaseRefference.setValue(true);

            }
        });

        rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                offerResponseDatabaseRefference.setValue(false);
            }
        });
    }

    private void fetchOrder(HashMap map) {
        clientName = getvalue(map,CLIENT_NAME);
        clientPhoneNumber = getvalue(map,CLIENT_PHONE_NUMBER);
        clientSource = getvalue(map,CLIENT_SOURCE);
        clientDestination = getvalue(map,CLIENT_DESTINATION);
        clientTruckType = Integer.valueOf(getvalue(map,CLIENT_TRUCKTYPE));
        clientDate = getDate(getvalue(map,CLIENT_DATE));
        clientTime = getTime(getvalue(map,CLIENT_TIME));
        clientNote = getvalue(map,CLIENT_NOTE);
        clientFirebaseId = getvalue(map,CLIENT_FIREBASE_ID);

    }

    private String getDate(String rawDate){
        return rawDate.replace(" ","-");
    }
    private String getTime(String rawTime){
        String timeArray[] = rawTime.split(" ");
        int hour = Integer.valueOf(timeArray[0]);
        if(hour > 12){
            hour = hour - 12;
            return String.valueOf(hour) + ":" + timeArray[1] + " PM";
        }else{
            return rawTime + " AM";
        }
    }

    private void setTruckType(){
        int radioButtonId = truckTypeRadioGroup.getChildAt(clientTruckType-1).getId();
        truckTypeRadioGroup.check(radioButtonId);
    }

    private void displayOrder() {
        nameTextView.setText(clientName);
        phoneTextView.setText(clientPhoneNumber);
        sourceTextView.setText(clientSource);
        destinationTextView.setText(clientDestination);
        setTruckType();
        dateTextView.setText(clientDate);
        timeTextView.setText(clientTime);
        noteTextView.setText(clientNote);
    }
    private String getvalue(HashMap map,String key){
        if(map.containsKey(key)){
            return map.get(key).toString();
        }
        return key+" is null";
    }

    private void showMsg(String msg) {
        //Snackbar.make(findViewById(android.R.id.content).getRootView(),msg, BaseTransientBottomBar.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
