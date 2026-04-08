package nep.timeline.cirno.hooks.android.process;

import nep.timeline.cirno.framework.MethodHook;
import nep.timeline.cirno.reflect.CakeHooker;
import nep.timeline.cirno.services.ProcessService;

public class ProcessRemoveHook extends MethodHook {
    public ProcessRemoveHook(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public String getTargetClass() {
        return "com.android.server.am.ProcessList";
    }

    @Override
    public String getTargetMethod() {
        return "removeProcessNameLocked";
    }

    @Override
    public Object[] getTargetParam() {
        return new Object[] { String.class, int.class, "com.android.server.am.ProcessRecord" };
    }

    @Override
    public CakeHooker.Callback getTargetHook() {
        return new CakeHooker.Callback() {
            @Override
            public void call(CakeHooker.BeforeHookCallback callback) {
                String name = (String) callback.getArgs()[0];
                int uid = (int) callback.getArgs()[1];
                ProcessService.removeProcessRecord(name, uid);
            }
        };
    }
}