package com.laundromat.admin.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.DialogFragment;

import com.laundromat.admin.R;
import com.laundromat.admin.model.util.TripStatus;
import com.laundromat.admin.ui.interfaces.ITripFilterListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TripFilterDialog extends DialogFragment
        implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    // Variables
    private final Map<Integer, TripStatus> tripStatusMap = new HashMap<>();
    private TripStatus selectedStatus = null;

    // Views
    private RadioGroup radioGroupFilter;
    private AppCompatButton buttonCancel;
    private AppCompatButton buttonApply;

    // Interfaces
    private ITripFilterListener tripFilterListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tripStatusMap.put(R.id.radio_button_all, null);
        tripStatusMap.put(R.id.radio_button_requested, TripStatus.REQUESTED);
        tripStatusMap.put(R.id.radio_button_accepted, TripStatus.ACCEPTED);
        tripStatusMap.put(R.id.radio_button_started, TripStatus.STARTED);
        tripStatusMap.put(R.id.radio_button_arrived_source, TripStatus.ARRIVED_SOURCE);
        tripStatusMap.put(R.id.radio_button_picked_up, TripStatus.PICKED_UP);
        tripStatusMap.put(R.id.radio_button_arrived_destination, TripStatus.ARRIVED_DESTINATION);
        tripStatusMap.put(R.id.radio_button_delivered, TripStatus.DELIVERED);
        tripStatusMap.put(R.id.radio_button_completed, TripStatus.COMPLETED);
        tripStatusMap.put(R.id.radio_button_declined, TripStatus.DECLINED);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.dialog_filter_trip, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        Objects.requireNonNull(getDialog()).getWindow()
                .setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void setListener(ITripFilterListener tripFilterListener) {
        this.tripFilterListener = tripFilterListener;
    }

    private void initViews(View view) {

        buttonApply = view.findViewById(R.id.button_apply);
        buttonApply.setOnClickListener(this);

        buttonCancel = view.findViewById(R.id.button_cancel);
        buttonCancel.setOnClickListener(this);

        initRadioGroup(view);
    }

    private void initRadioGroup(View view) {

        // init radio group
        radioGroupFilter = view.findViewById(R.id.radio_group_filter);
        radioGroupFilter.setOnCheckedChangeListener(this);
    }

    private void applyFilter() {

        int id = radioGroupFilter.getCheckedRadioButtonId();

        // if nothing is selected
        if (id == -1) {

            Toast.makeText(getContext(), "No Filter Selected", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tripFilterListener != null) {

            tripFilterListener.onTripFiltered(selectedStatus);
            dismiss();
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int id) {

        selectedStatus = tripStatusMap.get(id);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_apply) {

            applyFilter();

        } else if (view.getId() == R.id.button_cancel) {

            dismiss();
        }
    }
}
