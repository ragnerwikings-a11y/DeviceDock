package com.example.devicedock.ui.login;

import android.app.Application;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.devicedock.data.local.TokenHandler;

public class LoginViewModelFactory implements ViewModelProvider.Factory {

    private final TokenHandler tokenHandler;
    private final Application application;


    public LoginViewModelFactory(Application application,TokenHandler tokenHandler) {
        this.tokenHandler = tokenHandler;
        this.application = application;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LoginViewModel.class)) {

            try {
                return (T) new LoginViewModel(tokenHandler);
            } catch (Exception e) {
                throw new RuntimeException("Cannot create an instance of LoginViewModel", e);
            }
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
