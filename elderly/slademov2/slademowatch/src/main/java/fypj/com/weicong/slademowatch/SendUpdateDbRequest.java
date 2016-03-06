package fypj.com.weicong.slademowatch;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.Arrays;

import fypj.com.weicong.sharedservices.LocationStepsSession;

/**
 *
 * Updates aws database making use of wearable and handheld's connection
 * Uses DataLayer API from Android Wear SDK
 *
 * Created by L30911 on 12/16/2015.
 */
public class SendUpdateDbRequest {
    private static String TAG = SendUpdateDbRequest.class.getSimpleName();
    public static void sendData(String path, final String[] data, Context context){

        if(path.indexOf("/") == -1)
            path = "/"+path;


        /*PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(path);
        putDataMapRequest.getDataMap().putStringArray("arguments", data);
        PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
        final PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(client, putDataRequest);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Status status = pendingResult.await().getStatus();
                if(status.isSuccess()){
                    Log.d(TAG, "successfuly");
                }
            }
        }).start();*/
        if(data[0] == null)
            data[0] = LocationStepsSession.id;
        if(data[0] != null)
            data[0] = data[0].replace("-","");
        Log.d(TAG, path);
        new SendDataToThread(path, Arrays.toString(data), context).start();
    }
    static class SendDataToThread extends Thread{
        @Override
        public void run() {

            client = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {
                            Wearable.NodeApi.getConnectedNodes(client).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                                @Override
                                public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                                    if(getConnectedNodesResult.getNodes().size() > 0){


                                        for(final Node node : getConnectedNodesResult.getNodes()){
                                            Wearable.MessageApi.sendMessage(client, node.getId(), path, message.getBytes()).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                                                @Override
                                                public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                                    if (sendMessageResult.getStatus().isSuccess()) {

                                                        Log.v(TAG, path+" Message: {" + message + "} sent to: " + node.getDisplayName());
                                                    }
                                                    else {
                                                        // Log an error
                                                        Log.v(TAG, "ERROR: failed to send Message");
                                                    }
                                                }
                                            });
                                        }
                                    }else{

                                        Log.d(TAG, "No device connected, not able to push to internet, as such, record and push later");
                                        MySqlLiteHelper sqlLiteHelper = new MySqlLiteHelper(context);
                                        sqlLiteHelper.putEntry(path,message);
                                    }
                                }
                            });

                        }

                        @Override
                        public void onConnectionSuspended(int i) {

                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {

                        }
                    })
                    .addApi(Wearable.API)
                    .build();
            client.connect();
        }

        String path, message;
        GoogleApiClient client;
        Context context;
        public SendDataToThread(String path, String message, Context context) {
            super();
            this.path = path;
            this.message = message;
            this.context = context;
        }
    }
}
