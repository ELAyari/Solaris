package com.esprit.models;

import java.util.Date;

public class MFAPersistentToken {
    private int id;
    private int userId;
    private String deviceIdentifier;
    private String token;
    private Date expiryDate;

    public MFAPersistentToken() {
    }

    public MFAPersistentToken(int userId, String deviceIdentifier, String token, Date expiryDate) {
        this.userId = userId;
        this.deviceIdentifier = deviceIdentifier;
        this.token = token;
        this.expiryDate = expiryDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(String deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }
}
