package net.rasanovum.hardernether.util;

import eu.midnightdust.lib.config.MidnightConfig;

public class HarderNetherConfig extends MidnightConfig {

    // Nether Settings

    @Entry(min = 0)
    public static int netherWarningTicks = 200;

    @Entry(min = 0)
    public static int netherDangerTicks = 400;

    @Entry(min = 1)
    public static int netherEntryVariants = 3;

    @Entry(min = 1)
    public static int netherWarningVariants = 3;


    // Deep Dark Settings

    @Entry(min = 0)
    public static int deepDarkWarningTicks = 450;

    @Entry(min = 0)
    public static int deepDarkDangerTicks = 600;

    @Entry(min = 1)
    public static int deepDarkEntryVariants = 3;

    @Entry(min = 1)
    public static int deepDarkWarningVariants = 3;

    @Entry(min = 1)
    public static int totemVariants = 3;

    @Entry(min = 2)
    public static int wardenHealthValue = 300;

    // Regional Difficulty / Chunk Taming

    @Entry(min = 1)
    public static int chunkTameVariants = 2;

    @Entry(min = 1)
    public static int chunkUnsafeVariants = 2;

    // End

    @Entry (min = 100) public static int minSpawnRadius = 1000;
    @Entry (min = 100) public static int maxSpawnRadius = 2000;
    @Entry (min = 1) public static long phantomSpawnAttemptsPerDay = 3;
    @Entry (min = 0) public static float phantomSpawnRateFloat = 0.5F;

}
