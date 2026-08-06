package net.rasanovum.becareful.client;

import java.lang.reflect.Method;

public final class IrisCompat {
    private static final Object API;
    private static final Method SHADERS_IN_USE;

    static {
        Object api = null;
        Method shadersInUse = null;
        try {
            Class<?> apiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            api = apiClass.getMethod("getInstance").invoke(null);
            shadersInUse = apiClass.getMethod("isShaderPackInUse");
        } catch (ReflectiveOperationException ignored) {
        }
        API = api;
        SHADERS_IN_USE = shadersInUse;
    }

    private IrisCompat() {
    }

    public static boolean isShaderPackInUse() {
        if (API == null || SHADERS_IN_USE == null) return false;
        try {
            return (boolean) SHADERS_IN_USE.invoke(API);
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }
}
