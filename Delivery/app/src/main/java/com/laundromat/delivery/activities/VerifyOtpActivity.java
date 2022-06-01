package com.laundromat.delivery.activities;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.laundromat.delivery.R;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

public class VerifyOtpActivity extends AppCompatActivity {

    private TextView textViewPhone;
    private EditText editTextVerificationCode;
    private TextView textViewTimer;
    private Button buttonVerify;
    private Button buttonResendCode;
    private Button buttonGoBack;

    private String phoneNumber;
    private String callingActivityId;
    private String sentVerificationCode;
    private String receivedVerificationCode;

    private CountDownTimer timer;
    private FirebaseAuth firebaseAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks authCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        initFirebaseAuth();

        getIntentData();

        initViews();

        sendVerificationCode(phoneNumber);
    }

    @Override
    protected void onDestroy() {
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

                Toast.makeText(VerifyOtpActivity.this,
                        "Verification Failed. Try again", Toast.LENGTH_SHORT).show();
                Log.d("otp", "onVerificationFailed: " + e.getMessage());
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                super.onCodeAutoRetrievalTimeOut(s);
            }

            @Override
            public void onCodeSent(@NonNull String verificationCode,
                                   @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {

                Toast.makeText(VerifyOtpActivity.this, "OTP Sent", Toast.LENGTH_SHORT).show();

                super.onCodeSent(verificationCode, forceResendingToken);

                sentVerificationCode = verificationCode;
            }
        };
    }

    private void initViews() {

        this.textViewPhone = findViewById(R.id.text_view_phone);
        this.textViewPhone.setText(MessageFormat.format("Enter the verification code sent at\n********{0}",
                phoneNumber.substring(phoneNumber.length() - 3)));

        this.editTextVerificationCode = findViewById(R.id.edit_text_verification_code);
        this.textViewTimer = findViewById(R.id.text_view_timer);

        this.buttonVerify = findViewById(R.id.button_verify);
        this.buttonVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
            }
        });

        this.buttonResendCode = findViewById(R.id.button_resend_verification_code);
        this.buttonResendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendVerificationCode(phoneNumber);
            }
        });

        this.buttonGoBack = findViewById(R.id.button_go_back);
        this.buttonGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    private void getIntentData() {

        phoneNumber = getIntent().getStringExtra("phone_number");
        phoneNumber = "+92" + phoneNumber;

        callingActivityId = getIntent().getStringExtra("activity_id");
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
                        .setActivity(this)
                        .setCallbacks(authCallbacks)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);

        startResendOtpTimer(60);
    }

    private void completeVerification(PhoneAuthCredential credential) {

        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        setResult(RESULT_OK);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(VerifyOtpActivity.this,
                                "Verification Failed. Try again", Toast.LENGTH_SHORT).show();
                    }
                });
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
}