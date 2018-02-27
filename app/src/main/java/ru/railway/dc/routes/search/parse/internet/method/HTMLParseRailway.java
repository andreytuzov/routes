package ru.railway.dc.routes.search.parse.internet.method;

/**
 * Created by SQL on 28.12.2016.
 */

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;

import ru.railway.dc.routes.search.model.ListRoute;
import ru.railway.dc.routes.search.model.Route;

public class HTMLParseRailway implements IParseRailway {

    public static Logger logger = Logger.getLogger(HTMLParseRailway.class);

    // URL паттерн

    private final String URL_RAILWAY = "http://rasp.rw.by/ru/route/?from=%1$s&to=%2$s&date=%3$s&s=main";

    // Теги для парсинга
    private final String TAG_START_TIME = "b.train_start-time";
    private final String TAG_END_TIME = "b.train_end-time";
    private final String TAG_START_PLACE = "a.train_start-place";
    private final String TAG_END_PLACE = "a.train_end-place";
    private final String TAG_NUMBER_TRAIN = "small.train_id";
    private final String TAG_TYPE_TRAIN = "div.train_inner > i.b-pic.train_type";
    private final String TAG_DETAIL_URI = "a.train_text";
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
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
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
                Route route = new Route();

                route.setBEnterStation(bStation);
                route.setEEnterStation(eStation);
                route.setBStation(bPlaceElements.get(i).text());
                route.setEStation(ePlaceElements.get(i).text());
                route.setNumberTrain(numberTrain.get(i).text());
                route.setDetailURI(detailURI.get(i).attr("href"));
                // Дата и время
                String bDateTime = getDateTime(date, bTimeElements.get(i).text());
                String eDateTime = getDateTime(date, eTimeElements.get(i).text());
                route.setDateTime(bDateTime, eDateTime);
                // Тип поезда
                String type_attr = typeTrain.get(i).attr("class");
                String type = type_attr.substring(type_attr.lastIndexOf(' '), type_attr.length())
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

    // Определение даты
    private String getDateTime(String date, String time) {
        String[] months = { "января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября",
                "октября", "ноября", "декабря" };
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
