package net.rasanovum.becareful.taming;

public record ClientChunkTameState(int chunkX, int chunkZ, long inhabitedTime, int effectiveRequiredTicks,
                                   int rateMultiplier, int settlementCategories, boolean enabled) {
    private static ClientChunkTameState current;

    public static void set(ClientChunkTameState next) {
        current = next;
    }

    public static ClientChunkTameState get() {
        return current;
    }
}
