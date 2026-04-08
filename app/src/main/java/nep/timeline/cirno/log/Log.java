package nep.timeline.cirno.log;

import android.os.Build;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import nep.timeline.cirno.GlobalVars;
import nep.timeline.cirno.threads.Handlers;
import nep.timeline.cirno.utils.RWUtils;

public class Log {
    private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.JAPAN);
    private final static File currentLog = new File(GlobalVars.LOG_DIR, "current.log");

    static {
        i("设备Android SDK: " + Build.VERSION.SDK_INT);
    }

    public static void d(String msg) {
        execute("调试", msg);
    }

    public static void d(String msg, Throwable throwable) {
        d(msg + " 失败: " + throwable.getMessage());
    }

    public static void i(String msg) {
        execute("信息", msg);
    }

    public static void w(String msg) {
        execute("警告", msg);
    }

    public static void w(String msg, Throwable throwable) {
        w(msg + " 失败: " + throwable.getMessage());
    }

    public static void e(String msg) {
        execute("错误", msg);
    }

    public static void e(String msg, Throwable throwable) {
        e(msg + " 失败: " + throwable.getMessage());
    }

    public static void execute(String level, String msg) {
        Handlers.log.post(() -> fileLog(simpleDateFormat.format(new Date()) + " " + level.toUpperCase() + " -> " + msg));
    }

    public static void fileLog(String msg) {
        try {
            RWUtils.writeStringToFile(currentLog, msg, true);
        } catch (IOException e) {
            Log.e("Log write failed! msg: " + msg, e);
        }
    }
}
