package net.rasanovum.hardernether.util;


import net.minecraft.network.chat.Component;
import java.util.Random;

public class MessageManager {
    private static final Random RANDOM = new Random();
    public static Component getRandomTranslatable(String baseKey, int variants) {
        int index = RANDOM.nextInt(variants) + 1;
        return Component.translatable(baseKey + "." + index);
    }
}