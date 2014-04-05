package de.niemannsland.timelogger.storage;

import android.provider.BaseColumns;

/**
 * Created by ChristophN on 30.03.2014.
 */
public final class TimeloggerContract {

    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String REAL_TYPE = " REAL";
    private static final String COMMA_SEP = ", ";

    public static final String DATABASE_NAME = "Timelogger.db";
    public static final int DATABASE_VERSION = 7;

    public static final String CREATE_PROJECT =
            "CREATE TABLE "
                    + Project.TABLE_NAME + "("
                    + Project._ID                  + INT_TYPE  + " PRIMARY KEY AUTOINCREMENT" + COMMA_SEP
                    + Project.COLUMN_NAME_NAME     + TEXT_TYPE + " UNIQUE" + COMMA_SEP
                    + Project.COLUMN_NAME_ACTIVE   + INT_TYPE  + COMMA_SEP
                    + Project.COLUMN_NAME_BOOKABLE + INT_TYPE  + COMMA_SEP
                    + Project.COLUMN_NAME_PAID     + INT_TYPE  + COMMA_SEP
                    + Project.COLUMN_NAME_PRIVATE  + INT_TYPE
                    + ")";

    public static final String CREATE_EVENT =
            "CREATE TABLE "
                    + Event.TABLE_NAME + "("
                    + Event._ID                 + INT_TYPE  + " PRIMARY KEY AUTOINCREMENT" + COMMA_SEP
                    + Event.COLUMN_NAME_PROJECT + TEXT_TYPE + COMMA_SEP
                    + Event.COLUMN_NAME_START   + INT_TYPE  + COMMA_SEP
                    + Event.COLUMN_NAME_END     + INT_TYPE
                    + ")";

    public static final String DROP_PROJECT = "DROP TABLE " + Project.TABLE_NAME;
    public static final String DROP_EVENT = "DROP TABLE " + Event.TABLE_NAME;

    // to prevent accidental instantiation
    // provide an empty constructor
    public TimeloggerContract() {}

    public static abstract class Project implements BaseColumns {
        public static final String TABLE_NAME = "project";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_PAID = "paid";
        public static final String COLUMN_NAME_BOOKABLE = "bookable";
        public static final String COLUMN_NAME_ACTIVE = "active";
        public static final String COLUMN_NAME_PRIVATE = "private";
    }

    public static abstract class Event implements BaseColumns {
        public static final String TABLE_NAME = "event";
        public static final String COLUMN_NAME_PROJECT = "project";
        public static final String COLUMN_NAME_START = "start";
        public static final String COLUMN_NAME_END = "end";
    }
}
