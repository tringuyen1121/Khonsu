package com.example.a.khonsu.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Floor implements Serializable{

    @SerializedName("floor_id")
    @Expose
    private Integer floorId;
    @SerializedName("floor_name")
    @Expose
    private String floorName;
    @SerializedName("map_path")
    @Expose
    private String mapPath;

    public Floor(int id, String name, String path) {
        this.floorId = id;
        this.floorName = name;
        this.mapPath = path;
    }

    public String getFloorName() {
        return floorName;
    }

    public String getMapPath() {
        return mapPath;
    }
}
