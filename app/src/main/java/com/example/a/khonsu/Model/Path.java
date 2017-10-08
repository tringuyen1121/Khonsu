package com.example.a.khonsu.model;

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

    public Integer getFloorId() {
        return floorId;
    }

    public Double getDistance() {
        return distance;
    }

    public Double getStartX() {
        return startX;
    }

    public Double getEndX() {
        return endX;
    }

    public Double getStartY() {
        return startY;
    }


    public Double getEndY() {
        return endY;
    }

    public String getDirection() {
        return direction;
    }

    public Path.DIRECTION getDir() {
        return dir;
    }
}
