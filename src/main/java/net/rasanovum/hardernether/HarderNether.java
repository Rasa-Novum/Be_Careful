package net.rasanovum.hardernether;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class HarderNether implements ModInitializer {
	public static final String MOD_ID = "harder-nether";
	public static final StatusEffect CORRUPTION = new CorruptionEffect();
	public static final Map<UUID, Integer> deepDarkTimers = new HashMap<>();
	public static final Map<UUID, Integer> netherTimers = new HashMap<>();
	public static final RegistryKey<DamageType> CORRUPTION_DAMAGE_TYPE =
			RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier("harder-nether", "corruption"));
	public static final Item TOTEM_OF_LIGHT = new TotemOfLight(new FabricItemSettings().maxCount(1));
	public static final Map<UUID, Integer> messageSchedule = new HashMap<>();
	public static final TagKey<Item> FIRE_RESISTANT_FOODS =
			TagKey.of(RegistryKeys.ITEM, new Identifier("harder-nether", "fire_resistant_foods"));
	public static EnvironmentHazard DEEP_DARK;
	public static EnvironmentHazard NETHER;
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


	@Override
	public void onInitialize() {
		Registry.register(Registries.STATUS_EFFECT, new Identifier("harder-nether", "corruption"), CORRUPTION);
		Registry.register(Registries.ITEM, new Identifier("harder-nether", "totem_of_light"), TOTEM_OF_LIGHT);

		// Deep Dark Instance
		DEEP_DARK = new EnvironmentHazard(
				"A chill goes down your spine...",
				"The darkness is closing in...",
				450, 600,
				player -> {
					int playerTime = HarderNether.deepDarkTimers.getOrDefault(player.getUuid(), 0);
					int heartbeatRate = (playerTime > 550) ? 10 : 20;
					if (player.getWorld().getTime() % heartbeatRate == 0) {
						player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
								SoundEvents.ENTITY_WARDEN_HEARTBEAT, SoundCategory.PLAYERS, 1.5f, 1.0f);
					}
				},
				player -> {
					player.addStatusEffect(new StatusEffectInstance(HarderNether.CORRUPTION, 40, 0, false, false));
					player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 40, 0, false, false));
				}
		);

		// Nether Instance
		NETHER = new EnvironmentHazard(
				"The air here is blistering...",
				"The heat is becoming unbearable...",
				200, 400,
				player -> {
					// empty, no sound plays
				},
				player -> {
					player.setOnFireFor(2);
					player.addExhaustion(0.05f);
				}
		);

		ServerTickEvents.START_SERVER_TICK.register(server -> {
			int currentTick = server.getTicks();
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				UUID uuid = player.getUuid();
				if (messageSchedule.containsKey(uuid)) {
					if (currentTick >= messageSchedule.get(uuid)) {
						player.sendMessage(Text.literal("The light purges the encroaching darkness...").formatted(Formatting.GREEN), true);
						messageSchedule.remove(uuid);
					}
				}

				if (player.getWorld().getBiome(player.getBlockPos()).matchesKey(BiomeKeys.DEEP_DARK)) {
					int time = deepDarkTimers.getOrDefault(uuid, 0) + 1;
					deepDarkTimers.put(uuid, time);
					DEEP_DARK.tick(player, time);
				} else {
					deepDarkTimers.remove(uuid);
				}

				if (player.getWorld().getRegistryKey() == World.NETHER) {
					int time = netherTimers.getOrDefault(uuid, 0) + 1;
					netherTimers.put(uuid, time);
					NETHER.tick(player, time);
				} else {
					netherTimers.remove(uuid);
				}
			}
		});

		LOGGER.info("Hello Fabric world!");
	}
}