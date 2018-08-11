package prakash.triptracker;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class LeaveTripBottomsheetDialogFragment extends BottomSheetDialogFragment {

    private static final String TAG = LeaveTripBottomsheetDialogFragment.class.getSimpleName();

    private LeaveTripInterface leaveTripInterface;

    public LeaveTripBottomsheetDialogFragment() {
    }

    /**
     * Interface to communicate with the Activity
     */
    public interface LeaveTripInterface{
        void leaveTrip();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog =  super.onCreateDialog(savedInstanceState);

        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_bottom_sheet_dialog_leave_trip,null);

        dialog.setContentView(view);


        Button btn_confirm_leave = view.findViewById(R.id.confirm_leave);

        Button btn_go_back = view.findViewById(R.id.go_back);

        btn_go_back.setOnClickListener(new View.OnClickListener() {
            @Override
            /**
             * Abort removal from the trip
             */
            public void onClick(View view) {
                Log.d(TAG,"Leave trip abort");
                dismiss();
            }
        });

        btn_confirm_leave.setOnClickListener(new View.OnClickListener() {
            @Override
            /**
             * Remove this rider from the trip
             */
            public void onClick(View view) {
                Log.d(TAG,"User confirmed removal from trip");
                leaveTripInterface.leaveTrip();
                dismiss();
            }
        });

        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            leaveTripInterface = (LeaveTripInterface)context;
        }catch (Exception e){
            Log.d(TAG,"Error trying to catch ");
        }
    }

}
