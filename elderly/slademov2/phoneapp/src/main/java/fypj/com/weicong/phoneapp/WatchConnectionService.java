package fypj.com.weicong.phoneapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import fypj.com.weicong.sharedservices.db.RegisterDeviceTask;


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
    GoogleApiClient client;

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
        final String[] args = new String[items.length];
        for(int i = 0 ;i < items.length; i ++){
            args[i] = items[i];
        }

        try {
            Class _class = Class.forName("fypj.com.weicong.sharedservices.db."+messageEvent.getPath().replaceFirst("/", ""));
            final AsyncTask task = (AsyncTask) _class.getDeclaredConstructor(Context.class).newInstance(getApplicationContext());
           // args[0] = PersistentSession.uid;
            if(PersistentSession.uid != null && !PersistentSession.uid.isEmpty()){
                args[0] = PersistentSession.uid;
                task.execute(args).get();
            }else{
                client = new GoogleApiClient.Builder(getBaseContext())
                        .addApi(Wearable.API)
                        .build();
                client.blockingConnect();
                Wearable.NodeApi.getConnectedNodes(client).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        if (getConnectedNodesResult != null && getConnectedNodesResult.getNodes().size() > 0) {
                            PersistentSession.uid = getConnectedNodesResult.getNodes().get(0).getId();
                            try {
                                new RegisterDeviceTask(getApplicationContext()).execute(getConnectedNodesResult.getNodes().get(0).getId()).get();

                                args[0] = PersistentSession.uid;
                                task.execute(args).get();

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                });
            }

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
