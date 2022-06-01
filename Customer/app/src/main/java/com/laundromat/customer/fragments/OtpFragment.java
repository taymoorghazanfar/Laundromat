package com.laundromat.customer.fragments;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.laundromat.customer.R;
import com.laundromat.customer.ui.interfaces.IOtpFragmentListener;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

public class OtpFragment extends Fragment {

    // Views
    private TextView textViewPhone;
    private EditText editTextVerificationCode;
    private TextView textViewTimer;
    private Button buttonVerify;
    private Button buttonResendCode;

    private String sentVerificationCode;
    private String receivedVerificationCode;

    private CountDownTimer timer;
    private FirebaseAuth firebaseAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks authCallbacks;

    // Variables
    private String phoneNumberWithCode;
    private String phoneNumber;

    // Interface
    private IOtpFragmentListener listener;

    public OtpFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            phoneNumberWithCode = getArguments().getString("phone_number");
            phoneNumber = getArguments().getString("phone_number");
            phoneNumberWithCode = "+92" + phoneNumberWithCode;
        }

        initFirebaseAuth();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_otp, container, false);
        initViews(view);
        sendVerificationCode(phoneNumberWithCode);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {

            timer.cancel();
        }
    }

    private void initFirebaseAuth() {

        firebaseAuth = FirebaseAuth.getInstance();

        authCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                //verification code is successfully received by the user
                receivedVerificationCode = phoneAuthCredential.getSmsCode();
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

                Toast.makeText(getContext(),
                        "Verification Failed. Try again", Toast.LENGTH_SHORT).show();
                Log.d("signup", "initCustomer: otp " + e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                super.onCodeAutoRetrievalTimeOut(s);
            }

            @Override
            public void onCodeSent(@NonNull String verificationCode,
                                   @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {

                Toast.makeText(getContext(), "OTP Sent", Toast.LENGTH_SHORT).show();

                super.onCodeSent(verificationCode, forceResendingToken);

                sentVerificationCode = verificationCode;
            }
        };
    }

    private void initViews(View view) {

        this.textViewPhone = view.findViewById(R.id.text_view_phone);
        this.textViewPhone.setText(MessageFormat.format("Enter the verification code sent at\n********{0}",
                phoneNumber.substring(phoneNumber.length() - 3)));

        this.editTextVerificationCode = view.findViewById(R.id.edit_text_verification_code);
        this.textViewTimer = view.findViewById(R.id.text_view_timer);

        this.buttonVerify = view.findViewById(R.id.button_verify);
        this.buttonVerify.setOnClickListener(view1 -> {

            String verificationCode = editTextVerificationCode.getText().toString().trim();

            if (TextUtils.isEmpty(verificationCode)) {

                editTextVerificationCode.setError("Please enter the verification code");
                return;
            }

            if (!verificationCode.equals(receivedVerificationCode)) {

                editTextVerificationCode.setError("Invalid verification code entered");

                return;
            }

            verifyCode(verificationCode);
        });

        this.buttonResendCode = view.findViewById(R.id.button_resend_verification_code);
        this.buttonResendCode.setOnClickListener(view12 -> sendVerificationCode(phoneNumber));
    }

    private void verifyCode(String receivedVerificationCode) {

        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(sentVerificationCode, receivedVerificationCode);

        completeVerification(credential);
    }

    private void sendVerificationCode(String phoneNumber) {

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(getActivity())
                        .setCallbacks(authCallbacks)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);

        startResendOtpTimer(60);
    }

    private void completeVerification(PhoneAuthCredential credential) {

        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {

                    if (listener != null) {

                        listener.onOtpEntered(phoneNumber);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(),
                        "Verification Failed. Try again", Toast.LENGTH_SHORT).show());
    }

    public void startResendOtpTimer(int Seconds) {

        buttonResendCode.setEnabled(false);

        this.timer = new CountDownTimer(Seconds * 1000 + 1000, 1000) {

            public void onTick(long millisUntilFinished) {

                int seconds = (int) (millisUntilFinished / 1000);
                seconds = seconds % 60;
                textViewTimer.setText(MessageFormat
                        .format("Resend code in: {0} second(s)", seconds));
            }

            public void onFinish() {

                buttonResendCode.setEnabled(true);
                textViewTimer.setText("Didn't received the code ?");
            }
        }.start();
    }

    public void setListener(IOtpFragmentListener listener) {
        this.listener = listener;
    }
}