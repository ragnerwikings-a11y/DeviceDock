package com.example.devicedock.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.devicedock.data.model.Device;
import com.example.devicedock.data.repository.DeviceRepository;

import java.util.List;

public class DeviceViewModel extends AndroidViewModel {
    private final DeviceRepository repository;
    private final LiveData<List<Device>> devices;

    public DeviceViewModel(@NonNull Application application) {
        super(application);
        // Repository is instantiated here, giving it the Application Context
        repository = new DeviceRepository(application.getApplicationContext());

        // Expose the devices LiveData from the Repository
        devices = repository.getDiscoveredDevices();
    }

    // --- Public methods for Activity/Fragment to call ---

    public LiveData<List<Device>> getDevices() {
        return devices;
    }

    /**
     * Starts the NSD discovery process.
     */
    public void startDeviceDiscovery() {
        repository.startDiscovery();
    }

    /**
     * Stops the NSD discovery process.
     */
    public void stopDeviceDiscovery() {
        repository.stopDiscovery();
    }

    // This method is called when the ViewModel is destroyed (e.g., Activity finished)
    @Override
    protected void onCleared() {
        super.onCleared();
        repository.stopDiscovery(); // Ensure discovery is stopped
        // Note: The Repository's Executor is left to clean up by the system,
        // but for complex apps, you would want to manually shut it down here.
    }
}
