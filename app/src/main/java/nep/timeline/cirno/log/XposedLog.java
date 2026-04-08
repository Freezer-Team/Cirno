package nep.timeline.cirno.log;

import android.util.Log;

import nep.timeline.cirno.GlobalVars;
import nep.timeline.cirno.framework.XposedInstance;

public class XposedLog {
    public static void e(String msg, Throwable throwable) {
        XposedInstance.log(Log.ERROR, GlobalVars.TAG, msg, throwable);
    }

    public static void e(String msg) {
        XposedInstance.log(Log.ERROR, GlobalVars.TAG, msg);
    }

    public static void i(String msg) {
        XposedInstance.log(Log.INFO, GlobalVars.TAG, msg);
    }

    public static void w(String msg, Throwable throwable) {
        XposedInstance.log(Log.WARN, GlobalVars.TAG, msg, throwable);
    }

    public static void w(String msg) {
        XposedInstance.log(Log.WARN, GlobalVars.TAG, msg);
    }

    public static void d(String msg, Throwable throwable) {
        XposedInstance.log(Log.DEBUG, GlobalVars.TAG, msg, throwable);
    }

    public static void d(String msg) {
        XposedInstance.log(Log.DEBUG, GlobalVars.TAG, msg);
    }
}
