package de.niemannsland.timelogger.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.List;

import de.niemannsland.timelogger.model.Booking;

/**
 * Created by ChristophN on 30.03.2014.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String TAG = "DBHelper";

    public DBHelper(Context context) {
        super(context, TimeloggerContract.DATABASE_NAME, null, TimeloggerContract.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v(TAG, "SQL: " + TimeloggerContract.CREATE_PROJECT);
        db.execSQL(TimeloggerContract.CREATE_PROJECT);

        Log.v(TAG, "SQL: " + TimeloggerContract.CREATE_EVENT);
        db.execSQL(TimeloggerContract.CREATE_EVENT);

        insertInitalData(db);
    }

    private void insertInitalData(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        long rowId = 0;

        try {
            // project break
            values.put(TimeloggerContract.Project.COLUMN_NAME_NAME, "break");
            values.put(TimeloggerContract.Project.COLUMN_NAME_ACTIVE, true);
            values.put(TimeloggerContract.Project.COLUMN_NAME_BOOKABLE, false);
            values.put(TimeloggerContract.Project.COLUMN_NAME_PAID, false);
            values.put(TimeloggerContract.Project.COLUMN_NAME_PRIVATE, false);

            // insert into db
            rowId = db.insert(TimeloggerContract.Project.TABLE_NAME, null, values);
            Log.v(TAG, "inserted " + values.get(TimeloggerContract.Project.COLUMN_NAME_NAME) + " into db with rowID " + rowId);
            values.clear();

            // project local.ch
            values.put(TimeloggerContract.Project.COLUMN_NAME_NAME, "local.ch");
            values.put(TimeloggerContract.Project.COLUMN_NAME_ACTIVE, true);
            values.put(TimeloggerContract.Project.COLUMN_NAME_BOOKABLE, true);
            values.put(TimeloggerContract.Project.COLUMN_NAME_PAID, true);
            values.put(TimeloggerContract.Project.COLUMN_NAME_PRIVATE, false);

            // insert into db
            rowId = db.insert(TimeloggerContract.Project.TABLE_NAME, null, values);
            Log.v(TAG, "inserted " + values.get(TimeloggerContract.Project.COLUMN_NAME_NAME) + " into db with rowID " + rowId);
            values.clear();

            // project intern
            values.put(TimeloggerContract.Project.COLUMN_NAME_NAME, "intern");
            values.put(TimeloggerContract.Project.COLUMN_NAME_ACTIVE, true);
            values.put(TimeloggerContract.Project.COLUMN_NAME_BOOKABLE, true);
            values.put(TimeloggerContract.Project.COLUMN_NAME_PAID, false);
            values.put(TimeloggerContract.Project.COLUMN_NAME_PRIVATE, false);

            // insert into db
            rowId = db.insert(TimeloggerContract.Project.TABLE_NAME, null, values);
            Log.v(TAG, "inserted " + values.get(TimeloggerContract.Project.COLUMN_NAME_NAME) + " into db with rowID " + rowId);
            values.clear();

            // project Timelogger
            values.put(TimeloggerContract.Project.COLUMN_NAME_NAME, "Timelogger");
            values.put(TimeloggerContract.Project.COLUMN_NAME_ACTIVE, true);
            values.put(TimeloggerContract.Project.COLUMN_NAME_BOOKABLE, true);
            values.put(TimeloggerContract.Project.COLUMN_NAME_PAID, false);
            values.put(TimeloggerContract.Project.COLUMN_NAME_PRIVATE, true);

            // insert into db
            rowId = db.insert(TimeloggerContract.Project.TABLE_NAME, null, values);
            Log.v(TAG, "inserted " + values.get(TimeloggerContract.Project.COLUMN_NAME_NAME) + " into db with rowID " + rowId);
            values.clear();
        } catch (SQLiteConstraintException sqle) {
            Log.w(TAG, "SQLiteContstraint error: " + sqle.getMessage());
        }
    }

    public long addEvent(Booking booking) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        long rowId = 0;

        values.put(TimeloggerContract.Event.COLUMN_NAME_PROJECT, booking.getProject());
        values.put(TimeloggerContract.Event.COLUMN_NAME_START, booking.getInterval().getStartMillis());
        values.put(TimeloggerContract.Event.COLUMN_NAME_END, booking.getInterval().getEndMillis());

        // insert into db
        rowId = db.insert(TimeloggerContract.Event.TABLE_NAME, null, values);
        Log.v(TAG, "inserted " + values.get(TimeloggerContract.Event.COLUMN_NAME_PROJECT) + " into db with rowID " + rowId);

        return rowId;
    }

    public List<String> getDefaultProjects() {
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = TimeloggerContract.Project.COLUMN_NAME_BOOKABLE + " = ?";
        String selectionArgs[] = {"0"};

        Cursor c = db.query(TimeloggerContract.Project.TABLE_NAME, null, selection, selectionArgs, null, null, null);

        List<String> result = new ArrayList<>(c.getCount());

        // debug
        c.moveToFirst();
        Log.v(TAG, "getDefaultProjects retrieved " + c.getCount() + " rows");
        while (!c.isAfterLast()) {
            result.add(result.size(), c.getString(c.getColumnIndex(TimeloggerContract.Project.COLUMN_NAME_NAME)));
            String project = "";
            for (int i=0; i<c.getColumnCount(); i++) {
                project += c.getColumnName(i) + "=" + c.getString(i) + " ";
            }
            Log.v(TAG, project);
            c.moveToNext();
        }
        c.close();
        return result;
    }

    public boolean isBookable(String project) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = TimeloggerContract.Project.COLUMN_NAME_BOOKABLE + " = ? AND " + TimeloggerContract.Project.COLUMN_NAME_NAME + " = ?";
        String selectionArgs[] = {"1", project};

        Cursor c = db.query(TimeloggerContract.Project.TABLE_NAME, null, selection, selectionArgs, null, null, null);

        // debug
        c.moveToFirst();
        Log.v(TAG, "isBookable retrieved " + c.getCount() + " rows for project = " + project);
        while (!c.isAfterLast()) {
            String result = "";
            for (int i=0; i<c.getColumnCount(); i++) {
                result += c.getColumnName(i) + "=" + c.getString(i) + " ";
            }
            Log.v(TAG, result);
            c.moveToNext();
        }

        boolean bookable = c.getCount() > 0;

        c.close();
        return bookable;
    }

    public List<Booking> getAllIntervals() {
        SQLiteDatabase db = this.getReadableDatabase();

        String projection[] = {TimeloggerContract.Event.COLUMN_NAME_PROJECT,
                TimeloggerContract.Event.COLUMN_NAME_START,
                TimeloggerContract.Event.COLUMN_NAME_END};
        String sortOrder = TimeloggerContract.Event.COLUMN_NAME_START + " DESC";

        Cursor c = db.query(TimeloggerContract.Event.TABLE_NAME, projection, null, null, null, null, sortOrder);

        List<Booking> bookings = new ArrayList<>(c.getCount());

        c.moveToFirst();
        while (!c.isAfterLast()) {
            bookings.add(bookings.size(), new Booking(new Interval(new DateTime(c.getLong(c.getColumnIndex(TimeloggerContract.Event.COLUMN_NAME_START))),
                    new DateTime(c.getLong(c.getColumnIndex(TimeloggerContract.Event.COLUMN_NAME_END)))),
                    c.getString(c.getColumnIndex(TimeloggerContract.Event.COLUMN_NAME_PROJECT))));
            c.moveToNext();
        }
        c.close();

        return bookings;
    }

    public List<String> getActiveProjects() {

        SQLiteDatabase db = this.getReadableDatabase();

        String projection[] = {TimeloggerContract.Project._ID, TimeloggerContract.Project.COLUMN_NAME_NAME};
        String selection = TimeloggerContract.Project.COLUMN_NAME_ACTIVE + " = ?";
        String selectionArgs[] = {"1"};
        String sortOrder = TimeloggerContract.Project._ID  + " ASC";

        Cursor c = db.query(TimeloggerContract.Project.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);

        List<String> projects = new ArrayList<>(c.getCount());

        c.moveToFirst();
        while (!c.isAfterLast()) {
            projects.add(c.getString(c.getColumnIndexOrThrow(TimeloggerContract.Project.COLUMN_NAME_NAME)));
            c.moveToNext();
        }
        c.close();

        return projects;
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //TODO come up with a proper upgrade policy
        Log.i(TAG, "upgrading DB from version " + oldVersion + " to version " + newVersion);

        Log.v(TAG, "onUpgrade SQL: " + TimeloggerContract.DROP_PROJECT);
        db.execSQL(TimeloggerContract.DROP_PROJECT);

        Log.v(TAG, "onUpgrade SQL: " + TimeloggerContract.DROP_EVENT);
        db.execSQL(TimeloggerContract.DROP_EVENT);

        // recreate DB
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "downgrading DB from version " + oldVersion + " to version " + newVersion);

        //TODO come up with a proper downgrade policy
        onUpgrade(db, oldVersion, newVersion);
    }
}
