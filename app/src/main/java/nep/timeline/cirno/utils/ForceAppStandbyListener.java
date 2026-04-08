package nep.timeline.cirno.utils;

import lombok.Setter;
import nep.timeline.cirno.entity.AppRecord;
import nep.timeline.cirno.log.Log;
import nep.timeline.cirno.reflect.CakeReflection;

public class ForceAppStandbyListener {
    @Setter
    private static Object instance;

    public static void removeAlarmsForUid(AppRecord appRecord) {
        if (instance == null)
            return;

        CakeReflection.callMethod(instance, "removeAlarmsForUid", appRecord.getUid());
        Log.d(appRecord.getPackageNameWithUser() + " 移除Alarms");
    }
}