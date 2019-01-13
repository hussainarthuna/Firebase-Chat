package com.gossips.hussain.gossips;


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
public class ContactsFragment extends Fragment {


    private final static String CONTACTS="Contacts";
    private final static String NAME_KEY="name";
    private final static String STATUS_KEY="status";
    private final static String IMAGE_KEY="image";
    private final static String USERS="Users";




    private String currentUserID;


    private View contactsView;
    private RecyclerView myContactsList;


    private DatabaseReference contactsRef,usersRef;
    private FirebaseAuth mAuth;


    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        contactsView=inflater.inflate(R.layout.fragment_contacts, container, false);


        myContactsList=(RecyclerView)contactsView.findViewById(R.id.contacts_list);
        myContactsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        contactsRef=FirebaseDatabase.getInstance().getReference().child(CONTACTS).child(currentUserID);
        usersRef=FirebaseDatabase.getInstance().getReference().child(USERS);



        return contactsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options=
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef,Contacts.class)
                .build();


        FirebaseRecyclerAdapter<Contacts,ContactsViewHolder> adapter
                =new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Contacts model) {


                String userIDs=getRef(position).getKey();

                usersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                        if(dataSnapshot.hasChild(IMAGE_KEY)){

                            String profileImage=dataSnapshot.child(IMAGE_KEY).getValue().toString();
                            String profileName=dataSnapshot.child(NAME_KEY).getValue().toString();
                            String profileStatus=dataSnapshot.child(STATUS_KEY).getValue().toString();



                            holder.userName.setText(profileName);
                            holder.userStatus.setText(profileStatus);
                            Picasso.get().load(profileImage).placeholder(R.drawable.profile_image).into(holder.profileImage);


                        }

                        else {

                            String profileName=dataSnapshot.child(NAME_KEY).getValue().toString();
                            String profileStatus=dataSnapshot.child(STATUS_KEY).getValue().toString();



                            holder.userName.setText(profileName);
                            holder.userStatus.setText(profileStatus);



                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                ContactsViewHolder viewHolder=new ContactsViewHolder(view);
                return viewHolder;

            }
        };


        myContactsList.setAdapter(adapter);
        adapter.startListening();
    }




    public static class ContactsViewHolder extends RecyclerView.ViewHolder{


        TextView userName,userStatus;
        CircleImageView profileImage;



        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);


            userName=(TextView)itemView.findViewById(R.id.user_profile_name);
            userStatus=(TextView)itemView.findViewById(R.id.user_profile_status);
            profileImage=(CircleImageView)itemView.findViewById(R.id.user_profile_image);


        }

    }
}
