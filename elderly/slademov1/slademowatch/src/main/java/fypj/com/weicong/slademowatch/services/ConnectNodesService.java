package fypj.com.weicong.slademowatch.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;

import fypj.com.weicong.slademowatch.MySqlLiteHelper;
import fypj.com.weicong.slademowatch.SendUpdateDbRequest;
import fypj.com.weicong.slademowatch.UpdateDbRequestEntity;

/**
 * Created by L30911 on 12/17/2015.
 */
public class ConnectNodesService extends Service {

    LocalBroadcastManager bm;
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MySqlLiteHelper sqlLiteHelper = new MySqlLiteHelper(getApplicationContext());
            ArrayList<UpdateDbRequestEntity> list = sqlLiteHelper.fetchEntry();
            Log.d(ConnectNodesService.class.getSimpleName(), "New offline "+list.size()+" actions!");

            for(UpdateDbRequestEntity entity : list){
                String[] items = entity.getArgs().replace("[", "").replace("]","").split(",");
                SendUpdateDbRequest.sendData(entity.getPath(), items, getApplicationContext());
                Log.d(ConnectNodesService.class.getSimpleName(), "Pushing cached actions");
            }
        }
    };
    @Override
    public void onCreate() {
        super.onCreate();
        bm = LocalBroadcastManager.getInstance(getApplicationContext());

        bm.registerReceiver(receiver, new IntentFilter("fypj.com.weicong.slademowatch.PEER_CONNECTED"));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
