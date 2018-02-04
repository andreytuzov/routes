package ru.railway.dc.routes.request.fragment.dialog;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.TimePicker;

import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import ru.railway.dc.routes.MainActivity;
import ru.railway.dc.routes.request.data.RequestDataSingleton;

/**
 * Created by SQL on 02.12.2016.
 */

public class TimeFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    // Идентификатор параметра bTime или eTime
    public static final String PARAM = "param";

    // Вместо конструктора
    public static TimeFragment newInstance(String param) {
        TimeFragment timeFragment = new TimeFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PARAM, param);
        timeFragment.setArguments(bundle);
        return timeFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        TimePickerDialog timePickerDialog = null;
        try {
            // Создаем calendar
            Calendar calendar = new GregorianCalendar();
            String date = (String) RequestDataSingleton.getInstance().findDataByName(getParam());
            calendar.setTime((MainActivity.FORMAT_TIME.parse(date)));
            // Выделяем составляющие calendar
            int hour = calendar.get(Calendar.HOUR);
            int minute = calendar.get(Calendar.MINUTE);
            timePickerDialog = new TimePickerDialog(getActivity(), this, hour, minute, false);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timePickerDialog;
    }

    private String getParam() {
        return getArguments().getString(PARAM);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        String date = MainActivity.FORMAT_TIME.format(c.getTime());
        RequestDataSingleton.getInstance().notification(getParam(), date);
    }
}
