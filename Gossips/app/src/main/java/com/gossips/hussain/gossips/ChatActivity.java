package com.gossips.hussain.gossips;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity{


        private final static String VISIT_USER_ID="visit_user_id";
        private final static String VISIT_USER_NAME="visit_user_name";
        private final static String VISIT_USER_IMAGE="visit_user_image";

        private final static String NAME_KEY="name";
        private final static String MESSAGES="Messages";
        private final static String MESSAGE="message";
        private final static String TYPE="type";
        private final static String FROM="from";
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



        private FirebaseAuth mAuth;
        private DatabaseReference rootRef;



        private String messageReceiverID,messageReceiverUserName,messageReceiverUserImage,messageSenderID;


        private final List<Messages> messagesList=new ArrayList<>();
        private LinearLayoutManager linearLayoutManager;
        private MessageAdapter messageAdapter;



        private TextView userName,userLastSeen;
        private CircleImageView userImage;
        private Toolbar chatToolBar;
        private ImageButton sendMessageButton;
        private EditText messageInputText;
        private RecyclerView userMessagesList;
        private RelativeLayout relativeLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth=FirebaseAuth.getInstance();
        messageSenderID=mAuth.getCurrentUser().getUid();
        rootRef=FirebaseDatabase.getInstance().getReference();


        messageReceiverID=getIntent().getExtras().get(VISIT_USER_ID).toString();
        messageReceiverUserName=getIntent().getExtras().get(VISIT_USER_NAME).toString();
        messageReceiverUserImage=getIntent().getExtras().get(VISIT_USER_IMAGE).toString();
       // Toast.makeText(this, messageReceiverID+"\n"+messageReceiverUserName, Toast.LENGTH_SHORT).show();


        initViews();

        userName.setText(messageReceiverUserName);
        Picasso.get().load(messageReceiverUserImage).placeholder(R.drawable.profile_image).into(userImage);


        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent profileIntent=new Intent(ChatActivity.this,ProfileActivity.class);
                profileIntent.putExtra(VISIT_USER_ID,messageReceiverID);
                startActivity(profileIntent);

            }
        });


        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessage();
            }
        });



    }





    @Override
    protected void onStart() {
        super.onStart();


        rootRef.child(MESSAGES).child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {


                        Messages messages=dataSnapshot.getValue(Messages.class);


                        messagesList.add(messages);
                        messageAdapter.notifyDataSetChanged();

                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());



                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });




    }






    private void sendMessage(){



        String messageText=messageInputText.getText().toString();

        if(TextUtils.isEmpty(messageText)){

            Toast.makeText(this, "Please write a message", Toast.LENGTH_SHORT).show();
        }
        else {



            String messageSenderRef="Messages/"+messageSenderID+"/"+messageReceiverID;
            String messageReceiverRef="Messages/"+messageReceiverID+"/"+messageSenderID;

            DatabaseReference userMessageKeyRef=rootRef.child(MESSAGES)
                    .child(messageSenderID).child(messageReceiverID).push();

            String messagePushID=userMessageKeyRef.getKey();

            Map messageTextBody=new HashMap();
            messageTextBody.put(MESSAGE,messageText);
            messageTextBody.put(TYPE,"text");
            messageTextBody.put(FROM,messageSenderID);


            Map messageBodyDetails=new HashMap();
            messageBodyDetails.put(messageSenderRef+"/"+messagePushID,messageTextBody);
            messageBodyDetails.put(messageReceiverRef+"/"+messagePushID,messageTextBody);

            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {


                    if(task.isSuccessful()){

                        Toast.makeText(ChatActivity.this, "Message Send Successfully", Toast.LENGTH_SHORT).show();

                    }
                    else{


                        Toast.makeText(ChatActivity.this, "ERROR", Toast.LENGTH_SHORT).show();


                    }

                    messageInputText.setText("");


                }
            });


        }



    }



    private void initViews() {


            chatToolBar=(Toolbar)findViewById(R.id.chat_toolbar);

            setSupportActionBar(chatToolBar);
            ActionBar actionBar=getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);

        userImage=(CircleImageView)findViewById(R.id.custom_profile_image);
        userName=(TextView)findViewById(R.id.custom_profile_name);
        userLastSeen=(TextView)findViewById(R.id.custom_user_lastSeen);
        relativeLayout=(RelativeLayout)findViewById(R.id.custom_layout_profile);
        sendMessageButton=(ImageButton) findViewById(R.id.send_message_btn);
        messageInputText=(EditText)findViewById(R.id.input_message);


        messageAdapter=new MessageAdapter(messagesList);

        userMessagesList=(RecyclerView)findViewById(R.id.private_messages_list_of_users);
        linearLayoutManager=new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);




    }
}
