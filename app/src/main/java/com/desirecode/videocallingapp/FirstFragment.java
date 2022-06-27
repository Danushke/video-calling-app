package com.desirecode.videocallingapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.desirecode.videocallingapp.findfriend.FindFriendsGetterSetter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class FirstFragment extends Fragment {

    private View friendsView;
    private RecyclerView myFriendsRecyclerView;

    private DatabaseReference friendsRef,usersRef;
    private FirebaseAuth auth;
    private String currentUserID;
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment

        friendsView=inflater.inflate(R.layout.fragment_first, container, false);

        myFriendsRecyclerView=friendsView.findViewById(R.id.friends_recycler_view);
        myFriendsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        auth=FirebaseAuth.getInstance();
        currentUserID=auth.getCurrentUser().getUid();

        friendsRef= FirebaseDatabase.getInstance().getReference().child("friends").child(currentUserID); //getting all IDs of friends having under the current user
        usersRef=FirebaseDatabase.getInstance().getReference().child("Users");

        return friendsView;
        //return inflater.inflate(R.layout.fragment_first, container, false);
    }


    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options=
                new FirebaseRecyclerOptions.Builder<FindFriendsGetterSetter>()
                .setQuery(friendsRef,FindFriendsGetterSetter.class)
                .build();

        FirebaseRecyclerAdapter<FindFriendsGetterSetter,FriendsViewHolder> adapter
                = new FirebaseRecyclerAdapter<FindFriendsGetterSetter, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull FindFriendsGetterSetter model) {
                /**RETRIEVING DATA AND SET THAT FIELDS TO OUR FRIENDS LAYOUT */

                String userID=getRef(position).getKey();
                /**get the friends id via the position of recycler view and store it in "userID" in above statement
                 * AND
                 * select the user details of user what you are selected in the recycler view from the firebase "Users" node
                 */

                usersRef.child(userID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("image")){
                            String profileImage=dataSnapshot.child("image").getValue().toString();
                            String profileName=dataSnapshot.child("username").getValue().toString();
                            String profileStatus=dataSnapshot.child("status").getValue().toString();

                            holder.userName.setText(profileName);
                            holder.userStatus.setText(profileStatus);
                            Picasso.get().load(profileImage).into(holder.userImage);
                        }
                        else {
                            String profileName=dataSnapshot.child("username").getValue().toString();
                            String profileStatus=dataSnapshot.child("status").getValue().toString();

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
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                /***ACCESSING THE FRIENDS LAYOUT*/
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_layout,parent,false);
                FriendsViewHolder friendsViewHolder=new FriendsViewHolder(view);
                return friendsViewHolder;
            }
        };

        /**SET FIREBASE RECYCLER ADAPTER ON OUR RECYCLER VIEW */
        myFriendsRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }


    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        TextView userName,userStatus;
        CircleImageView userImage;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName=itemView.findViewById(R.id.friends_username_tv);
            userStatus=itemView.findViewById(R.id.friends_status_tv);
            userImage=itemView.findViewById(R.id.friends_profile_img);
        }
    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });
    }
}