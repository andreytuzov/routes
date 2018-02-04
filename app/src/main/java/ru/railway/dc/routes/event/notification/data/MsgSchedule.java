package ru.railway.dc.routes.event.notification.data;


import android.app.Notification;
import android.content.Context;

import org.apache.log4j.Logger;

import java.util.Calendar;
import java.util.List;

import ru.railway.dc.routes.event.notification.data.item.IMessage;

/**
 * Created by SQL on 31.12.2016.
 */

public class MsgSchedule {

    public final static Logger logger = Logger.getLogger(MsgSchedule.class);

    private List<MsgTime> msg;
    private Context context;

    public MsgSchedule(List<MsgTime> msg, Context context) {
        this.context = context;
        this.msg = msg;
        // Доходим до текущего события
        long time = Calendar.getInstance().getTimeInMillis();
        logger.debug("time = " + time + ", size = " + msg.size());
        // Находим первое событие, которое не завершилось
        for (int i = 0; i < msg.size(); i++) {
            MsgTime msgTime = msg.get(i);
            logger.debug("before check: " + msgTime);
            // Есть событие, которое еще не закончилось
            msgTime.checkCompleted(time);
            logger.debug("after check: " + msgTime);
        }
    }

    public boolean addMessage(long time, Notification.Builder builder) {
        return addMessage(time, 0, builder);
    }

    // TODO ускорить обход Index
    // Выводим сообщение для заданного времени
    public boolean addMessage(long time, Integer index, Notification.Builder builder) {
        MsgTime msgTime = null;
        boolean isNotCompleted = false;
        logger.debug("time = " + time + ", size = " + msg.size());
        // Доходим до события, которое еще не началось
        for (; index < msg.size(); index++) {
            msgTime = msg.get(index);
            if (msgTime.getSTime() > time) {
                return false;
            }
            msgTime.addMessage(builder, time, context);
            // Находим хотя бы одно не завершенное
            isNotCompleted |= !msgTime.isCompleted;
        }
        // Проверка, что последнее событие не завершилось
        return !isNotCompleted;
    }

    public static class MsgTime {
        private long sTime;
        private long eTime;
        private boolean isCompleted;
        private IMessage msg;

        public MsgTime(long time, IMessage msg) {
            this(time, time, msg);
        }

        public MsgTime(long sTime, long eTime, IMessage msg) {
            this.sTime = sTime;
            this.eTime = eTime;
            this.msg = msg;
        }

        public long getSTime() {
            return sTime;
        }

        public long getETime() {
            return eTime;
        }

        public boolean checkCompleted(long time) {
            if (time > eTime) {
                isCompleted = true;
            }
            return isCompleted;
        }

        public void addMessage(Notification.Builder builder, long time, Context context) {
            if (!isCompleted) {
                msg.addMessage(builder, time, context);
                checkCompleted(time);
            }
        }

        @Override
        public String toString() {
            StringBuilder str = new StringBuilder();
            str.append(msg + ": (" + sTime + ";" + eTime + ") = " + isCompleted);
            return str.toString();
        }
    }
}
