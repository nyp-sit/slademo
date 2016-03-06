
package fypj.com.weicong.slademowatch.services;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.opencsv.CSVReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import fypj.com.weicong.sharedservices.LocationStepsSession;
import fypj.com.weicong.slademowatch.SendUpdateDbRequest;

public class LocationWatchService extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private GoogleApiClient mGoogleApiClient;
    public String TAG = LocationWatchService.class.getSimpleName();
    String uid = "";
    Location previousLocation;
    LocalBroadcastManager bm;

    public final List<String[]> readCsv(Context context , String CSV_PATH) {
        List<String[]> questionList = new ArrayList<String[]>();
        AssetManager assetManager = context.getAssets();

        try {
            InputStream csvStream = assetManager.open(CSV_PATH);
            InputStreamReader csvStreamReader = new InputStreamReader(csvStream);
            CSVReader csvReader = new CSVReader(csvStreamReader);
            String[] line;

            // throw away the header
            csvReader.readNext();

            while ((line = csvReader.readNext()) != null) {
                questionList.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return questionList;
    }
    int currentIndex = 0;
    public void fakeLocation(){
        Handler handler  = new Handler();
        final List<String[]> list = readCsv(getApplicationContext(), "coordinates.csv");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                final Location location = new Location(LocationManager.NETWORK_PROVIDER);
                location.setLatitude(Double.parseDouble(list.get(currentIndex)[1]));
                location.setLongitude(Double.parseDouble(list.get(currentIndex)[0]));
                location.setAltitude(10);
                location.setTime(System.currentTimeMillis());
                location.setAccuracy(16f);
                location.setBearing(0);
                location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());

                Log.d(TAG, "Location 1 " + Double.parseDouble(list.get(currentIndex)[1]));
                LocationServices.FusedLocationApi.setMockMode(mGoogleApiClient, true).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            LocationServices.FusedLocationApi.setMockLocation(mGoogleApiClient, location);
                            if(currentIndex != list.size()){
                                fakeLocation();
                                currentIndex ++;
                            }else{
                                Log.d(TAG, "LOCATION FAKING COMPLETED!");
                            }
                        }
                    }
                });
            }
        }, 2 * 1000);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        bm = LocalBroadcastManager.getInstance(getApplicationContext());
        mGoogleApiClient = new GoogleApiClient.Builder(LocationWatchService.this)
                //.addApi(LocationServices.API)
                .addApi(Wearable.API)  // used for data layer API
                //.addApi(LocationServices.API)
                .addConnectionCallbacks(LocationWatchService.this)
                .addOnConnectionFailedListener(LocationWatchService.this)
                .build();
        mGoogleApiClient.connect();

        return START_STICKY;
    }

    @Override
    public void onConnected(Bundle bundle) {//This is called twice, one to request for wearable api, checking if I have a connected node, second time is called for location api
        if(mGoogleApiClient.hasConnectedApi(Wearable.API)) {
            Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                @Override
                public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                    if (getConnectedNodesResult != null && getConnectedNodesResult.getNodes().size() > 0) {
                        uid = getConnectedNodesResult.getNodes().get(0).getId();
                    }
                    mGoogleApiClient.disconnect();

                    mGoogleApiClient = new GoogleApiClient.Builder(LocationWatchService.this)
                            .addApi(LocationServices.API)
                                    //.addApi(Wearable.API)  // used for data layer API
                            .addConnectionCallbacks(LocationWatchService.this)
                            .addOnConnectionFailedListener(LocationWatchService.this)
                            .build();
                    mGoogleApiClient.connect();
                }
            });
        }else {

            LocationRequest locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    //.setInterval(1000 * 60 * 2)//
                    .setFastestInterval(30 * 1000)
                    .setSmallestDisplacement(10);//Track every 10 metres

            LocationStepsSession.location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);


            //fakeLocation();
            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleApiClient, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()){
                LocationServices.FusedLocationApi
                        .removeLocationUpdates(mGoogleApiClient, this);
            }
            mGoogleApiClient.disconnect();
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        Location previousLocation = LocationWatchService.this.previousLocation;
        System.out.println("location changed");

        //Geofence changes
        String[] locations = getApplicationContext().getResources().getStringArray(fypj.com.weicong.sharedservices.R.array.locations_geofence);

        for (String targetLocation : locations) {
            String splitted[] = targetLocation.split(",");
            float[] results = new float[2];
            Location.distanceBetween(Double.parseDouble(splitted[0]), Double.parseDouble(splitted[1]), location.getLatitude(), location.getLongitude(), results);
            if (results[0] <= 10 * 1000) {
                System.out.println(results[0] + " diff distance");
                System.out.println("detect into range");
                Intent intent = new Intent("sg.edu.nyp.slademo.LOCATION_IN_RANGE");
                bm.sendBroadcast(intent);
            } else {
                System.out.println(results[0] + " diff distance more");
                Intent intent = new Intent("sg.edu.nyp.slademo.LOCATION_OUT_OF_RANGE");
                bm.sendBroadcast(intent);
            }
        }
        if(uid != null) {
            LocationStepsSession.location = location;
                String lat = String.valueOf(location.getLatitude());
                String lng = String.valueOf(location.getLongitude());
                String acc = String.valueOf(location.getAccuracy());
                String alt = String.valueOf(location.getAltitude());
                String bearing = String.valueOf(location.getBearing());
                String speed = String.valueOf(location.getSpeed());
                if (previousLocation != null) {
                    SendUpdateDbRequest.sendData("UpdateDynamoDBTask", new String[]{uid, lat, lng, acc, alt, bearing, speed, String.valueOf(previousLocation.distanceTo(location)), String.valueOf(LocationStepsSession.steps)}, getApplicationContext());
                }else {
                    SendUpdateDbRequest.sendData("UpdateDynamoDBTask", new String[]{uid, lat, lng, acc, alt, bearing, speed, "0", String.valueOf(LocationStepsSession.steps)}, getApplicationContext());
                }

        }
        LocationWatchService.this.previousLocation = location;
    }

    private boolean hasGPS() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
    }

    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);
        uid = peer.getId();

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getApplicationContext());
        Intent i = new Intent("fypj.com.weicong.slademowatch.PEER_CONNECTED");
        i.putExtra("uid", uid);
        manager.sendBroadcast(i);
    }


}