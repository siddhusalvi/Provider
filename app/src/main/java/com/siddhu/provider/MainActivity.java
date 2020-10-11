package com.siddhu.provider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    public static final String providerPrefrences = "ProviderApp";
    public static final String DRIVER_NAME = "DRIVER_NAME";
    private static final String TAG = "TaG";
    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;
    boolean hasForegroundLocationPermission;
    boolean hasBackgroundLocationPermission;
    private EditText mPhoneNumber, mOTP;
    private Button mOTPRequest, mSignIn;
    private FirebaseAuth mAuth;
    private String phoneNumber;
    private String countryCode = "+91";
    private String otpCodeSent;
    private String enteredCode;
    private int timeout = 120;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedpreferences = getSharedPreferences(providerPrefrences, Context.MODE_PRIVATE);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else if (!sharedpreferences.contains(DRIVER_NAME)) {
            startActivity(new Intent(MainActivity.this, MandatoryDetailsActivity.class));
            finish();
        } else {
            startActivity(new Intent(this, WorkActivity.class));
            finish();
        }
    }

    private void showMsg(String msg){
        //Snackbar.make(findViewById(android.R.id.content).getRootView(),msg, BaseTransientBottomBar.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }


}