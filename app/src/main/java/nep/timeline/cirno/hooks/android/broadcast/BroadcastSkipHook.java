package nep.timeline.cirno.hooks.android.broadcast;

import android.os.Build;

import nep.timeline.cirno.framework.MethodHook;
import nep.timeline.cirno.reflect.CakeHooker;
import nep.timeline.cirno.reflect.CakeReflection;
import nep.timeline.cirno.services.ProcessService;
import nep.timeline.cirno.utils.SystemChecker;
import nep.timeline.cirno.virtuals.ProcessRecord;

public class BroadcastSkipHook extends MethodHook {
    public BroadcastSkipHook(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public String getTargetClass() {
        return "com.android.server.am.BroadcastSkipPolicy";
    }

    @Override
    public String getTargetMethod() {
        return "shouldSkipMessage";
    }

    @Override
    public Object[] getTargetParam() {
        if (SystemChecker.isVivo(classLoader))
            return new Object[] { "com.android.server.am.BroadcastRecord", "com.android.server.am.BroadcastFilter", boolean.class, int.class, "com.android.server.am.IVivoBroadcastQueueModern" };
        return CakeReflection.findParameterTypesOrDefault(CakeReflection.findClassIfExists(getTargetClass(), classLoader), getTargetMethod(), "com.android.server.am.BroadcastRecord", "com.android.server.am.BroadcastFilter");
    }

    @Override
    public CakeHooker.Callback getTargetHook() {
        return new CakeHooker.Callback() {
            @Override
            public void call(CakeHooker.AfterHookCallback callback) {
                if (callback.result != null)
                    return;

                Object filter = callback.getArgs()[1];
                if (filter == null)
                    return;

                Object receiver = CakeReflection.getObjectField(filter, "receiverList");
                if (receiver == null)
                    return;

                Object app = CakeReflection.getObjectField(receiver, "app");
                if (app == null)
                    return;

                ProcessRecord processRecord = ProcessService.getProcessRecord(app);
                if (processRecord == null)
                    return;

                if (processRecord.isFrozen())
                    callback.result = "Skipping deliver [Cirno]: frozen process";
            }
        };
    }

    @Override
    public int getMinVersion() {
        return Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
    }
}
