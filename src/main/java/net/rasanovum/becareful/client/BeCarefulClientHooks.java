package net.rasanovum.becareful.client;

import net.minecraft.client.Minecraft;
import net.rasanovum.becareful.BeCarefulConfig;
import net.rasanovum.becareful.corruption.ClientCorruptionState;
import net.rasanovum.becareful.light.ClientLightFieldState;
import net.rasanovum.rosetta.event.ClientHooks;
import net.rasanovum.rosetta.event.ClientRenderHooks;

public final class BeCarefulClientHooks {
    private static boolean registered;

    private BeCarefulClientHooks() {}

    public static void register() {
        if (registered) return;
        ClientHooks.register(new ClientHooks.Callbacks() {
            @Override
            public void onJoin(net.minecraft.world.entity.player.Player player) {
                ClientCorruptionState.clear();
                ClientLightFieldState.clear();
            }

            @Override
            public void onDisconnect() {
                ClientCorruptionState.clear();
                ClientLightFieldState.clear();
            }

            @Override
            public void onEndClientTick() {
                if (BeCarefulConfig.doFrozenFeatures) {
                    ClientBreathController.tick(Minecraft.getInstance());
                }
            }
        });
        ClientRenderHooks.register(new ClientRenderHooks.Callbacks() {
            @Override
            public void renderHud(net.minecraft.client.gui.GuiGraphics graphics, float tickDelta) {
                CorruptionOverlay.render(graphics, tickDelta);
            }
        });
        registered = true;
    }
}
