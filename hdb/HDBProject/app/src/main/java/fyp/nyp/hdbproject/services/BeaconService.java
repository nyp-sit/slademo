package fyp.nyp.hdbproject.services;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fyp.nyp.hdbproject.LoadedInfo;
import fyp.nyp.hdbproject.aws.Beacons;
import fyp.nyp.hdbproject.aws.DynamoDBHelper;
import fyp.nyp.hdbproject.aws.Presence;

/**
 * This file handles everything dealing with beacons for this HDB project
 *
 * Flow of program :
 *
 * 1)Get beacon data from DynamoDB
 * 2)Launch beacon scanning
 *   2a) Beacon setup with parser
 *   2b) Beacon bind
 * 3)onBeaconServiceConnect called
 * 4)
 */
public class BeaconService extends Service implements BeaconConsumer, MonitorNotifier {

    private static final String TAG = BeaconService.class.getSimpleName();
    private ArrayList<Region> beacons = new ArrayList<Region>();
    private ArrayList<Beacon> beaconsSorted = new ArrayList<>();

    private ArrayList<Beacons> beaconsFromAWS = new ArrayList<>();
    BeaconManager beaconManager;
    LocalBroadcastManager bm;


    double previousDistanceChange = 0;
    double previousDistance;
    long previoustime;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Toast.makeText(getApplicationContext(), "Start Beacon service!", Toast.LENGTH_LONG).show();
       bm = LocalBroadcastManager.getInstance(getApplicationContext());

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                getListOfBeacons();
            }
        });
        thread.start();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onBeaconServiceConnect() {
        Log.d(TAG, "Scanning for bluetooth now!");

        beaconManager.setMonitorNotifier(this);
        beaconManager.setBackgroundScanPeriod(30000l);
        beaconManager.setBackgroundBetweenScanPeriod(2000l);
        try {
            beaconManager.updateScanPeriods();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        for(Region region : beacons){
            try {
                Log.d(TAG, "Montior "+region.getId1()+" , "+region.getId2());
                beaconManager.startMonitoringBeaconsInRegion(region);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void getListOfBeacons(){
        // PaginatedQueryList<Beacons> results = DynamoDBHelper.get().query(Beacons.class,queryExp);

            DynamoDBScanExpression scanExp = new DynamoDBScanExpression();
            PaginatedScanList<Beacons> list = DynamoDBHelper.get().scan(Beacons.class, scanExp);
            for (Beacons beacon : list) {
                String id3 = beacon.getBeaconId3();
                if (id3 != null && id3.toString() != null) {//handling beaconid3 null case
                    beacons.add(new Region(beacon.getId(), Identifier.parse("0x" + new StringBuilder(beacon.getBeaconId1()).toString()), Identifier.parse(beacon.getBeaconId2()), Identifier.parse(beacon.getBeaconId3())));
                } else {
                    beacons.add(new Region(beacon.getId(), Identifier.parse("0x" + new StringBuilder(beacon.getBeaconId1()).toString()), Identifier.parse(beacon.getBeaconId2()), null));
                }
                beaconsFromAWS.add(beacon);
            }
            beaconManager = BeaconManager.getInstanceForApplication(getApplicationContext());

            beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
            beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));
            beaconManager.bind(this);

    }
    protected static double calculateAccuracy(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        double ratio = rssi*1.0/txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else {
            double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
            return accuracy;
        }
    }

    @Override
    public void didEnterRegion(Region region) {
        //TODO : Test what happens when u have multiple beacons within range!!!

        Log.d(TAG,"Enter range "+region.getId1());
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
                //Add beacon into range if does not have it
                List<Beacon> beaconList = new ArrayList<>(collection);
                int foundIndex = -1;
                Log.d(TAG, beaconList.size()+" size ");
                //Log.d(TAG, "First Beacon details "+ region.getId1()+ " "+region.getId2()+" "+region.getId3()+" "+(beaconList.size() > 0 ? beaconList.get(0).getDistance() : ""));
                //Find beacon in existing unique list,
                //If exists, set it,
                //If no exist, add to list
                //Beacons have different timing for broadcast
                for (int i = 0; i < beaconList.size(); i++) {
                    foundIndex = -1;
                    for (int j = 0; j < beaconsSorted.size(); j++) {
                        if (beaconsSorted.get(j).getId1().equals(beaconList.get(i).getId1()) && beaconsSorted.get(j).getId2().equals(beaconList.get(i).getId2()) && (beaconsSorted.get(j).getIdentifiers().size() < 3 || beaconsSorted.get(j).getId3().equals(beaconList.get(i).getId3()))) {
                            //Log.d(TAG, "Found equal");
                            foundIndex = j;//Use beacon sorted index because replace in unique list
                        }
                    }

                    if (foundIndex != -1) {
                        //Found something, add
                        beaconsSorted.set(foundIndex, beaconList.get(i));
                    } else {
                        beaconsSorted.add(beaconList.get(i));
                    }
                }
                Collections.sort(beaconsSorted, new Comparator<Beacon>() {
                    @Override
                    public int compare(Beacon lhs, Beacon rhs) {
                        return Double.compare(lhs.getDistance(), rhs.getDistance());
                    }
                });

                for (int i = 0; i < beaconsSorted.size(); i++) {
                    Log.d(TAG, i + " distance " + beaconsSorted.get(i).getDistance() + " :" + beaconsSorted.get(i).getId2().toString() + " "+calculateAccuracy(beaconsSorted.get(i).getTxPower(), beaconsSorted.get(i).getRssi()));
                }
                //Log.d(TAG, "distance Next ");
                if (beaconsSorted.size() > 0) {
                    //something that prevents small changes from being recorded

                    long differenceTime = System.currentTimeMillis() - previoustime;

                    if (differenceTime > 1500) {
                        previousDistanceChange = Math.abs(beaconsSorted.get(0).getDistance() - previousDistance);
                        Log.d(TAG, " previous hcane : "+previousDistanceChange);
                        if (previousDistanceChange >= 0.005) {
                            //String beaconId = beaconsSorted.get(0).getId1().toString().replace("-", "").replace("0x", "");


                            //In the case of doing beacon detection for id saving, we need to get aws primary key id instead of id1 and id2 as composite key
                            Beacons beacon = LoadedInfo.getBeaconById(beaconsFromAWS, beaconsSorted.get(0).getId1().toString().replace("0x", "").replace("-",""), beaconsSorted.get(0).getId2().toString(), (beaconsSorted.get(0).getIdentifiers().size() == 3 ? beaconsSorted.get(0).getId3().toString() : null));
                            //for(Beacons beacon : beaconsFromAWS){
                            //  if(beacon.getBeaconId1().equals(beaconId) && beacon.getBeaconId2().equals(beaconsSorted.get(0).getId2().toString()) && (beaconsSorted.get(0).getIdentifiers().size() < 3 || beacon.getBeaconId3().equals(beaconsSorted.get(0).getId3().toString()))){
                            //Log.d(TAG, "Beacon details "+beaconsSorted.get(0).getId1()+" "+beaconsSorted.get(0).getId2()+" "+beaconsSorted.get(0).getId3());


                            Intent intent = new Intent("fyp.nyp.hdbproject.SHORTEST_CHANGED");
                            Bundle bundle = new Bundle();
                            bundle.putDouble("beaconDistance", beaconsSorted.get(0).getDistance());
                            bundle.putString("beaconId", beacon.getId());
                            Log.d(TAG, "Send intent "+beacon.getId());
                            //Log.d(TAG, "Distance given : "+beaconsSorted.get(0).getDistance());
                            //Log.d(TAG, "Distance calculate : "+ getDistance(beaconsSorted.get(0).getRssi(), beaconsSorted.get(0).getTxPower()));
                            intent.putExtras(bundle);
                            bm.sendBroadcast(intent);

                            TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(TELEPHONY_SERVICE);

                            Presence presence = new Presence();

                            presence.setTime(System.currentTimeMillis());
                            presence.setDeviceId(telephonyManager.getDeviceId());
                            presence.setDistance(bundle.getDouble("beaconDistance"));
                            presence.setNearestBeaconId(beacon.getId());
                            List<Beacon> partList = beaconsSorted;
                            if(partList.size() > 3){
                                partList = beaconsSorted.subList(0, 3);
                            }

                            for (Beacon _beacon : partList) {

                                Beacons _beacons = LoadedInfo.getBeaconById(beaconsFromAWS, _beacon.getId1().toString().replace("0x", ""), _beacon.getId2().toString(), (_beacon.getIdentifiers().size() == 3 ? _beacon.getId3().toString() : null));
                                Presence.BeaconData beaconData = new Presence.BeaconData();
                                beaconData.setBeaconID(_beacons.getId());

                                beaconData.setDistance(_beacon.getDistance());
                                presence.addBeaconData(beaconData);
                                Log.d(TAG, "Beacon 3");

                            }

                            Log.d(TAG, "Beacons sorted " + beaconsSorted.size());
                            Log.d(TAG, "Presence : " + presence.getBeaconData().size());
                            DynamoDBHelper.save(presence);


                            //     break;
                            ///    }
                            //}
                            //DynamoDBHelper.save(presence);

                            Log.d(TAG, "send nearest " + previousDistanceChange);
                        } else {
                            Log.d(TAG, "send nearest difference : " + previousDistanceChange);
                        }
                        previousDistance = beaconsSorted.get(0).getDistance();

                        previoustime = System.currentTimeMillis();
                    } else {
                        Log.d(TAG, " send time");
                    }

                }
            }
        });
        try {
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void didExitRegion(Region region) {
        synchronized (beaconsSorted) {
            for (int i= 0; i < beaconsSorted.size(); i ++) {
                Beacon beacon = beaconsSorted.get(i);
                if (beacon.getId1().equals(region.getId1())) {
                    beaconsSorted.remove(beacon);
                    Log.d(TAG, "Exit region");
                }
            }
        }
    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {

    }
}
