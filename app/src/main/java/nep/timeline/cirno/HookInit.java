package nep.timeline.cirno;

import androidx.annotation.NonNull;

import java.io.File;

import io.github.libxposed.api.XposedModule;
import nep.timeline.cirno.framework.XposedInstance;
import nep.timeline.cirno.log.XposedLog;
import nep.timeline.cirno.master.AndroidHooks;
import nep.timeline.cirno.reflect.CakeHooker;

public class HookInit extends XposedModule {
    @Override
    public void onModuleLoaded(@NonNull ModuleLoadedParam param) {
        XposedInstance.setModule(this);
        CakeHooker.setXposedModule(this);
    }

    @Override
    public void onSystemServerStarting(@NonNull SystemServerStartingParam param) {
        ClassLoader classLoader = param.getClassLoader();
        CakeHooker.setHostClassLoader(classLoader);

        try {
            File source = new File(GlobalVars.LOG_DIR, "current.log");
            File dest = new File(GlobalVars.LOG_DIR, "last.log");
            boolean delete = dest.delete();
            boolean renameTo = source.renameTo(dest);
            AndroidHooks.start(classLoader);
        } catch (Throwable throwable) {
            XposedLog.e("Hook failed:", throwable);
        }
    }
}