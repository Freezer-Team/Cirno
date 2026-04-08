package nep.timeline.cirno.hooks.android.intent;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import nep.timeline.cirno.entity.AppRecord;
import nep.timeline.cirno.entity.PendingIntentKey;
import nep.timeline.cirno.framework.MethodHook;
import nep.timeline.cirno.reflect.CakeHooker;
import nep.timeline.cirno.reflect.CakeReflection;
import nep.timeline.cirno.services.AppService;
import nep.timeline.cirno.services.FreezerService;

public class PendingIntentHook extends MethodHook {
    public PendingIntentHook(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public String getTargetClass() {
        return "com.android.server.am.PendingIntentRecord";
    }

    @Override
    public String getTargetMethod() {
        return "sendInner";
    }

    @Override
    public Object[] getTargetParam() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU)
            return CakeReflection.findParameterTypesOrDefault(CakeReflection.findClassIfExists(getTargetClass(), classLoader), getTargetMethod(), "android.app.IApplicationThread", int.class, Intent.class, String.class, IBinder.class, "android.content.IIntentReceiver", String.class, IBinder.class, String.class, int.class, int.class, int.class, Bundle.class);
        return CakeReflection.findParameterTypesOrDefault(CakeReflection.findClassIfExists(getTargetClass(), classLoader), getTargetMethod(), int.class, Intent.class, String.class, IBinder.class, "android.content.IIntentReceiver", String.class, IBinder.class, String.class, int.class, int.class, int.class, Bundle.class);
    }

    @Override
    public CakeHooker.Callback getTargetHook() {
        return new CakeHooker.Callback() {
            @Override
            public void call(CakeHooker.BeforeHookCallback callback) {
                synchronized (CakeReflection.getObjectField(CakeReflection.getObjectField(callback.getThisObject(), "controller"), "mLock")) {
                    if (CakeReflection.getBooleanField(callback.getThisObject(), "canceled"))
                        return;

                    Object key = CakeReflection.getObjectField(callback.getThisObject(), "key");
                    if (key == null)
                        return;

                    PendingIntentKey pendingIntentKey = new PendingIntentKey(key);

                    AppRecord appRecord = AppService.get(pendingIntentKey.getPackageName(), pendingIntentKey.getUserId());

                    if (appRecord == null || !appRecord.isFrozen())
                        return;

                    FreezerService.temporaryUnfreezeIfNeed(appRecord, "Intent", 3000);
                }
            }
        };
    }
}