package mrriegel.flexibletools;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import mrriegel.flexibletools.item.ITool;
import mrriegel.limelib.helper.RecipeHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CompoundIngredient;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class ModRecipes {

	public static void init() {
		RecipeHelper.addShapelessRecipe(new ItemStack(ModItems.multi), ModItems.pick, ModItems.axe, ModItems.shovel, Blocks.SLIME_BLOCK);
		RecipeHelper.addShapedRecipe(new ItemStack(ModItems.sword), "qqq", "ltl", "gog", 't', Items.DIAMOND_SWORD, 'q', Items.QUARTZ, 'l', new ItemStack(Items.DYE, 1, 4), 'g', Items.GOLD_INGOT, 'o', Blocks.OBSIDIAN);
		RecipeHelper.addShapedRecipe(new ItemStack(ModItems.pick), "qqq", "ltl", "gog", 't', Items.DIAMOND_PICKAXE, 'q', Items.QUARTZ, 'l', new ItemStack(Items.DYE, 1, 4), 'g', Items.GOLD_INGOT, 'o', Blocks.OBSIDIAN);
		RecipeHelper.addShapedRecipe(new ItemStack(ModItems.axe), "qqq", "ltl", "gog", 't', Items.DIAMOND_AXE, 'q', Items.QUARTZ, 'l', new ItemStack(Items.DYE, 1, 4), 'g', Items.GOLD_INGOT, 'o', Blocks.OBSIDIAN);
		RecipeHelper.addShapedRecipe(new ItemStack(ModItems.shovel), "qqq", "ltl", "gog", 't', Items.DIAMOND_SHOVEL, 'q', Items.QUARTZ, 'l', new ItemStack(Items.DYE, 1, 4), 'g', Items.GOLD_INGOT, 'o', Blocks.OBSIDIAN);
		RecipeHelper.add(new Repair(ModItems.sword));
		RecipeHelper.add(new Repair(ModItems.pick));
		RecipeHelper.add(new Repair(ModItems.axe));
		RecipeHelper.add(new Repair(ModItems.shovel));
		RecipeHelper.add(new Repair(ModItems.multi));
		RecipeHelper.addShapedRecipe(new ItemStack(ModItems.upgrade_area, 1, 0), " f ", "dbd", " i ", 'f', Items.FLINT, 'd', "gemDiamond", 'b', "ingotBrick", 'i', "ingotIron");
		RecipeHelper.addShapedRecipe(new ItemStack(ModItems.upgrade_area, 1, 1), " f ", "dbd", " i ", 'f', Items.FLINT, 'd', "gemDiamond", 'b', "ingotBrick", 'i', "ingotGold");
		RecipeHelper.addShapedRecipe(new ItemStack(ModItems.upgrade_area, 1, 2), " f ", "dbd", " i ", 'f', Items.FLINT, 'd', "gemDiamond", 'b', "ingotBrick", 'i', "gemQuartz");
		RecipeHelper.addShapedRecipe(new ItemStack(ModItems.upgrade_area, 1, 3), " r ", "dbl", " c ", 'r', "oreRedstone", 'd', "oreDiamond", 'b', "ingotBrick", 'l', "oreLapis", 'c', "oreCoal");
		RecipeHelper.addShapedRecipe(new ItemStack(ModItems.upgrade_transport, 1, 0), " f ", "dbd", " i ", 'f', "enderpearl", 'd', Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, 'b', "ingotBrick", 'i', Blocks.HOPPER);
		RecipeHelper.addShapedRecipe(new ItemStack(ModItems.upgrade_transport, 1, 1), " f ", "dbd", " i ", 'f', "gemDiamond", 'd', "enderpearl", 'b', "ingotBrick", 'i', "obsidian");
		RecipeHelper.addShapedRecipe(new ItemStack(ModItems.upgrade_effect, 1, 0), " f ", "dbd", " i ", 'f', "dustGlowstone", 'd', Items.SPIDER_EYE, 'b', "ingotBrick", 'i', new ItemStack(Items.POTIONITEM, 1, OreDictionary.WILDCARD_VALUE));
		RecipeHelper.addShapedRecipe(new ItemStack(ModItems.upgrade_effect, 1, 1), " f ", "dbl", " i ", 'f', "dustGlowstone", 'd', Items.MAGMA_CREAM, 'b', "ingotBrick", 'i', new ItemStack(Items.POTIONITEM, 1, OreDictionary.WILDCARD_VALUE), 'l', Items.LAVA_BUCKET);
		RecipeHelper.addShapedRecipe(new ItemStack(ModItems.upgrade_effect, 1, 2), " f ", "dbd", " i ", 'f', "dustGlowstone", 'd', Items.FERMENTED_SPIDER_EYE, 'b', "ingotBrick", 'i', new ItemStack(Items.POTIONITEM, 1, OreDictionary.WILDCARD_VALUE));
		RecipeHelper.addShapedRecipe(new ItemStack(ModItems.upgrade_effect, 1, 3), " f ", "dbd", " i ", 'f', "dustGlowstone", 'd', new ItemStack(Items.SKULL, 1, 1), 'b', "ingotBrick", 'i', new ItemStack(Items.POTIONITEM, 1, OreDictionary.WILDCARD_VALUE));
		RecipeHelper.addShapedRecipe(new ItemStack(ModItems.upgrade_effect, 1, 4), " f ", "dbd", " i ", 'f', "dustGlowstone", 'd', Items.SPECKLED_MELON, 'b', "ingotBrick", 'i', new ItemStack(Items.POTIONITEM, 1, OreDictionary.WILDCARD_VALUE));
		RecipeHelper.addShapedRecipe(new ItemStack(ModItems.upgrade_support, 1, 0), " f ", "dbd", " f ", 'f', Items.FLINT, 'd', "blockQuartz", 'b', "ingotBrick");
		RecipeHelper.addShapedRecipe(new ItemStack(ModItems.upgrade_support, 1, 1), " f ", "dbd", " f ", 'f', Items.GLOWSTONE_DUST, 'd', "blockRedstone", 'b', "ingotBrick");
		RecipeHelper.addShapedRecipe(new ItemStack(ModItems.upgrade_support, 1, 2), " f ", "dbd", " f ", 'f', Items.GLOWSTONE_DUST, 'd', "blockLapis", 'b', "ingotBrick");
		RecipeHelper.addShapedRecipe(new ItemStack(ModItems.upgrade_support, 1, 3), " f ", "dbd", " e ", 'f', new ItemStack(Blocks.WOOL, 1, OreDictionary.WILDCARD_VALUE), 'd', "blockSlime", 'b', "ingotBrick", 'e', "gemEmerald");
		RecipeHelper.addShapedRecipe(new ItemStack(ModItems.upgrade_support, 1, 4), " f ", "dbd", " d ", 'f', Items.FLINT, 'd', new ItemStack(Items.SKULL, 1, OreDictionary.WILDCARD_VALUE), 'b', "ingotBrick");
		RecipeHelper.addShapedRecipe(new ItemStack(ModItems.upgrade_support, 1, 5), " f ", "dbd", " g ", 'f', "slimeball", 'd', new ItemStack(Blocks.WOOL, 1, OreDictionary.WILDCARD_VALUE), 'b', "ingotBrick", 'g', "gemDiamond");
		RecipeHelper.addShapedRecipe(new ItemStack(ModItems.upgrade_support, 1, 6), " f ", "dbd", " g ", 'f', "nuggetGold", 'd', Items.GOLDEN_CARROT, 'b', "ingotBrick", 'g', Items.GOLDEN_APPLE);
		RecipeHelper.addShapedRecipe(new ItemStack(ModItems.upgrade_support, 1, 7), " f ", "dbd", " f ", 'f', "ingotGold", 'd', "blockRedstone", 'b', "ingotBrick");
		RecipeHelper.addShapedRecipe(new ItemStack(ModItems.upgrade_skill, 1, 0), " g ", "gbg", " g ", 'b', "ingotBrick", 'g', "blockGlass");
		RecipeHelper.addShapedRecipe(new ItemStack(ModItems.upgrade_skill, 1, 1), " a ", "gbg", " o ", 'b', "ingotBrick", 'g', Items.FLINT_AND_STEEL, 'a', Items.ARROW, 'o', Items.BOW);
		RecipeHelper.addShapedRecipe(new ItemStack(ModItems.upgrade_skill, 1, 2), " a ", "gbg", " o ", 'b', "ingotBrick", 'g', "gemQuartz", 'a', "enderpearl", 'o', Items.ENDER_EYE);
		RecipeHelper.addShapedRecipe(new ItemStack(ModItems.upgrade_skill, 1, 3), " a ", "gbg", " o ", 'b', "ingotBrick", 'g', "blockGlass", 'a', "chestWood", 'o', Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE);
		RecipeHelper.addShapedRecipe(new ItemStack(ModItems.upgrade_skill, 1, 4), " a ", "gbg", " o ", 'b', "ingotBrick", 'g', "obsidian", 'a', "blockDiamond", 'o', "gemEmerald");
	}

	private static class Repair extends ShapelessOreRecipe {

		public Repair(Item tool) {
			super(new ResourceLocation(FlexibleTools.MODID, tool.getRegistryName().getResourcePath()), ((Function<Item, NonNullList<Ingredient>>) t -> {
				NonNullList<Ingredient> list = NonNullList.create();
				list.add(Ingredient.fromStacks(new ItemStack(t, 1, OreDictionary.WILDCARD_VALUE)));
				Collection<Ingredient> ar = ToolHelper.repairMap.keySet().stream().sorted((i1, i2) -> i1.getRegistryName().toString().compareTo(i2.getRegistryName().toString())).map(Ingredient::fromItem).collect(Collectors.toList());
				CompoundIngredient ci = null;
				try {
					ci = ReflectionHelper.findConstructor(CompoundIngredient.class, Collection.class).newInstance(ar);
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new RuntimeException(e);
				}
				list.add(ci);
				return list;
			}).apply(tool), new ItemStack(tool));
			isSimple = false;
		}

		@Override
		public ItemStack getCraftingResult(InventoryCrafting var1) {
			ItemStack tool = ItemStack.EMPTY;
			Item repair = null;
			for (int i = 0; i < var1.getSizeInventory(); i++) {
				ItemStack slot = var1.getStackInSlot(i);
				if (slot.getItem() instanceof ITool)
					tool = slot.copy();
				else if (!slot.isEmpty())
					repair = slot.getItem();
			}
			if (!tool.isEmpty() && tool.getMetadata() == 0)
				return ItemStack.EMPTY;
			if (!tool.isEmpty() && repair != null)
				ToolHelper.damageItem(-ToolHelper.repairMap.get(repair), null, tool, null);
			return tool;
		}

	}

}
