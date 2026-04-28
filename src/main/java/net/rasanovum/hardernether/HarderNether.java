package net.rasanovum.hardernether;

import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
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
	public static GameRules.Key<GameRules.IntegerValue> RULE_CHUNK_TAME_TIME;

	// deep dark
	public static final int DD_ENTRY_VARIANTS = HarderNetherConfig.deepDarkEntryVariants;
	public static final int DD_WARN_TICKS = HarderNetherConfig.deepDarkWarningTicks;
	public static final int DD_WARN_VARIANTS = HarderNetherConfig.deepDarkWarningVariants;
	public static final int DD_DANGER_TICKS = HarderNetherConfig.deepDarkDangerTicks;
	public static final int TOTEM_VARIANTS = HarderNetherConfig.totemVariants;

	// nether
	public static final int N_ENTRY_VARIANTS = HarderNetherConfig.netherEntryVariants;
	public static final int N_WARN_VARIANTS = HarderNetherConfig.netherWarningVariants;
	public static final int N_WARN_TICKS = HarderNetherConfig.netherWarningTicks;
	public static final int N_DANGER_TICKS = HarderNetherConfig.netherDangerTicks;

	// chunk tame
	public static final int CHUNK_TAME_VARIANTS = HarderNetherConfig.chunkTameVariants;

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


	@Override
	public void onInitialize() {

		MidnightConfig.init(MOD_ID, HarderNetherConfig.class);

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

		RULE_CHUNK_TAME_TIME = GameRuleRegistry.register(
				"chunkTameTime",
				GameRules.Category.PLAYER,
				GameRuleFactory.createIntRule(72000) // 1 hour default
		);

        DEEP_DARK = new EnvironmentHazard(
				"message.hardernether.deep_dark_entry",
				DD_ENTRY_VARIANTS,
				"message.hardernether.deep_dark_warning", DD_WARN_VARIANTS,
				DD_WARN_TICKS, DD_DANGER_TICKS,
				player -> {
					int playerTime = HarderNether.deepDarkTimers.getOrDefault(player.getUUID(), 0);
					int heartbeatRate = (playerTime > 550) ? 10 : 20;
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
				N_ENTRY_VARIANTS,
				"message.hardernether.nether_warning", N_WARN_VARIANTS,
				N_WARN_TICKS, N_DANGER_TICKS,
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
				int requiredTicks = player.serverLevel().getGameRules().getInt(RULE_CHUNK_TAME_TIME);
				long inhabitedTime = player.serverLevel().getChunkAt(player.blockPosition()).getInhabitedTime();
				if (inhabitedTime == requiredTicks) {
					player.displayClientMessage(
							MessageManager.getRandomTranslatable("message.hardernether.chunk_tamed", CHUNK_TAME_VARIANTS)
									.copy().withStyle(ChatFormatting.GOLD),
							false
					);
				}

				UUID uuid = player.getUUID();
				if (messageSchedule.containsKey(uuid)) {
					if (currentTick >= messageSchedule.get(uuid)) {
						player.displayClientMessage(MessageManager.getRandomTranslatable("message.hardernether.totem_cleanse", TOTEM_VARIANTS).copy().withStyle(ChatFormatting.GREEN), true);
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