package fyp.nyp.hdbproject.aws;

import android.os.AsyncTask;

/**
 * Created by L30911 on 2/1/2016.
 */
public class PostDeviceStatus extends AsyncTask<String, Void , Void>{

    @Override
    protected Void doInBackground(String... params) {

        DeviceStatus status = new DeviceStatus();
        status.setId(params[0]);
        status.setTime(System.currentTimeMillis());
        status.setStatus(params[1]);

        DynamoDBHelper.save(status);

        return null;
    }
}
