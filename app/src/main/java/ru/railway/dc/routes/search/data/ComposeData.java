package ru.railway.dc.routes.search.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.apache.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.railway.dc.routes.R;
import ru.railway.dc.routes.request.data.RequestData;
import ru.railway.dc.routes.request.data.RequestDataSingleton;
import ru.railway.dc.routes.request.model.Station;

public class ComposeData {

    private static final Logger logger = Logger.getLogger(ComposeData.class);

    public static final String B_STATION = "bStation";
    public static final String E_STATION = "eStation";
    public static final String B_DATETIME = "bDateTime";
    public static final String E_DATETIME = "eDateTime";
    public static final String MAP_STATION_DURATION = "mapStationDuration";
    public static final String LIST_STATION = "listStation";
    public static final String LIST_PARSE_STATION = "listParseStation";

    public static final String MAP_ITEM_STATION = "s";
    public static final String MAP_ITEM_DURATION = "d";

    public static boolean loadToThreadData(Context context) {
        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            boolean isCurrentBTime = sp.getBoolean(context.getString(R.string.pref_name_actualtime),
                    Boolean.valueOf(context.getString(R.string.pref_value_actualtime)));
            String bTime;
            if (isCurrentBTime) {
                Calendar calendar = Calendar.getInstance();
                bTime = new SimpleDateFormat(RequestData.FORMAT_TIME).format(calendar.getTime());
            } else {
                bTime = sp.getString(context.getString(R.string.pref_name_btime),
                        context.getString(R.string.pref_value_btime));
            }
            String eTime = sp.getString(context.getString(R.string.pref_name_etime),
                    context.getString(R.string.pref_value_etime));

            logger.debug("bTime = " + bTime + ", eTime = " + eTime);

            RequestDataSingleton rds = RequestDataSingleton.getInstance();

            // Запись даты и времени
            SimpleDateFormat format = new SimpleDateFormat(RequestData.FORMAT_DATE_TIME);
            String sBDateTime = rds.findDataByName(RequestDataSingleton.Param.B_DATE) + " "
                    + bTime;
            Calendar bDateTime = new GregorianCalendar();
            bDateTime.setTime(format.parse(sBDateTime));


            String sEDateTime = rds.findDataByName(RequestDataSingleton.Param.E_DATE) + " "
                    + eTime;
            Calendar eDateTime = new GregorianCalendar();
            eDateTime.setTime(format.parse(sEDateTime));


            if (bDateTime.after(eDateTime)) {
                return false;
            }

            Data data = Data.getInstance();
            data.setParam(B_DATETIME, bDateTime);
            data.setParam(E_DATETIME, eDateTime);

            Station bStation = (Station) rds.findDataByName(RequestDataSingleton.Param.B_STATION);
            data.setParam(B_STATION, bStation.getName());
            Station eStation = (Station) rds.findDataByName(RequestDataSingleton.Param.E_STATION);
            data.setParam(E_STATION, eStation.getName());
            // Получения сочетания станции и времени на ней
            Map<Station, Integer> map = (Map<Station, Integer>) rds.findDataByName(RequestDataSingleton
                    .Param.I_STATION_DURATION);
            Map<String, Integer> tMap = new HashMap<>();
            for (Station station : map.keySet()) {
                tMap.put(station.getName(), map.get(station));
            }
            data.setParam(MAP_STATION_DURATION, tMap);
            // Получение списка станций
            List<String> stations = new ArrayList<>();
            List<String> parseStations = new ArrayList<>();
            for (Station station : map.keySet()) {
                String name = station.getName();
                stations.add(name);
                if (!parseStations.contains(name)) {
                    parseStations.add(name);
                }
            }
            data.setParam(LIST_STATION, stations);
            data.setParam(LIST_PARSE_STATION, parseStations);
        } catch (ParseException ex) {
            logger.error("Ошибка парсинга");
            return false;
        }
        return true;
    }

}
