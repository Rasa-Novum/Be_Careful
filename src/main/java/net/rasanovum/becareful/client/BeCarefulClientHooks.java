package net.rasanovum.becareful.client;

import net.minecraft.client.Minecraft;
import net.rasanovum.becareful.BeCarefulConfig;
import net.rasanovum.rosetta.event.ClientHooks;

public final class BeCarefulClientHooks {
    private static boolean registered;

    private BeCarefulClientHooks() {}

    public static void register() {
        if (registered) return;
        ClientHooks.register(new ClientHooks.Callbacks() {
            @Override
            public void onEndClientTick() {
                if (BeCarefulConfig.doFrozenFeatures) {
                    ClientBreathController.tick(Minecraft.getInstance());
                }
            }
        });
        registered = true;
    }
}
