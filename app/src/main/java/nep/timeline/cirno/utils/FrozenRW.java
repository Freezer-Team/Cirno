package nep.timeline.cirno.utils;

import android.os.Process;

import java.nio.file.Files;
import java.nio.file.Paths;

public class FrozenRW {
    public static final String cgroupV2 = "/sys/fs/cgroup";
    private static final boolean cgroupV2SysAppIsolated;

    static {
        String path = "/sys/fs/cgroup/uid_1000/cgroup.freeze";
        cgroupV2SysAppIsolated = !Files.exists(Paths.get(path));
    }

    private static void writeFrozen(int uid, int pid, int frozenState) {
        if (!cgroupV2SysAppIsolated) {
            RWUtils.writeFrozen(cgroupV2 + "/uid_" + uid + "/pid_" + pid + "/cgroup.freeze", 1);
            return;
        }

        if (uid < Process.FIRST_APPLICATION_UID)
            RWUtils.writeFrozen(cgroupV2 + "/system/uid_" + uid + "/pid_" + pid + "/cgroup.freeze", frozenState);
        else
            RWUtils.writeFrozen(cgroupV2 + "/apps/uid_" + uid + "/pid_" + pid + "/cgroup.freeze", frozenState);
    }

    public static void frozen(int uid, int pid) {
        writeFrozen(uid, pid, 1);
    }

    public static void thaw(int uid, int pid) {
        writeFrozen(uid, pid, 0);
    }
}