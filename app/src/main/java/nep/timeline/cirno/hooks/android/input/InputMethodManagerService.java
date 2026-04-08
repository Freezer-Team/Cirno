package nep.timeline.cirno.hooks.android.input;

import android.os.Build;
import android.view.inputmethod.InputMethodInfo;

import java.util.Map;

import nep.timeline.cirno.entity.AppRecord;
import nep.timeline.cirno.framework.MethodHook;
import nep.timeline.cirno.reflect.CakeHooker;
import nep.timeline.cirno.reflect.CakeReflection;
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
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.VANILLA_ICE_CREAM)
            return CakeReflection.findParameterTypesOrDefault(CakeReflection.findClassIfExists(getTargetClass(), classLoader), getTargetMethod(), String.class, int.class, int.class, int.class);
        return CakeReflection.findParameterTypesOrDefault(CakeReflection.findClassIfExists(getTargetClass(), classLoader), getTargetMethod(), String.class, int.class);
    }

    @Override
    public CakeHooker.Callback getTargetHook() {
        return new CakeHooker.Callback() {
            @Override
            public void call(CakeHooker.BeforeHookCallback callback) {
                String id = (String) callback.getArgs()[0];
                if (id == null)
                    return;

                int userId = (Build.VERSION.SDK_INT > Build.VERSION_CODES.VANILLA_ICE_CREAM) ? (int) callback.getArgs()[3] : ActivityManagerService.getCurrentOrTargetUserId();
                Object settings = (Build.VERSION.SDK_INT > Build.VERSION_CODES.UPSIDE_DOWN_CAKE) ? CakeReflection.callStaticMethod(CakeReflection.findClassIfExists("com.android.server.inputmethod.InputMethodSettingsRepository", classLoader), "get", userId) : CakeReflection.getObjectField(callback.getThisObject(), "mSettings");

                synchronized (InputMethodData.class) {
                    if (InputMethodData.instance == null) {
                        InputMethodData.instance = callback.getThisObject();
                        if (settings != null) {
                            Object map = CakeReflection.getObjectField(settings, "mMethodMap");
                            if (map != null) {
                                if (map.getClass().getTypeName().equals("com.android.server.inputmethod.InputMethodMap"))
                                    InputMethodData.inputMethods = (Map<String, InputMethodInfo>) CakeReflection.getObjectField(map, "mMap");
                                else
                                    InputMethodData.inputMethods = (Map<String, InputMethodInfo>) map;
                            }
                            else
                                InputMethodData.inputMethods = null;
                        }
                        else
                            InputMethodData.inputMethods = null;
                    }

                    Map<String, InputMethodInfo> inputMethodMap = InputMethodData.inputMethods;
                    if (inputMethodMap == null)
                        return;

                    InputMethodInfo inputMethodInfo = inputMethodMap.get(id);

                    if (inputMethodInfo != null && !inputMethodInfo.equals(InputMethodData.currentInputMethodInfo)) {
                        InputMethodData.currentInputMethodInfo = inputMethodInfo;
                        AppRecord appRecord = AppService.get(inputMethodInfo.getPackageName(), userId);
                        if (appRecord != InputMethodData.currentInputMethodApp) {
                            AppRecord oldApp = InputMethodData.currentInputMethodApp;
                            InputMethodData.currentInputMethodApp = appRecord;
                            if (appRecord != null)
                                FreezerService.thaw(appRecord);
                            if (oldApp != null)
                                FreezerHandler.sendFreezeMessage(oldApp, 3000);
                        }
                    }
                }
            }
        };
    }
}
