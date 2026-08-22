package net.rasanovum.becareful.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.culling.Frustum;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.rasanovum.becareful.BeCarefulConfig;
import net.rasanovum.becareful.corruption.ClientCorruptionState;
import net.rasanovum.becareful.light.ClientLightFieldState;
import net.rasanovum.becareful.light.LightFieldNetworking;
import net.rasanovum.rosetta.event.ClientHooks;
import net.rasanovum.rosetta.event.ClientRenderHooks;
import net.rasanovum.rosetta.event.ClientShaderHooks;
import net.rasanovum.rosetta.util.RegistryCompat;

public final class BeCarefulClientHooks {
    private static boolean registered;

    private BeCarefulClientHooks() {}

    public static void register() {
        if (registered) return;
        LightFieldNetworking.setTotemActivationHandler(ClientTotemActivation::play);
        ClientShaderHooks.register(registrar -> registrar.register(
                RegistryCompat.getLocation("be_careful", "light_field"),
                DefaultVertexFormat.POSITION_COLOR,
                LightFieldShader::set
        ));
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
            public void renderWorld(PoseStack poseStack, ClientLevel level, net.minecraft.world.entity.player.Player player,
                                    float tickDelta, Frustum frustum) {
                LightFieldRenderer.render(poseStack, level, player, tickDelta, frustum);
            }

            @Override
            public void renderHud(net.minecraft.client.gui.GuiGraphics graphics, float tickDelta) {
                CorruptionOverlay.render(graphics, tickDelta);
            }
        });
        registered = true;
    }
}
