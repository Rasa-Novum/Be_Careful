package net.rasanovum.becareful.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.EntityHitResult;
import net.rasanovum.becareful.warden.WardenStunAccess;

public final class WardenKeyPrompt {
    private static final Component STEAL_KEY = Component.translatable("prompt.be_careful.steal_key");

    private WardenKeyPrompt() {}

    public static void render(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null
                || !(minecraft.hitResult instanceof EntityHitResult hitResult)
                || !(hitResult.getEntity() instanceof Warden warden)
                || !((WardenStunAccess) warden).beCareful$isStunned()
                || (!minecraft.player.getMainHandItem().isEmpty()
                && !minecraft.player.getOffhandItem().isEmpty())) {
            return;
        }

        int x = (graphics.guiWidth() - minecraft.font.width(STEAL_KEY)) / 2;
        int y = graphics.guiHeight() / 2 + 12;
        graphics.drawString(minecraft.font, STEAL_KEY, x, y, 0xFFFFFF, true);
    }
}
