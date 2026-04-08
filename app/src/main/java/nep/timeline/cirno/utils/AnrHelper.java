package nep.timeline.cirno.utils;

import nep.timeline.cirno.entity.AppRecord;
import nep.timeline.cirno.services.ProcessService;
import nep.timeline.cirno.virtuals.ProcessRecord;

public class AnrHelper {
    public static boolean blockANR(Object app) {
        if (app == null)
            return false;
        ProcessRecord processRecord = ProcessService.getProcessRecord(app);
        if (processRecord == null)
            return false;
        AppRecord appRecord = processRecord.getAppRecord();
        if (appRecord == null)
            return false;
        return !appRecord.isSystem();
    }
}
