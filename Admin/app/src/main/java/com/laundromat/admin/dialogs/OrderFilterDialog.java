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
import com.laundromat.admin.model.order.OrderStatus;
import com.laundromat.admin.ui.interfaces.IOrderFilterListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class OrderFilterDialog extends DialogFragment
        implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    // Variables
    private final Map<Integer, OrderStatus> orderStatusMap = new HashMap<>();
    private OrderStatus selectedStatus = null;

    // Views
    private RadioGroup radioGroupFilter;
    private AppCompatButton buttonCancel;
    private AppCompatButton buttonApply;

    // Interfaces
    private IOrderFilterListener orderFilterListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        orderStatusMap.put(R.id.radio_button_all, null);
        orderStatusMap.put(R.id.radio_button_requested, OrderStatus.REQUESTED);
        orderStatusMap.put(R.id.radio_button_accepted, OrderStatus.ACCEPTED);
        orderStatusMap.put(R.id.radio_button_pickup_requested, OrderStatus.PICKUP_REQUESTED);
        orderStatusMap.put(R.id.radio_button_pickup_accepted, OrderStatus.PICKUP_ACCEPTED);
        orderStatusMap.put(R.id.radio_button_pickup, OrderStatus.PICK_UP);
        orderStatusMap.put(R.id.radio_button_collected, OrderStatus.COLLECTED);
        orderStatusMap.put(R.id.radio_button_in_service, OrderStatus.IN_SERVICE);
        orderStatusMap.put(R.id.radio_button_washed, OrderStatus.WASHED);
        orderStatusMap.put(R.id.radio_button_delivery_requested, OrderStatus.DELIVERY_REQUESTED);
        orderStatusMap.put(R.id.radio_button_delivery_accepted, OrderStatus.DELIVERY_ACCEPTED);
        orderStatusMap.put(R.id.radio_button_delivering, OrderStatus.DELIVERING);
        orderStatusMap.put(R.id.radio_button_completed, OrderStatus.COMPLETED);
        orderStatusMap.put(R.id.radio_button_cancelled, OrderStatus.CANCELLED);
        orderStatusMap.put(R.id.radio_button_declined, OrderStatus.DECLINED);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.dialog_filter_order, container, false);
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

    public void setListener(IOrderFilterListener orderFilterListener) {
        this.orderFilterListener = orderFilterListener;
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

        if (orderFilterListener != null) {

            orderFilterListener.onOrderFiltered(selectedStatus);
            dismiss();
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int id) {

        selectedStatus = orderStatusMap.get(id);
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
