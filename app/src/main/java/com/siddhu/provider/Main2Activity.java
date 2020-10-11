package com.siddhu.provider;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String user = mAuth.getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Drviers").child(user).child("profile");


        Map<String,String> driverInfo = new HashMap();

        driverInfo.put("phone","9769037262");
        driverInfo.put("name","siddhu salvi");
        driverInfo.put("trucktype","2");
        driverInfo.put("locality","Tilak aali");
        ref.setValue(driverInfo);
        Toast.makeText(getApplicationContext(),"saved the data",Toast.LENGTH_SHORT).show();
    }
}
