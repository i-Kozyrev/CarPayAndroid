package ikozyrev.carpay;

import android.content.SharedPreferences;

import java.util.Calendar;

/**
 * Created in Android Studio
 * User: ikozyrev
 * Date: 11.07.2016.
 */


public class CalcDept {



   public boolean weekendInd(Calendar date){
        return date.get(Calendar.DAY_OF_WEEK) < 6;
    }


    public int calculate(Long datePaymant, int freeDays, int rate) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(datePaymant);
        int sum = 0;
        while (date.before(Calendar.getInstance())) {
            if (weekendInd(date)){
                sum+=rate;
            }
            date.add(Calendar.DATE, 1);
        }
        //date.get(Calendar.DA)
        sum-=freeDays*rate;
        return sum;
    }

}
