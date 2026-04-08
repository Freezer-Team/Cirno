package nep.timeline.cirno.hooks.android.alarms;

import nep.timeline.cirno.framework.MethodHook;
import nep.timeline.cirno.reflect.CakeHooker;
import nep.timeline.cirno.reflect.CakeReflection;
import nep.timeline.cirno.utils.ForceAppStandbyListener;

public class AlarmManagerService extends MethodHook {
    public AlarmManagerService(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public String getTargetClass() {
        return "com.android.server.alarm.AlarmManagerService";
    }

    @Override
    public String getTargetMethod() {
        return "onBootPhase";
    }

    @Override
    public Object[] getTargetParam() {
        return new Object[] { int.class };
    }

    @Override
    public CakeHooker.Callback getTargetHook() {
        return new CakeHooker.Callback() {
            @Override
            public void call(CakeHooker.BeforeHookCallback callback) {
                ForceAppStandbyListener.setInstance(CakeReflection.getObjectField(callback.getThisObject(), "mForceAppStandbyListener"));
            }
        };
    }
}
