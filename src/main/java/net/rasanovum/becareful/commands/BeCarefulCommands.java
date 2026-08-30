package net.rasanovum.becareful.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.rasanovum.becareful.BeCarefulHooks;
import net.rasanovum.becareful.taming.ChunkTameManager;
import net.rasanovum.becareful.taming.ChunkTameNetworking;

public final class BeCarefulCommands {
    private BeCarefulCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("be_careful")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("reset")
                        .then(Commands.literal("chunk_tame")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(BeCarefulCommands::resetChunkTame)))
                        .then(Commands.literal("deep_dark")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(BeCarefulCommands::resetDeepDark)))));
    }

    private static int resetChunkTame(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
        LevelChunk chunk = level.getChunkAt(pos);
        chunk.setInhabitedTime(0L);
        chunk.setUnsaved(true);
        ChunkTameManager.invalidate(level, pos);

        if (source.getPlayer() != null) {
            ChunkTameNetworking.syncPlayer(source.getPlayer());
        }

        source.sendSuccess(() -> Component.literal("Reset chunk tame time at " + chunk.getPos().x + ", " + chunk.getPos().z), true);
        return 1;
    }

    private static int resetDeepDark(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return resetDeepDark(context, EntityArgument.getPlayer(context, "player"));
    }

    private static int resetDeepDark(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        BeCarefulHooks.resetDeepDarkState(player);
        context.getSource().sendSuccess(() -> Component.literal("Reset Deep Dark timer and corruption for " + player.getGameProfile().getName()), true);
        return 1;
    }
}
