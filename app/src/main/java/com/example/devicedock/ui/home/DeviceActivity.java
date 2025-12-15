package com.example.devicedock.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.devicedock.R;
import com.example.devicedock.data.model.Device;
import com.example.devicedock.data.remote.NsdManagerHelper;
import com.example.devicedock.ui.detail.DetailActivity;

import java.util.ArrayList;

public class DeviceActivity extends AppCompatActivity implements NsdManagerHelper.NsdCallback, DeviceAdapter.OnDeviceClickListener{

    private DeviceViewModel viewModel;
    private DeviceAdapter deviceAdapter;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        RecyclerView recyclerView = findViewById(R.id.recycler_view_devices);
        emptyView = findViewById(R.id.text_empty_view);
        viewModel = new ViewModelProvider(this).get(DeviceViewModel.class);
        deviceAdapter = new DeviceAdapter(this, new ArrayList<>(),this);
        recyclerView.setAdapter(deviceAdapter);
        viewModel.getDevices().observe(this, devices -> {
            deviceAdapter.updateDeviceList(devices);
            emptyView.setVisibility(devices.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onDeviceClick(Device device) {
        if (device.getIpAddress() != null) {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_LOCAL_IP, device.getIpAddress());
            intent.putExtra(DetailActivity.EXTRA_DEVICE_NAME, device.getName());
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.startDeviceDiscovery();
    }

    @Override
    protected void onPause() {
        super.onPause();
        viewModel.stopDeviceDiscovery();
    }

    @Override
    public void onServiceResolved(Device device) {

    }

    @Override
    public void onServiceLost(String serviceName) {

    }
}