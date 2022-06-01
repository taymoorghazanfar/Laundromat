package com.laundromat.merchant.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.laundromat.merchant.R;
import com.laundromat.merchant.model.washable.WashableItem;
import com.laundromat.merchant.ui.adapters.SelectedServicesRecyclerViewAdapter;
import com.laundromat.merchant.ui.adapters.ServicesRecyclerViewAdapter;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class ShowMenuItemDialog extends DialogFragment
        implements View.OnClickListener {

    // Constants
    private static final String MENU_ITEM = "menu_item";

    // Variables
    private WashableItem menuItem;

    // Views
    private ImageView imageViewItem;
    private TextView textViewName;
    private RecyclerView recyclerViewServices;
    private Button buttonDismiss;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            menuItem = getArguments().getParcelable(MENU_ITEM);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.dialog_show_menu_item, container, false);
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

    private void initViews(View view) {

        recyclerViewServices = view.findViewById(R.id.recycler_view_services);

        imageViewItem = view.findViewById(R.id.image_view_item);
        Picasso.get()
                .load(menuItem.getImageUrl())
                .into(imageViewItem);

        textViewName = view.findViewById(R.id.text_view_name);
        textViewName.setText(menuItem.getName());

        buttonDismiss = view.findViewById(R.id.button_dismiss);

        buttonDismiss.setOnClickListener(this);

        initServicesRecyclerView();
    }

    private void initServicesRecyclerView() {

        SelectedServicesRecyclerViewAdapter adapter =
                new SelectedServicesRecyclerViewAdapter(menuItem.getServiceTypes());
        recyclerViewServices.setAdapter(adapter);
        recyclerViewServices.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_dismiss) {

            dismiss();
        }
    }
}