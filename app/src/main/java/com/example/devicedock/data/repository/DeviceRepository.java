package com.example.devicedock.data.repository;


import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.devicedock.data.model.Device;
import com.example.devicedock.data.model.DeviceDbHelper;
import com.example.devicedock.data.remote.NsdManagerHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeviceRepository implements NsdManagerHelper.NsdCallback{
    private static final String TAG = "DeviceRepository";

    private final DeviceDbHelper dbHelper;
    private final NsdManagerHelper nsdHelper;

    // LiveData to hold the list of devices (the single source of truth)
    private final MutableLiveData<List<Device>> discoveredDevices = new MutableLiveData<>();

    // Executor for database operations (to avoid blocking the main thread)
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public DeviceRepository(Context context) {
        this.dbHelper = new DeviceDbHelper(context);
        this.nsdHelper = new NsdManagerHelper(context, this);
        // Load initial data from DB immediately
        loadInitialDevices();
    }

    // --- Public methods for ViewModel ---

    public LiveData<List<Device>> getDiscoveredDevices() {
        return discoveredDevices;
    }

    public void startDiscovery() {
        // 1. Mark all existing devices as offline
        setAllDevicesOffline();

        // 2. Start mDNS discovery
        nsdHelper.startDiscovery();
    }

    public void stopDiscovery() {
        nsdHelper.stopDiscovery();
    }

    // --- Internal Data Management ---

    private void loadInitialDevices() {
        executor.execute(() -> {
            List<Device> initialList = dbHelper.getAllDevices();
            // Post the data to LiveData, which will notify the ViewModel/Activity
            discoveredDevices.postValue(initialList);
        });
    }

    private void setAllDevicesOffline() {
        List<Device> currentList = discoveredDevices.getValue();
        if (currentList != null) {
            for (Device device : currentList) {
                device.setOnline(false);
            }
            // Notify observers that the list has been updated (all offline)
            discoveredDevices.postValue(currentList);
        }
    }

    private void updateDeviceStatus(Device newDevice, boolean isOnline) {
        executor.execute(() -> {
            List<Device> currentList = discoveredDevices.getValue();
            if (currentList == null) {
                currentList = new ArrayList<>();
            }

            int index = currentList.indexOf(newDevice);

            if (index != -1) {
                // Update existing device
                Device existingDevice = currentList.get(index);
                existingDevice.setOnline(isOnline);
                if (isOnline) {
                    existingDevice.setIpAddress(newDevice.getIpAddress());
                }
            } else if (isOnline) {
                // New device discovered
                currentList.add(newDevice);
                // Save to database
                long id = dbHelper.saveDevice(newDevice);
                newDevice.setId(id); // Update ID in model
            }

            // Post the updated list to LiveData
            discoveredDevices.postValue(currentList);
        });
    }

    // --- NsdManagerHelper.NsdCallback implementation ---

    @Override
    public void onServiceResolved(Device device) {
        // Service found and resolved (online)
        updateDeviceStatus(device, true);
    }

    @Override
    public void onServiceLost(String serviceName) {
        // Service lost (offline)
        Device lostDevice = new Device(serviceName, null, NsdManagerHelper.SERVICE_TYPE);
        updateDeviceStatus(lostDevice, false);
    }
}
