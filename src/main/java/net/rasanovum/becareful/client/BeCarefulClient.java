package net.rasanovum.becareful.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;
import net.rasanovum.becareful.BeCareful;
import net.rasanovum.becareful.BeCarefulConfig;

public class BeCarefulClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        if (BeCarefulConfig.doFrozenFeatures) {
            ClientBreathController.register();
            BlockRenderLayerMap.INSTANCE.putBlock(BeCareful.FROZEN_CAMPFIRE, RenderType.cutout());
        }
    }
}