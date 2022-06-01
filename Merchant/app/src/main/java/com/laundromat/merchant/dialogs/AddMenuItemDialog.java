package com.laundromat.merchant.dialogs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.merchant.R;
import com.laundromat.merchant.activities.MenuCategoryActivity;
import com.laundromat.merchant.activities.MenuItemsActivity;
import com.laundromat.merchant.model.washable.ServiceType;
import com.laundromat.merchant.model.washable.WashableItem;
import com.laundromat.merchant.model.washable.WashableItemCategory;
import com.laundromat.merchant.prefs.Session;
import com.laundromat.merchant.ui.adapters.ServicesRecyclerViewAdapter;
import com.laundromat.merchant.ui.interfaces.IMenuItemCreatedListener;
import com.laundromat.merchant.utils.Globals;
import com.laundromat.merchant.utils.ImageUtils;
import com.laundromat.merchant.utils.ValidationUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AddMenuItemDialog extends DialogFragment
        implements View.OnClickListener, TextWatcher {

    // Constants
    private static final String MENU_CATEGORY = "menu_category";
    private static final int REQUEST_CODE_IMAGE = 111;

    // Variables
    private ArrayList<ServiceType> availableServiceTypes = Globals.getAvailableServices();
    private WashableItemCategory menuCategory;
    private Uri imageUri;

    // Views
    private ImageView imageViewItem;
    private EditText editTextName;
    private RecyclerView recyclerViewServices;
    private Button buttonSave;
    private Button buttonCancel;
    private LottieAnimationView loadingAnimation;

    // Interfaces
    private IMenuItemCreatedListener menuItemCreatedListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            menuCategory = getArguments().getParcelable(MENU_CATEGORY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.dialog_add_menu_item, container, false);
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

    public void setListener(IMenuItemCreatedListener menuItemCreatedListener) {
        this.menuItemCreatedListener = menuItemCreatedListener;
    }

    private void initViews(View view) {

        imageViewItem = view.findViewById(R.id.image_view_item);

        editTextName = view.findViewById(R.id.edit_text_name);
        editTextName.addTextChangedListener(this);

        recyclerViewServices = view.findViewById(R.id.recycler_view_services);
        buttonSave = view.findViewById(R.id.button_save);
        buttonCancel = view.findViewById(R.id.button_cancel);
        loadingAnimation = view.findViewById(R.id.loading_anim);

        imageViewItem.setOnClickListener(this);
        buttonSave.setOnClickListener(this);
        buttonCancel.setOnClickListener(this);

        ServicesRecyclerViewAdapter adapter =
                new ServicesRecyclerViewAdapter(availableServiceTypes);
        recyclerViewServices.setAdapter(adapter);
        recyclerViewServices.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void pickImage() {

        ImagePicker.with(this)
                .cropSquare()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start(REQUEST_CODE_IMAGE);
    }

    private void save() {

        boolean isEmpty = checkEmpty();
        boolean isDuplicate = checkDuplicate();

        //check empty
        if (isEmpty) {

            Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        String menuItemName = editTextName.getText().toString().trim();

        // check valid
        if (!ValidationUtils.isNameValid(menuItemName)) {

            editTextName.setError("Item name has invalid format");
            return;
        }

        //check duplicate
        if (isDuplicate) {

            editTextName.setError("Menu item with same name already exist");
            return;
        }

        //create menu item
        createMenuItem();
    }

    private boolean checkEmpty() {

        boolean imageSelected;
        boolean nameEntered;
        boolean serviceSelected = false;
        boolean priceEntered = false;

        // check if image is empty
        imageSelected = imageViewItem.getDrawable() != null;

        // check if name is entered
        String menuItemName = editTextName.getText().toString().trim();

        nameEntered = !TextUtils.isEmpty(menuItemName);

        for (ServiceType serviceType : availableServiceTypes) {

            // check if service is selected
            if (serviceType.isActive()) {

                serviceSelected = true;

                // check if price is entered
                if (serviceType.getPrice() > 0) {

                    priceEntered = true;

                } else {

                    priceEntered = false;
                    break;
                }
            }
        }

        return !imageSelected || !nameEntered || !serviceSelected || !priceEntered;
    }

    private boolean checkDuplicate() {

        String menuItemName = editTextName.getText().toString().trim();

        for (WashableItem searchItem : menuCategory.getWashableItems()) {

            if (searchItem.getName().equals(menuItemName)) {

                return true;
            }
        }

        return false;
    }

    private void createMenuItem() {

        String menuItemName = editTextName.getText().toString().trim();

        String image64 = ImageUtils.uriToBase64(getContext(), imageUri);

        List<ServiceType> selectedServices = new ArrayList<>();

        for (ServiceType serviceType : availableServiceTypes) {

            if (serviceType.isActive()) {

                selectedServices.add(serviceType);
            }
        }

        // create new menu item object
        WashableItem menuItem = new WashableItem(menuItemName);
        menuItem.setServiceTypes(selectedServices);
        menuItem.setImageUrl(image64);

        // create query data
        Map<String, Object> data = new HashMap<>();
        data.put("laundry_id", Session.user.getLaundry().getId());
        data.put("category", menuCategory.toJson());
        data.put("menu_item", menuItem.toJson());

        getDialog().hide();
        ((MenuItemsActivity) getActivity()).showLoadingAnimation();

        // save the menu item to database
        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("laundry-addMenuItem")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    if (httpsCallableResult.getData() != null) {

                        ((MenuItemsActivity) getActivity()).hideLoadingAnimation();

                        //get the download url of the menu item image
                        String imageDownloadUrl = (String) httpsCallableResult.getData();
                        menuItem.setImageUrl(imageDownloadUrl);

                        if (menuItemCreatedListener != null) {

                            menuItemCreatedListener.onMenuItemCreated(menuItem);
                            dismiss();
                        }
                    }
                })
                .addOnFailureListener(e -> {

                    ((MenuItemsActivity) getActivity()).hideLoadingAnimation();
                    getDialog().show();

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
    public void onClick(View view) {

        if (view.getId() == R.id.image_view_item) {

            pickImage();

        } else if (view.getId() == R.id.button_save) {

            save();

        } else if (view.getId() == R.id.button_cancel) {

            dismiss();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_IMAGE
                && resultCode == Activity.RESULT_OK) {

            if (data != null) {

                imageUri = data.getData();
                imageViewItem.setImageURI(imageUri);
            }
        }
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

        if (editTextName.getText().hashCode() == editable.hashCode()) {

            boolean isDuplicate = checkDuplicate();

            text = editTextName.getText().toString().trim();

            if (!ValidationUtils.isNameValid(text)) {

                editTextName.setError("Item name has invalid format");
                return;
            }

            if (isDuplicate) {

                editTextName.setError("Menu item with same name already exist");
            }
        }
    }
}