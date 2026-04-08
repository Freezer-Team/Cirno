package nep.timeline.cirno.hooks.android.binder;

import android.os.Build;

import nep.timeline.cirno.framework.MethodHook;
import nep.timeline.cirno.reflect.CakeHooker;
import nep.timeline.cirno.services.BinderService;
import nep.timeline.cirno.services.FreezerService;
import nep.timeline.cirno.utils.SystemChecker;

public class MilletBinderTransHook extends MethodHook {
    public MilletBinderTransHook(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public String getTargetClass() {
        return "com.miui.server.greeze.GreezeManagerService";
    }

    @Override
    public String getTargetMethod() {
        return "reportBinderTrans";
    }

    @Override
    public Object[] getTargetParam() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
            return new Object[]{ int.class, int.class, int.class, int.class, int.class, boolean.class, long.class, int.class, int.class };
        return new Object[]{ int.class, int.class, int.class, int.class, int.class, boolean.class, long.class, int.class };
    }

    @Override
    public CakeHooker.Callback getTargetHook() {
        return new CakeHooker.Callback() {
            @Override
            public void call(CakeHooker.BeforeHookCallback callback) {
                if (BinderService.received) {
                    unhook();
                    return;
                }

                boolean isOneway = (boolean) callback.getArgs()[5];
                if (isOneway)
                    return;

                int dstUid = (int) callback.getArgs()[0];

                FreezerService.temporaryUnfreezeIfNeed(dstUid, "Binder", 3000);
            }
        };
    }

    @Override
    public boolean isIgnoreError() {
        return !SystemChecker.isXiaomi(classLoader);
    }
}
