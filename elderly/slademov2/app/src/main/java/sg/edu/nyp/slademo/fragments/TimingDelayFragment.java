package sg.edu.nyp.slademo.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import sg.edu.nyp.slademo.R;

/**
 * Created by L30911 on 12/7/2015.
 */
public class TimingDelayFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();
    int selected = -1;
    int selectedIndex = 0;
    LocalBroadcastManager localBroadcastManager;
    boolean firstTimeChangeDelay = true;//prevent change delay from getting called the first time
    @Override
    public void onResume() {
        super.onResume();
        localBroadcastManager = LocalBroadcastManager.getInstance(this.getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_timing, null);
        localBroadcastManager = LocalBroadcastManager.getInstance(this.getContext());

        final TextView tvDelayEntries = (TextView) view.findViewById(R.id.tvDelayEntries);

        final String[] items = getActivity().getResources().getStringArray(R.array.delay_entries);

        SharedPreferences prefs = getActivity().getSharedPreferences("sg.edu.nyp.slademo", Context.MODE_PRIVATE);

        int index = 0;
        for(int i = 0; i < items.length;i++){
            if(prefs.getString("time_delay","").equals(items[i])){
                index = i;
            }
        }
        selectedIndex = index;
        selected =  Integer.parseInt(items[index]);
        //selected ++;
        tvDelayEntries.setText(selected+"");
        tvDelayEntries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences prefs = getActivity().getSharedPreferences("sg.edu.nyp.slademo", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("time_delay", items[selectedIndex]);
                editor.commit();

                Intent i = new Intent("sg.edu.nyp.slademo.CHANGED_DELAY");
                localBroadcastManager.sendBroadcast(i);

                selectedIndex++;
                if(selectedIndex == items.length ){
                    selectedIndex = 0;
                }
                selected =  Integer.parseInt(items[selectedIndex]);
                tvDelayEntries.setText(selected + "");


            }
        });
       // spDelayEntries.setSelection(items[index]);
/*
        spDelayEntries.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences prefs = getActivity().getSharedPreferences("sg.edu.nyp.slademo", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("time_delay", items[position]);
                editor.commit();

                if (!firstTimeChangeDelay) {
                    Intent i = new Intent("sg.edu.nyp.slademo.CHANGED_DELAY");
                    localBroadcastManager.sendBroadcast(i);
                }
                firstTimeChangeDelay = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });*/

        return view;
    }
}
