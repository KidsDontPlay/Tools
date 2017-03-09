package mrriegel.tools;

import java.util.Map;

import mrriegel.limelib.item.CommonItem;
import mrriegel.limelib.recipe.ShapelessRecipeExt;
import mrriegel.tools.item.ITool;
import mrriegel.tools.item.ItemAxe;
import mrriegel.tools.item.ItemAxpickvel;
import mrriegel.tools.item.ItemPick;
import mrriegel.tools.item.ItemShovel;
import mrriegel.tools.item.ItemSword;
import mrriegel.tools.item.ItemToolUpgrade;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ModItems {

	public static final Item sword = new ItemSword();
	public static final CommonItem pick = new ItemPick();
	public static final CommonItem axe = new ItemAxe();
	public static final Item shovel = new ItemShovel();
	public static final CommonItem multi = new ItemAxpickvel();
	public static final CommonItem upgrade_area = new ItemToolUpgrade("area");
	public static final CommonItem upgrade_transport = new ItemToolUpgrade("transport");
	public static final CommonItem upgrade_effect = new ItemToolUpgrade("effect");
	public static final CommonItem upgrade_support = new ItemToolUpgrade("support");
	public static final CommonItem upgrade_skill = new ItemToolUpgrade("skill");

	public static void init() {
		register(sword);
		pick.registerItem();
		axe.registerItem();
//		shovel.registerItem();
		register(shovel);
		multi.registerItem();
		upgrade_area.registerItem();
		upgrade_transport.registerItem();
		upgrade_effect.registerItem();
		upgrade_support.registerItem();
		upgrade_skill.registerItem();
		GameRegistry.addShapelessRecipe(new ItemStack(multi), pick, axe, shovel, Blocks.SLIME_BLOCK);
		GameRegistry.addShapedRecipe(new ItemStack(sword), "qqq", "ltl", "gog", 't', Items.DIAMOND_SWORD, 'q', Items.QUARTZ, 'l', new ItemStack(Items.DYE, 1, 4), 'g', Items.GOLD_INGOT, 'o', Blocks.OBSIDIAN);
		GameRegistry.addShapedRecipe(new ItemStack(pick), "qqq", "ltl", "gog", 't', Items.DIAMOND_PICKAXE, 'q', Items.QUARTZ, 'l', new ItemStack(Items.DYE, 1, 4), 'g', Items.GOLD_INGOT, 'o', Blocks.OBSIDIAN);
		GameRegistry.addShapedRecipe(new ItemStack(axe), "qqq", "ltl", "gog", 't', Items.DIAMOND_AXE, 'q', Items.QUARTZ, 'l', new ItemStack(Items.DYE, 1, 4), 'g', Items.GOLD_INGOT, 'o', Blocks.OBSIDIAN);
		GameRegistry.addShapedRecipe(new ItemStack(shovel), "qqq", "ltl", "gog", 't', Items.DIAMOND_SHOVEL, 'q', Items.QUARTZ, 'l', new ItemStack(Items.DYE, 1, 4), 'g', Items.GOLD_INGOT, 'o', Blocks.OBSIDIAN);
		RecipeSorter.register(Tools.MODID + ":repairTool", Repair.class, Category.SHAPELESS, "after:minecraft:shapeless");
		GameRegistry.addRecipe(new Repair(sword));
		GameRegistry.addRecipe(new Repair(pick));
		GameRegistry.addRecipe(new Repair(axe));
		GameRegistry.addRecipe(new Repair(shovel));
		GameRegistry.addRecipe(new Repair(multi));
	}

	public static void initClient() {
		initModel(sword);
		pick.initModel();
		axe.initModel();
//		shovel.initModel();
		initModel(shovel);
		multi.initModel();
		upgrade_area.initModel();
		upgrade_transport.initModel();
		upgrade_effect.initModel();
		upgrade_support.initModel();
		upgrade_skill.initModel();
	}

	private static void register(Item item) {
		GameRegistry.register(item);
	}

	private static void initModel(Item item) {
		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
	}

	public static class Repair extends ShapelessRecipeExt {

		public static Map<Item, Integer> map = Maps.newHashMap();
		static {
			map.put(Items.DIAMOND, 300);
			map.put(Items.GOLD_INGOT, 35);
			map.put(Item.getItemFromBlock(Blocks.OBSIDIAN), 40);
			map.put(Item.getItemFromBlock(Blocks.GOLD_BLOCK), map.get(Items.GOLD_INGOT) * 10);
			map.put(Item.getItemFromBlock(Blocks.DIAMOND_BLOCK), map.get(Items.DIAMOND) * 10);
		}

		public Repair(Item tool) {
			super(new ItemStack(tool), new ItemStack(tool, 1, OreDictionary.WILDCARD_VALUE), Lists.newArrayList(map.keySet()));
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
			ToolHelper.damageItem(-map.get(repair), null, tool);
			return tool;
		}

	}
}
