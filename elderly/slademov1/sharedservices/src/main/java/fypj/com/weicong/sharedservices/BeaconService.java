package fypj.com.weicong.sharedservices;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;

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
import java.util.concurrent.ExecutionException;

import fypj.com.weicong.sharedservices.db.UpdateDetectedBeacon;
import fypj.com.weicong.sharedservices.db.UpdateDynamoDBEnterRoom;
import fypj.com.weicong.sharedservices.db.UpdateDynamoDBExitRoom;


/**
 * Created by L30912 on 29/10/2015.
 */
public class BeaconService extends Service implements BeaconConsumer, RangeNotifier, MonitorNotifier {

    BeaconManager beaconManager;
    public static final String TAG = BeaconService.class.getSimpleName();
    public static String enterRoom;
    public static String exitRoom;
    public static String localtime = "";
    public static String uid;
    long[] pattern = {0, 300, 200, 300, 200};

    private static final org.altbeacon.beacon.Region[] BEACONS = new org.altbeacon.beacon.Region[]{
            new org.altbeacon.beacon.Region("Traffic Light", Identifier.parse("0x" + new StringBuilder("edd1ebeac04e5defa017").toString()), Identifier.parse("0xca46e3d2814d"), null),
    };

    BroadcastReceiver inRangeReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            blueToothCheck();
        }
    };
    BroadcastReceiver outRangeReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                beaconManager.stopMonitoringBeaconsInRegion(region);

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
        TelephonyManager telephonyManager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        uid = telephonyManager.getDeviceId();

    }

    LocalBroadcastManager bm;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        beaconManager = BeaconManager.getInstanceForApplication(getApplicationContext());

        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        beaconManager.bind(this);

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
        region = new org.altbeacon.beacon.Region("Beacon3", Identifier.parse("0x" + new StringBuilder("edd1ebeac04e5defa017").toString()), Identifier.parse("0xca46e3d2814d"), null);
        //Region region = new Region("", null,null,null);
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
            beaconManager.startMonitoringBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBeaconServiceConnect() {
        // blueToothCheck();
        Log.d(TAG, "Bluetooth check");
    }

    Beacon currentBeacon;

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> collection, final org.altbeacon.beacon.Region region) {
        for (Beacon beacon : collection) {
            System.out.println(beacon.getDistance() + " distance");
            if (currentBeacon == null || !currentBeacon.equals(beacon)) { //shall not enter region that i am already inside of

                Intent i = new Intent("sg.edu.nyp.slademo.ENTER_REGION");
                i.putExtra("region_name", region.getUniqueId());
                i.putExtra("region_id1", region.getId1().toString());
                i.putExtra("region_id1", region.getId1().toString());
                i.putExtra("region_id2", region.getId2().toString());
                i.putExtra("region_distance", beacon.getDistance());

                bm.sendBroadcast(i);
                try {
                    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                    Criteria mFineCriteria = new Criteria();
                    mFineCriteria.setAccuracy(Criteria.ACCURACY_FINE);
                    mFineCriteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
                    mFineCriteria.setBearingAccuracy(Criteria.ACCURACY_HIGH);
                    mFineCriteria.setBearingRequired(true);

                    String provider = lm.getBestProvider(mFineCriteria, true);
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for Activity#requestPermissions for more details.
                        return;
                    }
                    Location location = lm.getLastKnownLocation(provider);


                    new UpdateDetectedBeacon(getApplicationContext()).execute(uid, String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), beacon.getId1().toString(), beacon.getId2().toString(), String.valueOf(beacon.getDistance())).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
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
        if((System.currentTimeMillis() - lastEnterTime) > durationToExpire) {
            lastEnterTime = System.currentTimeMillis();
            expiryTime = lastEnterTime + durationToExpire;

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date resultdate = new Date(System.currentTimeMillis());
            localtime = sdf.format(resultdate);

            new UpdateDynamoDBEnterRoom(getApplicationContext()).execute(uid, localtime, enterRoom, exitRoom);

            enterRoom = region.getUniqueId();
            try {
                beaconManager.setRangeNotifier(BeaconService.this);
                beaconManager.startRangingBeaconsInRegion(region);
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

            new UpdateDynamoDBExitRoom(getApplicationContext()).execute(uid, localtime, enterRoom, exitRoom);
            enterRoom = null;

            leftRegion = true;
            currentBeacon = null;
        }
    }

    @Override
    public void didDetermineStateForRegion(int i, org.altbeacon.beacon.Region region) {

    }
}