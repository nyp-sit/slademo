package sg.edu.nyp.slademo.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import sg.edu.nyp.slademo.R;

/**
 * Created by L30911 on 12/2/2015.
 */
public class BeaconsFragment  extends Fragment{
    //TextView tvBeaconText;
    TextView tvDistance;
    public static final String TAG = BeaconsFragment.class.getSimpleName();
    LocalBroadcastManager bm;
    ImageView ivSignalStrength;

   BroadcastReceiver enterRegionReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Enter region timing");

            DecimalFormat df = new DecimalFormat("0.00");
            tvDistance.setText(df.format(intent.getExtras().getDouble("region_distance"))+"m away");
            System.out.println(intent.getExtras().getDouble("region_distance") + " distance");
        }
    };
    BroadcastReceiver exitRegionReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
           tvDistance.setText("-");
        }
    };
    private BroadcastReceiver timeoutRegionReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long millis = intent.getLongExtra("time_left", 0l);

            String date = String.format("%d min timeout",
                    TimeUnit.MILLISECONDS.toMinutes(millis),
                    TimeUnit.MILLISECONDS.toSeconds(millis) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
            );
            tvDistance.setText("-");
        }
    };
    private BroadcastReceiver changedDistanceReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        DecimalFormat df = new DecimalFormat("0.00");
        double distance = intent.getExtras().getDouble("region_distance");
        tvDistance.setText(df.format(distance) + "m away");
        if(distance > 1.2){
            //
            ivSignalStrength.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.beacon_state_weak));
        }else if(distance > 0.8){
            ivSignalStrength.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.beacon_state_med));

        }else{
            ivSignalStrength.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.beacon_state_strong));

        }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_beacons, null);

        tvDistance = (TextView) view.findViewById(R.id.tvDistance);
        ivSignalStrength = (ImageView) view.findViewById(R.id.ivSignalStrength);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter enterRegionFilter = new IntentFilter("sg.edu.nyp.slademo.ENTER_REGION");
        IntentFilter exitRegionFilter = new IntentFilter("sg.edu.nyp.slademo.EXIT_REGION");
        IntentFilter timeoutRegionFilter = new IntentFilter("sg.edu.nyp.slademo.TIME_OUT");
        IntentFilter changedDistanceFilter = new IntentFilter("sg.edu.nyp.slademo.CHANGED_DISTANCE");

        bm = LocalBroadcastManager.getInstance(getContext());

        bm.registerReceiver(enterRegionReciever, enterRegionFilter);
        bm.registerReceiver(exitRegionReciever, exitRegionFilter);
        bm.registerReceiver(timeoutRegionReciever , timeoutRegionFilter);
        bm.registerReceiver(changedDistanceReciever , changedDistanceFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        bm.unregisterReceiver(enterRegionReciever);
        bm.unregisterReceiver(exitRegionReciever);
        bm.unregisterReceiver(timeoutRegionReciever);
        bm.unregisterReceiver(changedDistanceReciever);
    }


}
