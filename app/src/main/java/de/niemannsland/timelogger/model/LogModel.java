package de.niemannsland.timelogger.model;

import android.content.Context;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.PeriodType;
import org.joda.time.ReadableInstant;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.List;

import de.niemannsland.timelogger.storage.DBHelper;

/**
 * Created by ChristophN on 25.03.2014.
 */
public class LogModel {

    private static final String TAG = "LogModel";

    private List<Booking> loggedIntervals;
    private DateTime start;
    private String project;

    private DBHelper dbHelper;

    public LogModel(Context context, String startProject) {
        dbHelper = new DBHelper(context);
        loggedIntervals = new ArrayList<>();
        if (dbHelper.getAllIntervals() != null) {
            loggedIntervals = dbHelper.getAllIntervals();
        }
        start = null;
        project = startProject;
    }

    public String getFirstUnbookableProject() {
        return dbHelper.getDefaultProjects().get(0);
    }

    public boolean switchProject(String newProject) {
        boolean logged = false;

        if (start != null && dbHelper.isBookable(project)) {
            Booking booking = new Booking(new Interval(start, new DateTime()), project);
            loggedIntervals.add(loggedIntervals.size(), booking);
            dbHelper.addEvent(booking);
            Log.v(TAG, "timer stopped at "
                    + loggedIntervals.get(loggedIntervals.size() - 1).getInterval().getEnd().toString(ISODateTimeFormat.dateHourMinuteSecond())
                    + ", logging "
                    + loggedIntervals.get(loggedIntervals.size() - 1).getInterval().toPeriod().normalizedStandard(PeriodType.time().withMillisRemoved()));
            logged = true;
        }

        start = new DateTime();
        this.project = newProject;
        Log.v(TAG, "timer started for " + project + " at " + start.toString(ISODateTimeFormat.dateHourMinuteSecond()));

        return logged;
    }

    public List<Booking> getLoggedIntervals() {
        return loggedIntervals;
    }

    public List<String> getActiveProjects() {
        return dbHelper.getActiveProjects();
    }

    public Booking getLast() {
        if (loggedIntervals.size() > 0) {
            return loggedIntervals.get(loggedIntervals.size() - 1);
        } else {
            return null;
        }
    }

    public Booking getCurrent() {
        if (start != null) {
            return new Booking(new Interval(start, new DateTime()), project);
        }

        return null;
    }
}
