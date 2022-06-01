package com.laundromat.merchant.dialogs;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.merchant.R;
import com.laundromat.merchant.activities.MenuItemsActivity;
import com.laundromat.merchant.model.washable.WashableItemCategory;
import com.laundromat.merchant.prefs.Session;
import com.laundromat.merchant.ui.interfaces.IMenuItemDeletedListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DeleteMenuItemDialog extends androidx.fragment.app.DialogFragment
        implements View.OnClickListener {

    // Variables
    WashableItemCategory menuCategory;
    int menuItemIndex;

    // Views
    private Button buttonDelete;
    private Button buttonCancel;
    private LottieAnimationView loadingAnimation;

    private IMenuItemDeletedListener menuItemDeletedListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            menuCategory = getArguments().getParcelable("menu_category");
            menuItemIndex = getArguments().getInt("menu_item_index");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.dialog_delete_menu_item, container, false);
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

    public void setListener(IMenuItemDeletedListener menuItemDeletedListener) {
        this.menuItemDeletedListener = menuItemDeletedListener;
    }

    private void initViews(View view) {

        buttonDelete = view.findViewById(R.id.button_delete);
        buttonCancel = view.findViewById(R.id.button_cancel);
        loadingAnimation = view.findViewById(R.id.loading_anim);

        buttonDelete.setOnClickListener(this);
        buttonCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_delete) {

            delete();

        } else if (view.getId() == R.id.button_cancel) {

            dismiss();
        }
    }

    private void delete() {

        Map<String, Object> data = new HashMap<>();
        data.put("laundry_id", Session.user.getLaundry().getId());
        data.put("category", menuCategory.toJson());
        data.put("menu_item_index", menuItemIndex);

        getDialog().hide();
        ((MenuItemsActivity) getActivity()).showLoadingAnimation();


        // delete menu category from database
        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("laundry-deleteMenuItem")
                .call(data)
                .addOnSuccessListener(httpsCallableResult2 -> {

                    ((MenuItemsActivity) getActivity()).hideLoadingAnimation();

                    if (menuItemDeletedListener != null) {

                        menuItemDeletedListener.onMenuItemDeleted(menuItemIndex);
                        dismiss();
                    }
                })
                .addOnFailureListener(e -> {

                    ((MenuItemsActivity) getActivity()).hideLoadingAnimation();

                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("deleted", "delete: " + e.getMessage());
                });
    }

    public void showLoadingAnimation() {

        loadingAnimation.bringToFront();
        loadingAnimation.setVisibility(View.VISIBLE);
    }

    public void hideLoadingAnimation() {

        loadingAnimation.setVisibility(View.GONE);
    }
}