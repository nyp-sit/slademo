package sg.edu.nyp.slademo.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.opencsv.CSVReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import fypj.com.weicong.sharedservices.LocationStepsSession;
import fypj.com.weicong.sharedservices.db.UpdateDynamoDBTask;
import sg.edu.nyp.slademo.StaticData;


/**
 * Created by L30911 on 12/2/2015.
 */
public class LocationService extends Service implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private String TAG = this.getClass().getSimpleName();
    GoogleApiClient client;
    Location location;
    LocalBroadcastManager bm;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        bm = LocalBroadcastManager.getInstance(getApplicationContext());
            client = new GoogleApiClient.Builder(getApplicationContext())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            client.connect();


        if(intent != null) {
            Bundle bundle = intent.getExtras();

           // uid = bundle.getString("id");

        }


        return START_STICKY;
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Connected");

        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(30 * 1000);
                //.setSmallestDisplacement(10);//Track every 10 metres

        LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);

    }

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
    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Suspended");

    }
    /*
    * This is called when there is no connectivity, data or wifi is off?
    * */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection Failed");


        //To enable offline mode, should broadcast and recieve on bluetooth side so that can start scanning regardless of location
        Intent intent = new Intent("sg.edu.nyp.slademo.DETECT_OFFLINE");
        bm.sendBroadcast(intent);
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

                Log.d(TAG, "Location 1 "+Double.parseDouble(list.get(currentIndex)[1]));
                LocationServices.FusedLocationApi.setMockMode(client, true).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            LocationServices.FusedLocationApi.setMockLocation(client, location);
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
    public void onLocationChanged(Location newLocation) {
        //Location changed

        Log.d(TAG, "location changed!");
        Location previousLocation = location;
        String[] locations = getApplicationContext().getResources().getStringArray(fypj.com.weicong.sharedservices.R.array.locations_geofence);

        for (String targetLocation : locations) {
            String splitted[] = targetLocation.split(",");
            float[] results = new float[2];
            Location.distanceBetween(Double.parseDouble(splitted[0]), Double.parseDouble(splitted[1]), newLocation.getLatitude(), newLocation.getLongitude(), results);
            if (results[0] <= 1000) {
                System.out.println(results[0] + " diff distance");
                System.out.println("detect into range");
                Intent intent = new Intent("sg.edu.nyp.slademo.LOCATION_IN_RANGE");
                bm.sendBroadcast(intent);
            } else {
                //System.out.println(results[0] + " diff distance more");
                Intent intent = new Intent("sg.edu.nyp.slademo.LOCATION_OUT_OF_RANGE");
                bm.sendBroadcast(intent);
            }
        }
        try {
            String lat = String.valueOf(newLocation.getLatitude());
            String lng = String.valueOf(newLocation.getLongitude());
            String acc = String.valueOf(newLocation.getAccuracy());
            String alt = String.valueOf(newLocation.getAltitude());
            String bearing = String.valueOf(newLocation.getBearing());
            String speed = String.valueOf(newLocation.getSpeed());
            if (previousLocation != null)
                new UpdateDynamoDBTask(getApplicationContext()).execute(StaticData.uid,lat, lng, acc, alt, bearing, speed, String.valueOf(previousLocation.distanceTo(newLocation)), String.valueOf(LocationStepsSession.steps)).get();
            else
                new UpdateDynamoDBTask(getApplicationContext()).execute(StaticData.uid, lat, lng, acc, alt, bearing, speed, "0", String.valueOf(LocationStepsSession.steps)).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
