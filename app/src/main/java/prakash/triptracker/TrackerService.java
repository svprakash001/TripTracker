package prakash.triptracker;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class TrackerService extends Service {

    private static final String TAG = TrackerService.class.getSimpleName();

    public static final String STOP_TRACKING = "STOP_TRACKING";
    private String tripcode;
    private String uuid;

    private FusedLocationProviderClient client;
    private LocationCallback locationCallback;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG,"Service started");
        Bundle extras = intent.getExtras();
        tripcode = extras.getString("tripcode");
        uuid = extras.getString("uuid");

        setupBroadcast();
        buildNotification();
        sendLocationUpdates();

        return START_REDELIVER_INTENT;
    }

    /**
     * Setup a broadcast receiver
     * This will stop the service when a broadcast with the intent {@link TrackerService#STOP_TRACKING} is received
     */
    private void setupBroadcast(){

        IntentFilter intentFilter = new IntentFilter(STOP_TRACKING);

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getApplicationContext());
        manager.registerReceiver(stopReceiver,intentFilter);

    }

    /**
     * Start the service as a foreground service
     * Build a notification that will take user to the current activity in the app
     */
    private void buildNotification() {

        Log.d(TAG,"Entering build notification");


        PendingIntent broadcastIntent = PendingIntent.getService(
                this, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);

        // Create the persistent notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"CH_!")
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Tracking your location")
                .setOngoing(true)
                .setContentIntent(broadcastIntent)
                .setSmallIcon(R.mipmap.bike_launcher);

        startForeground(1, builder.build());
    }

    /**
     * Anonymous class to handle broadcast receiver callback
     */
    private final BroadcastReceiver stopReceiver = new BroadcastReceiver() {

        @Override
        /**
         * Unsubscribe from location updates
         * Unregister this broadcast receiver
         * stop the service
         */
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Received broadcast to stop tracking");

            client.removeLocationUpdates(locationCallback);

            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getApplicationContext());
            manager.unregisterReceiver(stopReceiver);

            stopSelf();
        }
    };

    /**
     * Get location updates every 2 minutes and send it to Database
     */
    private void sendLocationUpdates(){

        final boolean first_time = true;

        Log.d(TAG,"Entering send location updates");

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(2 * 60 * 1000); //Update every 2 minutes
        locationRequest.setFastestInterval(5 * 1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        client = LocationServices.getFusedLocationProviderClient(this);

        int location_permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        locationCallback = new LocationCallback(){

            @Override
            public void onLocationResult(LocationResult locationResult) {

                if(first_time){

                    Log.d(TAG,"Location data ready, broadcasting");

                    Intent intent = new Intent("LOCATION_AVAILABLE");

                    LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getApplicationContext());
                    manager.sendBroadcast(intent);
                }

                Log.d(TAG,"Updating");

                DatabaseReference rider_latlng = DatabaseRef.tripsref.child(tripcode).child("riders").child(uuid);

                Map<String,Object> latlng = new HashMap<>();

                latlng.put("lat",locationResult.getLastLocation().getLatitude());
                latlng.put("lng",locationResult.getLastLocation().getLongitude());
                latlng.put("last_updated", ServerValue.TIMESTAMP);

                rider_latlng.updateChildren(latlng);
            }
        };

        if(location_permission == PackageManager.PERMISSION_GRANTED){

            client.requestLocationUpdates(locationRequest,locationCallback,null);
        }
        //TODO- permission not granted after service is launched
        else {

        }
    }
}
