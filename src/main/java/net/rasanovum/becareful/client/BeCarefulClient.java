package net.rasanovum.becareful.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.rasanovum.becareful.BeCareful;
import net.rasanovum.becareful.BeCarefulConfig;

public class BeCarefulClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        if (BeCarefulConfig.doFrozenFeatures) {
            ClientBreathController.register();
            BlockEntityRenderers.register(BeCareful.FROZEN_CAMPFIRE_ENTITY_TYPE, FrozenCampfireRenderer::new);
            BlockRenderLayerMap.INSTANCE.putBlock(BeCareful.FROZEN_CAMPFIRE, RenderType.cutout());
        }
    }
}