package net.rasanovum.becareful.corruption;

public record ClientDeepDarkDebugState(int deepDarkTime, int warningRemaining, int dangerRemaining,
                                       boolean inDeepDark, boolean protectedByLight, boolean nearbySculk) {
    private static ClientDeepDarkDebugState current;

    public static ClientDeepDarkDebugState get() {
        return current;
    }

    public static void set(ClientDeepDarkDebugState next) {
        current = next;
    }
}
