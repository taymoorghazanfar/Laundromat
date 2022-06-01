package com.laundromat.merchant.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.maps.model.LatLng;
import com.laundromat.merchant.R;
import com.laundromat.merchant.activities.PickLocationActivity;
import com.laundromat.merchant.model.Laundry;
import com.laundromat.merchant.model.util.Timings;
import com.laundromat.merchant.ui.interfaces.ILaundrySignupListener;
import com.laundromat.merchant.utils.ImageUtils;
import com.laundromat.merchant.utils.ValidationUtils;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;

public class LaundrySignupFragment extends Fragment
        implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    // Constants
    private static final int REQUEST_CODE_LOGO = 444;
    private static final int OPENING_TIME = 555;
    private static final int CLOSING_TIME = 666;
    private static final int REQUEST_CODE_LOCATION = 777;
    private static final int DESTINATION_ID = 1;

    // Widgets
    private ImageView imageViewLogo;
    private EditText editTextStoreName;
    private RadioGroup radioGroupStoreType;
    private EditText editTextOpeningTime;
    private EditText editTextClosingTime;
    private EditText editTextLocation;
    private Button buttonPrevious;
    private Button buttonProceed;

    // Interface
    private ILaundrySignupListener iLaundrySignupListener;

    // Variables
    private Uri storeLogoUri;
    private LatLng storeLocationLatLng;
    private int storeTypeIndex = -1;
    private String storeOpeningTime;
    private String storeClosingTime;

    public LaundrySignupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_laundry_signup, container, false);

        initViews(view);

        return view;
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);

        try {
            iLaundrySignupListener = (ILaundrySignupListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " Must implement ILaundrySignup");
        }
    }

    private void initViews(View view) {

        this.imageViewLogo = view.findViewById(R.id.image_view_logo);

        this.editTextStoreName = view.findViewById(R.id.edit_text_store_name);
        this.editTextStoreName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                String text = editTextStoreName.getText().toString().trim();

                if (!ValidationUtils.isInputValid(text)) {

                    editTextStoreName.setError("Store name has invalid format");
                }
            }
        });

        this.radioGroupStoreType = view.findViewById(R.id.radio_group_store_type);
        this.editTextOpeningTime = view.findViewById(R.id.edit_text_opening_time);
        this.editTextClosingTime = view.findViewById(R.id.edit_text_closing_time);
        this.editTextLocation = view.findViewById(R.id.edit_text_location);
        this.buttonPrevious = view.findViewById(R.id.button_previous);
        this.buttonProceed = view.findViewById(R.id.button_proceed);

        this.imageViewLogo.setOnClickListener(this);
        this.editTextOpeningTime.setOnClickListener(this);
        this.editTextClosingTime.setOnClickListener(this);
        this.editTextLocation.setOnClickListener(this);
        this.buttonPrevious.setOnClickListener(this);
        this.buttonProceed.setOnClickListener(this);
        this.radioGroupStoreType.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.image_view_logo) {

            getLogoImage();

        } else if (view.getId() == R.id.edit_text_opening_time) {

            getTimeFromUser(OPENING_TIME);

        } else if (view.getId() == R.id.edit_text_closing_time) {

            getTimeFromUser(CLOSING_TIME);

        } else if (view.getId() == R.id.edit_text_location) {

            getLocationFromUser();

        } else if (view.getId() == R.id.button_previous) {

            if (iLaundrySignupListener != null) {

                iLaundrySignupListener.onButtonPreviousClick();
            }

        } else if (view.getId() == R.id.button_proceed) {

            initLaundry();
        }
    }

    private void getLogoImage() {

        ImagePicker.with(this)
                .cropSquare()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start(REQUEST_CODE_LOGO);
    }

    private void getTimeFromUser(int timeType) {

        Calendar calendar = Calendar.getInstance();
        TimePickerDialog datePicker = TimePickerDialog.newInstance((view, hourOfDay, minute, second) -> {
            try {

                String hourValue = String.valueOf(hourOfDay);
                String minuteValue = String.valueOf(minute);

                if (hourValue.length() == 1) {

                    hourValue = "0" + hourValue;
                }

                if (minuteValue.length() == 1) {

                    minuteValue = "0" + minuteValue;
                }

                String timeString = hourValue + ":" + minuteValue;

                if (timeType == OPENING_TIME) {

                    storeOpeningTime = timeString;
                    editTextOpeningTime.setText(timeString);

                } else if (timeType == CLOSING_TIME) {

                    storeClosingTime = timeString;
                    editTextClosingTime.setText(timeString);
                }

            } catch (Exception ignored) {
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);

        datePicker.show(getFragmentManager(), "TimePickerDialog");
    }

    private void getLocationFromUser() {

        Intent intent = new Intent(getActivity(), PickLocationActivity.class);
        intent.putExtra(PickLocationActivity.FORM_VIEW_INDICATOR, DESTINATION_ID);

        startActivityForResult(intent, REQUEST_CODE_LOCATION);
    }

    private void initLaundry() {

        String storeName = editTextStoreName.getText().toString().trim();

        if (TextUtils.isEmpty(storeName) || storeLogoUri == null ||
                storeLocationLatLng == null || storeTypeIndex == -1 ||
                storeOpeningTime == null || storeClosingTime == null) {

            Toast.makeText(getContext(),
                    "All fields must be filled", Toast.LENGTH_SHORT).show();

            return;
        }

        if (!ValidationUtils.isInputValid(storeName)) {

            editTextStoreName.setError("Store name has invalid format");
            return;
        }

        String logo64 = ImageUtils.uriToBase64(getContext(), storeLogoUri);

        Laundry laundry = new Laundry(storeName, logo64, storeLocationLatLng,
                storeTypeIndex != 0, new Timings(storeOpeningTime, storeClosingTime));

        if (iLaundrySignupListener != null) {

            iLaundrySignupListener.onLaundrySignup(laundry);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {

        View radioButton = radioGroup.findViewById(checkedId);
        storeTypeIndex = (radioGroup.indexOfChild(radioButton));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_LOGO && resultCode == Activity.RESULT_OK) {

            if (data != null) {

                storeLogoUri = data.getData();
                imageViewLogo.setImageURI(storeLogoUri);
            }

        } else if (requestCode == REQUEST_CODE_LOCATION && resultCode == Activity.RESULT_OK) {

            if (data != null) {

                String locationName = data.getStringExtra(PickLocationActivity.LOCATION_NAME);
                storeLocationLatLng = data.getParcelableExtra(PickLocationActivity.LOCATION_LAT_LONG);

                editTextLocation.setText(locationName);
            }
        }
    }
}