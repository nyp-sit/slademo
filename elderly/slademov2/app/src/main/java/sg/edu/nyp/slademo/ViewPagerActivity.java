package sg.edu.nyp.slademo;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.augustopicciani.drawablepageindicator.widget.DrawablePagerIndicator;

import sg.edu.nyp.slademo.fragments.BeaconsFragment;
import sg.edu.nyp.slademo.fragments.StepTrackerFragment;
import sg.edu.nyp.slademo.fragments.TimingDelayFragment;
import sg.edu.nyp.slademo.fragments.TrafficLightFragment;
import sg.edu.nyp.slademo.services.BeaconService;
import sg.edu.nyp.slademo.services.LocationService;

public class ViewPagerActivity extends AppCompatActivity{

    ViewPager viewPager;
    int NUM_PAGES = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager);
        viewPager = (ViewPager) findViewById(R.id.vpPager);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));


        //Bind the title indicator to the adapter
        DrawablePagerIndicator  titleIndicator = (DrawablePagerIndicator )findViewById(R.id.indicator);
        titleIndicator.setViewPager(viewPager);

        viewPager.setOffscreenPageLimit(NUM_PAGES);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        wl.acquire();

        Intent locationService = new Intent(this, LocationService.class);
        locationService.putExtra("id", StaticData.uid);
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
                //case 1:
                  //  fragment = new BeaconsFragment();
                    //break;
                case 1:
                    fragment = new StepTrackerFragment();
                    break;
                case 2:
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
