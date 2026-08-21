package net.rasanovum.becareful.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.rasanovum.becareful.corruption.ClientCorruptionState;
import net.rasanovum.rosetta.util.RegistryCompat;

public final class CorruptionOverlay {
    private static final ResourceLocation CORRUPTION_OUTLINE =
            RegistryCompat.getLocation("be_careful", "textures/misc/corruption_outline.png");

    private CorruptionOverlay() {}

    public static void render(GuiGraphics graphics, float tickDelta) {
        float corruption = ClientCorruptionState.get();
        if (corruption <= 0.0F) {
            return;
        }

        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.setColor(1.0F, 1.0F, 1.0F, corruption);
        graphics.blit(CORRUPTION_OUTLINE, 0, 0, width, height, 0, 0, 256, 256, 256, 256);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();

        // TODO: Replace this temporary corruption-outline overlay with the custom corruption renderer.
    }
}
