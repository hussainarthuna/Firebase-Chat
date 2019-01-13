package com.gossips.hussain.gossips;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {




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


    private View requestFragmentView;
    private RecyclerView myRequestList;


    private DatabaseReference chatRequestRef,usersRef,contactsRef;
    private FirebaseAuth mAuth;


    private String currentUserID;


    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestFragmentView= inflater.inflate(R.layout.fragment_requests, container, false);

        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();

        chatRequestRef=FirebaseDatabase.getInstance().getReference().child(CHAT_REQUESTS);
        usersRef=FirebaseDatabase.getInstance().getReference().child(USERS);
        contactsRef=FirebaseDatabase.getInstance().getReference().child(CONTACTS);


        myRequestList=(RecyclerView)requestFragmentView.findViewById(R.id.chat_requests_list);
        myRequestList.setLayoutManager(new LinearLayoutManager(getContext()));



        return requestFragmentView;

    }


    @Override
    public void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<Contacts> options
                =new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatRequestRef.child(currentUserID),Contacts.class)
                .build();


        FirebaseRecyclerAdapter<Contacts,RequestViewHolder> adapter
                = new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull Contacts model) {



                holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);
                holder.acceptButton.setEnabled(true);
                holder.cancelButton.setEnabled(true);



                final String list_user_id=getRef(position).getKey();


                DatabaseReference getTypeRef=getRef(position).child(REQUEST_TYPE).getRef();
                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                        if(dataSnapshot.exists()){


                            String type=dataSnapshot.getValue().toString();

                            if(type.equals(RECEIVED)){


                                usersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if(dataSnapshot.hasChild(IMAGE_KEY)){

                                            final String requestProfileImage=dataSnapshot.child(IMAGE_KEY).getValue().toString();


                                            Picasso.get().load(requestProfileImage).placeholder(R.drawable.profile_image).into(holder.profileImage);

                                        }


                                            final String requestUsername=dataSnapshot.child(NAME_KEY).getValue().toString();
                                            final String requestStatus=dataSnapshot.child(STATUS_KEY).getValue().toString();


                                            holder.userName.setText(requestUsername);
                                            holder.userStatus.setText("Wants to connect with you");


                                            holder.acceptButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    acceptChatRequest(list_user_id);

                                                }
                                            });


                                            holder.cancelButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    cancelChatRequest(list_user_id);
                                                }
                                            });








                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }

                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                RequestViewHolder viewHolder=new RequestViewHolder(view);
                return viewHolder;
            }
        };


        myRequestList.setAdapter(adapter);
        adapter.startListening();

    }


    private void acceptChatRequest(final String list_user_id) {




        contactsRef.child(currentUserID).child(list_user_id)
                .child(CONTACTS).setValue(SAVED)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {


                        if (task.isSuccessful()){

                            contactsRef.child(list_user_id).child(currentUserID)
                                    .child(CONTACTS).setValue(SAVED)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {


                                            if(task.isSuccessful()){

                                                chatRequestRef.child(currentUserID).child(list_user_id)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {


                                                                if(task.isSuccessful()){

                                                                    chatRequestRef.child(list_user_id).child(currentUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    if(task.isSuccessful()){


                                                                                        Toast.makeText(getContext(), "Contact Added", Toast.LENGTH_SHORT).show();
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




    private void cancelChatRequest(final String list_user_id) {


        chatRequestRef.child(currentUserID).child(list_user_id)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {


                        if(task.isSuccessful()){

                            chatRequestRef.child(list_user_id).child(currentUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {


                                            if(task.isSuccessful()){

                                                Toast.makeText(getContext(), "Request Cancelled", Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    });
                        }


                    }
                });

    }




    public static class RequestViewHolder extends RecyclerView.ViewHolder{



        TextView userName,userStatus;
        CircleImageView profileImage;
        Button acceptButton,cancelButton;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);


            userName=(TextView)itemView.findViewById(R.id.user_profile_name);
            userStatus=(TextView)itemView.findViewById(R.id.user_profile_status);
            profileImage=(CircleImageView)itemView.findViewById(R.id.user_profile_image);
            acceptButton=(Button)itemView.findViewById(R.id.request_accept_btn);
            cancelButton=(Button)itemView.findViewById(R.id.request_cancel_btn);


        }
    }
}
