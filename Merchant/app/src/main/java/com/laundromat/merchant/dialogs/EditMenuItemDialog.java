package com.laundromat.merchant.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.merchant.R;
import com.laundromat.merchant.activities.MenuItemsActivity;
import com.laundromat.merchant.model.washable.ServiceType;
import com.laundromat.merchant.model.washable.WashableItem;
import com.laundromat.merchant.model.washable.WashableItemCategory;
import com.laundromat.merchant.prefs.Session;
import com.laundromat.merchant.ui.adapters.ServicesRecyclerViewAdapter;
import com.laundromat.merchant.ui.interfaces.IMenuItemEditedListener;
import com.laundromat.merchant.utils.Globals;
import com.laundromat.merchant.utils.ImageUtils;
import com.laundromat.merchant.utils.ValidationUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EditMenuItemDialog extends DialogFragment
        implements View.OnClickListener, TextWatcher {

    // Constants
    private static final String MENU_CATEGORY = "menu_category";
    private static final String MENU_ITEM = "menu_item";
    private static final String MENU_ITEM_INDEX = "menu_item_index";
    private static final int REQUEST_CODE_IMAGE = 111;
    boolean imageUpdated = false;
    // Variables
    private ArrayList<ServiceType> availableServiceTypes;
    private WashableItemCategory menuCategory;
    private WashableItem menuItem;
    private int menuItemIndex;
    private Uri imageUri;
    // Views
    private ImageView imageViewItem;
    private EditText editTextName;
    private RecyclerView recyclerViewServices;
    private Button buttonUpdate;
    private Button buttonCancel;
    private LottieAnimationView loadingAnimation;

    // Interfaces
    private IMenuItemEditedListener menuItemEditedListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initializing available service types list
        availableServiceTypes = Globals.getAvailableServices();

        if (getArguments() != null) {

            menuCategory = getArguments().getParcelable(MENU_CATEGORY);
            menuItem = getArguments().getParcelable(MENU_ITEM);
            menuItemIndex = getArguments().getInt(MENU_ITEM_INDEX, -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.dialog_edit_menu_item, container, false);
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

    public void setListener(IMenuItemEditedListener menuItemEditedListener) {
        this.menuItemEditedListener = menuItemEditedListener;
    }

    private void initViews(View view) {

        recyclerViewServices = view.findViewById(R.id.recycler_view_services);

        imageViewItem = view.findViewById(R.id.image_view_item);
        Picasso.get()
                .load(menuItem.getImageUrl())
                .into(imageViewItem);

        editTextName = view.findViewById(R.id.edit_text_name);
        editTextName.addTextChangedListener(this);
        editTextName.setText(menuItem.getName());

        buttonUpdate = view.findViewById(R.id.button_update);
        buttonCancel = view.findViewById(R.id.button_cancel);
        loadingAnimation = view.findViewById(R.id.loading_anim);

        imageViewItem.setOnClickListener(this);
        buttonUpdate.setOnClickListener(this);
        buttonCancel.setOnClickListener(this);

        initServicesRecyclerView();
    }

    private void initServicesRecyclerView() {

        // map selected services to available services
        for (ServiceType serviceType : menuItem.getServiceTypes()) {

            for (ServiceType availableServiceType : availableServiceTypes) {

                if (availableServiceType.getName().equals(serviceType.getName())) {

                    availableServiceType.setPrice(serviceType.getPrice());
                    availableServiceType.setActive(serviceType.isActive());
                }
            }
        }

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

    private void update() {

        boolean empty = checkEmpty();
        boolean updated = checkUpdated();
        boolean duplicate = checkDuplicate();

        // check empty
        if (empty) {

            Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        String menuItemName = editTextName.getText().toString().trim();

        if (!ValidationUtils.isNameValid(menuItemName)) {

            editTextName.setError("Item name has invalid format");
            return;
        }

        // check updated
        if (!updated) {

            Toast.makeText(getContext(), "Nothing to update", Toast.LENGTH_SHORT).show();
            return;
        }

        // check duplicate
        if (duplicate) {

            editTextName.setError("Menu item with same name already exist");
            return;
        }

        // update menu item
        updateMenuItem();
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

    private boolean checkUpdated() {

        boolean servicesChanged = false;

        // check if there is no change in service types
        if (menuItem.getServiceTypes().size() == getSelectedServices().size()) {

            for (ServiceType existingServiceType : menuItem.getServiceTypes()) {

                for (ServiceType selectedServiceType : getSelectedServices()) {

                    if (existingServiceType.getName().equals(selectedServiceType.getName())) {

                        // if price is changed
                        if (existingServiceType.getPrice() != selectedServiceType.getPrice()) {

                            servicesChanged = true;
                            break;
                        }
                    }
                }
            }
        } else {

            servicesChanged = true;
        }

        String menuItemName = editTextName.getText().toString().trim();

        return imageUri != null || !menuItemName.equals(menuItem.getName())
                || servicesChanged;
    }

    private boolean checkDuplicate() {

        String menuItemName = editTextName.getText().toString().trim();

        for (WashableItem searchItem : menuCategory.getWashableItems()) {

            if (searchItem.getName().equals(menuItem.getName())) {

                continue;
            }

            if (searchItem.getName().equals(menuItemName)) {

                return true;
            }
        }

        return false;
    }

    private void updateMenuItem() {

        String menuItemName = editTextName.getText().toString().trim();

        List<ServiceType> selectedServices = getSelectedServices();

        WashableItem updatedMenuItem = new WashableItem(menuItemName);


        // update menu item object
//        menuItem.setName(menuItemName);
        updatedMenuItem.setServiceTypes(selectedServices);
        updatedMenuItem.setDateCreated(menuItem.getDateCreated());
        updatedMenuItem.setImageUrl(menuItem.getImageUrl());

        if (imageUpdated) {

            String image64 = ImageUtils.uriToBase64(getContext(), imageUri);
            updatedMenuItem.setImageUrl(image64);
        }

        // create query data
        Map<String, Object> data = new HashMap<>();
        data.put("laundry_id", Session.user.getLaundry().getId());
        data.put("category", menuCategory.toJson());
        data.put("menu_item", updatedMenuItem.toJson());
        data.put("menu_item_index", menuItemIndex);
        data.put("imaged_updated", imageUpdated);

        // update the menu item in database
        getDialog().hide();
        ((MenuItemsActivity) getActivity()).showLoadingAnimation();


        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("laundry-updateMenuItem")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    if (httpsCallableResult.getData() != null) {

                        ((MenuItemsActivity) getActivity()).hideLoadingAnimation();


                        // get the response [download url OR acknowledgment]
                        String response = (String) httpsCallableResult.getData();

                        // if there is download url to get
                        if (!response.equals("updated")) {

                            updatedMenuItem.setImageUrl(response);
                        }

                        if (menuItemEditedListener != null) {

                            menuItemEditedListener.onMenuItemEdited(updatedMenuItem, menuItemIndex);
                            dismiss();
                        }
                    }
                })
                .addOnFailureListener(e -> {

                    ((MenuItemsActivity) getActivity()).showLoadingAnimation();
                    getDialog().show();

                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("updated", "updateMenuItem: " + e.getMessage());
                });
    }

    private List<ServiceType> getSelectedServices() {

        List<ServiceType> selectedServices = new ArrayList<>();

        for (ServiceType serviceType : availableServiceTypes) {

            if (serviceType.isActive()) {

                selectedServices.add(serviceType);
            }
        }

        return selectedServices;
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

        } else if (view.getId() == R.id.button_update) {

            update();

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

                imageUpdated = true;
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