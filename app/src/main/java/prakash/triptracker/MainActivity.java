package prakash.triptracker;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements StartTripDialogFragment.StartTripDialogListener,
        JoinTripDialogFragment.JoinTripDialogListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int FINE_LOCATION_PERMISSIONS_REQUEST = 1;

    private static final int TRIPCODE_LENGTH = 4;

    private FirebaseUser user;

    private String tripcode;

    private String tripname;

    private FirebaseAuth mauth;

    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mauth = FirebaseAuth.getInstance();

        user = mauth.getCurrentUser();

        if (user == null) {

            setContentView(R.layout.activity_username);

            Button btn_username = findViewById(R.id.btn_username);

            btn_username.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "entered onclick");
                    anonymousLogin();
                }

            });
        } else {
            renderHomePage();
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if(user!=null){
            renderBgvideo();
        }
    }

    private void anonymousLogin() {

        //Get the username
        EditText text_username = findViewById(R.id.text_username);

        final String username = text_username.getText().toString();

        Log.d(TAG, "about to login");

        mauth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        Log.d(TAG, "signin completed");

                        if (task.isSuccessful()) {

                            //Change display name to the name entered by user
                            FirebaseUser myuser = mauth.getCurrentUser();

                            Log.d(TAG, "Signedin anonymously, UUID "+myuser.toString());

                            UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(username).build();

                            myuser.updateProfile(userProfileChangeRequest);

                            //go to the main screen
                            renderHomePage();
                        }
                    }
                });
    }

    private void renderHomePage() {

        setContentView(R.layout.activity_main);

        Toolbar actionbar = findViewById(R.id.actionbar_main);
        setSupportActionBar(actionbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        renderBgvideo();

        Button start = findViewById(R.id.btn_start);
        Button join = findViewById(R.id.btn_join);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showStartTripDialog();
            }
        });

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showJoinTripDialog();
            }
        });
    }


    private void renderBgvideo(){

        VideoView videoView = findViewById(R.id.videoView);
        String path = "android.resource://"+getPackageName()+"/"+R.raw.bgvideo_bikride;
        videoView.setVideoURI(Uri.parse(path));
        videoView.setScaleY(2.0f);
        videoView.start();

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setLooping(true);
            }
        });
    }

    /**
     * Show the dialog fragment for starttrip where user can fill the trip details
     */
    private void showStartTripDialog() {
        StartTripDialogFragment startTripDialogFragment = new StartTripDialogFragment();
        startTripDialogFragment.show(getSupportFragmentManager(), "StartTripDialogFragment");
    }


    @Override
    /**
     * Get tripname from {@link StartTripDialogFragment} and pass it to {@link MainActivity#createTrip(String)}
     * @see StartTripDialogFragment.StartTripDialogListener
     */
    public void getTripName(String tripname) {

        this.tripname = tripname;

        //Generate a tripcode and check if tripcode is already taken
        tripcode = TripcodeGenerator.generateTripcode(TRIPCODE_LENGTH);

        isTripCodeTaken();

        showStartTripProgressDialog();
    }


    /**
     *Create a new trip by putting the {@link MainActivity#tripcode and tripname param} in db
     */
    private void createTrip(){

        Map<String, Object> trip = new HashMap<>();

        trip.put("tripcode", tripcode);
        trip.put("tripname", tripname);
        trip.put("timestamp", ServerValue.TIMESTAMP);

        DatabaseRef.dbref.child("trips").child(tripcode).setValue(trip)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    /**
                     * When trip is created successfully, add the current user as a rider
                     */
                    public void onSuccess(Void aVoid) {

                        Log.d(TAG,"Created trip "+tripcode);

                        hideStartTripProgressDialog();
                        showJoinTripProgressDialog();

                        joinTrip();

                        Intent map_intent = new Intent(MainActivity.this,DisplayMapActivity.class);
                        map_intent.putExtra("tripcode",tripcode);

//                        MainActivity.this.startActivity(map_intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,"Trip creation failure, tripcode : "+tripcode);
                    }
                });

    }

    private void isTripCodeTaken(){

        DatabaseReference tripsref = DatabaseRef.dbref.child("trips");

        tripsref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                while (dataSnapshot.hasChild(tripcode)){
                    tripcode = TripcodeGenerator.generateTripcode(TRIPCODE_LENGTH);
                }
                Log.d(TAG, "Final trip code generated " + tripcode);
                createTrip();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG,"Error in database while validating tripcode "+tripcode);
            }
        });
    }


    private void showStartTripProgressDialog(){

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating Trip");
        progressDialog.show();
    }

    private void hideStartTripProgressDialog(){
        progressDialog.dismiss();
    }

    /**
     *Show the dialog fragment for jointrip where user can fill the trip code and join a trip
     */
    private void showJoinTripDialog(){

        JoinTripDialogFragment joinTripDialogFragment = new JoinTripDialogFragment();
        joinTripDialogFragment.show(getSupportFragmentManager(),"JoinTripDialogFragment");
    }

    @Override
    /**
     * Get tripcode from {@link JoinTripDialogFragment} and check
      * If the tripcode is present
     * @see JoinTripDialogFragment.JoinTripDialogListener
     */
    public void getTripcode(String tripcode) {

        this.tripcode = tripcode;
        isTripcodePresent();
        showJoinTripProgressDialog();
    }

    /**
     */
    private void joinTrip(){

        FirebaseUser user = mauth.getCurrentUser();

        DatabaseReference mytrip_ref = DatabaseRef.dbref.child("trips").child(tripcode);

        final String myuser_id = user.getUid();

        String myuser_displayname = user.getDisplayName();

        Map<String, Object> rider = new HashMap<>();

        rider.put("UUID",myuser_id);  //Unique id of the rider fetched from Firebase auth
        rider.put("displayname",myuser_displayname);  //diplay name of the rider fetched from firebase auth
        rider.put("rider_joined_timestamp",ServerValue.TIMESTAMP); //when the rider was added to this ride
        rider.put("lat",null); //Placeholder for location of the rider
        rider.put("lng",null);
        rider.put("last_updated",null);

        mytrip_ref.child("riders").child(myuser_id).setValue(rider)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG,"Added user "+myuser_id+" to ride "+tripcode);
                        hideJoinTripProgressDialog();
                        prepareToTrack();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,"Failed : Adding user "+myuser_id+" to trip "+tripcode);
                    }
                });

    }

    /**
     * Generate a random {@link MainActivity#TRIPCODE_LENGTH} digit string(tripcode)
     * in client and maket sure that this code is not already taken
     * @return true if the tripcode is not already taken. False if its already taken
     */
    private void isTripcodePresent(){

        DatabaseReference tripsref = DatabaseRef.dbref.child("trips");

        tripsref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild(tripcode)){
                    Log.d(TAG,"isTripcodePresent - tripcode present in database");
                    userPresentinTrip();
                }else {
                    Log.d(TAG,"isTripCodePresent - tripcode not found in database. Tripcode invalid");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG,"Error in database while validating tripcode "+tripcode);
            }
        });

    }

    /**
     * Check if the currently logged in user is already part of this trip. If so, set
     */
    private void userPresentinTrip(){

        DatabaseReference ridersref = DatabaseRef.dbref.child("trips").child(tripcode).child("riders");

        ridersref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild(mauth.getCurrentUser().getUid())){
                    Log.d(TAG,"userPresentinTrip - user already present in the trip "+tripcode);
                }else{
                    joinTrip();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG,"Err in Database while  validating if user is already present in trip "+tripcode);
            }
        });
    }

    private void showJoinTripProgressDialog(){
       progressDialog = new ProgressDialog(this);
       progressDialog.setMessage("Joining Trip");
       progressDialog.show();
    }

    private void hideJoinTripProgressDialog(){
        progressDialog.dismiss();
    }

    /**
     *  Check location permission is granted - if it is, start tracking, otherwise request the permission
     */
    private void prepareToTrack(){

        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission == PackageManager.PERMISSION_GRANTED) {
            startTracking();
            setupBroadcast();
        } else {
            Log.d(TAG,"Requesting Location Permission");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    FINE_LOCATION_PERMISSIONS_REQUEST);
        }

    }

    @Override
    /**
     * Callback function that is triggered when user gives permission to track their location
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == FINE_LOCATION_PERMISSIONS_REQUEST && grantResults.length == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            startTracking();
            setupBroadcast();
        }else {
            Toast.makeText(this,"Please grant location access to start the trip",Toast.LENGTH_LONG).show();
            Log.d(TAG,"Location permission denied by user");
        }
    }

    /**
     *
     */
    private void startTracking(){

        Log.d(TAG,"Starting to track");
        Intent intent = new Intent(this,TrackerService.class);
        intent.putExtra("tripcode",tripcode);
        intent.putExtra("uuid",mauth.getCurrentUser().getUid());
        startService(intent);
    }

    /**
     * Setup a broadcast receiver
     * This will be called once location data of the device is available
     * Call {@link MainActivity#showMap()} from here
     */
    private void setupBroadcast(){

        IntentFilter intentFilter = new IntentFilter("LOCATION_AVAILABLE");

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getApplicationContext());
        manager.registerReceiver(locationReceiver,intentFilter);

    }

    /**
     * Anonymous class to handle broadcast reciver callback
     */
    private final BroadcastReceiver locationReceiver = new BroadcastReceiver() {

        @Override
        /**
         * Show the map
         * Unregister this broadcast receiver
         */
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Received broadcast: Device location data is ready");

            showMap();

            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getApplicationContext());
            manager.unregisterReceiver(locationReceiver);
        }
    };

    /**
     *
     */
    private void showMap(){
        Intent mapintent = new Intent(MainActivity.this,DisplayMapActivity.class);
        Bundle extras = new Bundle();
        extras.putString("tripcode",tripcode);
        extras.putString("uuid",mauth.getCurrentUser().getUid());
        extras.putString("tripname",tripname);
        mapintent.putExtras(extras);
        startActivity(mapintent);
    }

    /*private void setBackggroundImage(){

        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_username);

        *//* adapt the image to the size of the display *//*
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        Bitmap bmp = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(),R.drawable.logo),size.x,size.y,true);

        *//* fill the background ImageView with the resized image *//*
        ImageView iv_background = (ImageView) findViewById(R.id.login_bg_image);
        iv_background.setImageBitmap(bmp);
    }*/
}