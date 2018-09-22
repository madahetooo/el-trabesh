package com.wether.exchat;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
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

import java.util.ArrayList;


public class Users extends Fragment {
    DatabaseReference Xreference = FirebaseDatabase.getInstance().getReference().child("User");
    ListView lvUsers;
    private ArrayList<String> Uid = new ArrayList<String>();
    private ArrayList<String> Name = new ArrayList<String>();
    private ArrayList<String> State = new ArrayList<String>();
    FirebaseUser mUser;
    MyAdapter myAdapter;
//    private ArrayList<String> arraylist;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.activity_users, container, false);
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        lvUsers = (ListView) mView.findViewById(R.id.lvUsers);
        displayUsers();

        lvUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String ConversationId = mUser.getUid() + Uid.get(i);
                String reverseId = Uid.get(i) + mUser.getUid();
                Intent intent = new Intent(getActivity(), UserChat.class);
                intent.putExtra("ConversationId", ConversationId);
                intent.putExtra("reverseId", reverseId);
                startActivity(intent);
            }
        });
        return mView;

    }

    private void displayUsers() {
        FirebaseListAdapter<UserModel> adapter;
        adapter = new FirebaseListAdapter<UserModel>(getActivity(), UserModel.class, R.layout.user_item, Xreference) {
            @Override
            protected void populateView(View v, UserModel model, int position) {
                TextView txUserName = (TextView) v.findViewById(R.id.txUserName);
                ImageView state = (ImageView) v.findViewById(R.id.state);
                txUserName.setText(model.getName());
//                arraylist.add(model.getUserId());
                if (model.getState().equals("0")) {
                    state.setImageResource(android.R.drawable.presence_offline);
                } else {
                    state.setImageResource(android.R.drawable.presence_online);

                }

            }
        };
        lvUsers.setAdapter(adapter);


        Xreference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                Name.clear();
                State.clear();
                Uid.clear();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    if (!mUser.getUid().equals(dataSnapshot1.child("UserId").getValue().toString())) {
                        State.add(dataSnapshot1.child("State").getValue().toString());
                        Name.add(dataSnapshot1.child("Name").getValue().toString());
                        Uid.add(dataSnapshot1.child("UserId").getValue().toString());

                    }

                }

                myAdapter = new MyAdapter(getActivity(), Name, State);
                lvUsers.setAdapter(myAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
}
