package net.rasanovum.hardernether;

import eu.midnightdust.lib.config.MidnightConfig;

public class HarderNetherConfig extends MidnightConfig {

    // Nether Settings
    @Comment(centered = true)
    public static String netherCategory;

    @Entry(min = 0)
    public static int netherWarningTicks = 200;

    @Entry(min = 0)
    public static int netherDangerTicks = 400;

    @Entry(min = 1)
    public static int netherEntryVariants = 3;

    @Entry(min = 1)
    public static int netherWarningVariants = 3;

    // Deep Dark Settings
    @Comment(centered = true)
    public static String deepDarkCategory;

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

    // Regional Difficulty / Chunk Taming

    @Entry(min = 1)
    public static int chunkTameVariants = 2;

    @Entry(min = 1)
    public static int chunkUnsafeVariants = 2;

}
