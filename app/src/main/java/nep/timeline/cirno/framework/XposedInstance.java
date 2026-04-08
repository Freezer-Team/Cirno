package nep.timeline.cirno.framework;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Executable;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModule;

public class XposedInstance {
    private static XposedModule module;

    public static void setModule(XposedModule module) {
        XposedInstance.module = module;
    }

    public static boolean deoptimize(Executable executable) {
        if (module == null)
            return false;
        return module.deoptimize(executable);
    }

    public static void log(int priority, @Nullable String tag, @NonNull String msg) {
        if (module == null)
            return;
        module.log(priority, tag, msg);
    }

    public static void log(int priority, @Nullable String tag, @NonNull String msg, @Nullable Throwable tr) {
        if (module == null)
            return;
        module.log(priority, tag, msg, tr);
    }

    public static int getApiVersion() {
        if (module == null)
            return -1;
        return module.getApiVersion();
    }

    public static XposedInterface.HookBuilder hook(Executable executable) {
        if (executable == null)
            return null;
        return module.hook(executable);
    }
}
