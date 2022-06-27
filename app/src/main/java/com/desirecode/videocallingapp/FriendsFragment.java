package com.desirecode.videocallingapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.desirecode.videocallingapp.findfriend.FindFriendsGetterSetter;
import com.desirecode.videocallingapp.findfriend.ReceivedFriendRequestActivity;
import com.desirecode.videocallingapp.findfriend.SendFriendRequestActivity;
import com.desirecode.videocallingapp.messages.PrivateChatActivity;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FriendsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendsFragment extends Fragment {

    private View friendsView;
    private RecyclerView myFriendsRecyclerView;

    private DatabaseReference friendsRef,usersRef;
    private FirebaseAuth auth;
    private String currentUserID,receiverUserID;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FriendsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FriendsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendsFragment newInstance(String param1, String param2) {
        FriendsFragment fragment = new FriendsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        friendsView=inflater.inflate(R.layout.fragment_friends, container, false);

        myFriendsRecyclerView=friendsView.findViewById(R.id.friends_recycler_view);
        myFriendsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        auth= FirebaseAuth.getInstance();
        currentUserID=auth.getCurrentUser().getUid();

        friendsRef= FirebaseDatabase.getInstance().getReference().child("Friends"); //getting all IDs of friends having under the friends node
        usersRef=FirebaseDatabase.getInstance().getReference().child("Users");

        return friendsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        friendsRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                /**filter & check current ID is having or not in the friends node**/
                if (dataSnapshot.exists()){
                    setRecycler();
                }else {
                    Toast.makeText(getContext(),"You haven't friends yet",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    private void setRecycler() {
        FirebaseRecyclerOptions options=
                new FirebaseRecyclerOptions.Builder<FindFriendsGetterSetter>()
                        .setQuery(friendsRef.child(currentUserID),FindFriendsGetterSetter.class)
                        .build();

        FirebaseRecyclerAdapter<FindFriendsGetterSetter, FriendsViewHolder> adapter
                = new FirebaseRecyclerAdapter<FindFriendsGetterSetter, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, final int position, @NonNull FindFriendsGetterSetter model) {
                /**RETRIEVING DATA AND SET THAT FIELDS TO OUR FRIENDS LAYOUT */

                String userID=getRef(position).getKey();
                /**get the friends id via the position of recycler view and store it in "userID" in above statement
                 * AND
                 * select the user details of that user who you are selected in the recycler view from the firebase "Users" node
                 */

                usersRef.child(userID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("image")){
                            String profileImage=dataSnapshot.child("image").getValue().toString();

                            Picasso.get().load(profileImage).into(holder.userImage);
                        }

                            String profileName=dataSnapshot.child("username").getValue().toString();
                            String profileStatus=dataSnapshot.child("status").getValue().toString();

                            holder.userName.setText(profileName);
                            holder.userStatus.setText(profileStatus);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id=getRef(position).getKey(); //get the user id via position in recycler view
                        Toast.makeText(getContext(),visit_user_id,Toast.LENGTH_SHORT).show();
                        sendUserToProfileViewActivity(visit_user_id);
                    }
                });

                holder.messageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        receiverUserID=getRef(position).getKey();

                        //holder.itemView.setVisibility(View.INVISIBLE);
                        openChat(receiverUserID,"empty");
                        Toast.makeText(getContext(),"it will implement later",Toast.LENGTH_SHORT).show();
                    }
                });

                holder.unfriendButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        receiverUserID=getRef(position).getKey();
                        //holder.itemView.setVisibility(View.INVISIBLE);
                        removeSpecificFriend();
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

    private void openChat(String receiverUserID, String name) {
        Intent intent=new Intent(getContext(), PrivateChatActivity.class);
        intent.putExtra("visit_user_id",receiverUserID);
        intent.putExtra("visit_user_name",name);
        startActivity(intent);

    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        TextView userName,userStatus;
        CircleImageView userImage;
        Button messageButton,unfriendButton;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName=itemView.findViewById(R.id.friends_username_tv);
            userStatus=itemView.findViewById(R.id.friends_status_tv);
            userImage=itemView.findViewById(R.id.friends_profile_img);
            messageButton=itemView.findViewById(R.id.friends_message_btn);
            unfriendButton=itemView.findViewById(R.id.friends_unfriend_btn);
        }
    }

    private void sendUserToProfileViewActivity(String visit_user_id) {
        Intent intent=new Intent(getContext(),ProfileViewActivity.class);
        intent.putExtra("visit_user_id",visit_user_id);
        startActivity(intent);
    }
    private void removeSpecificFriend() {
        friendsRef.child(currentUserID).child(receiverUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            friendsRef.child(receiverUserID).child(currentUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){

                                            }
                                        }
                                    });

                        }
                    }
                });
    }
}