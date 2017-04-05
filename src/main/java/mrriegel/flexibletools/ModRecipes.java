package mrriegel.flexibletools;

import mrriegel.flexibletools.item.ITool;
import mrriegel.limelib.recipe.ShapelessRecipeExt;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;
import net.minecraftforge.oredict.ShapedOreRecipe;

import com.google.common.collect.Lists;

public class ModRecipes {

	public static void init() {
		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.multi), ModItems.pick, ModItems.axe, ModItems.shovel, Blocks.SLIME_BLOCK);
		GameRegistry.addShapedRecipe(new ItemStack(ModItems.sword), "qqq", "ltl", "gog", 't', Items.DIAMOND_SWORD, 'q', Items.QUARTZ, 'l', new ItemStack(Items.DYE, 1, 4), 'g', Items.GOLD_INGOT, 'o', Blocks.OBSIDIAN);
		GameRegistry.addShapedRecipe(new ItemStack(ModItems.pick), "qqq", "ltl", "gog", 't', Items.DIAMOND_PICKAXE, 'q', Items.QUARTZ, 'l', new ItemStack(Items.DYE, 1, 4), 'g', Items.GOLD_INGOT, 'o', Blocks.OBSIDIAN);
		GameRegistry.addShapedRecipe(new ItemStack(ModItems.axe), "qqq", "ltl", "gog", 't', Items.DIAMOND_AXE, 'q', Items.QUARTZ, 'l', new ItemStack(Items.DYE, 1, 4), 'g', Items.GOLD_INGOT, 'o', Blocks.OBSIDIAN);
		GameRegistry.addShapedRecipe(new ItemStack(ModItems.shovel), "qqq", "ltl", "gog", 't', Items.DIAMOND_SHOVEL, 'q', Items.QUARTZ, 'l', new ItemStack(Items.DYE, 1, 4), 'g', Items.GOLD_INGOT, 'o', Blocks.OBSIDIAN);
		RecipeSorter.register(FlexibleTools.MODID + ":repairTool", Repair.class, Category.SHAPELESS, "after:minecraft:shapeless");
		GameRegistry.addRecipe(new Repair(ModItems.sword));
		GameRegistry.addRecipe(new Repair(ModItems.pick));
		GameRegistry.addRecipe(new Repair(ModItems.axe));
		GameRegistry.addRecipe(new Repair(ModItems.shovel));
		GameRegistry.addRecipe(new Repair(ModItems.multi));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.upgrade_area, 1, 0), " f ", "dbd", " i ", 'f', Items.FLINT, 'd', "gemDiamond", 'b', "ingotBrick", 'i', "ingotIron"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.upgrade_area, 1, 1), " f ", "dbd", " i ", 'f', Items.FLINT, 'd', "gemDiamond", 'b', "ingotBrick", 'i', "ingotGold"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.upgrade_area, 1, 2), " f ", "dbd", " i ", 'f', Items.FLINT, 'd', "gemDiamond", 'b', "ingotBrick", 'i', "gemQuartz"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.upgrade_area, 1, 3), " r ", "dbl", " c ", 'r', "oreRedstone", 'd', "oreDiamond", 'b', "ingotBrick", 'l', "oreLapis", 'c', "oreCoal"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.upgrade_transport, 1, 0), " f ", "dbd", " i ", 'f', "enderpearl", 'd', Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, 'b', "ingotBrick", 'i', Blocks.HOPPER));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.upgrade_transport, 1, 1), " f ", "dbd", " i ", 'f', "gemDiamond", 'd', "enderpearl", 'b', "ingotBrick", 'i', "obsidian"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.upgrade_effect, 1, 0), " f ", "dbd", " i ", 'f', "dustGlowstone", 'd', Items.SPIDER_EYE, 'b', "ingotBrick", 'i', new ItemStack(Items.POTIONITEM, 1, OreDictionary.WILDCARD_VALUE)));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.upgrade_effect, 1, 1), " f ", "dbl", " i ", 'f', "dustGlowstone", 'd', Items.MAGMA_CREAM, 'b', "ingotBrick", 'i', new ItemStack(Items.POTIONITEM, 1, OreDictionary.WILDCARD_VALUE), 'l', Items.LAVA_BUCKET));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.upgrade_effect, 1, 2), " f ", "dbd", " i ", 'f', "dustGlowstone", 'd', Items.FERMENTED_SPIDER_EYE, 'b', "ingotBrick", 'i', new ItemStack(Items.POTIONITEM, 1, OreDictionary.WILDCARD_VALUE)));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.upgrade_effect, 1, 3), " f ", "dbd", " i ", 'f', "dustGlowstone", 'd', new ItemStack(Items.SKULL, 1, 1), 'b', "ingotBrick", 'i', new ItemStack(Items.POTIONITEM, 1, OreDictionary.WILDCARD_VALUE)));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.upgrade_effect, 1, 4), " f ", "dbd", " i ", 'f', "dustGlowstone", 'd', Items.SPECKLED_MELON, 'b', "ingotBrick", 'i', new ItemStack(Items.POTIONITEM, 1, OreDictionary.WILDCARD_VALUE)));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.upgrade_support, 1, 0), " f ", "dbd", " f ", 'f', Items.FLINT, 'd', "blockQuartz", 'b', "ingotBrick"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.upgrade_support, 1, 1), " f ", "dbd", " f ", 'f', Items.GLOWSTONE_DUST, 'd', "blockRedstone", 'b', "ingotBrick"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.upgrade_support, 1, 2), " f ", "dbd", " f ", 'f', Items.GLOWSTONE_DUST, 'd', "blockLapis", 'b', "ingotBrick"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.upgrade_support, 1, 3), " f ", "dbd", " e ", 'f', new ItemStack(Blocks.WOOL, 1, OreDictionary.WILDCARD_VALUE), 'd', "blockSlime", 'b', "ingotBrick", 'e', "gemEmerald"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.upgrade_support, 1, 4), " f ", "dbd", " d ", 'f', Items.FLINT, 'd', new ItemStack(Items.SKULL, 1, OreDictionary.WILDCARD_VALUE), 'b', "ingotBrick"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.upgrade_support, 1, 5), " f ", "dbd", " g ", 'f', "slimeball", 'd', new ItemStack(Blocks.WOOL, 1, OreDictionary.WILDCARD_VALUE), 'b', "ingotBrick", 'g', "gemDiamond"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.upgrade_support, 1, 6), " f ", "dbd", " g ", 'f', "nuggetGold", 'd', Items.GOLDEN_CARROT, 'b', "ingotBrick", 'g', Items.GOLDEN_APPLE));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.upgrade_support, 1, 7), " f ", "dbd", " f ", 'f', "ingotGold", 'd', "blockRedstone", 'b', "ingotBrick"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.upgrade_skill, 1, 0), " g ", "gbg", " g ", 'b', "ingotBrick", 'g', "blockGlass"));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.upgrade_skill, 1, 1), " a ", "gbg", " o ", 'b', "ingotBrick", 'g', Items.FLINT_AND_STEEL, 'a', Items.ARROW, 'o', Items.BOW));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.upgrade_skill, 1, 2), " a ", "gbg", " o ", 'b', "ingotBrick", 'g', "gemQuartz", 'a', "enderpearl", 'o', Items.ENDER_EYE));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.upgrade_skill, 1, 3), " a ", "gbg", " o ", 'b', "ingotBrick", 'g', "blockGlass", 'a', "chestWood", 'o', Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.upgrade_skill, 1, 4), " a ", "gbg", " o ", 'b', "ingotBrick", 'g', "obsidian", 'a', "blockDiamond", 'o', "gemEmerald"));
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
			ToolHelper.damageItem(-ToolHelper.repairMap.get(repair), null, tool, null);
			return tool;
		}

	}

}