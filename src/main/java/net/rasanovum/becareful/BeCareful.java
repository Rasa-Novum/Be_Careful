package net.rasanovum.becareful;

import eu.midnightdust.lib.config.MidnightConfig;

/*? if fabric {*/
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
/*?}*/

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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.rasanovum.becareful.blocks.FrozenCampfireBlock;
import net.rasanovum.becareful.blocks.FrozenCampfireBlockEntity;
import net.rasanovum.becareful.effects.CorruptionEffect;
import net.rasanovum.becareful.effects.TotemOfLight;
import net.rasanovum.becareful.spawning.EndPhantomSpawner;
import net.rasanovum.becareful.spawning.EndSpawnHandler;
import net.rasanovum.becareful.portals.AncientPortalHandler;
import net.rasanovum.becareful.util.*;
import net.rasanovum.rosetta.util.EntityCompat;
import net.rasanovum.rosetta.util.GameRuleCompat;
import net.rasanovum.rosetta.util.RegistryCompat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class BeCareful /*? if fabric {*/ implements ModInitializer /*?}*/ {
	public static final String MOD_ID = "be-careful";
	public static MobEffect CORRUPTION;
	/*? if >=1.21 {*/
	public static net.minecraft.core.Holder<MobEffect> CORRUPTION_HOLDER;
	/*?}*/
	public static Item TOTEM_OF_LIGHT;
	public static Item ECHO_SHARD_DUST;
	public static Item LOST_KEY;
	public static BlockEntityType<FrozenCampfireBlockEntity> FROZEN_CAMPFIRE_ENTITY_TYPE;
	public static FrozenCampfireBlock FROZEN_CAMPFIRE;
	public static BlockItem FROZEN_CAMPFIRE_ITEM;
	public static Item FROZEN_CORE;


	public static final Map<UUID, Integer> DEEP_DARK_TIMERS = new HashMap<>();
	public static final Map<UUID, Integer> NETHER_TIMERS = new HashMap<>();
	public static final Map<UUID, Integer> MESSAGE_SCHEDULE = new HashMap<>();

	public static final ResourceKey<DamageType> CORRUPTION_DAMAGE_TYPE =
			ResourceKey.create(Registries.DAMAGE_TYPE, RegistryCompat.getLocation(MOD_ID, "corruption"));

	public static final TagKey<Item> FIRE_RESISTANT_FOODS =
			TagKey.create(Registries.ITEM, RegistryCompat.getLocation(MOD_ID, "fire_resistant_foods"));

	public static EnvironmentHazard DEEP_DARK;
	public static EnvironmentHazard NETHER;

	private static final GameRuleCompat GAME_RULES = new GameRuleCompat(MOD_ID);
	public static GameRuleCompat.Key<Boolean> RULE_ONLY_RUINED_PORTALS;
	public static GameRuleCompat.Key<Integer> RULE_CHUNK_TAME_TIME;
	public static GameRuleCompat.Key<Boolean> RULE_DO_PORTAL_DEBUG;

	// deep dark
	public static final int DD_ENTRY_VARIANTS = BeCarefulConfig.deepDarkEntryVariants;
	public static final int DD_WARN_TICKS = BeCarefulConfig.deepDarkWarningTicks;
	public static final int DD_WARN_VARIANTS = BeCarefulConfig.deepDarkWarningVariants;
	public static final int DD_DANGER_TICKS = BeCarefulConfig.deepDarkDangerTicks;
	public static final int TOTEM_VARIANTS = BeCarefulConfig.totemVariants;

	// nether
	public static final int N_ENTRY_VARIANTS = BeCarefulConfig.netherEntryVariants;
	public static final int N_WARN_VARIANTS = BeCarefulConfig.netherWarningVariants;
	public static final int N_WARN_TICKS = BeCarefulConfig.netherWarningTicks;
	public static final int N_DANGER_TICKS = BeCarefulConfig.netherDangerTicks;

	// chunk tame
	public static final int CHUNK_TAME_VARIANTS = BeCarefulConfig.chunkTameVariants;

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static void setRegistrations(MobEffect corruption, Item totemOfLight, Item echoShardDust,
										Item lostKey, Item frozenCore, FrozenCampfireBlock frozenCampfire,
										BlockItem frozenCampfireItem,
										BlockEntityType<FrozenCampfireBlockEntity> frozenCampfireEntityType) {
		CORRUPTION = corruption;
		TOTEM_OF_LIGHT = totemOfLight;
		ECHO_SHARD_DUST = echoShardDust;
		LOST_KEY = lostKey;
		FROZEN_CORE = frozenCore;
		FROZEN_CAMPFIRE = frozenCampfire;
		FROZEN_CAMPFIRE_ITEM = frozenCampfireItem;
		FROZEN_CAMPFIRE_ENTITY_TYPE = frozenCampfireEntityType;
		/*? if >=1.21 {*/
		CORRUPTION_HOLDER = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(CORRUPTION);
		/*?}*/
	}

	/*? if fabric {*/
	@Override
	/*?}*/
	public void onInitialize() {

		MidnightConfig.init(MOD_ID, BeCarefulConfig.class);

		/*? if fabric {*/
		CORRUPTION = new CorruptionEffect();
		TOTEM_OF_LIGHT = new TotemOfLight(new Item.Properties().stacksTo(1));
		ECHO_SHARD_DUST = new Item(new Item.Properties());
		LOST_KEY = new Item(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
		FROZEN_CORE = new Item(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON));
		FROZEN_CAMPFIRE = new FrozenCampfireBlock(
				FabricBlockSettings.create()
						.mapColor(net.minecraft.world.level.material.MapColor.PODZOL)
						.strength(2.0F)
						.sound(net.minecraft.world.level.block.SoundType.WOOD)
						.lightLevel(state -> state.getValue(net.minecraft.world.level.block.CampfireBlock.LIT) ? 15 : 0)
						.ignitedByLava()
						.noOcclusion()
						.requiresCorrectToolForDrops()
		);
		FROZEN_CAMPFIRE_ITEM = new BlockItem(FROZEN_CAMPFIRE, new Item.Properties().stacksTo(16).rarity(Rarity.RARE));

		FROZEN_CAMPFIRE_ENTITY_TYPE = Registry.register(
				BuiltInRegistries.BLOCK_ENTITY_TYPE,
				RegistryCompat.getLocation(MOD_ID, "frozen_campfire_be"),
				FabricBlockEntityTypeBuilder.create(FrozenCampfireBlockEntity::new, FROZEN_CAMPFIRE).build()
		);

		Registry.register(BuiltInRegistries.MOB_EFFECT, RegistryCompat.getLocation(MOD_ID, "corruption"), CORRUPTION);
		/*? if >=1.21 {*/
		CORRUPTION_HOLDER = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(CORRUPTION);
		/*?}*/
		Registry.register(BuiltInRegistries.ITEM, RegistryCompat.getLocation(MOD_ID, "totem_of_light"), TOTEM_OF_LIGHT);
		Registry.register(BuiltInRegistries.ITEM, RegistryCompat.getLocation(MOD_ID, "echo_shard_dust"), ECHO_SHARD_DUST);
		Registry.register(BuiltInRegistries.ITEM, RegistryCompat.getLocation(MOD_ID, "lost_key"), LOST_KEY);
		Registry.register(BuiltInRegistries.ITEM, RegistryCompat.getLocation(MOD_ID, "frozen_core"), FROZEN_CORE);
		Registry.register(BuiltInRegistries.ITEM, RegistryCompat.getLocation(MOD_ID, "frozen_campfire"), FROZEN_CAMPFIRE_ITEM);
		Registry.register(BuiltInRegistries.BLOCK, RegistryCompat.getLocation(MOD_ID, "frozen_campfire"), FROZEN_CAMPFIRE);
		/*?}*/

		/*? if fabric {*/
		UseItemCallback.EVENT.register(EndSpawnHandler::onUseEnderEye);
		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(EndSpawnHandler::onPlayerEnterEnd);
		EndPhantomSpawner.register();

		if (BeCarefulConfig.doEndFeatures) {
			AncientPortalHandler.registerEvents();
		}

		if (BeCarefulConfig.doFrozenFeatures) {
			ColdEnvironmentManager.register();
		}
		/*?}*/

		RULE_ONLY_RUINED_PORTALS = GAME_RULES.registerBool("onlyRuinedPortals", GameRuleCompat.Category.PLAYER, true);
		RULE_CHUNK_TAME_TIME = GAME_RULES.registerInt("chunkTameTime", GameRuleCompat.Category.PLAYER, 72000);
		RULE_DO_PORTAL_DEBUG = GAME_RULES.registerBool("doPortalDebug", GameRuleCompat.Category.PLAYER, false);


		if (BeCarefulConfig.doDeepDarkFeatures) {
			DEEP_DARK = new EnvironmentHazard(
					"message.be-careful.deep_dark_entry",
					DD_ENTRY_VARIANTS,
					"message.be-careful.deep_dark_warning", DD_WARN_VARIANTS,
					DD_WARN_TICKS, DD_DANGER_TICKS,
					player -> {
						int playerTime = BeCareful.DEEP_DARK_TIMERS.getOrDefault(player.getUUID(), 0);
						int heartbeatRate = (playerTime > 550) ? 10 : 20;
						if ((player.level().getGameTime() % heartbeatRate) == 0) {
							player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
									SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.5f, 1.0f);
						}
					},
					player -> {
						/*? if <1.21 {*/
						/*player.addEffect(new MobEffectInstance(BeCareful.CORRUPTION, 40, 0, false, false));
						*//*?} else {*/
						player.addEffect(new MobEffectInstance(BeCareful.CORRUPTION_HOLDER, 40, 0, false, false));
						/*?}*/
						player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 0, false, false));
					}
			);
		}

		if (BeCarefulConfig.doNetherFeatures) {
			NETHER = new EnvironmentHazard(
					"message.be-careful.nether_entry",
					N_ENTRY_VARIANTS,
					"message.be-careful.nether_warning", N_WARN_VARIANTS,
					N_WARN_TICKS, N_DANGER_TICKS,
					player -> {
						// empty, no sound plays
					},
					player -> {
						/*? if <1.21 {*/
						/*player.setSecondsOnFire(2);
						*//*?} else {*/
						player.igniteForSeconds(2);
						/*?}*/
					}
			);
		}


		/*? if fabric {*/
		ServerTickEvents.START_SERVER_TICK.register(BeCareful::onServerTick);
		/*?}*/
		LOGGER.info("this mod does too much stuff man what on earth are we going to name it");
	}

	public static void onServerTick(net.minecraft.server.MinecraftServer server) {
			int currentTick = server.getTickCount();
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				int requiredTicks = GameRuleCompat.get(player.serverLevel(), RULE_CHUNK_TAME_TIME);
				long inhabitedTime = player.serverLevel().getChunkAt(player.blockPosition()).getInhabitedTime();
				int effectiveRequiredTicks = ChunkTameManager.getEffectiveTameTime(
						player.serverLevel(), player.blockPosition(), requiredTicks
				);
				if (inhabitedTime == effectiveRequiredTicks && BeCarefulConfig.doDifficultyFeatures) {
					EntityCompat.displayClientMessage(player,
							MessageManager.getRandomTranslatable("message.be-careful.chunk_tamed", CHUNK_TAME_VARIANTS)
									.copy().withStyle(ChatFormatting.GOLD),
							false
					);
				}

				UUID uuid = player.getUUID();
				if (MESSAGE_SCHEDULE.containsKey(uuid)) {
					if (currentTick >= MESSAGE_SCHEDULE.get(uuid)) {
						EntityCompat.displayClientMessage(player, MessageManager.getRandomTranslatable("message.be-careful.totem_cleanse", TOTEM_VARIANTS).copy().withStyle(ChatFormatting.GREEN), true);
						MESSAGE_SCHEDULE.remove(uuid);
					}
				}

				if (player.level().getBiome(player.blockPosition()).is(Biomes.DEEP_DARK) && BeCarefulConfig.doDeepDarkFeatures) {
					int time = DEEP_DARK_TIMERS.getOrDefault(player.getUUID(), 0) + 1;
					DEEP_DARK_TIMERS.put(player.getUUID(), time);
					DEEP_DARK.tick(player, time);
				} else {
					DEEP_DARK_TIMERS.remove(player.getUUID());
				}

				if (player.level().dimension().equals(Level.NETHER) && BeCarefulConfig.doNetherFeatures) {
					int time = NETHER_TIMERS.getOrDefault(player.getUUID(), 0) + 1;
					NETHER_TIMERS.put(player.getUUID(), time);
					NETHER.tick(player, time);
				}
			}
	}
}
