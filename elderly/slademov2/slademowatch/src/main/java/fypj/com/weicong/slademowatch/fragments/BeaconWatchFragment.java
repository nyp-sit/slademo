package fypj.com.weicong.slademowatch.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import fypj.com.weicong.slademowatch.R;

/**
 * Created by L30911 on 12/11/2015.
 */
public class BeaconWatchFragment extends Fragment {

     TextView tvDistance;
    ImageView ivSignalStrength;
    LocalBroadcastManager bm;
    String previousBeacon = "";
    public static final String TAG = BeaconWatchFragment.class.getSimpleName();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_beacon_watch, null);

        tvDistance = (TextView) view.findViewById(R.id.tvDistance);
        ivSignalStrength = (ImageView) view.findViewById(R.id.ivSignalStrength);
       // if(Session.fromNotification)
            //tvBeaconText.setText(previousBeacon);
        return view;
    }


    BroadcastReceiver enterRegionReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //tvBeaconText.setText(intent.getExtras().get("region_name").toString());

            previousBeacon= intent.getExtras().get("region_name").toString();
            DecimalFormat df = new DecimalFormat("0");
            tvDistance.setText(df.format(intent.getExtras().getDouble("region_distance"))+"m away");
            System.out.println(intent.getExtras().getDouble("region_distance") + " distance");
        }
    };
    BroadcastReceiver exitRegionReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //tvBeaconText.setText("No beacons");
            tvDistance.setText("0m");

        }
    };
    private BroadcastReceiver timeoutRegionReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long millis = intent.getLongExtra("time_left", 0l);

            String date = String.format("%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes(millis),
                    TimeUnit.MILLISECONDS.toSeconds(millis) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
            );
                //long minutes = TimeUnit.MILLISECONDS.toMinutes(intent.getLongExtra("time_left", 0l));
                //tvBeaconText.setText(date);
            tvDistance.setText("-");
        }
    };
    private BroadcastReceiver changedDistanceReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double distance = intent.getExtras().getDouble("region_distance");
            DecimalFormat df = new DecimalFormat("0.00");
            tvDistance.setText(df.format(intent.getExtras().getDouble("region_distance"))+"m away");
            System.out.println(intent.getExtras().getDouble("region_distance") + " distance");

            if(distance > 1.2){
                //
                ivSignalStrength.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.beacon_state_weak));
            }else if(distance > 0.8){
                ivSignalStrength.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.beacon_state_med));

            }else if(distance > 0.4){
                ivSignalStrength.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.beacon_state_strong));

            }
        }
    };


    @Override
    public void onResume() {
        super.onResume();

        IntentFilter enterRegionFilter = new IntentFilter("sg.edu.nyp.slademo.ENTER_REGION");
        IntentFilter exitRegionFilter = new IntentFilter("sg.edu.nyp.slademo.EXIT_REGION");
        IntentFilter timeoutRegionFilter = new IntentFilter("sg.edu.nyp.slademo.TIME_OUT");
        IntentFilter changedDistanceFilter = new IntentFilter("sg.edu.nyp.slademo.CHANGED_DISTANCE");

        bm = LocalBroadcastManager.getInstance(getActivity());

        bm.registerReceiver(enterRegionReciever, enterRegionFilter);
        bm.registerReceiver(exitRegionReciever, exitRegionFilter);
        bm.registerReceiver(timeoutRegionReciever , timeoutRegionFilter);
        bm.registerReceiver(changedDistanceReciever, changedDistanceFilter);

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
