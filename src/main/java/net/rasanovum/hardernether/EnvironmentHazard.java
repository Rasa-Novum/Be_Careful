package net.rasanovum.hardernether;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import java.util.function.Consumer;

public class EnvironmentHazard {
    private final String entryMessage;
    private final String warningMessage;
    private final int warningTicks;
    private final int dangerTicks;
    private final Consumer<ServerPlayer> warningAction;
    private final Consumer<ServerPlayer> dangerAction;

    public EnvironmentHazard(String entryMessage, String warningMessage, int warningTicks, int dangerTicks, Consumer<ServerPlayer> warningAction, Consumer<ServerPlayer> dangerAction) {
        this.entryMessage = entryMessage;
        this.warningMessage = warningMessage;
        this.warningTicks = warningTicks;
        this.dangerTicks = dangerTicks;
        this.warningAction = warningAction;
        this.dangerAction = dangerAction;
    }

    public void tick(ServerPlayer player, int time) {
        if (time == 80) {
            player.displayClientMessage(Component.literal(entryMessage).withStyle(ChatFormatting.YELLOW), true);
        }
        // warning block
        if (time >= warningTicks && time < dangerTicks) {
            if (time == warningTicks) {
                player.displayClientMessage(Component.literal(warningMessage).withStyle(ChatFormatting.RED), true);
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
