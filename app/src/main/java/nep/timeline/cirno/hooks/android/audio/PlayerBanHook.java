package nep.timeline.cirno.hooks.android.audio;

import android.media.AudioPlaybackConfiguration;

import java.util.List;

import nep.timeline.cirno.entity.AppRecord;
import nep.timeline.cirno.framework.MethodHook;
import nep.timeline.cirno.handlers.AudioHandler;
import nep.timeline.cirno.reflect.CakeHooker;
import nep.timeline.cirno.services.AppService;
import nep.timeline.cirno.threads.Handlers;
import nep.timeline.cirno.virtuals.AudioPlaybackConfigurationReflect;

public class PlayerBanHook extends MethodHook {
    public PlayerBanHook(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public String getTargetClass() {
        return "com.android.server.audio.PlaybackActivityMonitor";
    }

    @Override
    public String getTargetMethod() {
        return "checkBanPlayer";
    }

    @Override
    public Object[] getTargetParam() {
        return new Object[] { AudioPlaybackConfiguration.class, int.class };
    }

    @Override
    public CakeHooker.Callback getTargetHook() {
        return new CakeHooker.Callback() {
            @Override
            public void call(CakeHooker.AfterHookCallback callback) {
                if ((boolean) callback.result) {
                    Object configuration = callback.getArgs()[0];
                    if (configuration == null)
                        return;

                    AudioPlaybackConfigurationReflect reflect = new AudioPlaybackConfigurationReflect((AudioPlaybackConfiguration) configuration);

                    Handlers.audio.post(() -> {
                        List<AppRecord> appRecords = AppService.getByUid(reflect.getClientUid());
                        if (appRecords == null || appRecords.isEmpty())
                            return;

                        for (AppRecord appRecord : appRecords) {
                            if (appRecord == null)
                                continue;

                            int interfaceId = reflect.getPlayerInterfaceId();
                            AudioHandler.call(appRecord, AudioHandler.PLAYER_STATE_PAUSED, interfaceId);
                        }
                    });
                }
            }
        };
    }
}
