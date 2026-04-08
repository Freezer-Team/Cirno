package nep.timeline.cirno.hooks.android.audio;

import android.media.AudioPlaybackConfiguration;

import java.util.List;

import nep.timeline.cirno.entity.AppRecord;
import nep.timeline.cirno.framework.MethodHook;
import nep.timeline.cirno.handlers.AudioHandler;
import nep.timeline.cirno.reflect.CakeHooker;
import nep.timeline.cirno.reflect.CakeReflection;
import nep.timeline.cirno.services.AppService;
import nep.timeline.cirno.threads.Handlers;
import nep.timeline.cirno.virtuals.AudioPlaybackConfigurationReflect;

public class AudioStateHook extends MethodHook {
    public AudioStateHook(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public String getTargetClass() {
        return AudioPlaybackConfiguration.class.getTypeName();
    }

    @Override
    public String getTargetMethod() {
        return "handleStateEvent";
    }

    @Override
    public Object[] getTargetParam() {
        return CakeReflection.findParameterTypesOrDefault(AudioPlaybackConfiguration.class, getTargetMethod(), int.class);
    }

    @Override
    public CakeHooker.Callback getTargetHook() {
        return new CakeHooker.Callback() {
            @Override
            public void call(CakeHooker.AfterHookCallback callback) {
                if (!((boolean) callback.result))
                    return;

                int event = (int) callback.getArgs()[0];
                if (AudioHandler.LISTEN_EVENT.contains(event)) {
                    AudioPlaybackConfigurationReflect reflect = new AudioPlaybackConfigurationReflect((AudioPlaybackConfiguration) callback.getThisObject());

                    Handlers.audio.post(() -> {
                        List<AppRecord> appRecords = AppService.getByUid(reflect.getClientUid());
                        if (appRecords == null)
                            return;

                        int interfaceId = reflect.getPlayerInterfaceId();
                        for (AppRecord appRecord : appRecords) {
                            if (appRecord == null)
                                continue;

                            AudioHandler.call(appRecord, event, interfaceId);
                        }
                    });
                }
            }
        };
    }
}