package fypj.com.weicong.sharedservices.db;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import fypj.com.weicong.sharedservices.BeaconDetectionData;
import fypj.com.weicong.sharedservices.DynamoDBHelper;

/**
 * Created by L30911 on 12/16/2015.
 */
public class UpdateDetectedBeacon extends AsyncTask<String, Void, Void> {
    private Context context;
    public UpdateDetectedBeacon(Context context){
        this.context = context;
    }
    @Override
    protected Void doInBackground(String... params) {
        if(params[0] != null) {

            String id = params[0];
            String lat = params[1];
            String lng = params[2];
            String id1 = params[3];
            String id2 = params[4];
            String distance = params[5];
            Log.d(UpdateDetectedBeacon.class.getSimpleName(), id);

            BeaconDetectionData data = new BeaconDetectionData();

            data.setId(id);
            data.setLat(Double.valueOf(lat));
            data.setLng(Double.valueOf(lng));
            data.setId1(id1);
            data.setId2(id2);
            data.setDistance(Double.valueOf(distance));
            data.setTimestamp(System.currentTimeMillis());

            DynamoDBHelper.context = context;
            DynamoDBHelper.save(data);
        }
        return null;
    }
}
