package com.example.devicedock.data.remote;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import com.example.devicedock.data.model.Device;

import java.util.concurrent.ConcurrentHashMap;

public class NsdManagerHelper {
    private static final String TAG = "NsdManagerHelper";
   // public static final String SERVICE_TYPE = "_http._tcp."; // Common mDNS service type
    public static final String SERVICE_TYPE = "_airplay._tcp."; // Common mDNS service type

    private final NsdManager nsdManager;
    private final DiscoveryListener discoveryListener;
    private final NsdCallback callback;

    // Use a map to track services found but not yet resolved
    private final ConcurrentHashMap<String, NsdServiceInfo> foundServices = new ConcurrentHashMap<>();

    // Interface for communication back to the Activity/ViewModel
    public interface NsdCallback {
        void onServiceResolved(Device device);
        void onServiceLost(String serviceName);
    }

    public NsdManagerHelper(Context context, NsdCallback callback) {
        this.nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        this.callback = callback;
        this.discoveryListener = new DiscoveryListener();
    }

    public void startDiscovery() {
        Log.d(TAG, "Starting NSD Discovery for type: " + SERVICE_TYPE);
        foundServices.clear(); // Clear existing temporary list
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }

    public void stopDiscovery() {
        Log.d(TAG, "Stopping NSD Discovery.");
        try {
            nsdManager.stopServiceDiscovery(discoveryListener);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "NSD Discovery not running or listener already unregistered.", e);
        }
    }

    private class DiscoveryListener implements NsdManager.DiscoveryListener {
        @Override
        public void onStartDiscoveryFailed(String serviceType, int errorCode) {
            Log.e(TAG, "Discovery failed: Error code:" + errorCode);
            stopDiscovery();
        }

        @Override
        public void onStopDiscoveryFailed(String serviceType, int errorCode) {
            Log.e(TAG, "Discovery failed to stop: Error code:" + errorCode);
        }

        @Override
        public void onDiscoveryStarted(String serviceType) {
            Log.d(TAG, "Discovery started: " + serviceType);
        }

        @Override
        public void onDiscoveryStopped(String serviceType) {
            Log.d(TAG, "Discovery stopped: " + serviceType);
        }

        @Override
        public void onServiceFound(NsdServiceInfo service) {
            // Found a service, now resolve it to get IP address and port
            Log.d(TAG, "Service found: " + service.getServiceName());
            if (!foundServices.containsKey(service.getServiceName())) {
                foundServices.put(service.getServiceName(), service);
                nsdManager.resolveService(service, new ResolveListener());
            }
        }

        @Override
        public void onServiceLost(NsdServiceInfo service) {
            // A previously found service is now offline
            Log.d(TAG, "Service lost: " + service.getServiceName());
            foundServices.remove(service.getServiceName());
            callback.onServiceLost(service.getServiceName());
        }
    }

    private class ResolveListener implements NsdManager.ResolveListener {
        @Override
        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Log.e(TAG, "Resolve failed: " + errorCode);
            foundServices.remove(serviceInfo.getServiceName());
        }

        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {
            Log.d(TAG, "Service resolved: " + serviceInfo.getServiceName());

            // Get IP Address and Port from resolved service info
            String ipAddress = serviceInfo.getHost().getHostAddress();
            String serviceName = serviceInfo.getServiceName();
            String serviceType = serviceInfo.getServiceType();

            if (ipAddress != null) {
                Device device = new Device(serviceName, ipAddress, serviceType);
                // Inform the Activity/ViewModel
                callback.onServiceResolved(device);
            }
            foundServices.remove(serviceName); // Resolution complete
        }
    }
}
