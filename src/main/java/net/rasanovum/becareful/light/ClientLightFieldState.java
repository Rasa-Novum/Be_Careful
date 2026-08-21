package net.rasanovum.becareful.light;

import java.util.List;

public final class ClientLightFieldState {
    private static List<LightField> fields = List.of();

    private ClientLightFieldState() {}

    public static List<LightField> get() {
        return fields;
    }

    public static void set(List<LightField> next) {
        fields = List.copyOf(next);
    }

    public static void clear() {
        fields = List.of();
    }
}
