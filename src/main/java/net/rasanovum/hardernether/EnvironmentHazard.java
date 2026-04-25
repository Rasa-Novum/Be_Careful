package net.rasanovum.hardernether;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.function.Consumer;

public class EnvironmentHazard {
    private final String entryMessage;
    private final String warningMessage;
    private final int warningTicks;
    private final int dangerTicks;
    private final Consumer<ServerPlayerEntity> warningAction;
    private final Consumer<ServerPlayerEntity> dangerAction;

    public EnvironmentHazard(String entryMessage, String warningMessage, int warningTicks, int dangerTicks, Consumer<ServerPlayerEntity> warningAction, Consumer<ServerPlayerEntity> dangerAction) {
        this.entryMessage = entryMessage;
        this.warningMessage = warningMessage;
        this.warningTicks = warningTicks;
        this.dangerTicks = dangerTicks;
        this.warningAction = warningAction;
        this.dangerAction = dangerAction;
    }

    public void tick(ServerPlayerEntity player, int time) {
        if (time == 80) {
            player.sendMessage(Text.literal(entryMessage).formatted(Formatting.YELLOW), true);
        }
        // warning block
        if (time >= warningTicks && time < dangerTicks) {
            if (time == warningTicks) {
                player.sendMessage(Text.literal(warningMessage).formatted(Formatting.RED), true);
            }
            if (warningAction != null) {
                warningAction.accept(player);
            }
        }
        // danger/status effect appplication
        if (time >= dangerTicks) {
            dangerAction.accept(player);
        }
    }
}
