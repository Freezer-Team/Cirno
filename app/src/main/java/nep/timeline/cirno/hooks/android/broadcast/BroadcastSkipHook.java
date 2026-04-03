package nep.timeline.cirno.hooks.android.broadcast;

import android.os.Build;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import nep.timeline.cirno.framework.AbstractMethodHook;
import nep.timeline.cirno.framework.MethodHook;
import nep.timeline.cirno.log.Log;
import nep.timeline.cirno.services.ProcessService;
import nep.timeline.cirno.virtuals.ProcessRecord;
import nep.timeline.cirno.utils.SystemChecker;

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
        return new Object[] { "com.android.server.am.BroadcastRecord", "com.android.server.am.BroadcastFilter", boolean.class };
    }

    @Override
    public XC_MethodHook getTargetHook() {
        return new AbstractMethodHook() {
            @Override
            protected void afterMethod(MethodHookParam param) {
                try {
                    // ✅ 安全地获取返回值
                    Object result = param.getResult();
                    if (result != null) {
                        return;
                    }

                    Object filter = param.args[1];
                    if (filter == null) {
                        return;
                    }

                    Object receiver = XposedHelpers.getObjectField(filter, "receiverList");
                    if (receiver == null) {
                        return;
                    }

                    Object app = XposedHelpers.getObjectField(receiver, "app");
                    if (app == null) {
                        return;
                    }

                    ProcessRecord processRecord = ProcessService.getProcessRecord(app);
                    if (processRecord == null) {
                        return;
                    }

                    if (processRecord.isFrozen()) {
                        param.setResult("Skipping deliver [Cirno]: frozen process");
                    }
                } catch (Exception e) {
                    Log.e("BroadcastSkipHook 处理失败", e);
                }
            }
        };
    }

    @Override
    public int getMinVersion() {
        return Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
    }
}
