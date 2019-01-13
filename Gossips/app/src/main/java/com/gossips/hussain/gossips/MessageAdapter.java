package com.gossips.hussain.gossips;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

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
    private DatabaseReference usersRef;


    private List<Messages> userMessagesList;


    public MessageAdapter(List<Messages> userMessagesList){

        this.userMessagesList=userMessagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {


        View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_messages_layout,viewGroup,false);

        mAuth=FirebaseAuth.getInstance();


        return new  MessageViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i) {



        String messageSenderID=mAuth.getCurrentUser().getUid();
        Messages messages=userMessagesList.get(i);
        String fromUserID=messages.getFrom();
        String fromMessageType=messages.getType();

        usersRef=FirebaseDatabase.getInstance().getReference().child(USERS).child(fromUserID);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                if(dataSnapshot.hasChild(IMAGE_KEY)){


                    String receiverImage=dataSnapshot.child(IMAGE_KEY).getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(messageViewHolder.receiverProfileImage);



                }





            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




        if(fromMessageType.equals("text")){


            messageViewHolder.receiverMessageText.setVisibility(View.INVISIBLE);
            messageViewHolder.receiverProfileImage.setVisibility(View.INVISIBLE);
            messageViewHolder.senderMessageText.setVisibility(View.INVISIBLE);




            if(fromUserID.equals(messageSenderID)){


                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);

                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                messageViewHolder.senderMessageText.setText(messages.getMessage());

            }

            else {

                messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);


                messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                messageViewHolder.receiverMessageText.setText(messages.getMessage());

            }



        }




    }





    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder{

        CircleImageView receiverProfileImage;
        TextView senderMessageText,receiverMessageText;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);


            senderMessageText=(TextView)itemView.findViewById(R.id.sender_message_text);
            receiverMessageText=(TextView)itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage=(CircleImageView)itemView.findViewById(R.id.message_profile_image);


        }
    }

}
