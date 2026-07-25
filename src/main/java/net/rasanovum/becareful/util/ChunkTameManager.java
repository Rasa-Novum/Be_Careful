package net.rasanovum.becareful.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.rasanovum.becareful.BeCareful;
import net.rasanovum.rosetta.util.RegistryCompat;
import net.rasanovum.becareful.BeCarefulConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public final class ChunkTameManager {
    private static final Map<ServerLevel, Map<Long, SettlementProfile>> PROFILES = new WeakHashMap<>();

    public static final TagKey<Block> CHUNK_TAME_CRAFTING_TABLES =
            TagKey.create(Registries.BLOCK, RegistryCompat.getLocation(BeCareful.MOD_ID, "chunk_tame_crafting_tables"));
    public static final TagKey<Block> CHUNK_TAME_FURNACES =
            TagKey.create(Registries.BLOCK, RegistryCompat.getLocation(BeCareful.MOD_ID, "chunk_tame_furnaces"));
    public static final TagKey<Block> CHUNK_TAME_STORAGE =
            TagKey.create(Registries.BLOCK, RegistryCompat.getLocation(BeCareful.MOD_ID, "chunk_tame_storage"));

    private ChunkTameManager() {
    }

    public static int getEffectiveTameTime(ServerLevel level, BlockPos pos, int baseRequiredTicks) {
        if (!BeCarefulConfig.doChunkTameAcceleration || baseRequiredTicks <= 0) {
            return Math.max(0, baseRequiredTicks);
        }

        SettlementProfile profile = getProfile(level, level.getChunkAt(pos));
        return Math.max(1, baseRequiredTicks / profile.getDivisor());
    }

    public static boolean isSettlementBlock(BlockState state) {
        return state.getBlock() instanceof BedBlock
                || state.is(CHUNK_TAME_CRAFTING_TABLES)
                || state.is(CHUNK_TAME_FURNACES)
                || state.is(CHUNK_TAME_STORAGE);
    }

    public static void invalidate(ServerLevel level, BlockPos pos) {
        Map<Long, SettlementProfile> levelProfiles = PROFILES.get(level);
        if (levelProfiles != null) {
            levelProfiles.remove(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4));
        }
    }

    private static SettlementProfile getProfile(ServerLevel level, LevelChunk chunk) {
        Map<Long, SettlementProfile> levelProfiles = PROFILES.computeIfAbsent(level, ignored -> new HashMap<>());
        long chunkKey = chunk.getPos().toLong();
        SettlementProfile profile = levelProfiles.get(chunkKey);

        if (profile == null) {
            profile = scanChunk(level, chunk);
            levelProfiles.put(chunkKey, profile);
        }

        return profile;
    }

    private static SettlementProfile scanChunk(ServerLevel level, LevelChunk chunk) {
        boolean hasBed = false;
        boolean hasCraftingTable = false;
        boolean hasFurnace = false;
        boolean hasStorage = false;

        ChunkPos chunkPos = chunk.getPos();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int x = chunkPos.getMinBlockX(); x <= chunkPos.getMaxBlockX(); x++) {
            for (int z = chunkPos.getMinBlockZ(); z <= chunkPos.getMaxBlockZ(); z++) {
                for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
                    BlockState state = chunk.getBlockState(mutablePos.set(x, y, z));

                    if (!hasBed && state.getBlock() instanceof BedBlock) {
                        hasBed = true;
                    }
                    if (!hasCraftingTable && state.is(CHUNK_TAME_CRAFTING_TABLES)) {
                        hasCraftingTable = true;
                    }
                    if (!hasFurnace && state.is(CHUNK_TAME_FURNACES)) {
                        hasFurnace = true;
                    }
                    if (!hasStorage && state.is(CHUNK_TAME_STORAGE)) {
                        hasStorage = true;
                    }

                    if (hasBed && hasCraftingTable && hasFurnace && hasStorage) {
                        return new SettlementProfile(true, true, true, true);
                    }
                }
            }
        }

        return new SettlementProfile(hasBed, hasCraftingTable, hasFurnace, hasStorage);
    }

    private record SettlementProfile(boolean hasBed, boolean hasCraftingTable, boolean hasFurnace, boolean hasStorage) {
        private int getDivisor() {
            int checks = 0;
            if (hasBed) checks++;
            if (hasCraftingTable) checks++;
            if (hasFurnace) checks++;
            if (hasStorage) checks++;

            return 1 << checks;
        }
    }
}
