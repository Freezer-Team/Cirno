package nep.timeline.cirno.virtuals;

import android.os.IBinder;

import lombok.Getter;
import nep.timeline.cirno.reflect.CakeReflection;

@Getter
public class ILocationListener {
    private final Object instance;

    public ILocationListener(Object instance) {
        this.instance = instance;
    }

    public IBinder asBinder() {
        if (instance == null)
            return null;

        return (IBinder) CakeReflection.callMethod(instance, "asBinder");
    }
}
