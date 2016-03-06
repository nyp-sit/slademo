package fypj.com.weicong.sharedservices.db;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import fypj.com.weicong.sharedservices.DeviceStatus;
import fypj.com.weicong.sharedservices.DynamoDBHelper;

public class RegisterDeviceTask extends AsyncTask<String, Void, Void> {
    Context context;
    public RegisterDeviceTask(Context context){
        this.context = context;
    }
    @Override
    protected Void doInBackground(String... params) {

        DeviceStatus deviceStatus = new DeviceStatus();
        deviceStatus.setId(params[0]);

        Log.d(RegisterDeviceTask.class.getSimpleName(), "Register device");
        DynamoDBHelper.context = context;
        DynamoDBHelper.save(deviceStatus);
        return null;
    }
}
