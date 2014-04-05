package de.niemannsland.timelogger.app;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import net.danlew.android.joda.ResourceZoneInfoProvider;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import de.niemannsland.timelogger.model.Booking;
import de.niemannsland.timelogger.model.LogModel;
import de.niemannsland.timelogger.storage.DBHelper;
import de.niemannsland.timelogger.storage.TimeloggerContract;


public class MainActivity extends ActionBarActivity implements AdapterView.OnItemSelectedListener {

    private final static String TAG = "MainActivity";
    private final static int ROW_INDEX = 1;
    private final static String TIME_FORMAT = "HH:mm:ss";
    private final static String START_PROJECT = "break";

    private LogModel model;
    private SQLiteDatabase database;

    private DateTimeFormatter fmt;
    private PeriodFormatter pfmt;

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            TextView textView = (TextView)findViewById(R.id.textview_booking);

            if (model.getCurrent() != null) {
                textView.setText(model.getCurrent().getProject() + ": "
                        + model.getCurrent().getInterval().getStart().toString(fmt)
                        + "-" + model.getCurrent().getInterval().getEnd().toString(fmt)
                        + " (" + pfmt.print(model.getCurrent().getInterval().toPeriod()) + ")");
            }

            timerHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onResume.");

        // start the update routine to update the current project
        timerHandler.postDelayed(timerRunnable, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(TAG, "onPause.");

        // remove handler to update current project
        // idea for handler from http://stackoverflow.com/questions/4597690/android-timer-how
        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // close current event
        model.switchProject(model.getFirstUnbookableProject());
        Log.v(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "onStart");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        // init joda time
        ResourceZoneInfoProvider.init(this);

        // init data model
        model = new LogModel(this, START_PROJECT);

        //setup formatters
        fmt = DateTimeFormat.forPattern(TIME_FORMAT);
        pfmt = new PeriodFormatterBuilder()
                .appendHours()
                .appendSeparator(":")
                .printZeroAlways()
                .minimumPrintedDigits(2)
                .appendMinutes()
                .appendSeparator(":")
                .appendSeconds()
                .toFormatter();

        // load data for spinner
        Spinner spinner = (Spinner)findViewById(R.id.spinner_project);

        Log.v(TAG, "active projects: " + model.getActiveProjects());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, model.getActiveProjects());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        // prefill table
        for (Booking b: model.getLoggedIntervals()) {
            if (b != null) {
                addRow(b);
            }
        }

        Log.v(TAG, "initialized");
    }

    private void addRow(Booking booking) {
        TableLayout table = (TableLayout) findViewById(R.id.intervalTable);

        // inflate new row
        TableRow row = (TableRow) LayoutInflater.from(this).inflate(R.layout.interval_row, null);
        ((TextView) row.findViewById(R.id.attr_project)).setText(booking.getProject());
        ((TextView) row.findViewById(R.id.attr_start)).setText(booking.getInterval().getStart().toString(fmt));
        ((TextView) row.findViewById(R.id.attr_stop)).setText(booking.getInterval().getEnd().toString(fmt));
        ((TextView) row.findViewById(R.id.attr_duration)).setText(pfmt.print(booking.getInterval().toPeriod()));

        table.addView(row, ROW_INDEX);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        Log.v(TAG, "Selected an item on the spinner: " + parent.getItemAtPosition(pos));
        if(model.switchProject((String)parent.getItemAtPosition(pos))) {
            if (model.getLast() != null) {
                addRow(model.getLast());
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.v(TAG, "Clicked spinner, but did not select a thing.");
    }



}
