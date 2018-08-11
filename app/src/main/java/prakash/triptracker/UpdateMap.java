package prakash.triptracker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import java.util.HashMap;


class UpdateMap implements ChildEventListener {

    private static final String TAG = UpdateMap.class.getSimpleName();
    private final HashMap<String, Marker> mMarkers = new HashMap<>();

    private final GoogleMap map;
    private final Context context;

    public UpdateMap(Context context,GoogleMap map) {
        this.context = context;
        this.map = map;
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

        if(dataSnapshot.hasChild("lat"))
            setMarker(dataSnapshot);
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        setMarker(dataSnapshot);
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

        HashMap<String,Object> rider = (HashMap<String, Object>)dataSnapshot.getValue();
        String name = (String) rider.get("displayname");
        Toast.makeText(context,name,Toast.LENGTH_LONG).show();

        String key = dataSnapshot.getKey();
        mMarkers.get(key).remove();
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) { }

    @Override
    public void onCancelled(DatabaseError databaseError) { }

    private void setMarker(DataSnapshot dataSnapshot){

        Log.d(TAG,dataSnapshot.getValue().toString());

        String key = dataSnapshot.getKey();

        HashMap<String,Object> rider    =  (HashMap<String,Object>) dataSnapshot.getValue();

        Double lat = (Double) rider.get("lat");
        Double lng = (Double) rider.get("lng");
        String name = (String) rider.get("displayname");

        LatLng rider_loc = new LatLng(lat,lng);

        if(!mMarkers.containsKey(key)){
            mMarkers.put(key,map.addMarker(new MarkerOptions().position(rider_loc).title(name)
                    .icon(vectorToBitmap(context, Color.parseColor("#000000")))));
        }else{
            mMarkers.get(key).setPosition(rider_loc);
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for(Marker marker : mMarkers.values()){
            builder.include(marker.getPosition());
        }

        map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 20), 100, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                map.animateCamera(CameraUpdateFactory.zoomTo(16.0f));
            }

            @Override
            public void onCancel() {

            }
        });
    }

    /**
     * Currently we can't use a vector directly as icon in android maps
     * This is a workaround to convert the vector to bitmap image
     * @param context
     * @param color
     * @return Bitmaap image created from vector asset
     */
    private BitmapDescriptor vectorToBitmap(Context context, @ColorInt int color) {

        Drawable vectorDrawable = ContextCompat.getDrawable(context, R.drawable.ic_bike);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        DrawableCompat.setTint(vectorDrawable, color);
        vectorDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
