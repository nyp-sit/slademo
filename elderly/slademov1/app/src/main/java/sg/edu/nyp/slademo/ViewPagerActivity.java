package sg.edu.nyp.slademo;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.viewpagerindicator.CirclePageIndicator;

import fypj.com.weicong.sharedservices.BeaconService;
import fypj.com.weicong.sharedservices.LocationService;
import sg.edu.nyp.slademo.fragments.BeaconsFragment;
import sg.edu.nyp.slademo.fragments.StepTrackerFragment;
import sg.edu.nyp.slademo.fragments.TimingDelayFragment;
import sg.edu.nyp.slademo.fragments.TrafficLightFragment;

public class ViewPagerActivity extends AppCompatActivity{

    ViewPager viewPager;
    String uid;
    int NUM_PAGES = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager);
        viewPager = (ViewPager) findViewById(R.id.vpPager);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));


        //Bind the title indicator to the adapter
        CirclePageIndicator titleIndicator = (CirclePageIndicator)findViewById(R.id.indicator);
        titleIndicator.setViewPager(viewPager);

        viewPager.setOffscreenPageLimit(NUM_PAGES);

        TelephonyManager telephonyManager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        uid = telephonyManager.getDeviceId();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        wl.acquire();

        Intent locationService = new Intent(this, LocationService.class);
        locationService.putExtra("id", uid);
        startService(locationService);

        //startService(new Intent(ViewPagerActivity.this, WatchConnectionService.class));
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                // Bluetooth is not enable :)
            }else{

                startService(new Intent(this, BeaconService.class));
            }
        }


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                Location location = new Location(LocationManager.GPS_PROVIDER);
                location.setLatitude(1.377659);
                location.setLongitude(103.849928);
                location.setAltitude(10);
                location.setTime(System.currentTimeMillis());
                location.setAccuracy(16f);
                location.setBearing(0);
                location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());


                // show debug message in log
                Log.d(ViewPagerActivity.class.getSimpleName(), location.toString());

                // provide the new location
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                locationManager.setTestProviderLocation(LocationManager.NETWORK_PROVIDER, location);


                System.out.println("start with outside range");

            }
        }, 1100);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                System.out.println("go into range");

                Location location = new Location(LocationManager.GPS_PROVIDER);
                location.setLatitude(1.377659);
                location.setLongitude(103.849928);
                location.setAltitude(10);
                location.setTime(System.currentTimeMillis());
                location.setAccuracy(16f);
                location.setBearing(0);
                location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());


                // show debug message in log
                Log.d(ViewPagerActivity.class.getSimpleName(), location.toString());

                // provide the new location
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                locationManager.setTestProviderLocation(LocationManager.NETWORK_PROVIDER, location);

            }
        }, 2000);/*
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                System.out.println("exit range");

                Location location = new Location(LocationManager.GPS_PROVIDER);
                location.setLatitude(1.3059);
                location.setLongitude(103.149928);
                location.setAltitude(10);
                location.setTime(System.currentTimeMillis());
                location.setAccuracy(16f);
                location.setBearing(0);
                location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());


                // show debug message in log
                Log.d(ViewPagerActivity.class.getSimpleName(), location.toString());

                // provide the new location
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, location);

            }
        }, 10 * 1000);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                System.out.println("enter range");

                Location location = new Location(LocationManager.GPS_PROVIDER);
                location.setLatitude(1.377659);
                location.setLongitude(103.849928);
                location.setAltitude(10);
                location.setTime(System.currentTimeMillis());
                location.setAccuracy(16f);
                location.setBearing(0);
                location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());


                // show debug message in log
                Log.d(ViewPagerActivity.class.getSimpleName(), location.toString());

                // provide the new location
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, location);

            }
        }, 20 * 1000);*/

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_pager, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class ViewPagerAdapter extends FragmentStatePagerAdapter{

        public ViewPagerAdapter(FragmentManager fm){
            super(fm);
        }



        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new Fragment();

            switch(position){
                case 0 :
                    fragment = new TrafficLightFragment();
                    break;
                case 1:
                    fragment = new BeaconsFragment();
                    break;
                case 2:
                    fragment = new StepTrackerFragment();
                    break;
                case 3:
                    fragment = new TimingDelayFragment();
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }


}
