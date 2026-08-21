package net.rasanovum.becareful.util;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

public record EnvironmentHazard(
        String entryBaseKey,
        int entryVariants,
        String warningBaseKey,
        int warningVariants,
        int warningTicks,
        int dangerTicks,
        Consumer<ServerPlayer> warningAction,
        Consumer<ServerPlayer> dangerAction
) {

    public void tick(ServerPlayer player, int time) {
        if (time == 80) {
            player.displayClientMessage(
            MessageManager.getRandomTranslatable(entryBaseKey, entryVariants).copy().withStyle(ChatFormatting.YELLOW),
                    true);
        }
        // warning block
        if (time >= warningTicks && time < dangerTicks) {
            if (time == warningTicks) {
                player.displayClientMessage(
                MessageManager.getRandomTranslatable(warningBaseKey, warningVariants).copy().withStyle(ChatFormatting.RED),
                        true);
            }
            if (warningAction != null) {
                warningAction.accept(player);
            }
        }
        // danger/status effect application
        if (time >= dangerTicks) {
            dangerAction.accept(player);
        }
    }
}
