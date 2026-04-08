package nep.timeline.cirno.hooks.android.wakelock;

import android.os.Build;
import android.os.IBinder;
import android.os.WorkSource;

import nep.timeline.cirno.entity.AppRecord;
import nep.timeline.cirno.framework.MethodHook;
import nep.timeline.cirno.reflect.CakeHooker;
import nep.timeline.cirno.services.AppService;
import nep.timeline.cirno.utils.PKGUtils;

public class WakeLockHook extends MethodHook {
    public WakeLockHook(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public String getTargetClass() {
        return "com.android.server.power.PowerManagerService";
    }

    @Override
    public String getTargetMethod() {
        return "acquireWakeLockInternal";
    }

    @Override
    public Object[] getTargetParam() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2)
            return new Object[] { IBinder.class, int.class, int.class, String.class, String.class, WorkSource.class, String.class, int.class, int.class, "android.os.IWakeLockCallback" };
        return new Object[] { IBinder.class, int.class, int.class, String.class, String.class, WorkSource.class, String.class, int.class, int.class };
    }

    @Override
    public CakeHooker.Callback getTargetHook() {
        return new CakeHooker.Callback() {
            @Override
            public void call(CakeHooker.BeforeHookCallback callback) {
                String packageName = (String) callback.getArgs()[4];
                int uid = (int) callback.getArgs()[7];

                AppRecord appRecord = AppService.get(packageName, PKGUtils.getUserId(uid));
                if (appRecord == null)
                    return;

                if (appRecord.isFrozen())
                    callback.returnAndSkip(null);
            }
        };
    }
}
