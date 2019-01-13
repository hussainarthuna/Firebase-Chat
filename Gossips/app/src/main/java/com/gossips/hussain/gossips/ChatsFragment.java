package com.gossips.hussain.gossips;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private final static String VISIT_USER_ID="visit_user_id";
    private final static String VISIT_USER_NAME="visit_user_name";
    private final static String VISIT_USER_IMAGE="visit_user_image";
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



    private String currentUserID;




    private View privateChatsView;
    private RecyclerView chatList;



    private DatabaseReference chatsRef,usersRef;
    private FirebaseAuth mAuth;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateChatsView= inflater.inflate(R.layout.fragment_chats, container, false);


        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        chatsRef=FirebaseDatabase.getInstance().getReference().child(CONTACTS).child(currentUserID);
        usersRef=FirebaseDatabase.getInstance().getReference().child(USERS);


        chatList=(RecyclerView)privateChatsView.findViewById(R.id.chats_list);
        chatList.setLayoutManager(new LinearLayoutManager(getContext()));


        return privateChatsView;

    }


    @Override
    public void onStart() {


        super.onStart();


        FirebaseRecyclerOptions<Contacts> options
                =new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatsRef,Contacts.class)
                .build();


        FirebaseRecyclerAdapter<Contacts,ChatsViewHolder> adapter
                =new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model) {



                final String usersID=getRef(position).getKey();
                final String[] retImage = {"defaul_image"};


                usersRef.child(usersID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                        if(dataSnapshot.exists()){


                            if(dataSnapshot.hasChild(IMAGE_KEY)){


                                retImage[0] =dataSnapshot.child(IMAGE_KEY).getValue().toString();

                                Picasso.get().load(retImage[0]).placeholder(R.drawable.profile_image).into(holder.profileImage);

                            }

                            final String retName=dataSnapshot.child(NAME_KEY).getValue().toString();
                            final String retStatus=dataSnapshot.child(STATUS_KEY).getValue().toString();

                            holder.userName.setText(retName);
                            holder.userStatus.setText("Last Seen: "+"\n"+"Date "+"Time");




                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {


                                    //String visit_user_id=getRef(position).getKey();

                                    Intent chatIntent=new Intent(getContext(),ChatActivity.class);
                                    chatIntent.putExtra(VISIT_USER_ID,usersID);
                                    chatIntent.putExtra(VISIT_USER_NAME,retName);
                                    chatIntent.putExtra(VISIT_USER_IMAGE, retImage[0]);
                                    startActivity(chatIntent);


                                }
                            });

                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });






            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {



                View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                ChatsViewHolder viewHolder=new ChatsViewHolder(view);
                return viewHolder;





            }
        };


        chatList.setAdapter(adapter);
        adapter.startListening();




    }



    public static class ChatsViewHolder extends RecyclerView.ViewHolder{


        CircleImageView profileImage;
        TextView userName,userStatus;



        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage=(CircleImageView)itemView.findViewById(R.id.user_profile_image);
            userName=(TextView)itemView.findViewById(R.id.user_profile_name);
            userStatus=(TextView)itemView.findViewById(R.id.user_profile_status);




        }
    }



}
