package com.example.a.khonsu.model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ServerObject {

    @SerializedName("location")
    @Expose
    private List<Location> location = null;
    @SerializedName("floors")
    @Expose
    private List<Floor> floors = null;
    @SerializedName("routes")
    @Expose
    private List<Route> routes = null;

    public List<Location> getLocation() {
        return location;
    }

    public void setLocation(List<Location> location) {
        this.location = location;
    }

    public List<Floor> getFloors() {
        return floors;
    }

    public void setFloors(List<Floor> floors) {
        this.floors = floors;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }
}
