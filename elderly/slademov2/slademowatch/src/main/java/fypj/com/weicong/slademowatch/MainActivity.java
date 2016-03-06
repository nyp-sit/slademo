package fypj.com.weicong.slademowatch;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import fypj.com.weicong.slademowatch.fragments.MainFragment;
import fypj.com.weicong.slademowatch.services.BeaconWatchService;
import fypj.com.weicong.slademowatch.services.ConnectNodesService;
import fypj.com.weicong.slademowatch.services.LocationWatchService;



public class MainActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks {
    GoogleApiClient client;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!isMyServiceRunning(BeaconWatchService.class)){
            stopService(new Intent(MainActivity.this, BeaconWatchService.class));
        }
        startService(new Intent(MainActivity.this, BeaconWatchService.class));
        if(!isMyServiceRunning(ConnectNodesService.class)){
            stopService(new Intent(MainActivity.this, ConnectNodesService.class));
        }
        startService(new Intent(MainActivity.this, ConnectNodesService.class));
        if(!isMyServiceRunning(LocationWatchService.class)){
            stopService(new Intent(MainActivity.this, LocationWatchService.class));
        }
        startService(new Intent(MainActivity.this, LocationWatchService.class));
        Fragment frag = new MainFragment();
        Bundle bundle = getIntent().getExtras();
        if(bundle != null)
            frag.setArguments(bundle);


        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, frag).addToBackStack(null).commit();
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
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
