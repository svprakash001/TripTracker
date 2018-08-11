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

public class JoinTripDialogFragment extends DialogFragment {

    private static final String TAG = "JoinTrip";

    private EditText text_tripcode;
    private String tripcode;

    public interface JoinTripDialogListener{
        void getTripcode(String tripcode);
    }

    private JoinTripDialogListener joinTripDialogListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            joinTripDialogListener = (JoinTripDialogListener)context;
        }catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Dialog dialog =  super.onCreateDialog(savedInstanceState);

        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_join_trip,null);

        text_tripcode = view.findViewById(R.id.text_tripcode);

        Button btn = view.findViewById(R.id.btn_submit_tripcode);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tripcode = text_tripcode.getText().toString();
                joinTripDialogListener.getTripcode(tripcode);
                dismiss();
            }
        });

        dialog.setContentView(view);

        return dialog;
    }
}
