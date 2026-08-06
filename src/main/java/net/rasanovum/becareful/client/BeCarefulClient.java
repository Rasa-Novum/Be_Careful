package net.rasanovum.becareful.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.rasanovum.becareful.BeCareful;
import net.rasanovum.becareful.BeCarefulConfig;

public class BeCarefulClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRenderers.register(BlockEntityType.END_PORTAL, AncientEndPortalRenderer::new);
        if (BeCarefulConfig.doFrozenFeatures) {
            ClientTickEvents.END_CLIENT_TICK.register(ClientBreathController::tick);
            BlockEntityRenderers.register(BeCareful.FROZEN_CAMPFIRE_ENTITY_TYPE, FrozenCampfireRenderer::new);
            BlockRenderLayerMap.INSTANCE.putBlock(BeCareful.FROZEN_CAMPFIRE, RenderType.cutout());
        }
    }
}
