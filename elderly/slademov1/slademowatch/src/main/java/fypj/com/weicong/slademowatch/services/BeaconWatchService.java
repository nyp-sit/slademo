package fypj.com.weicong.slademowatch.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import fypj.com.weicong.slademowatch.SendUpdateDbRequest;


/**
 * Created by L30912 on 29/10/2015.
 */
public class BeaconWatchService extends Service implements BeaconConsumer, RangeNotifier, MonitorNotifier, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    BeaconManager beaconManager;
    public static final String TAG = BeaconWatchService.class.getSimpleName();
    public static String enterRoom;
    public static String exitRoom;
    public static String localtime = "";
    public static String uid;
    long[] pattern = {0, 300, 200, 300, 200};
    GoogleApiClient mGoogleApiClient;

    private static final org.altbeacon.beacon.Region[] BEACONS = new org.altbeacon.beacon.Region[]{
            new org.altbeacon.beacon.Region("Traffic Light", Identifier.parse("0x" + new StringBuilder("edd1ebeac04e5defa017").toString()), Identifier.parse("0xca46e3d2814d"), null),
            new org.altbeacon.beacon.Region("WGX", Identifier.parse("0x"+new StringBuilder("e2c56db5dffb48d2b060d0f5a71096e0")), Identifier.parse("0"), Identifier.parse("0"))
    };

    BroadcastReceiver inRangeReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            blueToothCheck();
            Log.d(TAG, "Into range of geofence");
        }
    };
    BroadcastReceiver outRangeReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                beaconManager.stopMonitoringBeaconsInRegion(region);

                Log.d(TAG, "Outside range of geofence");
                if (region != null) {
                    Intent i = new Intent("sg.edu.nyp.slademo.EXIT_REGION");
                    i.putExtra("region_name", region.getUniqueId());
                    i.putExtra("region_id1", region.getId1().toString());
                    i.putExtra("region_id2", region.getId2().toString());
                    bm.sendBroadcast(i);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    BroadcastReceiver changedDelayReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences prefs = getSharedPreferences("sg.edu.nyp.slademo", Context.MODE_PRIVATE);
            String delay = prefs.getString("time_delay", "");
            if (!delay.equals("")) {
                durationToExpire = (int) (Double.parseDouble(delay) * 1000 * 60);
                System.out.println("expire");
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    LocalBroadcastManager bm;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(getApplicationContext(), "Started beacon service", Toast.LENGTH_LONG).show();


        mGoogleApiClient = new GoogleApiClient.Builder(BeaconWatchService.this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(BeaconWatchService.this)
                .addOnConnectionFailedListener(BeaconWatchService.this)
                .build();
        mGoogleApiClient.connect();
        //check if got 0 device or single device
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                if(getConnectedNodesResult != null && getConnectedNodesResult.getNodes().size() > 0){
                    uid = getConnectedNodesResult.getNodes().get(0).getId();
                    Log.d(TAG, "1 Devices connected");
                }else{
                    Log.d(TAG, "0 Devices connected");
                }

                beaconManager = BeaconManager.getInstanceForApplication(getApplicationContext());

                beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
                beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));

                beaconManager.bind(BeaconWatchService.this);//Only should start connection after uid has been verified to exists or not

                bm = LocalBroadcastManager.getInstance(getApplicationContext());

                bm.registerReceiver(changedDelayReciever, new IntentFilter("sg.edu.nyp.slademo.CHANGED_DELAY"));
                bm.registerReceiver(inRangeReciever, new IntentFilter("sg.edu.nyp.slademo.LOCATION_IN_RANGE"));
                bm.registerReceiver(outRangeReciever, new IntentFilter("sg.edu.nyp.slademo.LOCATION_OUT_OF_RANGE"));


                SharedPreferences prefs = getSharedPreferences("sg.edu.nyp.slademo", Context.MODE_PRIVATE);
                String delay = prefs.getString("time_delay", "");
                if (!delay.equals(""))
                    durationToExpire = (int) (Double.parseDouble(delay) * 1000 * 60);
                else
                    durationToExpire = 1000 * 60 * 2;

            }
        });

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        beaconManager.unbind(this);
        bm.unregisterReceiver(changedDelayReciever);
        bm.unregisterReceiver(inRangeReciever);
        bm.unregisterReceiver(outRangeReciever);
    }

    org.altbeacon.beacon.Region region;

    public void blueToothCheck() {
        Log.d(TAG, "Launch bluetooth scanning now!");
        bm.unregisterReceiver(inRangeReciever);
        region = new org.altbeacon.beacon.Region("Beacon3", Identifier.parse("0x" + new StringBuilder("edd1ebeac04e5defa017").toString()), Identifier.parse("0xca46e3d2814d"), null);
        //Region region = new Region("", null,null,null);
        beaconManager.setMonitorNotifier(BeaconWatchService.this);
        beaconManager.setBackgroundMode(true);
        beaconManager.setBackgroundScanPeriod(1000l);
        beaconManager.setBackgroundBetweenScanPeriod(0l);
        try {
            beaconManager.updateScanPeriods();
        } catch (RemoteException e) {
            e.printStackTrace();
        }


        try {
            beaconManager.startMonitoringBeaconsInRegion(region);
            //beaconManager.startMonitoringBeaconsInRegion(BEACONS[1]);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBeaconServiceConnect() {
        if(uid == null) {//not connected to any device
            blueToothCheck();//start bluetooth scanning now, because devices will be in offline mode
            Log.d(TAG, "uid empty, immediately start scanning");
        }else{
            Log.d(TAG, "uid not empty,connected to a device, waiting for location to come in");
        }
    }

    Beacon currentBeacon;

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> collection, final org.altbeacon.beacon.Region region) {

        Log.d(TAG, "Range beacons!");
        for (Beacon beacon : collection) {
            System.out.println(beacon.getDistance() + " distance");
            if (currentBeacon == null || !currentBeacon.equals(beacon)) { //shall not  region that i am already inside of
                Intent i = new Intent("sg.edu.nyp.slademo.ENTER_REGION");
                i.putExtra("region_name", region.getUniqueId());
                i.putExtra("region_id1", region.getId1().toString());
                i.putExtra("region_id1", region.getId1().toString());
                i.putExtra("region_id2", region.getId2().toString());
                i.putExtra("region_distance", beacon.getDistance());

                bm.sendBroadcast(i);
                mGoogleApiClient.disconnect();
                mGoogleApiClient = new GoogleApiClient.Builder(BeaconWatchService.this)
                        .addApi(LocationServices.API)
                        .addApi(Wearable.API)
                        .addConnectionCallbacks(BeaconWatchService.this)
                        .addOnConnectionFailedListener(BeaconWatchService.this)
                        .build();

                mGoogleApiClient.connect();//Connect and get last known location

                currentBeacon = beacon;
            }else if(currentBeacon.equals(beacon)){
                Intent i = new Intent("sg.edu.nyp.slademo.CHANGED_DISTANCE");
                i.putExtra("region_name", region.getUniqueId());
                i.putExtra("region_id1", region.getId1().toString());
                i.putExtra("region_id1", region.getId1().toString());
                i.putExtra("region_id2", region.getId2().toString());
                i.putExtra("region_distance", beacon.getDistance());

                bm.sendBroadcast(i);
            }
        }
        if (collection.size() > 0) {
            try {
                beaconManager.stopRangingBeaconsInRegion(region);
                //beaconManager.stopMonitoringBeaconsInRegion(region);
            } catch (RemoteException e) {
                e.printStackTrace();
            }



        }
    }
    long lastEnterTime = 0;
    long expiryTime = 0;

    int durationToExpire; //= 1000 * 60 * 2;
    @Override
    public void didEnterRegion(org.altbeacon.beacon.Region region) {
        long currentTime = System.currentTimeMillis();
        long diff = currentTime - lastEnterTime;
        Log.d(TAG, "Enter region");
        if((System.currentTimeMillis() - lastEnterTime) > durationToExpire) {
            lastEnterTime = System.currentTimeMillis();
            expiryTime = lastEnterTime + durationToExpire;

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date resultdate = new Date(System.currentTimeMillis());
            localtime = sdf.format(resultdate);

            enterRoom = region.getUniqueId();

            SendUpdateDbRequest.sendData("UpdateDynamoDBEnterRoom", new String[]{uid, localtime, enterRoom, exitRoom}, getApplicationContext());
            //new UpdateDynamoDBEnterRoom(getApplicationContext()).execute(uid, localtime, enterRoom, exitRoom);

            try {
                beaconManager.setRangeNotifier(BeaconWatchService.this);
                beaconManager.startRangingBeaconsInRegion(region);
                Log.d(TAG, "Start range beacons");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else{
            Intent i = new Intent("sg.edu.nyp.slademo.TIME_OUT");
            long difference = expiryTime - System.currentTimeMillis();
            i.putExtra("time_left", difference);

            bm.sendBroadcast(i);
            System.out.println("Time left ");
        }
    }
    boolean leftRegion = true;
    @Override
    public void didExitRegion(org.altbeacon.beacon.Region region) {

        //Exit??
        if (enterRoom == null) {//may enter here due to enterregion counting down time check
            Log.e("User", " not in any room");

        } else {


            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date resultdate = new Date(System.currentTimeMillis());
            localtime = sdf.format(resultdate);
            exitRoom = enterRoom;

            Intent i = new Intent("sg.edu.nyp.slademo.EXIT_REGION");
            i.putExtra("region_name", region.getUniqueId());
            i.putExtra("region_id1", region.getId1().toString());
            i.putExtra("region_id2", region.getId2().toString());
            bm.sendBroadcast(i);

            SendUpdateDbRequest.sendData("UpdateDynamoDBExitRoom", new String[]{uid, localtime, enterRoom, exitRoom}, getApplicationContext());

            bm.registerReceiver(inRangeReciever, new IntentFilter("sg.edu.nyp.slademo.LOCATION_IN_RANGE"));

            //new UpdateDynamoDBExitRoom(getApplicationContext()).execute(uid, localtime, enterRoom, exitRoom);
            enterRoom = null;

            leftRegion = true;
            currentBeacon = null;
        }
    }

    @Override
    public void didDetermineStateForRegion(int i, org.altbeacon.beacon.Region region) {

    }

    @Override
    public void onConnected(Bundle bundle) {
        try {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (location != null && currentBeacon != null) {
                SendUpdateDbRequest.sendData("UpdateDetectedBeacon", new String[]{uid, String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), currentBeacon.getId1().toString(), currentBeacon.getId2().toString(), String.valueOf(currentBeacon.getDistance())}, getApplicationContext());
                //new UpdateDetectedBeacon(getApplicationContext()).execute(uid, location.getLatitude(), location.getLongitude(), currentBeacon).get();
            }
        }catch (NullPointerException e){

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }



}