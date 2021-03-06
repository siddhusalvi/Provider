package com.siddhu.provider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class MandatoryDetailsActivity extends AppCompatActivity {

    public static final String DRIVER_NAME = "DRIVER_NAME";
    public static final String  DRIVER_LOCALITY = "DRIVER_LOCALITY";
    public static final String  TRUCK_TYPE = "TRUCK_TYPE";
    public static final String  DRIVER_PHONE_NUMBER = "DRIVER_PHONE_NUMBER";

    public static final String  PHONE_NUMBER = "PHONE_NUMBER";
    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;

    private DatabaseReference mDriverProfileDatabase;

    private FirebaseAuth mAuth;


    private RadioGroup mTruckGroup;

    private Button mRegisterInfo;

    private TextView mName;
    private TextView mLocality;


    private String userID;
    private String driverName;
    private String driverLocality;
    private String phoneNumber;


    private int truckType = 0;

    private Map trucksId;
    public static final String providerPrefrences = "ProviderApp";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mandatory_details);

        phoneNumber = getIntent().getStringExtra("phone");
        trucksId = new HashMap<String, Integer>();
        addTruckId();

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();


        sharedpreferences = getSharedPreferences(providerPrefrences, Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();

        mTruckGroup = findViewById(R.id.truckTypeRadioGroup);

        mRegisterInfo = findViewById(R.id.registerInfo);

        mName = findViewById(R.id.nameEditText);
        mLocality = findViewById(R.id.locationEditText);

        mTruckGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = group.findViewById(checkedId);
                truckType = (Integer) trucksId.get(radioButton.getText().toString());
//                Toast.makeText(getApplicationContext(),String.valueOf(truckType),Toast.LENGTH_SHORT).show();
            }
        });

        mRegisterInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(allInformationIsFilled() && isProfileSavedOnFireBase() && isProfileSavedOnLocal()){
                    String msg = "Data Saved";
                    showMsg(msg);
                    startActivity(new Intent(MandatoryDetailsActivity.this,WorkActivity.class));
                    finish();
                }
            }
        });

    }


    private boolean allInformationIsFilled(){
        if(driverNameIsFilled() && truckTypeIsFilled() && driverLocalityIsFilled()){
            return true;
        }else {
            return false;
        }

    }
    private boolean driverNameIsFilled(){
        driverName = mName.getText().toString();
        if(mName.getText()!=null && driverName.trim().length()!=0){
            return true;
        }else{
            String msg = "Please Enter Valid Name";
            showMsg(msg);
            return false;
        }
    }
    private boolean truckTypeIsFilled(){
        if(truckType != 0){
            return true;
        }else {
            String msg = "Please Select Truck Type";
            showMsg(msg);
            return false;
        }
    }
    //checking locality is filled
    private boolean driverLocalityIsFilled(){
        driverLocality = mLocality.getText().toString();
        if(mLocality.getText()!=null && driverLocality.trim().length()!=0){
            return true;
        }else{
            String msg = "Please Enter Valid Locality";
            showMsg(msg);
            return false;
        }
    }
    private boolean isProfileSavedOnFireBase(){
        mDriverProfileDatabase = FirebaseDatabase.getInstance().getReference().child("Drivers").child(userID).child("Profile");
        Map driverInfo = new HashMap();

        phoneNumber = sharedpreferences.getString(DRIVER_PHONE_NUMBER,"");
        driverInfo.put(PHONE_NUMBER,phoneNumber);
        driverInfo.put(DRIVER_NAME,driverName);
        driverInfo.put(DRIVER_LOCALITY,driverLocality);

        driverInfo.put(TRUCK_TYPE,String.valueOf(truckType));
        mDriverProfileDatabase.setValue(driverInfo);
        return true;
    }
    private boolean isProfileSavedOnLocal(){
        editor.putString(PHONE_NUMBER,phoneNumber);
        editor.putString(DRIVER_NAME,driverName);
        editor.putString(DRIVER_LOCALITY,driverLocality);
        editor.putInt(TRUCK_TYPE,truckType);
        editor.commit();
        return true;
    }

    private void addTruckId(){
        trucksId.put("3 Wheeler Tempo", new Integer(1));
        trucksId.put("Tata Ace / Chota Hathi", new Integer(2));
        trucksId.put("Bolero Pickup 5 seater", new Integer(3));
        trucksId.put("Bolero Pickup 3 seater", new Integer(4));
        trucksId.put("Tata Tempo", new Integer(5));
        trucksId.put("Truck", new Integer(6));
    }

    private void showMsg(String msg){
        //Snackbar.make(findViewById(android.R.id.content).getRootView(),msg, BaseTransientBottomBar.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }

}
