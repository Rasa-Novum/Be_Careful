package net.rasanovum.becareful.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DustParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.concurrent.ThreadLocalRandom;

public class ClientBreathSpawner {

    public static void spawnBreath (Minecraft client, ClientLevel level, Player player) {
        Vec3 headPos = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        Vec3 look = player.getViewVector(1.0F).normalize();

        Vec3 forward = look.scale(0.28D);
        Vec3 down = new Vec3(0, -0.22D, 0);
        Vec3 spawnPos = headPos.add(forward).add(down);

        DustParticleOptions breathDust = new DustParticleOptions(
                new Vector3f(0.92F, 0.95F, 0.95F),
                0.38F
        );

        ThreadLocalRandom r = ThreadLocalRandom.current();

        int particleCount = 5 + r.nextInt(3);
        for (int i = 0; i < particleCount; i++) {
            Vec3 upVector = new Vec3(0, 1, 0);
            Vec3 rightVector = look.cross(upVector).normalize();

            double sideSpread = (r.nextDouble() - 0.5) * 0.025;
            double propulsionSpeed = 0.015;

            double vx = (look.x * propulsionSpeed) + (rightVector.x * sideSpread);
            double vy = (look.y * propulsionSpeed) + 0.012 + (r.nextDouble() * 0.005);
            double vz = (look.z * propulsionSpeed) + (rightVector.z * sideSpread);

            Particle particle = client.particleEngine.createParticle(
                    breathDust,
                    spawnPos.x, spawnPos.y, spawnPos.z,
                    vx, vy, vz
            );

            if (particle instanceof DustParticle dustParticle) {
                dustParticle.setParticleSpeed(vx, vy, vz);
                int lifetime = 16 + r.nextInt(8);
                dustParticle.setLifetime(lifetime);
            }
        }
    }
}