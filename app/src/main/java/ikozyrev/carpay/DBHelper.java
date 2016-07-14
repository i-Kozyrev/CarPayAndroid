package ikozyrev.carpay;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created in Android Studia
 * User: ikozyrev
 * Date: 11.07.2016.
 */
public class DBHelper extends SQLiteOpenHelper {


    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "carPay";
    public static final String TABLE_DATES = "dates";

    public static final String KEY_ID = "_id";
    public static final String KEY_DATE = "date";
    public static final String KEY_STATUS = "status";
    public static final String KEY_COST = "cost";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table " + TABLE_DATES + "(" + KEY_ID
                + " integer primary key," + KEY_DATE + " integer," + KEY_STATUS + " integer, " + KEY_COST + " integer" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table if exists " + TABLE_DATES);
        //lol
        onCreate(sqLiteDatabase);

    }
}
