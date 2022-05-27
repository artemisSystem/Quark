package vazkii.quark.content.building.module;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.handler.VariantHandler;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.content.building.block.QuarkSandstoneBlock;
import vazkii.quark.content.building.block.QuarkSmoothSandstoneBlock;

@LoadModule(category = ModuleCategory.BUILDING)
public class SoulSandstoneModule extends QuarkModule {

	@Override
	public void register() {
		Block.Properties props = Block.Properties.of(Material.STONE, MaterialColor.COLOR_BROWN)
				.requiresCorrectToolForDrops()
				.strength(0.8F);

		VariantHandler.addSlabStairsWall(new QuarkSandstoneBlock("soul_sandstone", this, CreativeModeTab.TAB_BUILDING_BLOCKS, props));
		new QuarkBlock("chiseled_soul_sandstone", this, CreativeModeTab.TAB_BUILDING_BLOCKS, props);
		VariantHandler.addSlab(new QuarkSandstoneBlock("cut_soul_sandstone", this, CreativeModeTab.TAB_BUILDING_BLOCKS, props));
		VariantHandler.addSlabAndStairs(new QuarkSmoothSandstoneBlock("smooth_soul_sandstone", this, CreativeModeTab.TAB_BUILDING_BLOCKS, props));
	}

}
