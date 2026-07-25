package net.rasanovum.becareful.mixin.portal;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.portal.PortalShape;
import net.rasanovum.becareful.BeCareful;
import net.rasanovum.becareful.util.MessageManager;
import net.rasanovum.becareful.portals.PortalState;
import net.rasanovum.rosetta.util.GameRuleCompat;
import net.rasanovum.rosetta.util.RegistryCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PortalShape.class)
public class PortalShapeMixin {

    private static final TagKey<Structure> RUINED_PORTALS =
            TagKey.create(Registries.STRUCTURE, RegistryCompat.getLocation("minecraft", "ruined_portal"));

    @Inject(method = "findEmptyPortalShape", at = @At("RETURN"), cancellable = true)
    private static void validatePortalLocation(LevelAccessor level, BlockPos pos, Direction.Axis axis, CallbackInfoReturnable<Optional<PortalShape>> cir) {

        if (!cir.getReturnValue().isPresent()) {
            return;
        }

        if (level instanceof ServerLevel serverLevel) {
            if (GameRuleCompat.get(serverLevel, BeCareful.RULE_ONLY_RUINED_PORTALS)) {

                PortalState state = PortalState.get(serverLevel);
                boolean isNether = serverLevel.dimension().equals(net.minecraft.world.level.Level.NETHER);

                if (isNether) {
                    if (!state.isAuthorized(pos, serverLevel)) {
                        Component msg = MessageManager.getRandomTranslatable("message.be-careful.no_overworld_anchor", 3);
                        sendFailure(serverLevel, pos, msg);
                        cir.setReturnValue(Optional.empty());
                    }
                } else {
                    StructureStart start = serverLevel.structureManager().getStructureWithPieceAt(pos, RUINED_PORTALS);

                    if ((start != null && start.isValid()) || state.isAuthorized(pos, serverLevel)) {
                        state.addPortal(pos);
                        return;
                    }
                    Component msg = MessageManager.getRandomTranslatable("message.be-careful.not_ruined_portal", 3);
                    sendFailure(serverLevel, pos, msg);
                    cir.setReturnValue(Optional.empty());
                }
            }
        }
    }

    private static void sendFailure(ServerLevel level, BlockPos pos, Component message) {
        ServerPlayer player = (ServerPlayer) level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 7.0, false);
        if (player != null) {
            player.displayClientMessage(
                    message.copy().withStyle(ChatFormatting.LIGHT_PURPLE),
                    true
            );
        }
    }
}
