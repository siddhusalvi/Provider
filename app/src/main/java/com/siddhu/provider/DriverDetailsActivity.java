package com.siddhu.provider;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

public class DriverDetailsActivity extends AppCompatActivity {

    public static final String DRIVER_NAME = "DRIVER_NAME";
    public static final String  DRIVER_LOCALITY = "DRIVER_LOCALITY";
    public static final String  TRUCK_TYPE = "TRUCK_TYPE";
    public static final String  DRIVER_PHONE_NUMBER = "DRIVER_PHONE_NUMBER";

    public static final String DRIVER_PROFILE_INFO = "DRIVER_PROFILE_INFO";

    private Button saveDriverContactButton;
    private Button callDriverButton;
    private Button saveDriverDetailsButton;

    private TextView driverNameTextView;
    private TextView driverPhoneTextView;
    private TextView driverLocalityTextView;

    private HashMap<String, Object> driverDatailsMap;

    private String driverName;
    private String driverPhone;
    private String driverLocality;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_details);

        Intent intent = getIntent();
        driverDatailsMap = (HashMap<String, Object>)intent.getSerializableExtra(DRIVER_PROFILE_INFO);

        driverNameTextView = findViewById(R.id.driverNameTextView);
        driverPhoneTextView = findViewById(R.id.driverPhoneTextView);
        driverLocalityTextView = findViewById(R.id.driverLocalityTextView);

        callDriverButton = findViewById(R.id.callDriverButton);
        saveDriverContactButton = findViewById(R.id.saveDriverContactButton);
        saveDriverDetailsButton = findViewById(R.id.saveDataButton);

        fetchDriverProfile(driverDatailsMap);
        displayDriverProfile();

        callDriverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+driverPhone));
                startActivity(intent);
            }
        });

        saveDriverContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContactsContract.Intents.SHOW_OR_CREATE_CONTACT,Uri.parse("tel:" + driverPhone));
                intent.putExtra(ContactsContract.Intents.EXTRA_FORCE_CREATE, true);
                startActivity(intent);
            }
        });

    }

    private void displayDriverProfile(){
        driverNameTextView.setText(driverName);
        driverPhoneTextView.setText(driverPhone);
        driverLocalityTextView.setText(driverLocality);
    }

    //Function to fetch driver details
    private void fetchDriverProfile(HashMap driverDatailsMap){
        driverName = getvalue(driverDatailsMap,DRIVER_NAME);
        driverPhone = getvalue(driverDatailsMap,"PHONE_NUMBER");
        driverLocality = getvalue(driverDatailsMap,DRIVER_LOCALITY);
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
