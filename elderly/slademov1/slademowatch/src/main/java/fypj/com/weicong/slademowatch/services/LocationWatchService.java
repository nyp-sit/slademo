
package fypj.com.weicong.slademowatch.services;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import fypj.com.weicong.sharedservices.LocationStepsSession;
import fypj.com.weicong.slademowatch.SendUpdateDbRequest;

public class LocationWatchService extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private GoogleApiClient mGoogleApiClient;
    public String TAG = LocationWatchService.class.getSimpleName();
    String uid = "";
    Location previousLocation;
    LocalBroadcastManager bm;

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
    public void onConnected(Bundle bundle) {
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
                    .setInterval(2000)
                    .setFastestInterval(1000)
                    .setSmallestDisplacement(1);
            LocationStepsSession.location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

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
            if (results[0] <= 500) {
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