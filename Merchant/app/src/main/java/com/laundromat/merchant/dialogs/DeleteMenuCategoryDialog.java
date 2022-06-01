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
import com.laundromat.merchant.activities.MenuCategoryActivity;
import com.laundromat.merchant.prefs.Session;
import com.laundromat.merchant.ui.interfaces.IMenuCategoryDeletedListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DeleteMenuCategoryDialog extends androidx.fragment.app.DialogFragment
        implements View.OnClickListener {

    // Variables
    int menuCategoryIndex;

    // Views
    private Button buttonDelete;
    private Button buttonCancel;
    private LottieAnimationView loadingAnimation;

    private IMenuCategoryDeletedListener menuCategoryDeletedListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            menuCategoryIndex = getArguments().getInt("menu_category_index");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.dialog_delete_menu_category, container, false);
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

    public void setListener(IMenuCategoryDeletedListener menuCategoryDeletedListener) {
        this.menuCategoryDeletedListener = menuCategoryDeletedListener;
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
        data.put("index", menuCategoryIndex);

        getDialog().hide();
        ((MenuCategoryActivity) getActivity()).showLoadingAnimation();

        // delete menu category from database
        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("laundry-deleteMenuCategory")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    ((MenuCategoryActivity) getActivity()).hideLoadingAnimation();

                    if (menuCategoryDeletedListener != null) {

                        menuCategoryDeletedListener.onMenuCategoryDeleted(menuCategoryIndex);
                        dismiss();
                    }
                })
                .addOnFailureListener(e -> {

                    ((MenuCategoryActivity) getActivity()).hideLoadingAnimation();
                    getDialog().show();
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("delete", "delete: " + e.getMessage());
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