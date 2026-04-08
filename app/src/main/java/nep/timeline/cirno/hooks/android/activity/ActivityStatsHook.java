package nep.timeline.cirno.hooks.android.activity;

import android.app.usage.UsageEvents;
import android.os.IBinder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nep.timeline.cirno.reflect.CakeHooker;
import nep.timeline.cirno.virtuals.ActivityRecord;
import nep.timeline.cirno.entity.AppRecord;
import nep.timeline.cirno.framework.MethodHook;
import nep.timeline.cirno.log.Log;
import nep.timeline.cirno.services.FreezerService;
import nep.timeline.cirno.threads.FreezerHandler;

public class ActivityStatsHook extends MethodHook {
    private final Map<IBinder, ActivityRecord> activityRecords = new ConcurrentHashMap<>();
    private final List<Integer> events = List.of(UsageEvents.Event.ACTIVITY_RESUMED, UsageEvents.Event.ACTIVITY_PAUSED, UsageEvents.Event.ACTIVITY_STOPPED, UsageEvents.Event.ACTIVITY_STOPPED + 1);

    public ActivityStatsHook(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public String getTargetClass() {
        return "com.android.server.wm.ActivityTaskManagerService";
    }

    @Override
    public String getTargetMethod() {
        return "updateActivityUsageStats";
    }

    @Override
    public Object[] getTargetParam() {
        return new Object[] { "com.android.server.wm.ActivityRecord", int.class };
    }

    @Override
    public CakeHooker.Callback getTargetHook() {
        return new CakeHooker.Callback() {
            @Override
            public void call(CakeHooker.BeforeHookCallback callback) {
                int event = (int) callback.getArgs()[1];

                if (!events.contains(event))
                    return;

                Object activityObject = callback.getArgs()[0];
                if (activityObject == null)
                    return;

                ActivityRecord record = new ActivityRecord(activityObject);

                ActivityRecord activityRecord = (event == UsageEvents.Event.ACTIVITY_RESUMED || event == UsageEvents.Event.ACTIVITY_PAUSED) ? activityRecords.computeIfAbsent(record.getToken(), k -> record) : activityRecords.remove(record.getToken());

                if (activityRecord == null)
                    return;

                AppRecord appRecord = activityRecord.toAppRecord();

                if (appRecord == null)
                    return;

                if (event == UsageEvents.Event.ACTIVITY_RESUMED)
                    appRecord.getAppState().getActivities().add(activityRecord.getToken());
                else
                    appRecord.getAppState().getActivities().remove(activityRecord.getToken());

                if (appRecord.getAppState().getActivities().isEmpty()) {
                    if (appRecord.getAppState().setVisible(false)) {
                        Log.d(appRecord.getPackageNameWithUser() + " 进入后台");
                        FreezerHandler.sendFreezeMessage(appRecord, 3000);
                    }
                } else if (appRecord.getAppState().setVisible(true)) {
                    Log.d(appRecord.getPackageNameWithUser() + " 进入前台");
                    FreezerService.thaw(appRecord);
                }
            }
        };
    }
}
