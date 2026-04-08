package nep.timeline.cirno.netlink;

import java.io.FileDescriptor;

import nep.timeline.cirno.reflect.CakeReflection;

public class IoUtils {
    public static void closeQuietly(ClassLoader classLoader, FileDescriptor fileDescriptor) {
        CakeReflection.callStaticMethod(CakeReflection.findClass("libcore.io.IoUtils", classLoader), "closeQuietly", fileDescriptor);
    }

    public static void setsockoptInt(ClassLoader classLoader, FileDescriptor fileDescriptor, int level, int option, int value) {
        Class<?> libcore = CakeReflection.findClass("libcore.io.Libcore", classLoader);
        Object os = CakeReflection.getStaticObjectField(libcore, "os");
        CakeReflection.callMethod(os, "setsockoptInt", fileDescriptor, level, option, value);
    }
}
