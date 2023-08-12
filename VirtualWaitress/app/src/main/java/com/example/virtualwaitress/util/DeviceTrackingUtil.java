package com.example.virtualwaitress.util;

import java.util.HashMap;
import java.util.Map;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DeviceTrackingUtil {
    public static void registerDevice(String userId, String deviceId, String deviceName) {
        DatabaseReference deviceRef = FirebaseDatabase.getInstance().getReference()
                .child("device_tracking")
                .child(userId)
                .child(deviceId);

        long loginTimestamp = System.currentTimeMillis();
        Map<String, Object> deviceInfo = new HashMap<>();
        deviceInfo.put("device_name", deviceName);
        deviceInfo.put("login_timestamp", loginTimestamp);

        deviceRef.setValue(deviceInfo);
    }

    public static void deregisterDevice(String userId, String deviceId) {
        DatabaseReference deviceRef = FirebaseDatabase.getInstance().getReference()
                .child("device_tracking")
                .child(userId)
                .child(deviceId);

        deviceRef.removeValue();
    }
}
