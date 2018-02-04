package ru.railway.dc.routes.setting;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import ru.railway.dc.routes.R;
import ru.railway.dc.routes.database.utils.CashDetailTableUtils;
import ru.railway.dc.routes.database.utils.CashTableUtils;
import ru.railway.dc.routes.database.utils.EventTableUtils;
import ru.railway.dc.routes.request.data.RequestData;
import ru.railway.dc.routes.tools.AppUtils;

/**
 * Created by SQL on 29.01.2017.
 */

public class MyPreferenceFragment extends PreferenceFragment
        implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    private static final int TASK_CLEAR_CASH = 0;
    private static final int TASK_CLEAR_EVENT = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.activity_preference);

        findPreference(getString(R.string.pref_name_clearcash)).setOnPreferenceClickListener(this);
        findPreference(getString(R.string.pref_name_clearevent)).setOnPreferenceClickListener(this);
        findPreference(getString(R.string.pref_name_period)).setOnPreferenceChangeListener(this);
        findPreference(getString(R.string.pref_name_stationtime)).setOnPreferenceChangeListener(this);
        findPreference(getString(R.string.pref_name_btime)).setOnPreferenceChangeListener(this);
        findPreference(getString(R.string.pref_name_etime)).setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String title = preference.getKey().toString();
        int key = -1;
        if (title.equals(getString(R.string.pref_name_clearcash))) {
            key = TASK_CLEAR_CASH;
        } else if (title.equals(getString(R.string.pref_name_clearevent))) {
            key = TASK_CLEAR_EVENT;
        }
        new MyTask(getActivity()).execute(new Integer[]{key});
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String title = preference.getKey().toString();
        String value = (String) newValue;
        if (title.equals(getString(R.string.pref_name_period)) && value.matches("^\\d+$")) {
            // Проверка корректности введенных данных
            int period = Integer.valueOf(value);
            if (period < 1) {
                return false;
            }
            updatePeriodTime(period);
        } else if (title.equals(getString(R.string.pref_name_stationtime)) && value.matches("^\\d+$")) {
            updateStationTime(Integer.valueOf(value));
        } else if ((title.equals(getString(R.string.pref_name_btime)) ||
                title.equals(getString(R.string.pref_name_etime)))
                && value.matches("^(([0,1][0-9])|(2[0-3])):[0-5][0-9]$")) {
        } else {
            return false;
        }
        return true;
    }

    private void updateStationTime(int stationTime) {
        RequestData.setDefaultDuration(stationTime);
    }

    private void updatePeriodTime(int period) {
        AppUtils.stopEventService();
        AppUtils.startEventService();
    }



    static class MyTask extends AsyncTask<Integer, Void, Integer> {

        private Context context;

        public MyTask(Context context) {
            this.context = context;
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            int key = params[0];
            switch (key) {
                case TASK_CLEAR_CASH:
                    CashTableUtils.clearData();
                    CashDetailTableUtils.clearData();
                    break;
                case TASK_CLEAR_EVENT:
                    EventTableUtils.removeAll();
                    break;
            }
            return key;
        }

        @Override
        protected void onPostExecute(Integer key) {
            switch (key) {
                case TASK_CLEAR_CASH:
                    Toast.makeText(context, "Очистка кэша завершена", Toast.LENGTH_SHORT).show();
                    break;
                case TASK_CLEAR_EVENT:
                    Toast.makeText(context, "Очистка событий завершена", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }


}
