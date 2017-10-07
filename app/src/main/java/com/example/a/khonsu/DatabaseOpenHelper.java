package com.example.a.khonsu;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

import com.example.a.khonsu.model.Floor;
import com.example.a.khonsu.model.Location;
import com.example.a.khonsu.model.Path;
import com.example.a.khonsu.model.Route;
import com.example.a.khonsu.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class DatabaseOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 6;

    private static final String DATABASE_NAME = "Khonsu.db";

    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Constants.DATABASE.CREATE_LOCATION_TABLE);
        db.execSQL(Constants.DATABASE.CREATE_FLOOR_TABLE);
        db.execSQL(Constants.DATABASE.CREATE_PATH_TABLE);
        db.execSQL(Constants.DATABASE.CREATE_ROUTE_TABLE);
        db.execSQL(Constants.DATABASE.CREATE_ROUTE_PATH_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(Constants.DATABASE.DROP_LOCATION_TABLE);
        db.execSQL(Constants.DATABASE.DROP_FLOOR_TABLE);
        db.execSQL(Constants.DATABASE.DROP_ROUTE_TABLE);
        db.execSQL(Constants.DATABASE.DROP_PATH_TABLE);
        db.execSQL(Constants.DATABASE.DROP_RP_TABLE);

        onCreate(db);
    }

    public void insertLocation(Location loc) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Constants.DATABASE.LOCATION_NAME, loc.getLoactionName());
        values.put(Constants.DATABASE.LOCATION_TYPE, loc.getLocationType());
        values.put(Constants.DATABASE.LOCATION_STICKER_UUID, loc.getStickerUuid());
        values.put(Constants.DATABASE.LOCATION_X, loc.getLocationX());
        values.put(Constants.DATABASE.LOCATION_Y, loc.getLocationY());
        values.put(Constants.DATABASE.LOCATION_COOR_X, loc.getCoordinateX());
        values.put(Constants.DATABASE.LOCATION_COOR_Y, loc.getCoordinateY());
        values.put(Constants.DATABASE.LOCATION_FLOOR_ID, loc.getFloorId());
        db.insert(Constants.DATABASE.LOCATION_TABLE_NAME, null, values);

        values.clear();
        db.close();
    }

    public void insertFloor(Floor floor) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Constants.DATABASE.FLOOR_NAME, floor.getFloorName());
        values.put(Constants.DATABASE.FLOOR_MAP, floor.getMapPath());
        db.insert(Constants.DATABASE.FLOOR_TABLE_NAME, null, values);

        values.clear();
        db.close();
    }

    public void insertPath(Path path) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Constants.DATABASE.PATH_DISTANCE, path.getDistance());
        values.put(Constants.DATABASE.PATH_FLOOR_ID, path.getFloorId());
        values.put(Constants.DATABASE.PATH_START_X, path.getStartX());
        values.put(Constants.DATABASE.PATH_END_X, path.getEndX());
        values.put(Constants.DATABASE.PATH_START_Y, path.getStartY());
        values.put(Constants.DATABASE.PATH_END_Y, path.getEndY());
        values.put(Constants.DATABASE.PATH_DIRECTION, path.getDirection());
        db.insert(Constants.DATABASE.PATH_TABLE_NAME, null, values);

        values.clear();
        db.close();
    }

    public void insertRoute(Route route) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Constants.DATABASE.ROUTE_FLOOR_ID, route.getFloorId());
        values.put(Constants.DATABASE.ROUTE_START_LOC, route.getStartLocationId());
        values.put(Constants.DATABASE.ROUTE_END_LOC, route.getEndLocationId());
        db.insert(Constants.DATABASE.ROUTE_TABLE_NAME, null, values);

        values.clear();
        db.close();
    }

    public void insertRoutePath(int routeId, int pathId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Constants.DATABASE.RP_ROUTE_ID, routeId);
        values.put(Constants.DATABASE.RP_PATH_ID, pathId);
        db.insert(Constants.DATABASE.RP_TABLE_NAME, null, values);

        values.clear();
        db.close();
    }

    public Location getStartLocation(String identifier) {
        Location loc = null;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(Constants.DATABASE.LOCATION_TABLE_NAME,
                null,
                Constants.DATABASE.LOCATION_STICKER_UUID + "=? OR " + Constants.DATABASE.LOCATION_NAME + "=?",
                new String[] {identifier, identifier}, null, null, null);
        if (cursor.moveToFirst()) {
            String type = cursor.getString(2).trim().toLowerCase();
            Location.LOCATION_TYPE locType = getType(type);

            loc = new Location(cursor.getInt(0),
                    cursor.getString(1),
                    locType,
                    cursor.getString(3),
                    cursor.getDouble(4),
                    cursor.getDouble(5),
                    cursor.getDouble(6),
                    cursor.getDouble(7),
                    cursor.getInt(8));
            cursor.close();
        }
        db.close();
        return loc;
    }

    public List<Location> getAllLocation() {
        List<Location> locations = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(Constants.DATABASE.LOCATION_TABLE_NAME, null, null, null, null, null, null);
        if(cursor.moveToFirst()){
            do{
                String type = cursor.getString(2).trim().toLowerCase();
                Location.LOCATION_TYPE locType = getType(type);

                Location l = new Location(cursor.getInt(0),
                        cursor.getString(1),
                        locType,
                        cursor.getString(3),
                        cursor.getDouble(4),
                        cursor.getDouble(5),
                        cursor.getDouble(6),
                        cursor.getDouble(7),
                        cursor.getInt(8));
                locations.add(l);
            }while(cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return locations;
    }

    private Location.LOCATION_TYPE getType(String type) {

        switch (type) {
            case "classroom":
                return Location.LOCATION_TYPE.CLASSROOM;
            case "elevator":
                return Location.LOCATION_TYPE.ELEVATOR;
            case "staircase":
                return Location.LOCATION_TYPE.STAIRCASE;
            case "exit":
                return Location.LOCATION_TYPE.EXIT;
            case "entry":
                return Location.LOCATION_TYPE.ENTRY;
            default:
                return Location.LOCATION_TYPE.OTHER;
        }
    }

    public Floor getFloor(int floorId) {
        Floor floor = null;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(Constants.DATABASE.FLOOR_TABLE_NAME,
                null,
                Constants.DATABASE.FLOOR_ID + "=?",
                new String[] {String.valueOf(floorId)}, null, null, null);
        if (cursor.moveToFirst()) {
            floor = new Floor(cursor.getInt(0), cursor.getString(1), cursor.getString(2));
            cursor.close();
        }
        db.close();
        return floor;
    }

    public Route getRoute(Location startLocation, Location endLocation) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(Constants.DATABASE.ROUTE_TABLE_NAME,
                null,
                Constants.DATABASE.ROUTE_START_LOC + "=? AND " + Constants.DATABASE.ROUTE_END_LOC + "=?",
                new String[] { String.valueOf(startLocation.getLocationId()),String.valueOf(endLocation.getLocationId())},
                null, null, null);
        Route r = null;
        if(cursor.moveToFirst()){
            r = new Route(cursor.getInt(0), cursor.getInt(1), startLocation, endLocation);
            r.setPaths(getPaths( r.getRouteId() ));
            cursor.close();
        }
        db.close();
        return r;
    }

    private List<Path> getPaths(int routeId) {
        List<Path> paths = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * " +
                " FROM " + Constants.DATABASE.PATH_TABLE_NAME +" WHERE pathId IN " +
                " (SELECT pathId FROM " + Constants.DATABASE.RP_TABLE_NAME + " WHERE routeId = " + routeId +")";
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.moveToFirst()){
            do{
                String direction = cursor.getString(7).trim().toLowerCase();
                Path.DIRECTION dir = getDirection(direction);

                Path p = new Path(cursor.getInt(0),
                        cursor.getDouble(1),
                        cursor.getInt(2),
                        cursor.getDouble(3),
                        cursor.getDouble(4),
                        cursor.getDouble(5),
                        cursor.getDouble(6),
                        dir);
                paths.add(p);
            }while(cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return paths;
    }

    private Path.DIRECTION getDirection(String dir) {

        switch (dir) {
            case "north":
                return Path.DIRECTION.NORTH;
            case "east":
                return Path.DIRECTION.EAST;
            case "south":
                return Path.DIRECTION.SOUTH;
            case "west":
                return Path.DIRECTION.WEST;
            case "north-east":
                return Path.DIRECTION.NORTH_EAST;
            case "north-west":
                return Path.DIRECTION.NORTH_WEST;
            case "south-east":
                return Path.DIRECTION.SOUTH_EAST;
            case "south-west":
                return Path.DIRECTION.SOUTH_WEST;
            default:
                return Path.DIRECTION.UNDEFINED;
        }
    }
}
