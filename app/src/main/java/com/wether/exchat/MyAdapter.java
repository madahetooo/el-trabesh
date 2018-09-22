package com.wether.exchat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by eslam on 29/08/2017.
 */

public class MyAdapter extends BaseAdapter {
    TextView txUserName;
    ImageView state;
    Context context;
    private ArrayList<String> Name=new ArrayList<String>();
    private ArrayList<String> State=new ArrayList<String>();

    public MyAdapter(Context context, ArrayList<String> name, ArrayList<String> state) {
        this.context = context;
        Name = name;
        State = state;



    }

    @Override
    public int getCount() {

        return Name.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view==null){
            view= LayoutInflater.from(context).inflate(R.layout.user_item,viewGroup,false);
            txUserName=(TextView)view.findViewById(R.id.txUserName);
            state=(ImageView) view.findViewById(R.id.state);

            txUserName.setText(Name.get(i));
            if (State.get(i).equals("0")) {
                state.setImageResource(android.R.drawable.presence_offline);
            } else {
                state.setImageResource(android.R.drawable.presence_online);

            }


        }
        return view;
    }
}
