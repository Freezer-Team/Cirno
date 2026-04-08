package nep.timeline.cirno.entity;

import lombok.Getter;
import nep.timeline.cirno.reflect.CakeReflection;

@Getter
public class PendingIntentKey {
    private final Object instance;
    private final String packageName;
    private final int userId;

    public PendingIntentKey(Object key) {
        this.instance = key;
        this.packageName = (String) CakeReflection.getObjectField(key, "packageName");
        this.userId = CakeReflection.getIntField(key, "userId");
    }
}