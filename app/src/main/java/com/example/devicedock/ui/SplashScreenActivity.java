package com.example.devicedock.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.devicedock.R;
import com.example.devicedock.data.local.TokenHandler;
import com.example.devicedock.ui.home.DeviceActivity;
import com.example.devicedock.ui.login.LoginActivity;
import com.example.devicedock.ui.login.LoginViewModel;
import com.example.devicedock.ui.login.LoginViewModelFactory;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onStart() {
        super.onStart();


        TokenHandler tokenHandler = new TokenHandler(this);
        LoginViewModelFactory factory = new LoginViewModelFactory(getApplication(), tokenHandler);
        LoginViewModel viewModel = new ViewModelProvider(this, factory).get(LoginViewModel.class);
        viewModel.getLoginStatus().observe(this, isSuccess -> {
            Intent nextIntent;
            if (isSuccess) {
                nextIntent = new Intent(this, DeviceActivity.class);
            } else {
                nextIntent = new Intent(this, LoginActivity.class);
            }
            startActivity(nextIntent);
            finish();
        });

        viewModel.attemptSilentAuthentication();
    }
}