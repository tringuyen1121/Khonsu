package com.example.a.khonsu.model;

import com.example.a.khonsu.util.Constants;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Path implements Serializable{

    public enum DIRECTION {
        NORTH,
        WEST,
        SOUTH,
        EAST,
        NORTH_EAST,
        NORTH_WEST,
        SOUTH_EAST,
        SOUTH_WEST,
        UNDEFINED
    }

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
    @SerializedName("direction")
    @Expose
    private String direction;
    private DIRECTION dir;

    public Path(int id, Double distance, int floorId, Double startX, Double endX, Double startY, Double endY, DIRECTION dir) {
        this.pathId = id;
        this.floorId = floorId;
        this.distance = distance;
        this.startX = startX;
        this.endX = endX;
        this.startY = startY;
        this.endY = endY;
        this.dir = dir;
    }

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

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }


}
