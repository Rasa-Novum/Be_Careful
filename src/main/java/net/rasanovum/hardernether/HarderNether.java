package net.rasanovum.hardernether;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class HarderNether implements ModInitializer {
	public static final String MOD_ID = "harder-nether";
	public static MobEffect CORRUPTION;
	public static Item TOTEM_OF_LIGHT;
	public static Item ECHO_SHARD_DUST;

	public static final Map<UUID, Integer> deepDarkTimers = new HashMap<>();
	public static final Map<UUID, Integer> netherTimers = new HashMap<>();
	public static final Map<UUID, Integer> messageSchedule = new HashMap<>();

	public static final ResourceKey<DamageType> CORRUPTION_DAMAGE_TYPE =
			ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(MOD_ID, "corruption"));

	public static final TagKey<Item> FIRE_RESISTANT_FOODS =
			TagKey.create(Registries.ITEM, new ResourceLocation(MOD_ID, "fire_resistant_foods"));

	public static EnvironmentHazard DEEP_DARK;
	public static EnvironmentHazard NETHER;

	public static GameRules.Key<GameRules.BooleanValue> RULE_ONLY_RUINED_PORTALS;

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


	@Override
	public void onInitialize() {

		CORRUPTION = new CorruptionEffect();
		TOTEM_OF_LIGHT = new TotemOfLight(new FabricItemSettings().maxCount(1));
		ECHO_SHARD_DUST = new Item(new FabricItemSettings());

		Registry.register(BuiltInRegistries.MOB_EFFECT, new ResourceLocation("harder-nether", "corruption"), CORRUPTION);
		Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(MOD_ID, "totem_of_light"), TOTEM_OF_LIGHT);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation("harder-nether", "echo_shard_dust"), ECHO_SHARD_DUST);

		RULE_ONLY_RUINED_PORTALS = GameRuleRegistry.register(
				"onlyRuinedPortals",
				GameRules.Category.PLAYER,
				GameRuleFactory.createBooleanRule(true)
		);

        DEEP_DARK = new EnvironmentHazard(
				"message.hardernether.deep_dark_entry",
				3,
				"message.hardernether.deep_dark_warning", 3,
				450, 600, //change this after
				player -> {
					int playerTime = HarderNether.deepDarkTimers.getOrDefault(player.getUUID(), 0);
					int heartbeatRate = (playerTime > 550) ? 10 : 20; //this too
					if ((player.level().getGameTime() % heartbeatRate) == 0) {
						player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
								SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.5f, 1.0f);
					}
				},
				player -> {
					player.addEffect(new MobEffectInstance(HarderNether.CORRUPTION, 40, 0, false, false));
					player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 0, false, false));
				}
		);

		NETHER = new EnvironmentHazard(
				"message.hardernether.nether_entry",
				3,
				"message.hardernether.nether_warning", 3,
				200, 400,
				player -> {
					// empty, no sound plays
				},
				player -> {
					player.setSecondsOnFire(2);
					player.causeFoodExhaustion(0.05f);
				}
		);

		ServerTickEvents.START_SERVER_TICK.register(server -> {
			int currentTick = server.getTickCount();
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				UUID uuid = player.getUUID();
				if (messageSchedule.containsKey(uuid)) {
					if (currentTick >= messageSchedule.get(uuid)) {
						player.displayClientMessage(MessageManager.getRandomTranslatable("message.hardernether.totem_cleanse", 3).copy().withStyle(ChatFormatting.GREEN), true);
						messageSchedule.remove(uuid);
					}
				}

				if (player.level().getBiome(player.blockPosition()).is(Biomes.DEEP_DARK)) {
					int time = deepDarkTimers.getOrDefault(player.getUUID(), 0) + 1;
					deepDarkTimers.put(player.getUUID(), time);
					DEEP_DARK.tick(player, time);
				} else {
					deepDarkTimers.remove(player.getUUID());
				}

				if (player.level().dimension().equals(Level.NETHER)) {
					int time = netherTimers.getOrDefault(player.getUUID(), 0) + 1;
					netherTimers.put(player.getUUID(), time);
					NETHER.tick(player, time);
				}
			}
		});

		LOGGER.info("Hello Fabric world!");
	}
}