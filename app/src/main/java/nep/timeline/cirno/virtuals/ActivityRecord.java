package nep.timeline.cirno.virtuals;

import android.os.IBinder;

import lombok.Getter;
import nep.timeline.cirno.entity.AppRecord;
import nep.timeline.cirno.reflect.CakeReflection;
import nep.timeline.cirno.services.AppService;

@Getter
public class ActivityRecord {
    private final Object instance;
    private final String packageName;
    private final int userId;
    private final IBinder token;

    public ActivityRecord(Object instance) {
        this.instance = instance;
        this.packageName = (String) CakeReflection.getObjectField(instance, "packageName");
        this.userId = CakeReflection.getIntField(instance, "mUserId");
        this.token = (IBinder) CakeReflection.getObjectField(instance, "token");
    }

    public AppRecord toAppRecord() {
        return AppService.get(packageName, userId);
    }
}
