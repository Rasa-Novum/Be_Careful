package net.rasanovum.becareful.client;

public final class ClientTotemUse {
    private static boolean awaitingRelease;

    private ClientTotemUse() {}

    public static boolean isAwaitingRelease() {
        return awaitingRelease;
    }

    public static void started() {
        awaitingRelease = true;
    }

    public static void reset() {
        awaitingRelease = false;
    }
}
