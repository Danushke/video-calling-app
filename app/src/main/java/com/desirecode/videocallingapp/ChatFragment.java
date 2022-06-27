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
import android.widget.ImageView;
import android.widget.TextView;

import com.desirecode.videocallingapp.findfriend.FindFriendsGetterSetter;
import com.desirecode.videocallingapp.messages.PrivateChatActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment {
    private View chatView;
    private RecyclerView chatListRecyclerView;
    DatabaseReference friendsRef,usersRef,messagesRef;
    FirebaseAuth auth;
    String currentUserID;
    String calledBy="";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
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
        chatView = inflater.inflate(R.layout.fragment_chat, container, false);

        auth=FirebaseAuth.getInstance();
        currentUserID=auth.getCurrentUser().getUid();

        messagesRef=FirebaseDatabase.getInstance().getReference().child("Private Messages").child(currentUserID);
        friendsRef=FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserID);
        usersRef=FirebaseDatabase.getInstance().getReference().child("Users");

        chatListRecyclerView=chatView.findViewById(R.id.fragment_chat_list_recycler_view);
        chatListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return chatView;
    }

    @Override
    public void onStart() {
        super.onStart();
        friendsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    setRecycler();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        checkForReceivingCall();

    }

    private void checkForReceivingCall() {
        usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("Ringing")){
                    calledBy=dataSnapshot.child("Ringing").child("ringing").getValue().toString();

                    Intent intent=new Intent(getContext(),VideoCallingActivity.class);
                    intent.putExtra("receiver_id",calledBy);
                    startActivity(intent);
                }
               /* if (dataSnapshot.hasChild("Calling")){
                    String ringing=dataSnapshot.child("Calling").child("calling").getValue().toString();

                    Intent intent=new Intent(getContext(), VideoCallingActivity.class);
                    intent.putExtra("receiver_id",ringing);
                    startActivity(intent);
                }*/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void setRecycler() {
        FirebaseRecyclerOptions<FindFriendsGetterSetter> options=
                new FirebaseRecyclerOptions.Builder<FindFriendsGetterSetter>()
                        .setQuery(messagesRef,FindFriendsGetterSetter.class)
                        .build();

        FirebaseRecyclerAdapter<FindFriendsGetterSetter, ChatsViewHolder> adapter=
                new FirebaseRecyclerAdapter<FindFriendsGetterSetter, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull FindFriendsGetterSetter model) {

                        final String usersID=getRef(position).getKey();
                        usersRef.child(usersID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild("image")){
                                    final String retImage=dataSnapshot.child("image").getValue().toString();
                                    Picasso.get().load(retImage).into(holder.profileImage);
                                }
                                final String retName=dataSnapshot.child("username").getValue().toString();
                                final String retStatus=dataSnapshot.child("status").getValue().toString();

                                holder.userName.setText(retName);
                                holder.userStatus.setText(retStatus);


/**************************************************************************************************************************/
                                if (dataSnapshot.child("UserState").hasChild("onlineState")){
                                    String state=dataSnapshot.child("UserState").child("onlineState").getValue().toString();
                                    String date=dataSnapshot.child("UserState").child("date").getValue().toString();
                                    String time=dataSnapshot.child("UserState").child("time").getValue().toString();
                                    if (state.equals("offline")){

                                        holder.userStatus.setText(time+"  "+dateCompare(date));
                                        holder.onlineImage.setVisibility(View.INVISIBLE);

                                    }else{
                                        holder.onlineImage.setVisibility(View.VISIBLE);
                                        holder.userStatus.setText(state);
                                    }



                                }else {
                                    holder.userStatus.setText("Offline");
                                }

                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent=new Intent(getContext(), PrivateChatActivity.class);
                                        intent.putExtra("visit_user_id",usersID);
                                        intent.putExtra("visit_user_name",retName);
                                        startActivity(intent);
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


                        /**ACCESSING OUR LAYOUT*/
                        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                        // in here i forgot to put a "false" for root, there for the app was crashed without showing proper error in logcat
                        return new ChatsViewHolder(view);
                    }
                };
        chatListRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    private String dateCompare(String date) {

        String ctDate,returnDate;
        Calendar calander= Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd yyyy");
        ctDate=currentDate.format(calander.getTime());

        if (date.equals(ctDate)){
            returnDate="Today";
            return returnDate;
        }/*else if (){

        }*/
        return date;
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder{

        CircleImageView profileImage;
        ImageView onlineImage;
        TextView userName,userStatus;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);
            onlineImage=itemView.findViewById(R.id.chat_online_img);
            profileImage=itemView.findViewById(R.id.chat_profile_img);
            userName=itemView.findViewById(R.id.chat_username_tv);
            userStatus=itemView.findViewById(R.id.chat_status_tv);
        }
    }
}