package fypj.com.weicong.sharedservices.db;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

import fypj.com.weicong.sharedservices.DynamoDBHelper;
import fypj.com.weicong.sharedservices.TrackData;

/**
 * Created by L30911 on 12/16/2015.
 */
public class UpdateDynamoDBTask extends AsyncTask<String, Void, String> {
    private String TAG = UpdateDynamoDBTask.class.getSimpleName();
    Context context;
    public UpdateDynamoDBTask(Context context){
        this.context = context;
    }
    protected String doInBackground(String... args) {
        String uid = (String) args[0];
        double lat = Double.valueOf(args[1]);
        double lng = Double.valueOf(args[2]);
        double acc = Double.valueOf(args[3]);
        double alt = Double.valueOf(args[4]);
        double bearing = Double.valueOf(args[5]);
        double speed = Double.valueOf(args[6]);
        double distance = Double.valueOf(args[7]);
        double step = Double.valueOf(args[8]);
        TrackData data = new TrackData();


        data.setId(uid);
        data.setLat(lat);
        data.setLng(lng);
        data.setAccuracy((float) acc);
        data.setAltitude(alt);
        data.setBearing((float) bearing);
        data.setSpeed((float) speed);
        data.setTimeStamp(System.currentTimeMillis());
        data.setStep((float) step);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date resultdate = new Date(System.currentTimeMillis());
        String localtime = sdf.format(resultdate);
        data.setDateTime(localtime);

        data.setDistance((float) distance);
        Log.d(TAG, "time now : "+data.getTimeStamp());
        DynamoDBHelper.context = context;
        DynamoDBHelper.save(data);
        return "";
    }
}
