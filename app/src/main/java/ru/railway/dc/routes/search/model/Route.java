package ru.railway.dc.routes.search.model;

/**
 * Created by SQL on 27.12.2016.
 */

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Класс для описания маршрута
 * @author SQL
 *
 */
public class Route {

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm";

    protected String bStation;
    protected String eStation;
    protected String bEnterStation;
    protected String eEnterStation;

    protected Calendar bDateTime;
    protected Calendar eDateTime;

    protected String numberTrain;
    protected String typeTrain;

    protected String detailURI;

    // ================================= КОНСТРУКТОРЫ ==============================================

    public Route() {
    }

    // Нулевой маршрут (нет маршрута)
    public Route(String bStation, String eStation, String date) throws ParseException {
        this.bEnterStation = bStation;
        this.eEnterStation = eStation;
        this.bStation = bStation;
        this.eStation = eStation;

        // Одинаковые даты
        setBDateTime(date + " 17:00");
        setEDateTime(date + " 17:00");
    }


    public Route(Route route) {
        this.bEnterStation = route.bEnterStation;
        this.eEnterStation = route.eEnterStation;
        // Название станций после парсинга
        this.bStation = route.bStation;
        this.eStation = route.eStation;
        // Номер и тип поезда
        this.numberTrain = route.numberTrain;
        this.typeTrain = route.typeTrain;
        // Преобразование времени и даты в тип Calendar
        this.bDateTime = route.bDateTime;
        this.eDateTime = route.eDateTime;
        this.detailURI = route.detailURI;
    }

    // ==================================== SET ====================================================

    public void setBStation(String bStation) {
        this.bStation = bStation;
    }

    public void setEStation(String eStation) {
        this.eStation = eStation;
    }

    public void setBEnterStation(String bEnterStation) {
        this.bEnterStation = bEnterStation;
    }

    public void setEEnterStation(String eEnterStation) {
        this.eEnterStation = eEnterStation;
    }

    public void setNumberTrain(String numberTrain) {
        this.numberTrain = numberTrain;
    }

    public void setTypeTrain(String typeTrain) {
        this.typeTrain = typeTrain;
    }

    public void setDetailURI(String detailURI) {
        this.detailURI = detailURI;
    }

    public void setBDateTime(long bDateTime) {
        this.bDateTime = new GregorianCalendar();
        this.bDateTime.setTime(new Date(bDateTime));
    }

    public void setBDateTime(String bDateTime) throws ParseException {
        this.bDateTime = new GregorianCalendar();
        this.bDateTime.setTime(new SimpleDateFormat(DATE_TIME_FORMAT).parse(bDateTime));
    }

    public void setBDateTime(Calendar bDateTime) {
        this.bDateTime = bDateTime;
    }

    public void setEDateTime(long eDateTime) {
        this.eDateTime = new GregorianCalendar();
        this.eDateTime.setTime(new Date(eDateTime));
    }

    public void setEDateTime(String eDateTime) throws ParseException {
        this.eDateTime = new GregorianCalendar();
        this.eDateTime.setTime(new SimpleDateFormat(DATE_TIME_FORMAT).parse(eDateTime));
    }

    public void setEDateTime(Calendar eDateTime) {
        this.eDateTime = eDateTime;
    }

    public void setDateTime(String bDateTime, String eDateTime) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat(DATE_TIME_FORMAT);
        this.bDateTime = new GregorianCalendar();
        this.bDateTime.setTime(format.parse(bDateTime));
        this.eDateTime = new GregorianCalendar();
        this.eDateTime.setTime(format.parse(eDateTime));
    }

    // ==================================== GET ====================================================

    public String getBStation() {
        return bStation;
    }

    public String getEStation() {
        return eStation;
    }

    public String getNumberTrain() {
        return numberTrain;
    }

    public String getTypeTrain() {
        return typeTrain;
    }

    public String getBEnterStation() {
        return bEnterStation;
    }

    public String getEEnterStation() {
        return eEnterStation;
    }

    public Calendar getBTime() {
        return bDateTime;
    }

    public Calendar getETime() {
        return eDateTime;
    }

    public String getDetailURI() {
        return detailURI;
    }

    // Строковое представления даты
    private String get(Calendar c, String f) {
        return new SimpleDateFormat(f)
                .format(c.getTime());
    }

    public String getETimeString(String pattern) {
        return get(eDateTime, pattern);
    }


    public String getBTimeString(String pattern) {
        return get(bDateTime, pattern);
    }

    // =============================== БАЗОВЫЕ ФУНКЦИИ =============================================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Route other = (Route) obj;
        if (!get(bDateTime, DATE_TIME_FORMAT).equals(get(other.bDateTime, DATE_TIME_FORMAT)) ||
                !get(eDateTime, DATE_TIME_FORMAT).equals(get(other.eDateTime, DATE_TIME_FORMAT)) ||
                !this.bEnterStation.equals(other.bEnterStation) ||
                !this.eEnterStation.equals(other.eEnterStation) ||
                !this.bStation.equals(other.bStation) ||
                !this.eStation.equals(other.eStation) ||
                !this.numberTrain.equals(other.numberTrain) ||
                !this.typeTrain.equals(other.typeTrain) ||
                !this.detailURI.equals(other.detailURI)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = bStation.hashCode() ^ bDateTime.hashCode();
        result *= 100 + eStation.hashCode() + eDateTime.hashCode();
        result -= typeTrain.hashCode() - numberTrain.hashCode();
        result += bEnterStation.hashCode() + eEnterStation.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("%3$s - %4$s %1$s - %2$s", bStation, eStation,
                getBTimeString(TIME_FORMAT), getETimeString(TIME_FORMAT));
    }
}
