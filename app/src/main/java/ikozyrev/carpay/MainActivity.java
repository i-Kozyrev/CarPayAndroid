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


    public static final String FIRST_RUN_FLAG = "car_pay_first_run_flag";
    //final int MAX_VALUE_NUMBER_PICKER = 20;
    //final int MIN_VALUE_NUMBER_PICKER = 0;

    SharedPreferences mSPref;
    DBHelper mDBHelper;
    SQLiteDatabase mDatabase;

    Button mPayButton;
    Toolbar mToolbar;
    NumberPicker mFreeDayNumberPicker;
    TextView mDeptTextView;
    TextView mFreeDayTextView;

    Calendar mLastPayDate;
    CalcDept mCalc;
    Calendar mTempLastPayDate;
    int mLastPayId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewsById();

        setSupportActionBar(mToolbar);

        mSPref = getSharedPreferences("carPayPref", MODE_PRIVATE);
        boolean firstRunFlag = mSPref.getBoolean(FIRST_RUN_FLAG, false);
        Log.e("MAIN ACT FLAG: ", "" + mSPref.getBoolean(FIRST_RUN_FLAG, false));
        if (!firstRunFlag) {
            Intent intent = new Intent(this, FirstStartAppActivity.class);
            startActivity(intent);
        }

        mDBHelper = new DBHelper(this);
        mDatabase = mDBHelper.getWritableDatabase();
        mLastPayDate = getLastPayDate();
        mTempLastPayDate = getLastPayDate();
        int maxNumberPickerValue = getMaxFreeDay();

        mPayButton.setOnClickListener(this);

        mFreeDayNumberPicker.setMaxValue(maxNumberPickerValue);
        mFreeDayNumberPicker.setValue(0);
        calc();
        checkPayStatusAndHideElements();
        mFreeDayNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                calc();
            }
        });


    }

    private void checkPayStatusAndHideElements() {
        if (getLastPayDate().after(Calendar.getInstance())) {
            mFreeDayNumberPicker.setVisibility(View.GONE);
            mPayButton.setVisibility(View.GONE);
            mFreeDayTextView.setVisibility(View.GONE);

        }
    }

    private int getMaxFreeDay() {
        int maxFreeDay = 0;

        Calendar curDate = Calendar.getInstance();
        while (mTempLastPayDate.before(curDate)) {
            if (mTempLastPayDate.get(Calendar.DAY_OF_WEEK) < 6) {
                maxFreeDay++;
            }
            mTempLastPayDate.add(Calendar.DATE, 1);
        }
        return maxFreeDay;
    }

    private void calc() {
        int dept;
        int rate;
        mCalc = new CalcDept();
        mSPref = getSharedPreferences("carPayPref", MODE_PRIVATE);
        rate = mSPref.getInt("rate_car_pay", 20);
        dept = mCalc.calculate(mLastPayDate.getTimeInMillis(), mFreeDayNumberPicker.getValue(), rate);
        mDeptTextView.setText(String.valueOf(dept));
    }

    private void findViewsById() {

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mFreeDayNumberPicker = (NumberPicker) findViewById(R.id.freeDayNumberPicker);
        mDeptTextView = (TextView) findViewById(R.id.deptTextView);
        mPayButton = (Button) findViewById(R.id.buttonPay);
        mFreeDayTextView = (TextView) findViewById(R.id.freeDayTextView);

    }


    private Calendar getLastPayDate() {

        Calendar lpd = Calendar.getInstance();
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
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
                    mLastPayId = cursor.getInt(idIndex);
                } else {
                    date.add(Calendar.DATE, 1);
                    lpd = date;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lpd;

    }


    @Override
    protected void onResume() {

        super.onResume();
        mSPref = getSharedPreferences("carPayPref", MODE_PRIVATE);
        boolean firstRunFlag = mSPref.getBoolean(FIRST_RUN_FLAG, false);
        if (!firstRunFlag) {
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
        if (id == R.id.action_history) {
            Intent intent = new Intent(this, PayHistoryActivity.class);
            startActivity(intent);


//
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(this, FirstStartAppActivity.class);
            intent.putExtra(FirstStartAppActivity.KEY_SETTINGS_ACTION_START, true);
            Log.e("KEY_SETTINGS in main", intent.getExtras().toString());
            startActivity(intent);
        }

//        } else if (id == R.id.action_reset_all_var) {
//            mSPref = getSharedPreferences("carPayPref", MODE_PRIVATE);
//            SharedPreferences.Editor ed = mSPref.edit();
//            ed.putString("rate_car_pay", null);
//            ed.putBoolean(FIRST_RUN_FLAG, false);
//            ed.apply();
//
//        } else if (id == R.id.action_delete_from_table) {
//            mDatabase.execSQL("DELETE FROM " + DBHelper.TABLE_DATES);
//
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if (view == mPayButton) {
            ContentValues contentValues = new ContentValues();
//            if (!getLastPayDate().after(Calendar.getInstance())) {
//
            if (!getLastPayDate().after(Calendar.getInstance())) {
                contentValues.put(DBHelper.KEY_DATE, mLastPayDate.getTimeInMillis());
                contentValues.put(DBHelper.KEY_STATUS, 1);
                contentValues.put(DBHelper.KEY_COST, Integer.parseInt(mDeptTextView.getText().toString()));
                //Log.e("CONTENT VALUES: ", contentValues.get(DBHelper.KEY_DATE).toString());
                mDatabase.update(DBHelper.TABLE_DATES, contentValues, DBHelper.KEY_ID + " = " + mLastPayId, null);
//            } else if(){


                mLastPayDate = Calendar.getInstance();
                mLastPayDate.add(Calendar.DATE, 1);
                contentValues.clear();
                contentValues.put(DBHelper.KEY_DATE, mLastPayDate.getTimeInMillis());
                contentValues.put(DBHelper.KEY_STATUS, 0);
                mDatabase.insert(DBHelper.TABLE_DATES, null, contentValues);

                mLastPayDate = getLastPayDate();
                mFreeDayNumberPicker.setValue(0);
                mFreeDayNumberPicker.setMaxValue(getMaxFreeDay());
                calc();
                checkPayStatusAndHideElements();
            }


        }
    }
}
