package com.laundromat.merchant.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.functions.FirebaseFunctions;
import com.laundromat.merchant.R;
import com.laundromat.merchant.activities.PickLocationActivity;
import com.laundromat.merchant.activities.ProfileActivity;
import com.laundromat.merchant.model.Laundry;
import com.laundromat.merchant.prefs.Session;
import com.laundromat.merchant.ui.interfaces.ILaundryEditedListener;
import com.laundromat.merchant.ui.interfaces.IMerchantEditedListener;
import com.laundromat.merchant.utils.ImageUtils;
import com.laundromat.merchant.utils.LocationUtils;
import com.squareup.picasso.Picasso;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EditLaundryDialog extends androidx.fragment.app.DialogFragment
        implements View.OnClickListener {

    // Constants
    private static final int REQUEST_CODE_LOGO = 444;
    private static final int OPENING_TIME = 555;
    private static final int CLOSING_TIME = 666;

    // Interfaces
    ILaundryEditedListener laundryEditedListener;

    // Views
    private ImageView imageViewLogo;
    private EditText editTextOpeningTime;
    private EditText editTextClosingTime;
    private EditText editTextDiscount;
    private Button buttonUpdate;
    private Button buttonCancel;

    //Variables
    private Uri storeLogoUri;
    private String storeOpeningTime;
    private String storeClosingTime;
    private Laundry laundry = Session.user.getLaundry();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.dialog_edit_laundry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        setupViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        Objects.requireNonNull(getDialog()).getWindow()
                .setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void setListener(ILaundryEditedListener laundryEditedListener) {
        this.laundryEditedListener = laundryEditedListener;
    }

    private void initViews(View view) {

        this.imageViewLogo = view.findViewById(R.id.image_view_logo);
        this.editTextOpeningTime = view.findViewById(R.id.edit_text_opening_time);
        this.editTextClosingTime = view.findViewById(R.id.edit_text_closing_time);

        this.imageViewLogo.setOnClickListener(this);
        this.editTextOpeningTime.setOnClickListener(this);
        this.editTextClosingTime.setOnClickListener(this);

        this.editTextDiscount = view.findViewById(R.id.edit_text_discount);

        buttonUpdate = view.findViewById(R.id.button_update);
        buttonUpdate.setOnClickListener(this);

        buttonCancel = view.findViewById(R.id.button_cancel);
        buttonCancel.setOnClickListener(this);
    }

    private void setupViews() {

        Picasso.get()
                .load(laundry.getLogoUrl())
                .into(imageViewLogo);

        editTextOpeningTime.setText(laundry.getTimings().getOpeningTime());

        editTextClosingTime.setText(laundry.getTimings().getClosingTime());

        editTextDiscount.setText(String.valueOf(laundry.getDiscount()));
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

        datePicker.show(getChildFragmentManager(), "TimePickerDialog");
    }

    private void update() {

        String discount = editTextDiscount.getText().toString().trim();

        if (TextUtils.isEmpty(discount)) {

            editTextDiscount.setError("Field cannot be empty");
            return;
        }

        if (storeOpeningTime == null
                && storeClosingTime == null
                && storeLogoUri == null
                && Double.parseDouble(discount) == laundry.getDiscount()) {

            Toast.makeText(getContext(),
                    "Nothing to update", Toast.LENGTH_SHORT).show();

            return;
        }

        double discountAmount = Double.parseDouble(discount);
        boolean imageUpdated = false;
        String image64 = null;

        if (storeLogoUri != null) {

            imageUpdated = true;
            image64 = ImageUtils.uriToBase64(getContext(), storeLogoUri);
        }

        if (storeOpeningTime == null) {

            storeOpeningTime = laundry.getTimings().getOpeningTime();
        }
        if (storeClosingTime == null) {

            storeClosingTime = laundry.getTimings().getClosingTime();
        }

        Map<String, Object> data = new HashMap<>();
        data.put("laundry_id", laundry.getId());
        data.put("image_updated", imageUpdated);
        data.put("image_64", image64);
        data.put("opening_time", storeOpeningTime);
        data.put("closing_time", storeClosingTime);
        data.put("discount", discountAmount);

        getDialog().hide();
        ((ProfileActivity) getActivity()).showLoadingAnimation();

        FirebaseFunctions
                .getInstance()
                .getHttpsCallable("laundry-updateLaundry")
                .call(data)
                .addOnSuccessListener(httpsCallableResult -> {

                    if (httpsCallableResult.getData() != null) {

                        ((ProfileActivity) getActivity()).hideLoadingAnimation();

                        // get the response [download url OR acknowledgment]
                        String response = (String) httpsCallableResult.getData();

                        String avatarUrl = null;

                        // if there is download url to get
                        if (!response.equals("updated")) {

                            avatarUrl = response;
                        }

                        if (laundryEditedListener != null) {

                            laundryEditedListener
                                    .onLaundryEdited(avatarUrl,
                                            storeOpeningTime, storeClosingTime, discountAmount);
                            dismiss();
                        }
                    }
                })
                .addOnFailureListener(e -> {

                    ((ProfileActivity) getActivity()).hideLoadingAnimation();

                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.image_view_logo) {

            getLogoImage();

        } else if (view.getId() == R.id.edit_text_opening_time) {

            getTimeFromUser(OPENING_TIME);

        } else if (view.getId() == R.id.edit_text_closing_time) {

            getTimeFromUser(CLOSING_TIME);

        } else if (view.getId() == R.id.button_update) {

            update();

        } else if (view.getId() == R.id.button_cancel) {

            dismiss();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_LOGO && resultCode == Activity.RESULT_OK) {

            if (data != null) {

                storeLogoUri = data.getData();
                imageViewLogo.setImageURI(storeLogoUri);
            }
        }
    }
}