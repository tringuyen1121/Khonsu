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
    private Location startLocation;
    @SerializedName("end_locationId")
    @Expose
    private Integer endLocationId;
    private Location endLocation;
    @SerializedName("paths")
    @Expose
    private List<Path> paths = null;

    public Route(int id, int floorId, Location start, Location end) {
        this.routeId = id;
        this.floorId = floorId;
        this.startLocation = start;
        this.endLocation = end;
    }

    public Integer getRouteId() {
        return routeId;
    }

    public Integer getFloorId() {
        return floorId;
    }

    public Integer getStartLocationId() {
        return startLocationId;
    }

    public Integer getEndLocationId() {
        return endLocationId;
    }

    public List<Path> getPaths() {
        return paths;
    }

    public void setPaths(List<Path> paths) {
        this.paths = paths;
    }

}
