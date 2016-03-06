package fypj.com.weicong.slademowatch.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import fypj.com.weicong.slademowatch.R;
import fypj.com.weicong.slademowatch.Session;


/**
 * Created by L30911 on 12/2/2015.
 */
public class TrafficLightWatchFragment extends Fragment {
    BroadcastReceiver enterreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            tvMessage.setText("Green man + is avaliable");
            tvMessage.setTextColor(0xff67c152);
            ivTrafficLight.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.green_light));

        }
    };
    BroadcastReceiver exitreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            tvMessage.setText("No traffic lights nearby");
            tvMessage.setTextColor(Color.parseColor("#9aa3ac"));

            ivTrafficLight.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.off));
        }
    };
    @Override
    public void onPause() {
        super.onPause();

        bm.unregisterReceiver(enterreceiver);
        bm.unregisterReceiver(exitreceiver);
    }
    TextView tvMessage;
    LocalBroadcastManager bm;
    ImageView ivTrafficLight;
    private int notifyID  = 0;
    @Override
    public void onResume() {
        super.onResume();

        bm = LocalBroadcastManager.getInstance(getActivity());

        bm.registerReceiver(enterreceiver, new IntentFilter("sg.edu.nyp.slademo.ENTER_REGION"));
        bm.registerReceiver(exitreceiver, new IntentFilter("sg.edu.nyp.slademo.EXIT_REGION"));
        //bm.registerReceiver(changedDistanceReceiver, new IntentFilter("sg.edu.nyp.slademo.CHANGED_DISTANCE"));

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_traffic_light_watch, null);

        tvMessage = (TextView)view.findViewById(R.id.tvMessage);
        ivTrafficLight = (ImageView) view.findViewById(R.id.ivTrafficLight);

        if(Session.fromNotification){
            tvMessage.setText("Green man + is avaliable");

            //ivImageBlinking.setBackground(getResources().getDrawable(R.drawable.animation_blinking_light));

            // Get the background, which has been compiled to an AnimationDrawable object.
            //frameAnimation = (AnimationDrawable) ivImageBlinking.getBackground();

            // Start the animation (looped playback by default).
            //frameAnimation.start();
        }

        return view;
    }
}
