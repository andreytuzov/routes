package ru.railway.dc.routes.request.data;

import java.util.HashMap;
import java.util.Map;

import ru.railway.dc.routes.request.model.Station;

/**
 * Created by SQL on 05.12.2016.
 */

public class RequestData {

    // Формат хранения данных
    public static final String FORMAT_DATE = "yyyy-MM-dd";
    public static final String FORMAT_TIME = "HH:mm";
    public static final String FORMAT_DATE_TIME = "yyyy-MM-dd HH:mm";

    private static int DEFAULT_DURATION;

    private Station bStation;
    private Station eStation;
    private String bDate;
    private String eDate;
    private Map<Station, Integer> mapSD;

    public RequestData() {
        mapSD = new HashMap<>();
    }

    public static void setDefaultDuration(int defaultDuration) {
        DEFAULT_DURATION = defaultDuration;
    }

    // =========================== GET & SET =======================================================

    public void setbStation(Object bStation) {
        this.bStation = (Station) bStation;
    }

    public Object getbStation() {
        return bStation;
    }

    public void seteStation(Object eStation) {
        this.eStation = (Station) eStation;
    }

    public Object geteStation() {
        return eStation;
    }

    public void setbDate(Object bDate) {
        this.bDate = (String)bDate;
    }

    public Object getbDate() {
        return bDate;
    }

    public void seteDate(Object eDate) {
        this.eDate = (String)eDate;
    }

    public Object geteDate() {
        return eDate;
    }

    public void setiStation(Object obj) {
        if (obj instanceof Station) {
            mapSD.put((Station) obj, DEFAULT_DURATION);
        } else if (obj instanceof HashMap) {
            Map<Station, Boolean> map = (Map<Station, Boolean>) obj;
            for (Station station : map.keySet()) {
                if (mapSD.containsKey(station)) {
                    mapSD.remove(station);
                } else {
                    mapSD.put(station, DEFAULT_DURATION);
                }
            }
        }
    }

    public void setiStationDuration(Object obj) {
        Map<Station, Integer> map = (Map<Station, Integer>) obj;
        for (Station station : map.keySet()) {
            map.put(station, map.get(station));
        }
    }

    public Object getiStationDuration() {
        return mapSD;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("bDate = " + bDate + "\n");
        str.append("eDate = " + eDate + "\n");
        return str.toString();
    }
}
