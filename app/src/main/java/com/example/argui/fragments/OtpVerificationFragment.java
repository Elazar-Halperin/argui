package com.example.argui.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.argui.MainActivity;
import com.example.argui.R;
import com.example.argui.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;


public class OtpVerificationFragment extends Fragment {
    String verificationId;
    String name;
    String phoneNumber;

    FirebaseAuth auth;

    LinearLayout ll_etHolder;
    Button btn_complete;

    CollectionReference db;

    NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance().collection("users");
        return inflater.inflate(R.layout.fragment_otp_verification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        ll_etHolder = v.findViewById(R.id.ll_etHolder);
        btn_complete = v.findViewById(R.id.btn_complete);

        navController = Navigation.findNavController(v);


        Bundle bundle = getArguments() == null ? new Bundle() : getArguments();

        verificationId = bundle.getString("verificationId");
        phoneNumber = bundle.getString("phoneNumber");
        name = bundle.getString("name");

        Log.d("check", "onViewCreated: " + bundle);

        addETListeners();


        btn_complete.setOnClickListener(view -> {
            if(verificationId == null) {
                Toast.makeText(getActivity(), "Sorry we cant do this right now, try again later", Toast.LENGTH_SHORT).show();
            }
            btn_complete.setEnabled(false);
            verificateCode();
        });
    }

    private void verificateCode() {
        String typedCode = getTypedCode();
        if(typedCode.isEmpty() && typedCode.length() < 6) {
            btn_complete.setEnabled(true);
            Toast.makeText(getActivity(), "playes type the whole code", Toast.LENGTH_SHORT).show();
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, typedCode);

        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                   if(task.isSuccessful()) {
                       addUserInDatabase();
                   }
                });

    }

    private void addUserInDatabase() {
        String uid = auth.getUid();
        UserModel userModel = new UserModel(uid, name, phoneNumber);
        db.document(uid).set(userModel).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                getActivity().finish();
                startActivity(new Intent(getActivity(), MainActivity.class));
            }
        });
    }

    private String getTypedCode() {
        StringBuilder code = new StringBuilder();
        for(int i = 0; i < ll_etHolder.getChildCount(); i++) {
            EditText et = (EditText) ll_etHolder.getChildAt(i);
            code.append(et.getText().toString().trim());
        }

        return code.toString();

    }

    private void addETListeners() {
        for(int i = 0; i < ll_etHolder.getChildCount();i ++) {
            EditText editText = (EditText) ll_etHolder.getChildAt(i);
            if(i==0) editText.requestFocus();
            editText.setText(" ");
            int finalI = i;

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                    String text = charSequence.toString();
                    Log.d("checking2", "text: " + text +"\nstart: " + start + "\n after: " + after + "\ncount: " + count);
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                    String text = charSequence.toString();
//                    if(text.isBlank())
                    Log.d("checking", "text: " + text +"\nstart: " + start + "\n before: " + before + "\ncount: " + count);

                    if(before == 0 && count == 1 && finalI < ll_etHolder.getChildCount() - 1) {
                        ll_etHolder.getChildAt(finalI + 1).requestFocus();

                    }
                    if(before == 1 && count == 0 && finalI > 0) {
                        ll_etHolder.getChildAt(finalI - 1).requestFocus();
                    }


                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }
    }


}