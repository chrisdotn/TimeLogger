package de.niemannsland.timelogger.model;

import org.joda.time.Interval;

/**
 * Created by ChristophN on 25.03.2014.
 */
public class Booking {

    private Interval interval;
    private String   project;

    public Booking(Interval interval, String project) {
        this.interval = interval;
        this.project = project;
    }

    public Interval getInterval() {
        return interval;
    }

    public String getProject() {
        return project;
    }
}
