package fyp.nyp.hdbproject.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import fyp.nyp.hdbproject.R;

/**
 * Created by L30911 on 2/1/2016.
 */
public class RewardsFragment extends Fragment {
    private static final String TAG = MainFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_rewards, null);
        SharedPreferences preferences = getActivity().getSharedPreferences(MainFragment.class.getSimpleName(), Context.MODE_PRIVATE);

        TextView tvScore = (TextView) view.findViewById(R.id.tvScore);
        int score = preferences.getInt("last_score", 0);
        tvScore.setText(score+"");
        //Button btnStartScreenButton = (Button) view.findViewById(R.id.btnStartScreen);

        return view;
    }
}
