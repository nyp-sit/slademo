package fypj.com.weicong.slademowatch.fragments;

import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import fypj.com.weicong.slademowatch.MainActivity;
import fypj.com.weicong.slademowatch.R;


/**
 * Created by L30911 on 12/2/2015.
 */
public class TrafficLightWatchFragment extends Fragment {
    boolean enter;
    long[] pattern = {0, 300, 200, 300, 200};
    BroadcastReceiver enterreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            //if(bundle.getString("region_name").equals("Traffic Light")) {
            tvMessage.setText("Green man + avaliable! Tap ezlink to extend time!");
            System.out.println("Green man");
            getEntryNotification();
            rlParentLayout.setBackgroundColor(0xFFA2CEB1);
            //}
        }
    };
    BroadcastReceiver exitreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            //if(bundle.getString("region_name").equals("Traffic Light")) {
            tvMessage.setText("No traffic lights nearby");
            System.out.println("Green man");
            rlParentLayout.setBackgroundColor(0xffffff);
            //}

            //Cancel notification when out of range
            ((NotificationManager)getActivity().getSystemService(getActivity().NOTIFICATION_SERVICE)).cancel(notifyID);
        }
    };
    @Override
    public void onPause() {
        super.onPause();

        bm.unregisterReceiver(enterreceiver);
        bm.unregisterReceiver(exitreceiver);
    }
    boolean ambient = false;
    TextView tvMessage;
    LocalBroadcastManager bm;
    private int notifyID  = 0;
    @Override
    public void onResume() {
        super.onResume();

        bm = LocalBroadcastManager.getInstance(getActivity());

        bm.registerReceiver(enterreceiver, new IntentFilter("sg.edu.nyp.slademo.ENTER_REGION"));
        bm.registerReceiver(exitreceiver, new IntentFilter("sg.edu.nyp.slademo.EXIT_REGION"));

    }

    RelativeLayout rlParentLayout;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_traffic_light_watch ,null);

        tvMessage = (TextView)view.findViewById(R.id.tvMessage);
        rlParentLayout = (RelativeLayout)view.findViewById(R.id.llParentLayout);

        if(getArguments() != null && getArguments().getBoolean("fromNotification")) {
            tvMessage.setText("Green man + avaliable! Tap ezlink to extend time!");
            rlParentLayout.setBackgroundColor(0xFFA2CEB1);
        }
        return view;
    }

    private void getEntryNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity());


        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.pionearlogo);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Green man + avaliable! Tap ezlink to extend time!")
                //.setTicker("Green man + avaliable! Tap ezlink to extend time!")
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setWhen(System.currentTimeMillis());

        Intent startIntent = new Intent(getActivity(),
                MainActivity.class);
        startIntent.putExtra("fromNotification", true);
        PendingIntent mapPendingIntent =
                PendingIntent.getActivity(getActivity(), 0, startIntent, 0);


        NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.mr_ic_play_dark, "Open", mapPendingIntent).build();

        builder.extend(new NotificationCompat.WearableExtender().addAction(action));
        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_ALL;

        ((NotificationManager)getActivity().getSystemService(getActivity().NOTIFICATION_SERVICE)).notify(1012, notification);
        notifyID = 1012;

    }
}
