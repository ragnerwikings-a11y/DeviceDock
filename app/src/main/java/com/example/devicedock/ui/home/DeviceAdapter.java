package com.example.devicedock.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.devicedock.R;
import com.example.devicedock.data.model.Device;

import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {
    private final List<Device> deviceList;
    private final int onlineColor;
    private final int offlineColor;

    public DeviceAdapter(Context context, List<Device> deviceList) {
        this.deviceList = deviceList;
        this.onlineColor = ContextCompat.getColor(context, android.R.color.holo_green_dark);
        this.offlineColor = ContextCompat.getColor(context, android.R.color.darker_gray);
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        Device device = deviceList.get(position);
        holder.nameTextView.setText(device.getName());
        holder.ipTextView.setText(device.getIpAddress());

        if (device.isOnline()) {
            holder.statusTextView.setText("Online");
            holder.statusTextView.setTextColor(onlineColor);
        } else {
            holder.statusTextView.setText("Offline");
            holder.statusTextView.setTextColor(offlineColor);
        }
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    // Custom method to update the list and notify changes
    public void updateDeviceList(List<Device> newList) {
        deviceList.clear();
        deviceList.addAll(newList);
        notifyDataSetChanged();
    }

    // ViewHolder class
    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView ipTextView;
        TextView statusTextView;

        DeviceViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.text_device_name);
            ipTextView = itemView.findViewById(R.id.text_device_ip);
            statusTextView = itemView.findViewById(R.id.text_status);
        }
    }
}
