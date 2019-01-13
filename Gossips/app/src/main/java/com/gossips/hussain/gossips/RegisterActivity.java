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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {


    private final static String USERS="Users";




    private Button createAccountBtn;
    private EditText userEmail,userPassword;
    private TextView alreadyHaveAnAccountLink;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private DatabaseReference rootRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initViews();

        mAuth=FirebaseAuth.getInstance();
        rootRef=FirebaseDatabase.getInstance().getReference();

        alreadyHaveAnAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendUserToLoginActivity();
            }
        });


        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                createNewAccount();
            }
        });
    }

    private void createNewAccount() {



        String email= userEmail.getText().toString();
        String password=userPassword.getText().toString();

        if(TextUtils.isEmpty(email)){

            Toast.makeText(this, "Please Enter your Email...", Toast.LENGTH_LONG).show();
        }
        if(TextUtils.isEmpty(password)){

            Toast.makeText(this, "Please Enter Password...", Toast.LENGTH_LONG).show();
        }

        else {


            progressDialog.setTitle("Creating New Account");
            progressDialog.setMessage("Please Wait...");
            progressDialog.show();


            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {


                    if(task.isSuccessful()){

                        storeUserIDtoFirebaseDatabase();

                        Toast.makeText(RegisterActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();

                        progressDialog.dismiss();
                        sendUserToMainActivity();
                    }

                    else {


                        String message=task.getException().toString();
                        Toast.makeText(RegisterActivity.this,"Error: "+message, Toast.LENGTH_SHORT).show();

                        progressDialog.dismiss();
                    }



                }
            });

        }


    }

    private void storeUserIDtoFirebaseDatabase() {



        String currentUserID=mAuth.getCurrentUser().getUid();
        rootRef.child(USERS).child(currentUserID).setValue("");


    }


    private void initViews() {

        createAccountBtn=(Button)findViewById(R.id.register_button);
        userEmail=(EditText)findViewById(R.id.register_email);
        userPassword=(EditText)findViewById(R.id.register_password);
        alreadyHaveAnAccountLink=(TextView)findViewById(R.id.already_have_account_link);
        progressDialog=new ProgressDialog(this);
    }

    private void sendUserToLoginActivity() {

        Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);

        startActivity(intent);
    }

    private void sendUserToMainActivity() {

        Intent loginIntent=new Intent(RegisterActivity.this,MainActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
}
