package nep.timeline.cirno.hooks.android.optimizer;

import android.os.Build;

import nep.timeline.cirno.framework.MethodHook;
import nep.timeline.cirno.reflect.CakeHooker;
import nep.timeline.cirno.services.CachedAppOptimizer;

public class CacheUseFreezerHook extends MethodHook {
    public CacheUseFreezerHook(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public String getTargetClass() {
        return "com.android.server.am.CachedAppOptimizer";
    }

    @Override
    public String getTargetMethod() {
        return "useFreezer";
    }

    @Override
    public Object[] getTargetParam() {
        return new Object[0];
    }

    @Override
    public CakeHooker.Callback getTargetHook() {
        return (CakeHooker.ReplacementCallback) chain -> {
            if (CachedAppOptimizer.getInstance() == null)
                synchronized (CachedAppOptimizer.class) {
                    CachedAppOptimizer.setInstance(chain.getThisObject());
                }

            return false;
        };
    }

    @Override
    public int getMinVersion() {
        return Build.VERSION_CODES.R;
    }
}