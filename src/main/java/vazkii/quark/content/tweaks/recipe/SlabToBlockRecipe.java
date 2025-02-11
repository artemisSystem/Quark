package vazkii.quark.content.tweaks.recipe;

import java.util.Optional;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.Level;
import vazkii.quark.content.tweaks.module.SlabsToBlocksModule;

public class SlabToBlockRecipe extends CustomRecipe {
	
	public static final SimpleRecipeSerializer<?> SERIALIZER = new SimpleRecipeSerializer<>(SlabToBlockRecipe::new);
	private boolean locked = false;
	
	public SlabToBlockRecipe(ResourceLocation id) {
		super(id);
	}

	@Override
	public boolean matches(CraftingContainer container, Level level) {
		if(locked)
			return false;
		
		Item target = null;
		
		for(int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if(!stack.isEmpty()) {
				Item item = stack.getItem();
				
				if(target != null)
					return item == target && checkForOtherRecipes(container, level);
				
				if(SlabsToBlocksModule.recipes.containsKey(item)) {
					target = item;
				} else return false;
			}
		}
		
		return false;
	}
	
	// very much doubt multiple threads would ever touch this but JUST IN CASE
	private synchronized boolean checkForOtherRecipes(CraftingContainer container, Level level) {
		locked = true;
		boolean ret = false;
		MinecraftServer server = level.getServer();
		if(server != null) {
			Optional<CraftingRecipe> optional = server.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, container, level);
			ret = !optional.isPresent();
		}
		
		locked = false;
		return ret;
	}

	@Override
	public ItemStack assemble(CraftingContainer container) {
		for(int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if(!stack.isEmpty()) {
				Item item = stack.getItem();
				
				if(SlabsToBlocksModule.recipes.containsKey(item))
					return new ItemStack(SlabsToBlocksModule.recipes.get(item));
			}
		}
		
		return ItemStack.EMPTY;
	}

	@Override
	public boolean isSpecial() {
		return true;
	}
	
	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return (width * height) >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}



}
