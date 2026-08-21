package net.rasanovum.becareful;

import eu.midnightdust.lib.config.MidnightConfig;

public class BeCarefulConfig extends MidnightConfig {

    public static final String NETHER = "nether";
    public static final String DEEP_DARK = "deepDark";
    public static final String END = "theEnd";
    public static final String FROZEN = "freezing";
    public static final String DIFFICULTY = "difficulty";

    // Nether Settings

    @Entry(category = NETHER)
    public static boolean doNetherFeatures = true;
    @Entry(category = NETHER, min = 0)
    public static int netherWarningTicks = 400;
    @Entry(category = NETHER, min = 0)
    public static int netherDangerTicks = 600;
    @Entry(category = NETHER,min = 1)
    public static int netherEntryVariants = 3;
    @Entry(category = NETHER,min = 1)
    public static int netherWarningVariants = 3;


    // Deep Dark Settings

    @Entry(category = DEEP_DARK)
    public static boolean doDeepDarkFeatures =  true;
    @Entry(category = DEEP_DARK,min = 0)
    public static int deepDarkWarningTicks = 1800;
    @Entry(category = DEEP_DARK,min = 0)
    public static int deepDarkDangerTicks = 2400;
    @Entry(category = DEEP_DARK,min = 1)
    public static int deepDarkEntryVariants = 3;
    @Entry(category = DEEP_DARK,min = 1)
    public static int deepDarkWarningVariants = 3;
    @Entry(category = DEEP_DARK,min = 1)
    public static int totemVariants = 3;
    @Entry(category = DEEP_DARK,min = 2)
    public static int wardenHealthValue = 300;
    @Entry(category = DEEP_DARK, min = 1)
    public static int lightFieldRadius = 5;
    @Entry(category = DEEP_DARK, min = 1)
    public static int lightFieldDurationTicks = 600;
    @Entry(category = DEEP_DARK, min = 0)
    public static int lightFieldTimerDecrement = 2;
    @Entry(category = DEEP_DARK, min = 1)
    public static int corruptionDamageIntervalTicks = 40;
    @Entry(category = DEEP_DARK)
    public static boolean lightFieldDebugParticles = true;

    // Regional Difficulty / Chunk Taming

    @Entry(category = DIFFICULTY)
    public static boolean doDifficultyFeatures = true;
    @Entry(category = DIFFICULTY)
    public static boolean doChunkTameAcceleration = true;
    @Entry(category = DIFFICULTY,min = 1)
    public static int chunkTameVariants = 2;
    @Entry(category = DIFFICULTY,min = 1)
    public static int chunkUnsafeVariants = 2;

    // Frozen

    @Entry(category = FROZEN)
    public static boolean doFrozenFeatures = true;
    @Entry(category = FROZEN,min=0)
    public static int frozenWarningTicks = 2400;
    @Entry(category = FROZEN,min=0)
    public static int frozenDangerTicks = 3600;
    @Entry(category = FROZEN,min=1)
    public static int heatCheckRadius = 8;
    @Entry(category = FROZEN,min=1)
    public static int frozenCampfireRadius = 12;

    // End

    @Entry(category = END)
    public static boolean doEndFeatures = true;
    @Entry(category = END)
    public static boolean spawnElytra = false;
    @Entry (category = END, min = 100)
    public static int minSpawnRadius = 1000;
    @Entry (category = END,min = 100)
    public static int maxSpawnRadius = 2000;
    @Entry (category = END,min = 1)
    public static long phantomSpawnAttemptsPerDay = 8;
    @Entry (category = END,min = 0)
    public static float phantomSpawnRateFloat = 0.75F;
    @Entry (category = END,min = 0)
    public static int maxEndGateways = 4;

}
