package nep.timeline.cirno.entity;

import android.content.pm.ApplicationInfo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.Data;
import nep.timeline.cirno.CommonConstants;
import nep.timeline.cirno.configs.checkers.AppConfigs;
import nep.timeline.cirno.utils.InputMethodData;
import nep.timeline.cirno.utils.PKGUtils;
import nep.timeline.cirno.virtuals.ProcessRecord;

@Data
public class AppRecord {
    private final String packageName;
    private final int userId;
    private final int uid;
    private final ApplicationInfo applicationInfo;
    private AppState appState;
    private boolean frozen;
    private final List<ProcessRecord> processRecords = new CopyOnWriteArrayList<>();

    public AppRecord(ApplicationInfo applicationInfo) {
        this.packageName = applicationInfo.packageName;
        this.userId = PKGUtils.getUserId(applicationInfo.uid);
        this.uid = applicationInfo.uid;
        this.applicationInfo = applicationInfo;
        this.appState = new AppState(this);
    }

    public boolean isSystem() {
        return packageName == null || equals(InputMethodData.currentInputMethodApp) || PKGUtils.isSystemApp(applicationInfo) || AppConfigs.isWhiteApp(packageName, userId) || CommonConstants.isWhitelistApps(packageName);
    }

    public String getPackageNameWithUser() {
        if (userId == 0)
            return packageName;
        return packageName + ":" + userId;
    }

    public void reset() {
        this.frozen = false;
        this.appState = new AppState(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj instanceof AppRecord appRecord)
            return getUserId() == appRecord.getUserId() && getPackageName().equals(appRecord.getPackageName());
        return false;
    }

    @Override
    public int hashCode() {
        int userId = getUserId();
        String packageName = getPackageName();
        return ((Integer.hashCode(userId) + 59) * 59) + (packageName == null ? 43 : packageName.hashCode());
    }
}
