package vazkii.quark.content.mobs.module;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.Registry;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements.Type;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;
import vazkii.arl.util.RegistryHelper;
import vazkii.quark.base.handler.EntityAttributeHandler;
import vazkii.quark.base.handler.advancement.QuarkAdvancementHandler;
import vazkii.quark.base.handler.advancement.QuarkGenericTrigger;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.module.config.type.CompoundBiomeConfig;
import vazkii.quark.base.module.config.type.DimensionConfig;
import vazkii.quark.base.module.config.type.EntitySpawnConfig;
import vazkii.quark.base.world.EntitySpawnHandler;
import vazkii.quark.content.mobs.client.render.entity.ToretoiseRenderer;
import vazkii.quark.content.mobs.entity.Toretoise;

@LoadModule(category = ModuleCategory.MOBS, hasSubscriptions = true)
public class ToretoiseModule extends QuarkModule {

	public static EntityType<Toretoise> toretoiseType;

	@Config public static int maxYLevel = 0;

	@Config(description="The number of ticks from mining a tortoise until feeding it could cause it to regrow.")
	public static int cooldownTicks = 20 * 60;

	@Config(description="The items that can be fed to toretoises to make them regrow ores.")
	public static List<String> foods = Lists.newArrayList("minecraft:glow_berries");

	@Config(flag = "toretoise_regrow")
	public static boolean allowToretoiseToRegrow = true;
	
	@Config(description="Feeding a toretoise after cooldown will regrow them with a one-in-this-number chance. "
			+ "Set to 1 to always regrow, higher = lower chance.")
	public static int regrowChance = 3;

	@Config
	public static DimensionConfig dimensions = DimensionConfig.overworld(false);

	@Config
	public static EntitySpawnConfig spawnConfig = new EntitySpawnConfig(120, 2, 4, CompoundBiomeConfig.fromBiomeTags(true, Tags.Biomes.IS_VOID, BiomeTags.IS_NETHER, BiomeTags.IS_END));

	public static QuarkGenericTrigger mineToretoiseTrigger;
	public static QuarkGenericTrigger mineFedToretoiseTrigger;
	
	@Override
	public void register() {
		toretoiseType = EntityType.Builder.of(Toretoise::new, MobCategory.CREATURE)
				.sized(2F, 1F)
				.clientTrackingRange(8)
				.fireImmune()
				.setCustomClientFactory((spawnEntity, world) -> new Toretoise(toretoiseType, world))
				.build("toretoise");

		RegistryHelper.register(toretoiseType, "toretoise", Registry.ENTITY_TYPE_REGISTRY);

		EntitySpawnHandler.registerSpawn(this, toretoiseType, MobCategory.MONSTER, Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, Toretoise::spawnPredicate, spawnConfig);
		EntitySpawnHandler.addEgg(toretoiseType, 0x55413b, 0x383237, spawnConfig);

		EntityAttributeHandler.put(toretoiseType, Toretoise::prepareAttributes);
		
		mineToretoiseTrigger = QuarkAdvancementHandler.registerGenericTrigger("mine_toretoise");
		mineFedToretoiseTrigger = QuarkAdvancementHandler.registerGenericTrigger("mine_fed_toretoise");
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientSetup() {
		EntityRenderers.register(toretoiseType, ToretoiseRenderer::new);
	}

}
