package com.laundromat.merchant.dialogs;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.functions.FirebaseFunctions;
import com.google.gson.Gson;
import com.laundromat.merchant.R;
import com.laundromat.merchant.activities.OrderActivity;
import com.laundromat.merchant.activities.OrderRequestActivity;
import com.laundromat.merchant.model.order.OrderStatus;
import com.laundromat.merchant.prefs.Session;
import com.laundromat.merchant.ui.interfaces.IMenuCategoryCreatedListener;
import com.laundromat.merchant.ui.interfaces.IOrderStatusUpdatedListener;
import com.laundromat.merchant.utils.GsonUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChangeOrderStatusDialog extends DialogFragment
        implements View.OnClickListener {

    // Variables
    Map<Integer, OrderStatus> orderStatusMap = new HashMap<>();
    private OrderStatus currentStatus;

    // Views
    private RadioGroup radioGroupOrderStatus;
    private RadioButton radioButtonInService;
    private RadioButton radioButtonWashed;
    private RadioButton radioButtonCancelled;
    private AppCompatButton buttonCancel;
    private AppCompatButton buttonUpdate;
    private LinearLayout layoutButtons;

    // Interfaces
    private IOrderStatusUpdatedListener orderStatusUpdatedListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            Gson gson = new Gson();
            String orderStatusJson = getArguments().getString("current_status");

            currentStatus = gson.fromJson(orderStatusJson, OrderStatus.class);
        }

        orderStatusMap.put(R.id.radio_button_in_service, OrderStatus.IN_SERVICE);
        orderStatusMap.put(R.id.radio_button_washed, OrderStatus.WASHED);
        orderStatusMap.put(R.id.radio_button_cancelled, OrderStatus.CANCELLED);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.dialog_change_order_status, container, false);
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

    public void setListener(IOrderStatusUpdatedListener orderStatusUpdatedListener) {
        this.orderStatusUpdatedListener = orderStatusUpdatedListener;
    }

    private void initViews(View view) {

        buttonUpdate = view.findViewById(R.id.button_update);
        buttonUpdate.setOnClickListener(this);

        buttonCancel = view.findViewById(R.id.button_cancel);
        buttonCancel.setOnClickListener(this);

        layoutButtons = view.findViewById(R.id.layout_buttons);
        layoutButtons.setVisibility(View.GONE);

        initRadioGroup(view);
    }

    private void initRadioGroup(View view) {

        // init radio group
        radioGroupOrderStatus = view.findViewById(R.id.radio_group_order_status);
        radioButtonInService = view.findViewById(R.id.radio_button_in_service);
        radioButtonWashed = view.findViewById(R.id.radio_button_washed);
        radioButtonCancelled = view.findViewById(R.id.radio_button_cancelled);

        // enable/disable according to status
        switch (currentStatus) {

            case ACCEPTED:
                radioButtonCancelled.setEnabled(true);
                layoutButtons.setVisibility(View.VISIBLE);
                break;

            case COLLECTED:
            case IN_SERVICE:
                radioButtonInService.setEnabled(true);
                radioButtonWashed.setEnabled(true);
                layoutButtons.setVisibility(View.VISIBLE);
                break;

            case WASHED:
                radioButtonWashed.setEnabled(true);
                break;
        }
    }

    private void updateOrderStatus() {

        int id = radioGroupOrderStatus.getCheckedRadioButtonId();

        // if nothing is selected
        if (id == -1) {

            Toast.makeText(getContext(), "Please select a status", Toast.LENGTH_SHORT).show();
            return;
        }

        // if no change has occurred
        if (orderStatusMap.get(id).equals(currentStatus)) {

            Toast.makeText(getContext(), "Nothing to update", Toast.LENGTH_SHORT).show();
            return;
        }

        // if cancelled was clicked
        if (orderStatusMap.get(id).equals(OrderStatus.CANCELLED)) {

            AlertDialog.Builder alert;
            alert = new AlertDialog.Builder(getContext());
            alert.setTitle("Cancel Order ?");

            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int whichButton) {

                    dialog.dismiss();

                    if (orderStatusUpdatedListener != null) {

                        dismiss();

                        orderStatusUpdatedListener
                                .onOrderStatusUpdated(orderStatusMap.get(id));
                    }
                }
            });

            alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    dialogInterface.dismiss();
                }
            });

            alert.show();
        }
        // change status and update in cloud + views
        else {

            if (orderStatusUpdatedListener != null) {

                dismiss();
                orderStatusUpdatedListener
                        .onOrderStatusUpdated(orderStatusMap.get(id));
            }
        }
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_update) {

            updateOrderStatus();

        } else if (view.getId() == R.id.button_cancel) {

            dismiss();
        }
    }
}
