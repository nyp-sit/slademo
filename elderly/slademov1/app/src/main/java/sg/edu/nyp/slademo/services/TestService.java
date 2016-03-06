package sg.edu.nyp.slademo.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import sg.edu.nyp.slademo.R;


/**
 * Created by L30912 on 26/10/2015.
 */
public class TestService extends Service{

    int REQUEST_CODE = 1;
    public static int INTERVAL = 60000 * 5; // 1 min * 5
    private PendingIntent pendingIntent;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Intent alarmIntent = new Intent(TestService.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(TestService.this, 0, alarmIntent, 0);
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), INTERVAL, pendingIntent);
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "TestService onDestroy()", Toast.LENGTH_SHORT).show();
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
        cancelNotification(this, 1);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//        ThreadLoop tl = new ThreadLoop();
       // tl.start();
        Log.e("TAG", "onStartCommand running...");
        getCompatNotification();
        //Toast.makeText(this, "Service onStartCommand started", Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }

    private void getCompatNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.pionearlogo);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Pio(Near)™ Tracking Service")
                .setTicker("Tracking Service Started!")
                .setContentText("Tracking enabled")
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128 ,false))
                .setWhen(System.currentTimeMillis());
        Intent startIntent = new Intent(getApplicationContext(),
                TestService.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this, REQUEST_CODE, startIntent, 0);
        builder.setContentIntent(contentIntent);
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(1, notification);
    }

//    private class ThreadLoop extends Thread{
//        @Override
//        public void run() {
//            super.run();
//            try{
//                sleep(1000 * 30);
//
//
//            }catch(Exception e){
//                e.getMessage();
//            }
//        }
//    }

    public static void cancelNotification(Context ctx, int notifyId) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
        nMgr.cancel(notifyId);
    }



//    @Override
//    public void onTaskRemoved(Intent rootIntent) {
//        Intent intent = new Intent(this, MapsActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
//    }
}
