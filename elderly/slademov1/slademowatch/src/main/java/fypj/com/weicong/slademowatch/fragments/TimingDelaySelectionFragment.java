package fypj.com.weicong.slademowatch.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fypj.com.weicong.slademowatch.R;
import fypj.com.weicong.slademowatch.WearableListAdapter;

/**
 * Created by L30911 on 12/18/2015.
 */
public class TimingDelaySelectionFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_timing_selection_watch, null);

        final WearableListView wrListView = (WearableListView) view.findViewById(R.id.wearable_list);
        wrListView.setAdapter(new WearableListAdapter(getActivity(), new String[] { "1" , "2", "3", "4", "5"}));


        return view;
    }
}
