package com.desirecode.videocallingapp.messages;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.desirecode.videocallingapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


/** this is the adapter class & it is a subtype of RecyclerView.Adapter class.
 * it takes the data which has to be displayed to the user in recyclerview.
 * It is like the main responsible to bind the views and display it
 *
 * most of tasks happend inside the adapter class of the recycler view**/
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> userMessageList;
    private DatabaseReference usersRef;
    private FirebaseAuth auth;

    public MessageAdapter(List<Messages>userMessageList){
        this.userMessageList=userMessageList;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        /** in this class all the binding of Views of individual items happens in this class.
         * It is a sub class of RecyclerView.ViewHolder class*/ //catching specific item view
        public TextView senderMessages,receiverMessages,senderDateTime,receiverDateTime;
        RelativeLayout receiverLinearLayout,senderLinearLayout;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverLinearLayout=itemView.findViewById(R.id.receiver_layout);
            receiverDateTime=itemView.findViewById(R.id.receiver_date_time_tv);
            receiverMessages=itemView.findViewById(R.id.receiver_message_tv);

            senderLinearLayout=itemView.findViewById(R.id.sender_layout);
            senderDateTime=itemView.findViewById(R.id.sender_date_time_tv);
            senderMessages=itemView.findViewById(R.id.sender_message_tv);


        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        /** set the message layout */
        View view=LayoutInflater.from(parent.getContext())
                .inflate(R.layout.messages_layout,parent,false);

        auth=FirebaseAuth.getInstance();
        /** pass the view to MessageViewHolder() constructor */
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        String messageSenderID=auth.getCurrentUser().getUid();
        Messages messages=userMessageList.get(position);

        String fromUserId=messages.getFrom();
        String fromMessageType=messages.getType();
        //String fromMessageType="text";

        /*usersRef=FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image")){

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/

        if (fromMessageType.equals("text")){
            holder.receiverLinearLayout.setVisibility(View.INVISIBLE);

            if (fromUserId.equals(messageSenderID)){
                holder.senderLinearLayout.setVisibility(View.VISIBLE);

                holder.senderMessages.setBackgroundResource(R.drawable.sender_message_layout);
                holder.senderMessages.setTextColor(Color.BLACK);
                holder.senderMessages.setText(messages.getMessage());
                holder.senderDateTime.setText(messages.getTime());
            }else {

                holder.senderLinearLayout.setVisibility(View.INVISIBLE);
                holder.receiverLinearLayout.setVisibility(View.VISIBLE);

                holder.receiverMessages.setBackgroundResource(R.drawable.receiver_message_layout);
                holder.receiverMessages.setTextColor(Color.BLACK);
                //holder.receiverMessages.setBackground(Color.BLUE);
                holder.receiverMessages.setText(messages.getMessage());
                holder.receiverDateTime.setText(messages.getTime());

            }
        }
    }


    @Override
    public int getItemCount() {
        return userMessageList.size();
        // first time when i run the app, it shows empty chat because i forgot to pass this return value of user message list
    }

}
