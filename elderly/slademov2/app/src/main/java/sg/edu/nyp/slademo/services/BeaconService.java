package sg.edu.nyp.slademo.services;

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
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

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
import java.util.concurrent.ExecutionException;

import fypj.com.weicong.sharedservices.db.UpdateDetectedBeacon;
import fypj.com.weicong.sharedservices.db.UpdateDynamoDBEnterRoom;
import fypj.com.weicong.sharedservices.db.UpdateDynamoDBExitRoom;
import sg.edu.nyp.slademo.R;
import sg.edu.nyp.slademo.StaticData;


/**
 * Created by L30912 on 29/10/2015.
 */
public class BeaconService extends Service implements BeaconConsumer, RangeNotifier, MonitorNotifier, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    BeaconManager beaconManager;
    public static final String TAG = BeaconService.class.getSimpleName();
    public static String enterRoom;
    public static String exitRoom;
    public static String localtime = "";
    long[] pattern = {0, 300, 200, 300, 200};

    private static final org.altbeacon.beacon.Region[] TRAFFIC_LIGHTS = new org.altbeacon.beacon.Region[]{
            new org.altbeacon.beacon.Region("Room", Identifier.parse("0x"+new StringBuilder("edd1ebeac04e5defa017")), Identifier.parse("0xdfff2341482f"),null),
            //new org.altbeacon.beacon.Region("WGX", Identifier.parse("0x"+new StringBuilder("e2c56db5dffb48d2b060d0f5a71096e0")), Identifier.parse("0"), Identifier.parse("1"))
    };
    private static final org.altbeacon.beacon.Region[] BEACONS = new org.altbeacon.beacon.Region[]{
            new org.altbeacon.beacon.Region("Room", Identifier.parse("0x"+new StringBuilder("edd1ebeac04e5defa017")), Identifier.parse("0xdfff2341482f"),null),
            new org.altbeacon.beacon.Region("Room", Identifier.parse("0x"+new StringBuilder("e2c56db5dffb48d2b060d0f5a71096e0")), Identifier.parse("0"), Identifier.parse("1")),
            new org.altbeacon.beacon.Region("Room 2", Identifier.parse("0x"+new StringBuilder("e2c56db5dffb48d2b060d0f5a71096e0")), Identifier.parse("3"), Identifier.parse("3"))
    };

    BroadcastReceiver inRangeReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "In range reciever");
            blueToothCheck();
        }
    };
    BroadcastReceiver outRangeReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {

                for(Region beacon : BEACONS){
                    beaconManager.stopMonitoringBeaconsInRegion(beacon);
                }

                if (currentBeacon != null) {
                    Intent i = new Intent("sg.edu.nyp.slademo.EXIT_REGION");
                    i.putExtra("region_id1", currentBeacon.getId1().toString());
                    i.putExtra("region_id2", currentBeacon.getId2().toString());
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
        //TelephonyManager telephonyManager =
         //       (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //uid = telephonyManager.getDeviceId();

    }

    LocalBroadcastManager bm;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        beaconManager = BeaconManager.getInstanceForApplication(getApplicationContext());

        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));//Enable iBeacon

        beaconManager.bind(this);

        bm = LocalBroadcastManager.getInstance(getApplicationContext());

        bm.registerReceiver(changedDelayReciever, new IntentFilter("sg.edu.nyp.slademo.CHANGED_DELAY"));
        bm.registerReceiver(inRangeReciever, new IntentFilter("sg.edu.nyp.slademo.LOCATION_IN_RANGE"));
       // bm.registerReceiver(outRangeReciever, new IntentFilter("sg.edu.nyp.slademo.LOCATION_OUT_OF_RANGE"));


        SharedPreferences prefs = getSharedPreferences("sg.edu.nyp.slademo", Context.MODE_PRIVATE);
        String delay = prefs.getString("time_delay", "");
        if (!delay.equals(""))
            durationToExpire = (int) (Double.parseDouble(delay) * 1000 * 60);
        else
            durationToExpire = 1000 * 60 * 2;
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



    public void blueToothCheck() {
        beaconManager.setMonitorNotifier(BeaconService.this);
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
        Log.d(TAG, "Bluetooth check");

        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(getApplicationContext().LOCATION_SERVICE);
        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        //if(!enabled) {//offline support
            blueToothCheck();
        //}
    }

    Beacon currentBeacon;
    GoogleApiClient mGoogleApiClient;
    private ArrayList<Beacon> beaconsSorted = new ArrayList<>();
    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> collection, final org.altbeacon.beacon.Region region) {
        int foundIndex = -1;
        for (Beacon beacon : collection) {

            foundIndex = -1;
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

            if (currentBeacon == null || !currentBeacon.equals(beacon)) { //shall not enter region that i am already inside of
                boolean isTraffic = false;
                for(int i = 0; i < TRAFFIC_LIGHTS.length;i++){
                    Region region1 = TRAFFIC_LIGHTS[i];

                    if(region1.getId1().equals(beacon.getId1()) && region1.getId2().equals(beacon.getId2()) &&  (region1.getId3() == null || region1.getId3().equals(beacon.getId3()))){
                        isTraffic = true;
                        break;
                    }
                }

                if(isTraffic && ((System.currentTimeMillis() - lastEnterTime) > durationToExpire)) {
                    Intent i = new Intent("sg.edu.nyp.slademo.ENTER_REGION");
                    i.putExtra("region_name", region.getUniqueId());
                    i.putExtra("region_id1", region.getId1().toString());
                    i.putExtra("region_id1", region.getId1().toString());
                    i.putExtra("region_id2", region.getId2().toString());
                    i.putExtra("region_distance", beacon.getDistance());
                    getEntryNotification();
                    bm.sendBroadcast(i);


                    lastEnterTime = System.currentTimeMillis();
                    expiryTime = lastEnterTime + durationToExpire;

                    currentBeacon = beacon;
                }

                mGoogleApiClient = new GoogleApiClient.Builder(BeaconService.this)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(BeaconService.this)
                        .addOnConnectionFailedListener(BeaconService.this)
                        .build();
                mGoogleApiClient.connect();

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
           // try {//   beaconManager.stopRangingBeaconsInRegion(region);
                //beaconManager.stopMonitoringBeaconsInRegion(region);
           // } catch (RemoteException e) {
             //   e.printStackTrace();
           // }



        }
    }
    long lastEnterTime = 0;
    long expiryTime = 0;

    int durationToExpire; //= 1000 * 60 * 2;
    @Override
    public void didEnterRegion(org.altbeacon.beacon.Region region) {
        long currentTime = System.currentTimeMillis();
        Log.d(TAG, " Enter  : "+region.getId2()+" , "+region.getId3());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date resultdate = new Date(System.currentTimeMillis());
        localtime = sdf.format(resultdate);

        new UpdateDynamoDBEnterRoom(getApplicationContext()).execute(StaticData.uid, localtime, enterRoom, exitRoom);

        enterRoom = region.getUniqueId();
        try {
            beaconManager.setRangeNotifier(BeaconService.this);
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    boolean leftRegion = true;
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

            new UpdateDynamoDBExitRoom(getApplicationContext()).execute(StaticData.uid, localtime, enterRoom, exitRoom);
            enterRoom = null;

            leftRegion = true;
            currentBeacon = null;
        }
    }

    @Override
    public void didDetermineStateForRegion(int i, org.altbeacon.beacon.Region region) {

    }
    Bitmap icon;
    private void getEntryNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());

        //if(icon == null){
       //    icon = BitmapFactory.decodeResource(getResources(), R.drawable.pionearlogo);
        //}
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Pio(Near)™ Tracking Service")
                .setTicker("Green man + avaliable! Tap ezlink to extend time!")
                //.setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128 ,false))
                .setWhen(System.currentTimeMillis());

        //Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        // builder.setSound(alarmSound);

        //builder.setVibrate(new long[]{1000, 1000});

        Intent startIntent = new Intent(getApplicationContext(),
                TestService.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                getApplicationContext(), 2, startIntent, 0);
        builder.setContentIntent(contentIntent);
        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_ALL;
        ((NotificationManager)getApplicationContext().getSystemService(getApplicationContext().NOTIFICATION_SERVICE)).notify(2, notification);
        //icon.recycle();
       // icon = null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null)
            try {
                new UpdateDetectedBeacon(getApplicationContext()).execute(StaticData.uid, String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), beaconsSorted.get(0).getId1().toString(), beaconsSorted.get(0).getId2().toString(), String.valueOf(beaconsSorted.get(0).getDistance())).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}