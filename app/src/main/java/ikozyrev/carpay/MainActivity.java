package ikozyrev.carpay;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    final String FIRST_RUN_FLAG = "car_pay_first_run_flag";
    //final int MAX_VALUE_NUMBER_PICKER = 20;
    //final int MIN_VALUE_NUMBER_PICKER = 0;

    SharedPreferences sPref;
    DBHelper dbHelper;
    SQLiteDatabase database;
    Toolbar toolbar;
    NumberPicker freeDayNumberPicker;
    TextView deptTextView;
    TextView freeDayTextView;
    Calendar lastPayDate;
    CalcDept calc;
    Calendar tempLastPayDate;
    int lastPayId;
    Button payButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewsById();

        setSupportActionBar(toolbar);

        sPref = getSharedPreferences("carPayPref", MODE_PRIVATE);
        boolean firstRunFlag = sPref.getBoolean(FIRST_RUN_FLAG, false);
        Log.e("MAIN ACT FLAG: ", "" + sPref.getBoolean(FIRST_RUN_FLAG, false));
        if (!firstRunFlag) {
            Intent intent = new Intent(this, FirstStartAppActivity.class);
            startActivity(intent);
        }

        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();
        lastPayDate = getLastPayDate();
        tempLastPayDate = getLastPayDate();
        int maxNumberPickerValue = getMaxFreeDay();

        payButton.setOnClickListener(this);

        freeDayNumberPicker.setMaxValue(maxNumberPickerValue);
        freeDayNumberPicker.setValue(0);
        calc();
        checkPayStatusAndHideElements();
        freeDayNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                calc();
            }
        });


    }

    private void checkPayStatusAndHideElements() {
        if(getLastPayDate().after(Calendar.getInstance())){
            freeDayNumberPicker.setVisibility(View.GONE);
            payButton.setVisibility(View.GONE);
            freeDayTextView.setVisibility(View.GONE);

        }
    }

    private int getMaxFreeDay() {
        int maxFreeDay = 0;

        Calendar curDate = Calendar.getInstance();
        while (tempLastPayDate.before(curDate)) {
            if (tempLastPayDate.get(Calendar.DAY_OF_WEEK) < 6) {
                maxFreeDay++;
            }
            tempLastPayDate.add(Calendar.DATE, 1);
        }
        return maxFreeDay;
    }

    private void calc() {
        int dept;
        int rate;
        calc = new CalcDept();
        sPref = getSharedPreferences("carPayPref", MODE_PRIVATE);
        rate = sPref.getInt("rate_car_pay", 20);
        dept = calc.calculate(lastPayDate.getTimeInMillis(), freeDayNumberPicker.getValue(), rate);
        deptTextView.setText(String.valueOf(dept));
    }

    private void findViewsById() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        freeDayNumberPicker = (NumberPicker) findViewById(R.id.freeDayNumberPicker);
        deptTextView = (TextView) findViewById(R.id.deptTextView);
        payButton = (Button) findViewById(R.id.buttonPay);
        freeDayTextView = (TextView) findViewById(R.id.freeDayTextView);

    }


    private Calendar getLastPayDate() {

        Calendar lpd = Calendar.getInstance();
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = database.query(DBHelper.TABLE_DATES, null, null, null, null, null, DBHelper.KEY_ID, null);
        Calendar date = Calendar.getInstance();
        if (cursor.moveToLast()) {
            int idIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
            int dateIndex = cursor.getColumnIndex(DBHelper.KEY_DATE);
            int statusIndex = cursor.getColumnIndex(DBHelper.KEY_STATUS);
            date.setTimeInMillis(cursor.getLong(dateIndex));
            do {
                date.setTimeInMillis(cursor.getLong(dateIndex));
                if (cursor.getInt(statusIndex) == 0) {
                    lpd = date;
                    lastPayId = cursor.getInt(idIndex);
                } else {
                    date.add(Calendar.DATE, 1);
                    lpd = date;
                }
                //Log.e("DATE", "милис: " + cursor.getLong(dateIndex) + "дата: " + date.getTime() + "id: " + cursor.getInt(idIndex));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lpd;

    }


    @Override
    protected void onResume() {

        super.onResume();
        sPref = getSharedPreferences("carPayPref", MODE_PRIVATE);
        boolean firstRunFlag = sPref.getBoolean(FIRST_RUN_FLAG, false);
        // Log.e("MAIN ACT OR FLAG: ", "" + firstRunFlag);
        if (!firstRunFlag) {
            //Log.e("MAIN ACT OR FLAG: ", "" + firstRunFlag);
            finish();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_help) {
//            Intent intent = new Intent(this, HelpActivity.class);
//            startActivity(intent);


//
        } else if (id == R.id.action_settings) {
            // database.execSQL("DELETE FROM " + DBHelper.TABLE_DATES);

        } else if (id == R.id.action_reset_all_var) {
            sPref = getSharedPreferences("carPayPref", MODE_PRIVATE);
            SharedPreferences.Editor ed = sPref.edit();
            ed.putString("rate_car_pay", null);
            ed.putBoolean(FIRST_RUN_FLAG, false);
            ed.apply();

        } else if (id == R.id.action_delete_from_table) {
            database.execSQL("DELETE FROM " + DBHelper.TABLE_DATES);

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if (view == payButton) {
            ContentValues contentValues = new ContentValues();
//            if (!getLastPayDate().after(Calendar.getInstance())) {
//
            if(!getLastPayDate().after(Calendar.getInstance())) {
                contentValues.put(DBHelper.KEY_DATE, lastPayDate.getTimeInMillis());
                contentValues.put(DBHelper.KEY_STATUS, 1);
                //Log.e("CONTENT VALUES: ", contentValues.get(DBHelper.KEY_DATE).toString());
                database.update(DBHelper.TABLE_DATES, contentValues, DBHelper.KEY_ID + " = " + lastPayId, null);
//            } else if(){


                lastPayDate = Calendar.getInstance();
                lastPayDate.add(Calendar.DATE, 1);
                contentValues.clear();
                contentValues.put(DBHelper.KEY_DATE, lastPayDate.getTimeInMillis());
                contentValues.put(DBHelper.KEY_STATUS, 0);
                database.insert(DBHelper.TABLE_DATES, null, contentValues);

                lastPayDate = getLastPayDate();
                freeDayNumberPicker.setValue(0);
                freeDayNumberPicker.setMaxValue(getMaxFreeDay());
                calc();
                checkPayStatusAndHideElements();
            }


        }
    }
}
