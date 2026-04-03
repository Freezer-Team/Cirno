package nep.timeline.cirno.hooks.android.input;

import android.os.Build;
import android.view.inputmethod.InputMethodInfo;

import java.lang.reflect.Method;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import nep.timeline.cirno.entity.AppRecord;
import nep.timeline.cirno.framework.AbstractMethodHook;
import nep.timeline.cirno.framework.MethodHook;
import nep.timeline.cirno.log.Log;
import nep.timeline.cirno.services.ActivityManagerService;
import nep.timeline.cirno.services.AppService;
import nep.timeline.cirno.services.FreezerService;
import nep.timeline.cirno.threads.FreezerHandler;
import nep.timeline.cirno.utils.InputMethodData;

public class InputMethodManagerService extends MethodHook {
    public InputMethodManagerService(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public String getTargetClass() {
        return "com.android.server.inputmethod.InputMethodManagerService";
    }

    @Override
    public String getTargetMethod() {
        return "setInputMethodLocked";
    }

    @Override
    public Object[] getTargetParam() {
        return new Object[0];
    }

    @Override
    public XC_MethodHook getTargetHook() {
        return new AbstractMethodHook() {
            @Override
            @SuppressWarnings("unchecked")
            protected void beforeMethod(MethodHookParam param) {
                try {
                    if (param.args.length < 1) {
                        return;
                    }

                    Object arg0 = param.args[0];
                    if (!(arg0 instanceof String)) {
                        return;
                    }

                    String id = (String) arg0;
                    if (id == null || id.isEmpty()) {
                        return;
                    }

                    int userId = ActivityManagerService.getCurrentOrTargetUserId();
                    
                    Object settings = null;
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        try {
                            settings = XposedHelpers.callStaticMethod(
                                XposedHelpers.findClassIfExists(
                                    "com.android.server.inputmethod.InputMethodSettingsRepository", 
                                    classLoader), 
                                "get", userId);
                        } catch (Exception e) {
                            Log.w("获取 InputMethodSettingsRepository 失败");
                        }
                    } else {
                        try {
                            settings = XposedHelpers.getObjectField(param.thisObject, "mSettings");
                        } catch (Exception e) {
                            Log.w("获取 mSettings 失败");
                        }
                    }

                    synchronized (InputMethodData.class) {
                        if (InputMethodData.instance == null) {
                            InputMethodData.instance = param.thisObject;
                            if (settings != null) {
                                try {
                                    Object map = XposedHelpers.getObjectField(settings, "mMethodMap");
                                    if (map != null) {
                                        if (map.getClass().getTypeName().equals("com.android.server.inputmethod.InputMethodMap")) {
                                            InputMethodData.inputMethods = (Map<String, InputMethodInfo>) 
                                                XposedHelpers.getObjectField(map, "mMap");
                                        } else {
                                            InputMethodData.inputMethods = (Map<String, InputMethodInfo>) map;
                                        }
                                    } else {
                                        InputMethodData.inputMethods = null;
                                    }
                                } catch (Exception e) {
                                    InputMethodData.inputMethods = null;
                                }
                            } else {
                                InputMethodData.inputMethods = null;
                            }
                        }

                        Map<String, InputMethodInfo> inputMethodMap = InputMethodData.inputMethods;
                        if (inputMethodMap == null) {
                            return;
                        }

                        InputMethodInfo inputMethodInfo = inputMethodMap.get(id);

                        if (inputMethodInfo != null && !inputMethodInfo.equals(InputMethodData.currentInputMethodInfo)) {
                            InputMethodData.currentInputMethodInfo = inputMethodInfo;
                            AppRecord appRecord = AppService.get(inputMethodInfo.getPackageName(), userId);
                            if (appRecord != InputMethodData.currentInputMethodApp) {
                                AppRecord oldApp = InputMethodData.currentInputMethodApp;
                                InputMethodData.currentInputMethodApp = appRecord;
                                if (appRecord != null) {
                                    FreezerService.thaw(appRecord);
                                }
                                if (oldApp != null) {
                                    FreezerHandler.sendFreezeMessage(oldApp, 3000);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("InputMethodManagerService 处理失败", e);
                }
            }
        };
    }

    @Override
    public void startHook() {
        try {
            Class<?> targetClass = XposedHelpers.findClass(getTargetClass(), classLoader);
            
            boolean hooked = false;
            for (Method method : targetClass.getDeclaredMethods()) {
                if (method.getName().equals(getTargetMethod())) {
                    try {
                        XposedBridge.hookMethod(method, getTargetHook());
                        Log.i(method.getName() + " [参数: " + method.getParameterCount() + "] -> 成功Hook完毕!");
                        hooked = true;
                    } catch (Exception e) {
                        Log.w("Hook " + method.getName() + " [参数: " + method.getParameterCount() + "] 失败");
                    }
                }
            }
            
            if (!hooked) {
                Log.w("未能 Hook 任何 setInputMethodLocked 方法");
            }
        } catch (Throwable e) {
            Log.e(getTargetMethod() + " Hook 失败", e);
        }
    }

    @Override
    public boolean isIgnoreError() {
        return true;
    }
}
