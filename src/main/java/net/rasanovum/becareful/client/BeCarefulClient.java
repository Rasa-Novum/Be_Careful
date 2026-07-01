package net.rasanovum.becareful.client;

import net.fabricmc.api.ClientModInitializer;
import net.rasanovum.becareful.BeCarefulConfig;

public class BeCarefulClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        if (BeCarefulConfig.doFrozenFeatures) {
            ClientBreathController.register();
        }
    }
}