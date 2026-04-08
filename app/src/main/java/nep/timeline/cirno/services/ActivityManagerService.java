package nep.timeline.cirno.services;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import lombok.Setter;
import nep.timeline.cirno.reflect.CakeReflection;

public class ActivityManagerService {
    @Setter
    public static volatile Object instance;

    public static Context getContext() {
        return (Context) CakeReflection.getObjectField(instance, "mContext");
    }

    public static ApplicationInfo getApplicationInfo(String packageName, int userId) {
        try {
            Context context = getContext();
            if (context == null)
                return null;
            PackageManager packageManager = context.getPackageManager();
            if (packageManager == null)
                return null;
            return (ApplicationInfo) CakeReflection.callMethod(packageManager, "getApplicationInfoAsUser", packageName, PackageManager.GET_META_DATA | PackageManager.GET_SIGNING_CERTIFICATES, userId);
        } catch (Throwable ignored) {

        }
        return null;
    }

    public static int getCurrentOrTargetUserId() {
        return (int) CakeReflection.callMethod(CakeReflection.getObjectField(instance, "mUserController"), "getCurrentOrTargetUserId");
    }

    public static String[] getPackagesForUid(int uid) {
        Context context = getContext();

        if (context == null)
            return null;

        PackageManager packageManager = context.getPackageManager();
        return packageManager.getPackagesForUid(uid);
    }

    public static Object getPidsSelfLocked() {
        return CakeReflection.getObjectField(instance, "mPidsSelfLocked");
    }
}
