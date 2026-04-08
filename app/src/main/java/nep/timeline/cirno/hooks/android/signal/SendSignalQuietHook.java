package nep.timeline.cirno.hooks.android.signal;

import android.os.Process;

import nep.timeline.cirno.framework.MethodHook;
import nep.timeline.cirno.reflect.CakeHooker;
import nep.timeline.cirno.services.ProcessService;
import nep.timeline.cirno.virtuals.ProcessRecord;

public class SendSignalQuietHook extends MethodHook {
    public SendSignalQuietHook(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public String getTargetClass() {
        return Process.class.getTypeName();
    }

    @Override
    public String getTargetMethod() {
        return "sendSignalQuiet";
    }

    @Override
    public Object[] getTargetParam() {
        return new Object[] { int.class, int.class };
    }

    @Override
    public CakeHooker.Callback getTargetHook() {
        return new CakeHooker.Callback() {
            @Override
            public void call(CakeHooker.BeforeHookCallback callback) {
                int pid = (int) callback.getArgs()[0];
                int signal = (int) callback.getArgs()[1];
                if (signal != Process.SIGNAL_KILL)
                    return;

                ProcessRecord processRecord = ProcessService.getProcessRecordByPid(pid);
                if (processRecord == null || processRecord.isDeathProcess())
                    return;

                ProcessService.removeProcessRecord(processRecord);
            }
        };
    }
}
