package nep.timeline.cirno.services;

import android.os.Build;
import android.os.RemoteException;

import lombok.Getter;
import lombok.Setter;
import nep.timeline.cirno.log.Log;
import nep.timeline.cirno.reflect.CakeReflection;

public class CachedAppOptimizer {
    @Getter
    @Setter
    public static volatile Object instance;
    public static final int UID_FROZEN_STATE_FROZEN = 1;
    public static final int UID_FROZEN_STATE_UNFROZEN = 2;

    public static void reportOneUidFrozenStateChanged(int uid, boolean frozenState) {
        final int[] uids = new int[1];
        final int[] frozenStates = new int[1];

        uids[0] = uid;
        frozenStates[0] = frozenState ? UID_FROZEN_STATE_FROZEN : UID_FROZEN_STATE_UNFROZEN;

        reportUidFrozenStateChanged(uids, frozenStates);
    }

    public static void reportUidFrozenStateChanged(int[] uids, int[] frozenStates) {
        if (instance == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
            return;

        Object mUidFrozenStateChangedCallbackList = CakeReflection.getObjectField(CakeReflection.getObjectField(instance, "mAm"), "mUidFrozenStateChangedCallbackList");
        synchronized (mUidFrozenStateChangedCallbackList) {
            final int n = (int) CakeReflection.callMethod(mUidFrozenStateChangedCallbackList, "beginBroadcast");
            for (int i = 0; i < n; i++) {
                try {
                    CakeReflection.callMethod(CakeReflection.callMethod(mUidFrozenStateChangedCallbackList, "getBroadcastItem", i), "onUidFrozenStateChanged", uids, frozenStates);
                } catch (CakeReflection.InvocationTargetError e) {
                    /*
                     * The process at the other end has died or otherwise gone away.
                     * According to spec, RemoteCallbackList will take care of unregistering any
                     * object associated with that process - we are safe to ignore the exception
                     * here.
                     */
                    if (!(e.getCause() instanceof RemoteException)) {
                        Log.e("reportUidFrozenStateChanged", e);
                    }
                }
            }
            CakeReflection.callMethod(mUidFrozenStateChangedCallbackList, "finishBroadcast");
        }
    }
}
