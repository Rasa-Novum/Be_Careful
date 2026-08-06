package net.rasanovum.becareful.loaders.neoforge;

/*? if neoforge {*/
/*import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.rasanovum.becareful.BeCareful;
import net.rasanovum.becareful.BeCarefulConfig;
import net.rasanovum.becareful.blocks.FrozenCampfireBlock;
import net.rasanovum.becareful.blocks.FrozenCampfireBlockEntity;
import net.rasanovum.becareful.client.ClientBreathController;
import net.rasanovum.becareful.client.AncientEndPortalRenderer;
import net.rasanovum.becareful.client.FrozenCampfireRenderer;
import net.rasanovum.becareful.effects.CorruptionEffect;
import net.rasanovum.becareful.effects.TotemOfLight;
import net.rasanovum.becareful.portals.AncientPortalHandler;
import net.rasanovum.becareful.portals.EndGatewaySavedData;
import net.rasanovum.becareful.portals.PortalState;
import net.rasanovum.becareful.spawning.EndPhantomSpawner;
import net.rasanovum.becareful.spawning.EndSpawnHandler;
import net.rasanovum.becareful.util.ColdEnvironmentManager;

@Mod("be_careful")
public final class NeoForgeMain {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(BeCareful.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BeCareful.MOD_ID);
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, BeCareful.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, BeCareful.MOD_ID);

    private static final DeferredHolder<net.minecraft.world.level.block.Block, FrozenCampfireBlock> FROZEN_CAMPFIRE =
            BLOCKS.register("frozen_campfire", () -> new FrozenCampfireBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PODZOL).strength(2.0F).sound(net.minecraft.world.level.block.SoundType.WOOD)
                    .lightLevel(state -> state.getValue(net.minecraft.world.level.block.CampfireBlock.LIT) ? 15 : 0)
                    .ignitedByLava().noOcclusion().requiresCorrectToolForDrops()));
    private static final DeferredHolder<Item, BlockItem> FROZEN_CAMPFIRE_ITEM =
            ITEMS.register("frozen_campfire", () -> new BlockItem(FROZEN_CAMPFIRE.get(),
                    new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));
    private static final DeferredHolder<Item, Item> TOTEM_OF_LIGHT =
            ITEMS.register("totem_of_light", () -> new TotemOfLight(new Item.Properties().stacksTo(1)));
    private static final DeferredHolder<Item, Item> ECHO_SHARD_DUST =
            ITEMS.register("echo_shard_dust", () -> new Item(new Item.Properties()));
    private static final DeferredHolder<Item, Item> LOST_KEY =
            ITEMS.register("lost_key", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    private static final DeferredHolder<Item, Item> FROZEN_CORE =
            ITEMS.register("frozen_core", () -> new Item(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));
    private static final DeferredHolder<MobEffect, MobEffect> CORRUPTION =
            EFFECTS.register("corruption", CorruptionEffect::new);
    private static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FrozenCampfireBlockEntity>> FROZEN_CAMPFIRE_ENTITY =
            BLOCK_ENTITIES.register("frozen_campfire_be",
                    () -> BlockEntityType.Builder.of(FrozenCampfireBlockEntity::new, FROZEN_CAMPFIRE.get()).build(null));

    public NeoForgeMain(IEventBus modEventBus) {
        EndGatewaySavedData.bootstrap();
        PortalState.bootstrap();
        BeCareful.registerGameRules();
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        EFFECTS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        modEventBus.addListener(NeoForgeMain::commonSetup);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(NeoForgeMain::clientSetup);
            modEventBus.addListener(NeoForgeMain::registerRenderers);
            NeoForge.EVENT_BUS.addListener(NeoForgeMain::clientTick);
        }

        NeoForge.EVENT_BUS.addListener(NeoForgeMain::serverTick);
        NeoForge.EVENT_BUS.addListener(NeoForgeMain::levelTick);
        NeoForge.EVENT_BUS.addListener(NeoForgeMain::rightClickItem);
        NeoForge.EVENT_BUS.addListener(NeoForgeMain::rightClickBlock);
        NeoForge.EVENT_BUS.addListener(NeoForgeMain::changedDimension);
        NeoForge.EVENT_BUS.addListener(NeoForgeMain::livingDeath);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            BeCareful.setRegistrations(CORRUPTION.get(), TOTEM_OF_LIGHT.get(), ECHO_SHARD_DUST.get(), LOST_KEY.get(),
                    FROZEN_CORE.get(), FROZEN_CAMPFIRE.get(), FROZEN_CAMPFIRE_ITEM.get(), FROZEN_CAMPFIRE_ENTITY.get());
            new BeCareful().onInitialize();
        });
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemBlockRenderTypes.setRenderLayer(FROZEN_CAMPFIRE.get(), RenderType.cutout()));
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntityType.END_PORTAL, AncientEndPortalRenderer::new);
        event.registerBlockEntityRenderer(FROZEN_CAMPFIRE_ENTITY.get(), FrozenCampfireRenderer::new);
    }

    private static void clientTick(ClientTickEvent.Post event) {
        if (BeCarefulConfig.doFrozenFeatures) ClientBreathController.tick(net.minecraft.client.Minecraft.getInstance());
    }

    private static void serverTick(ServerTickEvent.Post event) {
        BeCareful.onServerTick(event.getServer());
    }

    private static void levelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            EndPhantomSpawner.tick(level);
            if (BeCarefulConfig.doFrozenFeatures) ColdEnvironmentManager.tick(level);
        }
    }

    private static void rightClickItem(PlayerInteractEvent.RightClickItem event) {
        var result = EndSpawnHandler.onUseEnderEye(event.getEntity(), event.getLevel(), event.getHand());
        if (result.getResult() != InteractionResult.PASS) {
            event.setCancellationResult(result.getResult());
            event.setCanceled(true);
        }
    }

    private static void rightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!BeCarefulConfig.doEndFeatures) return;
        InteractionResult result = AncientPortalHandler.useBlock(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
        if (result != InteractionResult.PASS) {
            event.setCancellationResult(result);
            event.setCanceled(true);
        }
    }

    private static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player
                && player.level() instanceof ServerLevel target) {
            EndSpawnHandler.onPlayerEnterEnd(player, target, target);
        }
    }

    private static void livingDeath(LivingDeathEvent event) {
        if (BeCarefulConfig.doEndFeatures) {
            AncientPortalHandler.onKilledOtherEntity(event.getEntity().level(), event.getEntity());
        }
    }
}
*//*?}*/
