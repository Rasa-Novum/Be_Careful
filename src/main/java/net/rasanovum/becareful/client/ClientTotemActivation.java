package net.rasanovum.becareful.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.rasanovum.becareful.BeCareful;

public final class ClientTotemActivation {
    private ClientTotemActivation() {}

    public static void play() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.gameRenderer.displayItemActivation(new ItemStack(BeCareful.TOTEM_OF_LIGHT));
    }
}
