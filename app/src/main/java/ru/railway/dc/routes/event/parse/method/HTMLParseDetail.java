package ru.railway.dc.routes.event.parse.method;

/**
 * Created by SQL on 28.12.2016.
 */

import android.util.Log;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.railway.dc.routes.search.model.ListRoute;
import ru.railway.dc.routes.search.model.Route;


// TODO промежуточные станции могут быть без остановок
public class HTMLParseDetail implements IParseDetail {

    public static Logger logger = Logger.getLogger(HTMLParseDetail.class);

    private int year;
    private int month;
    private int day;

    // URL паттерн
    private final String URL_DETAIL = "http://rasp.rw.by%1$s";
    // User Agent
    private final String USER_AGENT = "Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6";

    private final String ATTR_FROM = "&from=";
    private final String ATTR_TO = "&to=";
    private final String ATTR_TRAIN = "train=";

    // Теги для парсинга
    private final String TAG_START_TIME = "td.train_item.train_start";
    private final String TAG_END_TIME = "td.train_item.train_end";
    private final String TAG_STATION = "a.train_text";
    // Максимальное время соединения
    private static final int TIMEOUT = 40000;

    private String getFormatURL(String url) throws UnsupportedEncodingException {
        Pattern p = Pattern.compile("(?:train=)([^&]+).+(?:from=)([^&]+).+(?:to=)([^&]+)");
        Matcher m = p.matcher(url);
        if (m.find()) {
            for (int i = 1; i <= m.groupCount(); i++) {
                url = url.replace("=" + m.group(i), "=" + URLEncoder.encode(m.group(i), "UTF-8"));
            }
        }
        return url;
    }

    public ListRoute get(Route route) {
        logger.debug("route = " + route);
        // Получаем параметры
        ListRoute routes = new ListRoute();
        String bStation = route.getBStation();
        String eStation = route.getEStation();
        String detailURI = route.getDetailURI();
        try {

            String url = getFormatURL(String.format(URL_DETAIL, detailURI));

            // Грузим HTML-страницу по URL_RAILWAY адресу с указанными параметрами
            Document doc = Jsoup.connect(url)
                    .timeout(TIMEOUT)
                    .userAgent(USER_AGENT)
                    .get();
            logger.debug("\turl = " + url + ", doc = " + doc);
            // Получаем элементы из HTML-страницы
            Elements bTimeElements = doc.select(TAG_START_TIME);
            Elements eTimeElements = doc.select(TAG_END_TIME);
            Elements stationElements = doc.select(TAG_STATION);

            // Удаляем пустые элементы (поезд не останавливается здесь)
            Elements dBTimeElements = new Elements();
            Elements dETimeElements = new Elements();
            Elements dStationElements = new Elements();
            for (int i = 0; i < bTimeElements.size(); i++) {
                if (bTimeElements.get(i).text().trim().isEmpty() &&
                        eTimeElements.get(i).text().trim().isEmpty()) {
                    dBTimeElements.add(bTimeElements.get(i));
                    dETimeElements.add(eTimeElements.get(i));
                    dStationElements.add(stationElements.get(i));
                }
            }
            bTimeElements.removeAll(dBTimeElements);
            eTimeElements.removeAll(dETimeElements);
            stationElements.removeAll(dStationElements);

            year = route.getBTime().get(Calendar.YEAR);
            month = route.getBTime().get(Calendar.MONTH) + 1;
            day = route.getBTime().get(Calendar.DAY_OF_MONTH);

            String bTime = null, eTime = null;
            // Получаем информацию из элементов
            for (int i = 0; i < bTimeElements.size() - 1; i++) {
                Route r = new Route();

                r.setBEnterStation(bStation);
                r.setEEnterStation(eStation);
                r.setNumberTrain(route.getNumberTrain());
                r.setTypeTrain(route.getTypeTrain());

                // Выделяем дату
                bTime = bTimeElements.get(i).text();
                // Если принята строка вида 10:20, 21 янв
                splitDate(bTime);
                // Если время меньше предыдущего, то новый день
                if (lt(bTime, eTime)) {
                    day++;
                }
                r.setBDateTime(year + "-" + month + "-" + day + " " + bTime);

                logger.debug("bTime = " + bTime + "day = " + day + ", month = " + month + ", year = " + year);

                // Конечная дата
                eTime = eTimeElements.get(i + 1).text();
                splitDate(eTime);
                if (lt(eTime, bTime)) {
                    day++;
                }
                r.setEDateTime(year + "-" + month + "-" + day + " " + eTime);

                logger.debug("eTime = " + eTime + "day = " + day + ", month = " + month + ", year = " + year);

                r.setBStation(stationElements.get(i).text());
                r.setEStation(stationElements.get(i + 1).text());

                routes.add(r);
            }
        } catch (ParseException e) {
            logger.error("Error parsing");
        } catch (IOException e) {
            logger.info("Host not found");
        }
        logger.debug("routes = " + routes);
        return routes;
    }

    // Сравнение времени по строке
    private boolean lt(String t1, String t2) throws ParseException {
        if (t1 == null || t2 == null) {
            return false;
        }
        SimpleDateFormat format = new SimpleDateFormat(Route.TIME_FORMAT);
        return format.parse(t1)
                .before(format.parse(t2));
    }

    // Выделение части даты
    private void splitDate(String time) {
        String[] months = { "января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября",
                "октября", "ноября", "декабря" };
        if (time.contains(",")) {
            time = time.replace((char)160, ' ').trim();
            // Выделение дня, месяца и года
            int day = 0, month = 0;
            int comma = time.indexOf(','),
                    space = time.lastIndexOf(' ');
            day = Integer.valueOf(time.substring(comma + 1, space).trim());
            String sMonth = time.substring(space, time.length()).trim();
            for (int i = 0; i < months.length; i++) {
                if (months[i].startsWith(sMonth)) {
                    month = i + 1;
                }
            }
            if (month >= this.month)
                this.month = month;
            if (day >= this.day)
                this.day = day;
        }
    }
}
