package ru.railway.dc.routes.request.data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by SQL on 05.12.2016.
 */

public class RequestDataSingleton {

    public final static String RDS = "rds";

    private RequestData requestData;

    private List<OnChangeDataListener> listeners;

    // ============================ РЕАЛИЗАЦИЯ ПАТТЕРНА SINGLETON ==================================

    private static RequestDataSingleton instance = new RequestDataSingleton();

    // Загрузка данные из файлов ресурсов
    public static void loadData(RequestData requestData) {
        instance.listeners = new ArrayList<>();
        instance.requestData = requestData;
    }

    public static RequestDataSingleton getInstance() {
        if (instance.requestData == null) {
            throw new IllegalStateException("RequestData is null. Call the method loadData");
        }
        return instance;
    }

    private RequestDataSingleton() {}

    // ========================= РАБОТА С ОПОВЕЩЕНИЯМИ =============================================

    // TODO разобраться с регистрацией
    public void register(OnChangeDataListener listener) {
        for (OnChangeDataListener l : listeners) {
            if (l.getClass().getName().equals(listener.getClass().getName())) {
                listeners.remove(l);
            }
        }
        listeners.add(listener);
    }

    public void unregister(OnChangeDataListener listener) {
        listeners.remove(listener);
    }

    public void notification(String param, Object data) {
        // Устанавливаем новые данные
        setData(param, data);
        // Оповещаем слушателей
        int i = 0;
        for (OnChangeDataListener listener : listeners) {
            listener.changeData(param, this);
        }
    }

    public void updateData() {
        for (OnChangeDataListener listener : listeners) {                                           // Изменить на массив
            listener.changeData(Param.B_TIME, this);
            listener.changeData(Param.E_TIME, this);
            listener.changeData(Param.B_STATION, this);
            listener.changeData(Param.E_STATION, this);
            listener.changeData(Param.B_DATE, this);
            listener.changeData(Param.E_DATE, this);
        }
    }

    public interface OnChangeDataListener {
        void changeData(String param, RequestDataSingleton rds);
    }

    // ============================ ВЗАИМОДЕЙСТВИЕ С ДАННЫМИ =======================================

    private void setData(String param, Object data) {
        // Обновляем данные
        try {
            Class c = RequestData.class;
            Method method = c.getMethod("set" + param, new Class[]{Object.class});
            method.invoke(requestData, data);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        }
    }

    public Object findDataByName(String param) {
        Object result = null;
        try {
            Class c = RequestData.class;
            Method method = c.getMethod("get" + param, null);
            result = method.invoke(requestData, null);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static class Param {
        // Основные константы, используемые для запроса
        public static final String B_STATION = "bStation";
        public static final String E_STATION = "eStation";
        public static final String I_STATION = "iStation";
        public static final String B_DATE = "bDate";
        public static final String E_DATE = "eDate";
        public static final String B_TIME = "bTime";
        public static final String E_TIME = "eTime";

        public static final String I_STATION_DURATION = "iStationDuration";
    }
}
