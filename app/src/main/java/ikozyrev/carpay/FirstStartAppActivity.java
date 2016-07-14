package ikozyrev.carpay;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.DialogFragment;
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

    DBHelper dbHelper;
    SharedPreferences sPref;
    final String RATE = "rate_car_pay";
    final String FIRST_RUN_FLAG = "car_pay_first_run_flag";
    TextView datePickerTextView;
    EditText ratePickerEditText;
    Button saveButton;
    private DatePickerDialog datePickerDialog;
    private SimpleDateFormat dateFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_start_app);

        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        findViewsById();
        setOnClickListeners();
        setDateTimeField();

//        datePickerTextView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                datePickerDialog.show();
//
//                //showDatePicker();
////                SQLiteDatabase database = dbHelper.getWritableDatabase();
////                Cursor cursor = database.query(DBHelper.TABLE_DATES,null,null,null,null,null,DBHelper.KEY_ID,null);
////                if (cursor.moveToLast()) {
////                    int dateIndex = cursor.getColumnIndex(DBHelper.KEY_DATE);
////                    Calendar date = Calendar.getInstance();
////                    date.setTimeInMillis(cursor.getLong(dateIndex));
////                    do {
////                        datePickerTextView.setText(date.getTime().toString());
//////                        Log.e("DATE" ,  "милис: " + cursor.getLong(dateIndex) + "дата: "+ date.getTime() + "id: " + cursor.getInt(idIndex));
////                    } while (cursor.moveToNext());
////                } else
////                    datePickerTextView.setText("Введена некорректная дата");
//            }
//        });

//        saveButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (ratePickerEditText.getText().length() != 0 && datePickerTextView.getText().length() != 0) {
//                    sPref = getSharedPreferences("MyPref", MODE_PRIVATE);
//                    SharedPreferences.Editor ed = sPref.edit();
//                    ed.putString(RATE, ratePickerEditText.getText().toString());
//                    ed.putBoolean(FIRST_RUN_FLAG, true);
//                    ed.apply();
//                    finish();
//                } else {
//                    Toast.makeText(getApplicationContext(), "Не все поля заполнены", Toast.LENGTH_LONG).show();
//
//                }
//            }
//        });
        dbHelper = new DBHelper(this);
    }

    private void setOnClickListeners() {
        saveButton.setOnClickListener(this);
        datePickerTextView.setOnClickListener(this);
    }

    private void findViewsById() {
        datePickerTextView = (TextView) findViewById(R.id.dataPickerTextView);
        ratePickerEditText = (EditText) findViewById(R.id.ratePIckerEditText);
        saveButton = (Button) findViewById(R.id.firstStartAppSaveButton);
        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
    }

    private void setDateTimeField() {

        Calendar newCalendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                SQLiteDatabase database = dbHelper.getWritableDatabase();
                ContentValues contentValues = new ContentValues();
                contentValues.put(DBHelper.KEY_DATE, newDate.getTimeInMillis());
                contentValues.put(DBHelper.KEY_STATUS, 0);
                Log.e("CONTENT VALUES: ", contentValues.get(DBHelper.KEY_DATE).toString());
                database.insert(DBHelper.TABLE_DATES, null, contentValues);
                datePickerTextView.setText(dateFormatter.format(newDate.getTime()));

            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

    }


    @Override
    public void onClick(View view) {
        if (view == datePickerTextView)
            datePickerDialog.show();
        else if (view == saveButton) {
            if (ratePickerEditText.getText().length() != 0 && datePickerTextView.getText().length() != 0) {
                sPref = getSharedPreferences("carPayPref", MODE_PRIVATE);
                SharedPreferences.Editor ed = sPref.edit();
                ed.putInt(RATE,Integer.parseInt( ratePickerEditText.getText().toString()));
                ed.putBoolean(FIRST_RUN_FLAG, true);

                ed.commit();
                Log.e("PREV SAVE: ", "" + sPref.getBoolean(FIRST_RUN_FLAG, false));
                startActivity(new Intent(this, MainActivity.class));
                this.finish();
            } else {
                Toast.makeText(getApplicationContext(), "Не все поля заполнены", Toast.LENGTH_LONG).show();

            }
        }
    }
}
