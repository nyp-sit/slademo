package fypj.com.weicong.sharedservices.db;

import android.content.Context;
import android.os.AsyncTask;

import fypj.com.weicong.sharedservices.DynamoDBHelper;
import fypj.com.weicong.sharedservices.TrackRoom;

/**
 * Created by L30911 on 12/16/2015.
 */
public class UpdateDynamoDBEnterRoom extends AsyncTask<String, Void, String> {
    Context context;
    public UpdateDynamoDBEnterRoom(Context context){
        this.context = context;
    }
    protected String doInBackground(String... args) {
        String uid = args[0];
        String localtime = args[1];
        String enterRoom = args[2];
        String exitRoom = args[3];
        if(uid != null) {

            TrackRoom data = new TrackRoom();
            data.setId(uid);
            data.setDateTime(localtime);
            data.setTimeStamp(System.currentTimeMillis());
            data.setEnterRoom(enterRoom);
            data.setExitRoom(exitRoom);

            DynamoDBHelper.context = context;
            DynamoDBHelper.save(data);
        }
        return "";
    }
}
