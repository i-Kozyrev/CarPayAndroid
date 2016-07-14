package ikozyrev.carpay;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class PayHistoryActivity extends AppCompatActivity {

    ListView mPayHistoryListView;
    DBHelper mDBHelper;
    SQLiteDatabase mDatabase;
    Cursor mCursor;
    ListAdapter mAdapter;

    public class History extends HashMap<String, String> {

        public static final String DATE = "date";
        public static final String COST = "cost";

        // Конструктор с параметрами
        public History(String date, String cost) {
            super();
            super.put(DATE, date);
            super.put(COST, cost);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_history);
        SimpleDateFormat mDateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        mDBHelper = new DBHelper(this);
        mDatabase = mDBHelper.getReadableDatabase();
        mPayHistoryListView = (ListView) findViewById(R.id.payHistoryListView);
        //getPayHistoryList();
        Calendar date = Calendar.getInstance();
        mCursor = mDatabase.rawQuery("Select * " + "from " + DBHelper.TABLE_DATES + " WHERE " + DBHelper.KEY_STATUS + " = 1" + " ORDER BY " + DBHelper.KEY_DATE, null);
        ArrayList<History> historyArrayList = new ArrayList<>();
        if (mCursor.moveToFirst()) {
            int dateIndex = mCursor.getColumnIndex(DBHelper.KEY_DATE);
            int costIndex = mCursor.getColumnIndex(DBHelper.KEY_COST);
            date.setTimeInMillis(mCursor.getLong(dateIndex));
            do {
                date.setTimeInMillis(mCursor.getLong(dateIndex));
                historyArrayList.add(new History(mDateFormatter.format(date.getTime()), String.valueOf(mCursor.getInt(costIndex))));

                //Log.e("DATE", "милис: " + cursor.getLong(dateIndex) + "дата: " + date.getTime() + "id: " + cursor.getInt(idIndex));
            } while (mCursor.moveToNext());
        }
        mCursor.close();

        mAdapter = new SimpleAdapter(this,historyArrayList,android.R.layout.two_line_list_item,
                new String[]{History.DATE, History.COST}, new int[]{android.R.id.text1, android.R.id.text2});

        mPayHistoryListView.setAdapter(mAdapter);

    }



}
