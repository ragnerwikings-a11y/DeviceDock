package com.example.devicedock.ui.login;

import android.app.ComponentCaller;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.devicedock.R;
import com.example.devicedock.data.local.TokenHandler;
import com.example.devicedock.ui.home.DeviceActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class LoginActivity extends AppCompatActivity {
    private LoginViewModel viewModel;
    private GoogleSignInClient mGoogleSignInClient;
    private ProgressBar progressBar;


    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleSignInResult(task);
                } else {
                    viewModel.handleGoogleSignInFailure();
                    Toast.makeText(this, "Google Sign-In failed.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        TokenHandler tokenManager = new TokenHandler(this);
        viewModel = new ViewModelProvider(this, new LoginViewModelFactory(getApplication(),tokenManager)).get(LoginViewModel.class);

        progressBar = findViewById(R.id.progressBar);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Get ID Token for backend
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        SignInButton signInButton = findViewById(R.id.google_sign_in_button);
        signInButton.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });


        viewModel.getLoginStatus().observe(this, isSuccess -> {
            if (isSuccess) {
                Intent mainIntent = new Intent(LoginActivity.this, DeviceActivity.class);
                startActivity(mainIntent);
                finish();
            } else {
                Toast.makeText(getApplicationContext(),"Login failed",Toast.LENGTH_LONG).show();

            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            signInButton.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        });
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null && account.getIdToken() != null) {
                viewModel.handleGoogleSignInSuccess(account.getIdToken());
            } else {
                viewModel.handleGoogleSignInFailure();
                Toast.makeText(this, "Could not get ID Token.", Toast.LENGTH_LONG).show();
            }
        } catch (ApiException e) {
            viewModel.handleGoogleSignInFailure();
            Toast.makeText(this, "Sign-in failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}