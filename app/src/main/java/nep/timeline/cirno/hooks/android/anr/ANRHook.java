package nep.timeline.cirno.hooks.android.anr;

import nep.timeline.cirno.framework.MethodHook;
import nep.timeline.cirno.reflect.CakeHooker;
import nep.timeline.cirno.reflect.CakeReflection;
import nep.timeline.cirno.utils.AnrHelper;

public class ANRHook extends MethodHook {
    public ANRHook(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public String getTargetClass() {
        return "com.android.server.am.AnrHelper$AnrRecord";
    }

    @Override
    public String getTargetMethod() {
        return "appNotResponding";
    }

    @Override
    public Object[] getTargetParam() {
        return new Object[] { boolean.class };
    }

    @Override
    public CakeHooker.Callback getTargetHook() {
        return new CakeHooker.Callback() {
            @Override
            public void call(CakeHooker.BeforeHookCallback callback) {
                Object app = CakeReflection.getObjectField(callback.getThisObject(), "mApp");
                if (app == null)
                    return;
                if (AnrHelper.blockANR(app))
                    callback.returnAndSkip(null);
            }
        };
    }
}
