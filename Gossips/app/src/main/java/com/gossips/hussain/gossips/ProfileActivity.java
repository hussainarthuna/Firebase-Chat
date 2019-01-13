package com.gossips.hussain.gossips;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {


    private final static String VISIT_USER_ID="visit_user_id";
    private final static String NAME_KEY="name";
    private final static String SAVED="saved";
    private final static String STATUS_KEY="status";
    private final static String IMAGE_KEY="image";
    private final static String USERS="Users";
    private final static String CONTACTS="Contacts";
    private final static String CHAT_REQUESTS="Chat Requests";
    private final static String REQUEST_TYPE="request_type";
    private final static String SENT="sent";
    private final static String REQUEST_SENT="request_sent";
    private final static String REQUEST_RECEIVED="request_received";
    private final static String RECEIVED="received";
    private final static String NEW="new";
    private final static String FRIENDS="friends";



    private DatabaseReference userRef,chatRequestRef,contactsRef;
    private FirebaseAuth mAuth;



    private String receiverUserID,senderUserID,current_state;


    private CircleImageView userProfileImage;
    private TextView userProfileName,userProfileStatus;
    private Button sendMessageRequestButton,declineRequestButton;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userRef=FirebaseDatabase.getInstance().getReference().child(USERS);
        chatRequestRef=FirebaseDatabase.getInstance().getReference().child(CHAT_REQUESTS);
        contactsRef=FirebaseDatabase.getInstance().getReference().child(CONTACTS);
        mAuth=FirebaseAuth.getInstance();

        receiverUserID=getIntent().getExtras().get(VISIT_USER_ID).toString();
        senderUserID=mAuth.getCurrentUser().getUid();

        //Toast.makeText(this, receiverUserID, Toast.LENGTH_SHORT).show();

        initViews();
        current_state=NEW;

        progressDialog.setTitle("Loading...");
        progressDialog.show();
        retrieveUserInfo();
        progressDialog.dismiss();
    }




    private void retrieveUserInfo() {


        userRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                if((dataSnapshot.exists()) && (dataSnapshot.hasChild(IMAGE_KEY))){


                    String userImage=dataSnapshot.child(IMAGE_KEY).getValue().toString();
                    String userName=dataSnapshot.child(NAME_KEY).getValue().toString();
                    String userStatus=dataSnapshot.child(STATUS_KEY).getValue().toString();


                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    manageChatRequests();

                }


                else {

                    String userName=dataSnapshot.child(NAME_KEY).getValue().toString();
                    String userStatus=dataSnapshot.child(STATUS_KEY).getValue().toString();
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                    manageChatRequests();

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }






    private void manageChatRequests() {



        chatRequestRef.child(senderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                        if(dataSnapshot.hasChild(receiverUserID)){

                            String request_type=dataSnapshot.child(receiverUserID).child(REQUEST_TYPE).getValue().toString();

                            if(request_type.equals(SENT)){

                                current_state=REQUEST_SENT;
                                sendMessageRequestButton.setText("Cancel Request");

                            }

                            else if(request_type.equals(RECEIVED)){

                                current_state=REQUEST_RECEIVED;
                                sendMessageRequestButton.setText("Accept Chat Request");

                                declineRequestButton.setVisibility(View.VISIBLE);
                                declineRequestButton.setEnabled(true);
                                declineRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        cancelChatRequest();
                                    }
                                });

                            }

                           /* else {

                                sendMessageRequestButton.setText("Send message");
                                declineRequestButton.setVisibility(View.INVISIBLE);
                                declineRequestButton.setEnabled(false);
                                current_state=NEW;

                            }

                            */
                        }


                        else {

                            contactsRef.child(senderUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                                            if(dataSnapshot.hasChild(receiverUserID)){


                                                current_state=FRIENDS;
                                                sendMessageRequestButton.setText("Remove contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });


                        }




                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


        if(!senderUserID.equals(receiverUserID)){


            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    sendMessageRequestButton.setEnabled(false);

                    if (current_state.equals(NEW)){

                        sendChatRequest();
                    }

                    if (current_state.equals(REQUEST_SENT)){

                        cancelChatRequest();
                    }
                    if(current_state.equals(REQUEST_RECEIVED)){

                        acceptChatRequest();
                    }
                    if(current_state.equals(FRIENDS)){

                        removeSpecificContact();
                    }


                }
            });


        }

        else {

            sendMessageRequestButton.setVisibility(View.INVISIBLE);
        }



    }

    private void removeSpecificContact() {


        contactsRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {


                        if(task.isSuccessful()){

                            contactsRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {


                                            if(task.isSuccessful()){

                                                sendMessageRequestButton.setEnabled(true);
                                                current_state=NEW;
                                                sendMessageRequestButton.setText("Send Message");
                                                declineRequestButton.setVisibility(View.INVISIBLE);
                                                declineRequestButton.setEnabled(false);
                                            }

                                        }
                                    });
                        }


                    }
                });




    }


    private void acceptChatRequest() {




        contactsRef.child(senderUserID).child(receiverUserID)
                .child(CONTACTS).setValue(SAVED)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {


                        if (task.isSuccessful()){

                            contactsRef.child(receiverUserID).child(senderUserID)
                                    .child(CONTACTS).setValue(SAVED)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {


                                            if(task.isSuccessful()){

                                                chatRequestRef.child(senderUserID).child(receiverUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {


                                                                if(task.isSuccessful()){

                                                                    chatRequestRef.child(receiverUserID).child(senderUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    if(task.isSuccessful()){


                                                                                        sendMessageRequestButton.setEnabled(true);
                                                                                        current_state=FRIENDS;
                                                                                        sendMessageRequestButton.setText("Remove contact");


                                                                                        declineRequestButton.setVisibility(View.INVISIBLE);
                                                                                        declineRequestButton.setEnabled(false);
                                                                                    }

                                                                                }
                                                                            });
                                                                }


                                                            }
                                                        });
                                            }



                                        }
                                    });
                        }


                    }
                });




    }





    private void cancelChatRequest() {


        chatRequestRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {


                        if(task.isSuccessful()){

                            chatRequestRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {


                                           if(task.isSuccessful()){

                                               sendMessageRequestButton.setEnabled(true);
                                               current_state=NEW;
                                               sendMessageRequestButton.setText("Send Message");
                                               declineRequestButton.setVisibility(View.INVISIBLE);
                                               declineRequestButton.setEnabled(false);
                                           }

                                        }
                                    });
                        }


                    }
                });

    }


    private void sendChatRequest() {



        chatRequestRef.child(senderUserID).child(receiverUserID)
                .child(REQUEST_TYPE).setValue(SENT)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {


                        if(task.isSuccessful()){


                            chatRequestRef.child(receiverUserID).child(senderUserID)
                                    .child(REQUEST_TYPE).setValue(RECEIVED)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {


                                            if(task.isSuccessful()){


                                                sendMessageRequestButton.setEnabled(true);
                                                current_state=REQUEST_SENT;
                                                sendMessageRequestButton.setText("Cancel Request");


                                            }


                                        }
                                    });


                        }



                    }
                });


    }





    private void initViews() {

        userProfileImage=(CircleImageView)findViewById(R.id.visit_profile_image);
        userProfileName=(TextView)findViewById(R.id.visit_user_name);
        userProfileStatus=(TextView)findViewById(R.id.visit_profile_status);
        sendMessageRequestButton=(Button)findViewById(R.id.send_message_request_button);
        declineRequestButton=(Button)findViewById(R.id.decline_message_request_button);
        progressDialog=new ProgressDialog(this);

    }
}
