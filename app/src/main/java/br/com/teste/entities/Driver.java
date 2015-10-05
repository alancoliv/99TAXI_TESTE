package br.com.teste.entities;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

/**
 * Created by aoliveira on 29/09/15.
 */
public class Driver {

    @SerializedName("latitude")
    private double latitude;
    @SerializedName("longitude")
    private double longitude;
    @SerializedName("driverId")
    private int driverId;
    @SerializedName("driverAvailable")
    private Boolean driverAvailable;


    public double getLatitude() {
        return latitude;
    }

    public Driver setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public double getLongitude() {
        return longitude;
    }

    public Driver setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    public long getDriverId() {
        return driverId;
    }

    public Driver setDriverId(int driverId) {
        this.driverId = driverId;
        return this;
    }

    public Boolean getDriverAvailable() {
        return driverAvailable;
    }

    public Driver setDriverAvailable(Boolean driverAvailable) {
        this.driverAvailable = driverAvailable;
        return this;
    }

}
