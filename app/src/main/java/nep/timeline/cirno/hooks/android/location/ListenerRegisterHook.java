package nep.timeline.cirno.hooks.android.location;

import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.Binder;
import android.os.IBinder;

import java.util.Set;

import nep.timeline.cirno.entity.AppRecord;
import nep.timeline.cirno.framework.MethodHook;
import nep.timeline.cirno.handlers.LocationHandler;
import nep.timeline.cirno.reflect.CakeHooker;
import nep.timeline.cirno.services.AppService;
import nep.timeline.cirno.threads.Handlers;
import nep.timeline.cirno.utils.PKGUtils;
import nep.timeline.cirno.virtuals.ILocationListener;

public class ListenerRegisterHook extends MethodHook {
    public ListenerRegisterHook(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public String getTargetClass() {
        return "com.android.server.location.LocationManagerService";
    }

    @Override
    public String getTargetMethod() {
        return "registerLocationListener";
    }

    @Override
    public Object[] getTargetParam() {
        return new Object[] { String.class, LocationRequest.class, "android.location.ILocationListener", String.class, String.class, String.class };
    }

    @Override
    public CakeHooker.Callback getTargetHook() {
        return new CakeHooker.Callback() {
            @Override
            public void call(CakeHooker.AfterHookCallback callback) {
                boolean isGPS = LocationManager.GPS_PROVIDER.equals(callback.getArgs()[0]);
                if (!isGPS)
                    return;

                String packageName = (String) callback.getArgs()[3];
                int uid = Binder.getCallingUid();

                ILocationListener listener = new ILocationListener(callback.getArgs()[2]);

                Handlers.location.post(() -> {
                    AppRecord appRecord = AppService.get(packageName, PKGUtils.getUserId(uid));

                    if (appRecord == null)
                        return;

                    Set<IBinder> set = appRecord.getAppState().getLocationListeners();
                    if (set.add(listener.asBinder()))
                        LocationHandler.call(appRecord, set);
                });
            }
        };
    }
}