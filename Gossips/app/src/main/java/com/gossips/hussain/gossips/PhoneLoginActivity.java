package com.gossips.hussain.gossips;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {



    private Button sendVerificationCodeButton,verifyButton;
    private EditText inputPhoneNumber,inputVerificationCode;
    private ProgressDialog progressDialog;


    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;


    private String mVerificationId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);


        mAuth=FirebaseAuth.getInstance();

        initViews();


        sendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {






                String phoneNumber=inputPhoneNumber.getText().toString();

                if(TextUtils.isEmpty(phoneNumber)){


                    Toast.makeText(PhoneLoginActivity.this, "Please Enter Phone Number", Toast.LENGTH_LONG).show();

                }

                else {


                    progressDialog.setTitle("Phone Verification");
                    progressDialog.setMessage("Please Wait...");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    sendVerifyCode(phoneNumber);

                }





            }
        });


        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                sendVerificationCodeButton.setVisibility(View.GONE);
                inputPhoneNumber.setVisibility(View.GONE);

                String verificationCode=inputVerificationCode.getText().toString();
                if(TextUtils.isEmpty(verificationCode)){

                    Toast.makeText(PhoneLoginActivity.this, "Please enter OTP", Toast.LENGTH_SHORT).show();
                    
                }

                else {

                    progressDialog.setTitle("OTP verification");
                    progressDialog.setMessage("Please Wait...");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);



                }


            }
        });


        onVerificationChangedState();



    }



    private void onVerificationChangedState() {


        callbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {


                signInWithPhoneAuthCredential(phoneAuthCredential);


            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                progressDialog.dismiss();

                Toast.makeText(PhoneLoginActivity.this, "Invalid phone number, please enter correct phone numeber with the country code", Toast.LENGTH_LONG).show();


                sendVerificationCodeButton.setVisibility(View.VISIBLE);
                inputPhoneNumber.setVisibility(View.VISIBLE);

                verifyButton.setVisibility(View.GONE);
                inputVerificationCode.setVisibility(View.GONE);


            }


            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.

                progressDialog.dismiss();

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                Toast.makeText(PhoneLoginActivity.this, "Code has been sent to the above phone number, please check and verify", Toast.LENGTH_LONG).show();

                sendVerificationCodeButton.setVisibility(View.GONE);
                inputPhoneNumber.setVisibility(View.GONE);

                verifyButton.setVisibility(View.VISIBLE);
                inputVerificationCode.setVisibility(View.VISIBLE);


            }
        };


    }


    private void sendVerifyCode(String phoneNumber) {



        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,      // Phone number to verify
                60,               // Timeout duration
                TimeUnit.SECONDS, // Unit of timeout
                this,             // Activity (for callback binding)
                callbacks); // OnVerificationStateChangedCallbacks



    }




    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            progressDialog.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Logged in Successfully", Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();

                        } else {


                            // Sign in failed, display a message and update the UI
                            String message=task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Error:\n"+message, Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }





    private void initViews() {


        sendVerificationCodeButton=(Button)findViewById(R.id.send_ver_code_button);
        verifyButton=(Button)findViewById(R.id.verify_button);
        inputPhoneNumber=(EditText)findViewById(R.id.phone_number_input);
        inputVerificationCode=(EditText)findViewById(R.id.verification_code_input);
        progressDialog=new ProgressDialog(this);


    }


    private void sendUserToMainActivity() {

        Intent loginIntent=new Intent(PhoneLoginActivity.this,MainActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
}
