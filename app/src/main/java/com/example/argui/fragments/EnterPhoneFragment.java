package com.example.argui.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.argui.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;


public class EnterPhoneFragment extends Fragment {
    EditText et_name, et_phoneNumber;
    Button btn_getOtp;
    FirebaseAuth auth;

    NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // initialize the FirebaseAuth.
        auth = FirebaseAuth.getInstance();
        return inflater.inflate(R.layout.fragment_enter_phone, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // Get the views from the layout file.
        et_name = v.findViewById(R.id.et_name);
        et_phoneNumber = v.findViewById(R.id.et_phoneNumber);

        btn_getOtp = v.findViewById(R.id.btn_getOtp);

        // initialize the navigation.
        navController = Navigation.findNavController(v);

        // send code after button is clicked
        // and navigate to next fragment.

        btn_getOtp.setOnClickListener(view -> {
            sendOtpCode();
        });


    }

    private void sendOtpCode() {
        btn_getOtp.setEnabled(false);

        String phoneNumber = "+972" + et_phoneNumber.getText().toString().trim();

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(getActivity())
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        // Enable button and hide progress bar
                        btn_getOtp.setEnabled(true);

                        // Display verification failed message
                        Toast.makeText(getActivity(), "Verification failed", Toast.LENGTH_LONG).show();

                        e.printStackTrace();

                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);

                        Bundle bundle = new Bundle();
                        bundle.putString("verificationId", s);
                        bundle.putString("name", et_name.getText().toString().trim());
                        bundle.putString("phoneNumber", phoneNumber);

                        Log.d("code sent", "Code sent" + bundle.toString());


                        navController.navigate(R.id.action_enterPhoneFragment_to_otpVerificationFragment, bundle);

                    }
                }).build();

        PhoneAuthProvider.verifyPhoneNumber(options);

    }
}