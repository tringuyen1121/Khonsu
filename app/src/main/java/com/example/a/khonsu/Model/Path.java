package com.example.a.khonsu.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Path {

    @SerializedName("path_id")
    @Expose
    private Integer pathId;
    @SerializedName("floor_id")
    @Expose
    private Integer floorId;
    @SerializedName("distance")
    @Expose
    private Double distance;
    @SerializedName("startX")
    @Expose
    private Double startX;
    @SerializedName("endX")
    @Expose
    private Double endX;
    @SerializedName("startY")
    @Expose
    private Double startY;
    @SerializedName("endY")
    @Expose
    private Double endY;

    public Integer getPathId() {
        return pathId;
    }

    public void setPathId(Integer pathId) {
        this.pathId = pathId;
    }

    public Integer getFloorId() {
        return floorId;
    }

    public void setFloorId(Integer floorId) {
        this.floorId = floorId;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Double getStartX() {
        return startX;
    }

    public void setStartX(Double startX) {
        this.startX = startX;
    }

    public Double getEndX() {
        return endX;
    }

    public void setEndX(Double endX) {
        this.endX = endX;
    }

    public Double getStartY() {
        return startY;
    }

    public void setStartY(Double startY) {
        this.startY = startY;
    }

    public Double getEndY() {
        return endY;
    }

    public void setEndY(Double endY) {
        this.endY = endY;
    }

}
