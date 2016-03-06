package fypj.com.weicong.phoneapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

/**
 * Created by L30911 on 12/16/2015.
 */
public class WatchConnectionService extends WearableListenerService {
    public String TAG = WatchConnectionService.class.getSimpleName();
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Toast.makeText(getApplicationContext(), "Start service!", Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);
        LocalBroadcastManager lm = LocalBroadcastManager.getInstance(getBaseContext());
        Intent i = new Intent("fypj.com.weicong.phoneapp.PEER_CONNECTED");
        lm.sendBroadcast(i);
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        super.onPeerDisconnected(peer);
        LocalBroadcastManager lm = LocalBroadcastManager.getInstance(getBaseContext());
        Intent i = new Intent("fypj.com.weicong.phoneapp.PEER_DISCONNECTED");
        lm.sendBroadcast(i);
        stopSelf();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        //super.onMessageReceived(messageEvent);

        Log.d(TAG, messageEvent.getPath());
        String data = new String(messageEvent.getData());
        data = data.replace("[", "").replace("]", "");

        String[] items = data.split(",");
        String[] args = new String[items.length];
        for(int i = 0 ;i < items.length; i ++){
            args[i] = items[i];
        }

        try {
            Class _class = Class.forName("fypj.com.weicong.sharedservices.db."+messageEvent.getPath().replaceFirst("/", ""));
            AsyncTask task = (AsyncTask) _class.getDeclaredConstructor(Context.class).newInstance(getApplicationContext());
            task.execute(args).get();
            Log.d(TAG, Arrays.toString(args));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
