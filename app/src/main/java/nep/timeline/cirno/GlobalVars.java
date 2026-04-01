package nep.timeline.cirno;

import nep.timeline.cirno.configs.settings.ApplicationSettings;
import nep.timeline.cirno.configs.settings.GlobalSettings;

public class GlobalVars {
    public static final String TAG = "Cirno";
    public static final String CONFIG = "Cirno";
    public final static String CONFIG_DIR = "/data/misc/" + GlobalVars.CONFIG;
    public final static String LOG_DIR = CONFIG_DIR + "/log";
    public static boolean isModuleActive = false;
    public static int XposedVersion = 0;
    public static ClassLoader classLoader;
    public static GlobalSettings globalSettings = null;
    public static ApplicationSettings applicationSettings = null;
}
