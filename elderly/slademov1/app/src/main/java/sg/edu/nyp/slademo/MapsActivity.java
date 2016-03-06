package sg.edu.nyp.slademo;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.BeaconManager;

import fypj.com.weicong.sharedservices.BeaconService;
import sg.edu.nyp.slademo.services.AlarmReceiver;
import sg.edu.nyp.slademo.services.TestService;


public class MapsActivity extends FragmentActivity implements
        LocationListener, SensorEventListener {

    public static int INTERVAL = 10000; // 10s
    public static String uid;
    public static double latitude, longitude;
    private final float NOISE = (float) 4.5;
    public static float numOfSteps;
    private double mLastX = 0;
    private double mLastY = 0;
    private double mLastZ = 0;
    public static String localtime = "";

    private boolean mInitialized;
    public SensorManager mSensorManager;
    public Sensor mAccelerometer;
    public static String enterRoom;
    public static String exitRoom;
    public Switch activitySwitch;
    public TextView tvBeaconCount;

    private Context context;
    private PendingIntent pendingIntent;

    public static MapsActivity mObject;
    public static final String TAG = MapsActivity.class.getSimpleName();

    BeaconManager beaconManager;
    boolean mRequestTrack;

    BroadcastReceiver enterRegionReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            tvBeaconCount.setText("Entered "+intent.getExtras().get("region_name").toString());
        }
    };
    BroadcastReceiver exitRegionReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            tvBeaconCount.setText("Exited "+intent.getExtras().get("region_name").toString());

        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        TelephonyManager telephonyManager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        uid = telephonyManager.getDeviceId();
        Intent alarmIntent = new Intent(MapsActivity.this, AlarmReceiver.class);
        alarmIntent.putExtra("id", uid);
        this.context = this;

        beaconManager = new BeaconManager(getApplicationContext());
        pendingIntent = PendingIntent.getBroadcast(MapsActivity.this, 0, alarmIntent, 0);
        mInitialized = false;
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mObject = this;

        tvBeaconCount = (TextView) findViewById(R.id.tvBeaconCount);

        IntentFilter enterRegionFilter = new IntentFilter("sg.edu.nyp.slademo.ENTER_REGION");
        IntentFilter exitRegionFilter = new IntentFilter("sg.edu.nyp.slademo.EXIT_REGION");

        registerReceiver(enterRegionReciever, enterRegionFilter);
        registerReceiver(exitRegionReciever, exitRegionFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Restart Alarm if device is rebooted
        if (mRequestTrack) {
            ComponentName receiver = new ComponentName(this, AlarmReceiver.class);
            PackageManager pm = this.getPackageManager();
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        } else {

        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu, menu);
        MenuItem item = menu.findItem(R.id.mySwitch);
        View actionView = item.getActionView();
        activitySwitch = (Switch) actionView.findViewById(R.id.switchForActionBar);
//        alarmUp = (PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(MapsActivity.this, AlarmReceiver.class),
//                PendingIntent.FLAG_NO_CREATE) != null);
        if (isMyServiceRunning(TestService.class) == true) {
            //mRequestTrack = true;
            activitySwitch.setChecked(true);
        } else {
            //mRequestTrack = false;
            activitySwitch.setChecked(false);
        }

        activitySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //do a check if the alarm is active
                if (isChecked) {
                    mRequestTrack = true;
                    toggleTracking();

                } else {
                    //start tracking
                    mRequestTrack = false;
                    toggleTracking();

                }
            }
        });

        return true;


    }


    public void toggleTracking() {

        if ( mRequestTrack && !isMyServiceRunning(BeaconService.class)) {
            startService(new Intent(this, BeaconService.class));
            startService(new Intent(this, TestService.class));
            Toast.makeText(this, "Tracking Started, Updating to DynamoDB in every " + INTERVAL + " ms", Toast.LENGTH_LONG).show();
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        } else {

            stopService(new Intent(this, TestService.class));
            stopService(new Intent(this, BeaconService.class));
            Toast.makeText(this, "Tracking Stopped!", Toast.LENGTH_SHORT).show();
            mSensorManager.unregisterListener(this);
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(enterRegionReciever);
        unregisterReceiver(exitRegionReciever);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i(TAG, provider + " enabled!");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i(TAG, provider + " disabled!");

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        TextView textView = (TextView) findViewById(R.id.tvSteps);

        double x;
        double y;
        double z;

        final double alpha = 0.8;

        double[] gravity = {0, 0, 0};

        //Low-pass filter of the force of gravity
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];


        //High-pass filter of gravity contribution
        x = event.values[0] - gravity[0];
        y = event.values[1] - gravity[1];
        z = event.values[2] - gravity[2];

        if (!mInitialized) {
            //if sensor is used first time, initialize last read values
            mLastX = x;
            mLastY = y;
            mLastZ = z;
            mInitialized = true;

        } else {
            //sensors initialized
            //difference off past and current values and decide which axis acceleration is.
            double deltaX = Math.abs(mLastX - x);
            double deltaY = Math.abs(mLastY - y);
            double deltaZ = Math.abs(mLastZ - z);

            if (deltaX < NOISE)
                deltaX = (float) 0.0;
            if (deltaY < NOISE)
                deltaY = (float) 0.0;
            if (deltaZ < NOISE)
                deltaZ = (float) 0.0;
            mLastX = x;
            mLastY = y;
            mLastZ = z;

            if (deltaX > deltaY) {
                //Horizontal Shake
            } else if (deltaY > deltaX) {
                //Vertical Shake
            } else if ((deltaZ > deltaX) && (deltaZ > deltaY)) {
                numOfSteps = numOfSteps + 1;
                textView.setText("Total Steps: " + numOfSteps);

            }

            if ((numOfSteps % 10) == 0) {

            } else {
                // no shake detected
            }


        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "Location change to " + location.toString());

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.i(TAG, "Status of " + provider + " change to " + status);
    }

}
