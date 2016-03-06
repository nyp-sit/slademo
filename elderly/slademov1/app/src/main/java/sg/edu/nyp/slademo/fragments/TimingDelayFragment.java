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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import sg.edu.nyp.slademo.R;

/**
 * Created by L30911 on 12/7/2015.
 */
public class TimingDelayFragment extends Fragment {

    LocalBroadcastManager localBroadcastManager;

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

        Spinner spDelayEntries = (Spinner) view.findViewById(R.id.spMinutes);

        final String[] items = getActivity().getResources().getStringArray(R.array.delay_entries);
        ArrayAdapter<String> aaItems= new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item, items);
        aaItems.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDelayEntries.setAdapter(aaItems);

        SharedPreferences prefs = getActivity().getSharedPreferences("sg.edu.nyp.slademo", Context.MODE_PRIVATE);

        int index = 0;
        for(int i = 0; i < items.length;i++){
            if(prefs.getString("time_delay","").equals(items[i])){
                index = i;
            }
        }
        spDelayEntries.setSelection(index);
        spDelayEntries.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences prefs = getActivity().getSharedPreferences("sg.edu.nyp.slademo", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("time_delay", items[position]);
                editor.commit();

                Intent i = new Intent("sg.edu.nyp.slademo.CHANGED_DELAY");
                localBroadcastManager.sendBroadcast(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }
}
