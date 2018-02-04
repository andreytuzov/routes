package ru.railway.dc.routes.event.notification.data.item;

import android.app.Notification;
import android.content.Context;
import android.graphics.Color;

/**
 * Created by SQL on 31.12.2016.
 */

public class EffectMessage implements IMessage {

    private EffectType effectType;

    public EffectMessage(EffectType effectType) {
        this.effectType = effectType;
    }

    @Override
    public void addMessage(Notification.Builder builder, long time, Context context) {
        effectType.addEffect(builder);
    }


    interface IEffect {
        void addEffect(Notification.Builder builder);
    }

    // Эффекты
    public enum EffectType implements IEffect {
        RED_LED_VIBRA {
            @Override
            public void addEffect(Notification.Builder builder) {
                builder.setLights(Color.argb(255, 255, 0, 0), 500, 500)
                        .setVibrate(new long[] {100, 200, 300, 400, 500});
            }
        }
    }


}
