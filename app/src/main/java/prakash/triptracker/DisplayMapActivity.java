package prakash.triptracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;

public class DisplayMapActivity extends AppCompatActivity implements OnMapReadyCallback, LeaveTripBottomsheetDialogFragment.LeaveTripInterface {

    private static final String TAG = DisplayMapActivity.class.getSimpleName();

    private GoogleMap mMap;
    private String tripcode;
    private String uuid;

    private DatabaseReference riderref;
    private ChildEventListener childEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_map);

        Intent intent = getIntent();
        tripcode = intent.getStringExtra("tripcode");
        String tripname = intent.getStringExtra("tripname");
        uuid = intent.getStringExtra("uuid");

        TextView text_tripname = findViewById(R.id.toolbar_tripname);
        TextView text_tripcode = findViewById(R.id.toolbar_tripcode);

        text_tripname.setText(tripname);
        text_tripcode.setText("Tripcode - " +tripcode);

        Toolbar actionbar = findViewById(R.id.map_toolbar);
        setSupportActionBar(actionbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        Button btn_leave = findViewById(R.id.btn_leave);
        Button btn_share = findViewById(R.id.btn_share);


        btn_leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "User requested to leave the trip");

                LeaveTripBottomsheetDialogFragment leave_trip = new LeaveTripBottomsheetDialogFragment();
                Bundle args = new Bundle();
                args.putString("tripcode", tripcode);
                args.putString("uuid", uuid);
                leave_trip.setArguments(args);
                leave_trip.show(getSupportFragmentManager(), "LEAVE_TRIP");
            }
        });
    }

    @Override
    /**
     * Don't go back to previous activty when back button is pressed.
     * The only way for user to go back to the previous activity is to leave this trip by pressing 'leave' button
     * Instead just minimize the app
     */
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        subscribeTrip();
    }

    private void subscribeTrip() {

        riderref = DatabaseRef.tripsref.child(tripcode).child("riders");

        UpdateMap updateMap = new UpdateMap(this,mMap);

        childEventListener = riderref.addChildEventListener(updateMap);
    }

    @Override
    /**
     * Remove user from this ride group in db
     * Remove the child event listerner for this trip
     * Stop the Location trackerservice
     * Close the Map activity
     */
    public void leaveTrip() {

        DatabaseReference mytrip_riders = DatabaseRef.tripsref.child(tripcode).child("riders");
        mytrip_riders.child(uuid).removeValue();
        Log.d(TAG, "Database - Removed user with uuid " + uuid + " from trip , tripcode " + tripcode);

        riderref.removeEventListener(childEventListener);

        Intent intent = new Intent(TrackerService.STOP_TRACKING);
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getApplicationContext());
        manager.sendBroadcast(intent);

        finish();
    }
}
