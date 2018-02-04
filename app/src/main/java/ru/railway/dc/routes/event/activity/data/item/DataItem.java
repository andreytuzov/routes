package ru.railway.dc.routes.event.activity.data.item;

/**
 * Created by SQL on 07.01.2017.
 */

public class DataItem {

    private String station;
    private String bTime;
    private String eTime;
    private String trainTime;
    private String trainTimeGroup;
    private String stationTime;
    private String typeTrainGroup;
    private boolean isGroup;

    // Для Child
    public DataItem(String station, String bTime, String eTime, String trainTime, String stationTime) {
        this(station, bTime, eTime, trainTime, trainTime, stationTime, false, null);
    }

    // Для Group
    public DataItem(String station, String bTime, String eTime,
                    String trainTime, String trainTimeGroup,
                    String stationTime, boolean isGroup, String typeTrainGroup) {
        this.station = station;
        this.bTime = bTime;
        this.eTime = eTime;
        this.trainTime = trainTime;
        this.trainTimeGroup = trainTimeGroup;
        this.stationTime = stationTime;
        this.isGroup = isGroup;
        this.typeTrainGroup = typeTrainGroup;
    }


    public String getStation() {
        return station;
    }

    public String getbTime() {
        return bTime;
    }

    public String geteTime() {
        return eTime;
    }

    public String getTrainTime() {
        return trainTime;
    }

    public String getStationTime() {
        return stationTime;
    }

    public String getTrainTimeGroup() {
        return trainTimeGroup;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public String getTypeTrainGroup() {
        return typeTrainGroup;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("station = " + station);
        str.append(", bTime = " + bTime);
        str.append(", eTime = " + eTime);
        str.append(", trainTime = " + trainTime);
        str.append(", stationTime = " + stationTime);
        str.append(", trainTimeGroup = " + trainTimeGroup);
        str.append(", isGroup = " + isGroup);
        return "\n" + str.toString();
    }
}
