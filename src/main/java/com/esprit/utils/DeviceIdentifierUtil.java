package com.esprit.utils;

import java.util.UUID;
import java.util.prefs.Preferences;

public class DeviceIdentifierUtil {
    private static final String PREF_NODE = "com.esprit.deviceidentifier";
    private static final String KEY_DEVICE_ID = "deviceId";

    private static Preferences prefs = Preferences.userRoot().node(PREF_NODE);

    /**
     * Generate or retrieve a unique persistent device identifier.
     * The identifier is stored in Java Preferences to persist across sessions.
     * @return the persistent device identifier
     */
    public static String getDeviceIdentifier() {
        String deviceId = prefs.get(KEY_DEVICE_ID, null);
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
            prefs.put(KEY_DEVICE_ID, deviceId);
            System.out.println("Generated new device identifier: " + deviceId);
        } else {
            System.out.println("Retrieved existing device identifier: " + deviceId);
        }
        return deviceId;
    }
}
