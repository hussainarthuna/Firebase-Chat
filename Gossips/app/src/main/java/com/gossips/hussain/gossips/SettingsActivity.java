package com.gossips.hussain.gossips;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private final static String UID_KEY="uid";
    private final static String NAME_KEY="name";
    private final static String STATUS_KEY="status";
    private final static String IMAGE_KEY="image";
    private final static String USERS="Users";
    private final static String PROFILE_IMAGES="Profile Images";
    private final static int GALLERY_PICK=1;



    private Button updateAccountSettings;
    private EditText userName,userStatus;
    private CircleImageView userProfileImage;
    private ProgressDialog progressDialog;
    private Toolbar mToolBar;


    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private StorageReference userProfileImagesRef;



    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);



        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        rootRef=FirebaseDatabase.getInstance().getReference();
        userProfileImagesRef=FirebaseStorage.getInstance().getReference().child(PROFILE_IMAGES);

        initViews();
        userName.setVisibility(View.INVISIBLE);

        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSettings();
            }
        });


        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                sendtoGallaryIntent();


            }
        });



        retrieveUserInfo();
    }






    private void sendtoGallaryIntent() {



        Intent galleryIntent=new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,GALLERY_PICK);


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode==GALLERY_PICK && resultCode==RESULT_OK && data!=null){


            Uri imageUri=data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);


        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode==RESULT_OK){



                progressDialog.setTitle("Updating Profile Image");
                progressDialog.setMessage("Please Wait...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();


                //this resultUri will contain cropped image
                Uri resultUri=result.getUri();

                StorageReference filePath=userProfileImagesRef.child(currentUserID+".jpg");

                filePath.putFile(resultUri)
                        .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {


                                if(task.isSuccessful()){

                                    Toast.makeText(SettingsActivity.this, "Profile Image Uploaded Successfully", Toast.LENGTH_SHORT).show();



                                    final String downloadUrl=task.getResult().getDownloadUrl().toString();

                                    rootRef.child(USERS).child(currentUserID).child(IMAGE_KEY)
                                            .setValue(downloadUrl)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {


                                                    if (task.isSuccessful()){

                                                        Toast.makeText(SettingsActivity.this, "Image Saved in database successfully", Toast.LENGTH_SHORT).show();
                                                        progressDialog.dismiss();
                                                    }

                                                    else {

                                                        String message=task.getException().toString();
                                                        Toast.makeText(SettingsActivity.this, "Error:\n"+message, Toast.LENGTH_LONG).show();

                                                        progressDialog.dismiss();
                                                    }




                                                }
                                            });



                                }

                                else {

                                    String meesage=task.getException().toString();
                                    Toast.makeText(SettingsActivity.this, "Error:\n"+meesage, Toast.LENGTH_LONG).show();

                                    progressDialog.dismiss();
                                }

                            }
                        });


            }
        }



    }

    private void retrieveUserInfo() {




        rootRef.child(USERS).child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild(NAME_KEY)) && (dataSnapshot.hasChild(IMAGE_KEY))){



                            String retrieveUsername=dataSnapshot.child(NAME_KEY).getValue().toString();
                            String retrieveStatus=dataSnapshot.child(STATUS_KEY).getValue().toString();
                            String retrieveProfileImage=dataSnapshot.child(IMAGE_KEY).getValue().toString();


                            userName.setText(retrieveUsername);
                            userStatus.setText(retrieveStatus);
                            Picasso.get().load(retrieveProfileImage).placeholder(R.drawable.profile_image).into(userProfileImage);

                        }

                        else if((dataSnapshot.exists()) && (dataSnapshot.hasChild(NAME_KEY))){


                            String retrieveUsername=dataSnapshot.child(NAME_KEY).getValue().toString();
                            String retrieveStatus=dataSnapshot.child(STATUS_KEY).getValue().toString();


                            userName.setText(retrieveUsername);
                            userStatus.setText(retrieveStatus);

                        }

                        else {


                            userName.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingsActivity.this, "Please set and update your Profile", Toast.LENGTH_LONG).show();
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



    }

    private void updateSettings() {


        String setUserName=userName.getText().toString();
        String setUserStatus=userStatus.getText().toString();

        if(TextUtils.isEmpty(setUserName)){

            Toast.makeText(this, "Please Enter Username", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setUserStatus)){

            Toast.makeText(this, "Please Enter Your Status", Toast.LENGTH_SHORT).show();
        }
        else {

            HashMap<String,Object> profileMap=new HashMap<>();
            profileMap.put(UID_KEY,currentUserID);
            profileMap.put(NAME_KEY,setUserName);
            profileMap.put(STATUS_KEY,setUserStatus);


            rootRef.child(USERS).child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {


                            if(task.isSuccessful()) {

                                sendUserToMainActivity();

                                Toast.makeText(SettingsActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();


                            }
                            else {


                                String message=task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();

                            }
                        }
                    });


        }



    }

    private void initViews() {


        updateAccountSettings=(Button)findViewById(R.id.update_settings_button);
        userName=(EditText)findViewById(R.id.set_user_name);
        userStatus=(EditText)findViewById(R.id.set_profile_status);
        userProfileImage=(CircleImageView)findViewById(R.id.set_profile_image);
        progressDialog=new ProgressDialog(this);

        mToolBar=(Toolbar)findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Settings");
    }


    private void sendUserToMainActivity() {

        Intent mainIntent=new Intent(SettingsActivity.this,MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
