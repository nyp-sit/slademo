package fypj.com.weicong.slademowatch.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import fypj.com.weicong.slademowatch.R;


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
        SharedPreferences prefs = getActivity().getSharedPreferences("sg.edu.nyp.slademo", Context.MODE_PRIVATE);

        time_delay = prefs.getString("time_delay", "");
    }
    String time_delay;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_timing_watch, null);
        localBroadcastManager = LocalBroadcastManager.getInstance(this.getActivity());

        TextView etDelayEntries = (TextView) view.findViewById(R.id.etMinutes);
        etDelayEntries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                fm.beginTransaction().add(R.id.content_frame, new TimingDelaySelectionFragment()).addToBackStack(null).commit();
            }
        });
        final String[] items = getActivity().getResources().getStringArray(R.array.delay_entries);
        //ArrayAdapter<String> aaItems= new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item, items);
        //aaItems.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //etDelayEntries.setAdapter(aaItems);

        SharedPreferences prefs = getActivity().getSharedPreferences("sg.edu.nyp.slademo", Context.MODE_PRIVATE);

        int index = 0;
        for(int i = 0; i < items.length;i++){
            if(prefs.getString("time_delay","").equals(items[i])){
                index = i;
                time_delay = items[i];
            }
        }
        etDelayEntries.setText(items[index]);
        /*
        etDelayEntries.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
*/
        return view;
    }
}
