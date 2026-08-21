package net.rasanovum.becareful.loaders.neoforge;

/*? if neoforge {*/
/*import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.rasanovum.becareful.BeCareful;
import net.rasanovum.becareful.BeCarefulContent;
import net.rasanovum.becareful.BeCarefulConfig;
import net.rasanovum.becareful.client.AncientEndPortalRenderer;
import net.rasanovum.becareful.client.BeCarefulClientHooks;
import net.rasanovum.becareful.client.FrozenCampfireRenderer;
import net.rasanovum.becareful.client.LightFieldShader;
import net.rasanovum.becareful.light.LightFieldManager;
import net.rasanovum.becareful.portals.AncientPortalHandler;
import net.rasanovum.becareful.portals.EndGatewaySavedData;
import net.rasanovum.becareful.portals.PortalState;
import net.rasanovum.becareful.spawning.EndSpawnHandler;
import net.rasanovum.rosetta.registry.RegistrationContext;

@Mod("be_careful")
public final class NeoForgeMain {
    public NeoForgeMain(IEventBus modEventBus) {
        EndGatewaySavedData.bootstrap();
        PortalState.bootstrap();
        LightFieldManager.bootstrap();
        BeCarefulContent.REGISTRAR.register(RegistrationContext.create(modEventBus));
        modEventBus.addListener(NeoForgeMain::commonSetup);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(NeoForgeMain::clientSetup);
            modEventBus.addListener(NeoForgeMain::registerRenderers);
            modEventBus.addListener(NeoForgeMain::registerShaders);
        }

        NeoForge.EVENT_BUS.addListener(NeoForgeMain::rightClickItem);
        NeoForge.EVENT_BUS.addListener(NeoForgeMain::rightClickBlock);
        NeoForge.EVENT_BUS.addListener(NeoForgeMain::changedDimension);
        NeoForge.EVENT_BUS.addListener(NeoForgeMain::livingDeath);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(BeCareful::initializeCommon);
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            BeCarefulClientHooks.register();
            ItemBlockRenderTypes.setRenderLayer(BeCarefulContent.FROZEN_CAMPFIRE.block().get(), RenderType.cutout());
        });
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntityType.END_PORTAL, AncientEndPortalRenderer::new);
        event.registerBlockEntityRenderer(BeCarefulContent.FROZEN_CAMPFIRE_ENTITY.get(), FrozenCampfireRenderer::new);
    }

    private static void registerShaders(RegisterShadersEvent event) {
        try {
            event.registerShader(
                    new ShaderInstance(
                            event.getResourceProvider(),
                            "be_careful:light_field",
                            DefaultVertexFormat.POSITION_COLOR
                    ),
                    LightFieldShader::set
            );
        } catch (java.io.IOException exception) {
            throw new RuntimeException("Failed to register Be Careful light field shader", exception);
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
                && player.level() instanceof net.minecraft.server.level.ServerLevel target) {
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
