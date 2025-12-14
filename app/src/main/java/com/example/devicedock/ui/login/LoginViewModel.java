package com.example.devicedock.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.devicedock.data.local.TokenHandler;

public class LoginViewModel extends ViewModel {
    private final TokenHandler tokenHandler;
    private final MutableLiveData<Boolean> loginStatus = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public LoginViewModel(TokenHandler tokenHandler) {
        this.tokenHandler = tokenHandler;
    }

    public LiveData<Boolean> getLoginStatus() {
        return loginStatus;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void handleGoogleSignInSuccess(String idToken) {
        isLoading.setValue(true);
        String appAccessToken = "app_token_from_backend_" + System.currentTimeMillis();
        tokenHandler.saveAuthToken(appAccessToken);
        isLoading.setValue(false);
        loginStatus.setValue(true);
    }

    public void handleGoogleSignInFailure() {
        loginStatus.setValue(false);
    }

    public void attemptSilentAuthentication() {
        if (!tokenHandler.hasValidToken()) {
            loginStatus.setValue(false);
            return;
        }

        isLoading.setValue(true);
        boolean refreshSuccess = true;

        if (refreshSuccess) {
            String newToken = "refreshed_app_token_" + System.currentTimeMillis();
            tokenHandler.saveAuthToken(newToken);
            isLoading.setValue(false);
            loginStatus.setValue(true);
        } else {

            forceLogout();
        }
    }

    public void forceLogout() {
        tokenHandler.clearAuthToken();
        isLoading.setValue(false);
        loginStatus.setValue(false);
    }


}
