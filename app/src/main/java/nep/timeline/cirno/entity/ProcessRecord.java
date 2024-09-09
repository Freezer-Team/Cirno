package nep.timeline.cirno.entity;

import android.content.pm.ApplicationInfo;

import de.robv.android.xposed.XposedHelpers;
import lombok.Getter;
import nep.timeline.cirno.services.AppService;

@Getter
public class ProcessRecord {
    private final Object instance;
    private final int userId;
    private final int runningUid;
    private final ApplicationInfo applicationInfo;
    private final int uid;
    private final String packageName;
    private final String processName;
    private AppRecord appRecord;

    public ProcessRecord(Object instance) {
        this.instance = instance;
        this.userId = XposedHelpers.getIntField(instance, "userId");
        this.runningUid = XposedHelpers.getIntField(instance, "uid");
        this.applicationInfo = (ApplicationInfo) XposedHelpers.getObjectField(instance, "info");
        this.uid = applicationInfo.uid;
        this.packageName = applicationInfo.packageName;
        this.processName = (String) XposedHelpers.getObjectField(instance, "processName");
        this.appRecord = AppService.get(packageName, userId);
    }

    public AppRecord getAppRecord() {
        if (appRecord == null)
            appRecord = AppService.get(packageName, userId);
        return appRecord;
    }
}