package com.siddhu.provider;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        mPhoneNumber = findViewById(R.id.phoneEditText);
        mOTP = findViewById(R.id.otpEditText);

        mOTPRequest = findViewById(R.id.getOtpButton);
        mSignIn = findViewById(R.id.signInButton);


        mOTPRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationCode();
            }
        });


        mSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifySignInCode();
            }
        });

    }

    private void sendVerificationCode() {
        if(phoneNumberIsValid()){
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNumber,        // Phone number to verify
                    timeout,                 // Timeout duration
                    TimeUnit.SECONDS,   // Unit of timeout
                    this,               // Activity (for callback binding)
                    mCallbacks);        // OnVerificationStateChangedCallbacks
            String msg = "OTP is sent to "+phoneNumber;
            showMsg(msg);
        }else{
            return;
        }
    }



    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Snackbar.make(getCurrentFocus(),e.getMessage(), BaseTransientBottomBar.LENGTH_SHORT).show();
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            otpCodeSent = s;
        }
    };

    private void verifySignInCode(){
        if(phoneNumberIsValid() && OTPIsValid()){
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(otpCodeSent, enteredCode );
            signInWithPhoneAuthCredential(credential);
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            String msg = "Sign in Successful :)";
                            showMsg(msg);

                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            showMsg(task.getException().getMessage());
                        }
                    }
                });
    }


    //Function to check phone number is valid
    private boolean phoneNumberIsValid(){
        phoneNumber = countryCode + mPhoneNumber.getText().toString();
        if(phoneNumber.length() == 13){
            return true;
        }else{
            String msg = "Please Enter Valid Phone Number!";
            showMsg(msg);
            return false;
        }
    }

    //Function to check OTP is valid
    private boolean OTPIsValid(){
        enteredCode = mOTP.getText().toString();
        if(enteredCode.length() == 6){
            return true;
        }else{

            String msg = "Please Enter Valid OTP!";
            showMsg(msg);
            return false;
        }
    }

    private void showMsg(String msg){
        Snackbar.make(findViewById(android.R.id.content).getRootView(),msg, BaseTransientBottomBar.LENGTH_SHORT).show();
    }

}
