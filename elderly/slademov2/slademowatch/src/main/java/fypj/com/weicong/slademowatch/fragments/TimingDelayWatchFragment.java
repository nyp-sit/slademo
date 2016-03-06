package fypj.com.weicong.slademowatch.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import fypj.com.weicong.slademowatch.R;
import fypj.com.weicong.slademowatch.Session;


/**
 * Created by L30911 on 12/7/2015.
 */
public class TimingDelayWatchFragment extends Fragment {

    LocalBroadcastManager localBroadcastManager;

    @Override
    public void onResume() {
        super.onResume();
        localBroadcastManager = LocalBroadcastManager.getInstance(this.getActivity());


    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
       // setText();
    }
    String time_delay;
    String[] items;
    TextView etDelayEntries;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_timing_watch, null);
        localBroadcastManager = LocalBroadcastManager.getInstance(this.getActivity());

        etDelayEntries = (TextView) view.findViewById(R.id.tvDelayEntries);
        etDelayEntries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Session.previousFragment = TimingDelayWatchFragment.this;
                FragmentManager fm = getActivity().getSupportFragmentManager();
                Fragment fragment = new TimingDelaySelectionFragment();
                fragment.setTargetFragment(TimingDelayWatchFragment.this, 1002);
                fm.beginTransaction().add(R.id.content_frame, fragment ).addToBackStack(null).commit();
            }
        });
        items = getActivity().getResources().getStringArray(R.array.delay_entries);


        setText();
        return view;
    }
    public void setText(){
        SharedPreferences prefs = getActivity().getSharedPreferences("sg.edu.nyp.slademo", Context.MODE_PRIVATE);

        int index = 0;
        for(int i = 0; i < items.length;i++){
            if(prefs.getString("time_delay","").equals(items[i])){
                index = i;
                time_delay = items[i];
            }
        }
        etDelayEntries.setText(items[index]);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1002 && resultCode == Activity.RESULT_OK){
            if(data != null && data.getStringExtra("time_delay") != null){
                etDelayEntries.setText(data.getStringExtra("time_delay"));
            }
        }
    }
}
