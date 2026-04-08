package nep.timeline.cirno.hooks.android.vpn;

import android.net.NetworkInfo;

import nep.timeline.cirno.entity.AppRecord;
import nep.timeline.cirno.framework.MethodHook;
import nep.timeline.cirno.log.Log;
import nep.timeline.cirno.reflect.CakeHooker;
import nep.timeline.cirno.reflect.CakeReflection;
import nep.timeline.cirno.services.AppService;
import nep.timeline.cirno.services.FreezerService;
import nep.timeline.cirno.threads.FreezerHandler;
import nep.timeline.cirno.utils.PKGUtils;

public class VpnStateHook extends MethodHook {
    public VpnStateHook(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public String getTargetClass() {
        return "com.android.server.connectivity.Vpn";
    }

    @Override
    public String getTargetMethod() {
        return "updateState";
    }

    @Override
    public Object[] getTargetParam() {
        return new Object[] { NetworkInfo.DetailedState.class, String.class };
    }

    @Override
    public CakeHooker.Callback getTargetHook() {
        return new CakeHooker.Callback() {
            @Override
            public void call(CakeHooker.BeforeHookCallback callback) {
                String state = callback.getArgs()[0].toString();
                int uid = CakeReflection.getIntField(callback.getThisObject(), "mOwnerUID");
                String packageName = (String) CakeReflection.getObjectField(callback.getThisObject(), "mPackage");

                AppRecord appRecord = AppService.get(packageName, PKGUtils.getUserId(uid));
                if (appRecord != null) {
                    if (appRecord.isSystem())
                        return;

                    if ("CONNECTED".equals(state) && appRecord.getAppState().setVpn(true)) {
                        Log.d(appRecord.getPackageNameWithUser() + " 连接至VPN");
                        FreezerService.thaw(appRecord);
                    }

                    if (("DISCONNECTED".equals(state) || "FAILED".equals(state)) && appRecord.getAppState().setVpn(false)) {
                        Log.d(appRecord.getPackageNameWithUser() + " 从VPN断开连接");
                        FreezerHandler.sendFreezeMessageIgnoreMessages(appRecord, 3000);
                    }
                }
            }
        };
    }
}
