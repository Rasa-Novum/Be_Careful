package net.rasanovum.becareful;

import eu.midnightdust.lib.config.MidnightConfig;

/*? if fabric {*/
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
/*?}*/

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.rasanovum.becareful.blocks.FrozenCampfireBlock;
import net.rasanovum.becareful.blocks.FrozenCampfireBlockEntity;
import net.rasanovum.becareful.corruption.CorruptionManager;
import net.rasanovum.becareful.light.LightFieldNetworking;
import net.rasanovum.becareful.portals.AncientPortalHandler;
import net.rasanovum.becareful.spawning.EndSpawnHandler;
import net.rasanovum.becareful.util.EnvironmentHazard;
import net.rasanovum.rosetta.registry.RegistrationContext;
import net.rasanovum.rosetta.util.GameRuleCompat;
import net.rasanovum.rosetta.util.RegistryCompat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeCareful /*? if fabric {*/ implements ModInitializer /*?}*/ {
	public static final String MOD_ID = "be_careful";
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

	public static final ResourceKey<DamageType> CORRUPTION_DAMAGE_TYPE =
			ResourceKey.create(Registries.DAMAGE_TYPE, RegistryCompat.getLocation(MOD_ID, "corruption"));

	public static final TagKey<Item> FIRE_RESISTANT_FOODS =
			TagKey.create(Registries.ITEM, RegistryCompat.getLocation(MOD_ID, "fire_resistant_foods"));

	public static EnvironmentHazard DEEP_DARK;
	public static EnvironmentHazard NETHER;

	private static final GameRuleCompat GAME_RULES = new GameRuleCompat(MOD_ID);
	private static boolean gameRulesRegistered;
	public static GameRuleCompat.Key<Boolean> RULE_ONLY_RUINED_PORTALS;
	public static GameRuleCompat.Key<Integer> RULE_CHUNK_TAME_TIME;
	public static GameRuleCompat.Key<Boolean> RULE_DO_PORTAL_DEBUG;

	// deep dark
	public static int DD_ENTRY_VARIANTS;
	public static int DD_WARN_TICKS;
	public static int DD_WARN_VARIANTS;
	public static int DD_DANGER_TICKS;
	public static int TOTEM_VARIANTS;

	// nether
	public static int N_ENTRY_VARIANTS;
	public static int N_WARN_VARIANTS;
	public static int N_WARN_TICKS;
	public static int N_DANGER_TICKS;

	// chunk tame
	public static int CHUNK_TAME_VARIANTS;

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/*? if fabric {*/
	@Override
	/*?}*/
	public void onInitialize() {
		/*? if fabric {*/
		BeCarefulContent.REGISTRAR.register(RegistrationContext.create());
		/*?}*/
		initializeCommon();
	}

	public static void initializeCommon() {
		MidnightConfig.init(MOD_ID, BeCarefulConfig.class);
		refreshConfigValues();

		CORRUPTION = BeCarefulContent.CORRUPTION.get();
		TOTEM_OF_LIGHT = BeCarefulContent.TOTEM_OF_LIGHT.get();
		ECHO_SHARD_DUST = BeCarefulContent.ECHO_SHARD_DUST.get();
		LOST_KEY = BeCarefulContent.LOST_KEY.get();
		FROZEN_CORE = BeCarefulContent.FROZEN_CORE.get();
		FROZEN_CAMPFIRE = BeCarefulContent.FROZEN_CAMPFIRE.block().get();
		FROZEN_CAMPFIRE_ITEM = BeCarefulContent.FROZEN_CAMPFIRE.item().get();
		FROZEN_CAMPFIRE_ENTITY_TYPE = BeCarefulContent.FROZEN_CAMPFIRE_ENTITY.get();
		/*? if >=1.21 {*/
		CORRUPTION_HOLDER = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(CORRUPTION);
		/*?}*/

		/*? if fabric {*/
		UseItemCallback.EVENT.register(EndSpawnHandler::onUseEnderEye);
		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(EndSpawnHandler::onPlayerEnterEnd);

		if (BeCarefulConfig.doEndFeatures) {
			AncientPortalHandler.registerEvents();
		}

		/*?}*/

		registerGameRules();
		LightFieldNetworking.register();
		BeCarefulHooks.register();
		initializeHazards();


		LOGGER.info("Be Careful out there!");
	}

	public static void reloadConfig() {
		/*? if <1.21 {*/
		MidnightConfig.init(MOD_ID, BeCarefulConfig.class);
		/*?} else {*/
		MidnightConfig config = MidnightConfig.configInstances.get(MOD_ID);
		if (config != null) {
			config.loadValuesFromJson();
		}
		/*?}*/
		refreshConfigValues();
		initializeHazards();
	}

	private static void initializeHazards() {
		DEEP_DARK = null;
		NETHER = null;

		if (BeCarefulConfig.doDeepDarkFeatures) {
			DEEP_DARK = new EnvironmentHazard(
					"message.be_careful.deep_dark_entry",
					DD_ENTRY_VARIANTS,
					"message.be_careful.deep_dark_warning", DD_WARN_VARIANTS,
					DD_WARN_TICKS, DD_DANGER_TICKS,
					player -> {
						int playerTime = BeCarefulHooks.deepDarkTime(player.getUUID());
						int heartbeatRate = (playerTime > 550) ? 10 : 20;
						if ((player.level().getGameTime() % heartbeatRate) == 0) {
							player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
									SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.5f, 1.0f);
						}
					},
					CorruptionManager::expose
			);
		}

		if (BeCarefulConfig.doNetherFeatures) {
			NETHER = new EnvironmentHazard(
					"message.be_careful.nether_entry",
					N_ENTRY_VARIANTS,
					"message.be_careful.nether_warning", N_WARN_VARIANTS,
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
	}

	private static void refreshConfigValues() {
		DD_ENTRY_VARIANTS = BeCarefulConfig.deepDarkEntryVariants;
		DD_WARN_TICKS = BeCarefulConfig.deepDarkWarningTicks;
		DD_WARN_VARIANTS = BeCarefulConfig.deepDarkWarningVariants;
		DD_DANGER_TICKS = BeCarefulConfig.deepDarkDangerTicks;
		TOTEM_VARIANTS = BeCarefulConfig.totemVariants;
		N_ENTRY_VARIANTS = BeCarefulConfig.netherEntryVariants;
		N_WARN_VARIANTS = BeCarefulConfig.netherWarningVariants;
		N_WARN_TICKS = BeCarefulConfig.netherWarningTicks;
		N_DANGER_TICKS = BeCarefulConfig.netherDangerTicks;
		CHUNK_TAME_VARIANTS = BeCarefulConfig.chunkTameVariants;
	}

	public static synchronized void registerGameRules() {
		if (gameRulesRegistered) return;
		RULE_ONLY_RUINED_PORTALS = GAME_RULES.registerBool("onlyRuinedPortals", GameRuleCompat.Category.PLAYER, true);
		RULE_CHUNK_TAME_TIME = GAME_RULES.registerInt("chunkTameTime", GameRuleCompat.Category.PLAYER, 72000);
		RULE_DO_PORTAL_DEBUG = GAME_RULES.registerBool("doPortalDebug", GameRuleCompat.Category.PLAYER, false);
		gameRulesRegistered = true;
	}
}
