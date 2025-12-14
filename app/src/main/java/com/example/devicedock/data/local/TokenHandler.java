package com.example.devicedock.data.local;


import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class TokenHandler {
    private static final String PREF_FILE_NAME = "secure_auth_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private final SharedPreferences prefs;

    public TokenHandler(Context context) {
        try {
            // 1. Create or retrieve the Master Key from the Android Keystore
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);

            // 2. Initialize EncryptedSharedPreferences
            this.prefs = EncryptedSharedPreferences.create(
                    PREF_FILE_NAME,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

        } catch (GeneralSecurityException | IOException e) {
            // Log the error and handle it gracefully (e.g., by falling back to
            // a less secure storage or forcing re-login)
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize SecureTokenManager", e);
        }
    }

    public void saveAuthToken(String token) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply();
    }

    public String getAuthToken() {
        // Default value is null if not found
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public void clearAuthToken() {
        prefs.edit().remove(KEY_ACCESS_TOKEN).apply();
    }

    public boolean hasValidToken() {
        // Checks if a token is present
        return getAuthToken() != null;
    }
}
