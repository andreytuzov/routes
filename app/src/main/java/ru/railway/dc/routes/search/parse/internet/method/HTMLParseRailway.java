package ru.railway.dc.routes.search.parse.internet.method;

/**
 * Created by SQL on 28.12.2016.
 */

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ru.railway.dc.routes.search.model.ListRoute;
import ru.railway.dc.routes.search.model.Route;

public class HTMLParseRailway implements IParseRailway {

    public static Logger logger = Logger.getLogger(HTMLParseRailway.class);

    // URL паттерн

    private final String URL_RAILWAY = "http://pass.rw.by/ru/route/?from=%1$s&to=%2$s&date=%3$s&s=main";

    // Теги для парсинга
    private final String TAG_START_TIME = ".sch-table__time.train-from-time";
    private final String TAG_END_TIME = ".sch-table__time.train-to-time";
    private final String TAG_START_PLACE = "div.train-from-name";
    private final String TAG_END_PLACE = "div.train-to-name";
    private final String TAG_NUMBER_TRAIN = "span.train-number";
    private final String TAG_TYPE_TRAIN = "div.sch-table__train-type > i";
    private final String TAG_DETAIL_URI = "a.sch-table__route";
    // Максимальное время соединения
    private static final int TIMEOUT = 40000;

    @Override
    public ListRoute get(String bStation, String eStation, String date) {
        ListRoute routes = null;
        try {
            String url = new String(String.format(URL_RAILWAY,
                    URLEncoder.encode(bStation, "UTF-8"),
                    URLEncoder.encode(eStation, "UTF-8"),
                    date));
            logger.debug("Начало загрузки\taddress = " + url);
            // Грузим HTML-страницу по URL_RAILWAY адресу с указанными параметрами
            Document doc = Jsoup.connect(url)
                    .timeout(TIMEOUT)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; Trident/7.0; rv:11.0) like Gecko")
                    .get();
            routes = new ListRoute();
            // Получаем элементы из HTML-страницы
            Elements bTimeElements = doc.select(TAG_START_TIME);
            Elements eTimeElements = doc.select(TAG_END_TIME);
            Elements bPlaceElements = doc.select(TAG_START_PLACE);
            Elements ePlaceElements = doc.select(TAG_END_PLACE);
            Elements numberTrain = doc.select(TAG_NUMBER_TRAIN);
            Elements typeTrain = doc.select(TAG_TYPE_TRAIN);
            Elements detailURI = doc.select(TAG_DETAIL_URI);
            // Выделяем информацию из элементов

            for (int i = 0; i < bTimeElements.size(); i++) {
                // Дата и время
                String bDateTime = getDateTime(date, bTimeElements.get(i).text());
                String eDateTime = getDateTime(date, eTimeElements.get(i).text());
                if (!isCorrectDateTime(bDateTime, eDateTime)) continue;;
                Route route = new Route();
                route.setBEnterStation(bStation);
                route.setEEnterStation(eStation);
                route.setBStation(bPlaceElements.get(i).text());
                route.setEStation(ePlaceElements.get(i).text());
                route.setNumberTrain(numberTrain.get(i).text());
                route.setDetailURI(detailURI.get(i).attr("href"));
                route.setDateTime(bDateTime, eDateTime);
                // Тип поезда
                String type_attr = typeTrain.get(i).attr("class");
                String type = type_attr.substring(type_attr.indexOf("-") + 1, type_attr.length())
                        .trim();
                route.setTypeTrain(type);

                routes.add(route);
            }
        } catch (ParseException e) {
            logger.error("Ошибка парсинга");
        } catch (IOException e) {
            logger.info("Host не доступен");
        }
        return routes;
    }

    private boolean isCorrectDateTime(String sBDateTime, String sEDateTime) {
        SimpleDateFormat format = new SimpleDateFormat(Route.DATE_TIME_FORMAT, Locale.getDefault());
        try {
            Date bDateTime = format.parse(sBDateTime);
            Date eDateTime = format.parse(sEDateTime);
            return eDateTime.getTime() > bDateTime.getTime();
        } catch (ParseException ex) {
            return false;
        }
    }

    // Определение даты
    private String getDateTime(String date, String time) {
        String[] months = {"января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября",
                "октября", "ноября", "декабря"};
        if (time.contains(",")) {
            // Выделение дня, месяца и года
            int day = 0, month = 0, year = 0;
            int comma = time.indexOf(","), space = time.trim().lastIndexOf(" ");
            day = Integer.valueOf(time.substring(comma + 1, space).trim());
            String sMonth = time.substring(space, time.length()).trim();
            for (int i = 0; i < months.length; i++) {
                if (months[i].startsWith(sMonth)) {
                    month = i + 1;
                }
            }
            year = Integer.valueOf(date.substring(0, date.indexOf("-")));
            return year + "-" + month + "-" + day + " " + time;
        }
        return date + " " + time;
    }
}
