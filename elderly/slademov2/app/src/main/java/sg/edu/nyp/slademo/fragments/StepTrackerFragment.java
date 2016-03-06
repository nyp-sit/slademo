package sg.edu.nyp.slademo.fragments;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.ExecutionException;

import fypj.com.weicong.sharedservices.LocationStepsSession;
import fypj.com.weicong.sharedservices.db.UpdateDynamoDBTask;
import sg.edu.nyp.slademo.R;
import sg.edu.nyp.slademo.StaticData;
import sg.edu.nyp.slademo.services.AlarmReceiver;

/**
 * Created by L30911 on 12/2/2015.
 */
public class StepTrackerFragment extends Fragment {
    View view;
    TextView tvStepText;
    //Switch swTracking;
    public SensorManager mSensorManager;
    public Sensor mAccelerometer;
    ImageView  ivTrigger;

    //String uid;
    boolean isTracking = true;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.fragment_steps, null);
        tvStepText = (TextView) view.findViewById(R.id.tvStepText);
        ivTrigger = (ImageView) view.findViewById(R.id.ivTrigger);

        //swTracking = (Switch) view.findViewById(R.id.swTracking);


        //TelephonyManager telephonyManager =
               // (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        //uid = telephonyManager.getDeviceId();
        ivTrigger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isTracking) {
                    mSensorManager.registerListener(listener, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
                    isTracking = true;
                    ivTrigger.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.elderly_step_on));
                } else {
                    mSensorManager.unregisterListener(listener);
                    isTracking = false;
                    ivTrigger.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.elderly_step_off));
                }
            }
        });



        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    double mLastX;
    double mLastY;
    double mLastZ;
    boolean mInitialized;
    float NOISE = 4.0f;
    int numOfSteps = 0;

    SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

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
                } else if ((deltaZ > deltaX) && (deltaZ > deltaY) && isTracking) {
                    numOfSteps = numOfSteps + 1;
                    tvStepText.setText("" + numOfSteps);

                    LocationStepsSession.steps = numOfSteps;
                    if(LocationStepsSession.location != null){

                        Location location = LocationStepsSession.location;
                        String lat = String.valueOf(location.getLatitude());
                        String lng = String.valueOf(location.getLongitude());
                        String acc = String.valueOf(location.getAccuracy());
                        String alt = String.valueOf(location.getAltitude());
                        String bearing = String.valueOf(location.getBearing());
                        String speed = String.valueOf(location.getSpeed());

                        try {
                            new UpdateDynamoDBTask(getActivity()).execute(StaticData.uid, lat, lng, acc, alt, bearing, speed, "0", String.valueOf(LocationStepsSession.steps)).get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
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

    };
    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(listener, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ComponentName receiver = new ComponentName(getActivity(), AlarmReceiver.class);
        PackageManager pm = this.getActivity().getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
