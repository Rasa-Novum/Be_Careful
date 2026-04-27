package net.rasanovum.hardernether;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import java.util.function.Consumer;

public class EnvironmentHazard {

    private final String entryBaseKey;
    private final int entryVariants;
    private final String warningBaseKey;
    private final int warningVariants;
    private final int warningTicks;
    private final int dangerTicks;
    private final Consumer<ServerPlayer> warningAction;
    private final Consumer<ServerPlayer> dangerAction;


    public EnvironmentHazard(String entryBaseKey, int entryVariants, String warningBaseKey, int warningVariants, int warningTicks, int dangerTicks, Consumer<ServerPlayer> warningAction, Consumer<ServerPlayer> dangerAction) {
        this.entryBaseKey = entryBaseKey;
        this.entryVariants = entryVariants;
        this.warningBaseKey = warningBaseKey;
        this.warningVariants = warningVariants;
        this.warningTicks = warningTicks;
        this.dangerTicks = dangerTicks;
        this.warningAction = warningAction;
        this.dangerAction = dangerAction;
    }

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
