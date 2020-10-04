package com.siddhu.provider;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedpreferences;

    SharedPreferences.Editor editor;

    public static final String providerPrefrences = "ProviderApp";
    public static final String DRIVER_NAME = "DRIVER_NAME";

    private static final String TAG = "TaG";
    private EditText mPhoneNumber,mOTP;
    private Button mOTPRequest,mSignIn;

    private FirebaseAuth mAuth;

    private String phoneNumber;
    private String countryCode = "+91";
    private String otpCodeSent;
    private String enteredCode;

    private int timeout = 120;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        sharedpreferences = getSharedPreferences(providerPrefrences, Context.MODE_PRIVATE);

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }

        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            startActivity(new Intent(this,LoginActivity.class));
            finish();
        }else if(!sharedpreferences.contains(DRIVER_NAME)){
            startActivity(new Intent(MainActivity.this,MandatoryDetailsActivity.class));
            finish();
        }else {
            startActivity(new Intent(this,WorkActivity.class));
            finish();
        }
    }
}