package nep.timeline.cirno.services;

import android.os.Build;

import java.util.List;

import nep.timeline.cirno.entity.AppRecord;
import nep.timeline.cirno.threads.Handlers;
import nep.timeline.cirno.utils.ForceAppStandbyListener;
import nep.timeline.cirno.virtuals.ProcessRecord;
import nep.timeline.cirno.log.Log;
import nep.timeline.cirno.threads.FreezerHandler;
import nep.timeline.cirno.utils.FrozenRW;

public class FreezerService {
    public static void freezer(AppRecord appRecord) {
        if (appRecord.isFrozen() || appRecord.isSystem() || appRecord.getAppState().isVisible() || appRecord.getAppState().isLocation() || appRecord.getAppState().isAudio() || appRecord.getAppState().isRecording() || appRecord.getAppState().isVpn())
            return;

        for (ProcessRecord processRecord : appRecord.getProcessRecords()) {
            if (processRecord.isDeathProcess() || processRecord.isFrozen())
                continue;

            FrozenRW.frozen(processRecord.getRunningUid(), processRecord.getPid());
            processRecord.setFrozen(true);
        }

        Handlers.alarms.post(() -> ForceAppStandbyListener.removeAlarmsForUid(appRecord));
        Handlers.network.post(() -> {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                CachedAppOptimizer.reportOneUidFrozenStateChanged(appRecord.getUid(), true);
                return;
            }
            NetworkManagementService.socketDestroy(appRecord);
        });
        appRecord.setFrozen(true);
    }

    public static void thaw(AppRecord appRecord) {
        FreezerHandler.removeAppMessage(appRecord);

        if (!appRecord.isFrozen())
            return;

        for (ProcessRecord processRecord : appRecord.getProcessRecords()) {
            if (processRecord.isDeathProcess() || !processRecord.isFrozen())
                continue;

            FrozenRW.thaw(processRecord.getRunningUid(), processRecord.getPid());
            processRecord.setFrozen(false);
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.VANILLA_ICE_CREAM)
            Handlers.network.post(() -> CachedAppOptimizer.reportOneUidFrozenStateChanged(appRecord.getUid(), false));
        appRecord.setFrozen(false);
    }

    public static void temporaryUnfreezeIfNeed(int uid, String reason, long interval) {
        List<AppRecord> appRecords = AppService.getByUid(uid);

        if (appRecords == null || appRecords.isEmpty())
            return;

        for (AppRecord appRecord : appRecords) {
            if (appRecord == null)
                continue;

            temporaryUnfreezeIfNeed(appRecord, reason, interval);
        }
    }

    public static void temporaryUnfreezeIfNeed(String packageName, int userId, String reason, long interval) {
        temporaryUnfreezeIfNeed(AppService.get(packageName, userId), reason, interval);
    }

    public static void temporaryUnfreezeIfNeed(AppRecord appRecord, String reason, long interval) {
        if (appRecord == null || appRecord.isSystem())
            return;

        if (appRecord.isFrozen())
            Log.i(appRecord.getPackageNameWithUser() + " " + reason);

        thaw(appRecord);
        FreezerHandler.sendFreezeMessageIgnoreMessages(appRecord, interval);
    }
}
