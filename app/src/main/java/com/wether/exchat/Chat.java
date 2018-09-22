package com.wether.exchat;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class Chat extends Fragment implements View.OnClickListener {


    FirebaseUser user;
    FloatingActionButton bSendMessage;
    EditText etInputMessage;
    ListView lvMessageList;
    DatabaseReference mReference;
    DatabaseReference Xreference;


    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    String Username;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.activity_chat, container, false);

        bSendMessage = (FloatingActionButton) mView.findViewById(R.id.bSendMessage);
        etInputMessage = (EditText) mView.findViewById(R.id.etInputMessage);
        lvMessageList = (ListView) mView.findViewById(R.id.lvMessageList);
        mReference = FirebaseDatabase.getInstance().getReference().child("Messages");
        bSendMessage.setOnClickListener(this);
        user = FirebaseAuth.getInstance().getCurrentUser();

        Xreference = FirebaseDatabase.getInstance().getReference().child("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        Xreference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Username = dataSnapshot.getValue(UserModel.class).getName();


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
        displayChatMessages();
        return mView;
    }

    private void displayChatMessages() {
        FirebaseListAdapter<ChatMessage> adapter;
        adapter = new FirebaseListAdapter<ChatMessage>(getActivity(), ChatMessage.class, R.layout.message, mReference) {

            @Override
            protected void populateView(View v, ChatMessage model, int position) {


                long unixTimestamp = (long) (System.currentTimeMillis() / 1000);
                long differenceBetweenTimes = unixTimestamp -Long.parseLong(String.valueOf(model.getMessageTime()));
                long seconds = (long) (differenceBetweenTimes ) % 60;
                long minutes = (long) ((differenceBetweenTimes /  60)) % 60;
                long hours = (long) ((differenceBetweenTimes / ( 60 * 60)) % 24);
                long days = (long) (differenceBetweenTimes / ( 60 * 60 * 24));



                TextView txMessageText = (TextView) v.findViewById(R.id.txMessageText);
                TextView txMessageUser = (TextView) v.findViewById(R.id.txMessageUser);
                TextView txMessageTime = (TextView) v.findViewById(R.id.txMessageTime);
                if (model.getMessageUser() != null && model.getMessageUser().equals(user.getDisplayName())) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    params.gravity = Gravity.RIGHT;
                    txMessageText.setTextColor(v.getResources().getColor(R.color.colorPrimary));


                    txMessageUser.setLayoutParams(params);

                    txMessageText.setLayoutParams(params);
                    txMessageTime.setLayoutParams(params);


                }else{
                    txMessageUser.setGravity(Gravity.LEFT | Gravity.START);
                    txMessageText.setGravity(Gravity.LEFT | Gravity.START);
                    txMessageTime.setGravity(Gravity.LEFT | Gravity.START);

                }
                txMessageText.setText(model.getMessageText());
                txMessageUser.setText(model.getMessageUser());
                txMessageTime.setText(DateFormat.format("dd-MM-yyyy (H:mm)",// Format the date before showing it
                        model.getMessageTime()));
                if (days > 0) {
                    txMessageTime.setText(days + ""+"day");
                }else if (hours > 0){
                    txMessageTime.setText(hours + ""+"hour");
                }else if (minutes > 0){
                    txMessageTime.setText(minutes + ""+"minute");
                }else {
                    txMessageTime.setText(seconds + ""+"second");
                }
            }
        };

        lvMessageList.setAdapter(adapter);
    }

    @Override
    public void onClick(View view) {
        mReference.push().setValue(new ChatMessage(etInputMessage.getText().toString(), Username));

        etInputMessage.setText("");  // Clear the input

    }
}
