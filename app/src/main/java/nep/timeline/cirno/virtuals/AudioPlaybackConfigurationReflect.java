package nep.timeline.cirno.virtuals;

import android.media.AudioPlaybackConfiguration;

import nep.timeline.cirno.reflect.CakeReflection;

public class AudioPlaybackConfigurationReflect {
    private final AudioPlaybackConfiguration instance;

    public AudioPlaybackConfigurationReflect(AudioPlaybackConfiguration audioRecordingConfiguration) {
        this.instance = audioRecordingConfiguration;
    }

    public int getPlayerInterfaceId() {
        return (int) CakeReflection.callMethod(this.instance, "getPlayerInterfaceId");
    }

    public int getClientUid() {
        return (int) CakeReflection.callMethod(this.instance, "getClientUid");
    }

    public int getClientPid() {
        return (int) CakeReflection.callMethod(this.instance, "getClientPid");
    }

    public int getPlayerType() {
        return (int) CakeReflection.callMethod(this.instance, "getPlayerType");
    }
}
