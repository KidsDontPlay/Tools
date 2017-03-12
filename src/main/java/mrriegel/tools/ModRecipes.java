package mrriegel.tools;

import mrriegel.limelib.recipe.ShapelessRecipeExt;
import mrriegel.tools.item.ITool;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;

import com.google.common.collect.Lists;

public class ModRecipes {

	public static void init() {
		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.multi),ModItems. pick, ModItems.axe, ModItems.shovel, Blocks.SLIME_BLOCK);
		GameRegistry.addShapedRecipe(new ItemStack(ModItems.sword), "qqq", "ltl", "gog", 't', Items.DIAMOND_SWORD, 'q', Items.QUARTZ, 'l', new ItemStack(Items.DYE, 1, 4), 'g', Items.GOLD_INGOT, 'o', Blocks.OBSIDIAN);
		GameRegistry.addShapedRecipe(new ItemStack(ModItems.pick), "qqq", "ltl", "gog", 't', Items.DIAMOND_PICKAXE, 'q', Items.QUARTZ, 'l', new ItemStack(Items.DYE, 1, 4), 'g', Items.GOLD_INGOT, 'o', Blocks.OBSIDIAN);
		GameRegistry.addShapedRecipe(new ItemStack(ModItems.axe), "qqq", "ltl", "gog", 't', Items.DIAMOND_AXE, 'q', Items.QUARTZ, 'l', new ItemStack(Items.DYE, 1, 4), 'g', Items.GOLD_INGOT, 'o', Blocks.OBSIDIAN);
		GameRegistry.addShapedRecipe(new ItemStack(ModItems.shovel), "qqq", "ltl", "gog", 't', Items.DIAMOND_SHOVEL, 'q', Items.QUARTZ, 'l', new ItemStack(Items.DYE, 1, 4), 'g', Items.GOLD_INGOT, 'o', Blocks.OBSIDIAN);
		RecipeSorter.register(Tools.MODID + ":repairTool", Repair.class, Category.SHAPELESS, "after:minecraft:shapeless");
		GameRegistry.addRecipe(new Repair(ModItems.sword));
		GameRegistry.addRecipe(new Repair(ModItems.pick));
		GameRegistry.addRecipe(new Repair(ModItems.axe));
		GameRegistry.addRecipe(new Repair(ModItems.shovel));
		GameRegistry.addRecipe(new Repair(ModItems.multi));
	}
	
	private static class Repair extends ShapelessRecipeExt {

		public Repair(Item tool) {
			super(new ItemStack(tool), new ItemStack(tool, 1, OreDictionary.WILDCARD_VALUE), Lists.newArrayList(ToolHelper.repairMap.keySet()));
		}

		@Override
		public ItemStack getCraftingResult(InventoryCrafting var1) {
			ItemStack tool = null;
			Item repair = null;
			for (int i = 0; i < var1.getSizeInventory(); i++) {
				ItemStack slot = var1.getStackInSlot(i);
				if (slot.getItem() instanceof ITool)
					tool = slot.copy();
				else if (!slot.isEmpty())
					repair = slot.getItem();
			}
			ToolHelper.damageItem(-ToolHelper.repairMap.get(repair), null, tool,null);
			return tool;
		}

	}

}
