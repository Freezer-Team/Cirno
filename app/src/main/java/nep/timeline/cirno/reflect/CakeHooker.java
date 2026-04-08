package nep.timeline.cirno.reflect;

import androidx.annotation.NonNull;

import java.lang.reflect.Executable;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModule;
import lombok.Getter;
import lombok.Setter;

public class CakeHooker {
    @Getter
    @Setter
    private static XposedModule xposedModule;
    @Getter
    @Setter
    private static ClassLoader hostClassLoader;

    public interface BeforeCallback {
        void call(BeforeHookCallback callback);
    }

    public interface AfterCallback {
        void call(AfterHookCallback callback);
    }

    public interface Callback {
        default void call(BeforeHookCallback callback) {}
        default void call(AfterHookCallback callback) {}
    }

    public interface ReplacementCallback extends Callback {
        Object call(XposedInterface.Chain chain) throws Throwable;
    }

    public static class BeforeHookCallback {
        private final XposedInterface.Chain chain;
        @Getter
        private boolean skipped = false;
        @Getter
        private Object skipResult = null;
        @Getter
        private boolean thrown = false;
        @Getter
        private Throwable throwable = null;

        public BeforeHookCallback(XposedInterface.Chain chain) {
            this.chain = chain;
        }

        public Object getThisObject() {
            return chain.getThisObject();
        }

        public Object[] getArgs() {
            return chain.getArgs().toArray();
        }

        public Object invokeOriginalMethod() throws Throwable {
            return chain.proceed();
        }

        public Executable getExecutable() {
            return chain.getExecutable();
        }

        public void returnAndSkip(Object result) {
            skipped = true;
            skipResult = result;
        }

        public void throwAndSkip(@NonNull Throwable result) {
            thrown = true;
            throwable = result;
        }
    }

    public static class AfterHookCallback {
        private final XposedInterface.Chain chain;
        public Object result;
        public Throwable throwable;

        public AfterHookCallback(XposedInterface.Chain chain, Object result, Throwable throwable) {
            this.chain = chain;
            this.result = result;
            this.throwable = throwable;
        }

        public Object getThisObject() {
            return chain.getThisObject();
        }

        public Object[] getArgs() {
            return chain.getArgs().toArray();
        }
    }

    public static class CustomHooker implements XposedInterface.Hooker {
        private BeforeCallback beforeCallback;
        private AfterCallback afterCallback;
        private Callback callback;
        private final boolean useCallback;

        public CustomHooker(BeforeCallback beforeCallback, AfterCallback afterCallback) {
            this.beforeCallback = beforeCallback != null ? beforeCallback : _ -> {};
            this.afterCallback = afterCallback != null ? afterCallback : _ -> {};
            this.useCallback = false;
        }

        public CustomHooker(Callback callback) {
            this.callback = callback;
            this.useCallback = true;
        }

        @Override
        public Object intercept(@NonNull XposedInterface.Chain chain) throws Throwable {
            if (useCallback && callback instanceof ReplacementCallback)
                return ((ReplacementCallback) callback).call(chain);

            Object result = null;
            Throwable throwable = null;
            boolean skipped = false;
            boolean thrown = false;

            BeforeHookCallback bcb = new BeforeHookCallback(chain);
            if (useCallback)
                callback.call(bcb);
            else
                beforeCallback.call(bcb);

            if (bcb.isSkipped()) {
                result = bcb.getSkipResult();
                skipped = true;
            }

            if (bcb.isThrown()) {
                throwable = bcb.getThrowable();
                thrown = true;
            }

            if (!skipped && !thrown) {
                try {
                    result = chain.proceed();
                } catch (Throwable t) {
                    throwable = t;
                }
            }

            AfterHookCallback acb = new AfterHookCallback(chain, result, throwable);
            if (useCallback)
                callback.call(acb);
            else
                afterCallback.call(acb);

            result = acb.result;
            throwable = acb.throwable;

            if (throwable != null)
                throw throwable;

            return result;
        }
    }

    public static XposedInterface.HookHandle hookBefore(
            Executable member,
            BeforeCallback callback
    ) {
        return xposedModule
                .hook(member)
                .intercept(new CustomHooker(callback, null));
    }

    public static XposedInterface.HookHandle hookAfter(
            Executable executable,
            AfterCallback callback
    ) {
        return xposedModule
                .hook(executable)
                .intercept(new CustomHooker(null, callback));
    }

    public static XposedInterface.HookHandle hook(
            Executable executable,
            Callback callback
    ) {
        return xposedModule
                .hook(executable)
                .intercept(new CustomHooker(callback));
    }
}
