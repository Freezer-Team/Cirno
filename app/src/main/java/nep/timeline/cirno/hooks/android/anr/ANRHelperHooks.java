package nep.timeline.cirno.hooks.android.anr;

import java.lang.reflect.Method;

import nep.timeline.cirno.reflect.CakeHooker;
import nep.timeline.cirno.reflect.CakeReflection;
import nep.timeline.cirno.utils.AnrHelper;

public class ANRHelperHooks {
    public final Integer findIndex(Class<?>[] parameterTypes, String clazz) {
        for (int i = 0; i < parameterTypes.length; i++)
            if (clazz.equals(parameterTypes[i].getName()))
                return i;
        return null;
    }

    public ANRHelperHooks(ClassLoader classLoader) {
        try {
            Class<?> targetClass = CakeReflection.findClassIfExists("com.android.server.am.AnrHelper", classLoader);

            if (targetClass == null)
                return;

            for (Method method : targetClass.getDeclaredMethods()) {
                if ((method.getName().equals("appNotResponding") || method.getName().equals("deferAppNotResponding")) && method.getReturnType().equals(void.class)) {
                    Integer index = findIndex(method.getParameterTypes(), "com.android.server.am.ProcessRecord");
                    if (index == null) { // Not found
                        Integer MIUIRecordIndex = findIndex(method.getParameterTypes(), "com.android.server.am.AnrHelper$AnrRecord");
                        if (MIUIRecordIndex != null) {
                            CakeHooker.hookBefore(method, callback -> {
                                Object anrRecord = callback.getArgs()[MIUIRecordIndex];
                                if (anrRecord == null)
                                    return;
                                Object app = CakeReflection.getObjectField(anrRecord, "mApp");
                                if (app == null)
                                    return;
                                if (AnrHelper.blockANR(app))
                                    callback.returnAndSkip(null);
                            });
                        }
                    } else {
                        CakeHooker.hookBefore(method, callback -> {
                            Object record = callback.getArgs()[index];
                            if (record == null)
                                return;
                            if (AnrHelper.blockANR(record))
                                callback.returnAndSkip(null);
                        });
                    }
                }
            }
        } catch (Throwable ignored) {

        }
    }
}
