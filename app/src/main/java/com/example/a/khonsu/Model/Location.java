package com.example.a.khonsu.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Location implements Serializable {

    public enum LOCATION_TYPE{
        CLASSROOM,
        ELEVATOR,
        STAIRCASE,
        EXIT,
        ENTRY,
        OTHER
    }

    @SerializedName("location_id")
    @Expose
    private Integer locationId;
    @SerializedName("loaction_name")
    @Expose
    private String loactionName;
    @SerializedName("location_type")
    @Expose
    private String locationType;
    private LOCATION_TYPE locType;
    @SerializedName("sticker_uuid")
    @Expose
    private String stickerUuid;
    @SerializedName("locationX")
    @Expose
    private Double locationX;
    @SerializedName("locationY")
    @Expose
    private Double locationY;
    @SerializedName("coordinateX")
    @Expose
    private Double coordinateX;
    @SerializedName("coordinateY")
    @Expose
    private Double coordinateY;
    @SerializedName("floor_id")
    @Expose
    private Integer floorId;

    public Location (int id, String name, LOCATION_TYPE type, String uuid, Double locX, Double locY, Double coorX, Double coorY, int floorId) {
        this.locationId = id;
        this.loactionName = name;
        this.locType = type;
        this.stickerUuid = uuid;
        this.locationX = locX;
        this.locationY = locY;
        this.coordinateX = coorX;
        this.coordinateY = coorY;
        this.floorId = floorId;
    }

    public Integer getLocationId() {
        return locationId;
    }

    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }

    public String getLoactionName() {
        return loactionName;
    }

    public void setLoactionName(String loactionName) {
        this.loactionName = loactionName;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public String getStickerUuid() {
        return stickerUuid;
    }

    public void setStickerUuid(String stickerUuid) {
        this.stickerUuid = stickerUuid;
    }

    public Double getLocationX() {
        return locationX;
    }

    public void setLocationX(Double locationX) {
        this.locationX = locationX;
    }

    public Double getLocationY() {
        return locationY;
    }

    public void setLocationY(Double locationY) {
        this.locationY = locationY;
    }

    public Double getCoordinateX() {
        return coordinateX;
    }

    public void setCoordinateX(Double coordinateX) {
        this.coordinateX = coordinateX;
    }

    public Double getCoordinateY() {
        return coordinateY;
    }

    public void setCoordinateY(Double coordinateY) {
        this.coordinateY = coordinateY;
    }

    public Integer getFloorId() {
        return floorId;
    }

    public void setFloorId(Integer floorId) {
        this.floorId = floorId;
    }

}

