package net.rasanovum.becareful.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.rasanovum.becareful.BeCarefulConfig;
import net.rasanovum.becareful.taming.ClientChunkTameState;
import net.rasanovum.becareful.taming.ShelterStatus;
import org.joml.Matrix4f;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ShelterRenderer {
    private static final int SAFE_RED = 80;
    private static final int SAFE_GREEN = 255;
    private static final int UNSAFE_RED = 255;
    private static final int UNSAFE_GREEN = 80;
    private static final int COLOR_BLUE = 40;
    private static final int COLOR_ALPHA = 220;
    private static final float EDGE_OFFSET = 0.002F;
    private ShelterRenderer() {
    }

    public static void render(PoseStack poseStack, ClientLevel level, Player player, Frustum frustum) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!BeCarefulConfig.doShelterDebugVisualizer || !isDebugScreenOpen(minecraft)) {
            return;
        }

        ClientChunkTameState state = ClientChunkTameState.get();
        if (state == null || state.shelter() == null || state.shelter().volume().isEmpty()) {
            return;
        }

        ShelterStatus shelter = state.shelter();

        Set<BlockPos> volume = new HashSet<>(shelter.volume());

        // Draw only faces at the outside edge of the flooded volume. This
        // follows the actual flood-fill shape instead of imposing an AABB.
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(3.0F);

        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderType renderType = RenderType.lines();
        VertexConsumer builder = buffers.getBuffer(renderType);
        Matrix4f pose = poseStack.last().pose();
        int red = shelter.isSafe() ? SAFE_RED : UNSAFE_RED;
        int green = shelter.isSafe() ? SAFE_GREEN : UNSAFE_GREEN;
        Map<GridEdge, EnumMap<Direction, Integer>> edges = new HashMap<>();

        for (BlockPos pos : shelter.volume()) {
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = pos.relative(direction);
                if (!volume.contains(neighbor) && !isPassable(level, neighbor)) {
                    addFaceEdges(edges, pos, direction);
                }
            }
        }

        for (Map.Entry<GridEdge, EnumMap<Direction, Integer>> entry : edges.entrySet()) {
            // Two faces with the same normal share a coplanar block edge. It
            // is an internal seam in the boundary and should not be rendered.
            if (entry.getValue().values().stream().allMatch(count -> count > 1)) {
                continue;
            }
            drawBoundaryEdge(builder, pose, entry.getKey(), entry.getValue(), red, green);
        }

        buffers.endBatch(renderType);

        RenderSystem.lineWidth(1.0F);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    private static boolean isDebugScreenOpen(Minecraft minecraft) {
        //? if <1.21 {
        /*return minecraft.options.renderDebug;
        *///?} else {
        return minecraft.getDebugOverlay().showDebugScreen();
        //?}
    }

    private static boolean isPassable(ClientLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getFluidState().isEmpty()
                && state.getCollisionShape(level, pos, CollisionContext.empty()).isEmpty();
    }

    private static void addFaceEdges(Map<GridEdge, EnumMap<Direction, Integer>> edges,
                                     BlockPos pos, Direction direction) {
        float x = pos.getX();
        float y = pos.getY();
        float z = pos.getZ();
        float x2 = x + 1.0F;
        float y2 = y + 1.0F;
        float z2 = z + 1.0F;

        switch (direction) {
            case DOWN -> addQuadEdges(edges, direction,
                    new GridPoint((int) x, (int) y, (int) z), new GridPoint((int) x2, (int) y, (int) z),
                    new GridPoint((int) x2, (int) y, (int) z2), new GridPoint((int) x, (int) y, (int) z2));
            case UP -> addQuadEdges(edges, direction,
                    new GridPoint((int) x, (int) y2, (int) z), new GridPoint((int) x2, (int) y2, (int) z),
                    new GridPoint((int) x2, (int) y2, (int) z2), new GridPoint((int) x, (int) y2, (int) z2));
            case NORTH -> addQuadEdges(edges, direction,
                    new GridPoint((int) x, (int) y, (int) z), new GridPoint((int) x2, (int) y, (int) z),
                    new GridPoint((int) x2, (int) y2, (int) z), new GridPoint((int) x, (int) y2, (int) z));
            case SOUTH -> addQuadEdges(edges, direction,
                    new GridPoint((int) x, (int) y, (int) z2), new GridPoint((int) x2, (int) y, (int) z2),
                    new GridPoint((int) x2, (int) y2, (int) z2), new GridPoint((int) x, (int) y2, (int) z2));
            case WEST -> addQuadEdges(edges, direction,
                    new GridPoint((int) x, (int) y, (int) z), new GridPoint((int) x, (int) y, (int) z2),
                    new GridPoint((int) x, (int) y2, (int) z2), new GridPoint((int) x, (int) y2, (int) z));
            case EAST -> addQuadEdges(edges, direction,
                    new GridPoint((int) x2, (int) y, (int) z), new GridPoint((int) x2, (int) y, (int) z2),
                    new GridPoint((int) x2, (int) y2, (int) z2), new GridPoint((int) x2, (int) y2, (int) z));
        }
    }

    private static void addQuadEdges(Map<GridEdge, EnumMap<Direction, Integer>> edges,
                                     Direction direction, GridPoint a, GridPoint b,
                                     GridPoint c, GridPoint d) {
        addEdge(edges, direction, a, b);
        addEdge(edges, direction, b, c);
        addEdge(edges, direction, c, d);
        addEdge(edges, direction, d, a);
    }

    private static void addEdge(Map<GridEdge, EnumMap<Direction, Integer>> edges,
                                Direction direction, GridPoint a, GridPoint b) {
        GridEdge edge = GridEdge.of(a, b);
        edges.computeIfAbsent(edge, ignored -> new EnumMap<>(Direction.class))
                .merge(direction, 1, Integer::sum);
    }

    private static void drawBoundaryEdge(VertexConsumer builder, Matrix4f pose, GridEdge edge,
                                         EnumMap<Direction, Integer> faceCounts,
                                         int red, int green) {
        float offsetX = 0.0F;
        float offsetY = 0.0F;
        float offsetZ = 0.0F;
        for (Map.Entry<Direction, Integer> face : faceCounts.entrySet()) {
            if (face.getValue() == 1) {
                offsetX += face.getKey().getStepX() * EDGE_OFFSET;
                offsetY += face.getKey().getStepY() * EDGE_OFFSET;
                offsetZ += face.getKey().getStepZ() * EDGE_OFFSET;
            }
        }

        line(builder, pose,
                edge.first().x() + offsetX, edge.first().y() + offsetY, edge.first().z() + offsetZ,
                edge.second().x() + offsetX, edge.second().y() + offsetY, edge.second().z() + offsetZ,
                red, green);
    }

    private static void line(VertexConsumer builder, Matrix4f pose,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             int red, int green) {
        //? if <1.21 {
        /*builder.vertex(pose, x1, y1, z1).color(red, green, COLOR_BLUE, COLOR_ALPHA).normal(0.0F, 1.0F, 0.0F).endVertex();
        builder.vertex(pose, x2, y2, z2).color(red, green, COLOR_BLUE, COLOR_ALPHA).normal(0.0F, 1.0F, 0.0F).endVertex();
        *///?} else {
        builder.addVertex(pose, x1, y1, z1).setColor(red, green, COLOR_BLUE, COLOR_ALPHA).setNormal(0.0F, 1.0F, 0.0F);
        builder.addVertex(pose, x2, y2, z2).setColor(red, green, COLOR_BLUE, COLOR_ALPHA).setNormal(0.0F, 1.0F, 0.0F);
        //?}
    }

    private record GridPoint(int x, int y, int z) {
    }

    private record GridEdge(GridPoint first, GridPoint second) {
        private static GridEdge of(GridPoint first, GridPoint second) {
            if (compare(first, second) <= 0) {
                return new GridEdge(first, second);
            }
            return new GridEdge(second, first);
        }

        private static int compare(GridPoint first, GridPoint second) {
            int x = Integer.compare(first.x(), second.x());
            if (x != 0) return x;
            int y = Integer.compare(first.y(), second.y());
            if (y != 0) return y;
            return Integer.compare(first.z(), second.z());
        }
    }
}
