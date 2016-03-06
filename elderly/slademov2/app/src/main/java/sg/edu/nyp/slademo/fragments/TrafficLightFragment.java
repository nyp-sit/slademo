package sg.edu.nyp.slademo.fragments;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import sg.edu.nyp.slademo.R;
import sg.edu.nyp.slademo.services.TestService;

/**
 * Created by L30911 on 12/2/2015.
 */
public class TrafficLightFragment extends Fragment {
    ImageView ivTrafficLight;
    long[] pattern = {0, 300, 200, 300, 200};
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
    @Override
    public void onResume() {
        super.onResume();

        bm = LocalBroadcastManager.getInstance(getActivity());

        bm.registerReceiver(enterreceiver, new IntentFilter("sg.edu.nyp.slademo.ENTER_REGION"));
        bm.registerReceiver(exitreceiver, new IntentFilter("sg.edu.nyp.slademo.EXIT_REGION"));

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_trafficlight ,null);
        ivTrafficLight = (ImageView) view.findViewById(R.id.ivTrafficLight);
        tvMessage = (TextView)view.findViewById(R.id.tvMessage);



        return view;
    }


    private void getExitNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext());
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.pionearlogo);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Pio(Near)™ iBeacon Service")
                .setTicker("")
                .setContentText("Not in any room!")
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setWhen(System.currentTimeMillis());
        Intent startIntent = new Intent(getContext(),
                TestService.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                getContext(), 2, startIntent, 0);
        builder.setContentIntent(contentIntent);
        Notification notification = builder.build();
        notification.vibrate = pattern;
        notification.defaults |= notification.DEFAULT_SOUND;
        ((NotificationManager)getContext().getSystemService(getContext().NOTIFICATION_SERVICE)).notify(2, notification);

    }
}
