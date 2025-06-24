package com.esprit.models;

import java.util.Date;

public class Commande {
    private int id;
    private Date date;
    private String statut;
    private int clientId;
    private String shipmentAddress;
    private Date estimatedDeliveryDate;
    private double total;  // Add this field

    public Commande() {
    }

    public Commande(int id, Date date, String statut, int clientId, String shipmentAddress, Date estimatedDeliveryDate, double total) {
        this.id = id;
        this.date = date;
        this.statut = statut;
        this.clientId = clientId;
        this.shipmentAddress = shipmentAddress;
        this.estimatedDeliveryDate = estimatedDeliveryDate;
        this.total = total;
    }

    // Existing getters and setters...

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public String getShipmentAddress() {
        return shipmentAddress;
    }

    public void setShipmentAddress(String shipmentAddress) {
        this.shipmentAddress = shipmentAddress;
    }

    public Date getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public void setEstimatedDeliveryDate(Date estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }

    // Add getter and setter for total
    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
