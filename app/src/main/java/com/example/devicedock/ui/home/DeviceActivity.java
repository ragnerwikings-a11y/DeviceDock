package com.example.devicedock.ui.home;

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

import java.util.ArrayList;

public class DeviceActivity extends AppCompatActivity {

    private DeviceViewModel viewModel;
    private DeviceAdapter deviceAdapter;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        RecyclerView recyclerView = findViewById(R.id.recycler_view_devices);
        emptyView = findViewById(R.id.text_empty_view);

        // 1. Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(DeviceViewModel.class);

        // 2. Setup RecyclerView
        deviceAdapter = new DeviceAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(deviceAdapter);

        // 3. Observe LiveData (The MVVM Core)
        viewModel.getDevices().observe(this, devices -> {
            // This is called whenever the data changes (NSD or SQLite update)
            deviceAdapter.updateDeviceList(devices);

            // Show/Hide empty view
            emptyView.setVisibility(devices.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    // --- Activity Lifecycle ---

    @Override
    protected void onResume() {
        super.onResume();
        // Start NSD discovery (ViewModel handles the logic)
        viewModel.startDeviceDiscovery();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop NSD discovery (ViewModel handles the logic)
        viewModel.stopDeviceDiscovery();
    }
}