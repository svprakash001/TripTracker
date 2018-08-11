package prakash.triptracker;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class StartTripDialogFragment extends DialogFragment {


    private static final String TAG = "StartTrip";

    private EditText text_tripname;
    private String tripname;

    public interface StartTripDialogListener {
        void getTripName(String tripname);
    }

    private StartTripDialogListener startTripDialogListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            startTripDialogListener = (StartTripDialogListener) context;
        } catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Dialog dialog = super.onCreateDialog(savedInstanceState);

        View view =  getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_start_trip,null);

        text_tripname = view.findViewById(R.id.text_tripname);

        Button btn_submit = view.findViewById(R.id.btn_submit_tripname);

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tripname = text_tripname.getText().toString();
                startTripDialogListener.getTripName(tripname);
                dismiss();
            }
        });

        dialog.setContentView(view);

        return dialog;
    }
}
