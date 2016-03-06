package fypj.com.weicong.slademowatch.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
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
import org.altbeacon.beacon.Region;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import fypj.com.weicong.slademowatch.MainActivity;
import fypj.com.weicong.slademowatch.R;
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

    private static final org.altbeacon.beacon.Region[] TRAFFIC_BEACONS = new org.altbeacon.beacon.Region[]{
            //new org.altbeacon.beacon.Region("Traffic Light", Identifier.parse("0x" + new StringBuilder("edd1ebeac04e5defa017").toString()), Identifier.parse("0xca46e3d2814d"), null),
            new org.altbeacon.beacon.Region("Room", Identifier.parse("0x"+new StringBuilder("edd1ebeac04e5defa017")), Identifier.parse("0xdfff2341482f"),null),
    };
    private static final org.altbeacon.beacon.Region[] BEACONS = new org.altbeacon.beacon.Region[]{

            new org.altbeacon.beacon.Region("Room", Identifier.parse("0x"+new StringBuilder("edd1ebeac04e5defa017")), Identifier.parse("0xdfff2341482f"),null),
            new org.altbeacon.beacon.Region("Room 1", Identifier.parse("0x"+new StringBuilder("edd1ebeac04e5defa017")), Identifier.parse("0xd8d7f90a68e9"),null),
            new org.altbeacon.beacon.Region("Room 2", Identifier.parse("0x"+new StringBuilder("edd1ebeac04e5defa017")), Identifier.parse("0xc684ddcc1f78"),null),
            //new org.altbeacon.beacon.Region("Room", Identifier.parse("0x"+new StringBuilder("e2c56db5dffb48d2b060d0f5a71096e0")), Identifier.parse("0"), Identifier.parse("1")),
           // new org.altbeacon.beacon.Region("Room 2", Identifier.parse("0x"+new StringBuilder("e2c56db5dffb48d2b060d0f5a71096e0")), Identifier.parse("2"), Identifier.parse("2"))
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
                for(Region region : BEACONS){
                    beaconManager.stopMonitoringBeaconsInRegion(region);
                }

                Log.d(TAG, "Outside range of geofence");
                if (currentBeacon != null) {
                    Intent i = new Intent("sg.edu.nyp.slademo.EXIT_REGION");
                    //i.putExtra("region_name", currentBeacon.getUniqueId());
                    i.putExtra("region_id1", currentBeacon.getId1().toString());
                    i.putExtra("region_id2", currentBeacon.getId2().toString());
                    bm.sendBroadcast(i);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    /*
    *       response["data"][0]["options"][2]
    * */

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
        //region = new org.altbeacon.beacon.Region("Traffic Light", Identifier.parse("0x" + new StringBuilder("edd1ebeac04e5defa017").toString()), Identifier.parse("0xca46e3d2814d"), null);
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
            for(Region region : BEACONS){
                beaconManager.startMonitoringBeaconsInRegion(region);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBeaconServiceConnect() {
        //if(uid == null) {//not connected to any device
            blueToothCheck();//start bluetooth scanning now, because devices will be in offline mode
          //  Log.d(TAG, "uid empty, immediately start scanning");
        //}else{
          //  Log.d(TAG, "uid not empty,connected to a device, waiting for location to come in");
        //}
    }

    Beacon currentBeacon;
    ArrayList<Beacon> beaconsSorted = new ArrayList<>();
    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> collection, final org.altbeacon.beacon.Region region) {
        int foundIndex = -1;
        Log.d(TAG, "Range beacons!");
        for (Beacon beacon : collection) {
            if(beacon == null){
                return;
            }
            Beacon[] beaconsSortedArray = beaconsSorted.toArray(new Beacon[]{});

            synchronized (beaconsSortedArray) {
                for (int j = 0; j < beaconsSortedArray.length; j++) {
                    boolean isSame = false;
                    if (beaconsSortedArray[j].getIdentifiers().size() > 2 && beaconsSortedArray[j].getId3() != null) {
                        isSame = beaconsSortedArray[j].getId1().equals(beacon.getId1()) && beaconsSortedArray[j].getId2().equals(beacon.getId2()) && beaconsSortedArray[j].getId3().equals(beacon.getId3());
                    } else {
                        isSame = beaconsSortedArray[j].getId1().equals(beacon.getId1()) && beaconsSortedArray[j].getId2().equals(beacon.getId2());
                    }
                    if (isSame)
                        foundIndex = j;

                }

                if (foundIndex != -1)
                    beaconsSorted.set(foundIndex, beacon);
                else
                    beaconsSorted.add(beacon);
                Collections.sort(beaconsSorted, new Comparator<Beacon>() {
                    @Override
                    public int compare(Beacon lhs, Beacon rhs) {
                        return Double.compare(lhs.getDistance(), rhs.getDistance());
                    }
                });
            }

            for (int i = 0; i < beaconsSorted.size(); i++) {
                Log.d(TAG, i + " distance " + beaconsSorted.get(i).getDistance() + " :" + beaconsSorted.get(i).getId2().toString());
            }
            //Need to somehow get a hashmap of beacons?
            mGoogleApiClient = new GoogleApiClient.Builder(BeaconWatchService.this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {
                            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                            if(location != null)
                                SendUpdateDbRequest.sendData("UpdateDetectedBeacon", new String[]{uid, String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), beaconsSorted.get(0).getId1().toString(), beaconsSorted.get(0).getId2().toString(), String.valueOf(beaconsSorted.get(0).getDistance())}, getApplicationContext());
                        }

                        @Override
                        public void onConnectionSuspended(int i) {

                        }
                    })
                    .addOnConnectionFailedListener(BeaconWatchService.this)
                    .build();

            mGoogleApiClient.connect();
            if (currentBeacon == null || !currentBeacon.equals(beacon)) { //shall not  region that i am already inside of
                boolean isTraffic = false;
                for(int i = 0; i < TRAFFIC_BEACONS.length;i++){
                    Region region1 = TRAFFIC_BEACONS[i];

                    if(region1.getId1().equals(beacon.getId1()) && region1.getId2().equals(beacon.getId2()) &&  (region1.getId3() == null || region1.getId3().equals(beacon.getId3()))){
                        isTraffic = true;
                        break;
                    }
                }
                if(isTraffic && (System.currentTimeMillis() - lastEnterTime) > durationToExpire) {

                    Intent i = new Intent("sg.edu.nyp.slademo.ENTER_REGION");
                    i.putExtra("region_name", region.getUniqueId());
                    i.putExtra("region_id1", region.getId1().toString());
                    i.putExtra("region_id1", region.getId1().toString());
                    i.putExtra("region_id2", region.getId2().toString());
                    i.putExtra("region_distance", beacon.getDistance());

                    bm.sendBroadcast(i);

                    getEntryNotification();

                    mGoogleApiClient.disconnect();
                    mGoogleApiClient = new GoogleApiClient.Builder(BeaconWatchService.this)
                            .addApi(LocationServices.API)
                            .addApi(Wearable.API)
                            .addConnectionCallbacks(BeaconWatchService.this)
                            .addOnConnectionFailedListener(BeaconWatchService.this)
                            .build();

                    mGoogleApiClient.connect();//Connect and get last known location

                    currentBeacon = beacon;

                    lastEnterTime = System.currentTimeMillis();
                    expiryTime = lastEnterTime + durationToExpire;

                }


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
            //try {
                //beaconManager.stopRangingBeaconsInRegion(region);
                //beaconManager.stopMonitoringBeaconsInRegion(region);
           // } catch (RemoteException e) {
            //    e.printStackTrace();
           // }



        }
    }
    long lastEnterTime = 0;
    long expiryTime = 0;

    int durationToExpire; //= 1000 * 60 * 2;
    @Override
    public void didEnterRegion(org.altbeacon.beacon.Region region) {
        Log.d(TAG, "Enter region");



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
    }
    boolean leftRegion = true;
    boolean onceSkipFromNotification  = false;
    @Override
    public void didExitRegion(org.altbeacon.beacon.Region region) {
        for(int i = 0 ; i < beaconsSorted.size();i++){
            if(beaconsSorted.get(i).getId1().equals(region.getId1()) && beaconsSorted.get(i).getId2().equals(region.getId2()) && (beaconsSorted.get(i).getIdentifiers().size() == 2 || beaconsSorted.get(i).getId3().equals(region.getId3()))){
                beaconsSorted.remove(i);
            }
        }
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

            //Cancel notification when out of range
            ((NotificationManager)getApplicationContext().getSystemService(getApplicationContext().NOTIFICATION_SERVICE)).cancel(1012);

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


    private void getEntryNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());


        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.pionearlogo);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Green man + avaliable! Tap ezlink to extend time!")
                        //.setTicker("Green man + avaliable! Tap ezlink to extend time!")
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setWhen(System.currentTimeMillis());

        Intent startIntent = new Intent(getApplicationContext(),
                MainActivity.class);
        startIntent.putExtra("fromNotification", true);
        PendingIntent mapPendingIntent =
                PendingIntent.getActivity(getApplicationContext(), 0, startIntent, 0);


        NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.mr_ic_play_dark, "Open", mapPendingIntent).build();

        builder.extend(new NotificationCompat.WearableExtender().addAction(action));
        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_ALL;

        ((NotificationManager)getApplicationContext().getSystemService(getApplicationContext().NOTIFICATION_SERVICE)).notify(1012, notification);
    }

}