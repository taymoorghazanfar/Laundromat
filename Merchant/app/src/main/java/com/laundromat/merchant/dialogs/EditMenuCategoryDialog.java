package com.laundromat.merchant.dialogs;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.merchant.R;
import com.laundromat.merchant.activities.MenuCategoryActivity;
import com.laundromat.merchant.model.washable.WashableItemCategory;
import com.laundromat.merchant.prefs.Session;
import com.laundromat.merchant.ui.interfaces.IMenuCategoryEditedListener;
import com.laundromat.merchant.utils.ValidationUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EditMenuCategoryDialog extends androidx.fragment.app.DialogFragment
        implements View.OnClickListener, TextWatcher {

    // Variables
    WashableItemCategory menuCategory;
    int menuCategoryIndex;

    // Views
    private EditText editTextTitle;
    private Button buttonUpdate;
    private Button buttonCancel;
    private LottieAnimationView loadingAnimation;
    private IMenuCategoryEditedListener menuCategoryEditedListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            menuCategory = getArguments().getParcelable("menu_category");
            menuCategoryIndex = getArguments().getInt("menu_category_index");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.dialog_edit_menu_category, container, false);
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

    public void setListener(IMenuCategoryEditedListener menuCategoryEditedListener) {
        this.menuCategoryEditedListener = menuCategoryEditedListener;
    }

    private void initViews(View view) {

        editTextTitle = view.findViewById(R.id.edit_text_title);
        editTextTitle.addTextChangedListener(this);
        editTextTitle.setText(menuCategory.getTitle());

        buttonUpdate = view.findViewById(R.id.button_update);
        buttonCancel = view.findViewById(R.id.button_cancel);
        loadingAnimation = view.findViewById(R.id.loading_anim);

        buttonUpdate.setOnClickListener(this);
        buttonCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_update) {

            update();

        } else if (view.getId() == R.id.button_cancel) {

            dismiss();
        }
    }

    private void update() {

        boolean isEmpty = checkEmpty();
        boolean isUpdated = checkUpdated();
        boolean isDuplicate = checkDuplicate();

        //check empty
        if (isEmpty) {

            Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        String categoryTitle = editTextTitle.getText().toString().trim();

        if (!ValidationUtils.isNameValid(categoryTitle)) {

            editTextTitle.setError("Title has invalid format");
            return;
        }

        //check updated
        if (!isUpdated) {

            Toast.makeText(getContext(), "Nothing to update", Toast.LENGTH_SHORT).show();
            return;
        }

        //check duplicate
        if (isDuplicate) {

            editTextTitle.setError("Menu category with same name already exist");
            return;
        }

        //create menu category
        updateMenuCategory();
    }

    private boolean checkEmpty() {

        String categoryTitle = editTextTitle.getText().toString().trim();

        return TextUtils.isEmpty(categoryTitle);
    }

    private boolean checkUpdated() {

        String menuCategoryTitle = editTextTitle.getText().toString().trim();

        return !menuCategoryTitle.equals(menuCategory.getTitle());
    }

    private boolean checkDuplicate() {

        String categoryTitle = editTextTitle.getText().toString().trim();

        for (WashableItemCategory searchCategory : Session.user.getLaundry().getMenu()) {

            // only search for title other than the object itself
            if (searchCategory.getTitle().equals(menuCategory.getTitle())) {

                continue;
            }

            if (searchCategory.getTitle().equals(categoryTitle)) {

                return true;
            }
        }

        return false;
    }

    private void updateMenuCategory() {

        String categoryTitle = editTextTitle.getText().toString().trim();

        menuCategory.setTitle(categoryTitle);

        // create query data
        Map<String, Object> data = new HashMap<>();
        data.put("laundry_id", Session.user.getLaundry().getId());
        data.put("category", menuCategory.toJson());
        data.put("category_index", menuCategoryIndex);

        getDialog().hide();
        ((MenuCategoryActivity) getActivity()).showLoadingAnimation();

        // update the menu category data in database
        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("laundry-updateMenuCategory")
                .call(data)
                .addOnSuccessListener(httpsCallableResult2 -> {

                    ((MenuCategoryActivity) getActivity()).hideLoadingAnimation();

                    if (menuCategoryEditedListener != null) {

                        menuCategoryEditedListener
                                .onMenuCategoryEdited(menuCategory, menuCategoryIndex);

                        dismiss();
                    }
                })
                .addOnFailureListener(e -> {

                    ((MenuCategoryActivity) getActivity()).hideLoadingAnimation();
                    getDialog().show();

                    Log.d("category", "updateMenuCategory: " + e.getMessage());
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void showLoadingAnimation() {

        loadingAnimation.bringToFront();
        loadingAnimation.setVisibility(View.VISIBLE);
    }

    public void hideLoadingAnimation() {

        loadingAnimation.setVisibility(View.GONE);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {

        String text = "";

        if (editTextTitle.getText().hashCode() == editable.hashCode()) {

            boolean isDuplicate = checkDuplicate();
            text = editTextTitle.getText().toString().trim();

            if (!ValidationUtils.isNameValid(text)) {

                editTextTitle.setError("Title has invalid format");
                return;
            }

            if (isDuplicate) {

                editTextTitle.setError("Menu category with same name already exist");
            }
        }
    }
}
