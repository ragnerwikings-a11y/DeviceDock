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

    private final MutableLiveData<List<Device>> discoveredDevices = new MutableLiveData<>();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public DeviceRepository(Context context) {
        this.dbHelper = new DeviceDbHelper(context);
        this.nsdHelper = new NsdManagerHelper(context, this);
        loadInitialDevices();
    }


    public LiveData<List<Device>> getDiscoveredDevices() {
        return discoveredDevices;
    }

    public void startDiscovery() {
        setAllDevicesOffline();

        nsdHelper.startDiscovery();
    }

    public void stopDiscovery() {
        nsdHelper.stopDiscovery();
    }


    private void loadInitialDevices() {
        executor.execute(() -> {
            List<Device> initialList = dbHelper.getAllDevices();
            discoveredDevices.postValue(initialList);
        });
    }

    private void setAllDevicesOffline() {
        List<Device> currentList = discoveredDevices.getValue();
        if (currentList != null) {
            for (Device device : currentList) {
                device.setOnline(false);
            }
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

            discoveredDevices.postValue(currentList);
        });
    }


    @Override
    public void onServiceResolved(Device device) {
        updateDeviceStatus(device, true);
    }

    @Override
    public void onServiceLost(String serviceName) {
        Device lostDevice = new Device(serviceName, null, NsdManagerHelper.SERVICE_TYPE);
        updateDeviceStatus(lostDevice, false);
    }
}
