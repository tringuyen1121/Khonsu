package com.example.a.khonsu.model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Route {

    @SerializedName("route_id")
    @Expose
    private Integer routeId;
    @SerializedName("floor_id")
    @Expose
    private Integer floorId;
    @SerializedName("start_locationId")
    @Expose
    private Integer startLocationId;
    @SerializedName("end_locationId")
    @Expose
    private Integer endLocationId;
    @SerializedName("paths")
    @Expose
    private List<Path> paths = null;

    public Integer getRouteId() {
        return routeId;
    }

    public void setRouteId(Integer routeId) {
        this.routeId = routeId;
    }

    public Integer getFloorId() {
        return floorId;
    }

    public void setFloorId(Integer floorId) {
        this.floorId = floorId;
    }

    public Integer getStartLocationId() {
        return startLocationId;
    }

    public void setStartLocationId(Integer startLocationId) {
        this.startLocationId = startLocationId;
    }

    public Integer getEndLocationId() {
        return endLocationId;
    }

    public void setEndLocationId(Integer endLocationId) {
        this.endLocationId = endLocationId;
    }

    public List<Path> getPaths() {
        return paths;
    }

    public void setPaths(List<Path> paths) {
        this.paths = paths;
    }

}
