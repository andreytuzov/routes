package ru.railway.dc.routes.request.fragment.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import ru.railway.dc.routes.MainActivity;
import ru.railway.dc.routes.R;
import ru.railway.dc.routes.request.data.RequestDataSingleton;


public class DateFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    public static final String PARAM = "param";
    public static final String IS_EDATETIME = "isEDateTime";

    public static DateFragment newInstance(String param) {
        DateFragment dateFragment = new DateFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PARAM, param);
        dateFragment.setArguments(bundle);
        return dateFragment;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DatePickerDialog datePickerDialog = null;
        try {
            // Создаем calendar
            Calendar calendar = new GregorianCalendar();
            String date = (String) RequestDataSingleton.getInstance()
                    .findDataByName(getParam());
            calendar.setTime((MainActivity.FORMAT_DATE.parse(date)));

            // Выделяем составляющие calendar
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return datePickerDialog;
    }

    private String getParam() {
        return getArguments().getString(PARAM);
    }

    private boolean isEDateTime() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sp.getBoolean(getString(R.string.pref_name_edatetime),
                Boolean.valueOf(getString(R.string.pref_value_edatetime)));
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, monthOfYear);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String date = MainActivity.FORMAT_DATE.format(c.getTime());

        if (isEDateTime()) {
            RequestDataSingleton.getInstance().notification(getParam(), date);
        } else {
            RequestDataSingleton.getInstance()
                    .notification(RequestDataSingleton.Param.B_DATE, date);
            RequestDataSingleton.getInstance()
                    .notification(RequestDataSingleton.Param.E_DATE, date);
        }
    }

}
