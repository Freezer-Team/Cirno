package nep.timeline.cirno.framework;

import android.os.Build;

import java.util.ArrayList;
import java.util.Arrays;

import io.github.libxposed.api.XposedInterface;
import nep.timeline.cirno.log.Log;
import nep.timeline.cirno.reflect.CakeHooker;
import nep.timeline.cirno.reflect.CakeReflection;

public abstract class MethodHook {
    public final int ANY_VERSION = -1;
    public final ClassLoader classLoader;
    public XposedInterface.HookHandle unhooker;

    public MethodHook(ClassLoader classLoader) {
        this.classLoader = classLoader;
        try {
            startHook();
        } catch (Throwable throwable) {
            if (!isIgnoreError())
                Log.e(getTargetMethod(), throwable);
        }
    }

    public abstract String getTargetClass();

    public abstract String getTargetMethod();

    public abstract Object[] getTargetParam();

    public abstract CakeHooker.Callback getTargetHook();

    public int getMinVersion() {
        return ANY_VERSION;
    }

    public boolean isIgnoreError() {
        return false;
    }

    public void startHook() {
        int minVersion = getMinVersion();
        if (minVersion == ANY_VERSION || Build.VERSION.SDK_INT >= minVersion) {
            Object[] targetParam = getTargetParam();
            CakeHooker.Callback targetHook = getTargetHook();

            if (targetHook == null)
                return;

            String targetMethod = getTargetMethod();
            String targetClass = getTargetClass();

            ArrayList<Object> param = new ArrayList<>(Arrays.asList(targetParam));
            param.add(targetHook);
            if (targetMethod == null)
                unhooker = CakeReflection.findAndHookConstructor(targetClass, classLoader, param.toArray());
            else
                unhooker = CakeReflection.findAndHookMethod(targetClass, classLoader, targetMethod, param.toArray());
            Log.i(getTargetMethod() + " -> 成功Hook完毕!");
        }
    }

    public void unhook() {
        if (unhooker == null)
            return;

        unhooker.unhook();
        unhooker = null;
    }
}
