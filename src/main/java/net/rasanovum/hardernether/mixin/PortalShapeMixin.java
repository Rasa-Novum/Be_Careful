package net.rasanovum.hardernether.mixin;

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
import net.rasanovum.hardernether.HarderNether;
import net.rasanovum.hardernether.PortalState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PortalShape.class)
public class PortalShapeMixin {

    private static final TagKey<Structure> RUINED_PORTALS =
            TagKey.create(Registries.STRUCTURE, new ResourceLocation("minecraft", "ruined_portal"));

    @Inject(method = "findEmptyPortalShape", at = @At("RETURN"), cancellable = true)
    private static void validatePortalLocation(LevelAccessor level, BlockPos pos, Direction.Axis axis, CallbackInfoReturnable<Optional<PortalShape>> cir) {

        if (!cir.getReturnValue().isPresent()) {
            return;
        }

        if (level instanceof ServerLevel serverLevel) {
            if (serverLevel.getGameRules().getBoolean(HarderNether.RULE_ONLY_RUINED_PORTALS)) {

                PortalState state = PortalState.get(serverLevel);
                boolean isNether = serverLevel.dimension().equals(net.minecraft.world.level.Level.NETHER);

                if (isNether) {
                    if (state.isAuthorized(pos, serverLevel)) {
                        return;
                    }
                    sendFailure(serverLevel, pos, "No overworld anchor found for this portal...");
                    cir.setReturnValue(Optional.empty());
                } else {
                    StructureStart start = serverLevel.structureManager().getStructureWithPieceAt(pos, RUINED_PORTALS);

                    if ((start != null && start.isValid()) || state.isAuthorized(pos, serverLevel)) {
                        state.addPortal(pos);
                        return;
                    }

                    sendFailure(serverLevel, pos, "The dimensional connection is not strong enough here...");
                    cir.setReturnValue(Optional.empty());
                }
            }
        }
    }

    private static void sendFailure(ServerLevel level, BlockPos pos, String message) {
        ServerPlayer player = (ServerPlayer) level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 7.0, false);
        if (player != null) {
            player.displayClientMessage(
                    Component.literal(message).withStyle(ChatFormatting.LIGHT_PURPLE),
                    true
            );
        }
    }
}