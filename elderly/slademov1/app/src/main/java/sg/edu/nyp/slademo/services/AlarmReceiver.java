package sg.edu.nyp.slademo.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import fypj.com.weicong.sharedservices.DynamoDBHelper;
import fypj.com.weicong.sharedservices.TrackData;
import sg.edu.nyp.slademo.MapsActivity;

/**
 * Created by L30912 on 22/09/2015.
 */
public class AlarmReceiver extends BroadcastReceiver implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient mGoogleApiClient;

    public static float numOfSteps;
    private String uid;
    LatLng pLocation = null;
    Location location = null;
    public static final String FILE = "location.dat";
    public static final String TAG = AlarmReceiver.class.getSimpleName();
    Context ctx;

   // public static final long time = System.currentTimeMillis();

    public static String localtime = "";


    LocationManager locationManager;
    Location cPosition;
    String provider;

    @Override
    public void onReceive(Context context, Intent intent) {
        ctx = context;
        uid = intent.getStringExtra("id");
        MapsActivity activity = new MapsActivity();
        numOfSteps = activity.numOfSteps;

        Log.d(TAG, "id : " + uid);
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date resultdate = new Date(System.currentTimeMillis());
        localtime = sdf.format(resultdate);

        new UpdateDynamoDBTask().execute(uid);
        Log.d(TAG, "onReceived completed");

//        Intent eventService = new Intent(context, TestService.class);
//        context.startService(eventService);
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Connected!");
        location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);

        //Criteria for user's location

        Criteria c = new Criteria();
        c.setAccuracy(Criteria.ACCURACY_FINE);
        c.setAltitudeRequired(true);
        c.setBearingRequired(true);
        c.setCostAllowed(true);
        c.setSpeedRequired(true);
        c.setPowerRequirement(Criteria.POWER_HIGH);

        provider = locationManager.getBestProvider(c, true);
        cPosition = locationManager.getLastKnownLocation(provider);

        pLocation = getPreviousLocation();

        Log.d(TAG, pLocation.latitude + ":" + pLocation.longitude);
        try {
            setPreviousLocation(location);
        }catch (Exception e){}

        //MapsActivity.getInstance().addMarker(pLocation);
        if (location != null) {
            Toast.makeText(ctx, "Updated to DB: Lat: " + location.getLatitude() + " Lng: " + location.getLongitude(), Toast.LENGTH_SHORT).show();
        }

    }


    private LatLng getPreviousLocation() {
        LatLng latlng;
        DataInputStream fis = null;
        try {
            fis = new DataInputStream(ctx.openFileInput(FILE));
            double latitude = fis.readDouble();
            double longitude = fis.readDouble();
            latlng = new LatLng(latitude, longitude);
        } catch (Exception e) {
            latlng = new LatLng(0,0);
//            Log.d(TAG, e.getMessage());
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {}
            }
        }
        return latlng;
    }

    private void setPreviousLocation(Location location) {
        DataOutputStream fos = null;
        try {
            fos = new DataOutputStream(ctx.openFileOutput(FILE, ctx.MODE_PRIVATE));
            fos.writeDouble(location.getLatitude());
            fos.writeDouble(location.getLongitude());

        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {}
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Suspended!");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection Failed!");

    }

    private class UpdateDynamoDBTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... uid) {

            TrackData data = new TrackData();
            data.setId(uid[0]);
            while (location == null ) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.d(TAG, e.getMessage());
                }
            }
            data.setLat(location.getLatitude());
            data.setLng(location.getLongitude());
            data.setAccuracy(location.getAccuracy());
            data.setAltitude(location.getAltitude());
            data.setBearing(location.getBearing());
            data.setSpeed(location.getSpeed());
            data.setTimeStamp(System.currentTimeMillis());
            data.setStep(numOfSteps);
            data.setDateTime(localtime);
            if (pLocation.latitude != 0 && pLocation.longitude != 0) {
                float[] values = new float[3];
                Location.distanceBetween(pLocation.latitude, pLocation.longitude, location.getLatitude(), location.getLongitude(), values);
                Log.d(TAG, "Distance: " + values[0]);

                data.setDistance(values[0]);
            }
            Log.d(TAG, location.getLatitude() + ":" + location.getLongitude());

            DynamoDBHelper.save(data);
            return "";
        }
    }
}
