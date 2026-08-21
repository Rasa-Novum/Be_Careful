package net.rasanovum.becareful.client;

import net.minecraft.client.renderer.ShaderInstance;

public final class LightFieldShader {
    private static ShaderInstance instance;

    private LightFieldShader() {}

    public static void set(ShaderInstance shader) {
        instance = shader;
    }

    public static ShaderInstance get() {
        return instance;
    }
}
