package net.rasanovum.becareful.portals;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class EndGatewaySavedData extends SavedData {

    private static final String DATA_NAME = "becareful_end_gateways";
    private int generatedCount = 0;

    public EndGatewaySavedData() {}

    public static EndGatewaySavedData load(CompoundTag nbt) {
        EndGatewaySavedData data = new EndGatewaySavedData();
        data.generatedCount = nbt.getInt("GeneratedCount");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt.putInt("GeneratedCount", this.generatedCount);
        return nbt;
    }

    public int getGeneratedCount() {
        return this.generatedCount;
    }

    public void incrementGeneratedCount() {
        this.generatedCount++;
        this.setDirty();
    }

    public static EndGatewaySavedData get(ServerLevel level) {
        DimensionDataStorage storage = level.getServer().overworld().getDataStorage();

        return storage.computeIfAbsent(
                EndGatewaySavedData::load,
                EndGatewaySavedData::new,
                DATA_NAME
        );
    }
}
