package prakash.triptracker;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * A class to hold all the database reference objects of the project
 */
class DatabaseRef {

    private static final FirebaseDatabase db = FirebaseDatabase.getInstance();
    public static final DatabaseReference dbref = db.getReference();
    public static final DatabaseReference tripsref = dbref.child("trips");
}
