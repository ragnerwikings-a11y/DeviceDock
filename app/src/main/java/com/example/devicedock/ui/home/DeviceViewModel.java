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
        repository = new DeviceRepository(application.getApplicationContext());
        devices = repository.getDiscoveredDevices();
    }

    public LiveData<List<Device>> getDevices() {
        return devices;
    }

    public void startDeviceDiscovery() {
        repository.startDiscovery();
    }

    public void stopDeviceDiscovery() {
        repository.stopDiscovery();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.stopDiscovery();
    }
}
