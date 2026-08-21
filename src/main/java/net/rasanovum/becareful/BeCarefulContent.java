package net.rasanovum.becareful;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.rasanovum.becareful.blocks.FrozenCampfireBlock;
import net.rasanovum.becareful.blocks.FrozenCampfireBlockEntity;
import net.rasanovum.becareful.effects.CorruptionEffect;
import net.rasanovum.becareful.effects.TotemOfLight;
import net.rasanovum.rosetta.registry.ModRegistrar;
import net.rasanovum.rosetta.registry.RegistryHandle;

public final class BeCarefulContent {
    public static final ModRegistrar REGISTRAR = new ModRegistrar(BeCareful.MOD_ID);

    public static final RegistryHandle<net.minecraft.world.effect.MobEffect> CORRUPTION =
            REGISTRAR.register(BuiltInRegistries.MOB_EFFECT, "corruption", CorruptionEffect::new);
    public static final RegistryHandle<Item> TOTEM_OF_LIGHT =
            REGISTRAR.item("totem_of_light", TotemOfLight::new, new Item.Properties().stacksTo(1));
    public static final RegistryHandle<Item> ECHO_SHARD_DUST =
            REGISTRAR.item("echo_shard_dust", Item::new, new Item.Properties());
    public static final RegistryHandle<Item> LOST_KEY =
            REGISTRAR.item("lost_key", Item::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static final RegistryHandle<Item> FROZEN_CORE =
            REGISTRAR.item("frozen_core", Item::new, new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON));

    public static final ModRegistrar.BlockItemEntry<FrozenCampfireBlock, BlockItem> FROZEN_CAMPFIRE =
            REGISTRAR.blockWithItem(
                    "frozen_campfire",
                    FrozenCampfireBlock::new,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.PODZOL)
                            .strength(2.0F)
                            .sound(net.minecraft.world.level.block.SoundType.WOOD)
                            .lightLevel(state -> state.getValue(CampfireBlock.LIT) ? 15 : 0)
                            .ignitedByLava()
                            .noOcclusion()
                            .requiresCorrectToolForDrops(),
                    new Item.Properties().stacksTo(16).rarity(Rarity.RARE)
            );

    public static final RegistryHandle<BlockEntityType<FrozenCampfireBlockEntity>> FROZEN_CAMPFIRE_ENTITY =
            REGISTRAR.blockEntity("frozen_campfire_be", FrozenCampfireBlockEntity::new, FROZEN_CAMPFIRE.block());

    private BeCarefulContent() {}
}
