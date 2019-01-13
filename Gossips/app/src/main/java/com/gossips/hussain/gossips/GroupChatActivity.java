package com.gossips.hussain.gossips;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {



    private final static String GROUPS_NAME="groupname";
    private final static String USERS="Users";
    private final static String NAME_KEY="name";
    private final static String GROUPS="Groups";
    private final static String MESSAGE="message";
    private final static String DATE="date";
    private final static String TIME="time";






    private Toolbar mToolBar;
    private ImageButton sendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessages;



    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, groupNameRef, groupMessageKeyRef;



    private String currentGroupName,currentUserID,currentUserName,currentDate,currentTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);


        currentGroupName=getIntent().getExtras().get(GROUPS_NAME).toString();
        Toast.makeText(this, currentGroupName, Toast.LENGTH_SHORT).show();


        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        usersRef=FirebaseDatabase.getInstance().getReference().child(USERS);
        groupNameRef=FirebaseDatabase.getInstance().getReference().child(GROUPS).child(currentGroupName);








        initViews();


        getUserInfo();


        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                saveMessageInfoToDatabase();

                userMessageInput.setText("");

                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);



            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();


        groupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {


                if(dataSnapshot.exists()){


                    displayMessages(dataSnapshot);

                }


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if(dataSnapshot.exists()){


                    displayMessages(dataSnapshot);

                }


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

    private void displayMessages(DataSnapshot dataSnapshot) {


        Iterator iterator=dataSnapshot.getChildren().iterator();

        while (iterator.hasNext()){

            String chatDate=(String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage=(String) ((DataSnapshot)iterator.next()).getValue();
            String chatName=(String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime=(String) ((DataSnapshot)iterator.next()).getValue();


            displayTextMessages.append(chatName+":\n"+chatMessage+"\n"+chatDate+"   "+chatTime+"\n\n");

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);



        }


    }

    private void saveMessageInfoToDatabase() {



        String message=userMessageInput.getText().toString();
        String messageKEY=groupNameRef.push().getKey();

        if(TextUtils.isEmpty(message)){

            Toast.makeText(this, "Cannot send blank message", Toast.LENGTH_SHORT).show();
        }
        else {


            //FOR DATE
            Calendar calForDate = Calendar.getInstance();

            SimpleDateFormat currentDateFormat=new SimpleDateFormat("MMM dd, yyyy");
            currentDate=currentDateFormat.format(calForDate.getTime());




            //FOR TIME
            Calendar calForTime = Calendar.getInstance();

            SimpleDateFormat currentTimeFormat=new SimpleDateFormat("hh:mm a");
            currentTime=currentTimeFormat.format(calForTime.getTime());


            HashMap<String,Object> groupMessageKey=new HashMap<>();
            groupNameRef.updateChildren(groupMessageKey);

            groupMessageKeyRef=groupNameRef.child(messageKEY);

            HashMap<String,Object> messageInfoMap=new HashMap<>();
            messageInfoMap.put(NAME_KEY,currentUserName);
            messageInfoMap.put(MESSAGE,message);
            messageInfoMap.put(DATE,currentDate);
            messageInfoMap.put(TIME,currentTime);

            groupMessageKeyRef.updateChildren(messageInfoMap);



        }



    }


    private void getUserInfo() {





        usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                if(dataSnapshot.exists()){

                    currentUserName=dataSnapshot.child(NAME_KEY).getValue().toString();


                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });






    }

    private void initViews() {


        mToolBar=(Toolbar)findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle(currentGroupName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        sendMessageButton=(ImageButton) findViewById(R.id.send_message_button);
        userMessageInput=(EditText)findViewById(R.id.input_group_message);
        mScrollView=(ScrollView)findViewById(R.id.my_scroll_view);
        displayTextMessages=(TextView)findViewById(R.id.group_chat_text_display);

    }
}
