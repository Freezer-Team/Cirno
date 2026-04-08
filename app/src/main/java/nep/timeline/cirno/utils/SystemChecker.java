package nep.timeline.cirno.utils;

import nep.timeline.cirno.reflect.CakeReflection;

public class SystemChecker {
    public static boolean isSamsung(ClassLoader classLoader)
    {
        return CakeReflection.findClassIfExists("com.android.server.am.FreecessController", classLoader) != null;
    }

    public static boolean isXiaomi(ClassLoader classLoader)
    {
        return CakeReflection.findClassIfExists("com.miui.server.greeze.GreezeManagerService", classLoader) != null;
    }

    public static boolean isOplus(ClassLoader classLoader)
    {
        return CakeReflection.findClassIfExists("com.android.server.am.OplusHansManager", classLoader) != null;
    }

    public static boolean isHuawei(ClassLoader classLoader)
    {
        return CakeReflection.findClassIfExists("com.huawei.turbozone.ITurboService", classLoader) != null;
    }

    public static boolean isVivo(ClassLoader classLoader)
    {
        return CakeReflection.findClassIfExists("com.android.server.am.IVivoBroadcastQueueModern", classLoader) != null;
    }

    public static boolean isNubia(ClassLoader classLoader) {
        return CakeReflection.findClassIfExists("cn.nubia.server.appmgmt.ApplicationControllerUtils", classLoader) != null;
    }
}
