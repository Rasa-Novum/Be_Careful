package net.rasanovum.becareful.light;

import com.google.common.collect.AbstractIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class SphericalShell extends AbstractIterator<BlockPos> {
    private final Vec3 center;
    private final double innerSqr;
    private final double outerSqr;
    private final int minX;
    private final int maxX;
    private final int maxZ;
    private final int minBuildY;
    private final int maxBuildY;
    private int x;
    private int z;
    private int y = 1;
    private int top;
    private int innerBottom;
    private int innerTop;

    public SphericalShell(Vec3 center, double innerRadius, double outerRadius, int minBuildY, int maxBuildY) {
        this.center = center;
        this.innerSqr = innerRadius < 0 ? -1 : innerRadius * innerRadius;
        this.outerSqr = outerRadius * outerRadius;
        this.minX = Mth.ceil(center.x - outerRadius - 1);
        this.maxX = Mth.floor(center.x + outerRadius);
        this.x = minX - 1;
        this.z = Mth.ceil(center.z - outerRadius - 1);
        this.maxZ = Mth.floor(center.z + outerRadius);
        this.minBuildY = minBuildY;
        this.maxBuildY = maxBuildY;
    }

    @Override
    protected BlockPos computeNext() {
        while (true) {
            if (y >= innerBottom && y <= innerTop) y = innerTop + 1;
            if (y <= top) return new BlockPos(x, y++, z);
            if (++x > maxX) {
                x = minX;
                z++;
            }
            if (z > maxZ) return endOfData();

            double dx = Math.max(0, Math.max(x - center.x, center.x - x - 1));
            double dz = Math.max(0, Math.max(z - center.z, center.z - z - 1));
            double columnSqr = dx * dx + dz * dz;
            y = 1;
            top = 0;
            if (columnSqr > outerSqr) continue;
            double height = Math.sqrt(outerSqr - columnSqr);
            y = Math.max(minBuildY, Mth.ceil(center.y - height - 1));
            top = Math.min(maxBuildY, Mth.floor(center.y + height));
            innerBottom = 1;
            innerTop = 0;
            if (columnSqr <= innerSqr) {
                double innerHeight = Math.sqrt(innerSqr - columnSqr);
                innerBottom = Mth.ceil(center.y - innerHeight - 1);
                innerTop = Mth.floor(center.y + innerHeight);
            }
        }
    }
}
