package net.rasanovum.becareful.corruption;

public record ClientCorruptionState(float value) {
    private static ClientCorruptionState current = new ClientCorruptionState(0.0F);

    public ClientCorruptionState {
        value = clamp(value);
    }

    public static float get() {
        return current.value();
    }

    public static void set(float next) {
        current = new ClientCorruptionState(next);
    }

    public static void clear() {
        current = new ClientCorruptionState(0.0F);
    }

    private static float clamp(float next) {
        return Math.max(0.0F, Math.min(1.0F, next));
    }
}
