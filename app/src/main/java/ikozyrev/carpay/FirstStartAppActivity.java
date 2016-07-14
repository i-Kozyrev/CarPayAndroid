package ikozyrev.carpay;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class FirstStartAppActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String RATE = "rate_car_pay";
    public static final String FIRST_RUN_FLAG = "car_pay_first_run_flag";

    DBHelper mDBHelper;
    SharedPreferences mSPref;
    TextView mDatePickerTextView;
    EditText mRatePickerEditText;
    Button mSaveButton;
    private DatePickerDialog mDatePickerDialog;
    private SimpleDateFormat mDateFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_start_app);

        mDateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        findViewsById();
        setOnClickListeners();
        setDateTimeField();


        mDBHelper = new DBHelper(this);
    }

    private void setOnClickListeners() {
        mSaveButton.setOnClickListener(this);
        mDatePickerTextView.setOnClickListener(this);
    }

    private void findViewsById() {
        mDatePickerTextView = (TextView) findViewById(R.id.dataPickerTextView);
        mRatePickerEditText = (EditText) findViewById(R.id.ratePIckerEditText);
        mSaveButton = (Button) findViewById(R.id.firstStartAppSaveButton);
        mDateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
    }

    private void setDateTimeField() {

        Calendar newCalendar = Calendar.getInstance();
        mDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                SQLiteDatabase database = mDBHelper.getWritableDatabase();
                ContentValues contentValues = new ContentValues();
                contentValues.put(DBHelper.KEY_DATE, newDate.getTimeInMillis());
                contentValues.put(DBHelper.KEY_STATUS, 0);
                Log.e("CONTENT VALUES: ", contentValues.get(DBHelper.KEY_DATE).toString());
                database.insert(DBHelper.TABLE_DATES, null, contentValues);
                mDatePickerTextView.setText(mDateFormatter.format(newDate.getTime()));

            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

    }


    @Override
    public void onClick(View view) {
        if (view == mDatePickerTextView)
            mDatePickerDialog.show();
        else if (view == mSaveButton) {
            if (mRatePickerEditText.getText().length() != 0 && mDatePickerTextView.getText().length() != 0) {
                mSPref = getSharedPreferences("carPayPref", MODE_PRIVATE);
                SharedPreferences.Editor ed = mSPref.edit();
                ed.putInt(RATE, Integer.parseInt(mRatePickerEditText.getText().toString()));
                ed.putBoolean(FIRST_RUN_FLAG, true);

                ed.commit();
                Log.e("PREV SAVE: ", "" + mSPref.getBoolean(FIRST_RUN_FLAG, false));
                startActivity(new Intent(this, MainActivity.class));
                this.finish();
            } else {
                Toast.makeText(getApplicationContext(), "Не все поля заполнены", Toast.LENGTH_LONG).show();

            }
        }
    }
}
