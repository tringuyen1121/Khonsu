package com.example.a.khonsu;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.a.khonsu.model.Floor;
import com.example.a.khonsu.model.Location;
import com.example.a.khonsu.model.Path;
import com.example.a.khonsu.model.Route;
import com.example.a.khonsu.util.Constants;

public class DatabaseOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 4;

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
}
