package sg.edu.nyp.slademo.services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

import fypj.com.weicong.sharedservices.LocationStepsSession;
import fypj.com.weicong.sharedservices.db.UpdateDynamoDBTask;


/**
 * Created by L30911 on 12/2/2015.
 */
public class LocationService_Reference extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    LocationManager lm;
    Location previousLocation;
    String TAG = LocationService_Reference.class.getSimpleName();
    String uid = "";
    LocalBroadcastManager bm;

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //TODO: load location from previous place!
        bm = LocalBroadcastManager.getInstance(this);

        Criteria mFineCriteria = new Criteria();
        mFineCriteria.setAccuracy(Criteria.ACCURACY_FINE);
        mFineCriteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        mFineCriteria.setBearingAccuracy(Criteria.ACCURACY_HIGH);
        mFineCriteria.setBearingRequired(true);

        if (intent != null) {
            Bundle bundle = intent.getExtras();
            uid = bundle.getString("id");
            lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            final String provider = lm.getBestProvider(mFineCriteria, true);
            // getting GPS status
            boolean isGPSEnabled = lm
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);


            // getting network status
            boolean isNetworkEnabled = lm
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGPSEnabled && !isNetworkEnabled) {
                Toast.makeText(getApplicationContext(), "Unable to get location", Toast.LENGTH_LONG).show();
            } else {

                lm.addTestProvider(LocationManager.GPS_PROVIDER, false, false,
                        false, false, true, false, false, 0, 5);
                lm.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);

                lm.addTestProvider(LocationManager.NETWORK_PROVIDER, false, false,
                        false, false, true, false, false, 0, 5);
                lm.setTestProviderEnabled(LocationManager.NETWORK_PROVIDER, true);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.
                    return 1;
                }

                if (previousLocation == null) {
                    previousLocation = lm.getLastKnownLocation(provider);
                }


                lm.requestLocationUpdates(provider, 1000, 1, new LocationListener() {


                    @Override
                    public void onLocationChanged(Location location) {

                        Location previousLocation = LocationService_Reference.this.previousLocation;
                        System.out.println("location changed");

                        //Geofence changes
                        String[] locations = getApplicationContext().getResources().getStringArray(fypj.com.weicong.sharedservices.R.array.locations_geofence);

                        for (String targetLocation : locations) {
                            String splitted[] = targetLocation.split(",");
                            float[] results = new float[2];
                            Location.distanceBetween(Double.parseDouble(splitted[0]), Double.parseDouble(splitted[1]), location.getLatitude(), location.getLongitude(), results);
                            if (results[0] <= 1000) {
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
                        try {
                            LocationStepsSession.location = location;

                            String lat = String.valueOf(location.getLatitude());
                            String lng = String.valueOf(location.getLongitude());
                            String acc = String.valueOf(location.getAccuracy());
                            String alt = String.valueOf(location.getAltitude());
                            String bearing = String.valueOf(location.getBearing());
                            String speed = String.valueOf(location.getSpeed());
                            if (previousLocation != null)
                                new UpdateDynamoDBTask(getApplicationContext()).execute(uid,lat, lng, acc, alt, bearing, speed, String.valueOf(previousLocation.distanceTo(location)), String.valueOf(LocationStepsSession.steps)).get();
                            else
                                new UpdateDynamoDBTask(getApplicationContext()).execute(uid, lat, lng, acc, alt, bearing, speed, "0", String.valueOf(LocationStepsSession.steps)).get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                        LocationService_Reference.this.previousLocation = location;
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {

                    }

                    @Override
                    public void onProviderDisabled(String provider) {

                    }
                });
            }
        }
        return START_STICKY;
    }


}
