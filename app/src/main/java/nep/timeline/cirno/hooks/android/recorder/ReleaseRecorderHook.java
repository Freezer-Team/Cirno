package nep.timeline.cirno.hooks.android.recorder;

import android.os.Binder;

import java.util.List;

import nep.timeline.cirno.entity.AppRecord;
import nep.timeline.cirno.framework.MethodHook;
import nep.timeline.cirno.handlers.RecordingHandler;
import nep.timeline.cirno.reflect.CakeHooker;
import nep.timeline.cirno.services.AppService;

public class ReleaseRecorderHook extends MethodHook {
    public ReleaseRecorderHook(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public String getTargetClass() {
        return "com.android.server.audio.RecordingActivityMonitor";
    }

    @Override
    public String getTargetMethod() {
        return "releaseRecorder";
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
                int uid = Binder.getCallingUid();
                int riid = (int) callback.getArgs()[0];

                if (riid == RecordingHandler.RECORD_RIID_INVALID)
                    return;

                List<AppRecord> appRecords = AppService.getByUid(uid);

                if (appRecords == null || appRecords.isEmpty())
                    return;

                for (AppRecord appRecord : appRecords) {
                    if (appRecord == null)
                        continue;

                    RecordingHandler.call(appRecord, RecordingHandler.RECORDER_STATE_STOPPED, riid);
                }
            }
        };
    }
}
