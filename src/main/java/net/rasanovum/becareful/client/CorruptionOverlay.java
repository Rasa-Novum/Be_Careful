package net.rasanovum.becareful.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.rasanovum.becareful.corruption.ClientCorruptionState;
import net.rasanovum.rosetta.util.RegistryCompat;

public final class CorruptionOverlay {
    private static final int FRAME_COUNT = 17;
    private static final long FRAME_DURATION_MS = 75L;
    private static final ResourceLocation[] CORRUPTION_FRAMES = new ResourceLocation[FRAME_COUNT];
    private static long animationStartMillis = -1L;

    static {
        for (int frame = 0; frame < FRAME_COUNT; frame++) {
            CORRUPTION_FRAMES[frame] = RegistryCompat.getLocation(
                    "be_careful", "textures/misc/corruption_overlay/corruption_overlay_" + frame + ".png"
            );
        }
    }

    private CorruptionOverlay() {}

    public static void render(GuiGraphics graphics, float tickDelta) {
        float corruption = ClientCorruptionState.get();
        if (corruption <= 0.0F) {
            animationStartMillis = -1L;
            return;
        }

        long nowMillis = Util.getMillis();
        if (animationStartMillis < 0L) {
            animationStartMillis = nowMillis;
        }
        long elapsedMillis = Math.max(0L, nowMillis - animationStartMillis);
        int frame = (int) ((elapsedMillis / FRAME_DURATION_MS) % FRAME_COUNT);

        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.setColor(1.0F, 1.0F, 1.0F, corruption);
        graphics.blit(CORRUPTION_FRAMES[frame], 0, 0, width, height, 0, 0, 128, 128, 128, 128);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }
}
