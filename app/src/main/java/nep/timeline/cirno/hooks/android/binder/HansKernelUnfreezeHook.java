package nep.timeline.cirno.hooks.android.binder;

import nep.timeline.cirno.framework.MethodHook;
import nep.timeline.cirno.reflect.CakeHooker;
import nep.timeline.cirno.services.BinderService;
import nep.timeline.cirno.services.FreezerService;
import nep.timeline.cirno.utils.SystemChecker;

public class HansKernelUnfreezeHook extends MethodHook {
    public HansKernelUnfreezeHook(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public String getTargetClass() {
        return "com.android.server.am.OplusHansManager";
    }

    @Override
    public String getTargetMethod() {
        return "unfreezeForKernel";
    }

    @Override
    public Object[] getTargetParam() {
        return new Object[] { int.class, int.class, int.class, int.class, int.class, String.class, int.class };
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

                int type = (int) callback.getArgs()[0];
                if (type != 1) // Sync binder
                    return;
                int target = (int) callback.getArgs()[4];

                FreezerService.temporaryUnfreezeIfNeed(target, "Binder", 3000);
            }
        };
    }

    @Override
    public boolean isIgnoreError() {
        return !SystemChecker.isOplus(classLoader);
    }
}
