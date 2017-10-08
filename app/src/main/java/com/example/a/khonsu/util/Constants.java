package com.example.a.khonsu.util;

/**
 * Constants to use through out the program. So far, mostly the names and fields of database
 */

public class Constants {

    public static final class STEP {
        public static final double AVERAGE_STEP_LENGTH = 0.7874; //in meters
    }

    public static final class DATABASE {

        // TABLE LOCATIONS
        public static final String LOCATION_TABLE_NAME = "locations";
        static final String LOCATION_ID = "_id";
        public static final String LOCATION_NAME = "location_name";
        public static final String LOCATION_TYPE = "location_type";
        public static final String LOCATION_STICKER_UUID = "sticker_uuid";
        public static final String LOCATION_X = "locationX";
        public static final String LOCATION_Y = "locationY";
        public static final String LOCATION_COOR_X = "coordinateX";
        public static final String LOCATION_COOR_Y = "coordinateY";
        public static final String LOCATION_FLOOR_ID = "floor_id";

        // TABLE FLOOR
        public static final String FLOOR_TABLE_NAME = "floors";
        public static final String FLOOR_ID = "_id";
        public static final String FLOOR_NAME = "floor_name";
        public static final String FLOOR_MAP = "map_path";

        // TABLE PATH
        public static final String PATH_TABLE_NAME = "paths";
        static final String PATH_ID = "_id";
        public static final String PATH_DISTANCE = "distance";
        public static final String PATH_FLOOR_ID = "floor_id";
        public static final String PATH_START_X = "startX";
        public static final String PATH_END_X = "endX";
        public static final String PATH_START_Y = "startY";
        public static final String PATH_END_Y = "endY";
        public static final String PATH_DIRECTION = "direction";

        // TABLE ROUTE
        public static final String ROUTE_TABLE_NAME = "routes";
        static final String ROUTE_ID = "_id";
        public static final String ROUTE_FLOOR_ID = "floor_id";
        public static final String ROUTE_START_LOC = "start_locationId";
        public static final String ROUTE_END_LOC = "end_locationId";

        //TABLE ROUTE_PATH
        public static final String RP_TABLE_NAME = "route_path";
        public static final String RP_ROUTE_ID = "routeId";
        public static final String RP_PATH_ID = "pathId";

        public static final String CREATE_LOCATION_TABLE = "CREATE TABLE " + LOCATION_TABLE_NAME + " (" +
                LOCATION_ID + " INTEGER PRIMARY KEY, " +
                LOCATION_NAME + " TEXT NOT null, " +
                LOCATION_TYPE + " TEXT, " +
                LOCATION_STICKER_UUID + " TEXT, " +
                LOCATION_X + " REAL NOT null, " +
                LOCATION_Y + " REAL NOT null, " +
                LOCATION_COOR_X + " REAL, " +
                LOCATION_COOR_Y + " REAL, " +
                LOCATION_FLOOR_ID + " INTEGER NOT null," +
                "UNIQUE(" + LOCATION_COOR_X + "," + LOCATION_COOR_Y + ") ON CONFLICT IGNORE)";

        public static final String CREATE_FLOOR_TABLE = "CREATE TABLE " + FLOOR_TABLE_NAME + " (" +
                FLOOR_ID + " INTEGER PRIMARY KEY, " +
                FLOOR_NAME + " TEXT NOT null, " +
                FLOOR_MAP + " TEXT," +
                "UNIQUE(" + FLOOR_NAME + ") ON CONFLICT IGNORE)";

        public static final String CREATE_PATH_TABLE = "CREATE TABLE " + PATH_TABLE_NAME + " (" +
                PATH_ID + " INTEGER PRIMARY KEY, " +
                PATH_DISTANCE + " REAL NOT null, " +
                PATH_FLOOR_ID + " INTEGER, " +
                PATH_START_X + " REAL NOT null, " +
                PATH_END_X + " REAL NOT null, " +
                PATH_START_Y + " REAL NOT null, " +
                PATH_END_Y + " REAL NOT null," +
                PATH_DIRECTION + " TEXT NOT null, " +
                "UNIQUE(" + PATH_START_X + "," + PATH_END_X + "," + PATH_START_Y + "," + PATH_END_Y + ") ON CONFLICT IGNORE)";

        public static final String CREATE_ROUTE_TABLE = "CREATE TABLE " + ROUTE_TABLE_NAME + " (" +
                ROUTE_ID + " INTEGER PRIMARY KEY, " +
                ROUTE_FLOOR_ID + " INTEGER, " +
                ROUTE_START_LOC + " INTEGER, " +
                ROUTE_END_LOC + " INTEGER" + ")";

        public static final String CREATE_ROUTE_PATH_TABLE = "CREATE TABLE " + RP_TABLE_NAME + " (" +
                RP_ROUTE_ID + " INTEGER NOT null, " +
                RP_PATH_ID + " INTEGER NOT null" + ")";

        public static final String DROP_LOCATION_TABLE = "DROP TABLE IF EXISTS " + Constants.DATABASE.LOCATION_TABLE_NAME;
        public static final String DROP_FLOOR_TABLE = "DROP TABLE IF EXISTS " + Constants.DATABASE.FLOOR_TABLE_NAME;
        public static final String DROP_ROUTE_TABLE = "DROP TABLE IF EXISTS " + Constants.DATABASE.ROUTE_TABLE_NAME;
        public static final String DROP_PATH_TABLE = "DROP TABLE IF EXISTS " + Constants.DATABASE.PATH_TABLE_NAME;
        public static final String DROP_RP_TABLE = "DROP TABLE IF EXISTS " + Constants.DATABASE.RP_TABLE_NAME;
    }
}
