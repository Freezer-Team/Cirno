package nep.timeline.cirno.hooks.android.optimizer;

import android.os.Build;

import nep.timeline.cirno.framework.MethodHook;
import nep.timeline.cirno.reflect.CakeHooker;
import nep.timeline.cirno.reflect.CakeReflection;
import nep.timeline.cirno.services.CachedAppOptimizer;

public class CacheEnableFreezerHook extends MethodHook {
    public CacheEnableFreezerHook(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public String getTargetClass() {
        return "com.android.server.am.CachedAppOptimizer";
    }

    @Override
    public String getTargetMethod() {
        return "enableFreezer";
    }

    @Override
    public Object[] getTargetParam() {
        return new Object[]{ boolean.class };
    }

    @Override
    public CakeHooker.Callback getTargetHook() {
        return (CakeHooker.ReplacementCallback) chain -> {
            synchronized (CachedAppOptimizer.class) {
                Object object = chain.getThisObject();
                if (CachedAppOptimizer.getInstance() == null)
                    CachedAppOptimizer.setInstance(object);
                if (CakeReflection.getBooleanField(object, "mUseFreezer"))
                    CakeReflection.setObjectField(object, "mUseFreezer", false);
            }
            return false;
        };
    }

    @Override
    public int getMinVersion() {
        return Build.VERSION_CODES.S;
    }
}