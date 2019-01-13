package com.gossips.hussain.gossips;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private final static String NAME_KEY="name";
    private final static String STATUS_KEY="status";
    private final static String IMAGE_KEY="image";
    private final static String USERS="Users";
    private final static String PROFILE_IMAGES="Profile Images";
    private final static String VISIT_USER_ID="visit_user_id";


    private Toolbar mToolBar;
    private RecyclerView findFriendsRecyclerList;



    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        usersRef=FirebaseDatabase.getInstance().getReference().child(USERS);


        initViews();
    }


    @Override
    protected void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<Contacts> options=
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(usersRef,Contacts.class)
                .build();



        FirebaseRecyclerAdapter<Contacts,FindFriendsViewHolder> adapter=
                new FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, final int position, @NonNull Contacts model) {



                        holder.userName.setText(model.getName());
                        holder.userStatus.setText(model.getStatus());
                        Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);


                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                String visit_user_id=getRef(position).getKey();

                                Intent profileIntent=new Intent(FindFriendsActivity.this,ProfileActivity.class);
                                profileIntent.putExtra(VISIT_USER_ID,visit_user_id);
                                startActivity(profileIntent);

                            }
                        });


                    }

                    @NonNull
                    @Override
                    public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {


                        View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                        FindFriendsViewHolder viewHolder=new FindFriendsViewHolder(view);
                        return viewHolder;


                    }
                };


        findFriendsRecyclerList.setAdapter(adapter);
        adapter.startListening();


    }










    private void initViews() {

        findFriendsRecyclerList=(RecyclerView)findViewById(R.id.find_friends_recyclerlist);
        findFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));


        mToolBar=(Toolbar)findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Find Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


    }



    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder{



        TextView userName,userStatus;
        CircleImageView profileImage;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);


            userName=(TextView)itemView.findViewById(R.id.user_profile_name);
            userStatus=(TextView)itemView.findViewById(R.id.user_profile_status);
            profileImage=(CircleImageView)itemView.findViewById(R.id.user_profile_image);
        }
    }
}
