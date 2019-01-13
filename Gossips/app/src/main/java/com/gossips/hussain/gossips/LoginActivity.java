package com.gossips.hussain.gossips;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {


    private Button loginBtn,phoneLoginBtn;
    private EditText userEmail,userPassword;
    private TextView needNewAccountLink,forgetPasswordLink;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth=FirebaseAuth.getInstance();


        initViews();
        needNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendUserToRegisterActivity();
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                allowUserToLogin();
            }
        });

        phoneLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToPhoneLoginActivity();
            }
        });
    }

    private void allowUserToLogin() {



        String email= userEmail.getText().toString();
        String password=userPassword.getText().toString();

        if(TextUtils.isEmpty(email)){

            Toast.makeText(this, "Please Enter your Email...", Toast.LENGTH_LONG).show();
        }
        if(TextUtils.isEmpty(password)){

            Toast.makeText(this, "Please Enter Password...", Toast.LENGTH_LONG).show();
        }

        else{


            progressDialog.setTitle("Logging In");
            progressDialog.setMessage("Please Wait...");
            progressDialog.show();



            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {


                    if(task.isSuccessful()){


                        sendUserToMainActivity();
                        Toast.makeText(LoginActivity.this, "Logged In Successfull", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                    else {


                        String message=task.getException().toString();
                        Toast.makeText(LoginActivity.this,"Error: "+message, Toast.LENGTH_SHORT).show();

                        progressDialog.dismiss();
                    }



                }
            });


        }


    }

    private void initViews() {

        loginBtn=(Button)findViewById(R.id.login_button);
        phoneLoginBtn=(Button)findViewById(R.id.phone_login_button);
        userEmail=(EditText)findViewById(R.id.login_email);
        userPassword=(EditText)findViewById(R.id.login_password);
        needNewAccountLink=(TextView)findViewById(R.id.need_new_account_link);
        forgetPasswordLink=(TextView)findViewById(R.id.forget_password_link);
        progressDialog=new ProgressDialog(this);

    }




    private void sendUserToMainActivity() {

        Intent loginIntent=new Intent(LoginActivity.this,MainActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
    private void sendUserToRegisterActivity() {

        Intent regIntent=new Intent(LoginActivity.this,RegisterActivity.class);

        startActivity(regIntent);
    }
    private void sendUserToPhoneLoginActivity() {

        Intent phoneIntent=new Intent(LoginActivity.this,PhoneLoginActivity.class);

        startActivity(phoneIntent);
    }

}
