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

}
